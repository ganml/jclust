package clustering.dataset;

import java.util.*;
import java.io.*;

public class Dataset {
	private Schema schema;
	private List<Record> records;
	
	public Dataset(Schema schema) {
		this.schema = schema;
		records = new ArrayList<Record>();
	}
	
	public Schema getSchema() {
		return schema;
	}
	
	public void add(Record record) {
		records.add(record);
	}
	
	public Record get(int index) {
		return records.get(index);
	}
	
	public int size() {
		return records.size();
	}
	
	public int dimension() {
		return schema.size();
	}
	
	public double[] getStd() {
		double[] std = new double[schema.size()];
		if(records.size() < 1) {
			return std;
		}
		
		for(int k=0; k<schema.size(); ++k) {
			if(schema.getVariable(k).getType() == VariableType.Nominal) {
				std[k] = 1.0;
			} else {
				double m1 = records.get(0).get(k);
				double m2 = 0.0;				
				for(int i=1; i<records.size(); ++i) {
					double y = records.get(i).get(k) - m1;
					m2 += y * y * (i/ (i+1.0));
					m1 += y / (i+1.0);					
				}
								
				std[k] = Math.sqrt(m2 / (records.size()-1));
				
			}
		}		
		return std;
	}
	
	public double[] getMean() {
		double[] mean = new double[schema.size()];
		if(records.size() < 1) {
			return mean;
		}
		for(int k=0; k<schema.size(); ++k) {			
			double m1 = records.get(0).get(k);							
			for(int i=1; i<records.size(); ++i) {
				double y = records.get(i).get(k) - m1;				
				m1 += y / (i+1.0);					
			}
								
			mean[k] = m1;	
		}		
		return mean;
	}
	
	public void normalizeZscore() {
		if(records.size() == 1) {
			return;
		}
		
		for(int i=0; i<schema.size(); ++i) {
			if(schema.getVariable(i).getType() == VariableType.Nominal) {
				continue;
			}
			
			double n=0;
			double mean=0;
			double M2 = 0;
			double delta;
			
			for (Record x : records) {
		        n = n + 1;
		        delta = x.get(i) - mean;
		        mean = mean + delta/n;
		        M2 = M2 + delta*(x.get(i) - mean);
			}
		    double sigma = Math.sqrt(M2/(n - 1));
		    if(sigma < 1e-8) {
		    	sigma = 1.0;
		    }
		    for(Record x : records) {
		    	x.set(i, x.get(i) / sigma);
		    }
		}
	}
	
	public void save(String datafile, String schemafile) throws Exception {
		// write schema file
		StringBuilder sb = new StringBuilder();
		sb.append("schema file for the dataset " + datafile);
		sb.append(System.getProperty("line.separator"));
		sb.append("///:schema");
		sb.append(System.getProperty("line.separator"));
		sb.append("1,recordid");
		sb.append(System.getProperty("line.separator"));
		for(int i=0; i<schema.size(); ++i) {
			sb.append(schema.getVariable(i).getName()).append(",");
			if(schema.getVariable(i).getType() == VariableType.Nominal) {
				sb.append("discrete");
			} else {
				sb.append("continuous");
			}
			sb.append(System.getProperty("line.separator"));	
		}
		sb.append("class,class");
		sb.append(System.getProperty("line.separator"));
		
		FileWriter outFile = new FileWriter(schemafile);
		PrintWriter out = new PrintWriter(outFile);
		out.print(sb.toString());
		out.close();
		
		//write data file
		sb = new StringBuilder();
		for(Record r: records) {
			sb.append(r.getId());
			for(int j=0; j<schema.size(); ++j) {
				sb.append(String.format(",%.6f",r.get(j)));
			}
			sb.append(",").append(r.getLabel());
			sb.append(System.getProperty("line.separator"));
		}
		
		outFile = new FileWriter(datafile);
		out = new PrintWriter(outFile);
		out.print(sb.toString());
		out.close();
	}	
	
}
