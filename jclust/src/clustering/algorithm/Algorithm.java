package clustering.algorithm;

import clustering.dataset.Dataset;
import clustering.util.*;

public abstract class Algorithm {
	protected String separator = System.getProperty("line.separator");
	protected Dataset ds;
	protected int nRecord;
	protected int nDimension;
	
	protected Storage results;
	protected Storage arguments;
	protected double runtime;
	
	public Algorithm() {
		results = new Storage();
		arguments = new Storage();
	}
	
	public Storage getArguments() {
		return arguments;
	}
	
	public void setArguments(Storage args) {
		arguments = args;
	}
	
	public Storage getResults() {
		return results;
	}
	
	public void reset() {
		results.clear();
	}

	protected void setupArguments() throws Exception {
		ds = (Dataset) arguments.get("dataset");
		if(ds == null) {
			throw new Exception("ds is null");
		}
		nRecord = ds.size();
		nDimension = ds.dimension();
	}	

}
