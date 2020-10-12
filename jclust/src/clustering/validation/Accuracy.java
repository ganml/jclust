package clustering.validation;

import java.util.*;

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import clustering.cluster.PartitionClustering;
import clustering.util.ConfusionMatrix;
import clustering.util.Storage;

public class Accuracy extends ValidationIndex {
	//private int[][] _crosstab;
	
	@Override
	public double getIndex(Storage res) throws Exception {
		PartitionClustering pc = (PartitionClustering) res.get("pc"); 
		ConfusionMatrix cm = pc.getConfusionMatrix();
		
		int[][] mTable = cm.getTable();
		double dMax = solve(mTable);
		
		return dMax / pc.getDataset().size(); 
	}
	
	protected double solve(int[][] W) {
		int n = W.length;
		int m = W[0].length;
		
		double[] vCoef = new double[n*m];
		for(int i=0; i<n; ++i) {
			for(int j=0; j<m; ++j) {
				vCoef[i *n + j] = W[i][j];
			}
		}
		
		LinearObjectiveFunction f = new LinearObjectiveFunction(vCoef, 0);		
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        for(int i=0; i<n; ++i) {
        	double[] vCon = new double[n*m];
        	Arrays.fill(vCon, 0.0);
        	for(int j=0; j<m; ++j) {
        		vCon[i*n + j] = 1.0;
        	}
        	constraints.add(new LinearConstraint(vCon, Relationship.LEQ, 1.0));
        }
        for(int j=0; j<m; ++j) {
        	double[] vCon = new double[n*m];
        	Arrays.fill(vCon, 0.0);
        	for(int i=0; i<n; ++i) {
        		vCon[i*n + j] = 1.0;
        	}
        	constraints.add(new LinearConstraint(vCon, Relationship.LEQ, 1.0));
        }
        for(int i=0; i<n; ++i) {
        	for(int j=0; j<m; ++j) {
            	double[] vCon = new double[n*m];
            	Arrays.fill(vCon, 0.0);
       		    vCon[i*n + j] = 1.0;
        	    constraints.add(new LinearConstraint(vCon, Relationship.GEQ, 0));
        	    
        	    vCon = new double[n*m];
            	Arrays.fill(vCon, 0.0);
       		    vCon[i*n + j] = 1.0;
        	    constraints.add(new LinearConstraint(vCon, Relationship.LEQ, 1));
        	}
        }
        
        //create and run solver
        PointValuePair solution = null;
        solution = new SimplexSolver().optimize(f, new LinearConstraintSet(constraints), GoalType.MAXIMIZE);
        
        return solution.getValue();
	}

}
