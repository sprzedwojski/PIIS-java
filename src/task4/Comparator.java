package task4;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

public class Comparator {
	public HashMap<Integer, String> featureValues; // featureId, value
	public HashMap<Integer, HashMap<Integer, String>> features; // movieId, 'featureValues'
	
	public int evaluateFeatureSelection(HashMap<Integer, User> usersValidation, HashMap<Integer, User> usersValidationCopy) {
		
		int diffSum = 0;
		
		for (Map.Entry<Integer, User> user : usersValidationCopy.entrySet()) {
			for (Map.Entry<Integer, Integer> movie : user.getValue().movies.entrySet()) {
				int realMovieEval = usersValidation.get(user.getKey()).movies.get(movie.getKey());
				
				diffSum += Math.abs(realMovieEval - movie.getValue()); 
			}
		}
		
		return diffSum;
	}
	
	public double compareMovies(int movieId1, int movieId2, int[] featureIds) {
		
		double similaritySum = 0.0;
		
		for(int i=0; i<featureIds.length; i++) {
			String feature1 = features.get((Integer)movieId1).get((Integer)featureIds[i]);
			String feature2 = features.get((Integer)movieId2).get((Integer)featureIds[i]);
			
			switch(featureIds[i]) {
			case 1: //genres
				similaritySum += 0.25 * compareGenres(feature1, feature2);
				break;
			case 2: //actors
				similaritySum += 0.17 * compareActors(feature1, feature2);
				break;
			case 3: //director
				similaritySum += 0.17 * compareList(feature1, feature2);
				break;
			case 4: //author
				similaritySum += 0.05 * compareList(feature1, feature2);
				break;
			case 5: //music
				similaritySum += 0.03 * compareList(feature1, feature2);
				break;
			case 6: //cameramen
				similaritySum += 0.03 * compareList(feature1, feature2);
				break;
			case 7: //budget
				// do nothing
				break;
			case 8: //revenue
				// do nothing
				break;
			case 9: //year
				similaritySum += 0.05 * compareYear(Integer.parseInt(feature1), 
						Integer.parseInt(feature2));
				break;
			case 10: //keywords
				similaritySum += 0.2 * compareKeywords(feature1, feature2);
				break;
			case 11: //countries
				similaritySum += 0.05 * compareList(feature1, feature2);				
				break;
			}
		}
		
		return similaritySum;
	}
	
	private double compareGenres(String genres1, String genres2) {
		
		JSONArray arr1 = new JSONArray(genres1);
		JSONArray arr2 = new JSONArray(genres2);
		
		double sum = 0;
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
	
	private double compareKeywords(String keywords1, String keywords2) {
		
		JSONArray list1 = new JSONArray(keywords1);
		JSONArray list2 = new JSONArray(keywords2);
		
		int matching = 0;
		
		for(int i=0; i<list1.length(); i++) {
			boolean found = false;
			
			for(int j=0; j<list2.length(); j++) {
				if(list1.get(i).equals(list2.get(j))) {
					found = true;
					break;
				}
			}
			
			if(found) matching++;
		}
		
		switch(matching){
		case 0:
			return 0;			
		case 1:
			return 0.5;
		case 2:
			return 0.7;
		case 3:
			return 0.8;
		case 4:
			return 0.9;
		default:
			return 1;
			
		}
	}
	
	private double compareActors(String actors1, String actors2) {
		
		JSONArray list1 = new JSONArray(actors1);
		JSONArray list2 = new JSONArray(actors2);
		
		int matching = 0, multiplier = 1;
		
		for(int i=0; i<list1.length(); i++) {
			boolean found = false;
			
			for(int j=0; j<list2.length(); j++) {
				if(list1.get(i).equals(list2.get(j))) {
					found = true;
					break;
				}
			}
			
			if(found) matching += multiplier;
			multiplier /= 2;
		}
		
		multiplier = 1;
		
		for(int i=0; i<list2.length(); i++) {
			boolean found = false;
			
			for(int j=0; j<list1.length(); j++) {
				if(list2.get(i).equals(list1.get(j))) {
					found = true;
					break;
				}
			}
			
			if(found) matching += multiplier;
			multiplier /= 2;
		}
		
		return matching/5;
		
	}

	private double compareNumeric(double value1, double value2) {		
		
		if(value1 == 0 || value2 == 0) 
			return 0;
		
		if(value1 > value2) 
			return 1 - ((value1 - value2)/value1);
		else
			return 1 - ((value2 - value1)/value2);
	}
	
	private int compareYear(int year1, int year2) {
		return (1-Math.abs(year1-year2)/64);
	}
	
	private double compareList(String string1, String string2) {
		JSONArray list1 = new JSONArray(string1);
		JSONArray list2 = new JSONArray(string2);

		int max = Math.max(list1.length(), list2.length());
		if(max==0)
			return 0;		
		
		double sum = 0;		
		
		for(int i=0; i<list1.length(); i++) {
			boolean found = false;
			
			for(int j=0; j<list2.length(); j++) {
				if(list1.get(i).equals(list2.get(j))) {
					found = true;
					break;
				}
			}
			
			if(found) sum++;			
		}
		
		return (double)sum / max;
		
	}
	
}
