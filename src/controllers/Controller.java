/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;
import algorithm.MazeExplorer;
import algorithm.PathRunner;
import models.*;
import mdpgroup4final.MDPGroup4Final;
import services.SerialService;
import utilities.GlobalUtilities;
import utilities.Orientation;
/**
 *
 * @author matth
 */
public class Controller {
    private final Arena arena;
    private final Robot robot;
    private int[][] previous;//used to store previous location
    private int[][] detected;//[sensor]{ROW,COL,ORIENTATION,DISTANCE}
    private int[][] location;
    private boolean isDone;//indicate an action issued is done
    private boolean update;//indicate whether an udpate is needed
    private boolean isStopped;
    private int time;
    public static int simulationSpeed = 10; //10 being the standard speed
    private final int NUMBER_OF_SENSOR = 5;
    private static Controller instance;


    public static final int ABSOLUTE_ROW = 0;
    public static final int ABSOLUTE_COL = 1;
    public static final int ABSOLUTE_ORIENTATION = 2;
    public static final int DISTANCE = 3;
    public static final int DETECT_RANGE = 4;

    private int[][][] data;
    public static final int PERCEIVE_ROW = 0;
    public static final int PERCEIVE_COL = 1;
    public static final int PERCEIVE_DAT = 2;
    public static final int UNKNOWN = -1;
    public static final int PATH = 0;
    public static final int OBSTACLE = 1;

    public static boolean isRealRun = false;

    public static Controller getInstance(){
        if(instance==null)
            instance = new Controller();
        return instance;
    }
    
    private Controller(){
        isStopped = true;
        Arena.mazeState[][] maze = new Arena.mazeState [Arena.ROW][Arena.COL];
        for (int i = 0; i < maze.length; i++)
            for (int j = 0; j < maze[i].length; j++)
                maze[i][j] = Arena.mazeState.freeSpace;  //initializing maze to freeSpace
        int[] start = {Arena.ROW-2,1};
        int[] goal = {1,Arena.COL-2};
        arena = new Arena(start, goal, maze);
        robot = Robot.getInstance();
        robot.initialize(start, Orientation.EAST);  //Start Facing the right

        int max_sensor_range = 0;
        for(Sensor s:robot.getSensors()){
            if(max_sensor_range<s.getMaxRange())max_sensor_range = s.getMaxRange();
        }
        data = new int[robot.getSensors().length][max_sensor_range][3];

        detected = new int[NUMBER_OF_SENSOR][5];//Stops using -1 for any
        for(int i=0;i<detected.length;i++)
            detected[i][0] = -1;

        location = new int[9][2];
        for(int i=0;i<location.length;i++)
            location[i][0] = -1;//used as a block to previous

        time = 0;
    }
    
    public Arena.mazeState[][] getMaze(){
        return arena.getMaze();
    }
    
    public int isInStartGoalArea(int row,int col){
        int half_size = (Arena.START_GOAL_SIZE/2);
        int[] start = arena.getStart();
        int[] goal = arena.getGoal();
        if(start[0] - half_size<=row&&row<=start[0]+half_size)
            if(start[1] - half_size<=col&&col<=start[1]+half_size)
                return 1;
        if(goal[0] - half_size<=row&&row<=goal[0]+half_size)
            if(goal[1] - half_size<=col&&col<=goal[1]+half_size)
                return 2;
        return 0;
    }
    
    public boolean setObstacle(int row,int col){
        if(isInStartGoalArea(row,col)==0) {
            arena.setObstacle(row, col, Arena.mazeState.obstacle);
            return true;
        }
        return false;
    }
    
    public boolean isValidForStartGoal(boolean setStart,int row,int col){
        int[] relative = {row,col};
        int[] compare  = setStart?arena.getGoal():arena.getStart();
        int half_size = (Arena.START_GOAL_SIZE/2)+1;

        System.out.println(Math.abs(relative[0]-compare[0])+" "+Math.abs(relative[1]-compare[1]));
        if(Math.abs(relative[0]-compare[0])<=half_size&&Math.abs(relative[1]-compare[1])<=half_size)
            return false;
        return true;
    }
    
    public void setStartGoal(boolean setStart,int row,int col){
        int half_size = (Arena.START_GOAL_SIZE/2);
        while(row<half_size)row++;//if number of grids at left side of center<half size
        while(Arena.ROW-1-row<half_size)row--;//if number of grids at right side

        while(col<half_size)col++;//if number of grids at left side of center<half size
        while(Arena.COL-1-col<half_size)col--;//if number of grids at right side

        //invalid
        if(!isValidForStartGoal(setStart,row,col))
            return;

        for(int i=row-half_size;i<row-half_size+Arena.START_GOAL_SIZE;i++)
            for(int j=col-half_size;j<col-half_size+Arena.START_GOAL_SIZE;j++)
                arena.setObstacle(i,j,Arena.mazeState.freeSpace);

        int[] data = {row,col};
        if(setStart)
            arena.setStart(data);
        else
            arena.setGoal(data);

    }
    
    public int getStart_goal_size() {
        return Arena.START_GOAL_SIZE;
    }

    //return robot location to paint
    //assumed robot size = 3
    public int[][] getRobotLocation(){
        //calculate robot location base on robot loc(center)+robot size
        for(int i=0;i<Robot.SIZE;i++)
            for(int j=0;j<Robot.SIZE;j++){
                location[i*Robot.SIZE+j][0] = robot.getLocation()[0]-Robot.HALF_SIZE+i;//location+half size
                location[i*Robot.SIZE+j][1] = robot.getLocation()[1]-Robot.HALF_SIZE+j;
            }
        update = false;
        return location;
    }

    public void savePrevious(){
        previous = robot.getRobotBlocks();
    }
    
    public int[][] getPrevious(){
        return previous;
    }
    
    public void startRobot() {
        resetTime();
        isDone = false;
        isStopped = false;

        previous = null;

        for(int i=0;i<location.length;i++)
            location[i][0] = -1;//used as a block to previous

        update = false;
        //update = true;//to show first update

        if(isRealRun)
            SerialService.getInstance().connectToHost();

        Path shortestPath = MazeExplorer.getInstance().explore(isRealRun);

        if(shortestPath == null){
            System.out.println("I can't find any path to the goal. I'm sorry!");
            return;
        }


        System.out.println("The path cost is " + shortestPath.getTotalCost());
        //System.out.println("The path cost is " + shortestPath.getDiagonalPathCost());

        try{
            FileController.getInstance().writeTo(FileController.PERCEIVED_MAP_NAME, robot.getPerceivedArena().toMapDescriptor());
        }catch (Exception e){
            e.printStackTrace();
        }

        PathRunner.getInstance().runShortestPath(shortestPath, isRealRun);
        //PathRunner.getInstance().runDiagonalPath(shortestPath, isRealRun);

        isDone = true;
        isStopped = true;
    }
    
    public void setUpdate(boolean update) {
        this.update = update;
    }
    
    public void resetTime(){time = 0;}
    public int getTimeLimit(){
        return MazeExplorer.getInstance().TIME_LIMIT/1000;
    }

    public void startTimer(){
        MDPGroup4Final.getInstance().startTimer();
    }

    public void stopTimer(){
        MDPGroup4Final.getInstance().stopTimer();
    }
    
    public void setRobotOrientation(int orientation){robot.setOrientation(orientation);}
    public boolean isDone(){return isDone;}
    public boolean isStopped(){return isStopped;}
    public boolean needUpdate(){return update;}
    public void updated(){update = false;}
    public void setSimulationSpeed(int speed){simulationSpeed = speed;}
    public Arena getArena() {
        return arena;
    }
    
    public void detect(int[] readings){

        Sensor sensor;
        for(int i = 0; i < readings.length; i++) {
            sensor = robot.getSensors()[i];

            detected[i][ABSOLUTE_ROW] = sensor.getAbsoluteLocation()[0];
            detected[i][ABSOLUTE_COL] = sensor.getAbsoluteLocation()[1];
            detected[i][ABSOLUTE_ORIENTATION] = sensor.getAbsoluteOrientation();
            detected[i][DISTANCE] = readings[i];
            detected[i][DETECT_RANGE] = sensor.getMaxRange();
        }
    }
    
    public void saveMap() throws Exception{
        FileController.getInstance().writeTo(FileController.MAP_FILE_NAME,arena.toMapDescriptor());
    }

    public void loadMap()throws Exception{
        arena.loadMapDescriptor(FileController.getInstance().readFrom(FileController.MAP_FILE_NAME));
    }
    
    public int[][][] getPerceivedMapData(){
        //1 = ROW 2 = COL 3 = DATA
        for(int i = 0;i<data.length;i++){
            int[] sensor_loc = robot.getSensors()[i].getAbsoluteLocation();
            for(int j=0;j<data[i].length;j++){
                int[] target_loc = GlobalUtilities.locationParser(sensor_loc,robot.getSensors()[i].getAbsoluteOrientation(),j+1);
                Arena.mazeState state;
                try{
                    state = robot.getPerceivedArena().getMaze()[target_loc[0]][target_loc[1]];
                }
                catch(ArrayIndexOutOfBoundsException aiobe){
                    state = Arena.mazeState.unknown;
                }
                data[i][j][PERCEIVE_ROW] = target_loc[0];
                data[i][j][PERCEIVE_COL] = target_loc[1];
                if(state == Arena.mazeState.unknown)data[i][j][PERCEIVE_DAT] = Controller.UNKNOWN;
                else if(state == Arena.mazeState.obstacle)data[i][j][PERCEIVE_DAT] = Controller.OBSTACLE;
                else data[i][j][PERCEIVE_DAT] = Controller.PATH;
            }
        }
        return data;
    }
    
    public int getTime(){return time;}
    public void updateTime(int time_elapse){time+=time_elapse;}
    
    public int getCoverageLimit(){
        return (int)MazeExplorer.getInstance().TARGET_COVERAGE*100;
    }

    //if anything goes wrong,return false
    public boolean setCoverageLimit(String input){
        double value;
        boolean error = false;
        try{
            value = Double.parseDouble(input)/100;
            System.out.println(value);
            if(value<0||value>1)throw new Exception("Incorrect Value (Less than 0 or more than 100) "+value);
        }
        catch(Exception ex){
            value = MazeExplorer.DEFAULT_TARGET_COVERAGE;
            error = true;
            ex.printStackTrace();
        }
        MazeExplorer.getInstance().TARGET_COVERAGE = value;
        return error;
    }
    
    public void setRobotLocation(int[] loc){robot.setLocation(loc);}
    public int getRobotOrientation(){return robot.getOrientation();}
    public int getRobotOrientation1357(){
        int orientation;
        switch(getRobotOrientation()){
            case Orientation.NORTH:orientation = 1;break;
            case Orientation.WEST:orientation = 3;break;
            case Orientation.EAST:orientation = 5;break;
            case Orientation.SOUTH:orientation = 7;break;
            default : orientation = 0;
        }
        return orientation;
    }
    
    public boolean setTimeLimit(String input){
        int time;
        boolean error = false;
        try{
            time = Integer.parseInt(input);
            if(time<=0)throw new Exception("Incorrect Value (Less than or equal to 0) "+time);
            time*=1000;
        }
        catch(Exception ex){
            time = MazeExplorer.DEFAULT_TIME_LIMIT;
            error = true;
            ex.printStackTrace();
        }
        MazeExplorer.getInstance().TIME_LIMIT = time;
        return error;
    }

    public int[] getStartGoalLoc(boolean isStart) {
        int[] data = new int[Arena.START_GOAL_SIZE*Arena.START_GOAL_SIZE];
        int first = (isStart?arena.getStart()[0]*Arena.COL+arena.getStart()[1]:arena.getGoal()[0]*Arena.COL+arena.getGoal()[1]) - Arena.START_GOAL_SIZE/2;//indicate middle row 1st col
        for(int i=0;i<Arena.START_GOAL_SIZE;i++){
            data[i] = first - Arena.COL + i;//upper row
            data[i+Arena.START_GOAL_SIZE] = first + i;//center
            data[i+2*Arena.START_GOAL_SIZE] = first +Arena.COL + i;//lower row
        }
        return data;
    }
    
    public void setFree(int row,int col){
        arena.setObstacle(row, col, Arena.mazeState.freeSpace);
    }
    
    public boolean[][] getArenaInformation(){
        boolean[][] arena_maze = new boolean[Arena.ROW][Arena.COL];
        Arena.mazeState[][] maze = arena.getMaze();
        for(int i = 0; i< maze.length; i++)
            for(int j=0;j<maze[i].length;j++)
                arena_maze[i][j] = maze[i][j]==Arena.mazeState.obstacle;
        return arena_maze;
    }
}
