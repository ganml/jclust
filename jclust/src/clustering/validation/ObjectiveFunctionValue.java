package clustering.validation;

import clustering.util.Storage;;

public class ObjectiveFunctionValue extends ValidationIndex {

	@Override
	public double getIndex(Storage res) throws Exception {
		
		return res.getReal("dobj");
	}

}
