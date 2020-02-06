package clustering.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommonFunction {
	public static void normalize(double[] v) {
		double sum = 0.0;
		for(int j=0; j<v.length; ++j) {
			sum += v[j];
		}
		for(int j=0; j<v.length; ++j) {
			v[j] /= sum;
		}
	}
	
	/* return exp(v[i]) / (sum exp(v[i])) */ 
	public static void expNormalize(double[] v) {
		double max = v[0];
		for (int i = 1; i < v.length; i++) {
			if (v[i] > max) {
				max = v[i];
			}
		}

		double sum = 0;
		for (int i = 0; i < v.length; i++) {
			v[i] = Math.exp(v[i] - max);
			sum += v[i];
		}

		for (int i = 0; i < v.length; i++) {
			v[i] /= sum;			
		}
	}

	public static void log(String message, double[][] m) {
		Logger logger = LogManager.getRootLogger();
		String newline = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append(message).append(newline);
		for(int i=0; i<m.length; ++i) {
			for(int j=0; j<m[i].length; ++j) {
				sb.append(String.format(",%f", m[i][j]));
			}
			sb.append(newline);
		}
		logger.info(sb.toString());
	}
	
	public static void log(String message, double[] m) {
		Logger logger = LogManager.getRootLogger();
		String newline = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append(message).append(newline);
		for(int i=0; i<m.length; ++i) {			
			sb.append(String.format(",%f", m[i]));
		}
		logger.info(sb.toString());
	}
	
	public static void log(String message, double d) {
		Logger logger = LogManager.getRootLogger();
		String newline = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%s,%f",message, d)).append(newline);
		logger.info(sb.toString());
	}
	
	public static void log(String message) {
		Logger logger = LogManager.getRootLogger();		
		logger.info(message);
	}
}
