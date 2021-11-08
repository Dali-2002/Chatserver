package chatserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {

    private final StringParsen stringParsen = new StringParsen(this);
    private final int serverPort;

    private BufferedWriter bw;
    private BufferedReader br;
    private File f = new File("src\\chatserver\\UserData.txt");

    private ArrayList<ServerWorker> workerList = new ArrayList<>();

    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    public List<ServerWorker> getWorkerList() {
        return workerList;
    }

    public void sendToAll(String message, ServerWorker myWorker) {
        // kopiere schlau von ServerWorker.handleLogin
        for (ServerWorker worker : workerList) {
            if (worker == myWorker) {
                worker.send("\033[" + myWorker.getColour() + "me: " + message + "\033[0m");
            } else if (worker.getLogin() != null) {
                worker.send("\033[" + myWorker.getColour() +myWorker.getLogin() + ": " + message + "\033[0m");
            }
        }
    }

    public void sendTo(String message, ServerWorker myWorker, String toWorker) {
        //send message from user to user
        for (ServerWorker worker : workerList) {
            if (worker == myWorker) {
                worker.send("me to " + toWorker + ": " + message);
            } else if (worker.getLogin().equals(toWorker)) {
                worker.send("private from " + myWorker.getLogin() + ": " + "\033[" + myWorker.getColour() + message + "\033[0m");
            }
        }
    }

    public void parse(String eingabe, ServerWorker worker) {
        stringParsen.parse(eingabe, worker);
    }

    public void nick(String eingabe, ServerWorker myWorker) {
        for (ServerWorker worker : workerList) {
            if (!doesUserExists(myWorker.getLogin()) && worker == myWorker) {
                worker.setLogin(eingabe);
                worker.send("username changed to: " + eingabe);
            }
        }
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {
                System.out.println("About to accept client connection");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted Connection from " + clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //////////////

    public String readUserPassword( String userName) {
        try {
            br = new BufferedReader(new FileReader(f));
            String line;
            String[] lineArray;
            while ((line = br.readLine()) != null) {

                lineArray = line.split(" ");
                if (lineArray[0].equals(userName)) {
                    return lineArray[1];
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createUser(String userName, String password) {
        if(userName.equals("guest") || doesUserExists(userName)) {
            return;
        }
        try {

            bw = new BufferedWriter(new FileWriter(f, true));
            br = new BufferedReader(new FileReader(f));
            while(br.readLine() != null) {
                bw.newLine();
            }
            bw.write(userName + " " + password);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean doesUserExists( String userName) {
        try {
            br = new BufferedReader(new FileReader(f));
            String line;
            String[] lineArray;
            while ((line = br.readLine()) != null) {

                lineArray = line.split(" ");
                if (lineArray[0].equals(userName)) {
                    br.close();
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
