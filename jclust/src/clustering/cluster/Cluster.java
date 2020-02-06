package clustering.cluster;

import clustering.dataset.*;
import clustering.util.Storage;
import java.util.*;

public class Cluster extends Storage {
	protected String name;
	protected List<Record> records;
	protected Record nearestRecord;
	
	public Cluster(String name) {
		this.name = name;
		records = new ArrayList<Record>();		
		nearestRecord = null;
	}
	
	public String name() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<Record> getRecords() {
		return records;
	}
	
	public Record getRecord(int i) {
		assert i>=0 && i<records.size() : "getRecord index outbound";
		return records.get(i);
	}
	
	public void add(Record record) {
		records.add(record);
	}
	
	public void remove(Record record) {
		records.remove(record);
	}
	
	public boolean contains(Record record) {
		return records.contains(record);
	}
	
	public void removeAll() {
		records.clear();
	}
	
	public int size() {
		return records.size();
	}
	
	public Record getNearestRecord() {
		return nearestRecord;
	}
	
	public void calculateNearestRecord() {
		int d = records.get(0).dimension();
		int nSize = records.size();
		double[] vZ = new double[d];
		Arrays.fill(vZ, 0.0);
		for(int i=0; i<nSize; ++i) {
			for(int j=0; j<d; ++j) {
				vZ[j] += records.get(i).get(j);
			}
		}
		for(int j=0; j<d; ++j) {
			vZ[j] /= nSize;
		}
		
		double dMin = Double.MAX_VALUE;
		int iMin = -1;
		for(int i=0; i<nSize; ++i) {
			double dTmp = 0.0;
			for(int j=0; j<d; ++j) {
				dTmp +=  Math.pow(records.get(i).get(j) - vZ[j], 2.0);
			}
			if(dMin > dTmp) {
				dMin = dTmp;
				iMin = i;
			}
		}
		
		nearestRecord = records.get(iMin);
	}
	
	public Dataset toDataset() {
		Dataset ds = new Dataset(records.get(0).getSchema());
		for(int i=0; i<records.size(); ++i) {
			ds.add(records.get(i));
		}
		return ds;
	}
}
