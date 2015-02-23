package task4;

public class FeatureArrayGenerator {

	int[] featureIds = {0}; // the last spot is changed in each iteration
	
	public void generateNewArray(int bestLastFeatureId) {
		int[] featureIdsTemp = new int[featureIds.length+1];
		
		for(int i=0; i<featureIds.length-1; i++) {
			featureIdsTemp[i] = featureIds[i];
		}
		featureIdsTemp[featureIds.length-1] = bestLastFeatureId;
		featureIdsTemp[featureIds.length] = 0;
		featureIds = featureIdsTemp;
	}
	
}
