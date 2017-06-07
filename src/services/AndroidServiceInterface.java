
package services;

/**
 *
 * @author matth
 */
public interface AndroidServiceInterface {
    int waitToStartExploration();

    int waitToRunShortestPath();

    int sendMapDescriptor();

    int sendObstacleInfo();
}
