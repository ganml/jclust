package clustering.validation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import clustering.util.Storage;


public abstract class ValidationIndex {
	protected Logger log = LogManager.getLogger(ValidationIndex.class);
	
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	public abstract double getIndex(Storage res) throws Exception;
	
}
