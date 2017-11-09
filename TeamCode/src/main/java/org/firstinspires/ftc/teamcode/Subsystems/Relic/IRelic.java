package org.firstinspires.ftc.teamcode.Subsystems.Relic;

/**
 * Created by Sarthak on 9/24/2017.
 */

public interface IRelic {
    //Gains possession of relic
    void pickUpRelic();
    //Drop relic
    void releaseRelic();

    /**
     * @param power power to extend relic at
     */
    void extend(double power, boolean condition);

    /**
     * retract relic mechanism
     * @param power power to retract mechanism at
     */
    void retract(double power, boolean condition);
}
