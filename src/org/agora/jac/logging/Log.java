package org.agora.jac.logging;

import java.util.*;

import org.agora.jac.*;

public abstract class Log {
  
  protected static List<Log> logs = new LinkedList<Log>();
  
  public static void addLog(Log log) { logs.add(log); }
  public static void removeLog(Log log) { logs.remove(log); }
  
  
  public static void log(String message) { log(message, true); }
  public static void log(String message, boolean newline) {
    if (!Options.LOG_MESSAGES) return;
    for (Log logInst : Log.logs)
      logInst.logMessage(message, newline);
  }
  
  public static void error(String message) { error(message, true); }
  public static void error(String message, boolean newline) {
    if (!Options.ERROR_MESSAGES) return;
    for (Log logInst : Log.logs)
      logInst.errorMessage(message, newline);
  }
  
  public static void debug(String message) { debug(message, true); }
  public static void debug(String message, boolean newline) {
    if (!Options.DEBUG_MESSAGES) return;
    for (Log logInst : Log.logs)
      logInst.debugMessage(message, newline);
  }
  
  public abstract void logMessage(String message, boolean newline);
  public abstract void errorMessage(String message, boolean newline);
  public abstract void debugMessage(String message, boolean newline);
}
