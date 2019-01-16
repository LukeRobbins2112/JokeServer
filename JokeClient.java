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

public class JokeClient{

    public static void main(String[] args){

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
                
                if (request != "quit"){
                    requestJokeOrProverb(serverName);
                }
                    
            } while (request != "quit");

            System.out.println("Cancelled by user request");
        } catch(IOException e){
            e.printStackTrace();
        }
    }


    static void requestJokeOrProverb(String serverName){
        
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        String textFromServer;

        try{

             // Connect to Joke server at given server name & port
             // Port is hardcoded here at 45678
             // serverName is defauled to localhost, so in this case 127.0.0.1:45678
             sock = new Socket(serverName, 45678);

             // I/O streams for reading/writing to the server socket
             fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
             toServer = new PrintStream(sock.getOutputStream());

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

             // Close the socket when finished
             sock.close();
        }catch(IOException e){
            System.out.println("Socket error");
            e.printStackTrace();
        }
    }
}

