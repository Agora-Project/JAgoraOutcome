package org.agora.jac.logging;

import java.io.*;

public class WriterLog extends Log {

  protected PrintWriter logStream;
  protected PrintWriter errorStream;
  protected PrintWriter debugStream;
  
  public WriterLog(PrintWriter logStream, PrintWriter errorStream, PrintWriter debugStream) {
    this.logStream = logStream;
    this.errorStream = errorStream;
    this.debugStream = debugStream;
  }
  
  @Override
  public void logMessage(String message, boolean newline) {
    if (newline) logStream.println(message);
    else logStream.print(message);
  }

  @Override
  public void errorMessage(String message, boolean newline) {
    if (newline) errorStream.println(message);
    else errorStream.print(message);
  }
  
  @Override
  public void debugMessage(String message, boolean newline) {
    if (newline) debugStream.println(message);
    else debugStream.print(message);
  }
}
