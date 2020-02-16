package clustering.util;

import java.io.*;
import java.util.*;

import org.apache.commons.math3.stat.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import clustering.algorithm.*;
import clustering.cluster.*;
import clustering.dataset.Dataset;
import clustering.validation.*;

public class AlgorithmRunner {
	protected Logger log = LogManager.getLogger(AlgorithmRunner.class);
	protected int numThread;
	protected String runFile;
	protected String resultFilePrefix;
	protected String validationIndex;
	protected List<Map<String, String>> lmRuns;
	protected int outputLevel; // 0 - aggregate 1 - seriatim 2 - individual run info 3 - individual run file
	protected int next;
	protected List<ValidationIndex> listVI;
	
	protected Map<String, double[]> mResult;
	protected String newline = System.getProperty("line.separator");
	
	public AlgorithmRunner(String runFile, String resultFilePrefix, int numThread, int outputLevel, 
			String validationIndex) {
		this.runFile = runFile;
		this.numThread = numThread;		
		this.resultFilePrefix = resultFilePrefix;
		this.outputLevel = outputLevel;
		this.validationIndex = validationIndex;
	}
	
	public void run() throws Exception {
		readRunFile();
		createValidationIndex();
		
		next = 0;
		mResult = new HashMap<String, double[]>();
		if (outputLevel >= 1) {
			StringBuilder sb = new StringBuilder();
			sb.append("RunIndex,Algorithm,DataSet,Run");
			for(int i=0; i<listVI.size(); ++i) {
				sb.append(",").append(listVI.get(i).getName());
			}
			FileWriter outFile = new FileWriter(resultFilePrefix + "_seriatim.csv");
			PrintWriter out = new PrintWriter(outFile);
			out.println(sb.toString());
			out.close();
		}
		if(outputLevel >=2 ) {
			FileWriter outFile = new FileWriter(resultFilePrefix + "_detail.csv");
			PrintWriter out = new PrintWriter(outFile);
			out.println("Run detail");
			out.close();
		}
		List<Thread> listThread = new ArrayList<Thread>();
		for(int i=0; i<numThread; ++i) {
			class MyTask implements Runnable {							
			    public void run(){
			    	work();
			    }				  
			}
			
			Thread t = new Thread(new MyTask());
			listThread.add(t);
			t.start();
		}
        
		for(Thread t: listThread) {
			t.join();
		}
		
		saveResult();
	}
	
	protected void work() {
		while(true) {
	    	int count;					    	
	    	synchronized(this) {
	    		count = next;	    		
	    		++next;				    		
	    	}
	    	
	    	if(count >= lmRuns.size()) {
	    		break;
	    	}
	    	log.info(String.format("Processing %d of %d", count+1, lmRuns.size()));
	    	Map<String, String> mParam = lmRuns.get(count);
	        
    		try {
    			String runIndex = mParam.get("RunIndex");
    			String algName = mParam.get("Algorithm");
		    	String dataFolder = mParam.get("DataFolder");
		    	String dataFile = mParam.get("DataFile");
		    	String schemaFile = mParam.get("SchemaFile");
		    	String normalize = mParam.get("Normalize");
		    	
		    	int numRun = Integer.parseInt(mParam.get("NumRun"));
		    	
		    	File fDataFolder = new File(dataFolder);
    			File fDataFile = new File(fDataFolder, dataFile);
    			File fSchemafile = new File(fDataFolder, schemaFile);
		    	
    			DatasetReader dr = new DatasetReader(fDataFile.getPath(), fSchemafile.getPath());
    			dr.read();
    			if(normalize.toLowerCase().equals("yes")) {
    				CommonFunction.log("normalizing dataset ...");
    				dr.getDataset().normalizeZscore();
    			}
    			Dataset ds = dr.getDataset();
    			    			
		    	Class<?> clazz = Class.forName("clustering.algorithm."+algName);		    	
		    	double[][] mStat = new double[listVI.size()][numRun];
		    	
		    	StringBuilder sb1 = new StringBuilder();
		    	StringBuilder sb2 = new StringBuilder();
		    	List<List<String>> mCM = new ArrayList<List<String>>();
		    	for(int run=1; run<=numRun; ++run) {
		    		//log.info(String.format("iter %d", run));					
			    	ClusteringAlgorithm ca = (ClusteringAlgorithm)clazz.newInstance();
			    	
			        Storage Arg = ca.getArguments();
			        Arg.insert("dataset", ds);
			        for(String key : mParam.keySet()) {
			        	Arg.insert(key, mParam.get(key));
			        }		        
			        if (numRun == 1) {	
			        	int seed = Integer.parseInt(mParam.get("seed"));
		                Arg.insert("seed", seed);
		            } else {	                
		                Arg.insert("seed", run+numRun);
		            }						        
			        		        
					ca.run();							
		        
			        Storage tmp = ca.getResults();	   
			        PartitionClustering pc = (PartitionClustering) tmp.get("pc"); 			        
			        mCM.add(pc.getLabel());
			        
			        for(int i=0; i<listVI.size(); ++i) {
			        	mStat[i][run-1] = listVI.get(i).getIndex(tmp);
			        }			        
			        
			        if(outputLevel >= 1) {
			        	sb1.append(String.format("Run%s,%s,%s,%d", 
			        			runIndex, algName, dataFile, run));
			        	for(int i=0; i<listVI.size(); ++i) {
			        		sb1.append(String.format(",%.6f", mStat[i][run-1]));
			        	}
			        	sb1.append(newline);
			        }
			        if(outputLevel >= 2) {
			        	sb2.append(String.format("Run%s,%s,%s,%d", 
			        			runIndex, algName, dataFile, run));
			        	for(int i=0; i<listVI.size(); ++i) {
			        		sb2.append(String.format(",%.6f", mStat[i][run-1]));
			        	}
			        	sb2.append(newline);			        	
			        	sb2.append(tmp.toString());
				        sb2.append("Parameters used: ").append(newline);
				        sb2.append(Arg.toString());
			        }
			        if(outputLevel >=3) {
			        	pc.saveClustering(String.format("%s_%s_%s_%s_%d.csv", resultFilePrefix,
			        			runIndex,algName,dataFile.replaceFirst("[.][^.]+$", ""), run));
			        }
		    	}
		    	
		    	double[] result = new double[mStat.length * 2];
		    	for(int i=0; i<mStat.length; ++i) {
		    		result[2*i] = StatUtils.mean(mStat[i]);
		    		result[2*i+1] = Math.sqrt(StatUtils.variance(mStat[i]));
		    	}
		    	synchronized(this) {
		        	mResult.put(runIndex, result);
		        	
		        	if (outputLevel >= 1) {
		    			FileWriter outFile = new FileWriter(resultFilePrefix + "_seriatim.csv", true);
		    			PrintWriter out = new PrintWriter(outFile);
		    			out.print(sb1.toString());
		    			out.close();
		    		}
		    		if(outputLevel >=2 ) {
		    			FileWriter outFile = new FileWriter(resultFilePrefix + "_detail.csv", true);
		    			PrintWriter out = new PrintWriter(outFile);
		    			out.println(sb2.toString());
		    			out.close();
		    		}
		        }
		    	
		    	String strCMFile = String.format("CM%d_%s.csv",count,algName);
		    	FileWriter outFile = new FileWriter(strCMFile);
		    	for(int j=0; j<numRun-1; ++j) {
		    		outFile.write("Run" + (j+1));
		    		outFile.write(',');
		    	}
		    	outFile.write("Run" + numRun);
		    	outFile.write(newline);
		    	for(int i=0; i<ds.size(); ++i) {
		    		for(int j=0; j<numRun-1; ++j) {
			    		outFile.write(mCM.get(j).get(i));
			    		outFile.write(',');
			    	}
			    	outFile.write(mCM.get(numRun-1).get(i));
			    	outFile.write(newline);
		    	}
		    	outFile.close();
		    	
	        } catch (Exception e) {									
				e.printStackTrace();
			}
	    	
    	}
	}
	
	protected void saveResult() throws IOException {
		StringBuilder sb = new StringBuilder();
		
		sb.append("RunIndex,Algorithm,DataSet");
		for(int i=0; i<listVI.size(); ++i) {
			sb.append(String.format(",%s(Avg),%s(Std)", listVI.get(i).getName(),
					listVI.get(i).getName()));
		}
		sb.append(newline);
		for(int i=0; i<lmRuns.size(); ++i) {
			Map<String,String> mTemp = lmRuns.get(i);
			String runIndex = mTemp.get("RunIndex");
			
			sb.append(String.format("%s,%s,%s", runIndex, mTemp.get("Algorithm"), mTemp.get("DataFile")));
			double[] vRes = mResult.get(runIndex);
			for(int j=0; j<vRes.length; ++j) {
				sb.append(String.format(",%.6f", vRes[j]));
			}
			sb.append(newline);
		}
		
		FileWriter outFile = new FileWriter(resultFilePrefix + "_aggregate.csv");
		PrintWriter out = new PrintWriter(outFile);
		out.print(sb.toString());
		out.close();
	}
	
	protected void readRunFile() throws Exception {
		FileInputStream fstream = new FileInputStream(runFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String headerLine = br.readLine();
		String[] header = headerLine.split(",");
		for(int i=0; i<header.length; ++i) {
			header[i] = header[i].trim();
			if(header[i].equals("")) {
				br.close();
				throw new Exception("Empty header name: " + headerLine);
			}
		}
		lmRuns = new ArrayList<Map<String, String>>();
		
		String line;
		while(true) {
			line = br.readLine();
			if(line == null || line.trim().equals("")) {
				break;
			}
			
			String[] cell = line.split(",");		
			if(cell.length==0 || cell[0].trim().equals("")) {
				break;
			}
			if(cell.length != header.length) {
				br.close();
				throw new Exception("bad run file line: " + line);
			}
			
			
			
			Map<String, String> mTemp = new HashMap<String, String>();
			for(int i=0; i<header.length; ++i) {				
				mTemp.put(header[i], cell[i].trim());
			}
			
			lmRuns.add(mTemp);
		}
		
		br.close();
		log.info(String.format("Number of Runs loaded: %d", lmRuns.size()));
	}
	
	protected void createValidationIndex() throws Exception {
		String[] vname = validationIndex.split(":");
		listVI = new ArrayList<ValidationIndex>();
		
		for(String name : vname) {
			Class<?> clazz = Class.forName("clustering.validation."+name);
			listVI.add((ValidationIndex) clazz.newInstance());
		}
	}
}
