package clustering.initialization;

import java.util.*;

import clustering.distance.SquaredEuclideanDistance;
import clustering.util.*;

public class KmeanppMethod extends InitializationMethod {
	protected int seed;
	protected int popSize;
	
	protected void setupArguments() throws Exception {
		super.setupArguments();
		seed = arguments.getInt("seed");	
		popSize = Math.min(ds.size(), numSample * arguments.getInt("M"));
	}
	
	@Override
	protected void work() {
        Random generator = new Random(seed);        
		int numRecords = ds.size();
		List<DoubleIntPair> lstPop = new ArrayList<DoubleIntPair>();
		for(int i=0; i<numRecords; ++i) {
			lstPop.add(new DoubleIntPair(generator.nextDouble(), i));
		}
		Collections.sort(lstPop);
		int[] vPop = new int[popSize];
		for(int i=0; i<popSize; ++i) {
			vPop[i] = lstPop.get(i).getIndex();
		}
		
        vIndex = new int[numSample];
        vIndex[0] = vPop[0];
        double[][] mDist = new double[popSize][numSample];
        SquaredEuclideanDistance dist = new SquaredEuclideanDistance();
        double[] vProb = new double[popSize];
        double[] vMin = new double[popSize];
        Arrays.fill(vMin, Double.MAX_VALUE);
        for(int i=1;i<numSample;++i){  
        	double dSum = 0.0;
            for(int j=0; j<popSize; ++j) {
            	mDist[j][i-1] = dist.dist(ds.get(vIndex[i-1]), ds.get(vPop[j]));
            	if(mDist[j][i-1] < vMin[j]) {
            		vMin[j] = mDist[j][i-1];
            	}
            	dSum += vMin[j];
            }
            vProb[0] = vMin[0] / dSum;
            for(int j=1; j<popSize; ++j) {
            	vProb[j] = vProb[j-1] + vMin[j] / dSum;
            }
            double dRand = generator.nextDouble();
            
            for(int j=1; j<popSize; ++j) {
            	if(dRand >= vProb[j-1] && dRand < vProb[j]) {
            		vIndex[i] = vPop[j];
            	}
            }
        }
        	
	}
}
