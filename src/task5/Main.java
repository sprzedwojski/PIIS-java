package task5;
import java.sql.ResultSet;
import java.util.ArrayList;

import mysql.MySQLAccess;


public class Main {

	public static int k = 5;
	
	public static void main(String[] args) throws Exception {
	    MySQLAccess dao = new MySQLAccess();
	    ResultSet train, task, usersWhoRatedMovie;
	    String personId, movieId;
	    ArrayList<String> similarUsers = new ArrayList<String>();
	    
	    dao.connectToDataBase();
	    
	    // get task and train
	    train = dao.getTrain();
	    task = dao.getTask();
	    
	    // for each person from task
    	// 		for each movie 	-> select all other persons that rated that movie (from train)
	    //	    				-> choose k most similar users
	    //						-> calculate their average grade and save it to DB as the main user's rating
	    
	    while(task.next()) {
	    	personId = task.getString("PersonID");
	    	movieId = task.getString("MovieID");
	    	
	    	// get all users from train that rated the movie (id = movieId)
	    	usersWhoRatedMovie = dao.getMostSimilarUsersWhoRatedMovie(personId, movieId, k);
	    	
	    	while(usersWhoRatedMovie.next()) {
	    		if(!usersWhoRatedMovie.getString("PersonID1").equals(personId)) {
	    			similarUsers.add(usersWhoRatedMovie.getString("PersonID1"));
	    		} else {
	    			similarUsers.add(usersWhoRatedMovie.getString("PersonID2"));
	    		}
	    	}
	    	
	    	int evalSum = 0;
	    	// for each similar user get its evaluation of the movie in question (not optimised but I don't care...)
	    	for(String similarUserId : similarUsers) {
	    		evalSum += dao.getMovieEvaluationFromTrainForUser(movieId, similarUserId);
	    	}
	    	
	    	double evalAvg = (double)evalSum/similarUsers.size();
	    	
	    	dao.updateUserRating(personId, movieId, evalAvg);
	    	
	    	similarUsers.clear();
	    }
	    
	    
	    dao.closeDataBaseConnection();
	}
	
}
