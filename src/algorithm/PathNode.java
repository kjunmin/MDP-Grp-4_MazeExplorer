package algorithm;
import models.Arena;
import utilities.HeapPriorityQueue;
import utilities.Orientation;
import utilities.Updatable;
/**
 *
 * @author matth
 */
public class PathNode implements Comparable<PathNode>, Updatable{
    public int[] index;
    public Arena.mazeState state;
    public double pathCost;
    public int heuristics;
    public int[] previousNode;
    public boolean expanded = false;
    public int orientation;
    public boolean pathCostUpdated = false;

    public PathNode(){
        pathCost = Integer.MAX_VALUE;
    }
    
    public double getTotalCost(){
        return pathCost+heuristics;
    }

    //returns the indices of surrounding nodes
    public int[][] getSurrondingNodeIndices() throws ArrayIndexOutOfBoundsException{
        int[][] surroundingNodes = new int[9][2];

        int n = 0;
        for(int i=-1; i<=1; i++)
            for(int j=-1; j<=1; j++){
                surroundingNodes[n] = new int[]{index[0]+i, index[1]+j};
                n++;
            }

        return surroundingNodes;
    }

    //returns the indices of reachable nodes by a robot
    public int[][] getReachableNodeIndices() throws ArrayIndexOutOfBoundsException{
        int[][] reachableNodes = new int[4][2];

        reachableNodes[0] = new int[]{index[0]-1, index[1]};
        reachableNodes[1] = new int[]{index[0], index[1]+1};
        reachableNodes[2] = new int[]{index[0]+1, index[1]};
        reachableNodes[3] = new int[]{index[0], index[1]-1};

        return reachableNodes;
    }

    @Override
    public int compareTo(PathNode pathNode) {
        if(this.getTotalCost() > pathNode.getTotalCost())
            return 1;
        else if(this.getTotalCost() < pathNode.getTotalCost())
            return -1;
        else
            return 0;
    }


    @Override
    public boolean needUpdate() {
        return pathCostUpdated;
    }

    @Override
    public String toString() {
        String str = "";
        switch (state) {
            case freeSpace:
                str += "f\t";
                break;
            case obstacle:
                str += "o\t";
                break;
            case unknown:
                str += "u\t";
                break;
            case virtualObstacle:
                str += "v\t";
                break;
            case path:
                str += "p\t";
                break;
            default:
                str += "0\t";
                break;

        }
        return str;
    }

    public boolean isNodeFree(){
        if(state == Arena.mazeState.freeSpace || state == Arena.mazeState.path)
            return true;
        return false;

    }

    public void setPathCost(double pathCost){
        this.pathCost = pathCost;
    }
}
