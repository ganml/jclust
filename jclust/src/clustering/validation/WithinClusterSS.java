package clustering.validation;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.Variance;

import clustering.cluster.*;
import clustering.util.Storage;

public class WithinClusterSS extends ValidationIndex {

	@Override
	public double getIndex(Storage res) throws Exception {
		PartitionClustering pc = (PartitionClustering) res.get("pc"); 
		List<Cluster> lC = pc.getClusters();
		double dRes = 0.0;
		for(int k=0; k<pc.getNumCluster(); ++k) {
			Cluster C = lC.get(k);
			if(C.size() == 0) {
				continue;
			}
			double[] vTmp = new double[C.size()];
			for(int j=0; j<pc.getDataset().dimension(); ++j) {
				
				for(int i=0; i<C.size(); ++i) {
					vTmp[i] = C.getRecord(i).get(j);
				}
				dRes += (new Variance()).evaluate(vTmp) * (C.size() - 1.0);
			}
		}
		
		/*
		double dTotal = 0.0;
		Dataset ds = pc.getDataset();
		double[] vTmp = new double[ds.size()];
		for(int j=0; j<pc.getDataset().dimension(); ++j) {
			for(int i=0; i<ds.size(); ++i) {
				vTmp[i] = ds.get(i).get(j);
			}
			dTotal += (new Variance()).evaluate(vTmp) * (ds.size() - 1.0);
		}
		
		return dRes / dTotal;
		*/
		
		return dRes;
	}
	
	public double getIndex2(Storage res) throws Exception {
		PartitionClustering pc = (PartitionClustering) res.get("pc"); 
		List<Cluster> lC = pc.getClusters();
		double dRes = 0.0;
		for(int k=0; k<pc.getNumCluster(); ++k) {
			Cluster C = lC.get(k);
			if(C.size() == 0) {
				continue;
			}
			for(int j=0; j<pc.getDataset().dimension(); ++j) {
				double dSum1 = 0.0;
				double dSum2 = 0.0;
				for(int i=0; i<C.size(); ++i) {
					dSum1 += Math.pow(C.getRecord(i).get(j), 2.0);
					dSum2 += C.getRecord(i).get(j);
				}
				dRes += dSum1 - dSum2 * dSum2 / C.size();
			}
		}
		return dRes;
	}

}
