package controllers;

/**
 * Created by Jiaxiang on 22/1/16.
 */
public class RobotController {

    private static RobotController instance = new RobotController();

    private RobotController() {
    }

    public static RobotController getInstance(){
        if(instance==null)
            instance = new RobotController();
        return instance;
    }

    public int calibrate(){
        return 0;
    }
}
