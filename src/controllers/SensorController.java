package controllers;

import models.Sensor;

/**
 * Created by Jiaxiang on 22/1/16.
 */
public class SensorController {

    private static SensorController instance = new SensorController();

    private SensorController() {
    }

    public static SensorController getInstance(){
        if(instance==null)
            instance = new SensorController();
        return instance;
    }
}
