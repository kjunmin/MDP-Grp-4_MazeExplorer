package models;
import algorithm.PathNode;
import algorithm.VirtualMap;
import com.sun.org.apache.bcel.internal.generic.GOTO;
import utilities.GlobalUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
/**
 *
 * @author matth
 */
public class Path {
    private ArrayList<PathNode> pathNodes = new ArrayList<>();
    
    private double totalCost = 0;

    public Path(VirtualMap virtualMap, int[] goalIndex){
        PathNode pathNode = virtualMap.getPathNode(goalIndex);
        totalCost = pathNode.pathCost;
        pathNodes.add(pathNode);
        while(pathNode.previousNode != null){
            pathNode = virtualMap.getPathNode(pathNode.previousNode);
            pathNodes.add(pathNode);
        }
        Collections.reverse(pathNodes);
    }
    
    public Path(){};

    public void print(){
        System.out.println("The path is: ");
        for(PathNode node : pathNodes){
            System.out.println("Index: " + node.index[0] + ", " + node.index[1] + "; Orientation: " + node.orientation);
        }
    }
    
    public double getTotalCost(){
        return totalCost;
    }
    
    public int[] getGoalIndex(){
        return pathNodes.get(pathNodes.size()-1).index;
    }

    public ArrayList<PathNode> getPathNodes(){
        return pathNodes;
    }
}
