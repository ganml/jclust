package clustering.algorithm;

import java.util.*;

import clustering.cluster.*;
import clustering.util.Storage;

public class Hkmean extends ClusteringAlgorithm {
	// parameters
	protected int numclust;
	protected int maxiter;
	protected double delta;
	protected String imName;
	protected int seed;
	
	// results
	protected List<Cluster> lstcluster;

	@Override
	protected void work() throws Exception {
		iteration(); 
	}
	
	protected void setupArguments() throws Exception {
		super.setupArguments();
		delta = arguments.getReal("delta");		
		numclust = arguments.getInt("numcluster");
		maxiter = arguments.getInt("maxiter");		
		imName = arguments.getStr("im");		
		seed = arguments.getInt("seed");
	}
	
	@Override
	protected void fetchResults() throws Exception {
		for(Cluster c : lstcluster) {
			c.calculateNearestRecord();
		}
		PartitionClustering pc = new PartitionClustering(ds, lstcluster);
	    
        results.insert("pc", pc);
    }	
	
	protected void iteration() throws Exception {
		lstcluster = new ArrayList<Cluster>();
		Cluster c = new Cluster("C0");
		for(int i=0; i<ds.size(); ++i) {
			c.add(ds.get(i));
		}
		lstcluster.add(c);
		
		while(lstcluster.size() < numclust) {
			List<Cluster> lstTmp = lstcluster;
		    int nMax = -1;
		    int iMax = -1;
		    for(int i=0; i<lstTmp.size(); ++i) {
		    	if(nMax < lstTmp.get(i).size()) {
		    		nMax = lstTmp.get(i).size();
		    		iMax = i;
		    	}
		    }
		    
		    // k-means
		    Kmean km = new Kmean();
		    Storage Arg = km.getArguments();
	        Arg.insert("dataset", lstTmp.get(iMax).toDataset());
	        Arg.insert("numcluster", 2);
	        Arg.insert("delta", delta);
	        Arg.insert("maxiter", maxiter);
	        Arg.insert("im", imName);
	        Arg.insert("seed", seed);
	        km.run();
	        PartitionClustering pc = (PartitionClustering) km.getResults().get("pc");
	        
			lstcluster = new ArrayList<Cluster>();
			lstcluster.addAll(pc.getClusters());
			for(int i=0; i<lstTmp.size(); ++i) {
				if(i == iMax) {
					continue;
				}
				lstcluster.add(lstTmp.get(i));
			}
		}
		
		for(int i=0; i<lstcluster.size(); ++i) {
			lstcluster.get(i).setName(String.format("%d", i+1));
		}
	}
}
