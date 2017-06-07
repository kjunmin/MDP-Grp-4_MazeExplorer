/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import com.sun.org.apache.xpath.internal.operations.Or;
import controllers.Controller;
import utilities.GlobalUtilities;
import utilities.Orientation;

/**
 *
 * @author matth
 */
public class Robot {
    public static final int SIZE = 3;
    public static final int HALF_SIZE = SIZE/2;
    private int[] location;
    private int orientation;
    private double fullOrientation;
    
    
    
    public double getFullOrientation() {
        return fullOrientation;
    }
    
    public void setFullOrientation(double fullOrientation) {
        this.fullOrientation = fullOrientation;
    }

    //update the full orientation based on integer orientation
    public void updateFullOrientation(){
        setFullOrientation(Orientation.directionToDegree(getOrientation()));
    }
    
    private final int NUMBER_OF_SENSORS = 5;
    //The format of sensorString format: "relativeLocation;relativeOrientation;maxRange;minRange;index", example "topLeft;0;5;1;0"
    private final String[] SENSOR_STRINGS = {"topLeft;0;2;1", "topCenter;0;2;1", "topRight;0;2;1", "topLeft;3;2;1", "topRight;1;2;1"};
    
    private Arena perceivedArena;
    private boolean[][] explored;
    private static Robot instance = new Robot();
    
    private Sensor[] sensors;
//    ********
    //temporary attribute
    //robot size  assume robot size = 3

    private Robot(){}
    
    public static Robot getInstance(){
        if(instance==null)
            instance = new Robot();
        return instance;
    }
    
    public int[][] getRobotBlocks(){
        int[][] blocks = new int[9][2];
        int n = 0;
        for(int i=-(SIZE/2); i<=SIZE/2; i++)
            for(int j=-(SIZE/2); j<=SIZE/2; j++){
                blocks[n++] = new int[]{this.getLocation()[0]+i, this.getLocation()[1]+j};
            }
        return blocks;
    }
    
    public void initialize(int[] location, int orientation){
        int[] start = {Arena.ROW-2,1};
        int[] goal = {1,Arena.COL-2};
        perceivedArena = new Arena(start, goal);
        perceivedArena.resetToCertainState(Arena.mazeState.unknown);
        Arena.mazeState[][] maze = getPerceivedArena().getMaze();

        this.location = new int[]{location[0], location[1]};
        this.orientation = orientation;

        this.explored = new boolean[maze.length][maze[0].length];

        this.sensors = new Sensor[NUMBER_OF_SENSORS];
        for(int sensorNumber=0; sensorNumber<NUMBER_OF_SENSORS; sensorNumber++){       //converting the sensor strings to sensor objects
            sensors[sensorNumber] = new Sensor(SENSOR_STRINGS[sensorNumber]);
        }

        for(int i=0;i<explored.length;i++)
            for(int j=0;j<explored[i].length;j++)
                explored[i][j] = false;

    }
    
    public Sensor[] getSensors(){
        return sensors;
    }

    public int[] getLocation(){
        return location;
    }

    public void printStatus(){
        System.out.print("Robot location: Row: " + getLocation()[0] + ", Col: " + getLocation()[1]+ ". ");
        System.out.println("Orientation: " + getOrientation());
    }

    public int getOrientation(){return orientation;}

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
    public void setLocation(int[] location) {
        this.location = location;
    }


    public int moveForward(int steps){
        try{
            Controller.getInstance().savePrevious();
            setLocation(GlobalUtilities.locationParser(getLocation(), orientation, steps));
        }catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            System.out.println("Error inside Robot class, method moveForward");
        }
        return 0;
    }

    public int turn(int direction){
        setOrientation(Orientation.turn(getOrientation(), direction));
        return 0;
    }

    public Arena getPerceivedArena() {
        return perceivedArena;
    }
}
