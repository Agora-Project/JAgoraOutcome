package org.agora.jac;

import java.io.*;
import java.sql.Connection;
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
  
	protected List<DatabaseConnection> dbConnections;
	
	public JAgoraComputation() {
	  dbConnections = new LinkedList<DatabaseConnection>();
	}
	
	public SAAGraph loadDataFromDB(DatabaseConnection dbc) {
	  SAAGraph graph = new SAAGraph();
	  Statement s;
	  ResultSet rs;
	  try {
	    s = dbc.getConnection().createStatement();
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
	    return graph;
	  } catch (SQLException e) {
	    Log.error("JAgoraComputation: problem retrieving graph.");
	    Log.error(e.getMessage());
	  }
    return null;
	}
	
	
	public boolean updateDatabase(DatabaseConnection dbc, SAAGraph graph) {
	  try {
	    Connection c = dbc.getConnection();
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
	    s.close();
	    c.setAutoCommit(true);
	    return true;
	  } catch (SQLException e){
	    Log.error("[ERROR] JAgoraComputation: problem storing outcomes.");
      Log.error(e.getMessage());
      e.printStackTrace();
	  }
	  return false;
	}
	
	public boolean processDatabase(DatabaseConnection dbc) {
    if (!dbc.initiateConnection())
      return false;
    
    SAAGraph graph = loadDataFromDB(dbc);
    if (graph == null)
      return false;
    
    graph.printGraph();
    
    Log.log("Starting computation... ", false);
    graph.computeOutcomes();
    Log.log("done!");
    
    Log.log("Updating database... ", false);
    if(updateDatabase(dbc, graph)) Log.log("done!");
    else                           Log.log("failed!");
    
    if (!dbc.terminateConnection())
      return false;
    graph.printGraph();
    return true;
	}
	
	
	public boolean processAllDatabases() {
	  boolean allSuccessful = true;
	  
	  for (DatabaseConnection dbc : dbConnections) {
	    Log.log("[LOG] Processing database '"+dbc.getUrl()+"'... ");
	    boolean success = processDatabase(dbc);
	    Log.log("[LOG] Processing database '"+dbc.getUrl()+"' was a " + ((success) ? "success!" : "failure!"));
	    allSuccessful = allSuccessful && success;
	  }
	  return allSuccessful; 
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
		jac.processAllDatabases();
	}
}





