package clustering.initialization;

import java.util.*;

import clustering.dataset.*;
import clustering.distance.*;

public class KaufmanMethod extends InitializationMethod {	
	protected Distance distance;
	
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
		List<Integer> lstS = new ArrayList<Integer>();
		
		double[] mean = ds.getMean();
		Record r1 = new Record(0, "", ds.getSchema());
		for(int j=0; j<mean.length; ++j) {
			r1.set(j, mean[j]);
		}
		// select the first seed
		double dMin = Double.MAX_VALUE;
		int sInd = -1;
		for(int i=0; i<ds.size(); ++i) {
			double dTemp = distance.dist(r1, ds.get(i));
			if(dMin > dTemp) {
				dMin = dTemp;
				sInd = i;
			}
		}
		lstS.add(sInd);
		
		// select the remaining seeds
		double[][] D = new double[ds.size()][ds.size()];
		for(int i=1; i<ds.size(); ++i) {
			for(int j=0; j<i; ++j) {
				D[i][j] = distance.dist(ds.get(i),ds.get(j));
				D[j][i] = D[i][j];
			}
		}
		List<Integer> index = new ArrayList<Integer>();
		for(int i=0; i<ds.size(); ++i) {
			index.add(i);
		}
		index.remove(sInd);
		for(int s=1; s<numSample; ++s) {
			dMin = Double.MAX_VALUE;
			double dMax = -Double.MAX_VALUE;
			sInd = -1;
			for(int i =0; i<index.size(); ++i) {
				double dSumC = 0.0;
				int ii = index.get(i);
				for(int j=0; j< index.size(); ++j) {
					int jj = index.get(j);
					double Dj = Double.MAX_VALUE;
					for(int k : lstS) {
						if(Dj > D[k][jj]) {
							Dj = D[k][jj];
						}
					}
					
					dSumC += Math.max(Dj - D[ii][jj], 0.0);
				}
				
				if(dMax < dSumC) {
					dMax = dSumC;
					sInd = i;
				}
			}
			
			lstS.add(index.get(sInd));
			index.remove(sInd);
		}
		vIndex = new int[numSample];
		for(int i=0; i<numSample; ++i) {
			vIndex[i] = lstS.get(i);
		}

	}

}
