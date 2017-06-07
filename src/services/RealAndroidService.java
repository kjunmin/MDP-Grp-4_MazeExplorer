/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import utilities.Messages;
import utilities.Orientation;

/**
 *
 * @author matth
 */
public class RealAndroidService implements AndroidServiceInterface {
    
    private static RealAndroidService instance;

    private final int TIME_TO_RESEND = 10;

    private SerialService serial = SerialService.getInstance();

    public static RealAndroidService getInstance(){
        if(instance==null)
            instance= new RealAndroidService();
        return instance;
    }
    
    private RealAndroidService(){};
    
    @Override
    public int waitToStartExploration() {
        System.out.println("Waiting for start exploration command from Android...");
        String message = serial.readMessage();
        while(!message.equals(Messages.startExploration())) {     //if the return message matches
            System.out.println("The command from Android to start exploration is not correct");
            message = serial.readMessage();
        }
        System.out.println("(Real Run)Received start exploring command from Android");
        System.out.println("Exploration starting...");
        return 0;
    }
    
    @Override
    public int sendObstacleInfo() {

        System.out.println("Sending map info...");
        serial.sendMessage(Messages.ANDROID_CODE + Messages.obstacleInfo());
        System.out.println("Map info sent!!");
        return 0;
    }
    
    @Override
    public int sendMapDescriptor() {
        System.out.println("Sending map descriptor...");
        serial.sendMessage(Messages.ANDROID_CODE + Messages.mapDescriptor());
        System.out.println("Map descriptor sent!!");
        return 0;
    }
    
    @Override
    public int waitToRunShortestPath() {
        System.out.println("Waiting for start shortest path command from Android...");
        String message = serial.readMessage();

        while(!message.equals(Messages.startShortestPath())) {     //if the return message matches
            System.out.println("The command from Android to start shortest path is not correct");
            message = serial.readMessage();
        }
        System.out.println("(Real Run)Received start shortest path command from Android");
        System.out.println("Shortest path run starting...");
        return 0;
    }
    
    
}
