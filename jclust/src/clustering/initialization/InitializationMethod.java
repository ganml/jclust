package clustering.initialization;

import clustering.algorithm.Algorithm;

public abstract class InitializationMethod extends Algorithm {
	protected int numSample;
	protected int[] vIndex;
	
	protected void setupArguments() throws Exception {
		super.setupArguments();
		numSample = arguments.getInt("numcluster");
	}
	
	public void run() throws Exception {
		double startTime = System.currentTimeMillis();
        setupArguments();
        work();
        reset();
        fetchResults();
        runtime = (-startTime + System.currentTimeMillis()) / 1000.0;
        
        results.insert("runtime", runtime);
	}
	
	protected abstract void work();
	
	protected void fetchResults() throws Exception {
		results.insert("index", vIndex);	
	}

}
