package org.agora.jac.logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class FileLog extends WriterLog {
  
  public PrintWriter createPrintWriter(String file) {
    PrintWriter printWriter = null;
    try {
    File f = new File(file);
    if (!f.exists())
      f.createNewFile();

    printWriter = new PrintWriter(new FileOutputStream(f, true));
    } catch (IOException e) {
      System.err.println("[ERROR] Failed to create PrintWriter for '"+file+"'.");
      Log.error("[ERROR] Failed to create PrintWriter for '"+file+"'.");
    }
    
    return printWriter;
  }
  
  public FileLog(String logFile, String errorFile, String debugFile) {
    super(null, null, null);
    logStream = createPrintWriter(logFile);
    errorStream = createPrintWriter(errorFile);
    debugStream = createPrintWriter(debugFile);
  }

}
