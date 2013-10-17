package org.agora.jac;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.agora.jac.saa.SAAArgument;
import org.agora.jac.saa.SAAGraph;



public class JAgoraComputation {
	
	protected Connection c;
	protected SAAGraph graph;
	
	public boolean initiateConnection(String url, String user, String password) {
	  try {
      Class.forName("com.mysql.jdbc.Driver");
      c = DriverManager.getConnection(url, user, password);
      return true;
    } catch (ClassNotFoundException e) {
      System.err.println("JAgoraComputation: unable to load com.mysql.jdbc.Driver.");
      System.err.println(e.getMessage());
    } catch (SQLException e) {
      System.err.println("JAgoraComputation: problem connecting to '" + url + "'");
      System.err.println(e.getMessage());
    }
	  return false;
	}
	
	public boolean terminateConnection() {
	  try {
      c.close();
      return true;
    } catch (SQLException e) {
      System.err.println("JAgoraComputation: problems disconnecting database.");
      System.err.println(e.getMessage());
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
	    System.err.println("JAgoraComputation: problem retrieving graph.");
      System.err.println(e.getMessage());
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
	    System.err.println("JAgoraComputation: problem storing outcomes.");
      System.err.println(e.getMessage());
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
	
	
	
	public static void main(String[] args) throws Exception{
		JAgoraComputation jac = new JAgoraComputation();
		if (!jac.initiateConnection("jdbc:mysql://192.168.8.200:3306/agora-db", "agora-dev", "pythagoras"))
		  return;
		if (!jac.loadDataFromDB()) return;
		jac.printGraph();
		System.out.print("Starting computation... ");
		jac.computeOutcomes();
		System.out.println("done!");
		System.out.print("Updating database... ");
		if(jac.updateDatabase())
		  System.out.println("done!");
		else
		  System.out.println("failed!");
		System.out.println();
		if (!jac.terminateConnection()) return;
		jac.printGraph();
	}
}
