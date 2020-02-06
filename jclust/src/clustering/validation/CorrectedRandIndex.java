package clustering.validation;

import clustering.cluster.PartitionClustering;
import clustering.util.ConfusionMatrix;
import clustering.util.Storage;

public class CorrectedRandIndex extends ValidationIndex {
	//private int[][] _crosstab;
	
	@Override
	public double getIndex(Storage res) throws Exception {
		PartitionClustering pc = (PartitionClustering) res.get("pc"); 
		ConfusionMatrix cm = pc.getConfusionMatrix();
		
		int[][] mTable = cm.getTable();
		// calculate corrected Rand index - May 31, 2014
	    double dSumij = 0.0;
	    double dSumi = 0.0;
	    double N = 0.0;
	    for(int i=0; i<mTable.length; ++i) {
	    	double dTemp = 0;
	    	for(int j=0; j<mTable[0].length; ++j) {
	    		dTemp += mTable[i][j];
	    		dSumij += mTable[i][j] * (mTable[i][j] - 1) / 2.0;
	    	}
	    	N += dTemp;
	    	dSumi += dTemp * (dTemp-1) / 2.0;
	    }
	    double dSumj = 0;
	    for(int j=0; j<mTable[0].length; ++j) {
	    	double dTemp = 0;
	    	for(int i=0; i<mTable.length; ++i) {
	    		dTemp += mTable[i][j];
	    	}
	    	dSumj += dTemp * (dTemp - 1) / 2.0;
	    }
	    double dCorrectedRand = ( N*(N-1) * dSumij / 2 - dSumi * dSumj ) / 
	    		( N*(N-1) * (dSumi + dSumj) / 4 - dSumi * dSumj);
	    
		return dCorrectedRand;
	}

}
