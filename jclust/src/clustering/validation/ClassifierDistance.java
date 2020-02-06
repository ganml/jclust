package clustering.validation;

import clustering.util.Storage;
import clustering.cluster.PartitionClustering;

public class ClassifierDistance extends ValidationIndex {

	@Override
	public double getIndex(Storage res) throws Exception {
		PartitionClustering pc = (PartitionClustering) res.get("pc3"); 
		
		int[][] mTable = pc.getConfusionMatrix().getTable();
		double rf, rt;
		
		if(mTable.length == 2 && mTable[0].length ==2) {
	    	rf = mTable[0][1] / (mTable[0][1] + mTable[1][1] + 0.0);
	    	rt = mTable[0][0] / (mTable[0][0] + mTable[1][0] + 0.0);
		} else {
	    	rf = 1.0; 
	    	rt = 1.0; 			
		}
    	return Math.sqrt(rf*rf + (1-rt)*(1-rt));    	 
	}

}
