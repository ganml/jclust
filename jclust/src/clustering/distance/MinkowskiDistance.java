package clustering.distance;

import clustering.dataset.Record;
import clustering.dataset.Schema;

public class MinkowskiDistance extends Distance {
	protected double p;
		
	protected void setupArguments() throws Exception {
		super.setupArguments();
		p = arguments.getReal("p");
		assert p>1.0 : "invalide distance parameter p";		
	}
	
	@Override
	public double dist(Record r1, Record r2) {
        double temp = 0.0;
      
        for(int i=0;i<nDimension;++i){
            temp += Math.pow(Math.abs(r1.get(i)-r2.get(i)), p);
        }
        //CommonFunction.log("temp",temp);
        return Math.pow(temp, 1/p);
	}
	
	public double dist(Record r1, Record r2, double[] w) {
		Schema schema = r1.getSchema();
		assert schema == r2.getSchema() : "schemas do not match";
		
        double temp = 0.0;
        
    	for(int i=0;i<schema.size();++i){
            temp += Math.pow(Math.abs(schema.getVariable(i).distance(r1.get(i),r2.get(i))*w[i]), p);
        }
      
        return Math.pow(temp, 1/p);

	}

	
}
