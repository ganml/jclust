package clustering.util;

public class OrderedTwoIntKey {
	public final int k1;
	public final int k2;
	
	public OrderedTwoIntKey(int k1, int k2) { 
		this.k1 = k1; 
		this.k2 = k2;		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + k1;
		result = prime * result + k2;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrderedTwoIntKey other = (OrderedTwoIntKey) obj;
		if (k1 != other.k1)
			return false;
		if (k2 != other.k2)
			return false;
		return true;
	}
	
	
}
