package task4;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import mysql.MySQLAccess;

public class Task {

	HashMap<Integer, User> users = new HashMap<Integer, User>();
	HashMap<Integer,Integer> movies = null; //movieId, evaluation
	HashMap<Integer, User> users2 = new HashMap<Integer, User>();
	HashMap<Integer,Integer> movies2 = null; //movieId, evaluation
	
	MySQLAccess dao = null;
	
	public Task(MySQLAccess dao) {
		this.dao = dao;
	}
	
	public void doTask(Comparator comp, int[] featureIds) throws Exception {
		int k = FeatureSelection.k;
    	int userId = 0;

    	ResultSet usersFromTrainRS = dao.getAllUsersFromTrain();
    	ResultSet usersFromTaskRS = dao.getAllUsersFromTask();
    	
    	while(usersFromTrainRS.next()) {
	    	userId = usersFromTrainRS.getInt("PersonID");
	    	ResultSet moviesForUserRS = dao.getAllEvaluationsForUser(userId);
	    	movies = new HashMap<Integer,Integer>();

			while(moviesForUserRS.next()) {
				movies.put(moviesForUserRS.getInt("MovieID"), moviesForUserRS.getInt("Evaluation"));
			}
	    	
	    	User user = new User(movies);
	    	users.put(userId, user);
	    }
    	
    	while(usersFromTaskRS.next()) {
	    	userId = usersFromTaskRS.getInt("PersonID");
	    	ResultSet moviesForUserRS = dao.getAllMovieForUserFromTask(userId);
	    	movies2 = new HashMap<Integer,Integer>();

			while(moviesForUserRS.next()) {
				movies2.put(moviesForUserRS.getInt("MovieID"), 0);
			}
	    	
	    	User user = new User(movies2);
	    	users2.put(userId, user);
	    }
    	
    	
    	
    	// evaluate
    	for (Map.Entry<Integer, User> user : users2.entrySet()) {	    		
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
    			
    			int rating = 0; 
    			for(int num=0; num<k; num++) {
	    			if(mostSimilarMovieIds[num] != 0)
	    				rating += moviesMap.get(mostSimilarMovieIds[num]);
	    			else
	    				rating += 1; // in case e.g. no actors are matched, we have to assign a "random" rating
    			}
    			if(rating == 0) // just in case
    				rating = 1;
    			
    			movieToBeEvaluated.setValue(rating/k);
    			
//    			System.out.println("Person :" + user.getKey() + " | Movie: " + movieToBeEvaluated.getKey() +" | Rating: " + rating);
    					
    		}
    	}
	}
	
	public void generateCSV() throws Exception {
    	PrintWriter writer = new PrintWriter("kNN_FS_k=" + FeatureSelection.k + ".csv", "UTF-8");
    	
    	ResultSet task = dao.getTask();
    	while(task.next()) {
    		int id = task.getInt(1);
    		int personId = task.getInt(2);
    		int movieId = task.getInt(3);
    		
    		writer.println(id + ";" + personId + ";" + movieId + ";" + users2.get(personId).movies.get(movieId));
    	}
    	
    	writer.close();
	}
	
}
