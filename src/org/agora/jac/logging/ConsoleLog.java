package org.agora.jac.logging;

public class ConsoleLog extends StreamLog {
  public ConsoleLog() {
    super(System.out, System.err, System.out);
  }
}
