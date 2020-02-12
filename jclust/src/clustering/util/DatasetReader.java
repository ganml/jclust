package clustering.util;

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import clustering.dataset.*;

public class DatasetReader {
	private Dataset dataset;
	private Schema schema;
	private int idColumnIndex;
	private int classColumnIndex;
	private List<Integer> columnIndex;
	private int numColumn;
	private String dataFileName;
	private String schemaFileName;
	private  static Logger log = LogManager.getLogger(DatasetReader.class.getName());
	
	public DatasetReader(String dataFileName, String schemaFileName) {
		this.dataFileName = dataFileName;
		this.schemaFileName = schemaFileName;
		columnIndex = new ArrayList<Integer>();
		idColumnIndex = -1;
		classColumnIndex = -1;
	}
	
	public Dataset getDataset() {
		return dataset;
	}
	
	public void read() throws Exception {
		createSchema();
		createDataset();		
		log.info(String.format("Read %d x %d data", dataset.size(), schema.size()));
	}
	
	private void createSchema() throws Exception {
		FileInputStream fstream = new FileInputStream(schemaFileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String line;
		boolean bTag = false;
		while(true) {
			line = br.readLine();
			if(line == null) {
				break;
			}
			if(line.trim().startsWith("///:")) {
				bTag = true;
				break;
			}
		}
		
		if(!bTag) {
			fstream.close();
			br.close();
			throw new Exception("Cannot find ///: in the schema file: " + schemaFileName);
		}
		
		schema = new Schema();
		
		columnIndex = new ArrayList<Integer>();
		int colIndex = 0;
		String[] cell;
		while(true) {
			line = br.readLine();
			if(line == null || line.trim().equals("")) {
				break;
			}
			
			cell = line.split(",");
			if(cell.length !=2) {
				br.close();
				throw new Exception("bad schema line: " + line);
			}
			
			if(cell[1].trim().toLowerCase().equals("recordid")) {
				idColumnIndex = colIndex;
			}
			if(cell[1].trim().toLowerCase().equals("continuous")) {
				NumericalVariable v = new NumericalVariable(cell[0]);				
				schema.addVariable(v);
				columnIndex.add(colIndex);
			}
			if(cell[1].trim().toLowerCase().equals("discrete")) {
				NominalVariable v = new NominalVariable(cell[0]);
				schema.addVariable(v);
				columnIndex.add(colIndex);
			}
			if(cell[1].trim().toLowerCase().equals("class")) {
				classColumnIndex = colIndex;	
			}
				
			colIndex++;
		}
		numColumn = colIndex;
		br.close();
		
		for(int j=0; j<schema.size(); ++j) {
			schema.getVariable(j).setId(j);
		}
	}
	
	private void createDataset() throws Exception {
		dataset = new Dataset(schema);
		FileInputStream fstream = new FileInputStream(dataFileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String line;
		String[] cell;
		int count = 0;
		while(true) {
			line = br.readLine();
			if(line == null || line.trim().equals("")) {
				break;
			}
			
			cell = line.split(",");
			if(cell.length != numColumn) {
				br.close();
				throw new Exception("bad data file line: " + line);
			}
			
			String rname = String.format("R%d", count);			
			if(idColumnIndex > -1) {
				rname = cell[idColumnIndex];
			} 
			
			String rlabel = "NA";
			if(classColumnIndex > -1) {
				rlabel = cell[classColumnIndex];
			} 
			Record record = new Record(count,rname,rlabel,schema);
			
			
			for(int i=0; i<schema.size(); ++i) {
				if(schema.getVariable(i).getType() == VariableType.Nominal) {
					NominalVariable v = (NominalVariable) schema.getVariable(i);
					record.set(i, v.getValue(cell[columnIndex.get(i)]));
				}
				if(schema.getVariable(i).getType() == VariableType.Numerical) {
					record.set(i, Double.parseDouble(cell[columnIndex.get(i)]));
				}
			}
			
			dataset.add(record);
			count++;
		}
		br.close();
	}
}
