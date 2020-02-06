package clustering.util;

public class TriangleKernel implements Kernel {

	//@Override
	public double value(double u) {
		if(Math.abs(u)<1) {
			return 1 - Math.abs(u);			
		} else {
			return 0;
		}
	}

}
