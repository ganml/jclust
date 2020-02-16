package clustering.algorithm;

import java.util.ArrayList;
import java.util.List;

import clustering.cluster.Cluster;
import clustering.cluster.PartitionClustering;
import clustering.dataset.Record;
import clustering.initialization.InitializationMethod;
import clustering.util.CommonFunction;

public class Fsc extends ClusteringAlgorithm {
	// parameters
	protected int numclust;
	protected int maxiter;
	protected double alpha;
	protected double delta;
	protected double epsilon;
	
	// results
	protected List<Cluster> lstcluster;
	protected int[] CM;
	protected double[][] Z;
	protected double[][] W;
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
		if(arguments.contains("delta")) {
			delta = arguments.getReal("delta");	
		} else {
			delta = 1e-6;
		}
		if(arguments.contains("epsilon")) {
			epsilon = arguments.getReal("epsilon");
		} else {
			epsilon = 0.0;
		}
		numclust = arguments.getInt("numcluster");
		maxiter = arguments.getInt("maxiter");
		
		String imName = arguments.getStr("im");
		Class<?> clazz = Class.forName("clustering.initialization."+imName);	
		im = (InitializationMethod) clazz.newInstance();
		im.setArguments(arguments);
	}
	
	@Override
	protected void fetchResults() throws Exception {
		PartitionClustering pc = new PartitionClustering(ds, lstcluster);
	    
        results.insert("pc", pc);
        results.insert("pc2", pc);
        results.insert("numiter", new Integer(numiter));
        results.insert("dobj", new Double(dobj));        
        results.insert("center", Z);
        results.insert("weight", W);
    }

	protected void initialization() throws Exception {
		Z = new double[numclust][nDimension];
		W = new double[numclust][nDimension];
        CM = new int[nRecord];
        CommonFunction.log("Initializing ...");
        im.run();        
        CommonFunction.log("Initializing finished");
        int[] vInd = (int[]) im.getResults().get("index");
        lstcluster = new ArrayList<Cluster>();       
		for(int i=0;i<numclust;++i){            
            Record cr = ds.get(vInd[i]);
            for(int j=0; j<nDimension; ++j) {
            	Z[i][j] = cr.get(j);
            	W[i][j] = 1.0 / nDimension;
            }
            Cluster c = new Cluster(String.format("C%d", i+1));            
            lstcluster.add(c);            
        }
        int s = -1;
        double dMin,dDist;
        for(int i=0;i<nRecord;++i){
            dMin = Double.MAX_VALUE;
            s = -1;
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

        updateCenter();
        updateWeight();
        numiter = 1;
        while(true) { 
            updateMembership();
            updateCenter();
            updateWeight();

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
	
	protected void updateMembership() {
		int s = -1;
        double dMin,dDist;
        for(int i=0;i<nRecord;++i) {
            dMin = Double.MAX_VALUE;
            s = -1;
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
                //++nChanges;
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
    	double[] vTemp = new double[nDimension];
    	double dSum;
        for(int k=0;k<lstcluster.size();++k){ 
        	dSum = 0.0;
            for(int j=0;j<nDimension;++j){
                vTemp[j] = epsilon;
                for(int i=0; i<lstcluster.get(k).size();++i){
                    Record rec = lstcluster.get(k).getRecord(i);
                    vTemp[j] += Math.pow(rec.get(j) - Z[k][j], 2.0);
                }
                                	
                dSum += Math.pow(1/vTemp[j], 1/(alpha-1.0));
            }
            
            for(int j=0; j<nDimension; ++j) {
            	W[k][j] = Math.pow(1/vTemp[j], 1/(alpha-1.0)) / dSum;
            }
        }
    }
    
    protected double dist(Record x, int cind) {
    	double dTemp = 0.0;
        for(int j=0;j<nDimension;++j){
            dTemp += Math.pow(x.get(j)-Z[cind][j], 2.0) * Math.pow(W[cind][j], alpha);        	
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
