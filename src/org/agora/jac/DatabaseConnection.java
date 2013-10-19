package org.agora.jac;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.agora.jac.logging.Log;
import org.json.simple.*;



public class DatabaseConnection {

  protected String url;
  protected String user;
  protected String password;
  
  protected Connection c;
  
  public void loadFromJSON(JSONObject obj){
    url = (String) obj.get("url");
    user = (String) obj.get("user");
    password = (String) obj.get("pass");
  }
  
  public boolean initiateConnection() {
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
  
  public Connection getConnection() { return c; }
  public String getUrl() { return url; }
  public String getUser() { return user; }
  public String getPassword() { return password; }
}
