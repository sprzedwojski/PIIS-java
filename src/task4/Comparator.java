package task4;

import java.util.HashMap;

import org.json.JSONArray;

public class Comparator {
	public HashMap<Integer, String> featureValues; // featureId, value
	public HashMap<Integer, HashMap<Integer, String>> features; // movieId, 'featureValues'
	
	public double compareMovies(int id1, int id2, int[] featureIds) {
		
		compareGenres("[\"Comedy\",\"Drama\",\"Romance\"]", "[\"Drama\"]");
		
		return 0.0;
	}
	
	private double compareGenres(String genres1, String genres2) {
		
		JSONArray arr1 = new JSONArray(genres1);
		JSONArray arr2 = new JSONArray(genres2);
		
		int sum = 0;
		for(int i=0; i<arr1.length(); i++) {
			for(int j=0; j<arr2.length(); j++) {
				if(arr1.getString(i).equals(arr2.getString(j))) {
					sum++;
					break;
				}
			}
		}
		
		return sum / Math.min(arr1.length(), arr2.length());
	}
	
}
