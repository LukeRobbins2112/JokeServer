/*--------------------------------------------------------

1. Luke Robbins / 1/15/2019

2. Java 1.8

3. Compilation Instructions

> javac JokeServer.java

4. Run Instructions

In separate windows:
> java JokeServer
> java JokeClient
> java JokeClientAdmin


5. List of files needed for running the program.

 a. checklist-joke.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

6. Notes:

----------------------------------------------------------*/

import java.io.*;  // Include all IO libraries
import java.net.*; // Include all networking libraries
import java.util.HashMap;
import java.util.Random;
import java.util.Collections;

enum MESSAGE_TYPE{
    JOKE,
    PROVERB;
}

class ClientState{

    public int clientID = -1;
    int [] jokeOrder = {0, 1, 2, 3};
    int [] proverbOrder = {0, 1, 2, 3};
    int jokeIndex = 0;
    int proverbIndex = 0;
}


// Worker class will be used to perform some function for each connection
// Each new Worker is launched in an individual thread
class Worker extends Thread{

    // Data -> Socket object, assigned based on what Worker is given by the JokeServer
    private Socket socket;
    private int currClientID;
    private static HashMap<Integer, ClientState> clientState = new HashMap<>();

    private static MESSAGE_TYPE messageType = MESSAGE_TYPE.JOKE;
    private static String proverbs[] = {
        "Don’t put off until tomorrow what you can do today",
        "The pen is mightier than the sword",
        "Knowledge is power",
        "Hope for the best, prepare for the worst"
    };
    private static String jokes[] = {
        "What did the Buddhist ask the hot dog vendor? ..... 'Make me one with everything.'",
        "I bought the world’s worst thesaurus yesterday ..... Not only is it terrible, it’s terrible.",
        "How does NASA organize a party? .... They planet.",
        "What’s a pirates favorite letter? ..... You think it’s R but it be the C."
    };

    // Constructor - takes Socket object as argument
    Worker (Socket s){
        this.socket = s;
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
               
                if (domain.equals("TOGGLE_JOKE_PROVERB_MODE")){
                    messageType = (messageType == MESSAGE_TYPE.JOKE) ? MESSAGE_TYPE.PROVERB : MESSAGE_TYPE.JOKE;
                    System.out.printf("MODE SET TO %s\n", ((messageType == MESSAGE_TYPE.JOKE) ? "JOKE" : "PROVERB"));
                 }
                else if (domain.equals("quit")){
                    System.out.println("Client ending session");
                    out.print("Ending session\n");
                }
                else {
                    // Look up the address, print
                    printJokeOrProverb(out, Integer.parseInt(domain));
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

    static void printJokeOrProverb(PrintStream out, int clientID){

            // Alert client that you are looking up the domain
            out.println("Request to print out joke/proverb received");

            // Look up or create client state entry
            ClientState cState;

            if (clientState.containsKey(clientID)){
                cState = clientState.get(clientID);
            }
            else{
                cState = new ClientState();
                cState.clientID = clientID;
                clientState.put(clientID, cState);
            }

            if (messageType == MESSAGE_TYPE.JOKE){

                if (cState.jokeIndex == cState.jokeOrder.length){
                    out.print("JOKE CYCLE COMPLETED\n");
                    cState.jokeIndex = 0;
                }

                int joke = cState.jokeOrder[cState.jokeIndex++];
                out.println(jokes[joke]);
            }
            else{

                if (cState.proverbIndex == cState.proverbOrder.length){
                    out.print("PROVERB CYCLE COMPLETED\n");
                    cState.proverbIndex = 0;
                }

                int proverb = cState.proverbOrder[cState.proverbIndex++];
                out.println(proverbs[proverb]);
            }
            
    }

}

public class JokeServer{

    public static void main(String[] args) throws IOException {

        int q_len = 6; // max number of concurrent connections
        int port = 45678; // "Random" port number - can be any that's valid / not already used
        Socket sock; // Client socket object, to be assigned as they come in

        // Server socket
        ServerSocket servSock = new ServerSocket(port, q_len);

        System.out.println("Luke Robbins's Inet server 1.8 starting up, listening at port 45678.\n");

        // Loop "forever", accepting new connections as they come in
        while(true){
            sock = servSock.accept();  // Wait for next client connection, accept when it comes
            new Worker(sock).start();  // Once we get a connection, pass it to worker
        }

        // Our loop runs "forever", but realistically it won't, and we should close that socket
        //servSock.close();
    }
}