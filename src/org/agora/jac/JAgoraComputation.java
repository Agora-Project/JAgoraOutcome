package org.agora.jac;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import org.agora.jac.logging.*;
import org.agora.jac.saa.SAAArgument;
import org.agora.jac.saa.SAAGraph;
import org.json.simple.*;
import org.json.simple.parser.*;



public class JAgoraComputation {
	
	protected static final String DEFAULT_DATABASE_FILE = "databases.conf";
  protected Connection c;
	protected SAAGraph graph;
	
	protected List<DatabaseConnection> dbConnections;
	
	public JAgoraComputation() {
	  dbConnections = new LinkedList<DatabaseConnection>();
	}
	
	public boolean initiateConnection(String url, String user, String password) {
	  try {
      Class.forName("com.mysql.jdbc.Driver");
      c = DriverManager.getConnection(url, user, password);
      return true;
    } catch (ClassNotFoundException e) {
      Log.error("JAgoraComputation: unable to load com.mysql.jdbc.Driver.");
      Log.error(e.getMessage());
    } catch (SQLException e) {
      Log.error("JAgoraComputation: problem connecting to '" + url + "'");
      Log.error(e.getMessage());
    }
	  return false;
	}
	
	public boolean terminateConnection() {
	  try {
      c.close();
      return true;
    } catch (SQLException e) {
      Log.error("JAgoraComputation: problems disconnecting database.");
      Log.error(e.getMessage());
    }
	  return false;
	}
	
	public boolean loadDataFromDB() {
	  graph = new SAAGraph();
	  Statement s;
	  ResultSet rs;
	  try {
	    s = c.createStatement();
	    rs = s.executeQuery("SELECT source_ID, arg_ID FROM arguments;");
	    graph.loadArgumentsFromResultSet(rs);
	    rs = s.executeQuery("SELECT source_ID_attacker, arg_ID_attacker,"
	                            + " source_ID_defender, arg_ID_defender"
	                            + " FROM attacks;");
	    graph.loadAttacksFromResultSet(rs);
	    rs = s.executeQuery("SELECT source_ID, arg_ID, "
                              + " SUM(CASE WHEN type = 1 THEN 1 ELSE 0 END) AS positive_votes, "
                              + " SUM(CASE WHEN type = 0 THEN 1 ELSE 0 END) AS negative_votes "
                              + " FROM votes WHERE arg_ID IS NOT NULL "
                              + " GROUP BY source_ID, arg_ID");
	    graph.loadArgumentVotesFromResultSet(rs);
	    rs = s.executeQuery("SELECT source_ID_attacker, arg_ID_attacker, "
                              + " source_ID_defender, arg_ID_defender, "
                              + " SUM(CASE WHEN type = 1 THEN 1 ELSE 0 END) AS positive_votes, "
                              + " SUM(CASE WHEN type = 0 THEN 1 ELSE 0 END) AS negative_votes "
                              + " FROM votes WHERE arg_ID IS NULL "
                              + " GROUP BY source_ID_attacker, arg_ID_attacker, "
                              +          " source_ID_defender, arg_ID_defender");
	    graph.loadAttackVotesFromResultSet(rs);
	    s.close();
	    return true;
	  } catch (SQLException e) {
	    Log.error("JAgoraComputation: problem retrieving graph.");
	    Log.error(e.getMessage());
	  }
    return false;
	}
	
	
	public boolean updateDatabase() {
	  try {
	    c.setAutoCommit(false);
	    
	    PreparedStatement s = c.prepareStatement("UPDATE arguments SET acceptability=? WHERE arg_ID=? AND source_ID=?;");
	    
	    for (SAAArgument a : graph.getArguments()) {
	      s.setDouble(1, a.getValuation());
	      s.setInt(2, a.getID());
	      s.setString(3, a.getSource());
	      s.addBatch();
	    }
	     
	    s.executeBatch();
	    
	    c.commit();
	    c.setAutoCommit(true);
	    c.close();
	    return true;
	  } catch (SQLException e){
	    Log.error("[ERROR] JAgoraComputation: problem storing outcomes.");
      Log.error(e.getMessage());
      e.printStackTrace();
	  }
	  return false;
	}
	
	public void computeOutcomes() {
	  graph.computeOutcomes();
	}
	
	public void printGraph() {
	  graph.printGraph();
	}
	
	public void run() {
	  JAgoraComputation jac = new JAgoraComputation();
    if (!jac.initiateConnection("jdbc:mysql://192.168.8.200:3306/agora-db", "agora-dev", "pythagoras"))
      return;
    if (!jac.loadDataFromDB()) return;
    jac.printGraph();
    Log.log("Starting computation... ", false);
    jac.computeOutcomes();
    Log.log("done!");
    Log.log("Updating database... ");
    if(jac.updateDatabase())
      Log.log("done!");
    else
      Log.log("failed!");
    System.out.println();
    if (!jac.terminateConnection()) return;
    jac.printGraph();
	}
	
	public boolean loadDatabaseFile(String path) {
	  try {
	    JSONParser parser = new JSONParser();
	    JSONArray dbs = (JSONArray) parser.parse(new FileReader(new File(path)));
	    for (Object databaseInfo : dbs) {
	      DatabaseConnection dbc = new DatabaseConnection();
	      dbc.loadFromJSON((JSONObject)databaseInfo);
	      dbConnections.add(dbc);
	    }
	    Log.log("[LOG] JAgoraComputation: Database file '"+path+"' loaded successfully.");
	    return true;
	  } catch (IOException e) {
	    Log.error("[ERROR] JAgoraComputation: could not open databases file '" + path +"'.");
	  } catch (ParseException e) {
	    Log.error("[ERROR] JAgoraComputation: could not parse databases file '" + path +"'.");
	    Log.error("[ERROR] " + e.getLocalizedMessage());
	  } catch (ClassCastException e) {
	    Log.error("[ERROR] JAgoraComputation: database file '" + path +"' was not in expected format.");
	  }
	  return false;
	}
	
	public static void main(String[] args) throws Exception{
		JAgoraComputation jac = new JAgoraComputation();
		Log.addLog(new ConsoleLog());
		jac.loadDatabaseFile(JAgoraComputation.DEFAULT_DATABASE_FILE);
	}
}





