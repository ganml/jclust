package clustering.validation;

import clustering.cluster.PartitionClustering;
import clustering.util.Storage;

public class OutlierCounter extends ValidationIndex {

	@Override
	public double getIndex(Storage res) throws Exception {
		PartitionClustering pc = (PartitionClustering) res.get("pc3"); 
		
		return pc.getClusters().get(1).size();
	}

}
