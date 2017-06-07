
package services;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author matthew
 */
public class SerialService {
    public static final String RPI_IP_ADDRESS = "192.168.4.4";
    public static final int RPI_PORT = 12345;

    private static SerialService instance;
    private Socket clientSocket;
    private PrintWriter rpiWrite;
    private Scanner rpiRead;
    private final int DELAY_IN_SENDING_MESSAGE = 2;

    private final int TIME_TO_RETRY = 1000;       //1s to retry
    
    private SerialService() {}
    
    public static SerialService getInstance(){
        if (instance == null) {
            instance = new SerialService();
        }
        return instance;
    }
    
    public static void main (String[] args){

        SerialService serial = SerialService.getInstance();
        serial.connectToHost();
        AndroidServiceInterface androidService = RealAndroidService.getInstance();
        RPiServiceInterface rpiService = RealRPiService.getInstance();


        try{
            Thread.sleep(3000);
        }catch (InterruptedException ite){
            ite.printStackTrace();
        }

        try{
            Thread.sleep(1000000);
        }catch (InterruptedException ite){
            ite.printStackTrace();
        }

        serial.closeConnection();
    }

    public void connectToHost(){
        try {
            clientSocket = new Socket(RPI_IP_ADDRESS, RPI_PORT);
            rpiWrite = new PrintWriter(clientSocket.getOutputStream());
            rpiRead = new Scanner(clientSocket.getInputStream());
        }catch (IOException ioe){
            ioe.printStackTrace();
            try{
                Thread.sleep(TIME_TO_RETRY);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            connectToHost();
        }
        System.out.println("RPi successfully connected");
    }

    public void closeConnection(){
        try {
            if (!clientSocket.isClosed()){
                clientSocket.close();
            }
        }catch (IOException ioe){
            ioe.printStackTrace();
            try{
                Thread.sleep(TIME_TO_RETRY);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            closeConnection();
        }
        System.out.println("Connection closed");
    }

    public void sendMessage(String message){
        try {
            Thread.sleep(DELAY_IN_SENDING_MESSAGE);
            rpiWrite.print(message);
            rpiWrite.flush();
        }catch (InterruptedException ite){
            ite.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
            try{
                Thread.sleep(TIME_TO_RETRY);
            }catch (InterruptedException ite){
                ite.printStackTrace();
            }
            connectToHost();
            sendMessage(message);
        }

        System.out.println("Message sent: ****" + message + "****");
    }

    public String readMessage(){

        String messageReceived = "";
        try {
            messageReceived = rpiRead.nextLine();
            System.out.println("Message received: ****" + messageReceived + "****");

        }catch (Exception e){
            e.printStackTrace();
            try{
                Thread.sleep(TIME_TO_RETRY);
            }catch (InterruptedException ite){
                ite.printStackTrace();
            }
            connectToHost();
            readMessage();
        }

        return messageReceived;
    }

}
