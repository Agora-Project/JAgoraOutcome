package org.agora.jac.saa;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.agora.jac.Options;
import org.agora.jac.logging.Log;

public class SAAGraph {
	
  protected SAAArgument[] arguments;
  protected Map<String, SAAArgument> argumentMap;
  
  protected Map<String, SAAAttack> attackMap;
  
  protected double MAX_FIXPOINT_ERROR = 0.001; 
  
  public void computeOutcomes(){
    double change = Double.MAX_VALUE;
    while (change > MAX_FIXPOINT_ERROR) {
      change = 0;
      for (SAAArgument a : arguments) {
        double oldValuation = a.getValuation();
        a.updateValuation();
        change = Math.max(change, Math.abs(oldValuation - a.getValuation()));
      }
    }
  }
  
  /**
   * This takes in a ResultSet and makes the following assumptions: 1) first
   * column is argument source; 2) second column is argument ID.
   * @param rs Where the database results come from.
   */
  public void loadArgumentsFromResultSet(ResultSet rs) throws SQLException {
    List<SAAArgument> tmpArgs = new LinkedList<SAAArgument>();
    argumentMap = new HashMap<String, SAAArgument>();
    attackMap = new HashMap<String, SAAAttack>();
    
    while (rs.next()) {
      SAAArgument a = new SAAArgument(rs.getString("source_ID"), rs.getInt("arg_ID"));
      tmpArgs.add(a);
      argumentMap.put(a.getGlobalID(), a);
    }
    
    this.arguments = tmpArgs.toArray(new SAAArgument[0]);
  }
  
  /**
   * Assumes the column ordering is sourceArg.source, sourceArg.localID,
   * targetArg.source, targetArg.localID
   * @param rs Where the database results come from.
   * @throws SQLException
   */
  public void loadAttacksFromResultSet(ResultSet rs) throws SQLException  {
    while(rs.next()) {
      String attGlobalID = SAAArgument.makeGlobalID(rs.getString("source_ID_attacker"), rs.getInt("arg_ID_attacker"));
      String defGlobalID = SAAArgument.makeGlobalID(rs.getString("source_ID_defender"), rs.getInt("arg_ID_defender"));
      SAAAttack attack = new SAAAttack(argumentMap.get(attGlobalID), argumentMap.get(defGlobalID));
      addAttack(attack);
    }
  }
  
  public void addAttack(SAAAttack attack) {
    attackMap.put(attack.getGlobalID(), attack);
    attack.getDefender().addAttack(attack);
  }
  
  /**
   * Assumes the ResultSet contains COUNT(*) information.
   * @param rs
   * @throws SQLException
   */
  public void loadAttackVotesFromResultSet(ResultSet rs) throws SQLException  {
    while (rs.next()) {
      String attID = SAAAttack.makeGlobalID(rs.getString("source_ID_attacker"),
                                            rs.getInt("arg_ID_attacker"),
                                            rs.getString("source_ID_defender"),
                                            rs.getInt("arg_ID_defender"));
      SAAAttack attack = attackMap.get(attID);
      attack.setVotes(rs.getInt("positive_votes"), rs.getInt("negative_votes"));
    }
  }
  
  public void loadArgumentVotesFromResultSet(ResultSet rs) throws SQLException  {
    while (rs.next()) {
      String argID = SAAArgument.makeGlobalID(rs.getString("source_ID"), rs.getInt("arg_ID"));
      SAAArgument arg = argumentMap.get(argID);
      arg.setVotes(rs.getInt("positive_votes"), rs.getInt("negative_votes"));
    }
  }
  
  public SAAArgument[] getArguments() { return arguments; }
  
  public void printGraph() {
    if (!Options.DEBUG_MESSAGES)
      return;
    
    for (SAAArgument a : arguments) {
      Log.debug(a.getGlobalID() + " - " + a.getValuation());
    }
  }
}





