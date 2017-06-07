package models;
import utilities.GlobalUtilities;
import utilities.Orientation;

/**
 * Created by matthew on 22/1/16.
 */
public class Sensor {
    private int[] relativeLocation;
    private int relativeOrientation;
    private int maxRange;
    private int minRange;
    private int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    private final Robot robot = Robot.getInstance();

    public Sensor(int[] relativeLocation, int relativeOrientation, int maxRange, int minRange, int index) {
        this.relativeLocation = relativeLocation;
        this.relativeOrientation = relativeOrientation;
        this.maxRange = maxRange;
        this.minRange = minRange;
//        this.index = index;
    }

    //The format of sensorString format: "relativeLocation;relativeOrientation;maxRange;minRange", example "topLeft;0;5;1"
    public Sensor(String sensorString){
        String[] sensorStringSplits = sensorString.split(";");
        this.relativeLocation = GlobalUtilities.relativeLocation.get(sensorStringSplits[0]);
        this.relativeOrientation = Integer.parseInt(sensorStringSplits[1]);
        this.maxRange = Integer.parseInt(sensorStringSplits[2]);
        this.minRange = Integer.parseInt(sensorStringSplits[3]);
//        this.index = Integer.parseInt(sensorStringSplits[4]);

    }

    public int[] getAbsoluteLocation(){
        int[] absoluteLocation= new int[2];
        int[] rotatedRelativeLocation = Orientation.rotateCoordinates(getRelativeLocation(), robot.getOrientation());
        absoluteLocation[0] = robot.getLocation()[0] + rotatedRelativeLocation[0];
        absoluteLocation[1] = robot.getLocation()[1] + rotatedRelativeLocation[1];
        return absoluteLocation;
    }

    public void setRelativeLocation(int[] relativeLocation){
        this.relativeLocation = relativeLocation;
    }

    public int[] getRelativeLocation(){
        return relativeLocation;
    }

    public int getrelativeOrientation() {
        return relativeOrientation;
    }

    public int getAbsoluteOrientation() {
        return Orientation.turn(robot.getOrientation(), relativeOrientation);
    }

    public int getMinRange() {
        return minRange;
    }

    public int getMaxRange() {
        return maxRange;
    }
}
