package clustering.algorithm;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

import clustering.cluster.*;
import clustering.dataset.Record;
import clustering.initialization.*;
import clustering.util.*;

public class Tfcmpp extends ClusteringAlgorithm {
    // parameters
	protected int numclust;
	protected int maxiter;
	protected double delta;
	protected double alpha;
	protected int nT;
	protected int nT2;
	protected double epsilon;
	
	// results
	protected List<Cluster> lstcluster;
	protected int[][] CM;
	protected double[][] U;
	protected double[][] Z;
	protected double dobj;
	protected int numiter;
	protected InitializationMethod im;
	protected  Random rnd;
		
	@Override
	protected void work() throws Exception {
		initialization(); 
        iteration(); 
	}
	
	protected void setupArguments() throws Exception {
		super.setupArguments();
		alpha = arguments.getReal("alpha");	
		delta = arguments.getReal("delta");		
		numclust = arguments.getInt("numcluster");
		maxiter = arguments.getInt("maxiter");
		nT =  Math.min(arguments.getInt("T"), numclust);
		nT2 = Math.min(2*nT, numclust);
		epsilon = 1e-6;
		
		im = new KmeanppMethod();
		im.setArguments(arguments);
		rnd = new Random(arguments.getInt("seed"));
	}

	@Override
	protected void fetchResults() throws Exception {
        lstcluster = new ArrayList<Cluster>();       
		for(int i=0;i<numclust;++i){   
            Cluster c = new Cluster(String.format("C%d", i+1));  
            lstcluster.add(c);            
        }
		for(int i=0; i<nRecord; ++i) {
			lstcluster.get(CM[i][0]).add(ds.get(i));
		}
		
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<numclust; ++i) {
			double dMin = Double.MAX_VALUE;
			int nInd = -1;
			for(int j=0; j<lstcluster.get(i).size(); ++j) {
				double dTmp = dist(lstcluster.get(i).getRecord(j), i);
				if(dMin > dTmp) {
					dMin = dTmp;
					nInd = lstcluster.get(i).getRecord(j).getId();
				}				
			}
			if(nInd >0) {
			    sb.append(nInd).append(System.getProperty( "line.separator" ));
			}
		}
		
		FileWriter outFile = new FileWriter("index" + numclust + ".csv");
		PrintWriter out = new PrintWriter(outFile);
		out.print(sb.toString());
		out.close();
		
		PartitionClustering pc = new PartitionClustering(ds, lstcluster);
	    
        results.insert("pc", pc);
        //results.insert("pc2", pc);
        results.insert("numiter", new Integer(numiter));
        results.insert("dobj", new Double(dobj));        
        results.insert("center", Z);		
	}
	
	protected void initialization() throws Exception {
		Z = new double[numclust][nDimension];
		CM = new int[nRecord][nT];
		U = new double[nRecord][nT];

        im.run();        
        int[] vInd = (int[]) im.getResults().get("index");
		for(int i=0;i<numclust;++i){            
            Record cr = ds.get(vInd[i]);
            for(int j=0; j<nDimension; ++j) {
            	Z[i][j] = cr.get(j);
            }
        }
        
        double dDist;
        List<DoubleIntPair> listN = new ArrayList<DoubleIntPair>();
        for(int i=0; i<numclust; ++i) {
        	listN.add(new DoubleIntPair());
        }
        List<DoubleIntPair> listD = new ArrayList<DoubleIntPair>();
        for(int i=0; i<nT2; ++i) {
        	listD.add(new DoubleIntPair());
        }
        for(int i=0;i<nRecord;++i){   
            for(int j=0;j<numclust;++j){ 
            	listN.get(j).set(rnd.nextDouble(), j);
            }
            Collections.sort(listN);
            for(int j=0;j<nT2;++j){ 
                dDist = dist(ds.get(i), listN.get(j).getIndex());
                listD.get(j).set(-dDist, listN.get(j).getIndex());
            }
            Collections.sort(listD);
            double dSum = 0.0;
            for(int j=0; j<nT; ++j) {
            	CM[i][j] = listD.get(j).getIndex();
            	U[i][j] = Math.pow(-listD.get(j).getValue() + epsilon, -1/(alpha-1));
            	dSum += U[i][j];
            }
            for(int j=0; j<nT; ++j) {
            	U[i][j] /= dSum;
            }
            
         } 
        
	}
	
	protected void iteration() {
		double dObjPre, dDist;
        List<DoubleIntPair> listN = new ArrayList<DoubleIntPair>();
        for(int i=0; i<numclust; ++i) {
        	listN.add(new DoubleIntPair());
        }
        List<DoubleIntPair> listD = new ArrayList<DoubleIntPair>();
        for(int i=0; i<nT2; ++i) {
        	listD.add(new DoubleIntPair());
        }

        updateCenter();
        numiter = 1;
        while(true) {             
            for(int i=0;i<nRecord;++i) {
                for(int k=0; k<numclust; ++k) {
                	if(indexOf(CM[i], k) <0) {
                		listN.get(k).set(rnd.nextDouble(), k);
                	} else {
                		listN.get(k).set(2.0, k);
                	}
                }
                Collections.sort(listN);                
                
                for(int j=0;j<nT2;++j){ 
                    dDist = dist(ds.get(i), listN.get(j).getIndex());
                    listD.get(j).set(-dDist, listN.get(j).getIndex());
                }
                Collections.sort(listD);
                double dSum = 0.0;
                for(int j=0; j<nT; ++j) {
                	CM[i][j] = listD.get(j).getIndex();
                	U[i][j] = Math.pow(-listD.get(j).getValue() + epsilon, -1/(alpha-1));
                	dSum += U[i][j];
                }
                for(int j=0; j<nT; ++j) {
                	U[i][j] /= dSum;
                }
                
            }
            CommonFunction.log(String.format("dobj, %f", dobj));
            updateCenter();

            dObjPre = dobj;
            calculateObj();
            if(Math.abs(dObjPre - dobj) / dObjPre < delta){
                break;
            }

            numiter++;
            if (numiter > maxiter){
                break;
            }
        }
	}
	
	protected int indexOf(int[] array, int valueToFind) {
		for (int i = 0; i < array.length; i++) {
	          if (valueToFind == array[i]) {
	              return i;
	          }
	    }
		return -1;
	}

    protected void updateCenter() {
    	for(int k=0; k<numclust; ++k) {
    		for(int j=0; j<nDimension; ++j) {
    			Z[k][j] = 0.0;
    		}
    	}
    	
    	double[] vSum = new double[numclust];
    	Arrays.fill(vSum, 0.0);
    	for(int i=0; i<nRecord; ++i) {
    		for(int s=0; s<nT; ++s) {
    			double dTmp = Math.pow(U[i][s], alpha);
    			for(int j=0; j<nDimension; ++j) {
    				Z[CM[i][s]][j] += ds.get(i).get(j) * dTmp;
    			}
    			vSum[CM[i][s]] += dTmp;
    		}
    	}
    	    	
        for(int k=0;k<numclust;++k){ 
            for(int j=0; j<nDimension; ++j){                
                Z[k][j] /= vSum[k];
            }
        }
    }
    
    protected double dist(Record x, int cind) {
    	double dTemp = 0.0;
        for(int j=0;j<nDimension;++j){
            dTemp += Math.pow(x.get(j)-Z[cind][j], 2.0);        	
        }
                
        return dTemp;
    }
    
    protected void calculateObj() {
        double dTemp = 0.0;
        for(int i=0; i<nRecord; ++i){
        	for(int j=0; j<nT; ++j) {
                dTemp += ( dist(ds.get(i), CM[i][j]) + epsilon) * Math.pow(U[i][j], alpha);
        	}
        }
        
        dobj = dTemp;
    }
}
