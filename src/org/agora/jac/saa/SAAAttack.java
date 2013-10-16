package org.agora.jac.saa;

public class SAAAttack {
  protected SAAArgument attacker;
  protected SAAArgument defender;
  
  protected int positiveVotes;
  protected int negativeVotes;
  
  protected double crowdSupport;
  
  protected String globalID;
  
  public SAAAttack(SAAArgument attacker, SAAArgument defender) {
    this.attacker = attacker;
    this.defender = defender;
    
    positiveVotes = 0;
    negativeVotes = 0;
    
    crowdSupport = 0.5;
    
    globalID = SAAAttack.makeGlobalID(attacker.getSource(), attacker.getID(),
                                      defender.getSource(), defender.getID());
  }
  
  public String getGlobalID() { return globalID; }
  public SAAArgument getDefender() { return defender; }
  public SAAArgument getAttacker() { return attacker; }
  public double getCrowdSupport() { return crowdSupport; }
  
  public void setVotes(int positiveVotes, int negativeVotes) {
    this.positiveVotes = positiveVotes;
    this.negativeVotes = negativeVotes;
    calculateCrowdSupport();
  }
  
  public void calculateCrowdSupport() {
    if (positiveVotes + negativeVotes == 0)
      crowdSupport = 0.5;
    else
      crowdSupport = 1.0*positiveVotes / (positiveVotes + negativeVotes);
  }
  
  public static String makeGlobalID(String sourceAtt, int idAtt, String sourceDef, int idDef) {
    return SAAArgument.makeGlobalID(sourceAtt, idAtt) + "->" + SAAArgument.makeGlobalID(sourceDef, idDef);
  }
}
