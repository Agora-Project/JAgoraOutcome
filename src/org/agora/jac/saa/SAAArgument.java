package org.agora.jac.saa;

import java.util.ArrayList;
import java.util.List;

public class SAAArgument{
  protected String source;
  protected int localID;
  protected String globalID;
  
  protected List<SAAAttack> attackers;
  
  protected int positiveVotes;
  protected int negativeVotes;
  
  protected double crowdSupport;
  
  protected double valuation;
  
  public SAAArgument(String source, int localID) {
    this.source = source;
    this.localID = localID;
    globalID = SAAArgument.makeGlobalID(source, localID);
    
    attackers = new ArrayList<SAAAttack>();
    
    positiveVotes = 0;
    negativeVotes = 0;
    valuation = crowdSupport = 0.5;
  }
  
  public static String makeGlobalID(String source, int localID) {
    return source + "(" +Integer.toHexString(localID) +")";
  }
  
  /**
   * Adds an attack <i>in which this argument is the defender</i>!
   * @param att
   */
  public void addAttack(SAAAttack att) {
    attackers.add(att);
  }
  
  public void setVotes(int positiveVotes, int negativeVotes) {
    this.positiveVotes = positiveVotes;
    this.negativeVotes = negativeVotes;
    calculateCrowdSupport();
  }
  
  public void calculateCrowdSupport() {
    // Assumes attackers have been parsed.
    if (positiveVotes + negativeVotes == 0)
      crowdSupport = 0.5;
    else
      crowdSupport = (1.0*positiveVotes / (positiveVotes + negativeVotes)) / (attackers.size()+1);
  }
  
  public void updateValuation() {
    valuation = crowdSupport * calculateAggregatedAttack();
  }
  
  protected double calculateAggregatedAttack() {
    double result = 1;
    for (SAAAttack a : attackers) {
      result = result * (1 - a.getCrowdSupport()*a.getAttacker().getValuation());
    }
    
    return result;
  }
  
  public double getValuation() { return valuation; }
  public String getSource() { return source; }
  public int getID() { return localID; }
  public String getGlobalID() { return globalID; }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((globalID == null) ? 0 : globalID.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SAAArgument other = (SAAArgument) obj;
    if (globalID == null) {
      if (other.globalID != null)
        return false;
    } else if (!globalID.equals(other.globalID))
      return false;
    return true;
  }
}