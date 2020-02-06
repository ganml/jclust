package clustering.algorithm;

import java.util.*;

import clustering.cluster.Cluster;
import clustering.cluster.PartitionClustering;
import clustering.dataset.Record;
import clustering.initialization.InitializationMethod;

public class Fcm extends ClusteringAlgorithm {
    // parameters
	protected int numclust;
	protected int maxiter;
	protected double delta;
	protected double alpha;
	
	// results
	protected List<Cluster> lstcluster;
	protected int[] CM;
	protected double[][] U;
	protected double[][] Z;
	protected double dobj;
	protected int numiter;
	protected InitializationMethod im;
		
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
		
		String imName = arguments.getStr("im");
		Class<?> clazz = Class.forName("clustering.initialization."+imName);	
		im = (InitializationMethod) clazz.newInstance();
		im.setArguments(arguments);
	}

	@Override
	protected void fetchResults() throws Exception {
        lstcluster = new ArrayList<Cluster>();       
		for(int i=0;i<numclust;++i){   
            Cluster c = new Cluster(String.format("C%d", i+1));  
            lstcluster.add(c);            
        }
		for(int i=0; i<nRecord; ++i) {
			lstcluster.get(CM[i]).add(ds.get(i));
		}
		
		PartitionClustering pc = new PartitionClustering(ds, lstcluster);
	    
        results.insert("pc", pc);
        //results.insert("pc2", pc);
        results.insert("numiter", new Integer(numiter));
        results.insert("dobj", new Double(dobj));        
        results.insert("center", Z);		
	}
	
	protected void initialization() throws Exception {
		Z = new double[numclust][nDimension];
		CM = new int[nRecord];
		U = new double[nRecord][numclust];

        im.run();        
        int[] vInd = (int[]) im.getResults().get("index");
		for(int i=0;i<numclust;++i){            
            Record cr = ds.get(vInd[i]);
            for(int j=0; j<nDimension; ++j) {
            	Z[i][j] = cr.get(j);
            }
        }

        double dDist;
        for(int i=0;i<nRecord;++i){
            double dSum = 0.0;
            double dMin = Double.MAX_VALUE;
            int kind = -1;
            for(int j=0; j<numclust; ++j) {
            	dDist = dist(ds.get(i), j) + 1e-6;
            	U[i][j] = Math.pow(dDist, -2/(alpha-1));
            	dSum += U[i][j];
            	if(dMin > dDist) {
            		dMin = dDist;
            		kind = j;
            	}
            }
            CM[i] = kind;
            for(int j=0; j<numclust; ++j) {
            	U[i][j] /= dSum;
            }
        } 
  	}
	
	protected void iteration() {
		double dObjPre, dDist;

        updateCenter();
        numiter = 1;
        while(true) {             
            for(int i=0;i<nRecord;++i) {
            	double dSum = 0.0;
            	double dMin = Double.MAX_VALUE;
            	int kind = -1;
                for(int j=0;j<numclust;++j){ 
                    dDist = dist(ds.get(i), j);                      
                	U[i][j] = Math.pow(dDist, -2/(alpha-1));
                	dSum += U[i][j];
                	if(dMin > dDist) {
                		dMin = dDist;
                		kind = j;
                	}
                }
                CM[i] = kind;                
               
                for(int j=0; j<numclust; ++j) {
                	U[i][j] /= dSum;
                }
            }
            //CommonFunction.log(String.format("dobj, %f, changes, %d", dobj, nChanges));
            updateCenter();

            dObjPre = dobj;
            calculateObj();
            if(Math.abs(dObjPre - dobj) < delta){
                break;
            }

            numiter++;
            if (numiter > maxiter){
                break;
            }
        }
	}
	
    protected void updateCenter() {
    	for(int k=0; k<numclust; ++k) {
    		for(int j=0; j<nDimension; ++j) {
    			Z[k][j] = 0.0;
    		}
    	}
    	
    	double[] vSum = new double[numclust];
    	Arrays.fill(vSum, 0.0);
    	for(int s=0; s<numclust; ++s) {
    		for(int i=0; i<nRecord; ++i) {    		
    			double dTmp = Math.pow(U[i][s], alpha);
    			for(int j=0; j<nDimension; ++j) {
    				Z[s][j] += ds.get(i).get(j) * dTmp;
    			}
    			vSum[s] += dTmp;
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
        	for(int j=0; j<numclust; ++j) {
                dTemp += dist(ds.get(i), CM[i]) * Math.pow(U[i][j], alpha);
        	}
        }
        
        dobj = dTemp;
    }
}
