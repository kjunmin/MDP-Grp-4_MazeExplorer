package algorithm;
import controllers.Controller;
import models.Arena;
import models.Path;
import models.Robot;
import models.Sensor;
import services.*;
import utilities.GlobalUtilities;
import utilities.Orientation;
import static utilities.GlobalUtilities.locationParser;
/**
 *
 * @author matth
 */
public class MazeExplorer {
    private static MazeExplorer instance = new MazeExplorer();
    private final Controller controller = Controller.getInstance();
    private final Robot robot = Robot.getInstance();
    
    public static final int DEFAULT_TIME_LIMIT = 6*60*1000;
    
    private RPiServiceInterface rpiService;
    private SensorServiceInterface sensorService;
    private AndroidServiceInterface androidService;
     private static final int CALIBRATE_LIMIT = 8;
    private static final int CALIBRATE_DISTANCE = 1;


    private int calibrate_age_ns;
    private int calibrate_age_we;
    public int TIME_LIMIT = 6 * 60 * 1000;   // 6 minutes
    public double TARGET_COVERAGE = 1;           // 100%
    public static final int IGNORE_DISTANCE = -3004;
    public static final double DEFAULT_TARGET_COVERAGE = 1;
     
     private MazeExplorer(){}

    public static MazeExplorer getInstance(){
        if(instance==null)
            instance = new MazeExplorer();
        return instance;
    }
    
    private void initialiseServices(boolean isRealRun){
        if(isRealRun){
            rpiService = RealRPiService.getInstance();
            sensorService = RealSensorService.getInstance();
            androidService = RealAndroidService.getInstance();
        }
        else{
            System.out.println("No sim yet");
//            rpiService = SimuRPiService.getInstance();
//            sensorService = SimuSensorService.getInstance();
//            androidService = SimuAndroidService.getInstance();
        }
    }
    
    public Path explore(boolean isRealRun){

        initialiseServices(isRealRun);

        robot.printStatus();


        androidService.waitToStartExploration();
        controller.startTimer();

        robot.getPerceivedArena().makeBlocksPath(robot.getRobotBlocks());
        robot.getPerceivedArena().makeBlocksPath(robot.getPerceivedArena().getStartBlocks());
        robot.getPerceivedArena().makeBlocksPath(robot.getPerceivedArena().getGoalBlocks());
        robot.getPerceivedArena().print();

        forcePerformDoubleCalibrate();

        int moves = 0; //keeping track of the moves robot has made

        while(!controller.isStopped()){

            robot.getPerceivedArena().makeBlocksPath(robot.getRobotBlocks());

            long time1 = System.nanoTime();

            observe();

            long time2 = System.nanoTime();
            System.out.println("RealRun = " + isRealRun + ", observation delay = " + (time2 - time1));

            androidService.sendObstacleInfo();

            long time3 = System.nanoTime();
            analyzeAndMove();

            long time4 = System.nanoTime();
            System.out.println("RealRun = " + isRealRun + ", analysis delay = " + (time4 - time3));

            moves++;

            //if the robot comes back to start location after more than or equal to 58 moves, break
            //in order to reach goal and come back to start, 58 is the minimum number of moves
            if(moves>=58 && GlobalUtilities.sameLocation(robot.getLocation(), controller.getArena().getStart()))
                break;

        }

        robot.getPerceivedArena().print();

        androidService.sendMapDescriptor();

        System.out.println("The map string is :******" + robot.getPerceivedArena().toMapDescriptor() + "*******");
        controller.stopTimer();

        Path shortestPath = getReadyForDiagonalShortestPath();
        //Path shortestPath = getReadyForShortestPath();
        System.out.println("Exploration completed");

        return shortestPath;

    }
    
    private void analyzeAndMove(){

        //if(getMazeStateOverridenOrientation()>=Orientation.NORTH){
        preemptRobotCircling(Orientation.RIGHT);
        //}
        analyzeAndCalibrate();

        //this a few lines were used to shoot video
//        if(HARD_CODE==1 && robot.getLocation()[0]==10 && robot.getLocation()[1] == 8){
//            rpiService.turn(Orientation.LEFT);
//        }

        if(isRightEmpty()) {
            rpiService.turn(Orientation.RIGHT);
            observe();
            rpiService.moveForward(1);

        } else if(isFrontEmpty()) {
            rpiService.moveForward(1);

        } else if(isLeftEmpty()) {
            rpiService.turn(Orientation.LEFT);
            observe();
            rpiService.moveForward(1);

        } else {
            rpiService.turn(Orientation.LEFT);
            //rpiService.turn(Orientation.RIGHT);
            System.out.println("DEFAULT CASE");
        }

    }
    
    private boolean forcePerformDoubleCalibrate(){

        calibrate_age_ns = CALIBRATE_LIMIT;     //force the robot to calibrate
        calibrate_age_we = CALIBRATE_LIMIT;     //force the robot to calibrate

        int[] robot_loc = robot.getLocation();
        int[] start = controller.getArena().getStart();

        if(robot_loc[0]==start[0]&& //row-1
        robot_loc[1]==start[1])//if diagonal offset from start
        {
            //if(robot.getOrientation()!=Orientation.NORTH&&robot.getOrientation()!=Orientation.EAST)return;
            while(robot.getOrientation()!=Orientation.WEST)
                rpiService.turn(Orientation.LEFT);

            //force update perceived arena
            int[] reading = new int[robot.getSensors().length];
//            for(int i=0;i<reading.length;i++)
//                reading[i] = robot.getSensors()[i].getrelativeOrientation()==Orientation.FRONT ? CALIBRATE_DISTANCE : IGNORE_DISTANCE;

            //turn to back
            for(int i=0;i<2;i++){
                //rpiService.turn(orientation==Orientation.NORTH?Orientation.LEFT:Orientation.RIGHT);
                markObstaclesOnUI(reading);
                for(int j=0;j<reading.length;j++)
                    if( robot.getSensors()[i].getrelativeOrientation()==Orientation.FRONT ){
                        markMaze(robot.getSensors()[j],reading[j]);
                    }
                markObstaclesOnUI(reading);
                analyzeAndCalibrate(i%2==1);
                rpiService.turn(Orientation.LEFT);
            }

            return true;
            //rpiService.turn(Orientation.BACK);
        }
        return false;
    }
    
    private void notEnoughTimeAction(){}
    private void targetCoverageReachedAction(){}
    
    public void observe(){
        int[] readings = getSensorReadings();
        Sensor[] sensors = robot.getSensors();
        for(int i=0; i<robot.getSensors().length; i++){
            markMaze(sensors[i], readings[i]);
        }

        markObstaclesOnUI(readings);
    }
    
    private void markMaze(Sensor sensor, int steps){
        try {
            if(steps == IGNORE_DISTANCE)return;
            Arena arena = robot.getPerceivedArena();
            if (steps < 0) {
                for (int i = sensor.getMinRange(); i <= sensor.getMaxRange(); i++) {
                    int[] location = locationParser(sensor.getAbsoluteLocation(), sensor.getAbsoluteOrientation(), i);

                    //adding maze state type: path
                    if(arena.getMaze()[location[0]][location[1]] == Arena.mazeState.path)
                        continue;

                    if(i != sensor.getMaxRange() /*&& i != sensor.getMaxRange()-1*/) {         //override the original obstacle info only when sensor reading does not equal to max range
                        arena.setObstacle(location[0], location[1], Arena.mazeState.freeSpace);
                    } else if(arena.getMaze()[location[0]][location[1]]== Arena.mazeState.unknown)
                        arena.setObstacle(location[0], location[1], Arena.mazeState.freeSpace);

                }
            } else if (sensor.getMinRange() <= steps && steps <= sensor.getMaxRange()) {
                for (int i = sensor.getMinRange(); i < steps; i++) {
                    int[] location = locationParser(sensor.getAbsoluteLocation(), sensor.getAbsoluteOrientation(), i);
                    //adding maze state type: path
                    if(arena.getMaze()[location[0]][location[1]] == Arena.mazeState.path)
                        continue;
                    arena.setObstacle(location[0], location[1], Arena.mazeState.freeSpace);
                }
                int[] location = locationParser(sensor.getAbsoluteLocation(), sensor.getAbsoluteOrientation(), steps);

                //adding maze state type: path
                if(arena.getMaze()[location[0]][location[1]] != Arena.mazeState.path) {
                    if (steps != sensor.getMaxRange()/* && steps != sensor.getMaxRange()-1*/) {      //override the original obstacle info only when sensor reading does not equal to max range
                        arena.setObstacle(location[0], location[1], Arena.mazeState.obstacle);
                    } else if (arena.getMaze()[location[0]][location[1]] == Arena.mazeState.unknown)
                        arena.setObstacle(location[0], location[1], Arena.mazeState.obstacle);

                }
            }
        }catch (ArrayIndexOutOfBoundsException e){
//            System.out.println("This message comes from MazeExplorer method markMaze. This is normal and please ignore this message");
        }catch (Exception e){
            e.printStackTrace();
        }
        notifyUIChange();
    }
    
    private void preemptRobotCircling(int orientation){
        //setMazeStateOverridenOrientation(-1);
        if(isRightEmpty()&&isBottomRigthCornerEmpty()){
            //System.out.println("*************Inside function preemptRobotCircling*******");
            rpiService.turn(orientation);
            observe();
            rpiService.moveForward(1);
            observe();
            if(!isFrontEmpty()){
                rpiService.turn(Orientation.LEFT);
                return;
            }
            preemptRobotCircling(Orientation.FRONT);
        }
    }
    
    private boolean isBottomRigthCornerEmpty(){
        try {
            int[] rightSide = GlobalUtilities.locationParser(robot.getLocation(), Orientation.turn(robot.getOrientation(), Orientation.RIGHT), Robot.HALF_SIZE + 1);
            int[] bottomRightCorner = GlobalUtilities.locationParser(rightSide, Orientation.oppositeOrientation(robot.getOrientation()), Robot.HALF_SIZE + 1);
            Arena.mazeState state = robot.getPerceivedArena().getMaze()[bottomRightCorner[0]][bottomRightCorner[1]];
            if (state == Arena.mazeState.freeSpace || state == Arena.mazeState.path) {
                return true;
            }
        }catch (ArrayIndexOutOfBoundsException aiobe){
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean isRightEmpty(){
        if(locateObstacle("topRight", Orientation.RIGHT,true)==1||locateObstacle("middleRight", Orientation.RIGHT,true)==1
                ||locateObstacle("bottomRight", Orientation.RIGHT,true)==1)
            return false;
        return true;
    }

    private boolean isFrontEmpty(){
        if(locateObstacle("topLeft", Orientation.FRONT,true)==1||locateObstacle("topCenter", Orientation.FRONT,true)==1
                ||locateObstacle("topRight", Orientation.FRONT,true)==1)
            return false;
        return true;
    }

    private boolean isLeftEmpty(){
        if(locateObstacle("topLeft", Orientation.LEFT,true)==1||locateObstacle("middleLeft", Orientation.LEFT,true)==1
                ||locateObstacle("bottomLeft", Orientation.LEFT,true)==1)
            return false;
        return true;
    }

    private boolean isDeadEnd(){
        int orientation = robot.getOrientation();
        return false;
    }
    
    private int locateObstacle(String relativeLocation, int relativeOrientation,boolean treatUnknownAsObstacle) {
        Arena.mazeState[][] maze = robot.getPerceivedArena().getMaze();
        for(int step=1; step<Math.max(Arena.ROW,Arena.COL); step++){
            try{
                int[] absoluteLocation = toAbsoluteLocation(relativeLocation);
                int absoluteOrientation = Orientation.turn(relativeOrientation, robot.getOrientation());
                int[] tempLocation = locationParser(absoluteLocation, absoluteOrientation, step);
                Arena.mazeState obstacle = maze[tempLocation[0]][tempLocation[1]];
                if(obstacle!=Arena.mazeState.freeSpace && obstacle!=Arena.mazeState.path)
                    if(treatUnknownAsObstacle)
                        return step;
                    else {
                        if(obstacle==Arena.mazeState.unknown)
                            return -1;
                        else
                            return step;
                    }
            }catch (ArrayIndexOutOfBoundsException e){
                return step;
            }
        }
        return -1;
    }
    
    public int[] toAbsoluteLocation(String relativeLocation){
        int[] absoluteLocation= new int[2];
        int[] rotatedRelativeLocation = Orientation.rotateCoordinates(GlobalUtilities.relativeLocation.get(relativeLocation), robot.getOrientation());
        absoluteLocation[0] = robot.getLocation()[0] + rotatedRelativeLocation[0];
        absoluteLocation[1] = robot.getLocation()[1] + rotatedRelativeLocation[1];
        return absoluteLocation;
    }
    
    public Path getReadyForShortestPath(){

        Arena.mazeState[][] maze = robot.getPerceivedArena().getMaze();
        int[] start = robot.getPerceivedArena().getStart();
        int[] goal = robot.getPerceivedArena().getGoal();

        Path pathStartFacingNorth, pathStartFacingEast;

        boolean treatUnknownAsObstacle = true;



        //get path if robot starts facing north
        pathStartFacingNorth = PathFinder.getInstance().aStarStraight(maze, start, goal, treatUnknownAsObstacle, Orientation.NORTH);

        //if path does not exist
        if(pathStartFacingNorth == null){
            treatUnknownAsObstacle = false;
            //get path again
            pathStartFacingNorth = PathFinder.getInstance().aStarStraight(maze, start, goal, treatUnknownAsObstacle, Orientation.NORTH);
        }
        //if path still does not exist even if unknown areas are treated free space
        if(pathStartFacingNorth == null)
            return null;

        //get path if robot starts facing east
        pathStartFacingEast = PathFinder.getInstance().aStarStraight(maze, start, goal, treatUnknownAsObstacle, Orientation.EAST);

        int targetFacingDirection;
        Path idealPath;

        if(pathStartFacingNorth.getTotalCost() < pathStartFacingEast.getTotalCost())
            idealPath = pathStartFacingNorth;
        else
            idealPath = pathStartFacingEast;

        if(robot.getOrientation()==Orientation.SOUTH)
            rpiService.turn(Orientation.RIGHT);
        //forcePerformDoubleCalibrate();  //force a double calibration before shortest

        targetFacingDirection = idealPath.getPathNodes().get(0).orientation;

        while(robot.getOrientation() != targetFacingDirection)
            rpiService.turn(Orientation.LEFT);

        //take the first move before running shortest path, as the center of robot is still within start zone after 1 move
        if(robot.getOrientation() == idealPath.getPathNodes().get(1).orientation)
            rpiService.moveForward(1);

        //if the second move is turning, then do this as well
        if(robot.getOrientation() != idealPath.getPathNodes().get(2).orientation) {
            int direction = Orientation.whichDirectionToTurn(idealPath.getPathNodes().get(2).orientation, robot.getOrientation());
            rpiService.turn(direction);
        }

        idealPath = PathFinder.getInstance().aStarStraight(maze, robot.getLocation(), goal, treatUnknownAsObstacle, robot.getOrientation());
        //return the ideal path
        return idealPath;
    }
    
    public void notifyUIChange() {
        controller.setUpdate(true);
    }
    
    private void markObstaclesOnUI(int[] readings){
        controller.detect(readings);
    }

    private void analyzeAndCalibrate(boolean isNS){
        if(isNS)
            calibrate_age_ns++;
        else
            calibrate_age_we++;
        int orientation = checkCalibrate(isNS);
        System.out.println("age ns:"+calibrate_age_ns+" age we:"+calibrate_age_we+" Orientation: "+orientation);
        if(orientation>=Orientation.FRONT)
            calibrate(orientation);
    }
    private void analyzeAndCalibrate(){
        analyzeAndCalibrate(true);
        analyzeAndCalibrate(false);
    }
    
    private boolean checkCanCalibrateFront(){
        if(locateObstacle("topLeft", Orientation.FRONT,false)==CALIBRATE_DISTANCE&&locateObstacle("topCenter", Orientation.FRONT,false)==CALIBRATE_DISTANCE
                &&locateObstacle("topRight", Orientation.FRONT,false)==CALIBRATE_DISTANCE)
            return true;
        return false;
    }

    private boolean checkCanCalibrateBack(){
        if(locateObstacle("bottomLeft", Orientation.BACK,false)==CALIBRATE_DISTANCE&&locateObstacle("bottomCenter", Orientation.BACK,false)==CALIBRATE_DISTANCE
                &&locateObstacle("bottomRight", Orientation.BACK,false)==CALIBRATE_DISTANCE)
            return true;
        return false;
    }

    private boolean checkCanCalibrateRight(){
        if (locateObstacle("topRight", Orientation.RIGHT,false) == CALIBRATE_DISTANCE && locateObstacle("middleRight", Orientation.RIGHT,false) == CALIBRATE_DISTANCE
                && locateObstacle("bottomRight", Orientation.RIGHT,false) == CALIBRATE_DISTANCE)
            return true;
        return false;
    }

    private boolean checkCanCalibrateLeft(){
        if (locateObstacle("topLeft", Orientation.LEFT,false) == CALIBRATE_DISTANCE && locateObstacle("middleLeft", Orientation.LEFT,false) == CALIBRATE_DISTANCE
                && locateObstacle("bottomLeft", Orientation.LEFT,false) == CALIBRATE_DISTANCE)
            return true;
        return false;
    }

    private int checkCalibrate(boolean isNS) {
        if (calibrate_age_ns < CALIBRATE_LIMIT && calibrate_age_we < CALIBRATE_LIMIT) return -1;
        //if we are checking NS,
        //NS age
        //North South,  if robot facing North/South, direct check, if not orientation = turn(1)
        int orientation = robot.getOrientation();
        if(isNS){
            if(calibrate_age_ns < CALIBRATE_LIMIT) return -1;
            if(orientation==Orientation.WEST||orientation==Orientation.EAST){
                if(checkCanCalibrateRight())return Orientation.RIGHT;
                if(checkCanCalibrateLeft())return Orientation.LEFT;
            }
            else{
                if(checkCanCalibrateFront())return Orientation.FRONT;
                if(checkCanCalibrateBack())return Orientation.BACK;
            }
        }
        else{
            if(calibrate_age_we < CALIBRATE_LIMIT) return -1;
            if(orientation==Orientation.WEST||orientation==Orientation.EAST){
                if(checkCanCalibrateFront())return Orientation.FRONT;
                if(checkCanCalibrateBack())return Orientation.BACK;
            }
            else{
                if(checkCanCalibrateRight())return Orientation.RIGHT;
                if(checkCanCalibrateLeft())return Orientation.LEFT;
            }
        }
        return -1;
    }


    private void calibrate(int orientation){
        rpiService.turn(orientation);  //turn to face the wall/long obstacle
        rpiService.callibrate();
        if(orientation != Orientation.FRONT && orientation != Orientation.BACK)
            rpiService.turn(Orientation.oppositeOrientation(orientation));
        else if(orientation == Orientation.BACK)
            rpiService.turn(Orientation.BACK);

        int calibrated_orientation = Orientation.turn(robot.getOrientation(),orientation);
        if(calibrated_orientation == Orientation.NORTH||calibrated_orientation == Orientation.SOUTH)
            calibrate_age_ns = 0;
        if(calibrated_orientation == Orientation.WEST||calibrated_orientation == Orientation.EAST)
            calibrate_age_we = 0;
        //calibrate_age = 0;
    }
    
    private void doubleCalibrate(int orientation){
        calibrate(Orientation.FRONT);
        calibrate(orientation);
        //double_calibrate_age = 0;
        System.out.println("Double Calibrated");
    }
    
    public Path getReadyForDiagonalShortestPath(){

        Arena.mazeState[][] maze = robot.getPerceivedArena().getMaze();
        int[] start = robot.getPerceivedArena().getStart();
        int[] goal = robot.getPerceivedArena().getGoal();

        Path pathStartFacingNorth, pathStartFacingEast;

        boolean treatUnknownAsObstacle = true;

        //get path if robot starts facing north
        pathStartFacingNorth = PathFinder.getInstance().aStarStraight(maze, start, goal, treatUnknownAsObstacle, Orientation.NORTH);

        //if path does not exist
        if(pathStartFacingNorth == null){
            treatUnknownAsObstacle = false;
            //get path again
            pathStartFacingNorth = PathFinder.getInstance().aStarStraight(maze, start, goal, treatUnknownAsObstacle, Orientation.NORTH);
        }
        //if path still does not exist even if unknown areas are treated free space
        if(pathStartFacingNorth == null)
            return null;

        //get path if robot starts facing east
        pathStartFacingEast = PathFinder.getInstance().aStarStraight(maze, start, goal, treatUnknownAsObstacle, Orientation.EAST);

        int targetFacingDirection;
        Path idealPath;

        if(pathStartFacingNorth.getTotalCost() < pathStartFacingEast.getTotalCost())
            idealPath = pathStartFacingNorth;
        else
            idealPath = pathStartFacingEast;

        if(robot.getOrientation()==Orientation.SOUTH)
            rpiService.turn(Orientation.RIGHT);
        forcePerformDoubleCalibrate();  //force a double calibration before shortest

        targetFacingDirection = idealPath.getPathNodes().get(0).orientation;

        while(robot.getOrientation() != targetFacingDirection)
            rpiService.turn(Orientation.LEFT);

        VirtualMap virtualMap = new VirtualMap(maze, treatUnknownAsObstacle);
        long startTime = System.nanoTime();
        Path diagonalPath = PathFinder.getInstance().getDiagonalPath(robot.getPerceivedArena().getGoal(), idealPath, virtualMap);
        long endTime = System.nanoTime();
        System.out.println("The duration is "  + (endTime - startTime));
        virtualMap.printShortestPath(diagonalPath);
        System.out.println("The size of the path is " + diagonalPath.getPathNodes().size());
        //diagonalPath.print();

        //idealPath = PathFinder.getInstance().aStarStraight(maze, robot.getLocation(), goal, treatUnknownAsObstacle, robot.getOrientation());
        //return the diagonal path
        return diagonalPath;
    }
    
    private int[] getSensorReadings(){
        String sensorReadings = sensorService.detect();
        System.out.println("The reading is: **" + sensorReadings + "**\n");
        String[] parts = sensorReadings.split(":");
        int[] readings = new int[parts.length];
        try{
            int i = 0;
            for(String part : parts) {
                readings[i] = Integer.parseInt(part);
                i++;
            }
        }catch (Exception e){
            e.printStackTrace();

            //if something goes wrong, retry after 1 milisecond
            try{
                Thread.sleep(1);
            }catch (InterruptedException expt){
                expt.printStackTrace();
            }
            return getSensorReadings();
        }
        return readings;
    }
}

