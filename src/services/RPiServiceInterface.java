/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

/**
 *
 * @author matth
 */
public interface RPiServiceInterface {

    int moveForward(int steps);

    int turn(int direction);

    int callibrate();


    void notifyUIChange();

}
