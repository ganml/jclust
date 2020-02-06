package clustering.distance;

import clustering.dataset.Record;
import clustering.dataset.Schema;

public class SquaredEuclideanDistance extends Distance {

	@Override
	public double dist(Record r1, Record r2) {
		Schema schema = r1.getSchema();
		assert schema == r2.getSchema() : "schemas do not match";
		
        double temp = 0.0;
        for(int i=0;i<schema.size();++i){
            temp += Math.pow(Math.abs(schema.getVariable(i).distance(r1.get(i),r2.get(i))), 2.0);
        }
        
		return temp;// / Math.pow(schema.size(), 2.0);
	}

}
