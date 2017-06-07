/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;
import com.sun.org.apache.xpath.internal.operations.Or;
import models.Robot;
/**
 *
 * @author matth
 */
public class Messages {
    public static final String ARDUINO_CODE = "h";
    public static final String ANDROID_CODE = "a";
    public static final String RESEND_CODE = "resend";
    
    public static String startExploration(){
        return "explore";
    }

    public static String startShortestPath(){
        return "shortestPath";
    }
 
    
    public static String moveRobotForward(int n){
        return "forward:"+n;
    }

    public static String robotMovedForward(int n){
        return "forward:"+n+"D";
    }
    
    public static String turnDegree(int centiDegree, int direction){
        final int smallDegreeeLimit = 20;
        if(direction == Orientation.LEFT) {
            return "left";
        }
        else if (direction == Orientation.RIGHT) {
            return "right";
        }
        else{
            System.out.println("Incorrect turn command");
            return null;
        }
    }
    
    public static String robotTurnedDegree(int centiDegree, int direction){
        final int smallDegreeeLimit = 20;
        if(direction == Orientation.LEFT) {
            return "leftD";
        }
        else if (direction == Orientation.RIGHT) {
//            if (centiDegree < smallDegreeeLimit)
//                return "d" + centiDegree + "done";
            return "rightD";
        }
        else{
            System.out.println("Incorrect acknowledgement");
            return null;
        }
    }
    
    public static String obstacleInfo(){
        String message = Robot.getInstance().getPerceivedArena().toObstacleInfo();
        return message;
    }
    
    public static String mapDescriptor(){
        String message = Robot.getInstance().getPerceivedArena().toMapDescriptor();
        return message;
    }
    
    public static String turnRobot(int direction){
        if(direction==Orientation.LEFT)
            return "left";
        else if(direction==Orientation.RIGHT)
            return "right";
        else if(direction==Orientation.BACK)
            return "back";
        return null;
    }

    public static String robotTurned(int direction){
        if(direction==Orientation.LEFT)
            return "leftD";
        else if(direction==Orientation.RIGHT)
            return "rightD";
        else if(direction==Orientation.BACK)
            return "backD";
        return null;
    }
    
    public static String callibrate(){
        return "callibrate";
    }

    public static String callibrated(){
        return "callibrateD";
    }
    
    public static String detectObstacles(){
        return "detect";
    }
}
