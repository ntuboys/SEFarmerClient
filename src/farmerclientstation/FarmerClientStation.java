/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package farmerclientstation;

/**
 *
 * @author arek
 */
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import javax.swing.*;
import org.json.simple.*;

class ServerConnection {
  int id;
  Socket socket;
  String host;
  int port;
  PrintWriter socketWriter;
  BufferedReader socketReader;
  boolean init = false;
  SwingWorker<Void, Void> loop;
  public boolean authed = false;

  ServerConnection(String host, int port) {
    this.host = host;
    this.port = port;
    Random rand = new Random();
    id = rand.nextInt(999999);
  }

  boolean init() {
    if (init) {
      return false;
    }
    try {
      socket = new Socket(host, port);
      socketWriter = new PrintWriter(socket.getOutputStream(), true);
      socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    } catch (UnknownHostException ex) {
      System.out.println("Server not found: " + ex.getMessage());
    } catch (IOException ex) {
      System.out.println("I/O error: " + ex.getMessage());
    }
    init = true;
    return true;
  }

  boolean auth() {
    JSONObject toSend = new JSONObject();
    toSend.put("clientType", "station");
    toSend.put("purpose", "auth");
    socketWriter.println(toSend.toJSONString());
    try {
      String line = socketReader.readLine();
      JSONObject obj = (JSONObject) JSONValue.parse(line);
      System.out.println(line);
      if (obj.get("purpose").equals("auth")) {
        if (obj.get("result").equals("pass")) {
          startLoop();
          authed = true;
          return true;
        }
        return false;

      }
    } catch (IOException ex) {

    }
    return false;
  }

  void sendData(JSONObject data) {
    JSONObject toSend = new JSONObject();
    toSend.put("clientType", "station");
    toSend.put("purpose", "data");

    toSend.put("data", data);
    socketWriter.println(toSend);
  }

  void startLoop() {
    while (init) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ie) {

      }

      JSONObject toSend = new JSONObject();
      Random rand = new Random();
      toSend.put("id", id);
      toSend.put("temp", rand.nextInt(40));
      toSend.put("humidity", rand.nextInt(1000));
      toSend.put("wind", rand.nextInt(50));
      toSend.put("lightLevel", rand.nextInt(100));
      sendData(toSend);
    }
  }
}

public class FarmerClientStation {

  static ServerConnection s;
  static java.util.Timer t;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    // TODO code application logic here
    s = new ServerConnection("localhost", 1111);
    s.init();
    if (s.auth()) {
      System.out.println("authed");
    }
    s.startLoop();
  }
}
