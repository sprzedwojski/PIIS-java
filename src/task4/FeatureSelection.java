package task4;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import mysql.MySQLAccess;


public class FeatureSelection {

	// N features: greedy forward selection
    // kNN: k=1,3,5
    //
    // choose k
	public static int k = 5;
	
	public static int validationSize = 5; // 4 = every 4th row (1/3), 5 = every 5th row (1/6)
	public static int FEATURES_TOTAL = 11;		
	
	public static boolean SKIP_TRAINING = false;
	public static int[] hardcodedFeatureIds = {5, 6, 11, 3, 4};
	
	public static void main(String[] args) throws Exception {
	    System.out.println(">> Initialising the data structure...");
		
	    MySQLAccess dao = new MySQLAccess();	    
	    dao.connectToDataBase();	
	    
	    int[] overallBestFeatureIdsArray = null;
	    int overallMinDiffSum = 10000000;
	    
	    Comparator comp = new Comparator();
	    
	    comp.features = new HashMap<Integer, HashMap<Integer, String>>();
	    int movieCounter = 1; // 1 - 200
	    for(movieCounter=1; movieCounter<=200; movieCounter++) {
	    	ResultSet featuresRS = dao.getAllFeaturesForMovie(movieCounter);
	    	comp.featureValues = new HashMap<Integer, String>();
	    	while(featuresRS.next()) {
	    		comp.featureValues.put(featuresRS.getInt("FeatureID"), featuresRS.getString("Value"));
	    	}
	    	comp.features.put(movieCounter, comp.featureValues);
	    }
	    
	    if(SKIP_TRAINING) {
	    	System.out.println(" !! Skipping training! | Using hardcoded feature ids: ");
	    	for(int k=0; k<hardcodedFeatureIds.length; k++) {
	    		System.out.print(hardcodedFeatureIds[k] + " ");
	    	}
	    } else {
		    FeatureArrayGenerator fag = new FeatureArrayGenerator();
		    
		    HashMap<Integer, User> users = new HashMap<Integer, User>(); 
		    HashMap<Integer, User> usersValidation = new HashMap<Integer, User>(); 	   	    
		    
		    // get users
		    ResultSet usersFromTrainRS = dao.getAllUsersFromTrain();
		    int userId;
		    int valSizeCounter=0;
		    
		    HashMap<Integer,Integer> movies = null; //movieId, evaluation
		    
		    while(usersFromTrainRS.next()) {
		    	userId = usersFromTrainRS.getInt("PersonID");
		    	ResultSet moviesForUserRS = dao.getAllEvaluationsForUser(userId);
		    	movies = new HashMap<Integer,Integer>();
		    	User userValidation = new User();
		    	
			    // 
			    // split the task into training and validation data (e.g. 70-30)
			    //
				while(moviesForUserRS.next()) {
					if(valSizeCounter++%validationSize==0) //-->validation
						userValidation.movies.put(moviesForUserRS.getInt("MovieID"), moviesForUserRS.getInt("Evaluation"));
					else
						movies.put(moviesForUserRS.getInt("MovieID"), moviesForUserRS.getInt("Evaluation"));
				}
		    	
		    	User user = new User(movies);
		    	users.put(userId, user);
		    	
		    	usersValidation.put(userId, userValidation);
		    }
		    
	
		    
		    System.out.println(">> Init finished");
		    
	
		    System.out.println(">> Beginning feature selection...");
		    
		    // 1.	N subsets of 1 feature each {f1}, {f2}, ... , {fn}
		    // 		for each subset train the training set and check it against real data - obtain some similarity measure
		    //		select the subset with the highest similarity measure
		    //
		    int bestLastFeatureId = 0;
		    

		    
		    for(int row=0; row<FEATURES_TOTAL; row++) {	    		    
		    	int minDiffSum = 10000000;
		    	
		    	if(row>0) fag.generateNewArray(bestLastFeatureId);
		    	bestLastFeatureId = 0;
		    	
		    	int[] featureIds = fag.featureIds;
		    	
		    	System.out.println(">> row=" + row + " | " + " featureIds:");
		    	for(int k=0; k<featureIds.length; k++) {
		    		System.out.print(featureIds[k] + " ");
		    	}
		    	System.out.print("\n");
		    	
			    for(int i=0; i<FEATURES_TOTAL; i++) {	
			    	
			    	
			    	// skip features that are already in the featureIds
			    	boolean skipFeature = false;
			    	for(int j=0; j<featureIds.length-1; j++) {
			    		if(featureIds[j] == i+1) {
			    			skipFeature = true;
			    			break;
			    		}
			    	}
			    	if(skipFeature) {
			    		System.out.println(" !! Skipping feature: " + (i+1));
			    		continue;
			    	}
			    	
			    	
			    	HashMap<Integer, User> usersValidationCopy = new HashMap<Integer, User>();
			    	for (Map.Entry<Integer, User> user : usersValidation.entrySet()) {
			    		User userCopy = new User();
			    		for (Map.Entry<Integer, Integer> movie : user.getValue().movies.entrySet()) {
			    			userCopy.movies.put(movie.getKey(), 0);
			    		}
			    		usersValidationCopy.put(user.getKey(), userCopy);
			    	}
			    			    	
			    	// switching the last featureId
			    	featureIds[featureIds.length-1] = i+1;
			    	
			    	for (Map.Entry<Integer, User> user : usersValidationCopy.entrySet()) {	    		
			    		for (Map.Entry<Integer, Integer> movieToBeEvaluated : user.getValue().movies.entrySet()) {	    			
			    			
			    			Map<Integer, Double> similarity = new HashMap<Integer, Double>(); // movieId, similarity	    			
			    			Map<Integer, Integer> moviesMap = new HashMap<Integer, Integer>(); // movieId, evaluation	    			
			    			moviesMap = users.get(user.getKey()).movies;
			    			
			    			for(Map.Entry<Integer, Integer> movie : moviesMap.entrySet()) {
			    				if(movie.getKey() != movieToBeEvaluated.getKey()) {
			    					//int[] featureIds = {i+1};
			    					similarity.put(movie.getKey(), comp.compareMovies(movie.getKey(), movieToBeEvaluated.getKey(), featureIds));
			    				}
			    			}
			    			
			    			// searching for k most similar movies (currently k=1 only) TODO for any k		    			
			    			int[] mostSimilarMovieIds = new int[k];
			    			
			    			for(int num=0; num<k; num++) {
			    				int mostSimilarMovieId = 0;
				    			double highestSimilarity = 0.0;
				    			for(Map.Entry<Integer, Double> sim : similarity.entrySet()) {
				    				if(sim.getValue() > highestSimilarity) {
				    					highestSimilarity = sim.getValue();
				    					mostSimilarMovieId = sim.getKey();
				    				}
				    			}
				    			mostSimilarMovieIds[num] = mostSimilarMovieId;
				    			
				    			similarity.remove(mostSimilarMovieId);
			    			}
			    			/*System.out.println("\n");
			    			System.out.println(" -- movie to be evaluated id: " + movieToBeEvaluated.getKey());
			    			System.out.println(" -- most similar movie id: " + mostSimilarMovieId);
			    			*/
			    			
			    			int rating = 0; 
			    			for(int num=0; num<k; num++) {
				    			if(mostSimilarMovieIds[num] != 0)
				    				rating += moviesMap.get(mostSimilarMovieIds[num]);
				    			else
				    				rating += 1; // in case e.g. no actors are matched, we have to assign a "random" rating
			    			}
			    			movieToBeEvaluated.setValue(rating/k);
			    			
			    			/*System.out.println("Movie: " + movieToBeEvaluated.getKey() + " | Most similar: " + mostSimilarMovieId + ", sim: " +
			    					highestSimilarity + " | Rating: " + rating);
			    					*/
			    		}
			    	}
			    	
			    	// evaluate feature selection
			    	int diffSum = comp.evaluateFeatureSelection(usersValidation, usersValidationCopy);
			    	System.out.println(" -- feature: " + (i+1) + " | diffSum: " + diffSum);
			    	if(diffSum < minDiffSum) {
			    		minDiffSum = diffSum;
			    		bestLastFeatureId = featureIds[featureIds.length-1];		    		
			    	}
			    	
			    }
		    
	    		System.out.println("minDiffSum: " + minDiffSum);
	    		System.out.println("bestLastFeatureId: " + bestLastFeatureId);
	    		System.out.println();
	    		
	    		if(minDiffSum < overallMinDiffSum) {
	    			overallMinDiffSum = minDiffSum;
	    			overallBestFeatureIdsArray = featureIds.clone();
	    			overallBestFeatureIdsArray[overallBestFeatureIdsArray.length-1] = bestLastFeatureId;
	    			
	    			System.out.println(" $$ NEW BEST FEATURE SUBSET:");
	    	    	for(int k=0; k<overallBestFeatureIdsArray.length; k++) {
	    	    		System.out.print(overallBestFeatureIdsArray[k] + " ");
	    	    	}
	    	    	System.out.println("\n");
	    		}
		    }
		    
		    
			System.out.println(" ---------\n $$ OVERALL BEST FEATURE SUBSET:");
	    	for(int k=0; k<overallBestFeatureIdsArray.length; k++) {
	    		System.out.print(overallBestFeatureIdsArray[k] + " ");
	    	}
	    	System.out.println(" ---------\n");
	    		    	
	    	System.out.println(">> Training finished. Beginning task for k=" + k);
	    }
    	
    	// TODO task
	    Task task = new Task(dao);
	    if(!SKIP_TRAINING) {    	
	    	task.doTask(comp, overallBestFeatureIdsArray);
	    } else {
	    	task.doTask(comp, hardcodedFeatureIds);
	    }
    	
    	
    	System.out.println(">> Task finished. Exporting to CSV...");
    	
    	task.generateCSV();
	    
	    System.out.println(">>");
	    
	    
	    
	    // 2.	Create N-1 subsets of 2 features each, with the feature selected in 1. in each of them:
	    //		(e.g. f5 was the best in 1.): {f1, f5}, {f2, f5}, ... , {fn, f5}
	    //
	    //		...
	    //
	    //		repeat until the subset with N elements
	    //		from ALL generated subsets select the one with highest similarity measure
	    //
	    //		use this BEST subset to calculate similarity
	    //
	    //
	    //		repeat all for all k-s (1,3,5)
	    
	}
	
}

class User {
	HashMap<Integer, Integer> movies; //MovieID, Evaluation
	
	User() {
		movies = new HashMap<Integer, Integer>(); 
	}
	
	User(HashMap<Integer, Integer> m) {
		movies = m;
	}
	
	User(ResultSet moviesRS) throws SQLException {
		movies = new HashMap<Integer, Integer>();
		while(moviesRS.next()) {
			movies.put(moviesRS.getInt("MovieID"), moviesRS.getInt("Evaluation"));
		}
	}
}
