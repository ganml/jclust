package clustering.distance;

import clustering.dataset.Record;
import clustering.dataset.Schema;

public class PearsonDistance extends Distance {
	
	@Override
	public double dist(Record r1, Record r2) {
		Schema schema = r1.getSchema();
		assert schema == r2.getSchema() : "schemas do not match";
		
        double mx = r1.get(0);
        double my = r2.get(0);
        double m2x = 0.0;
        double m2y = 0.0;
        double mxy = 0.0;
        double deltax;
        double deltay;
        for(int i=1;i<schema.size();++i){
            deltax = r1.get(i) - mx;
            deltay = r2.get(i) - my;
            mx += deltax / (i+1.0);
            my += deltay / (i+1.0);
            m2x += deltax * deltax * i / (i+1.0);
            m2y += deltay * deltay * i / (i+1.0);
            mxy += deltax * deltay * i / (i+1.0);
        }

		return 1 - mxy / Math.sqrt(m2x * m2y);
	}

	
}
