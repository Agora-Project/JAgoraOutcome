package org.agora.jac;

import org.json.simple.*;



public class DatabaseConnection {
  protected String databaseURL;
  protected String databaseUsername;
  protected String databasePassword;
  
  public void loadFromJSON(JSONObject obj){
    databaseURL = (String) obj.get("url");
    databaseUsername = (String) obj.get("user");
    databasePassword = (String) obj.get("pass");
  }
}
