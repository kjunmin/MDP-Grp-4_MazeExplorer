
package services;
import models.Sensor;
import utilities.Messages;
/**
 *
 * @author matth
 */
public class RealSensorService implements SensorServiceInterface{

    private static RealSensorService instance;

    private SerialService serial = SerialService.getInstance();

    public static RealSensorService getInstance(){
        if(instance==null)
            instance= new RealSensorService();
        return instance;
    }
    
    private RealSensorService(){}

    @Override
    public String detect() {

        serial.sendMessage(Messages.ARDUINO_CODE + Messages.detectObstacles());

        String returnMessage = serial.readMessage();
        
        return returnMessage;
    }
    
    @Override
    public int detectObstacle(Sensor sensor) {
        return 0;
    }
}
