package clustering.distance;

import clustering.algorithm.Algorithm;
import clustering.dataset.Record;

public abstract class Distance extends Algorithm {	
	
	public void initialize() throws Exception {
		setupArguments();
	}
	
	public abstract double dist(Record r1, Record r2);
	
	public double dist(Record r1, Record r2, double[] w) {
		return dist(r1, r2);
	}	
}
