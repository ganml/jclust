package clustering.validation;

import java.util.Arrays;

import clustering.cluster.PartitionClustering;
import clustering.util.ConfusionMatrix;
import clustering.util.Storage;

public class NormalizedMutualInformation extends ValidationIndex {
	//private int[][] _crosstab;
	
	@Override
	public double getIndex(Storage res) throws Exception {
		PartitionClustering pc = (PartitionClustering) res.get("pc"); 
		ConfusionMatrix cm = pc.getConfusionMatrix();
		
		int[][] mTable = cm.getTable();
		int n = mTable.length;
		int m = mTable[0].length;
		double dSize = pc.getDataset().size();
		
		int[] vC = new int[n];
		int[] vB = new int[m];
		Arrays.fill(vC, 0);
		Arrays.fill(vB, 0);
		
		for(int i = 0; i<n; ++i) {
			for(int j=0; j<m; ++j) {
				vC[i] += mTable[i][j];
				vB[j] += mTable[i][j];
			}
		}
		
		double dHB = 0;
		double dHC = 0;
		for(int i=0; i<n; ++i) {
			if(vC[i] > 0) {
				dHC -= vC[i] / dSize * Math.log(vC[i] / dSize);
			}
		}
		for(int j=0; j<m; ++j) {
			if(vB[j] > 0) {
				dHC -= vB[j] / dSize * Math.log(vB[j] / dSize);
			}
		}
		double dIBC = dHB + dHC;
		for(int i=0; i<n; ++i) {
			for(int j=0; j<m; ++j) {
				if(mTable[i][j] > 0) {
					dIBC += mTable[i][j] / dSize * Math.log(mTable[i][j] / dSize);
				}
			}
		}
		
		return 2*dIBC / (dHB + dHC);
	}

}
