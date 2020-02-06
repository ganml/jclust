package clustering.initialization;

import java.util.*;
import org.apache.commons.math3.random.MersenneTwister;
import clustering.distance.*;
import clustering.util.DoubleIntPair;

public class MaxMinMethod extends InitializationMethod {
	private int seed;
	private int numSample;
	private Distance distance;
	
	protected void setupArguments() throws Exception {
		super.setupArguments();
		String distName = arguments.getStr("distance");	
		
		Class<?> clazz = Class.forName("clustering.distance."+distName);	
		distance = (Distance) clazz.newInstance();
		distance.setArguments(arguments);
		distance.initialize();
	}
	
	@Override
	protected void work() {
		MersenneTwister mt = new MersenneTwister(seed);  				
		vIndex = new int[numSample];
		double dMax = -Double.MIN_VALUE;		
		for(int i=0; i<numSample; ++i) {
			List<DoubleIntPair> lstPair = new ArrayList<DoubleIntPair>();
			for(int j=0; j<ds.size(); ++j) {
				lstPair.add(new DoubleIntPair(mt.nextDouble(), j));
			}
			Collections.sort(lstPair);
			
			double dMinDist = Double.MAX_VALUE;
			for(int j=1; j<numSample; ++j) {
				for(int k=0; k<j; ++k) {
					double dTemp = distance.dist(ds.get(lstPair.get(j).getIndex()), 
							ds.get(lstPair.get(k).getIndex()));
					if(dMinDist > dTemp) {
						dMinDist = dTemp;						
					}
				}
			}
			
			if(dMax < dMinDist) {
				dMax = dMinDist;
				for(int j=0; j<numSample; ++j) {
					vIndex[j] = lstPair.get(j).getIndex();
				}
			}
		}
				
	}

}
