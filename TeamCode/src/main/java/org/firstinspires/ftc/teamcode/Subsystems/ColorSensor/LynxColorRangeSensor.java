package org.firstinspires.ftc.teamcode.Subsystems.ColorSensor;

import android.graphics.Color;

import com.qualcomm.hardware.lynx.LynxI2cColorRangeSensor;
import com.qualcomm.robotcore.hardware.ColorSensor;

/**
 * Created by Sarthak on 10/15/2017.
 */

public class LynxColorRangeSensor implements IColorSensor {

    //Create color sensor
    private LynxI2cColorRangeSensor color;

    //Constructor for color sensor
    public LynxColorRangeSensor(LynxI2cColorRangeSensor color){
        this.color = color;
    }

    /**
     * Returns the red value from the sensor reading
     * @return the red value as an integer
     */
    @Override
    public int red() {
        return color.red();
    }

    /**
     * Returns the green value from the sensor reading
     * @return the green value as an integer
     */
    @Override
    public int green() {
        return color.green();
    }

    /**
     * Returns the blue value from the sensor reading
     * @return the blue value as an integer
     */
    @Override
    public int blue() {
        return color.blue();
    }

    /**
     * Returns the hue value from the sensor reading
     * @return the hue as an integer
     */
    @Override
    public int getHue() {
        return color.argb();
    }

    /**
     * Collects the HSV (Hue, Saturation, Value) and returns all three values in the form of a float array
     * @return the HSV value within a float array
     */
    public float[] getHSV(){
        float[] hsv = {0F, 0F, 0F};
        Color.RGBToHSV(this.red(), this.green(), this.blue(), hsv);
        return hsv;
    }

    @Override
    public int alpha() {
        return color.alpha();
    }
}
