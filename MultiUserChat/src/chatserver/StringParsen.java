package chatserver;

import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringParsen {
    private Server server;
    public StringParsen(Server server) {
        this.server = server;
    }

    public void parse(String eingabe, ServerWorker worker) {
        // Reguläre Ausdrücke (Regular Expressions, RegEx)

            Pattern patternSendTo = Pattern.compile("^!sendTo ([A-Za-z]+) (.*)");
            Pattern patternCreateUser = Pattern.compile("^!createUser ([A-Za-z]+) ([A-Za-z]+)");
            Pattern patternColour = Pattern.compile("^!myColour ([A-Za-z]+)");
            Pattern patternNick = Pattern.compile("^!nick ([A-Za-z]+)");
            Pattern patternHelp = Pattern.compile("^!help");

            Matcher matcherSendTo = patternSendTo.matcher(eingabe);
            Matcher matcherCreateUser = patternCreateUser.matcher(eingabe);
            Matcher matcherColour = patternColour.matcher(eingabe);
            Matcher matcherNick = patternNick.matcher(eingabe);
            Matcher matcherHelp = patternHelp.matcher(eingabe);

        if (matcherSendTo.find()) {
            // führe auf dem Server sendTo(user, message);
            String userName = matcherSendTo.group(1);
            String message = matcherSendTo.group(2);

            server.sendTo(message, worker, userName);
            System.out.println("sendTo " + userName + ", message: " + message);
        } else if (matcherColour.find()) {
            // ändere im Client die Farbe auf "colour"
            String colour = matcherColour.group(1);
            System.out.println("change colour to " + colour);
            if(colour.equals("red")) {
                worker.setColour("31m");
            } else if(colour.equals("yellow")) {
                worker.setColour("33m");
            } else if(colour.equals("green")) {
                worker.setColour("32m");
            } else if(colour.equals("blue")) {
                worker.setColour("34m");
            } else if(colour.equals("white")) {
                worker.setColour("97m");
            }

        } else if(matcherNick.find()){
            worker.setLogin(matcherNick.group(1));
            System.out.println("changed name to: " + worker.getLogin());
        } else if (matcherCreateUser.find()) {
            String name = matcherCreateUser.group(1);
            String password = matcherCreateUser.group(2);

            server.createUser(name,password);
            System.out.println("created new user " + matcherCreateUser.group(1));
        } else if (matcherHelp.find()) {
            worker.send("\r\n!sendTo //sends message to user ([target username] [message])\r\n" +
                            "!createUser //creates a new user ([name] [password])\r\n" +
                            "!myColour //changes the colour you're writing with ([myColour])\r\n (red,green,yellow,blue,white)\r\n" +
                            "!nick //changes your nickname for this session, but not your loginname([new nickname])\r\n" +
                            "!help //show this message\r\n"+
                            "logoff //disconnect client from server\r\n");
        } else {
            // sende die Eingabe an alle
            server.sendToAll(eingabe, worker);
        }
    }

}
