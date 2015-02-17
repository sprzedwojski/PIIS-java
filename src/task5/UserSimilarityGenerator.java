package task5;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import mysql.MySQLAccess;

public class UserSimilarityGenerator {
	static MySQLAccess dao;
	
	public static void main(String[] args) throws Exception {
	    dao = new MySQLAccess();
	    dao.connectToDataBase();
	    
	    ResultSet userIds = dao.getAllUsers();
	    
	    ArrayList<String> ids = new ArrayList<String>();
	    while(userIds.next()) {
	    	ids.add(userIds.getString("ID"));
	    }
	    
	    int counter = 0;
	    ResultSet commonMovies;
	    for(String userId : ids) {
	    	for(String otherUserId : ids) {
	    		if(Integer.parseInt(userId) >= 120 && Integer.parseInt(otherUserId) > Integer.parseInt(userId)) {
	    			commonMovies = dao.getCommonMoviesForUsers(userId, otherUserId);
	    			double similarity = getUserSimilarity(commonMovies, userId, otherUserId);
	    			System.out.println(++counter + ": " + userId + " | " + otherUserId + " | sim: " + similarity);
	    		}
	    	}
	    }
	    
	    dao.closeDataBaseConnection();
	}
	
	private static double getUserSimilarity(ResultSet commonMovies, String userId1, String userId2) throws Exception {
		double sum = 0.0;
		String TMDBID; 
		int userRating1, userRating2;
		int counter=0;
		
		while(commonMovies.next()) {
			TMDBID = commonMovies.getString("TMDBID");
			userRating1 = dao.getMovieEvaluationForUser(TMDBID, userId1);
			userRating2 = dao.getMovieEvaluationForUser(TMDBID, userId2);
			
			sum += 1 - (double)Math.abs(userRating1 - userRating2)/5;
			
			counter++;
		}
		
		double similarity = counter==0 ? 0 : sum/counter;
		
		dao.insertUserSimilarity(userId1, userId2, similarity);
		
		return similarity;
	}
	
}
