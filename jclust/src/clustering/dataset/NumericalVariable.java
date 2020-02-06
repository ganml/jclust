package clustering.dataset;

public class NumericalVariable extends Variable {
	
	public NumericalVariable(String name) {
		super(name, VariableType.Numerical);
	}
	
	public NumericalVariable(String name, boolean scalable) {
		super(name, VariableType.Numerical);
		this.scalable = scalable;
	}

	@Override
	public double distance(double x, double y) {		
		return x-y;
	}
}
