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

All acceptable commands are displayed on the various consoles, but to be explict - when prompted:
> enter your name, as a single word without spaces
> to receive the next joke/proverb, enter "next"
> to switch servers, enter "s"
> enter "quit" to end the program

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

class ClientHelper{

    int clientID;
    Socket socket;

    public ClientHelper(){
        Random r = new Random();
        int rand = r.nextInt(1000000000);
        this.clientID = rand; // assign random number to client as ID
    }

    void connectToServer(String serverName, int serverPort){
        // Connect to Joke server at given server name & port
        // Port is hardcoded here at 4545
        // serverName is defauled to localhost, so in this case 127.0.0.1:4545
        try{
            this.socket = new Socket(serverName, serverPort);
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

    void sendName(String name){

        PrintStream toServer;

        try{
            toServer = new PrintStream(this.socket.getOutputStream());

            String data = "NAME:" + name + " " + clientID;
            toServer.println(data);
            toServer.flush();

        }catch(IOException e){
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
             toServer.println(Integer.toString(this.clientID));

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

class JokeClient{
    
    public static void main(String[] args){

        // Client object
        ClientHelper jokeClient = new ClientHelper();
        String clientName;

        // Set servername to name given as command line arg; if none given then default to "localhost", i.e. 127.0.0.1
        int serverPorts[] = {4545, 4546};
        String serverNames[] = {null, null};
        int currServer = 0;

        serverNames[0] = (args.length >= 1) ? args[0] : "localhost";
        serverNames[1] = (args.length >= 2) ? args[1] : null;

        System.out.println("Luke Robbins's Joke Client, 1.8\n");
        System.out.printf("Server one: " + serverNames[0] + ", Port: %d\n", serverPorts[0]);
        if (serverNames[1] != null){
            System.out.printf("Server two: " + serverNames[1] + ", Port: %d\n", serverPorts[1]);
        }
        
        // Read from stdin to read input
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        // Get name
        try{
            System.out.println("Please enter your name (single word, no spaces): ");
            clientName = in.readLine();
            jokeClient.connectToServer(serverNames[0], serverPorts[0]);
            jokeClient.sendName(clientName);
            jokeClient.closeSocket();

            if (serverNames[1] != null){
                jokeClient.connectToServer(serverNames[1], serverPorts[1]);
                jokeClient.sendName(clientName);
                jokeClient.closeSocket();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        

        // Enter request loop
        try{
            String request;
            do{
                System.out.print("Enter 'next' to receive the next joke or proverb, (quit to end): ");
                System.out.flush();

                // Get the domain to look up
                request = in.readLine();
                
                if (request.equals("next")){
                    jokeClient.connectToServer(serverNames[currServer], serverPorts[currServer]);
                    jokeClient.requestJokeOrProverb();
                    jokeClient.closeSocket();
                }
                else if (request.equals("s")){

                    if (serverNames[1] == null){
                        System.out.println("No secondary server available");
                    }
                    else{
                        currServer = (currServer == 0) ? 1 : 0;
                        System.out.printf("Now communicating with: " + serverNames[currServer] + ", Port: %d\n", serverPorts[currServer]);
                    }
                }
                    
            } while (!request.equals("quit"));

            System.out.println("Cancelled by user request");
        } catch(IOException e){
            e.printStackTrace();
        }

    }

}

