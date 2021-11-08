package chatserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ServerWorker extends Thread {
    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;
    private String colour ="97m";

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("quit".equalsIgnoreCase(cmd) || "logoff".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                } else {
                    server.parse(line,this);
                }
            }
            outputStream.flush();
        }

        clientSocket.close();
    }

    private void handleLogoff() {
        List<ServerWorker> workerList = server.getWorkerList();
        // send other online users current user's status
        String onlineMsg = "offline " + login + "\r\n";
        for (ServerWorker worker : workerList) {
            if (!login.equals(worker.getLogin())) {
                worker.send(onlineMsg);
            }
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLogin() {
        return login;
    }
    public String getColour() { return colour;}
    public void setColour(String colour) {this.colour = colour;}

    public void setLogin(String login) {
        if(!login.equals(null)) {
            this.login = login;
        }
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) {
        if (tokens.length >= 2 && tokens.length <=3) {
            String login = tokens[1];
            String password = "error";
            if(tokens.length == 3) {
                password = tokens[2];
            }
            if (login.equals("guest") || password.equals(server.readUserPassword(login))) {
                String msg = "ok login\r\n";
                try {
                    outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.login = login;
                System.out.println("User logged in succsesfully: " + login);

                List<ServerWorker> workerList = server.getWorkerList();

                // send current user all other online logins
                for (ServerWorker worker : workerList) {
                    if (worker.getLogin() != null) {
                        if (!login.equals(worker.getLogin())) {
                            String msg2 = "online " + worker.getLogin() + "\r\n";
                            send(msg2);
                        }
                    }
                }
                // send other online users current user's status
                String onlineMsg = "online " + login + "\r\n";
                for (ServerWorker worker : workerList) {
                    if (!login.equals(worker.getLogin())) {
                        worker.send(onlineMsg);
                    }
                }
            } else {
                String msg = "error login\r\n";
                try {
                    outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void send(String msg) {
        if (login != null) {
            try {
                msg = msg+"\r\n";
                outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
