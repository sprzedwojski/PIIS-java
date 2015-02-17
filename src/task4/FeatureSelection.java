package task4;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import mysql.MySQLAccess;


public class FeatureSelection {

	// N features: greedy forward selection
    // kNN: k=1,3,5
    //
    // choose k
	public static int k = 1;
	
	public static int validationSize = 5; // 4 = every 4th row (1/3), 5 = every 5th row (1/6)
	
	public static void main(String[] args) throws Exception {
	    System.out.println(">> Initialising the data structure...");
		
	    MySQLAccess dao = new MySQLAccess();	    
	    dao.connectToDataBase();
	    
	    HashMap<Integer, User> users = new HashMap<Integer, User>(); 
	    HashMap<Integer, User> usersValidation = new HashMap<Integer, User>(); 
	    
	    // get users
	    ResultSet usersFromTrainRS = dao.getAllUsersFromTrain();
	    int userId;
	    int valSizeCounter=0;
	    
	    while(usersFromTrainRS.next()) {
	    	userId = usersFromTrainRS.getInt("PersonID");
	    	ResultSet moviesForUserRS = dao.getAllEvaluationsForUser(userId);
	    	HashMap<Integer,Integer> movies = new HashMap<Integer,Integer>();
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
	    
	    System.out.println(">> Init finished");
	    
	    comp.compareMovies(1, 1, null);
	    
//	    Iterator it = users.entrySet().iterator();
//	    while(it.hasNext()) {
//	    	Map.Entry pairs = (Map.Entry)it.next();
//	    	System.out.println(pairs.getKey() + "=" + pairs.getValue());
//	    }
//	    System.out.println(users.get(1642).movies.size());
//	    System.out.println(usersValidation.get(1642).movies.size());
	    

	    
	    // 1.	N subsets of 1 feature each {f1}, {f2}, ... , {fn}
	    // 		for each subset train the training set and check it against real data - obtain some similarity measure
	    //		select the subset with the highest similarity measure
	    //
	    
	    
	    
	    
	    
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
