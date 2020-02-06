package clustering.dataset;

import java.util.*;

public class Schema {
	private List<Variable> variables;
	
	public Schema() {
		variables = new ArrayList<Variable>();
	}
	
	public void addVariable(Variable v) {
		variables.add(v);
	}
	
	public Variable getVariable(int index) {
		return variables.get(index);
	}
	
	public int size() {
		return variables.size();
	}	
}
