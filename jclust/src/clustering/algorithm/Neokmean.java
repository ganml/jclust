package clustering.algorithm;

import java.util.*;

import clustering.cluster.*;
import clustering.dataset.Record;
import clustering.initialization.InitializationMethod;
import clustering.util.*;

public class Neokmean extends ClusteringAlgorithm {
	// parameters
	protected int  numclust;
	protected double alpha;
	protected double beta;
	protected int maxiter;
	protected String outlierLabel;
	
	// results	
	protected List<Cluster> lstcluster;
	protected List<Set<Integer>> CM2;
	protected int[] CM;
	protected double[][] Z;
	protected int numiter;
	protected double dobj;
	protected InitializationMethod im;
	protected int nChanges;
	
	@Override
	protected void work() throws Exception {
		initialization(); 
        iteration(); 
	}

	protected void setupArguments() throws Exception {
		super.setupArguments();
		alpha = arguments.getReal("alpha");		
		beta = arguments.getReal("beta");
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
		List<Cluster> lstcluster1 = new ArrayList<Cluster>();       
		for(int i=0;i<numclust;++i){   
			Cluster c = new Cluster(String.format("C%d", i+1));   
            lstcluster1.add(c);            
        }
		lstcluster1.add(new Cluster("Outlier"));
		for(int i=0; i<CM.length; ++i) {
			lstcluster1.get(CM[i]).add(ds.get(i));
		}
		PartitionClustering pc = new PartitionClustering(ds, lstcluster1);

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
        CM2 = new ArrayList<Set<Integer>>();
        for(int i=0; i<nRecord; ++i) {
        	CM2.add(new HashSet<Integer>());
        }
        Z = new double[numclust][nDimension];
        CM = new int[nRecord];
        
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
                
	}
	
	protected void iteration() {
        numiter = 1;
        while(true) {
        	calculateObj();
            
         	updateU();
        	if(nChanges == 0) {
        		break;
        	}
        	
        	updateZ();
            ++numiter;
            if (numiter > maxiter){
                break;
            }
        }

	}
	
	protected void updateU() {
        double dDist;
        nChanges = 0;
        
        List<DoubleIntPair> listPair = new ArrayList<DoubleIntPair>();
        for(int i=0;i<nRecord;++i) {
            for(int k=0;k<numclust;++k) { 
                dDist = dist(ds.get(i),k);
                listPair.add(new DoubleIntPair(-dDist, i, k));
            }            
        }
        Collections.sort(listPair);
        
        /*
        if(numiter == 1) {
        	for(int i=0; i<nRecord; ++i) {
        		log.info(String.format("dist, %f",listPair.get(i).getValue()));
        	}
		}*/
        List<Set<Integer>> CMPre = CM2;
        CM2 = new ArrayList<Set<Integer>>();
        for(int i=0; i<nRecord; ++i) {
        	CM2.add(new HashSet<Integer>());
        	CM[i] = numclust;
        }
        
		for(int i=0;i<=numclust;++i){
            lstcluster.get(i).removeAll();            
        }
		        
        Set<Integer> setS = new HashSet<Integer>();
        Set<Integer> setT = new HashSet<Integer>();
        int j = 0;
        while(setS.size() < (1-beta)*nRecord) {
        	int i = listPair.get(j).getIndex();
        	int k = listPair.get(j).getIndex2();
        	++j;
        	if(setS.contains(i)) {
        		continue;
        	}
        	        	
            lstcluster.get(k).add(ds.get(i));
            CM2.get(i).add(k);
            CM[i] = k;
            setS.add(i);
            setT.add(i + nRecord * k);
        }
        
        int count = 0;
        for(int h=0; h<listPair.size(); ++h) {
        	int i = listPair.get(j).getIndex();
        	int k = listPair.get(j).getIndex2();
            if(setT.contains(i + nRecord * k)) {
            	continue;
            }
            setT.add(i + nRecord * k);
            
            lstcluster.get(k).add(ds.get(i));
            CM2.get(i).add(k);
            ++count;
            if(!setS.contains(i)) {
            	CM[i] = k;
            }
            
            if(count >= (alpha+beta)*nRecord) {
            	break;
            }
        }
        
	    nChanges = 0;
	    for(int i=0; i<nRecord; ++i) {
	    	if(CMPre.get(i).size() != CM2.get(i).size()) {
	    		++nChanges;
	    	} else {
	    		for(int key : CMPre.get(i)) {
	    			if(! CM2.get(i).contains(key)) {
	    				++nChanges;
	    			}
	    		}
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
			for(int j : CM2.get(i)) {
				dSum1 += dist(ds.get(i), j);
			}
		}		
				
		dobj = dSum1;
	}
}
