package clustering.algorithm;

import java.util.*;
import clustering.cluster.Cluster;
import clustering.cluster.PartitionClustering;
import clustering.dataset.Record;
import clustering.initialization.InitializationMethod;

public class Scfs extends ClusteringAlgorithm {
	// parameters
	protected double gamma;
	protected double delta;
	protected int numclust;
	protected int maxiter;
	
	// results
	protected List<Cluster> lstcluster;
	protected int[] CM;
	protected double[][] Z;
	protected int[][] W;
	protected double dobj;
	protected int numiter;
	protected InitializationMethod im;

	@Override
	protected void work() throws Exception {
        initialization(); 
        iteration(); 		
	}

	@Override
	protected void fetchResults() throws Exception {	
		PartitionClustering pc = new PartitionClustering(ds, lstcluster);
    
        results.insert("pc", pc);
        results.insert("numiter", new Integer(numiter));
        results.insert("dobj", new Double(dobj));
        results.insert("center", Z);
        results.insert("weight", W);        
	}
		
	protected void setupArguments() throws Exception {
		super.setupArguments();
		gamma = arguments.getReal("gamma");
		delta = arguments.getReal("delta");
		numclust = arguments.getInt("numCluster");
		maxiter = arguments.getInt("maxiter");
		
		String imName = arguments.getStr("im");
		Class<?> clazz = Class.forName("clustering.initialization."+imName);	
		im = (InitializationMethod) clazz.newInstance();
		im.setArguments(arguments);
	}
	
    protected void initialization() throws Exception {
    	Z = new double[numclust][nDimension];
    	W = new int[numclust][nDimension];
        CM = new int[nRecord];

        im.run();        
        int[] vInd = (int[]) im.getResults().get("index");
        lstcluster = new ArrayList<Cluster>();       
		for(int i=0;i<numclust;++i){            
            Record cr = ds.get(vInd[i]);
            for(int j=0; j<nDimension; ++j) {
            	Z[i][j] = cr.get(j);
            	W[i][j] = 1;
            }
            Cluster c = new Cluster(String.format("C%d", i+1));            
            lstcluster.add(c);            
        }
        int s = -1;
        double dMin,dDist;
        for(int i=0;i<nRecord;++i){
            dMin = Double.MAX_VALUE;
            for(int j=0;j<numclust;++j){ 
                dDist = dist(ds.get(i), j);
                if (dDist<dMin){
                    s = j;
                    dMin = dDist;
                }
            }
            lstcluster.get(s).add(ds.get(i)); 
            CM[i] = s;
        } 
    }

    
	
	protected void iteration() {
        double dObjPre;

        numiter = 1;
        while(true) { 
            updateCenter();
            updateWeight();

            int s = -1;
            double dMin,dDist;
            for(int i=0;i<nRecord;++i) {
                dMin = Double.MAX_VALUE;
                for(int k=0;k<lstcluster.size();++k) { 
                    dDist = dist(ds.get(i),k);
                    if (dMin > dDist) {
                        dMin = dDist;
                        s = k;
                    }
                }

                if (CM[i] != s){
                    lstcluster.get(CM[i]).remove(ds.get(i));
                    lstcluster.get(s).add(ds.get(i));
                    CM[i] = s;
                }
            }
            

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
        double dTemp;        
        for(int k=0;k<lstcluster.size();++k){ 
            for(int j=0;j<nDimension;++j){
                dTemp = 0.0;
                for(int i=0; i<lstcluster.get(k).size();++i){
                    Record rec = lstcluster.get(k).getRecord(i);
                    dTemp += rec.get(j);
                }
                Z[k][j] = dTemp/lstcluster.get(k).size();
            }
        }
    }

    protected void updateWeight() {
        double dVar;        
        Record r;         
        double vV[] = new double[nDimension];
        for(int k=0;k<lstcluster.size();++k){      
        	double dV = 0.0;
            for(int j=0;j<nDimension;++j){
                dVar = 0.0;
                for(int i=0; i<lstcluster.get(k).size();++i){
                    r = lstcluster.get(k).getRecord(i);
                    dVar += Math.pow(r.get(j) -Z[k][j], 2.0);
                } 
                vV[j] = dVar;
                dV += (dVar - dV) / (j+1.0);                
            }
                        
            dV *= gamma;
            for(int j=0;j<nDimension;++j) {
                if(vV[j] <= dV) {
                	W[k][j] = 1;
                } else {
                	W[k][j] = 0;
                }
            }
        }
    }

    protected double dist(Record x, int cind) {
    	double dTemp = 0.0;
    	double dE = 0.0;
    	for(int j=0;j<nDimension;++j){
    		dE += 1.0 - W[cind][j];
    	}
    	dE *= gamma / nDimension;
        for(int j=0;j<nDimension;++j){
            dTemp += (W[cind][j] + dE) * Math.pow(x.get(j)-Z[cind][j], 2.0);
        }

        return dTemp;
    }
    
    protected void calculateObj() {
        double dTemp = 0.0;
        for(int i=0; i<nRecord; ++i){
            dTemp += dist(ds.get(i), CM[i]);
        }
        
        dobj = dTemp;
    }
}
