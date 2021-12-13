package me.ezpzstreamz.sysbotcontroller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SysBotController {

    protected final String ip;
    protected final int port;
    protected Socket conn;
    protected PrintWriter out;
    protected BufferedReader in;

    public SysBotController(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void connect() {
        try {
            conn = new Socket(ip, port);
            if(conn.isConnected()) {
                System.out.println("Connected");
            }
            out = new PrintWriter(conn.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error connecting to the socket.");
        }
    }

    public void disconnect() throws IOException {
        conn.close();
        conn = null;
        out.close();
        out = null;
        in.close();
        in = null;
    }

    public String parseJumps(String pointer) {
        String ptr = pointer;
        if(ptr.charAt(ptr.length() - 1) == ']')
            ptr += "+0";
        String[] eval = ptr.replace("main", "").replace("[", "").replace("]", "").split("\\+");
        StringBuilder jumps = new StringBuilder();
        for (String s :
                eval) {
            if(!s.equalsIgnoreCase(""))
                jumps.append(" 0x").append(s);
        }
        return jumps.toString().trim();
    }

    public String resolvePointer(String ptr) throws InterruptedException, IOException {
        sendCommand("pointerRelative " + parseJumps(ptr));
        Thread.sleep(500);
        return in.readLine();
    }

    public void sendCommand(String command) {
        System.out.println(command);
        out.println(command);
    }

    public void click(String button, int sleep) throws InterruptedException {
        sendCommand("click " + button);
        Thread.sleep(sleep);
    }

    public void press(String button, int sleep) throws InterruptedException {
        sendCommand("press " + button);
        Thread.sleep(sleep);
    }

    public void release(String button, int sleep) throws InterruptedException {
        sendCommand("release " + button);
        Thread.sleep(sleep);
    }

    public String peek(String ptr, int size) throws InterruptedException, IOException {
        sendCommand("pointerPeek " + size + " " + parseJumps(ptr));
        Thread.sleep(400);
        return in.readLine();
    }

    public void poke(String ptr, String data) {
        sendCommand("pointerPoke " + data + " " + parseJumps(ptr));
    }

    public void setStick(String stick, int x, int y, int sleep) throws InterruptedException {
        sendCommand("setStick " + stick + " " + x + " " + y);
        Thread.sleep(sleep);
    }

    public String getTitleID() throws InterruptedException, IOException {
        sendCommand("getTitleID");
        Thread.sleep(400);
        return in.readLine();
    }

    public String getHeapBase() throws InterruptedException, IOException {
        sendCommand("getHeapBase");
        Thread.sleep(400);
        return in.readLine();
    }

    public String getMainNsoBase() throws InterruptedException, IOException {
        sendCommand("getMainNsoBase");
        Thread.sleep(400);
        return in.readLine();
    }

    public void enterCode(String code) {
        StringBuilder command = new StringBuilder();
        for(byte b : code.getBytes()) {
            if(b == '0') {
                command.append(39).append(" ");
            } else {
                command.append(b - 19).append(" ");
            }
        }
        sendCommand("key " + command.toString().trim());
    }

    public void touch(int x, int y) {
        sendCommand("touch " + x + " " + y);
    }

    public void updateFreezeRate() {
        sendCommand("configure freeRate 1");
    }

    public void freeze(String ptr, String data) throws IOException, InterruptedException {
        sendCommand("freeze 0x" + resolvePointer(ptr).trim() + " " + data);
    }

    public void moveForward(int sleep) throws InterruptedException {
        setStick("LEFT", 0, 0x7FFE, sleep);
        setStick("LEFT", 0, 0, 0);
    }

}
