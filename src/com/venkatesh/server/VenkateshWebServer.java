package com.venkatesh.server;

import com.venkatesh.server.util.ServerConstants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

import static com.venkatesh.server.util.ServerConstants.*;

/**
 * source : https://medium.com/@ssaurel/create-a-simple-http-web-server-in-java-3fc12b29d5fd
 * A simple WebServer to understand the nunances of how web server works
 */
public class VenkateshWebServer implements Runnable {

private Socket connect;

    public VenkateshWebServer(Socket connect) {
      this.connect = connect;
    }


   public static void main(String[] args){


       try {

           //create a socket with the dedicated port number
           // A socket is the end point for communication between two machines
           ServerSocket serverSocket = new ServerSocket(ServerConstants.PORT);
           System.out.println("Server Started Listening on PORT : " + ServerConstants.PORT);

           /**
            * Continuously listen to Network requests with the created socket above
            * accept() method kicks in for the socket only after the connection is made.
            */
           while (true){
               VenkateshWebServer venkateshWebServer = new VenkateshWebServer(serverSocket.accept());
               if(ServerConstants.ENABLE_LOGGING){
                System.out.println("Connection Opened on The Socket : Date :  (" + new Date()+ ")");
               }

               //dedicated thread to handle the request once the connection is established from the client
               Thread thread = new Thread(venkateshWebServer);
               thread.start();
           }


       } catch (IOException e) {
           System.err.println("Server Connection Failed " + e.getMessage());
           e.printStackTrace();
       }
   }

    @Override
    public void run() {

       //once the connection is established with the socket handle the request

        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;

        try {
            //read the input from client with help of inputStream from socket
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            out = new PrintWriter(connect.getOutputStream());
            dataOut = new BufferedOutputStream(connect.getOutputStream());

            String input = in.readLine();

            StringTokenizer stringTokenizer = new StringTokenizer(input);
            String httpMethod = stringTokenizer.nextToken().toUpperCase();
            fileRequested = stringTokenizer.nextToken().toLowerCase();
            // we support only GET and HEAD methods, we check
            if (!httpMethod.equals("GET")  &&  !httpMethod.equals("HEAD")) {
                if (ServerConstants.ENABLE_LOGGING) {
                    System.out.println("501 Not Implemented : " + httpMethod + " method.");
                }

                // we return the not supported file to the client
                File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
                int fileLength = (int) file.length();
                String contentMimeType = "text/html";
                //read content to return to client
                byte[] fileData = readFileData(file, fileLength);

                // we send HTTP Headers with data to client
                out.println("HTTP/1.1 501 Not Implemented");
                out.println("Server: Java HTTP Server from SSaurel : 1.0");
                out.println("Date: " + new Date());
                out.println("Content-type: " + contentMimeType);
                out.println("Content-length: " + fileLength);
                out.println(); // blank line between headers and content, very important !
                out.flush(); // flush character output stream buffer
                // file
                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();

            } else {
                // GET or HEAD method
                if (fileRequested.endsWith("/")) {
                    fileRequested += DEFAULT_FILE;
                }

                File file = new File(WEB_ROOT, fileRequested);
                int fileLength = (int) file.length();
                String content = getContentType(fileRequested);

                if (httpMethod.equals("GET")) { // GET method so we return content
                    byte[] fileData = readFileData(file, fileLength);

                    // send HTTP Headers
                    out.println("HTTP/1.1 200 OK");
                    out.println("Server: Java HTTP Server from SSaurel : 1.0");
                    out.println("Date: " + new Date());
                    out.println("Content-type: " + content);
                    out.println("Content-length: " + fileLength);
                    out.println(); // blank line between headers and content, very important !
                    out.flush(); // flush character output stream buffer

                    dataOut.write(fileData, 0, fileLength);
                    dataOut.flush();
                }

                if (ENABLE_LOGGING) {
                    System.out.println("File " + fileRequested + " of type " + content + " returned");
                }

            }

        } catch (FileNotFoundException fnfe) {
            try {
                fileNotFound(out, dataOut, fileRequested);
            } catch (IOException ioe) {
                System.err.println("Error with file not found exception : " + ioe.getMessage());
            }

        } catch (IOException ioe) {
            System.err.println("Server error : " + ioe);
        } finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                connect.close(); // we close socket connection
            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }

            if (ENABLE_LOGGING) {
                System.out.println("Connection closed.\n");
            }
        }


    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return fileData;
    }

    // return supported MIME Types
    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html"))
            return "text/html";
        else
            return "text/plain";
    }

    private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 404 File Not Found");
        out.println("Server: Java HTTP Server from SSaurel : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println(); // blank line between headers and content, very important !
        out.flush(); // flush character output stream buffer

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

        if (ENABLE_LOGGING) {
            System.out.println("File " + fileRequested + " not found");
        }
    }
}
