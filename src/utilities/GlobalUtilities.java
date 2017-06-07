package utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by matthew on 22/1/16.
 */
public class GlobalUtilities {


    public static int[] locationParser(int[] origin, int orientation, int steps){
        int[] originCopy = origin.clone();
        switch(orientation){
            case Orientation.NORTH:
                originCopy[0] -= steps;
                break;
            case Orientation.SOUTH:
                originCopy[0] += steps;
                break;
            case Orientation.EAST:
                originCopy[1] += steps;
                break;
            case Orientation.WEST:
                originCopy[1] -= steps;
                break;
            default:
                System.out.println("In class GlobalUtilities, case does not exist. The current orientation is: " + orientation);
                originCopy[0] = -1;
                originCopy[1] = -1;
                break;
        }
        return originCopy;
    }

    public static HashMap<String, int[] > relativeLocation = new HashMap<String, int[]>(){{
        put("topLeft", new int[]{-1, -1});
        put("topCenter", new int[]{-1, 0});
        put("topRight", new int[]{-1, 1});
        put("middleLeft", new int[]{0, -1});
        put("middleCenter", new int[]{0, 0});
        put("middleRight", new int[]{0, 1});
        put("bottomLeft", new int[]{1, -1});
        put("bottomCenter", new int[]{1, 0});
        put("bottomRight", new int[]{1, 1});
    }};

    public static boolean sameLocation(int[] location1, int[] location2){
        return location1[0]==location2[0] && location1[1]==location2[1];
    }

    public static double relativeDistance(int[] from, int[] to){
        //return in milimeter
        return 100 * Math.sqrt((from[0] - to[0])* (from[0] - to[0]) + (from[1] - to[1])* (from[1] - to[1]));
    }

}
