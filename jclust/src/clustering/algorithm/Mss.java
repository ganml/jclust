package clustering.algorithm;

import java.util.*;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.ranking.NaturalRanking;

import clustering.cluster.*;
import clustering.util.CommonFunction;

public class Mss extends ClusteringAlgorithm {
	protected double delta;
	protected double[][] Z; // rank matrix
	protected PearsonsCorrelation pc;
	protected NaturalRanking nr;
	protected double[][] Zn;
	
	// results
	protected List<Cluster> lstcluster;
	protected int numiter;
	
	public Mss() {
		pc = new PearsonsCorrelation();
		nr = new NaturalRanking();
	}
	
	@Override
	protected void work() throws Exception {
		initialize();
		iteration();
	}

	@Override
	protected void fetchResults() throws Exception {
		lstcluster = new ArrayList<Cluster>();
		Set<Integer> setIndex = new HashSet<Integer>();
		for(int i=0; i<ds.size(); ++i) {
			setIndex.add(i);
		}
				
		int nCount = 1;
		while(!setIndex.isEmpty()) {
			int ii = setIndex.iterator().next();
			setIndex.remove(ii);
			
			Cluster c = new Cluster(String.format("C%d", nCount++));
			c.add(ds.get(ii));
			lstcluster.add(c);
			
			List<Integer> listIndex = new ArrayList<Integer>();
			for(int k : setIndex) {
				double dDiff = 0.0;
				for(int j=0; j<ds.dimension(); ++j) {
					dDiff += Math.abs(Zn[ii][j] - Zn[k][j]);
				}
				if(dDiff < 1e-6) {
					listIndex.add(k);
				}
			}
			
			for(int k: listIndex) {				
				c.add(ds.get(k));
				setIndex.remove(k);
			}
		}
				
		PartitionClustering pc = new PartitionClustering(ds, lstcluster);
        results.insert("pc", pc);
        results.insert("numiter", new Integer(numiter));                
	}
	
	protected void initialize() {	
		Z = new double[nRecord][nDimension];
		
		for(int i=0; i<nRecord; ++i) {
			double[] rank = nr.rank(ds.get(i).getValues());
			for(int j=0; j<nDimension; ++j) {
				Z[i][j] = rank[j];
			}
		}		
		
	}
	
	protected void iteration() {
		Zn = Z.clone();
		boolean bChanged = true;		
		double[] oldRank;
		double[] dSum = new double[nDimension];
		numiter = 0;
		while(bChanged) {
			bChanged = false;
			for(int i=0; i<nRecord; ++i) {				
				for(int r=0; r<nDimension; ++r) {
					dSum[r] = 0.0;
				}
				for(int j=0; j<nRecord; ++j) {
					double dTemp = (1-pc.correlation(Zn[i], Z[j]))/2;
					if(dTemp < delta) {
						for(int r=0; r<nDimension; ++r) {
							dSum[r] += Z[j][r];
						}
					}
				}
				oldRank = Zn[i];
				Zn[i] = nr.rank(dSum);
				for(int r=0; r<nDimension; ++r) {
					if(Math.abs(oldRank[r] - Zn[i][r]) > 1e-8) {
						bChanged = true;
						break;
					}
				}
			}
			
			++numiter;
			if(numiter % 100 ==0 ){
				CommonFunction.log(String.format("iteration %d", numiter));
			}
		}
	}
	
	protected void setupArguments() throws Exception {
		super.setupArguments();
		delta = arguments.getReal("delta");		
	}

}
