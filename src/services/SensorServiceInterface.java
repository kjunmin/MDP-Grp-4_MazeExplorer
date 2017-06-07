
package services;
import models.Sensor;
/**
 *
 * @author matth
 */
public interface SensorServiceInterface {
    int detectObstacle(Sensor sensor);

    String detect();
}