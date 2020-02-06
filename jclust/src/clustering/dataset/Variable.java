package clustering.dataset;

public abstract class Variable {	
	protected String name;
	protected VariableType type;
	protected boolean scalable;
	protected int id;
	
	public Variable(String name, VariableType type) {
		this.type = type;
		this.name = name;
		scalable = false;
	}
			
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public VariableType getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isScalable() {
		return scalable;
	}	
	
	public abstract double distance(double x, double y);
}
