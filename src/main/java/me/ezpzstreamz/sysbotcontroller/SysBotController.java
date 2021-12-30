package me.ezpzstreamz.sysbotcontroller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

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
            if (conn.isConnected()) {
                System.out.println("Connected");
            }
            out = new PrintWriter(conn.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error connecting to the socket.");
        }
    }

    public void disconnect() throws IOException {
        sendCommand("detachController");
        conn.close();
        conn = null;
        out.close();
        out = null;
        in.close();
        in = null;
    }

    public String parseJumps(String pointer) {
        String ptr = pointer;
        if (ptr.charAt(ptr.length() - 1) == ']')
            ptr += "+0";
        String[] eval = ptr.replace("main", "").replace("[", "").replace("]", "").split("\\+");
        StringBuilder jumps = new StringBuilder();
        for (String s :
                eval) {
            if (!s.equalsIgnoreCase(""))
                jumps.append(" 0x").append(s);
        }
        return jumps.toString().trim();
    }

    public CompletableFuture<String> resolvePointer(String ptr) {
        return sendCommand("pointerRelative " + parseJumps(ptr)).thenCompose(v -> CompletableFuture.supplyAsync(() -> {
            try {
                String line = in.readLine();
                while (line == null) {
                    line = in.readLine();
                }
                return line;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }));
    }

    public CompletableFuture<Void> sendCommand(String command) {
        return CompletableFuture.runAsync(() -> {
            System.out.println(command);
            out.println(command);
        });
    }

    public CompletableFuture<Void> click(String button, int sleep) {
        return sendCommand("click " + button).thenRun(() -> {
            try {
                TimeUnit.SECONDS.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> press(String button, int sleep) {
        return sendCommand("press " + button).thenRun(() -> {
            try {
                TimeUnit.SECONDS.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> release(String button, int sleep) {
        return sendCommand("release " + button).thenRun(() -> {
            try {
                TimeUnit.SECONDS.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<String> peek(String ptr, int size) {
        return sendCommand("pointerPeek " + size + " " + parseJumps(ptr)).thenCompose(
                v -> CompletableFuture.supplyAsync(() -> {
                    try {
                        String line = in.readLine();
                        while (line == null) {
                            line = in.readLine();
                        }
                        return line;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }));
    }

    public CompletableFuture<Void> poke(String ptr, String data) {
        return sendCommand("pointerPoke " + data + " " + parseJumps(ptr));
    }

    public CompletableFuture<Void> setStick(String stick, int x, int y, int sleep) {
        return sendCommand("setStick " + stick + " " + x + " " + y).thenRun(() -> {
            try {
                TimeUnit.SECONDS.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<String> getTitleID() {
        return sendCommand("getTitleID").thenCompose(v -> CompletableFuture.supplyAsync(() -> {
            try {
                String line = in.readLine();
                while (line == null) {
                    line = in.readLine();
                }
                return line;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }));
    }

    public CompletableFuture<String> getHeapBase() {
        return sendCommand("getHeapBase").thenCompose(v -> CompletableFuture.supplyAsync(() -> {
            try {
                String line = in.readLine();
                while (line == null) {
                    line = in.readLine();
                }
                return line;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }));
    }

    public CompletableFuture<String> getMainNsoBase() {
        return sendCommand("getMainNsoBase").thenCompose(v -> CompletableFuture.supplyAsync(() -> {
            try {
                String line = in.readLine();
                while (line == null) {
                    line = in.readLine();
                }
                return line;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }));
    }

    public CompletableFuture<Void> enterCode(String code) {
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder command = new StringBuilder();
            for (byte b : code.getBytes()) {
                if (b == '0') {
                    command.append(39).append(" ");
                } else {
                    command.append(b - 19).append(" ");
                }
            }
            return command.toString();
        }).thenCompose(s -> sendCommand("key " + s.trim()));
    }

    public CompletableFuture<Void> touch(int x, int y) {
        return sendCommand("touch " + x + " " + y);
    }

    public CompletableFuture<Void> updateFreezeRate() {
        return sendCommand("configure freeRate 1");
    }

    public CompletableFuture<Void> freeze(String ptr, String data) throws ExecutionException, InterruptedException {
        return sendCommand("freeze 0x" + resolvePointer(ptr).get().trim() + " " + data);
    }

    public CompletableFuture<Void> moveForward(int sleep) {
        return setStick("LEFT", 0, 0x7FFE, sleep).thenCompose(v -> setStick("LEFT", 0, 0, 0));

    }

}
