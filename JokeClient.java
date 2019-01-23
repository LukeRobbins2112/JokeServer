/*--------------------------------------------------------
1. Luke Robbins / 1/15/2019

2. Java 1.8

3. Compilation Instructions

> javac JokeClient.java

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

5. Notes:

----------------------------------------------------------*/

import java.io.*;   // Import all IO libraries
import java.net.*;  // Import networking libraries
import java.util.Random;

public class JokeClient{

    int clientID;
    Socket socket;

    public JokeClient(){
        Random r = new Random();
        int rand = r.nextInt(1000000000);
        this.clientID = rand; // assign random number to client as ID
    }

    void connectToServer(String serverName){
        // Connect to Joke server at given server name & port
        // Port is hardcoded here at 45678
        // serverName is defauled to localhost, so in this case 127.0.0.1:45678
        try{
            this.socket = new Socket(serverName, 45678);
        } catch(IOException e){
            e.printStackTrace();
        }
        
    }

    void closeSocket(){
        try{
            this.socket.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
    void requestJokeOrProverb(){
        
        BufferedReader fromServer;
        PrintStream toServer;
        String textFromServer;

        try{

             // I/O streams for reading/writing to the server socket
             fromServer = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
             toServer = new PrintStream(this.socket.getOutputStream());

             // Send the domain name our request
             toServer.println("next");

             // Flush so that each request doesn't include anything left over in the buffer
             toServer.flush();

             // Read response from server (maybe not a great idea to hardcode the # of lines)
             while((textFromServer = fromServer.readLine()) != null){
                 // Read a single line
                 textFromServer = fromServer.readLine();

                 // If a line is not empty, print it to the client
                 if (textFromServer != null) // Unnecessary now that the null check is in the loop condition
                    System.out.println(textFromServer);
             }

        }catch(IOException e){
            System.out.println("Socket error");
            e.printStackTrace();
        }
    }
}

class ClientMain{
    
    public static void main(String[] args){

        // Client object
        JokeClient jokeClient = new JokeClient();

        // To be assigned - either user supplied or defaulted to "localhost"
        String serverName;

        // Set servername to name given as command line arg; if none given then default to "localhost", i.e. 127.0.0.1
        if (args.length < 1) serverName = "localhost";
        else serverName = args[0];

        System.out.println("Luke Robbins's Joke Client, 1.8\n");
        System.out.println("Using server: " + serverName + ", Port: 45678");  // Port is hard set
        
        // Read from stdin to read input
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        try{
            String request;
            do{
                System.out.print("Enter 'next' to receive the next joke or proverb, (quit to end): ");
                System.out.flush();

                // Get the domain to look up
                request = in.readLine();
                
                if (!request.equals("quit")){
                    jokeClient.connectToServer(serverName);
                    jokeClient.requestJokeOrProverb();
                    jokeClient.closeSocket();
                }
                    
            } while (!request.equals("quit"));

            System.out.println("Cancelled by user request");
        } catch(IOException e){
            e.printStackTrace();
        }

    }

}

