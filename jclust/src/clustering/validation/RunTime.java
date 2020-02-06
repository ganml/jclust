package clustering.validation;

import clustering.util.Storage;

public class RunTime extends ValidationIndex {

	@Override
	public double getIndex(Storage res) throws Exception {
		
		return res.getReal("runtime");
	}

}
