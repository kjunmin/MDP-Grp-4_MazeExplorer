
package algorithm;
import models.Arena;
import models.Path;
/**
 *
 * @author matth
 */
public class VirtualMap {
    private PathNode[][] virtualMap;
    
    private VirtualMap(){
        this.virtualMap = new PathNode[Arena.ROW][Arena.COL];

        //creating objects inside each cell
        for(int i=0; i<Arena.ROW; i++) {       //for each cell in the map
            for (int j = 0; j < Arena.COL; j++) {
                virtualMap[i][j] = new PathNode();
            }
        }
    }
    
    private boolean isNodeOnSide(int row, int col, Arena.mazeState[][] maze){
        return (row==0 || col ==0 || row==(maze.length - 1) || col==(maze[0].length - 1));
    }
    
    public VirtualMap(Arena.mazeState[][] maze, boolean treatUnknownAsObstacle) {

        this();

        //creating virtual obstacles
        for (int i = 0; i < maze.length; i++)        //for each cell in the map
            for (int j = 0; j < maze[0].length; j++) {

                this.virtualMap[i][j].state = maze[i][j];
                this.virtualMap[i][j].index = new int[]{i, j};
                this.virtualMap[i][j].pathCost = Integer.MAX_VALUE;
            }

        for (int i = 0; i < maze.length; i++)        //for each cell in the map
            for (int j = 0; j < maze[0].length; j++) {
                //if this node is an obstacle, or unknown in the case of treating unknown as obstacle
                if (maze[i][j] == Arena.mazeState.obstacle || (treatUnknownAsObstacle && maze[i][j] == Arena.mazeState.unknown)) {

                    //marking its surrounding cells
                    for (int[] surroundingNodeIndex : virtualMap[i][j].getSurrondingNodeIndices()) {

                        try {
                            //if this surrounding node is a free space or unknown in the case of treating unknown as free space
                            if (this.virtualMap[surroundingNodeIndex[0]][surroundingNodeIndex[1]].state == Arena.mazeState.freeSpace
                                    || this.virtualMap[surroundingNodeIndex[0]][surroundingNodeIndex[1]].state == Arena.mazeState.path
                                    || (!treatUnknownAsObstacle &&
                                    this.virtualMap[surroundingNodeIndex[0]][surroundingNodeIndex[1]].state == Arena.mazeState.unknown))

                                this.virtualMap[surroundingNodeIndex[0]][surroundingNodeIndex[1]].state
                                        = Arena.mazeState.virtualObstacle;
                        } catch (ArrayIndexOutOfBoundsException e) {
                        }
                        //handled exception if the cell is at the side or corner
                    }
                }

                //if this node is on the side and is a free space or unknown in the case of treating unknown as free space
                if (isNodeOnSide(i, j, maze) && ((this.virtualMap[i][j].state == Arena.mazeState.freeSpace)
                        || (this.virtualMap[i][j].state == Arena.mazeState.path)
                        || (!treatUnknownAsObstacle && this.virtualMap[i][j].state == Arena.mazeState.unknown)))
                    this.virtualMap[i][j].state = Arena.mazeState.virtualObstacle;
            }

        //replace all unknowns with either virtual obstacles or free space
        for (int i = 0; i < maze.length; i++)        //for each cell in the map
            for (int j = 0; j < maze[0].length; j++) {
                if (virtualMap[i][j].state == Arena.mazeState.unknown) {
                    if(treatUnknownAsObstacle)
                        virtualMap[i][j].state = Arena.mazeState.virtualObstacle;
                    else
                        virtualMap[i][j].state = Arena.mazeState.freeSpace;
                }
            }

    }
    
    public void print(){
        System.out.println("*************************");
        for(int i = 0; i < virtualMap.length; i++) {
            for (int j = 0; j < virtualMap[i].length; j++) {

                System.out.print(virtualMap[i][j]);

            }
            System.out.println();
        }
        System.out.println("*************************");
    }

    public void printShortestPath(Path path){
        System.out.println("*************************");
        String[][] printing = new String[virtualMap.length][virtualMap[0].length];
        for(int i = 0; i < virtualMap.length; i++) {
            for (int j = 0; j < virtualMap[i].length; j++) {
                printing[i][j] = virtualMap[i][j].toString();
            }
        }
        for(PathNode node : path.getPathNodes()){
            printing[node.index[0]][node.index[1]] = "S\t";   // stands for shortest path
        }

        for(int i = 0; i < virtualMap.length; i++) {
            for (int j = 0; j < virtualMap[i].length; j++) {
                System.out.print(printing[i][j]);
            }
            System.out.println();
        }
        System.out.println("*************************");
    }

    public void printExpanded(){
        System.out.println("*************************");
        for(int i = 0; i < virtualMap.length; i++) {
            for (int j = 0; j < virtualMap[i].length; j++) {

                System.out.print(virtualMap[i][j].expanded?"T ":"  ");

            }
            System.out.println();
        }
        System.out.println("*************************");
    }

    public PathNode[][] getVirtualMap() {
        return virtualMap;
    }

    public PathNode getPathNode(int[] index){
        return virtualMap[index[0]][index[1]];
    }
}
