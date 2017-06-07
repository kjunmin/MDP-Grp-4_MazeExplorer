
package models;
import utilities.Convertor;
/**
 *
 * @author matth
 */
public class Arena {
    public static final int COL = 15;
    public static final int ROW = 20;
    public static final int START_GOAL_SIZE = 3;
    
    public enum mazeState{
        freeSpace, obstacle, unknown, virtualObstacle, path
    }
    
    private int[] start;//indicate start center
    private int[] goal;//indicate goal center
    private mazeState[][] maze; //indicate whether the grid is a obstacle
    
    public Arena(){}

    public Arena(int[] start,int[] goal){
        this.start = start;
        this.goal = goal;
        this.maze = new mazeState[ROW][COL];
    }
    
    public Arena(int[] start,int[] goal, mazeState[][] maze){
        this.start = start;
        this.goal = goal;
        this.maze = maze;
    }
    
    public void resetToCertainState(mazeState state){
        for(int i=0; i<maze.length; i++){
            for(int j=0; j<maze[0].length; j++)
                maze[i][j] = state;
        }
    }
    
    public int[][] getStartBlocks(){
        int[][] blocks = new int[9][2];
        int n = 0;
        for(int i=-(Robot.SIZE/2); i<=Robot.SIZE/2; i++)
            for(int j=-(Robot.SIZE/2); j<=Robot.SIZE/2; j++){
                blocks[n++] = new int[]{this.getStart()[0]+i, this.getStart()[1]+j};
            }
        return blocks;
    }

    public int[][] getGoalBlocks(){
        int[][] blocks = new int[9][2];
        int n = 0;
        for(int i=-(Robot.SIZE/2); i<=Robot.SIZE/2; i++)
            for(int j=-(Robot.SIZE/2); j<=Robot.SIZE/2; j++){
                blocks[n++] = new int[]{this.getGoal()[0]+i, this.getGoal()[1]+j};
            }
        return blocks;
    }

    public int[] getStart(){
        return start;
    }
    public int[] getGoal(){
        return goal;
    }

    public void setStart(int[] start){
        this.start = start;
    }

    public void setGoal(int[] goal){
        this.goal = goal;
    }

    public void setObstacle(int row,int col,mazeState state){
        maze[row][col] = state;
    }

    public mazeState[][] getMaze(){
        return maze;
    }

    public void print(){
        for(int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {

                switch (maze[i][j]){
                    case freeSpace:
                        System.out.print("f\t");
                        break;
                    case obstacle:
                        System.out.print("o\t");
                        break;
                    case unknown:
                        System.out.print("u\t");
                        break;
                    case virtualObstacle:
                        System.out.print("v\t");
                        break;
                    case path:
                        System.out.print("p\t");
                        break;
                    default:
                        System.out.print("0\t");
                        break;
                }
            }
            System.out.println();
        }
    }

    private final int BREAKS_TO_SEPARATE_DESCRIPTOR_PARTS = 2;



    public String toObstacleInfo() {
        String obstacleInfo = "";
        char[] ternary = new char[3];
        int index = 0;
        for (int i = ROW - 1; i >= 0; i--) {
            for (int j = 0; j < COL; j++) {
                if (maze[i][j] == mazeState.obstacle)
                    ternary[index] = '1';
                else if (maze[i][j] == mazeState.freeSpace || maze[i][j] == mazeState.path) {
                    ternary[index] = '0';
                } else
                    ternary[index] = '2';

                if (++index == 3) {
                    index = 0;
                    obstacleInfo += String.valueOf(Convertor.toSuperDecimal(ternary));
                }
            }
        }
        return obstacleInfo;
    }

    public String addSpace(String mdf){
        String mdfWithSpace = "";
        int i = 0;
        for(; i<mdf.length()-1; i++){
            mdfWithSpace += mdf.charAt(i) + " ";
        }
        mdfWithSpace += mdf.charAt(i);
        return mdfWithSpace;
    }


    public String toMapDescriptor(){
        String descriptorPart1 = "11\n";
        String descriptorPart2 = "";
        for(int i= ROW-1; i>=0; i--) {
            for (int j = 0; j < COL; j++) {
                if (maze[i][j] == mazeState.unknown)
                    descriptorPart1 += "0";
                else {
                    descriptorPart1 += "1";
                    if(maze[i][j] == mazeState.freeSpace || maze[i][j] == mazeState.path)
                        descriptorPart2 += "0";
                    else
                        descriptorPart2 += "1";
                }
            }
            descriptorPart1 += "\n";
            descriptorPart2 += "\n";
        }
        descriptorPart1 += "11";
        int append = (8- (descriptorPart2.length() % 8));
        for(int i = 0; i< append; i++)
            descriptorPart2 += "1";

        String separate = "";
        for(int i = 0; i< BREAKS_TO_SEPARATE_DESCRIPTOR_PARTS; i++)
            separate += "\n";
        //return descriptorPart1 + separate + ";\n"+ descriptorPart2;
        return Convertor.convertToHex(descriptorPart1) + separate + ";\n"+ Convertor.convertToHex(descriptorPart2);
    }

    public void loadMapDescriptor(String descriptor){
        String[] descriptors = descriptor.split(";");
        descriptors[0] = Convertor.convertFromHex(descriptors[0].replaceAll("\n",""));
        descriptors[1] = Convertor.convertFromHex(descriptors[1].replaceAll("\n",""));
        //int counter1 = 3;
        //int counter2 = 1
        int counter1 = 2;
        int counter2 = 0;
        for(int i= ROW-1; i>=0; i--) {
            for (int j = 0; j < COL; j++) {

                if(descriptors[0].charAt(counter1) == '1'){
                    if(descriptors[1].charAt(counter2) == '1')
                        maze[i][j] = mazeState.obstacle;
                    else
                        maze[i][j] = mazeState.freeSpace;
                }else
                    maze[i][j] = mazeState.unknown;

                counter1++;
                counter2++;
            }
        }
    }

    //returns the percentage of map that has been covered by detected areas
    public double coverage(){
        int cover = 0;
        for(int i=0; i<ROW; i++)
            for(int j=0; j<COL; j++){
                if(maze[i][j] != mazeState.unknown)
                    cover++;
            }
        return (double) cover / (ROW * COL);
    }

    public void makeBlocksPath(int[][] blocks){
        for(int[] block : blocks){
            maze[block[0]][block[1]] = mazeState.path;
        }
    }

}
