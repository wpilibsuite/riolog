import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.BlockingQueue;

import netconsole2.ErrorMessage;
import netconsole2.ILogger;
import netconsole2.Message;
import netconsole2.PrintMessage;
import netconsole2.RioConsole;

class Riolog implements ILogger {
  static class PlainAppendable implements Message.StyledAppendable {
    private Appendable out;

    public PlainAppendable(Appendable out) {
      this.out = out;
    }

    @Override
    public Message.StyledAppendable append(char c) {
      try {
        out.append(c);
      } catch (IOException e) {
      }
      return this;
    }

    @Override
    public Message.StyledAppendable append(CharSequence csq) {
      try {
        out.append(csq);
      } catch (IOException e) {
      }
      return this;
    }

    @Override
    public Message.StyledAppendable append(CharSequence csq, int start, int end) {
      try {
        out.append(csq, start, end);
      } catch (IOException e) {
      }
      return this;
    }

    @Override
    public Message.StyledAppendable startStyle(int style) {
      return this;
    }

    @Override
    public Message.StyledAppendable endStyle() {
      return this;
    }
  }

  public void log(String msg) {
    System.out.println("LOG: " + msg);
  }

  public void log(String msg, Exception e) {
    System.out.println("LOG: " + msg + ": " + e);
  }

  public static void main(String[] args) {
    ILogger logger = new Riolog();
    RioConsole rioConsole = new RioConsole(logger);

    rioConsole.setConnectedCallback(connected -> {
      logger.log(connected.booleanValue() ? "CONNECTED" : "DISCONNECTED");
    });

    rioConsole.startListening(() -> {
      String teamStr = args[0];
      try {
        return Integer.valueOf(teamStr, 10);
      } catch (NumberFormatException e) {
        logger.log("could not parse team \"" + teamStr + "\"");
        return null;
      }
    });

    final BlockingQueue<Message> messageQueue = rioConsole.getMessageQueue();
    Message.RenderOptions renderOptions = new Message.RenderOptions();

    while (!Thread.interrupted()) {
      Message msg;
      try {
        msg = messageQueue.take();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
      StringWriter writer = new StringWriter();
      PlainAppendable out = new PlainAppendable(writer);
      msg.render(out, renderOptions);
      System.out.println(writer.toString());
    }
  }
}
