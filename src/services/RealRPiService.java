package services;
import com.sun.org.apache.xpath.internal.operations.Or;
import controllers.Controller;
import models.Robot;
import utilities.Messages;
import utilities.Orientation;
/**
 *
 * @author matth
 */
public class RealRPiService implements RPiServiceInterface{
    private static RealRPiService instance;

    private final int TIME_TO_RESEND = 10;

    private SerialService serial = SerialService.getInstance();

    private Robot robot = Robot.getInstance();

    public static RealRPiService getInstance(){
        if(instance==null)
            instance= new RealRPiService();
        return instance;
    }
    
    private RealRPiService(){}

    @Override
    public int moveForward(int steps) {
        serial.sendMessage(Messages.ARDUINO_CODE + Messages.moveRobotForward(steps));  //send to Arduino

        String returnMessage = serial.readMessage();
        System.out.println("The return message is supposed to be **" + Messages.robotMovedForward(steps) + "**");
        while(!returnMessage.equals(Messages.robotMovedForward(steps))) {     //if the return message matches
            try{
                    Thread.sleep(TIME_TO_RESEND);
            }catch (InterruptedException ite){
                    ite.printStackTrace();
            }
            serial.sendMessage(Messages.ARDUINO_CODE + Messages.moveRobotForward(steps));  //send to Arduino
            returnMessage = serial.readMessage();
        }

        serial.sendMessage(Messages.ANDROID_CODE + Messages.moveRobotForward(steps));    //send to Android

        robot.moveForward(steps);
        notifyUIChange();
        robot.printStatus();
        System.out.println("The move forward action is successful");
        return 0;
    }
    
    @Override
    public int turn(int direction) {

        if(direction == Orientation.FRONT)
            return -1;

        //Testing hard code*****************
        if(direction == Orientation.BACK){
            turn(Orientation.RIGHT);
            turn(Orientation.RIGHT);
            return 0;
        }
        //Testing hard code**********

        serial.sendMessage(Messages.ARDUINO_CODE + Messages.turnRobot(direction));

        String returnMessage = serial.readMessage();
        System.out.println("The return message is supposed to be **" + Messages.robotTurned(direction) + "**");
        while(!returnMessage.equals(Messages.robotTurned(direction))) {     //if the return message matches
            try{
                    Thread.sleep(TIME_TO_RESEND);
                }catch (InterruptedException ite){
                    ite.printStackTrace();
                }
                serial.sendMessage(Messages.ARDUINO_CODE + Messages.turnRobot(direction));
                returnMessage = serial.readMessage();
        }

        serial.sendMessage(Messages.ANDROID_CODE + Messages.turnRobot(direction));

        robot.turn(direction);
        notifyUIChange();
        robot.printStatus();
        System.out.println("The turning action is successful");
        return 0;
    }
    
    @Override
    public int callibrate() {

        serial.sendMessage(Messages.ARDUINO_CODE + Messages.callibrate());

        System.out.println("Robot calibrating...");
        robot.printStatus();

        String returnMessage = serial.readMessage();
        while(!returnMessage.equals(Messages.callibrated())) {     //if the return message matches
            try{
                    Thread.sleep(TIME_TO_RESEND);
                }catch (InterruptedException ite){
                    ite.printStackTrace();
                }
                serial.sendMessage(Messages.ARDUINO_CODE + Messages.callibrate());
                returnMessage = serial.readMessage();
        }
        serial.sendMessage(Messages.ANDROID_CODE + Messages.callibrate());

        System.out.println("Robot calibrated!!");
        return 0;
    }
    
    @Override
    public void notifyUIChange() {
        Controller.getInstance().setUpdate(true);
    }
}

