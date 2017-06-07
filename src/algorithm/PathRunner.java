package algorithm;
import controllers.Controller;
import models.Path;
import models.Robot;
import org.omg.PortableInterceptor.ORBInitInfo;
import services.*;
import utilities.GlobalUtilities;
import utilities.Orientation;
/**
 *
 * @author matth
 */
public class PathRunner {
    private static PathRunner instance = new PathRunner();
    private final Controller controller = Controller.getInstance();
    private final Robot robot = Robot.getInstance();

    private RPiServiceInterface rpiService;
    private SensorServiceInterface sensorService;
    private AndroidServiceInterface androidService;

    private PathRunner(){}

    public static PathRunner getInstance(){
        if(instance==null)
            instance = new PathRunner();
        return instance;
    }
    
    private void initialiseServices(boolean isRealRun){
        if(isRealRun){
            rpiService = RealRPiService.getInstance();
            sensorService = RealSensorService.getInstance();
            androidService = RealAndroidService.getInstance();
        }
        else{
//            rpiService = SimuRPiService.getInstance();
//            sensorService = SimuSensorService.getInstance();
//            androidService = SimuAndroidService.getInstance();
        }
    }
    
    public void runShortestPath(Path path, boolean isRealRun){
        initialiseServices(isRealRun);
        androidService.waitToRunShortestPath();
        runPath(path, isRealRun);
    }
    
     public void runPath(Path path, boolean isRealRun){
        initialiseServices(isRealRun);

        //if starting orientation is different from starting orientation, turn to that orientation
        if(path==null){
            System.out.println("The path is null...");
            return;
        }
        while(robot.getOrientation() != path.getPathNodes().get(0).orientation)
            rpiService.turn(Orientation.LEFT);

        int stepsToMove = 0;
        //start from the second node
        int i=1;

        while(i<path.getPathNodes().size()){
            stepsToMove = 0;
            while(i< path.getPathNodes().size() &&
                    robot.getOrientation() == path.getPathNodes().get(i).orientation){
                stepsToMove++;
                i++;
            }

            rpiService.moveForward(stepsToMove);

            //if robot has reached the destination, break out
            if(i==path.getPathNodes().size())
                break;

            //make a turn to the relative orientation of this node as compared to last node
            int turnDirection = Orientation.whichDirectionToTurn(path.getPathNodes().get(i).orientation, robot.getOrientation());
            rpiService.turn(turnDirection);
        }
        System.out.println("Shortest path completed");
    }
}
