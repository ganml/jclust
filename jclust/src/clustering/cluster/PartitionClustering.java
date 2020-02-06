package clustering.cluster;

import java.io.*;
import java.util.*;

import clustering.dataset.*;
import clustering.util.ConfusionMatrix;

public class PartitionClustering {	
    protected Dataset ds;
	protected List<Cluster> lstClusters;
	List<String> lstLabel;
	List<String> lstLabelGiven;
	protected ConfusionMatrix cm;
    
    public PartitionClustering(Dataset ds, List<Cluster> lstClusters) throws Exception {
    	this.ds = ds;
    	this.lstClusters = lstClusters;
    	    	
    	lstLabel = new ArrayList<String>();
    	lstLabelGiven = new ArrayList<String>();
    	
    	for(int i=0; i<ds.size(); ++i) {
    		lstLabelGiven.add(ds.get(i).getLabel());
    		for(Cluster c : lstClusters) {
    			if(c.contains(ds.get(i))) {
    				lstLabel.add(c.name());
    				break;
    			}
    		}
    	}
    	
    	cm = new ConfusionMatrix(lstLabel, lstLabelGiven);
    }
    
    public PartitionClustering(Dataset ds, List<String> lstLabelGiven, List<Cluster> lstClusters) throws Exception {
    	this.ds = ds;
    	this.lstClusters = lstClusters;
    	this.lstLabelGiven = lstLabelGiven;
    	    	
    	lstLabel = new ArrayList<String>();
    	    	
    	for(int i=0; i<ds.size(); ++i) {    		
    		for(Cluster c : lstClusters) {
    			if(c.contains(ds.get(i))) {
    				lstLabel.add(c.name());
    				break;
    			}
    		}
    	}
    	
    	cm = new ConfusionMatrix(lstLabel, lstLabelGiven);
    }
           
    public ConfusionMatrix getConfusionMatrix() {
    	return cm;
    }
             
    public List<Cluster> getClusters() {
    	return lstClusters;
    }
    
    public List<String> getLabel() {
    	return lstLabel;
    }
    
    public Dataset getDataset() {
    	return ds;
    }
        
    public int getNumCluster() {
    	return lstClusters.size();
    }
        
    @Override
	public String toString() {
        
        String separator = System.getProperty( "line.separator" );
        StringBuilder sb = new StringBuilder();
        sb.append("Clustering Summary").append(separator);        
        sb.append("Number of clusters, ").append(lstClusters.size()).append(separator);
        for(int i=0;i<lstClusters.size();++i){
            sb.append("Size of Cluster ").append(i+1).append(", ").append(lstClusters.get(i).size());
            Record nearestRecord = lstClusters.get(i).getNearestRecord();
            if(nearestRecord != null) {
            	sb.append(',').append(nearestRecord.getName());
            }
            sb.append(separator);
        }
       
        sb.append("Confusion Matrix").append(separator); 
        sb.append(cm.toString());      
        
        return sb.toString();
	}
     
	public void save(String filename) throws IOException { 
		FileWriter outFile = new FileWriter(filename);
		PrintWriter out = new PrintWriter(outFile);
		out.print(toString());
		out.close();
	}
    
	public void saveClustering(String filename) throws Exception {
		String separator = System.getProperty( "line.separator" );
        StringBuilder sb = new StringBuilder();
        
        List<Integer> lstExemplar = new ArrayList<Integer>();
        for(Cluster c : lstClusters) {        	
    		Record r = (Record) c.get("center");
    		if(r!=null) {
    			lstExemplar.add(r.getId());
    		}
        	
        }
        
        sb.append("Record ID");
        Schema schema = ds.getSchema();
        for(int i=0; i<schema.size(); ++i) {
        	sb.append(',').append(schema.getVariable(i).getName());
        }
        
        sb.append(",found,given,center,exemplar").append(separator);
        
        for(int i=0; i<ds.size();++i) {
            sb.append(ds.get(i).getId());
            for(int j=0; j<schema.size(); ++j) {
            	sb.append(",").append(ds.get(i).get(j));
            }
            sb.append(",").append(lstLabel.get(i));                                      
            sb.append(",").append(lstLabelGiven.get(i));
            
            Cluster c = null;
            for(int l=0; l<lstClusters.size(); ++l) {
            	if(lstClusters.get(l).contains(ds.get(i))) {
            		c = lstClusters.get(l);
            		break;
            	}
            }
            Record r = (Record) c.get("center");
            if(r==null) {
            	sb.append(",").append(-1);
            } else {
            	sb.append(",").append(r.getId());
            }
        	if(lstExemplar.contains(i)){
        		sb.append(",").append(1);
        	} else {
        		sb.append(",").append(0);
        	}
            sb.append(separator);
        }
        
        FileWriter outFile = new FileWriter(filename);
		PrintWriter out = new PrintWriter(outFile);
		out.print(sb.toString());
		out.close();
	}
	       
   
}
