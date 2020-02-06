package clustering.util;

public class GaussianKernel implements Kernel {

	public double value(double u) {		
		return Math.exp(-0.5*u*u) / Math.sqrt(2*Math.PI);
	}

}
