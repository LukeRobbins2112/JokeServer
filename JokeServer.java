/*--------------------------------------------------------

1. Luke Robbins / 1/25/2019

2. Java 1.8

3. Compilation Instructions

> javac JokeServer.java

4. Run Instructions

In separate windows:
> java JokeServer
> java JokeClient
> java JokeClientAdmin

All acceptable commands are displayed on the various consoles.
Hit Control-C to end the server application

5. List of files needed for running the program.

 a. JokeServer.java
 b. JokeClient.java
 c. JokeClientAdmin.java

6. Notes:

----------------------------------------------------------*/

import java.awt.TrayIcon.MessageType;
import java.io.*;  // Include all IO libraries
import java.net.*; // Include all networking libraries
import java.util.HashMap;
import java.util.Random;

//import com.sun.corba.se.spi.activation.Server;

import java.util.Arrays;
import java.util.Collections;

enum ConnectionType{
    CLIENT,
    CLIENT_ADMIN;
}

enum MESSAGE_TYPE{
    JOKE,
    PROVERB;
}

class ClientState{

    public int clientID = -1;
    String clientName;
    Integer [] jokeOrder = {0, 1, 2, 3};
    Integer [] proverbOrder = {0, 1, 2, 3};
    int jokeIndex = 0;
    int proverbIndex = 0;
}


// Worker class will be used to perform some function for each connection
// Each new Worker is launched in an individual thread
class Worker extends Thread{

    // Data -> Socket object, assigned based on what Worker is given by the JokeServer
    private Socket socket;
    private String header;
    private static HashMap<Integer, ClientState> clientState = new HashMap<>();
    private static MESSAGE_TYPE messageType = MESSAGE_TYPE.JOKE;
    
    private static String proverbIDs[] = {"PA", "PB", "PC", "PD"};
    private static String proverbs[] = {
        "Don't put off until tomorrow what you can do today",
        "The pen is mightier than the sword",
        "Knowledge is power",
        "Hope for the best, prepare for the worst"
    };

    private static String jokeIDs[] = {"JA", "JB", "JC", "JD"};
    private static String jokes[]= {
        "What did the Buddhist ask the hot dog vendor? ..... 'Make me one with everything.'",
        "I bought the world's worst thesaurus yesterday ..... Not only is it terrible, it's terrible.",
        "How does NASA organize a party? .... They planet.",
        "What's a pirates favorite letter? ..... You think it's R but it be the C."
    };

    // Constructor - takes Socket object as argument
    Worker (Socket s){
        this.socket = s;
        this.header = (s.getLocalPort() == 4545) ? "" : "<S2> ";
    }

    // Overloading Thread function
    public void run(){
        
        BufferedReader in = null;
        PrintStream out = null;

        try{
            // Get IO streams from the socket, to communicate back and forth with client
            in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out = new PrintStream(this.socket.getOutputStream());

            try{
                // Read in the domain name, alert user (on the server side) that server is performing a lookup
                String domain;
                domain = in.readLine();
               
                if (domain.equals("quit")){
                    System.out.println("Client ending session");
                    out.print("Ending session\n");
                }
                else if (domain.startsWith("NAME:")){
                    addNewClient(domain);
                }
                else {
                    try{
                        int clientID = Integer.parseInt(domain);
                        printJokeOrProverb(out, clientID);
                    }catch (NumberFormatException e){
                        out.print("Error - expected Client ID");
                    }
                    
                }
                
               
            } catch(IOException e){                          // Print any IO errors that occur during the lookup
                System.out.println("Server read error");
                e.printStackTrace();
            }

            // Close the socket after we're done
            this.socket.close();
        } catch(IOException ex){
            System.out.println(ex);  // Print any I/O errors that occur during socket opening/closing
        }
    }

    void addNewClient(String data){

        String [] input = data.split(" ");
        String name = input[0].substring(5);
        int clientID = Integer.parseInt(input[1]);
        if (!clientState.containsKey(clientID)){
            ClientState newClient = new ClientState();
            newClient.clientName = name;
            newClient.clientID = clientID;

            // Shuffle initial order before adding new ClientState
            Collections.shuffle(Arrays.asList(newClient.jokeOrder));
            Collections.shuffle(Arrays.asList(newClient.proverbOrder));
            clientState.put(clientID, newClient);
        }
    }

    void printJokeOrProverb(PrintStream out, int clientID){

            // Alert client that you are looking up the domain
            out.println("Request to print out joke/proverb received");

            // Look up or create client state entry
            ClientState cState;

            if (clientState.containsKey(clientID)){
                cState = clientState.get(clientID);
            }
            else{

                // @TODO with the addition of the name step, all clients at this stage must
                // Already exist, so this conditional should be removed and just assume that the client exists
                cState = new ClientState();
                cState.clientID = clientID;

                // Shuffle initial order before adding new ClientState
                Collections.shuffle(Arrays.asList(cState.jokeOrder));
                Collections.shuffle(Arrays.asList(cState.proverbOrder));
                clientState.put(clientID, cState);
            }

            String response = this.header;
            String clientString =  " (" + cState.clientName + ")";

            if (Mode.getMode() == MESSAGE_TYPE.JOKE){

                int joke = cState.jokeOrder[cState.jokeIndex++];
                response += jokeIDs[joke];
                response += clientString;
                response += ": ";
                response += (jokes[joke]);

                if (cState.jokeIndex == cState.jokeOrder.length){
                    response += (" -- (JOKE CYCLE COMPLETED)\n");
                    Collections.shuffle(Arrays.asList(cState.jokeOrder));
                    cState.jokeIndex = 0;
                }

                out.println(response);
            }
            else{

                int proverb = cState.proverbOrder[cState.proverbIndex++];
                response += proverbIDs[proverb];
                response += clientString;
                response += ": ";
                response += (proverbs[proverb]);

                if (cState.proverbIndex == cState.proverbOrder.length){
                    response += (" -- PROVERB CYCLE COMPLETED\n");
                    Collections.shuffle(Arrays.asList(cState.proverbOrder));
                    cState.proverbIndex = 0;
                }

                out.println(response);
            }
            
    }

}

class Mode{
    private static MESSAGE_TYPE type = MESSAGE_TYPE.JOKE;
    
    public static void toggle(){
        type = (type == MESSAGE_TYPE.JOKE) ? MESSAGE_TYPE.PROVERB : MESSAGE_TYPE.JOKE;
        System.out.printf("MODE SET TO %s\n", ((type == MESSAGE_TYPE.JOKE) ? "JOKE" : "PROVERB"));
    }

    public static MESSAGE_TYPE getMode(){
        return type;
    }
}

class ToggleMode extends Thread{

    private ServerSocket serverSocket;

    public ToggleMode(ServerSocket servSock){
        this.serverSocket = servSock;
    }

    public void run(){

        Socket sock;

        try{
            while(true){
                sock = this.serverSocket.accept();  // Wait for next client connection, accept when it comes
                Mode.toggle();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        
    }
}

public class JokeServer{

    public static void main(String[] args) throws IOException {

        int q_len = 6; 

        int primaryClientPort = 4545; 
        int secondaryClientPort = 4546;
        int primaryAdminPort = 5050;
        int secondaryAdminPort = 5051;
        int serverPort = -1;
        int adminPort = -1;

        if (args.length > 0 && args[0].equals("secondary")){
            serverPort = secondaryClientPort;
            adminPort = secondaryAdminPort;
        }
        else{
            serverPort = primaryClientPort;
            adminPort = primaryAdminPort;
        }

        Socket sock; // Client socket object, to be assigned as they come in

        // Server socket
        ServerSocket servSock = new ServerSocket(serverPort, q_len);
        ServerSocket adminSock = new ServerSocket(adminPort, q_len);

        // Admin setup
        ToggleMode tMode = new ToggleMode(adminSock);
        tMode.start();

        System.out.printf("Luke Robbins's Joke server 1.8 starting up, listening at port %d.\n", serverPort);

        // Loop "forever", accepting new connections as they come in
        while(true){
            sock = servSock.accept();  // Wait for next client connection, accept when it comes
            new Worker(sock).start();  // Once we get a connection, pass it to worker
        }

        // Our loop runs "forever", but realistically it won't, and we should close that socket
        //servSock.close();
    }

    
}