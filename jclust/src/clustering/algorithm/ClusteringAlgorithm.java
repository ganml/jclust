package clustering.algorithm;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class ClusteringAlgorithm extends Algorithm {
	
	public void run() throws Exception {
		double startTime = System.currentTimeMillis();
        setupArguments();
        work();
        reset();
        fetchResults();
        runtime = (-startTime + System.currentTimeMillis()) / 1000.0;
        
        results.insert("runtime", runtime);
	}
	
	protected abstract void work() throws Exception;
	protected abstract void fetchResults() throws Exception;
	
	protected void save(String filename, double[][] M) throws IOException {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<M.length; ++i) {
			sb.append(M[i][0]);
			for(int k=1; k<M[0].length; ++k) {
				sb.append(',').append(M[i][k]);
			}
			sb.append(separator);
		}
		
		FileWriter outFile = new FileWriter(filename);
		PrintWriter out = new PrintWriter(outFile);
		out.print(sb.toString());
		out.close();

	}
	
	protected void save(String filename, int[][] M) throws IOException {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<M.length; ++i) {
			sb.append(M[i][0]);
			for(int k=1; k<M[0].length; ++k) {
				sb.append(',').append(M[i][k]);
			}
			sb.append(separator);
		}
		
		FileWriter outFile = new FileWriter(filename);
		PrintWriter out = new PrintWriter(outFile);
		out.print(sb.toString());
		out.close();

	}
}
