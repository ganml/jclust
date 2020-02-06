package clustering.util;

public class DoubleIntPair implements Comparable<DoubleIntPair> {
	private double value;
	private int index;
	private int index2;

	public DoubleIntPair() {
		value = 0.0;
		index = 0;
		index2 = 0;
	}
	
	public DoubleIntPair(double value, int index) {
		this.value = value;
		this.index = index;
	}

	public DoubleIntPair(double value, int index, int index2) {
		this.value = value;
		this.index = index;
		this.index2 = index2;
	}
	
	//@Override
	public int compareTo(DoubleIntPair arg0) {		
		return Double.compare(arg0.getValue(), value); // descending order
	}

	public double getValue() {
		return value;
	}
	
	public void set(double value, int index) {
		this.value = value;
		this.index = index;
	}
	
	public void set(double value, int index, int index2) {
		this.value = value;
		this.index = index;
		this.index2 = index2;
	}
	
	public int getIndex() {
		return index;
	}
	
	public int getIndex2() {
		return index2;
	}
}
