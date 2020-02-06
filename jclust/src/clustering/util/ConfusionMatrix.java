package clustering.util;

import java.util.*;

import clustering.dataset.NominalVariable;

public class ConfusionMatrix {
	protected List<String> lstLabel1;
	protected List<String> lstLabel2;
	
	protected int length;
	
	protected int[] vMembership; // vMembership[i] is the cluster index of point i
	protected NominalVariable classInfo;
	
	protected int[] vMembershipGiven;
	protected NominalVariable classInfoGiven;
	
	protected int[][] mTable;
	protected double dRand;
	protected double dAdjustedRand;
	
	public ConfusionMatrix(List<String> lstLabel1, List<String> lstLabel2) throws Exception {
		this.lstLabel1 = lstLabel1;
		this.lstLabel2 = lstLabel2;
		
		length = lstLabel1.size();
		if(length != lstLabel2.size()) {
			throw new Exception("lstLabel1 and lstLabel2 are not of the same length");
		}
	
		calculate();
	}
	
	public int[][] getTable() {
		return mTable;
	}
			
	protected void calculate() {
		vMembership = new int[length];
		classInfo = new NominalVariable("ClusterFound");
		vMembershipGiven = new int[length];
		classInfoGiven = new NominalVariable("ClusterGiven");
		for(int i=0; i<length; ++i) {		
			vMembership[i] = classInfo.getValue(lstLabel1.get(i));		
			vMembershipGiven[i] = classInfoGiven.getValue(lstLabel2.get(i));
		}
		
		mTable = new int[classInfo.numValues()][classInfoGiven.numValues()];
	    for(int i=0; i<length;++i) {
	        mTable[vMembership[i]][vMembershipGiven[i]] += 1;
	    }
	}
	
	public String toString() {
		String separator = System.getProperty( "line.separator" );
		StringBuilder sb = new StringBuilder();
        for(int j=0;j<classInfoGiven.numValues();++j) {
            sb.append(",").append(classInfoGiven.getValueInStr(j));                
        }
        sb.append(separator);
        for(int i=0;i<mTable.length;++i){
            sb.append(classInfo.getValueInStr(i));
            for(int j=0;j<mTable[0].length;++j) {
            	sb.append(", ").append(mTable[i][j]);                    
            }
            sb.append(separator);
        }
		
		return sb.toString();
	}
}
