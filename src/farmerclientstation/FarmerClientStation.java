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

    boolean auth(String user, String pass) {
        JSONObject toSend = new JSONObject();
        toSend.put("type", "station");
        toSend.put("purpose", "auth");
        toSend.put("username", user);
        toSend.put("password", pass);
        socketWriter.println(toSend);
        try {
            String line = socketReader.readLine();
            JSONObject obj = (JSONObject) JSONValue.parse(line);
            System.out.println(line);
            if (obj.get("purpose").equals("auth")) {
                if (obj.get("result").equals("pass")) {
                    startLoop();
                    authed = true;
                    return true;
                } else {
                    return false;
                }
            }
        } catch (IOException ex) {
            
        }
        return false;
    }

    void startLoop() {
        loop = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (init) {
                    try {
                        String line = socketReader.readLine();
                        //JSONObject obj = (JSONObject) JSONValue.parse(line);
                        System.out.println(line);
                    } catch (IOException ex) {
                        System.out.println("I/O error occured");
                    }
                }
                return null;
            }
        };
        loop.execute();
    }
    
    void sendData(String data) {
        JSONObject toSend = new JSONObject();
        toSend.put("message", data);
        socketWriter.println(toSend);
    }
}
class SayHello extends TimerTask {
    ServerConnection c;
    String to;
    SayHello(ServerConnection c, String to){
        this.c = c;
        this.to = to;
    }
    public void run() {
        c.sendData(to);
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
        if(s.auth("stationloign", "stationpass")){
            System.out.println("authed");
        }
        s.startLoop();
        s.sendData("hello from station");
        t = new java.util.Timer();
        t.schedule(new SayHello(s, "Hello from server"), 0, 5000);
    }
    
}
