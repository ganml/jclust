package clustering.algorithm;

import java.io.*;
import java.util.*;

import clustering.cluster.*;
import clustering.distance.Distance;
import clustering.util.*;

public class Pp extends ClusteringAlgorithm {
	protected double[][] _w;
	protected double[][] _w0;
	protected double _delta;
	protected int _s;
	protected Distance _distance;
	protected int _maxiter;
	protected int _conviter;
	protected double[][] D;
	protected Kernel _kernel;
	
	// results
	protected List<Cluster> lstcluster;
	protected int[] vExemplar;
	protected double dobj;
	protected int numiter;

	@Override
	protected void work() throws Exception {
		initializeW();
		iteration();
	}

	@Override
	protected void fetchResults() throws Exception {
		lstcluster = new ArrayList<Cluster>();
		Set<Integer> exemplarSet = new HashSet<Integer>();
		for(int i=0; i<nRecord; ++i) {
			exemplarSet.add(vExemplar[i]);
		}
		Map<Integer, Integer> mTemp = new HashMap<Integer, Integer>();		
		for(int k : exemplarSet) {
			Cluster c = new Cluster(String.format("C%d", k+1));			
			mTemp.put(k, lstcluster.size());
			lstcluster.add(c);
		}
		for(int i=0; i<nRecord; ++i) {
			lstcluster.get(mTemp.get(vExemplar[i])).add(ds.get(i));
		}
		
		PartitionClustering pc = new PartitionClustering(ds, lstcluster);
		  
        results.insert("pc", pc);
        results.insert("numiter", new Integer(numiter));        
	}

	protected void setupArguments() throws Exception {
		super.setupArguments();
		_delta = arguments.getReal("delta");
		_s = arguments.getInt("s");
		_maxiter = arguments.getInt("maxiter");
		_conviter = arguments.getInt("conviter");
		
		String distName = arguments.getStr("distance");
		Class<?> clazz = Class.forName("clustering.distance."+distName);	
		_distance = (Distance) clazz.newInstance();
		_distance.setArguments(arguments);
		_distance.initialize();
		
		String knName = arguments.getStr("kernel");
		clazz = Class.forName("clustering.util."+knName);	
		_kernel = (Kernel) clazz.newInstance();
	}

	protected void calculateBandwidth() {	
		List<Double> listD = new ArrayList<Double>();
		for(int i=0; i<nRecord; ++i) {			
			for(int j=0; j<nRecord; ++j) {
				if(j==i) {
					continue;
				}				
				listD.add(D[i][j]);	
			}
		}		
						
		Collections.sort(listD);
		int[] vp = new int[]{2,6,10,20};
		StringBuilder sb = new StringBuilder();
		sb.append(System.getProperty("line.separator"));
		for(int i=1; i<=vp.length; ++i) {
			int ni = (int) Math.floor(listD.size() * vp[i-1] / 100.0);		
			sb.append(String.format("Q%d distance: %f", vp[i-1], listD.get(ni)));
			sb.append(System.getProperty("line.separator"));
		}						
		CommonFunction.log(sb.toString());
		
		_delta = listD.get((int) Math.floor(listD.size() * 0.05));
	}
		
	protected void initializeW() {		
		vExemplar = new int[nRecord];
		D = new double[nRecord][nRecord];
		_w0 = new double[nRecord][nRecord];
		for(int i=0; i<nRecord; ++i) {
			for(int j=0; j<i; ++j) {
				D[i][j] = _distance.dist(ds.get(i), ds.get(j));
				D[j][i] = D[i][j];
			}			
		}
		
		if(_delta == 0 ) {
			calculateBandwidth();
		}
		
		// count neighbors
		double[] vMse = new double[nRecord];
		for(int i=0; i<nRecord; ++i) {					
			for(int j=0; j<nRecord; ++j) {
				if(D[i][j] < _delta) {
					vMse[i] += _kernel.value(D[i][j] / _delta);					
				}
			}	
		}
		
		for(int i=0; i<nRecord; ++i) {			
			double dSum = 0;
			for(int j=0; j<nRecord; ++j) {				
				if(D[i][j] < _delta) {
					dSum += vMse[j];					
				}
			}
			
			List<DoubleIntPair> listPair = new ArrayList<DoubleIntPair>();
			for(int j=0; j<nRecord; ++j) {				
				if(D[i][j] < _delta) {					
					listPair.add(new DoubleIntPair(vMse[j] / dSum, j));
				}
			}
			Collections.sort(listPair);
			double dSum2 = 0.0;
			int kk = Math.min(_s, listPair.size());
			for(int j=0; j<kk; ++j) {
				dSum2 += listPair.get(j).getValue();
			}
			for(int j=0; j<kk; ++j) {
				_w0[i][listPair.get(j).getIndex()] = listPair.get(j).getValue() / dSum2;
			}
			
		}
				
		_w = _w0.clone();
		/*
		try{
			saveW("w0.csv");	
			saveD("D.csv");
		} catch(Exception ex) {
			ex.printStackTrace();
		}*/
	}
		
	public void saveW(String filename) throws IOException {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<nRecord; ++i) {
			sb.append(_w[i][0]);
			for(int k=1; k<nRecord; ++k) {
				sb.append(',').append(_w[i][k]);
			}
			sb.append(separator);
		}
		
		FileWriter outFile = new FileWriter(filename);
		PrintWriter out = new PrintWriter(outFile);
		out.print(sb.toString());
		out.close();

	}
	
	public void saveD(String filename) throws IOException {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<nRecord; ++i) {
			sb.append(D[i][0]);
			for(int k=1; k<nRecord; ++k) {
				sb.append(',').append(D[i][k]);
			}
			sb.append(separator);
		}
		
		FileWriter outFile = new FileWriter(filename);
		PrintWriter out = new PrintWriter(outFile);
		out.print(sb.toString());
		out.close();

	}
	
	protected void iteration() {
		numiter = 1;
				
		List<DoubleIntPair> lstPair = new ArrayList<DoubleIntPair>();
		for(int i=0; i<nRecord; ++i) {
			lstPair.add(new DoubleIntPair());
		}		
		double[][] C;

		while(true) {
			// weight propagation			
			C = new double[nRecord][nRecord];
	        for (int i = 0; i < nRecord; i++) {
	            double[] arowi = _w[i];
	            double[] crowi = C[i];
	            for (int k = 0; k < nRecord; k++) {
	                double[] browk = _w[k];
	                double aik = arowi[k];	                
	                for (int j = 0; j < nRecord; j++) {
	                    crowi[j] += aik * browk[j];
	                }
	            }
	        }
	        	        
	        _w = C;	     

			/*
			try{
				saveW(String.format("w%d.csv",numiter));				
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			//*/
			
			// check exemplar
			int[] vExemplarOld = vExemplar.clone();
			calculateCM();			
			
			int nTemp = 0;
			for(int i=0; i<nRecord; ++i) {
				if(vExemplarOld[i] != vExemplar[i]) {
					nTemp++;
				}
			}
			
			if(nTemp ==0) {
				break;
			}
			++numiter;
			if(numiter >= _maxiter) {
				break;
			}
			
		}	
	}
	
	private void calculateCM() {		
		List<DoubleIntPair> lstPair = new ArrayList<DoubleIntPair>();
		for(int i=0; i<nRecord; ++i) {
			lstPair.add(new DoubleIntPair());
		}
		for(int i=0; i<nRecord; ++i) {
			for(int k=0; k<nRecord; ++k) {
				lstPair.get(k).set(_w[i][k], k);
			}
			
			Collections.sort(lstPair);
			vExemplar[i] = lstPair.get(0).getIndex();
		}
	}
}
