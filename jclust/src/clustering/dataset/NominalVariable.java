package clustering.dataset;

import java.util.*;

public class NominalVariable extends Variable {
	private Map<String, Integer> valueMap;
	private List<Integer> values;
	
	public NominalVariable(String name) {
		super(name, VariableType.Nomial);
		valueMap = new HashMap<String, Integer>();
		values = new ArrayList<Integer>();
	}
	
	public int getValue(String svalue) {
		if(valueMap.containsKey(svalue)) {
			return valueMap.get(svalue);
		} else {
			int nvalue = valueMap.size();
			valueMap.put(svalue, nvalue);
			values.add(nvalue);
			return nvalue;
		}
	}
	
	public int numValues() {
		return values.size();
	}
	
	public String getValueInStr(int nvalue) {
		String res = null;
		for(String key : valueMap.keySet()) {
			if(valueMap.get(key) == nvalue) {
				res = key;
				break;
			}
		}
		return res;
	}
	
	public List<Integer> getValues() {
		return values;
	}

	@Override
	public double distance(double x, double y) {
		if( Math.abs(x-y) < 1e-8 ) {
		    return 0.0;
		} else {
			return 1.0;
		}
	}
}
