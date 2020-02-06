package clustering.initialization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomMethod extends InitializationMethod {
	protected int seed;
	
	protected void setupArguments() throws Exception {
		super.setupArguments();
		seed = arguments.getInt("seed");		
	}
	
	@Override
	protected void work() {
		int numRecords = ds.size();
        List<Integer> index = new ArrayList<Integer>();        
        for(int i=0;i<numRecords;++i){
            index.add(i);            
        }
        
        vIndex = new int[numSample];
        Random generator = new Random(seed);        
        for(int i=0;i<numSample;++i){            
            int r = generator.nextInt(numRecords-i);            
            vIndex[i] = index.get(r);
            index.remove(r);
        }
        	
	}

}
