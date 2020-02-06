package clustering.algorithm;

import java.io.*;
import java.util.*;

import clustering.cluster.*;
import clustering.distance.Distance;
import clustering.util.DoubleIntPair;

public class Ap extends ClusteringAlgorithm {
	// parameters
	protected int maxiter;
	protected int conviter;
	protected double preference;
	protected double lambda;
	protected Distance distance;
	protected double[][] S; // similarity
	protected double[][] R; // responsibility
	protected double[][] A; // availability
	
	// results
	protected List<Cluster> lstcluster;
	protected int[] vExemplar;
	protected double dobj;
	protected int numiter;


	@Override
	protected void work() throws Exception {
		S = new double[nRecord][nRecord];
		R = new double[nRecord][nRecord];
		A = new double[nRecord][nRecord];
		vExemplar = new int[nRecord];
				
		calculateSimilarity();
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
		preference = arguments.getReal("preference");		
		lambda = arguments.getReal("lambda");
		maxiter = arguments.getInt("maxiter");
		conviter = arguments.getInt("conviter");

		String distName = arguments.getStr("distance");
		Class<?> clazz = Class.forName("clustering.distance."+distName);	
		distance = (Distance) clazz.newInstance();
		distance.setArguments(arguments);
	}

	protected void calculateSimilarity() {
		for(int i=0; i<nRecord; ++i) {
			for(int j=0; j<i; ++j) {
				S[i][j] = -distance.dist(ds.get(i), ds.get(j));
				S[j][i] = S[i][j];
			}
			S[i][i] = preference; 
		}
		
		if(preference == 0) {
			List<Double> lstTemp = new ArrayList<Double>();
			for(int i=0; i<nRecord; ++i) {
				for(int j=0; j<i; ++j) {
					lstTemp.add(S[i][j]);
					lstTemp.add(S[j][i]);
				}
			}
			Collections.sort(lstTemp);
			int nMid = nRecord * (nRecord-1) / 2;
			preference = (lstTemp.get(nMid-1) + lstTemp.get(nMid)) / 2;
			
			for(int i=0; i<nRecord; ++i) {
				S[i][i] = preference;
			}
		}
		/*
		try{
			save("S.csv", S);
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
		while(true) {
			// compute responsibility
			for(int i=0; i<nRecord; ++i) {				
				for(int k=0; k<nRecord; ++k) {
					lstPair.get(k).set(A[i][k] + S[i][k], k);
				}
				Collections.sort(lstPair);
				
				int nMax1 = lstPair.get(0).getIndex();
				double dMax1 = lstPair.get(0).getValue();		
				for(int k=0; k<nRecord; ++k) {
					double dOld = R[i][k];
					if(k == nMax1) {
						R[i][k] = S[i][k] -  lstPair.get(1).getValue();
					} else {
						R[i][k] = S[i][k] -  dMax1;
					}					
					R[i][k] = lambda * dOld + (1-lambda) * R[i][k];
				}
			}
			
			// compute availability
			for(int k=0; k<nRecord; ++k) {
				double dSum = 0.0;
				for(int i=0; i<nRecord; ++i) {
					dSum += Math.max(0, R[i][k]);
				}
				dSum -= Math.max(0, R[k][k]) - R[k][k];
				for(int i=0; i<nRecord; ++i) {
					double dOld = A[i][k];
					if(i == k) {
						A[i][k] = dSum - R[k][k];
					} else {
						A[i][k] = Math.min(0, dSum - Math.max(0, R[i][k]));
					}
					
					A[i][k] = lambda * dOld + (1-lambda) * A[i][k];
				}
			}
			
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
						
			if(nChanges >= conviter) {
				break;
			}
			
			++numiter;
			if(numiter >= maxiter) {
				break;
			}
			
		}
		
		/*
		try{
		saveAR("AR.csv");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		*/
	}
	
	public void saveAR(String filename) throws IOException {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<nRecord; ++i) {
			sb.append(A[i][0] + R[i][0]);
			for(int k=1; k<nRecord; ++k) {
				sb.append(',').append(A[i][k] + R[i][k]);
			}
			sb.append(separator);
		}
		
		FileWriter outFile = new FileWriter(filename);
		PrintWriter out = new PrintWriter(outFile);
		out.print(sb.toString());
		out.close();
	}
	
	private void calculateCM() {		
		List<DoubleIntPair> lstPair = new ArrayList<DoubleIntPair>();
		for(int i=0; i<nRecord; ++i) {
			lstPair.add(new DoubleIntPair());
		}
		for(int i=0; i<nRecord; ++i) {
			for(int k=0; k<nRecord; ++k) {
				lstPair.get(k).set(A[i][k] + R[i][k], k);
			}
			
			Collections.sort(lstPair);
			vExemplar[i] = lstPair.get(0).getIndex();
		}
	}
}
