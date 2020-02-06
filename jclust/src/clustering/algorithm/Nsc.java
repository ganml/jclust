package clustering.algorithm;

import org.apache.commons.math3.linear.*;

import java.util.*;

import clustering.dataset.*;
import clustering.distance.Distance;
import clustering.util.*;

// normalized spectral clustering
public class Nsc extends ClusteringAlgorithm {
	protected double[][] W;
	protected double[] D;
	protected double[][] Lrw;
	
	protected Distance distance;
	protected int numclust;
	protected double delta;
	protected int H;
	protected int maxiter;
	protected Storage kmResults;
	
	@Override
	protected void work() throws Exception {
		createL();
		kmeans();
	}

	@Override
	protected void fetchResults() throws Exception {
		results = kmResults;
	}
	
	protected void setupArguments() throws Exception {
		super.setupArguments();
		delta = arguments.getReal("delta");	
		numclust = arguments.getInt("numcluster");
		maxiter = arguments.getInt("maxiter");
		H = arguments.getInt("H");
		
		String distName = arguments.getStr("distance");
		Class<?> clazz = Class.forName("clustering.distance."+distName);	
		distance = (Distance) clazz.newInstance();
		distance.setArguments(arguments);
		distance.initialize();		
	}	
	
	protected void createL() {
		W = new double[nRecord][nRecord];
		double dTemp;
		for(int i=0; i<nRecord; ++i) {
			for(int j=0; j<i; ++j) {
				dTemp = distance.dist(ds.get(i), ds.get(j)) / delta;
				/*
				if(dTemp < delta) {
					W[i][j] = 1;
					W[j][i] = 1;
				} else {
					W[i][j] = 0;
					W[j][i] = 0;
				}
				*/
				W[i][j] = Math.exp(- dTemp * dTemp);
				W[j][i] = W[i][j];
			}	
			W[i][i] = 1;
		}
		D = new double[nRecord];
		for(int i=0; i<nRecord; ++i) {
			dTemp = 0.0;
			for(int j=0; j<nRecord; ++j) {
				dTemp += W[i][j];
			}
			D[i] = dTemp;
		}
		
		Lrw = new double[nRecord][nRecord];
		for(int i=0; i<nRecord; ++i) {
			for(int j=0; j<nRecord; ++j) {
				if(i==j) {
					Lrw[i][j] = 1.0 - W[i][j] / D[i];
				} else {
					Lrw[i][j] = - W[i][j] / D[i];
				}
			}
		}
	}
	
	protected void kmeans() throws Exception {
		RealMatrix rmL = new Array2DRowRealMatrix(Lrw);
		EigenDecomposition ed = new EigenDecomposition(rmL);
		double[] vEV = ed.getRealEigenvalues();
		
		List<DoubleIntPair> lstPair = new ArrayList<DoubleIntPair>();
		for(int i=0; i<vEV.length; ++i) {
			lstPair.add(new DoubleIntPair(-vEV[i],i));
		}
		Collections.sort(lstPair);
				
		//create a new dataset
		Schema schema = new Schema();
		for(int j=0; j<H; ++j) {
			NumericalVariable v = new NumericalVariable(String.format("Comp%d", lstPair.get(j).getIndex()+1));
			schema.addVariable(v);
		}
		Dataset dataset2 = new Dataset(schema);
		for(int i=0; i<nRecord; ++i) {
			Record r = new Record(ds.get(i).getId(), ds.get(i).getName(), ds.get(i).getLabel(),schema);
			dataset2.add(r);
		}
		for(int j=0; j<H; ++j) {
			RealVector vTmp = ed.getEigenvector(lstPair.get(j).getIndex());
			for(int i=0; i<nRecord; ++i) {
				dataset2.get(i).set(j, vTmp.getEntry(i));				
			}
		}
		for(int i=0; i<nRecord; ++i) {			
			for(int j=0; j<H; ++j) {				
				normalizeRecord(dataset2.get(i));
			}
		}
		
		//dataset2.save("ds2.csv", "ds2.names");
		// apply kmeans
		double dMinObj = Double.MAX_VALUE;
		for(int run=0; run<10; ++run) {
			Kmean km = new Kmean();
			Storage Arg = km.getArguments();
			Arg.insert("dataset", dataset2);
			Arg.insert("numcluster", numclust);
			Arg.insert("delta", 1e-6);
			Arg.insert("maxiter", 100);
			Arg.insert("im", "RandomMethod");
			Arg.insert("seed", run+1);
			
			km.run();
			
			Storage res = km.getResults();
			double dObj = res.getReal("dobj");
			if(dObj < dMinObj) {
				dMinObj = dObj;
				kmResults = res;
			}
		}
	}
	
	protected void normalizeRecord(Record r) {
		double dMax = -Double.MAX_VALUE;
		for(int j=0; j<r.dimension(); ++j) {
			if(dMax < Math.abs(r.get(j))) {
				dMax = Math.abs(r.get(j));
			}
		}
		
		if(dMax < Double.MIN_VALUE) {
			return;
		}
		
		double dSum = 0.0;
		for(int j=0; j<r.dimension(); ++j) {
			dSum += Math.pow(r.get(j)/dMax, 2.0);
		}
		dSum = 1.0 / Math.sqrt(dSum);
		for(int j=0; j<r.dimension(); ++j) {
			double dTemp = r.get(j) * dSum / dMax;
			r.set(j, dTemp);
		}
	}

}
