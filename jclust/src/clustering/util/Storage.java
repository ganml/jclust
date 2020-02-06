package clustering.util;

import java.util.*;

import clustering.cluster.PartitionClustering;

public class Storage {
	public Map<String, Object> mapValue;
	
	public Storage() {
		mapValue = new HashMap<String, Object>();
	}
	
	public boolean contains(String name) {
		String nameL = name.toLowerCase();
		if (mapValue.containsKey(nameL)) {
			return true;
		} else {
			return false;
		}
	}
	
	public Object get(String name) throws Exception {
		String nameL = name.toLowerCase();
		if (mapValue.containsKey(nameL)) {
			return mapValue.get(nameL);
		} else {
			return null;
		}
	}
	
	public double getReal(String name) throws Exception {
		Object obj = get(name);
		if(obj instanceof Double) {
			Double dobj = (Double) obj;
			return dobj.doubleValue();
		} else if (obj instanceof String) {
			String sobj = (String) obj;
			return Double.parseDouble(sobj);
		} else {
			throw new Exception("Cannot convert to double: " + name);
		}
	}
	
	public int getInt(String name) throws Exception {
		Object obj = get(name);
		if(obj instanceof Integer) {
			Integer dobj = (Integer) obj;
			return dobj.intValue();
		} else if (obj instanceof String) {
			String sobj = (String) obj;
			return Integer.parseInt(sobj);
		} else {
			throw new Exception("Cannot convert to int: " + name);
		}
		
	}
	
	public String getStr(String name) throws Exception {
		String nameL = name.toLowerCase();
		if (mapValue.containsKey(nameL)) {
			return (String) mapValue.get(nameL);
		} else {
			return null;
		}
	}
	
	public void insert(String name, Object val) {
		mapValue.put(name.toLowerCase(), val);
	}
	
	public void clear() {
		mapValue.clear();
	}

	public String toString() {
		String newline = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		for(String key : mapValue.keySet()) {
			Object obj = mapValue.get(key);
			if(obj instanceof PartitionClustering) {
				PartitionClustering pc = (PartitionClustering) obj;
				sb.append(key).append(newline);
				sb.append(pc.toString());
			}
			if(obj instanceof Double ||
					obj instanceof Integer ||
					obj instanceof String) {
				sb.append(key).append(",").append(obj).append(newline);
			}
			if(obj instanceof double[]) {
				double[] vo = (double[]) obj;
				sb.append(key);
				for(int i=0; i<vo.length; ++i) {
					sb.append(",").append(vo[i]);
				}
				sb.append(newline);
			}
			if(obj instanceof double[][]) {
				double[][] vo = (double[][]) obj;
				sb.append(key);
				for(int i=0; i<vo.length; ++i) {
					sb.append(i);
					for(int j=0; j<vo[i].length; ++j) {
					    sb.append(",").append(vo[i][j]);
					}
					sb.append(newline);
				}
			}
			if(obj instanceof int[][]) {
				int[][] vo = (int[][]) obj;
				sb.append(key);
				for(int i=0; i<vo.length; ++i) {
					sb.append(i);
					for(int j=0; j<vo[i].length; ++j) {
					    sb.append(",").append(vo[i][j]);
					}
					sb.append(newline);
				}
			}
		}
		return sb.toString();
	}
}
