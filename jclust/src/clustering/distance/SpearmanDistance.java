package clustering.distance;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import clustering.dataset.Record;

public class SpearmanDistance extends Distance {
	private SpearmansCorrelation _sc;
	
	public SpearmanDistance() {		
		_sc = new SpearmansCorrelation();
	}

	@Override
	public double dist(Record r1, Record r2) {
		int nSize = r1.getSchema().size();
		double[] x = new double[nSize];
		double[] y = new double[nSize];
		for(int i=0; i<nSize; ++i) {
			x[i] = r1.get(i);
			y[i] = r2.get(i);
		}
				
		return 1-_sc.correlation(x, y);
	}
	

}
