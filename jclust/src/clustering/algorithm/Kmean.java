package clustering.algorithm;

import java.util.*;

import clustering.cluster.*;
import clustering.dataset.Record;
import clustering.initialization.*;
import clustering.util.CommonFunction;

public class Kmean extends ClusteringAlgorithm {
	// parameters
	protected int numclust;
	protected int maxiter;
	protected double delta;
	
	// results
	protected List<Cluster> lstcluster;
	protected int[] CM;
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
		PartitionClustering pc = new PartitionClustering(ds, lstcluster);
	    
        results.insert("pc", pc);
        results.insert("pc2", pc);
        results.insert("numiter", new Integer(numiter));
        results.insert("dobj", new Double(dobj));        
        results.insert("center", Z);
    }

	protected void initialization() throws Exception {
		Z = new double[numclust][nDimension];
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
        numiter = 1;
        while(true) { 
            //int nChanges = 0;
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
            //CommonFunction.log(String.format("dobj, %f", dobj));
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
            dTemp += dist(ds.get(i), CM[i]);
        }
        
        dobj = dTemp;
    }
}
