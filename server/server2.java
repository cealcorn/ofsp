package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class server2 {
    private static final int MAX_CLIENT_MESSAGE_LENGTH = 2048;
    private static final int MAX_CLIENT_FILE_PAYLOAD_SIZE = 1048576; // allow a 1MB file (includes the file
                                                                     // metadata/path/name)
    private static ServerSocket serveSocket;
    private static Socket clientSocket;
    private static BufferedReader in;
    private static PrintWriter out;
    private static String directorySeperator;

    server2 thisServer;

    server2() {
        thisServer = this;
    }

    private static void print_usage() {
        System.out.println("USAGE: java ServerTCP.java listening_port");
    }

    private static int verify_arguments(String[] args) throws InvalidPortNumber, InvalidArgumentLength {
        if (args.length != 1) {
            throw new server2.InvalidArgumentLength("Invalid number of arguments");
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            System.out.println("The port number could not be parsed. please input a valid integer between 1 and 65535");
            throw new server2.InvalidPortNumber("Port not an integer");
        }

        if (port > 65535) {
            System.out.println("The port is above the range, please input a port between 1 and 65535");
            throw new server2.InvalidPortNumber("Port above range.");
        } else if (port < 1) {
            System.out.println("The port is below the range, please input a port between 1 and 65535");
            throw new server2.InvalidPortNumber("Port below range.");
        }
        return port;
    }

    public static void main(String[] args) {
        int port;
        try {
            port = verify_arguments(args);
        } catch (InvalidArgumentLength e) {
            System.out.println(e.getMessage());
            print_usage();
            return;
        } catch (InvalidPortNumber e) {
            System.out.println(e.getMessage());
            print_usage();
            return;
        }

        try {
            serveSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Unable to bind to port " + String.valueOf(port) + ". Error: " + e.getMessage());
            return;
        }

        //get the seperator here because if you dont have server conection, this is pointless (you also only need to do this once)
        getDirectorySeperator();

        while (true) { //keeps server connection open and accepts multiple clients
            try {
                clientSocket = serveSocket.accept();
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                System.out.println("Unable to create client connection");
                return;
            }

            // adds ability to switch to different parts of the code
            boolean recievedCommand = false;
            boolean recievedPayload = false;

            String currentInput;
            String humanReadableCommand;
            char command = 'z';
            String payload;
            Boolean isError = false;
            try {
                while ((currentInput = in.readLine()) != null) {
                    if (isError) {
                        break;
                    }
                    // verify that the command is valid
                    if (!recievedCommand) {
                        command = currentInput.charAt(0);
                        switch (command) {
                            case 'D':
                                humanReadableCommand = "Download";
                                break;
                            case 'U':
                                humanReadableCommand = "Upload";
                                break;
                            case 'L':
                                humanReadableCommand = "List";
                                break;
                            case 'R':
                                humanReadableCommand = "Rename";
                                break;
                            case 'M':
                                humanReadableCommand = "Move";
                                break;
                            case 'H':
                                humanReadableCommand = "Help";
                                break;
                            case 'T':
                                humanReadableCommand = "Delete";
                                break;
                            default:
                                humanReadableCommand = "Unknown";
                                break;
                        }
                        System.out.println("Command recieved from client: " + humanReadableCommand);
                        recievedCommand = true;
                        if (humanReadableCommand == "Unknown") {
                            recievedCommand = false;
                            throwError("Unknown Command. Please retry");
                        } else if (humanReadableCommand == "Help"){
                            throwError("No... that is for a therapist not me...");
                            break;
                        } else {
                            out.println("C");
                        }
                        continue; // go wait for the next line to come in
                    } else if (!recievedPayload) {
                        switch (command) {
                            case 'D':
                                payload = currentInput.substring(1);
                                File downloadFile;
                                try {
                                    downloadFile = checkFileAvailability(payload);
                                } catch (BadPermissionsException e) {
                                    System.out.println("Error from client download command: " + e.getMessage());
                                    break;
                                } catch (IncorrectFileNameException e) {
                                    System.out.println("Error from client download command: " + e.getMessage());
                                    break;
                                } catch (IsDirectoryException e) {
                                    System.out.println("Error from client download command: " + e.getMessage());
                                    break;
                                }
                                try {
                                    sendFile(downloadFile);
                                } catch (EmptyFileException e) {
                                    throwError("File is empty, nothing to send");
                                    System.out.println("File is Empty for [" + downloadFile.getName() + "].");
                                }
                                break;
                            case 'U':
                                payload = currentInput.substring(1);
                                int indexOfNull = payload.indexOf("\0");
                                String fileName = payload.substring(0,indexOfNull);
                                //adding one because indexes start at 0 and indexOf starts at 0 as well
                                String fileContents = payload.substring(indexOfNull+1);
                                try {
                                    checkFileAvailability(fileName, true);
                                } catch (BadPermissionsException e) {
                                    System.out.println("Error from client download command: " + e.getMessage());
                                    break;
                                } catch (IncorrectFileNameException e) {
                                } catch (IsDirectoryException e) {
                                    System.out.println("Error from client download command: " + e.getMessage());
                                    break;
                                }
                                createFile(fileName, unSanitizeInput(fileContents));
                                break;
                            case 'L':
                                payload = currentInput.substring(1);
                                File directory;
                                try {
                                    directory = checkFolderAvailability(payload);
                                } catch (IsFileException e) {
                                    System.out.println("Directory is shown as a file");
                                    break;
                                } catch (IncorrectDirectoryName e){
                                    System.out.println("Directory name [data/" + payload + "] was not found");
                                    break;
                                }
                                listDirectory(directory);
                                break;
                            case 'R':
                            case 'M':
                                payload = currentInput.substring(1);
                                int originalIndexOfNull = payload.indexOf("\0");
                                String originalFileString = payload.substring(0,originalIndexOfNull);
                                String newFile = payload.substring((originalIndexOfNull+1));
                                File originalFile;
                                try{
                                    originalFile = checkFileAvailability(originalFileString);
                                } catch (BadPermissionsException e) {
                                    System.out.println("Error from client rename/delete command: " + e.getMessage());
                                    break;
                                } catch (IncorrectFileNameException e) {
                                    System.out.println("Error from client rename/delete command: " + e.getMessage());
                                    break;
                                } catch (IsDirectoryException e) {
                                    System.out.println("Error from client rename/delete command: " + e.getMessage());
                                    break;
                                }

                                try {
                                    moveFile(originalFile, newFile, true);
                                } catch (BadPermissionsException e1) {
                                    System.out.println("Could not rename/delete folder because of bad permissions");
                                } catch (IsDirectoryException e1) {
                                    System.out.println("Could not rename/delete folder because the requested file was a directory");
                                }
                                break;
                            case 'H':
                                humanReadableCommand = "Help";
                                break;
                            case 'T':
                                payload = currentInput.substring(1);
                                File deleteFile;
                                try {
                                    deleteFile = checkFileAvailability(payload);
                                } catch (BadPermissionsException e) {
                                    System.out.println("Error from client delete command: " + e.getMessage());
                                    break;
                                } catch (IncorrectFileNameException e) {
                                    System.out.println("Error from client delete command: " + e.getMessage());
                                    break;
                                } catch (IsDirectoryException e) {
                                    System.out.println("Error from client delete command: " + e.getMessage());
                                    break;
                                }
                                try {
                                    deleteFile(deleteFile);
                                } catch (FileDeletionException e) {
                                    System.out.println("Error from client delete command while deleting: " + e.getMessage());
                                }
                                break;
                            default:
                                throwError(
                                        "You were not supposed to get to this point... contact the admin to fix this error along with how you got here");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Unable to read from input. Error: " + e);
            }

            try {
                in.close();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Unable to close client socket. Error: " + e.getMessage());
            }
        }
    }

    // sends the error message over the TCP socket
    private static void throwError(String error) {
        out.println("E" + error);
    }

    private static void sendFile(File file) throws EmptyFileException {
        if (file.length() != 0) {
            char[] charBuffer = new char[(int) file.length()];
            FileReader fileReader;
            try {
                fileReader = new FileReader(file.toString());
                fileReader.read(charBuffer);
                fileReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("How did you not find this File reader? I already checked to see if you exist...");
            } catch (IOException e){
                e.printStackTrace();
            }
            String fileString = "";
            for(int i=0;i<file.length();i++){
                fileString += charBuffer[i];
            }
            out.println("C" + fileString);
        } else {
            throw new server2.EmptyFileException("File is empty");
        }
    }

    // verifies that the file is a file and the server (aka user on the computer)
    // has suffiecient
    // permissions to read it
    private static File checkFileAvailability(String name)
            throws BadPermissionsException, IncorrectFileNameException, IsDirectoryException {
        File file = new File("data" + directorySeperator + correctDirectorySeperator(name));
        if (!file.exists()) {
            throwError("file does not exist: " + file.getPath());
            throw new server2.IncorrectFileNameException(IncorrectFileNameException.BadFileName, name);
        }
        if (file.isDirectory()) {
            throwError("file is a directory");
            throw new server2.IsDirectoryException("The file specified is a directory");
        }
        if (file.canRead()) {
            return file;
        } else {
            throwError("Server error occured, contact server admin if this keeps occuring; CODE: READ DENIED");
            throw new server2.BadPermissionsException(BadPermissionsException.BadRead);
        }
    }

    private static File checkFileAvailability(String name, boolean upload)
            throws BadPermissionsException, IncorrectFileNameException, IsDirectoryException {
        File file = new File("data" + directorySeperator + name);
        if (!file.exists()) {
            if(!upload){
                throwError("Invalid file name");
            }
            throw new server2.IncorrectFileNameException(IncorrectFileNameException.BadFileName, name);
        }
        if (file.isDirectory()) {
            throwError("file is a directory");
            throw new server2.IsDirectoryException("The file specified is a directory");
        }
        if (file.canRead()) {
            return file;
        } else {
            throwError("Server error occured, contact server admin if this keeps occuring; CODE: READ DENIED");
            throw new server2.BadPermissionsException(BadPermissionsException.BadRead);
        }
    }

    //this is here because the built in java method is broken :(
    private static void getDirectorySeperator(){
        String operatingSystem = System.getProperty("os.name");
        if(operatingSystem.contains("Windows")){
            directorySeperator = "\\";
        } else {
            directorySeperator = "/";
        }
    }

    private static String correctDirectorySeperator(String path){
        if(path.contains(directorySeperator)){
            return path;
        } else {
            if(directorySeperator == "/"){
                return path.replace("\\", "/");
            } else{
                return path.replace("/", "\\");
            }
        }
    }

    private static void createFile(String fileName, String content){
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter("data" + directorySeperator + fileName);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException e) {
            throwError("Unable to upload file");
            e.printStackTrace();
        }
        out.println("C");
    }

    private static File checkFolderAvailability(String path) throws IsFileException, IncorrectDirectoryName{
        File directory = new File("data" + directorySeperator + correctDirectorySeperator(path));
        if(directory.isFile()){
            throwError("Directory is currently a file");
            throw new server2.IsFileException("Directory is shown as a file");
        }
        if(directory.exists()){
            return directory;
        } else {
            throwError("unknown directory: " + path);
            throw new server2.IncorrectDirectoryName("Unknown Directory");
        }
    }

    private static void listDirectory(File directory){
        String[] listedDirectory = directory.list();
        String list = "";
        for(int i=0; i<listedDirectory.length; i++){
            list += sanitizeInput(listedDirectory[i]);
        }
        out.println("C" + list);
    }

    private static void deleteFile(File file) throws FileDeletionException{
        if(!file.delete()){
            throw new server2.FileDeletionException("Unable to delete file");
        }
    }

    private static void moveFile(File file, String destination, boolean rename) throws BadPermissionsException, IsDirectoryException{
        String command;
        if(rename){
            command = "rename";
        } else {
            command = "move";
        }
        try {
            checkFileAvailability(destination);
        } catch (BadPermissionsException e) {
            throwError("Unable to " + command + " file. Error: Permission Denied");
            throw e;
        } catch (IncorrectFileNameException e) {
            file.renameTo(new File("data" + directorySeperator + destination));
        } catch (IsDirectoryException e) {
            throwError("Unable to " + command + " file. ");
            throw e;
        }
    }

    //because new lines break the code, this is a sanitazation function th=o make sure that the code does not break
    private static String sanitizeInput(String unsanitizedString){
        return unsanitizedString.replaceAll("\n", "\0\\n");
    }

    private static String unSanitizeInput(String sanitizedInput){
        return sanitizedInput.replaceAll("\0\\n", "\n");
    }

    private static class BadPermissionsException extends Exception {
        public static final String BadRead = "File is unreadable";

        public BadPermissionsException(String errorMessage) {
            super(errorMessage);
        }
    }

    private static class IncorrectFileNameException extends Exception {
        public static final String BadFileName = "File not found: ";

        public IncorrectFileNameException(String errorMessage, String fileName) {
            super(errorMessage + fileName);
        }

        public IncorrectFileNameException(String errorMessage) {
            super(errorMessage);
        }
    }

    private static class IncorrectDirectoryName extends Exception{
        public IncorrectDirectoryName(String errorMessage){
            super(errorMessage);
        }
    }

    private static class IsDirectoryException extends Exception {
        public IsDirectoryException(String errorMessage) {
            super(errorMessage);
        }
    }

    private static class EmptyFileException extends Exception {
        public EmptyFileException(String errorMessage) {
            super(errorMessage);
        }
    }

    private static class InvalidPortNumber extends Exception {
        public InvalidPortNumber(String errorMessage) {
            super(errorMessage);
        }
    }

    private static class InvalidArgumentLength extends Exception {
        public InvalidArgumentLength(String errorMessage) {
            super(errorMessage);
        }
    }

    private static class IsFileException extends Exception{
        public IsFileException(String errorMessage){
            super(errorMessage);
        }
    }

    private static class FileDeletionException extends Exception {
        public FileDeletionException(String errorMessage){
            super(errorMessage);
        }
    }
}
