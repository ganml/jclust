package clustering.algorithm;

import java.util.*;

import clustering.cluster.*;
import clustering.dataset.Record;
import clustering.initialization.InitializationMethod;

public class Kmod extends ClusteringAlgorithm {
	// parameters
	protected int  numclust;
	protected double gamma;
	protected double delta;
	protected int maxiter;
	protected String outlierLabel;
	
	// results	
	protected List<Cluster> lstcluster;
	protected int[] CM; // k clusters + one group of outliers
	protected int[] CMV; // k clusters
	protected double[][] Z;
	protected double dobj;
	protected double dAvgDist;
	protected int numiter;
	protected InitializationMethod im;
	protected int nChanges;
	
	@Override
	protected void work() throws Exception {
		initialization(); 
        iteration(); 
	}

	protected void setupArguments() throws Exception {
		super.setupArguments();
		gamma = arguments.getReal("gamma");		
		delta = arguments.getReal("delta");
		numclust = arguments.getInt("numcluster");
		maxiter = arguments.getInt("maxiter");
		outlierLabel = arguments.getStr("outlierLabel");
		
		String imName = arguments.getStr("im");
		Class<?> clazz = Class.forName("clustering.initialization."+imName);	
		im = (InitializationMethod) clazz.newInstance();
		im.setArguments(arguments);
	}
	
	@Override
	protected void fetchResults() throws Exception {		
		PartitionClustering pc = new PartitionClustering(ds, lstcluster);
		
		List<Cluster> lstcluster2 = new ArrayList<Cluster>();
    	Map<Integer, Integer> mapInd = new HashMap<Integer, Integer>();
    	for(int i=0; i<CMV.length; ++i) {    		
    		if(mapInd.containsKey(CMV[i])) {
    			lstcluster2.get(mapInd.get(CMV[i])).add(ds.get(i));
    		} else {
    			mapInd.put(CMV[i], mapInd.size());
    			Cluster c = new Cluster(String.format("C%d", mapInd.get(CMV[i])+1));
    			c.add(ds.get(i));
    			lstcluster2.add(c);
    		}
    	}
		PartitionClustering pc2 = new PartitionClustering(ds, lstcluster2);
		
		List<String> lstLabelGiven = new ArrayList<String>();
		List<Cluster> lstcluster3 = new ArrayList<Cluster>();
    	lstcluster3.add(new Cluster("Normal"));
    	lstcluster3.add(new Cluster("Outlier"));
    	for(int i=0; i<CM.length; ++i) {    		
    		if(CM[i] < numclust) {
    			lstcluster3.get(0).add(ds.get(i));
    		} else {
    			lstcluster3.get(1).add(ds.get(i));
    		}
    		if(ds.get(i).getLabel().equals(outlierLabel)) {
    			lstLabelGiven.add("Outlier");
    		} else {
    			lstLabelGiven.add("Normal");
    		}
    	}
		PartitionClustering pc3 = new PartitionClustering(ds, lstLabelGiven, lstcluster3);
		
        results.insert("pc", pc);
        results.insert("pc2", pc2);
        results.insert("pc3", pc3);
        results.insert("numiter", new Integer(numiter));
        results.insert("dobj", new Double(dobj));     
        results.insert("Z", Z);   
        results.insert("CM", CM);   
        results.insert("CMV", CMV);
	}

	protected void initialization() throws Exception {     	
        CM = new int[nRecord];
        CMV = new int[nRecord];
        Z = new double[numclust][nDimension];
        
        im.run();        
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
		lstcluster.add(new Cluster("Outlier"));
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
            CMV[i] = s;
        } 
         
	}
	
	protected void iteration() {
		calculateObj();
		double dObjPre;
		
        numiter = 1;
        while(true) {
        	
        	updateV();
        	updateU();
        	updateZ();
            
        	dObjPre = dobj;
            calculateObj();
            
            if(Math.abs(dObjPre - dobj) < delta){
                break;
            }

            ++numiter;
            if (numiter > maxiter){
                break;
            }
        }

	}
	
	protected void updateV() {
		double dSum = 0.0;
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
                      	
        	CMV[i] = s;
        	dSum  += (dMin - dSum) / (i+1.0);
        } 
        
        dAvgDist = dSum * gamma;
	}
	
	protected void updateU() {
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
            if(dMin > dAvgDist) {
            	s = numclust;
            }
        	if(CM[i] != s) {
        		lstcluster.get(CM[i]).remove(ds.get(i));
        		lstcluster.get(s).add(ds.get(i));
        		CM[i] = s;
        	}        	
        } 		
	}
	
	protected void updateZ() {
		double r0 = gamma * lstcluster.get(numclust).size() / nRecord;
		double[] vSum = new double[numclust];
		double[][] mSum = new double[numclust][nDimension];
		
		for(int i=0; i<nRecord; ++i) {
			if(CM[i] < numclust) {
				for(int j=0; j<nDimension; ++j) {
					mSum[CMV[i]][j] += (1+r0) * ds.get(i).get(j);
				}
				vSum[CMV[i]] += 1+r0;
			} else {
				for(int j=0; j<nDimension; ++j) {
					mSum[CMV[i]][j] += r0 * ds.get(i).get(j);
				}
				vSum[CMV[i]] += r0;
			}
		}
		
        for(int k=0;k<numclust;++k){ 
            for(int j=0;j<nDimension;++j){                             
                Z[k][j] = mSum[k][j] / vSum[k];
            }
        }
	}
	
			
	protected double dist(Record x, int l) {
		double dSum = 0.0;		
		for(int j=0; j<nDimension; ++j) {
			dSum += Math.pow(x.get(j) - Z[l][j], 2.0);
		}
		return dSum;
	}
	
	protected void calculateObj() {
		double r0 = gamma * lstcluster.get(numclust).size() / nRecord;
		double dSum = 0.0;
		for(int i=0; i<nRecord; ++i) {
			int j = CMV[i];
			if(CM[i] < numclust) {
				dSum += (1+r0) * dist(ds.get(i), j);
			} else {
				dSum += r0 * dist(ds.get(i), j);
			}
		}		
				
		dobj = dSum;
	}
}
