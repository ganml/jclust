package clustering.util;

public class SparseMatrix {
	private final int nrow;           // n-by-n matrix
	private final int ncol;
    private SparseVector[] rows;   // the rows, each row is a sparse vector

    // initialize an n-by-n matrix of all 0s
    public SparseMatrix(int nrow, int ncol) {
        this.nrow = nrow;
        this.ncol = ncol;
        rows = new SparseVector[nrow];
        for (int i = 0; i < nrow; i++)
            rows[i] = new SparseVector(ncol);
    }

    public int getRow() {
    	return nrow;
    }
    
    public int getCol() {
    	return ncol;
    }
    
    // put A[i][j] = value
    public void put(int i, int j, double value) {
        rows[i].put(j, value);
    }

    // return A[i][j]
    public double get(int i, int j) {
        return rows[i].get(j);
    }
    
    public SparseVector get(int i) {
    	return rows[i];
    }

    // return the number of nonzero entries (not the most efficient implementation)
    public int nnz() { 
        int sum = 0;
        for (int i = 0; i < nrow; i++)
            sum += rows[i].nnz();
        return sum;
    }

    // return a string representation
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("nrow = " + nrow + ", ncol = " + ncol + ", nonzeros = " + nnz() + "\n");
        for (int i = 0; i < nrow; i++) {
            s.append(i + ": " + rows[i] + "\n");
        }
        return s.toString();
    }
}
