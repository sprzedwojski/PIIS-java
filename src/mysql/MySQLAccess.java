package mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLAccess {
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	public void connectToDataBase() throws Exception {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/movies?"
							+ "user=root");
		} catch (Exception e) {
			close();
			throw e;
		}
	}
	
	public void insertMissingMovieIDs() throws Exception {
		try {
			ResultSet data = connect.createStatement().executeQuery("select * from Data");
			
			while(data.next()) {
				String tmdbId = data.getString("TMDBID");
				
				ResultSet movie = connect.createStatement().executeQuery("select ID from Movies where TMDBID=" + tmdbId);
				movie.next();
				String movieId = movie.getString("ID");							
				
				connect.createStatement().executeUpdate("update Data set MovieID=" + movieId + " where TMDBID=" + tmdbId);
			}
			
		} catch(Exception e) {
			close();
			throw e;
		}
	}
	
	public ResultSet getTrain() throws Exception {
		try {
			return connect.createStatement().executeQuery("select * from Train");
		} catch(Exception e) {
			close();
			throw e;
		}
	}
	
	public ResultSet getTask() throws Exception {
		try {
			return connect.createStatement().executeQuery("select * from Task");
		} catch(Exception e) {
			close();
			throw e;
		}
	}
	
	public ResultSet getAllUsers() throws Exception {
		try {
			return connect.createStatement().executeQuery("select ID from Persons");
		} catch(Exception e) {
			close();
			throw e;
		}
	}
	
	public ResultSet getAllUsersFromTrain() throws Exception {
		try {
			return connect.createStatement().executeQuery("select distinct PersonID from Train");
		} catch(Exception e) {
			close();
			throw e;
		}
	}

	public ResultSet getMostSimilarUsersWhoRatedMovie(String personId, String movieId, int k) throws Exception {
		try {
			return connect.createStatement()
					.executeQuery("SELECT * FROM `UserSimilarityFromTrain` WHERE (PersonID1=" + personId 
							+ " AND PersonID2 IN (SELECT PersonID FROM `Train` WHERE MovieID=" + movieId 
							+ " AND Evaluation IS NOT NULL)) OR (PersonID2=" + personId 
							+ " AND PersonID1 IN (SELECT PersonID FROM `Train` WHERE MovieID=" + movieId 
							+ " AND Evaluation IS NOT NULL)) ORDER BY Similarity DESC LIMIT " + k);
		} catch(Exception e) {
			close();
			throw e;
		}
	}
	
	public ResultSet getCommonMoviesForUsers(String userId1, String userId2) throws Exception {
		try {
			return connect.createStatement()
					.executeQuery("SELECT TMDBID FROM `Evaluations` WHERE Evaluation IS NOT NULL AND PersonID="
							+ userId1 + " AND TMDBID IN "
							+ "(SELECT TMDBID FROM `Evaluations` WHERE Evaluation IS NOT NULL AND PersonID=" 
							+ userId2 + ")");
		} catch(Exception e) {
			close();
			throw e;
		}
	}
	
	public ResultSet getCommonMoviesForUsersFromTrain(String userId1, String userId2) throws Exception {
		try {
			preparedStatement = connect.prepareStatement("SELECT MovieID FROM Train WHERE Evaluation IS NOT NULL AND PersonID=? AND MovieID IN (SELECT MovieID FROM Train WHERE Evaluation IS NOT NULL AND PersonID=?)");
			preparedStatement.setString(1, userId1);
			preparedStatement.setString(2, userId2);
			return preparedStatement.executeQuery();
		} catch(Exception e) {
			close();
			throw e;
		}
	}
	
	public int getMovieEvaluationFromTrainForUser(String movieId, String userId) throws Exception {
		try {
			ResultSet eval = connect.createStatement()
					.executeQuery("SELECT Evaluation FROM Train WHERE MovieID="+ movieId +" AND PersonID="+ userId);
			eval.next();
			return eval.getInt("Evaluation");
		} catch(Exception e) {
			close();
			throw e;
		}
	}
	
	public int getMovieEvaluationForUser(String TMDBID, String userId) throws Exception {
		try {
			ResultSet eval = connect.createStatement()
					.executeQuery("select Evaluation from Evaluations WHERE PersonID=" + userId 
							+ " AND TMDBID=" + TMDBID);
			eval.next();
			return eval.getInt("Evaluation");
		} catch(Exception e) {
			close();
			throw e;
		}
	}
	
	public ResultSet getAllEvaluationsForUser(int userId) throws Exception {
		try {
			return connect.createStatement().executeQuery("SELECT MovieID, Evaluation FROM `Train` WHERE PersonID=" + userId);
		} catch(Exception e) {
			close();
			throw e;
		}
	}
	
	public ResultSet getAllFeaturesForMovie(int movieId) throws Exception {
		try {
			return connect.createStatement().executeQuery("SELECT MovieID, FeatureID, Value FROM `Data` WHERE `MovieID`=" + movieId);
		} catch(Exception e) {
			close();
			throw e;
		}
	}
	
//	public ResultSet getAllMovies(int movieId) throws Exception {
//		try {
//			return connect.createStatement().executeQuery("SELECT MovieID, FeatureID, Value FROM `Data` WHERE `MovieID`=" + movieId);
//		} catch(Exception e) {
//			close();
//			throw e;
//		}
//	}
	
	public void insertUserSimilarity(String userId1, String userId2, double similarity) throws Exception {
		try {
			 preparedStatement = connect.prepareStatement(
					 "insert into UserSimilarity values (default, ?, ?, ?)"); 
			 preparedStatement.setString(1, userId1);
			 preparedStatement.setString(2, userId2);
			 preparedStatement.setDouble(3, similarity);
			 preparedStatement.executeUpdate();
		} catch(Exception e) {
			close();
			throw e;
		}
	}
	
	public void insertUserSimilarityFromTrain(String userId1, String userId2, double similarity) throws Exception {
		try {
			 preparedStatement = connect.prepareStatement(
					 "insert into UserSimilarityFromTrain values (default, ?, ?, ?)"); 
			 preparedStatement.setString(1, userId1);
			 preparedStatement.setString(2, userId2);
			 preparedStatement.setDouble(3, similarity);
			 preparedStatement.executeUpdate();
		} catch(Exception e) {
			close();
			throw e;
		}
	}
	
	public void updateUserRating(String userId, String movieId, double rating) throws Exception {
		try {
			 preparedStatement = connect.prepareStatement(
					 "UPDATE `Task` SET `Evaluation`=? WHERE MovieID=? AND PersonID=?"); 
			 preparedStatement.setDouble(1, rating);
			 preparedStatement.setString(2, movieId);
			 preparedStatement.setString(3, userId);
			 
			 System.out.println(preparedStatement.toString());
			 
			 preparedStatement.executeUpdate();
		} catch(Exception e) {
			close();
			throw e;
		}
	}
	
	// DALEJ DO USUNIĘCIA (?)
	
	public void readDataBase() throws Exception {
		try {

			// statements allow to issue SQL queries to the database
			statement = connect.createStatement();
			// resultSet gets the result of the SQL query
			resultSet = statement.executeQuery("select * from Movies");
			writeResultSet(resultSet);

			// preparedStatements can use variables and are more efficient
			/*
			 * preparedStatement = connect .prepareStatement(
			 * "insert into  FEEDBACK.COMMENTS values (default, ?, ?, ?, ? , ?, ?)"
			 * ); preparedStatement.setString(1, "Test");
			 * preparedStatement.setString(2, "TestEmail");
			 * preparedStatement.setString(3, "TestWebpage");
			 * preparedStatement.setDate(4, new java.sql.Date(2009, 12, 11));
			 * preparedStatement.setString(5, "TestSummary");
			 * preparedStatement.setString(6, "TestComment");
			 * preparedStatement.executeUpdate();
			 */

			/*
			 * preparedStatement = connect .prepareStatement(
			 * "SELECT myuser, webpage, datum, summary, COMMENTS from FEEDBACK.COMMENTS"
			 * ); resultSet = preparedStatement.executeQuery();
			 * writeResultSet(resultSet);
			 * 
			 * preparedStatement = connect
			 * .prepareStatement("delete from FEEDBACK.COMMENTS where myuser= ? ; "
			 * ); preparedStatement.setString(1, "Test");
			 * preparedStatement.executeUpdate();
			 * 
			 * resultSet = statement
			 * .executeQuery("select * from FEEDBACK.COMMENTS");
			 * writeMetaData(resultSet);
			 */

		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}

	}

	public void writeMetaData(ResultSet resultSet) throws SQLException {
		// now get some metadata from the database
		System.out.println("The columns in the table are: ");
		System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
		for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
			System.out.println("Column " + i + " "
					+ resultSet.getMetaData().getColumnName(i));
		}
	}

	public void writeResultSet(ResultSet resultSet) throws SQLException {
		// resultSet is initialized before the first data set
		while (resultSet.next()) {
			// it is possible to get the columns via name
			// also possible to get the columns via the column number
			// which starts at 1
			// e.g., resultSet.getSTring(2);
			String tmdbid = resultSet.getString("TMDBID");
			String title = resultSet.getString("Title");
			/*
			 * String summary = resultSet.getString("summary"); Date date =
			 * resultSet.getDate("datum"); String comment =
			 * resultSet.getString("comments");
			 */
			System.out.println("tmdbid: " + tmdbid);
			System.out.println("title: " + title);
			/*
			 * System.out.println("Summary: " + summary);
			 * System.out.println("Date: " + date);
			 * System.out.println("Comment: " + comment);
			 */
		}
	}

	public void closeDataBaseConnection() {
		close();
	}
	
	// you need to close all three to make sure
	private void close() {
		close(resultSet);
		close(statement);
		close(connect);
	}

	private void close(AutoCloseable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			// don't throw now as it might leave following closables in
			// undefined state
		}
	}
}