package clustering.util;

import java.util.*;

//import org.apache.log4j.Logger;

public class SparseVector {
	//private  static Logger log = Logger.getLogger(SparseVector.class.getName());
	private final int n;             // length
    private Map<Integer, Double> st;  // the vector, represented by index-value pairs

    // initialize the all 0s vector of length n
    public SparseVector(int n) {
        this.n  = n;
        this.st = new HashMap<Integer, Double>();
    }
    
    public Set<Integer> keySet() {
    	return st.keySet();
    }

    // put st[i] = value
    public void put(int i, double value) {
         if (value == 0.0) st.remove(i);
        else              st.put(i, value);
    }

    // return st[i]
    public double get(int i) {
        if (st.containsKey(i)) return st.get(i);
        else                return 0.0;
    }

    // return the number of nonzero entries
    public int nnz() {
        return st.size();
    }

    // return the size of the vector
    public int size() {
        return n;
    }

    // return the dot product of this vector with that vector
    public double dot(SparseVector that) {
        if (this.n != that.size()) throw new IllegalArgumentException("Vector lengths disagree");
        double sum = 0.0;

        // iterate over the vector with the fewest nonzeros
        if (this.st.size() <= that.keySet().size()) {
            for (int i : this.st.keySet())
                sum += this.get(i) * that.get(i);
        }
        else  {
            for (int i : that.keySet())
                sum += this.get(i) * that.get(i);
        }
        return sum;
    }

    // return the 2-norm
    public double norm() {
        return Math.sqrt(this.dot(this));
    }
    
    public void normalize() {
    	double dNorm = norm();
    	//log.info("dNorm, " + dNorm);
    	if(dNorm == 0.0) {
    		return;
    	}
    	for(int i : st.keySet()) {
    		st.put(i, st.get(i) / dNorm);
    	}
    }
    
    public void normalize2() {
    	double dSum = 0.0;
    	for(int i : st.keySet()) {
    		dSum += st.get(i);
    	}
    	if(dSum == 0.0) {
    		dSum = 1.0;
    	}
    	for(int i : st.keySet()) {
    		st.put(i, st.get(i) / dSum);
    	}
    }
    
    public SparseVector truncate(int p) {
    	SparseVector res = new SparseVector(n);
    	List<DoubleIntPair> lst = new ArrayList<DoubleIntPair>();
    	for(int i:st.keySet()) {
    		lst.add(new DoubleIntPair(st.get(i), i));
    	}
    	Collections.sort(lst);
    	if(p>st.keySet().size()) {
    		p = st.keySet().size();
    	}
    	for(int i=0; i<p; ++i) {
    		res.put(lst.get(i).getIndex(), lst.get(i).getValue());
    	}
    	res.normalize();
    	return res;
    }

    // return alpha * this
    public SparseVector scale(double alpha) {
        SparseVector result = new SparseVector(n);
        for (int i : this.st.keySet())
            result.put(i, alpha * this.get(i));
        return result;
    }

    // return this + that
    public SparseVector plus(SparseVector that) {
         SparseVector result = new SparseVector(n);
        for (int i : this.st.keySet()) result.put(i, this.get(i));
        for (int i : that.st.keySet()) result.put(i, that.get(i) + result.get(i));
        return result;
    }

    public void add(double coef, SparseVector that) {
        for (int i : that.st.keySet()) {
        	this.put(i, coef * that.get(i) + this.get(i));
        }
    }
    
    // return a string representation
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i : st.keySet()) {
            s.append("(" + i + ", " + st.get(i) + ") ");
        }
        return s.toString();
    }
}
