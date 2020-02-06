package clustering.util;

public class QuarticKernel implements Kernel {

	//@Override
	public double value(double u) {
		if(Math.abs(u)<1) {
			double dTemp = 1 - u*u;
			return dTemp * dTemp * 15 / 16;
		} else {
			return 0;
		}
	}

}
