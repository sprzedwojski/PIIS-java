package task5;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import mysql.MySQLAccess;

public class UserSimilarityFromTrainGenerator {
	static MySQLAccess dao;
	
	public static void main(String[] args) throws Exception {
	    dao = new MySQLAccess();
	    dao.connectToDataBase();
	    
	    ResultSet userIds = dao.getAllUsersFromTrain();
	    
	    ArrayList<String> ids = new ArrayList<String>();
	    while(userIds.next()) {
	    	ids.add(userIds.getString("PersonID"));
	    }
	    
	    int counter = 0;
	    ResultSet commonMovies;
	    boolean is71found = false;
	    for(String userId : ids) {
	    	if(userId.equals("455")) {
	    		is71found = true;
	    	}
	    	if(is71found) {
	 	    	for(String otherUserId : ids) {
		    		if(Integer.parseInt(otherUserId) > Integer.parseInt(userId)) {
		    			commonMovies = dao.getCommonMoviesForUsersFromTrain(userId, otherUserId);
		    			double similarity = getUserSimilarity(commonMovies, userId, otherUserId);
		    			System.out.println(++counter + ": " + userId + " | " + otherUserId + " | sim: " + similarity);
		    		}
		    	}
	    	}
	    }
	    
	    dao.closeDataBaseConnection();
	}
	
	private static double getUserSimilarity(ResultSet commonMovies, String userId1, String userId2) throws Exception {
		double sum = 0.0;
		String movieId; 
		int userRating1, userRating2;
		int counter=0;
		
		while(commonMovies.next()) {
			movieId = commonMovies.getString("MovieID");
			userRating1 = dao.getMovieEvaluationFromTrainForUser(movieId, userId1);
			userRating2 = dao.getMovieEvaluationFromTrainForUser(movieId, userId2);
			
			sum += 1 - (double)Math.abs(userRating1 - userRating2)/5;
			
			counter++;
		}
		
		double similarity = counter==0 ? 0 : sum/counter;
		
		dao.insertUserSimilarityFromTrain(userId1, userId2, similarity);
		
		return similarity;
	}
	
}
