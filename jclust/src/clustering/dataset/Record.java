package clustering.dataset;

public class Record {
	private int id;
	private String label;
	private String name;
	private Schema schema;
	private double[] values;
	
	public Record(int id, String name, Schema schema) {
		this.id = id;
		this.name = name;
		this.schema = schema;
		label = "NA";
		values = new double[schema.size()];
	}
	
	public Record(int id, String name, String label, Schema schema) {
		this.id = id;
		this.name = name;
		this.label = label;
		this.schema = schema;
		values = new double[schema.size()];
	}
	
	public Record(Record record) {
		this.schema = record.getSchema();
		values = new double[schema.size()];
		for(int i=0; i<schema.size(); ++i) {
			values[i] = record.get(i);
		}
		this.id = record.getId();
	}
	
	public int dimension() {
		return values.length;
	}
	
	public int getId() {
		return id;
	}
		
	public String getName() {
		return name;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void set(int index, double value) {
		values[index] = value;
	}
	
	public double get(int index) {
		return values[index];
	}
	
	public double[] getValues() {
		return values;
	}
	
	public Schema getSchema() {
		return schema;
	}
}
