package task4;
import java.util.HashMap;

import mysql.MySQLAccess;


public class CorrectMovieIDs {

	public static int k = 1;
	
	public static void main(String[] args) throws Exception {
	    MySQLAccess dao = new MySQLAccess();
	    dao.connectToDataBase();
	    
	    dao.insertMissingMovieIDs();	    
	}
	
}
