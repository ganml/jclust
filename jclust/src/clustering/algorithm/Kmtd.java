package clustering.algorithm;

import java.util.ArrayList;
import java.util.List;
import clustering.cluster.*;
import clustering.dataset.Record;
import clustering.initialization.InitializationMethod;
import clustering.util.CommonFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class Kmtd extends ClusteringAlgorithm {
	// parameters
	protected int  numclust;
	protected double nu;
	protected double delta;
	protected double theta;
	protected double thegema;
	protected int maxiter;
	
	// results	
	protected List<Cluster> lstcluster;
	protected int[] CM;
	protected double[][] Z;
	protected double[][] U;
	protected double[][] UPre;
	protected double[][] D;
	protected double[] W;
	protected double dMaxUDiff;
	protected double dAvgDist;
	protected int numiter;
	protected InitializationMethod im;
	protected int nChanges;
	
	@Override
	protected void work() throws Exception {
		initialization(); 
        iteration(); 
	}

	protected void setupArguments() throws Exception {
		super.setupArguments();
		nu = arguments.getReal("nu");	
		delta = arguments.getReal("delta");
		theta = arguments.getReal("theta");
		numclust = arguments.getInt("numcluster");
		maxiter = arguments.getInt("maxiter");
		
		String imName = arguments.getStr("im");
		Class<?> clazz = Class.forName("clustering.initialization."+imName);	
		im = (InitializationMethod) clazz.newInstance();
		im.setArguments(arguments);
	}
	
	@Override
	protected void fetchResults() throws Exception {		
        lstcluster = new ArrayList<Cluster>();       
		for(int i=0;i<numclust;++i){   
            Cluster c = new Cluster(String.format("C%d", i+1));  
            lstcluster.add(c);            
        }
		for(int i=0;i<nRecord;++i) {
            double dMax = -Double.MAX_VALUE;
            int kind = -1;
            for(int k=0;k<numclust;++k) { 
                if(dMax < U[i][k]) {
            		dMax = U[i][k];
            		kind = k;
            	}
            }
            CM[i] = kind;
		}
		CommonFunction.log("numiter", numiter);
		for(int i=0; i<nRecord; ++i) {
			lstcluster.get(CM[i]).add(ds.get(i));
		}

		PartitionClustering pc = new PartitionClustering(ds, lstcluster);
		
        results.insert("pc", pc);
        results.insert("pc2", pc);
        results.insert("numiter", new Integer(numiter));      
        results.insert("center", Z);
   
	}

	protected void initialization() throws Exception {     	
        CM = new int[nRecord];
        Z = new double[numclust][nDimension];
        U = new double[nRecord][numclust];
        D = new double[nRecord][numclust];
        W = new double[numclust];
        
        im.run();        
        int[] vInd = (int[]) im.getResults().get("index");  
        lstcluster = new ArrayList<Cluster>();       
		for(int i=0;i<numclust;++i){            
            Record cr = ds.get(vInd[i]);
            for(int j=0; j<nDimension; ++j) {
            	Z[i][j] = cr.get(j);
            }
            Cluster c = new Cluster(String.format("C%d", i+1));            
            lstcluster.add(c);     
            W[i] = 1.0 / numclust;
        }
		
        updateU();
         
	}
	
	protected void iteration() {
        numiter = 1;
        getthegema();
        

        while(true) {
        	updateZ();
        	updateW();
        	updateU();
        	
        	
            if(dMaxUDiff < delta){
                break;
            }

            ++numiter;
            if (numiter > maxiter){
                break;
            }
        }

	}
	protected double getthegema() {
		double [] single =new double[nDimension];
		RealVector mid = new ArrayRealVector(single);
		double jvlisum=0;
		double jvliavg=0;
		for(int j=0;j<nRecord;j++){
			Record record = ds.get(j);
			double[] values = record.getValues();
			RealVector vector = new ArrayRealVector(values);			
			mid=mid.add(vector);
			}
		RealVector pingjun = mid.mapDivide(nRecord);		
		for(int j=0;j<nRecord;j++){
			Record record = ds.get(j);
			double[] values = record.getValues();
			RealVector vector = new ArrayRealVector(values);		
			double jvli=vector.getDistance(pingjun);
			jvlisum+=jvli*jvli;
			}
		jvliavg=Math.pow(jvlisum/nRecord,0.5);
		thegema=jvliavg/numclust;
		return thegema;
		}
	protected void updateU() {
		UPre = U;
		U = new double[nRecord][numclust];
		
		dMaxUDiff = 0;
        for(int i=0;i<nRecord;++i) {
            double dSum = 0.0;
            for(int k=0;k<numclust;++k) { 
                D[i][k] = dist(ds.get(i),k) + theta*theta*nu*thegema*thegema*((nu-2)/nu) + 1e-8;
                U[i][k] = W[k] * Math.pow(D[i][k] , -(nu + nDimension) / 2.0);
                dSum += U[i][k];
            }
            
            for(int k=0;k<numclust;++k) { 
            	U[i][k] /= dSum;
            	
            	if(dMaxUDiff < Math.abs(U[i][k] - UPre[i][k])) {
            		dMaxUDiff = Math.abs(U[i][k] - UPre[i][k]);
            	}
            }
        }
	
	}
	
	protected void updateZ() {
		double dSum1, dSum2;  
        for(int k=0;k<numclust;++k){ 
            for(int j=0;j<nDimension;++j){
                dSum1 = 0.0;
                dSum2 = 0.0;         
                for(int i=0; i<nRecord; ++i) {
                	dSum1 += U[i][k] * ds.get(i).get(j) / D[i][k];
                	dSum2 += U[i][k] / D[i][k];
                }
                Z[k][j] = dSum1 / dSum2;
            }
        }
	}
	
	protected void updateW() {
		double dSum;
		for(int k=0; k<numclust; ++k) {
			dSum = 0.0;
			for(int i=0; i<nRecord; ++i) {
				dSum += U[i][k];
			}
			W[k] = dSum / nRecord;
		}
	}
			
	protected double dist(Record x, int l) {
		double dSum = 0.0;		
		for(int j=0; j<nDimension; ++j) {
			dSum += Math.pow(x.get(j) - Z[l][j], 2.0);
		}
		return dSum;
	}
	
	
}
