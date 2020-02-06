package clustering.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import clustering.cluster.*;
import clustering.dataset.Record;
import clustering.initialization.InitializationMethod;
import clustering.util.DoubleIntPair;

public class Kmor extends ClusteringAlgorithm {
	// parameters
	protected int  numclust;
	protected double gamma;
	protected double delta;
	protected int maxiter;
	protected int n0;
	protected String outlierLabel;
	
	// results	
	protected List<Cluster> lstcluster;
	protected int[] CM;
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
		double p0 = arguments.getReal("pzero");
		n0 = (int) Math.floor(p0 * nRecord);
		outlierLabel = arguments.getStr("outlierLabel");
		
		String imName = arguments.getStr("im");
		Class<?> clazz = Class.forName("clustering.initialization."+imName);	
		im = (InitializationMethod) clazz.newInstance();
		im.setArguments(arguments);
	}
	
	@Override
	protected void fetchResults() throws Exception {		
		PartitionClustering pc = new PartitionClustering(ds, lstcluster);

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
        results.insert("pc3", pc3);
        results.insert("numiter", new Integer(numiter));
        results.insert("dobj", new Double(dobj));     
    
	}

	protected void initialization() throws Exception {     	
        CM = new int[nRecord];
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
        } 
         
	}
	
	protected void iteration() {
		calculateObj();
		double dObjPre;
		
        numiter = 1;
        while(true) {
        	
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
	
	protected void updateU() {
        double dMin,dDist;
        nChanges = 0;
        List<DoubleIntPair> listPair = new ArrayList<DoubleIntPair>();
        int[] vD = new int[nRecord];
        for(int i=0;i<nRecord;++i) {
            dMin = Double.MAX_VALUE;
            int s = -1;
            for(int k=0;k<numclust;++k) { 
                dDist = dist(ds.get(i),k);
                if (dMin > dDist) {
                    dMin = dDist;
                    s = k;
                }                
            }
            vD[i] = s;
            listPair.add(new DoubleIntPair(dMin, i)); 
        }
        Collections.sort(listPair);
        
        /*
        if(numiter == 1) {
        	for(int i=0; i<nRecord; ++i) {
        		log.info(String.format("dist, %f",listPair.get(i).getValue()));
        	}
		}*/
        
        int nOutliers = 0;
        for(int i=0; i<n0; ++i) {            	
        	if(listPair.get(i).getValue() <= dAvgDist) {             		
        		break;
        	}
        	nOutliers = i+1;
        }
        for(int j=0; j<nOutliers; ++j) {
        	int i = listPair.get(j).getIndex();
        	if (CM[i] != numclust){
                lstcluster.get(CM[i]).remove(ds.get(i));
                lstcluster.get(numclust).add(ds.get(i));
                CM[i] = numclust;
                ++nChanges;
            }
        }
        for(int j=nOutliers;j<nRecord;++j) {
        	int i = listPair.get(j).getIndex();
        	int s = vD[i];
            if (CM[i] != s){
                lstcluster.get(CM[i]).remove(ds.get(i));
                lstcluster.get(s).add(ds.get(i));
                CM[i] = s;
                ++nChanges;
            }
        }
        
	
	}
	
	protected void updateZ() {
		double dTemp;  
        for(int k=0;k<numclust;++k){ 
            for(int j=0;j<nDimension;++j){
                dTemp = 0.0;
                for(int i=0; i<lstcluster.get(k).size();++i){
                    Record rec = lstcluster.get(k).getRecord(i);
                    dTemp += rec.get(j);
                }               
                Z[k][j] = dTemp / lstcluster.get(k).size();
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
		double dSum1 = 0.0;
		for(int i=0; i<nRecord; ++i) {
			int j = CM[i];
			if(j < numclust) {
				dSum1 += dist(ds.get(i), j);
			}
		}		
		dAvgDist = dSum1 * gamma / (nRecord - lstcluster.get(numclust).size());
				
		dobj = dSum1 + dAvgDist * lstcluster.get(numclust).size();
	}
}
