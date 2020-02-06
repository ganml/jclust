package clustering.algorithm;

import java.io.*;
import java.util.*;

import clustering.cluster.*;
import clustering.distance.Distance;
import clustering.util.DoubleIntPair;

public class Mcl extends ClusteringAlgorithm {
	protected double _e;
	protected double _r;
	protected double _delta;
	protected double[][] D;
	protected double[][] M;
	protected Distance _distance;
	protected int _maxiter;
	protected int _conviter;
	
	// results
	protected List<Cluster> lstcluster;
	protected int[] vExemplar;
	protected int numiter;

	@Override
	protected void work() throws Exception {
		initializeM();
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
		_e = arguments.getReal("e");		
		_r = arguments.getReal("r");
		_delta = arguments.getReal("delta");
		_maxiter = arguments.getInt("maxiter");
		_conviter = arguments.getInt("conviter");
		
		String distName = arguments.getStr("distance");
		Class<?> clazz = Class.forName("clustering.distance."+distName);	
		_distance = (Distance) clazz.newInstance();
		_distance.setArguments(arguments);
	}
	
	protected void initializeM() {
		vExemplar = new int[nRecord];
		D = new double[nRecord][nRecord];
		M = new double[nRecord][nRecord];
		double dMax = Double.MIN_VALUE;
		for(int i=0; i<nRecord; ++i) {
			for(int j=0; j<i; ++j) {
				D[i][j] = _distance.dist(ds.get(i), ds.get(j));
				D[j][i] = D[i][j];
				if(dMax < D[i][j]) {
					dMax = D[i][j];
				}
			}			
		}
		
		if(_delta == 0) {
			List<Double> lstTemp = new ArrayList<Double>();
			for(int i=0; i<nRecord; ++i) {
				for(int j=0; j<i; ++j) {
					lstTemp.add(D[i][j]);
					lstTemp.add(D[j][i]);
				}
			}
			Collections.sort(lstTemp);
			int nMid = nRecord * (nRecord-1) / 2;
			_delta = (lstTemp.get(nMid-1) + lstTemp.get(nMid)) / 2;
		}
		/*/
		for(int j=0; j<_ds.size(); ++j) {
			double dSum = 0.0;
			for(int i=0; i<_ds.size(); ++i) {
				dSum += dMax - D[i][j];
			}
			for(int i=0; i<_ds.size(); ++i) {
				M[i][j] = (dMax - D[i][j]) / dSum;
			}		
		}
		/*/
		for(int j=0; j<nRecord; ++j) {
			double dSum = 0.0;
			for(int i=0; i<nRecord; ++i) {
				if(D[i][j] < _delta) {
					dSum += 1;
					M[i][j] = 1;
				}
			}
			for(int i=0; i<nRecord; ++i) {
				M[i][j] = M[i][j] / dSum;
			}		
		}
		/*/
				
		try{			
			save("D.csv", D);			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		//*/
	}
	
	protected void iteration() {
		numiter = 1;
		int nChanges = 0;
		
		List<DoubleIntPair> lstPair = new ArrayList<DoubleIntPair>();
		for(int i=0; i<nRecord; ++i) {
			lstPair.add(new DoubleIntPair());
		}		
		//double[][] mTemp = new double[N][N];
		double[][] C;
		//double[] bcolj = new double[N];
		while(true) {
			// weight propagation

			C = new double[nRecord][nRecord];
	        for (int i = 0; i < nRecord; i++) {
	            double[] arowi = M[i];
	            double[] crowi = C[i];
	            for (int k = 0; k < nRecord; k++) {
	                double[] browk = M[k];
	                double aik = arowi[k];	                
	                for (int j = 0; j < nRecord; j++) {
	                    crowi[j] += aik * browk[j];
	                }
	            }
	        }
	        
	        // inflation
	        for(int i=0; i<nRecord; ++i) {
	        	double dSum = 0.0;	        	
	        	for (int k = 0; k < nRecord; k++) {
	        		dSum += Math.pow(C[k][i], _r);
	        	}
	        	for (int k = 0; k < nRecord; k++) {
	        		C[k][i] = Math.pow(C[k][i], _r) / dSum;
	        	}
	        }
	        //*/
	        	        
	        M = C;	     

			/*
			try{
				saveW(String.format("w%d.csv",numiter));				
			} catch(Exception ex) {
				ex.printStackTrace();
			}*/
			
			// check exemplar
			int[] vExemplarOld = vExemplar.clone();
			calculateCM();			
			
			int nTemp = 0;
			for(int i=0; i<nRecord; ++i) {
				if(vExemplarOld[i] != vExemplar[i]) {
					nTemp++;
				}
			}			
			
			if(nTemp>0) {
				nChanges = 0;
			} else {
				nChanges++;
			}
						
			if(nChanges >= _conviter) {
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
				lstPair.get(k).set(M[k][i], k);
			}
			
			Collections.sort(lstPair);
			vExemplar[i] = lstPair.get(0).getIndex();
		}
	}
	
	public void saveM(String filename) throws IOException {
		save(filename, M);

	}
}
