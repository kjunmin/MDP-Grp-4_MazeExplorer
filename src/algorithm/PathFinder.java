package algorithm;
import controllers.Controller;
import models.Arena;
import models.Path;
import models.Robot;
import utilities.GlobalUtilities;
import utilities.HeapPriorityQueue;
import utilities.Orientation;
/**
 *
 * @author matth
 */
public class PathFinder {
    private static final double TURN_COST = 0.8;
    private static PathFinder instance = new PathFinder();
    private final Controller controller = Controller.getInstance();
    private final Robot robot = Robot.getInstance();
    private PathFinder(){}

    public static final double COST_TO_MOVE_ONE_STEP = 1;
    public static final double COST_TO_MAKE_A_TURN = 1;
    
    public static PathFinder getInstance(){
        if(instance==null)
            instance = new PathFinder();
        return instance;
    }



    public Path aStarStraight(Arena.mazeState[][] maze, int[] start, int[] goal, boolean treatUnknownAsObstacle, int startOrientation){

        VirtualMap virtualMap = new VirtualMap(maze, treatUnknownAsObstacle);

        //initializing heuristics
        for(int i=0; i<Arena.ROW; i++) {
            for (int j = 0; j < Arena.COL; j++) {
                virtualMap.getVirtualMap()[i][j].heuristics = calculateHeuristic(i, j, goal);
            }
        }

        PathNode startNode = virtualMap.getPathNode(start);

        startNode.orientation = startOrientation;
        startNode.pathCost = 0;
        startNode.previousNode = null;

        HeapPriorityQueue<PathNode> queue = new HeapPriorityQueue<PathNode>();

        expand(startNode, virtualMap, queue);

        PathNode expandingNode = startNode;

        while(queue.size()>0){    //repeatedly polling from the queue and expand
            expandingNode = queue.poll();

            //*********debugging code
            //virtualMap.printExpanded();

            expand(expandingNode, virtualMap, queue);

            if(GlobalUtilities.sameLocation(expandingNode.index, goal)) {
                System.out.println("Reached goal");
                break;
            }
        }

        if(!GlobalUtilities.sameLocation(expandingNode.index, goal))
            return null;

        Path path = new Path(virtualMap, goal);

        virtualMap.printShortestPath(path);

        return path;
    }


    //expand a node and mark its reachables nodes
    public void expand(PathNode thisNode, VirtualMap virtualMap, HeapPriorityQueue<PathNode> queue){
        thisNode.expanded = true;
//        System.out.println("****Expanded index: " + thisNode.index[0] + ", " + thisNode.index[1]);
        for(int[] reachableNodeIndex : thisNode.getReachableNodeIndices()){
            //trying to mark reachable nodes and ignore those that are out of index bound
            try{
                mark(virtualMap.getPathNode(reachableNodeIndex), thisNode, queue);
            }catch (ArrayIndexOutOfBoundsException e){}
        }

        queue.update();
    }

    private void mark(PathNode thisNode, PathNode previousNode, HeapPriorityQueue<PathNode> queue){
        if(thisNode.state == Arena.mazeState.freeSpace || thisNode.state == Arena.mazeState.path) {
            int orientation = Orientation.relativeOrientation(thisNode.index, previousNode.index);
            double stepCost = PathFinder.COST_TO_MOVE_ONE_STEP;

            //if previous orientation is the same as relative orientation, then no need to turn
            if(orientation == previousNode.orientation){}

            //if orienation is opposite, turn twice
            else if(orientation == Orientation.oppositeOrientation(previousNode.orientation))
                stepCost += 2 * PathFinder.COST_TO_MAKE_A_TURN;

            //otherwise, turn once
            else
                stepCost += PathFinder.COST_TO_MAKE_A_TURN;

            if (previousNode.pathCost + stepCost < thisNode.pathCost) {
                if(thisNode.pathCost == Integer.MAX_VALUE){
                    queue.offer(thisNode);
                }
                thisNode.pathCostUpdated = true;
                thisNode.pathCost = previousNode.pathCost + stepCost;
                thisNode.previousNode = previousNode.index;
                thisNode.orientation = orientation;
            }
        }
    }


    //this function was used to calculated a non-optimal diagonal path
    public Path diagonalPath(Arena.mazeState[][] maze, boolean treatUnknownAsObstacle, Path path){
        VirtualMap virtualMap = new VirtualMap(maze, treatUnknownAsObstacle);
        Path diagonalPath = new Path();
        PathNode node = path.getPathNodes().get(0);
        diagonalPath.getPathNodes().add(node);

        //code to test reachable() method
//        System.out.println(reachable(virtualMap.getPathNode(new int[]{1,1}), virtualMap.getPathNode(new int[]{9,7}), virtualMap));

        int n = 0;
        while(n < path.getPathNodes().size()){
            int i = n + 1;
            while(i < path.getPathNodes().size()){
                if(reachable(node, path.getPathNodes().get(i), virtualMap))
                    n = i;
                i++;
            }
            node = path.getPathNodes().get(n);
            diagonalPath.getPathNodes().add(node);
            if(n == path.getPathNodes().size() - 1)
                break;
        }

        return diagonalPath;
    }

    public Path getDiagonalPath(int[] goalIndex, Path straightShortestPath, VirtualMap virtualMap){
        makePathInVirtualMap(straightShortestPath, virtualMap);
        prepareDiagonalPath(straightShortestPath,virtualMap,0);
        virtualMap.getPathNode(goalIndex).setPathCost(straightShortestPath.getPathNodes().get(straightShortestPath.getPathNodes().size()-1).pathCost);
        Path diagonalPath = new Path(virtualMap,goalIndex);
        return diagonalPath;
    }

    private void prepareDiagonalPath(Path path,VirtualMap virtualMap,int current_index){
        if(current_index==path.getPathNodes().size()-1) {
            return;
        }
        PathNode currentNode = path.getPathNodes().get(current_index);  //node on path
        //currentNode = virtualMap.getVirtualMap()[currentNode.index[0]][currentNode.index[1]]; //node on virtual map
        PathNode targetNode;
        path.getPathNodes().get(0).pathCost = -PathFinder.TURN_COST;  // remove the impact of first turning action
        for(int i=current_index+1;i<path.getPathNodes().size();i++){
            targetNode = path.getPathNodes().get(i);   //node on path
            //targetNode = virtualMap.getVirtualMap()[targetNode.index[0]][targetNode.index[1]]; //node on virtual map
            if(reachable(currentNode,targetNode,virtualMap)){
                double offerCost = currentNode.pathCost + GlobalUtilities.relativeDistance(targetNode.index,currentNode.index)/100 + PathFinder.TURN_COST;
                //System.out.println("offer cost = "+offerCost + ", current cost = " + targetNode.pathCost);
                if(offerCost < targetNode.pathCost + 0.00001){
                    virtualMap.getVirtualMap()[targetNode.index[0]][targetNode.index[1]].previousNode = currentNode.index;//do update to virtual map
                    targetNode.setPathCost(offerCost);
                }
            }
        }
        prepareDiagonalPath(path,virtualMap,current_index+1);//start from next node
    }

    private void makePathInVirtualMap(Path path, VirtualMap virtualMap){
        for(PathNode node : path.getPathNodes()){
            virtualMap.getPathNode(node.index).previousNode = node.previousNode;
        }
    }



    private boolean reachable(PathNode fromNode, PathNode toNode, VirtualMap virtualMap){
        double fX = fromNode.index[0] + 0.5;
        double fY = fromNode.index[1] + 0.5;
        double tX = toNode.index[0] + 0.5;
        double tY = toNode.index[1] + 0.5;

        final int sectionNumber = 1000;
        final double errorMargin = 0.01;

        double xSection = (tX - fX)/ sectionNumber;
        double ySection = (tY - fY)/ sectionNumber;

        for(int i = 0; i < sectionNumber; i++){
            double x = fX + i * xSection;
            double y = fY + i * ySection;
            if(!virtualMap.getPathNode(new int[]{(int) x, (int) y}).isNodeFree()
                    || !virtualMap.getPathNode(new int[]{(int) (x-errorMargin), (int) y}).isNodeFree()
                    || !virtualMap.getPathNode(new int[]{(int) x, (int) (y-errorMargin)}).isNodeFree()
                    || !virtualMap.getPathNode(new int[]{(int) (x-errorMargin), (int) (y-errorMargin)}).isNodeFree()){
                return false;
            }
        }
        return true;

//        if(fromNode.index[0] == toNode.index[0] || fromNode.index[1] == toNode.index[1])
//            return true;
//        return false;
    }

//    public static void main(String[] args){
//        System.out.println("****" + (int) 4.000);
//    }


    private int calculateHeuristic(int row, int col, int[] goal){
        return Math.abs(goal[0] - row) + Math.abs(goal[1] - col);
    }
}
