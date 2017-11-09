package org.firstinspires.ftc.teamcode.Subsystems.Relic;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Created by Sarthak on 11/8/2017.
 */

public class ClawThreePoint implements IRelic {
    private DcMotor relic_extension;
    private Servo arm, tilt, claw;
    private Telemetry telemetry;

    private double relicArmAngle = 0;
    private double relicTiltPos = 0;
    private double tiltOffset = 0.095;

    private final double RELIC_CLAW_CLOSED = 1;
    private final double RELIC_CLAW_OPENED = 0;

    private final double RELIC_TILT_ORIGIN = 1;

    private final double RELIC_ARM_ORIGIN = 0;
    private final double RELIC_ARM_GRAB_POS = 0.96;

    public ClawThreePoint(DcMotor extension, Servo arm, Servo tilt, Servo claw, Telemetry telemetry){
        this.relic_extension = extension;
        this.arm = arm;
        this.tilt = tilt;
        this.claw = claw;
        this.telemetry = telemetry;
        this.claw.setPosition(RELIC_CLAW_CLOSED);
        this.arm.setPosition(RELIC_ARM_ORIGIN);
        this.tilt.setPosition(RELIC_TILT_ORIGIN);
        this.relic_extension.setDirection(DcMotorSimple.Direction.REVERSE);
        relic_extension.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        relic_extension.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        relic_extension.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        relic_extension.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    @Override
    public void pickUpRelic() {
        claw.setPosition(RELIC_CLAW_CLOSED);
    }

    @Override
    public void releaseRelic() {
        claw.setPosition(RELIC_CLAW_OPENED);
    }

    public void adjustArm(boolean condition, double minPos, double maxPos, double increment){
        if(condition){
            arm.setPosition(arm.getPosition() + increment);
            if(arm.getPosition() > 0.74) {
                relicArmAngle = (arm.getPosition() - 0.74) / ((0.9 - .74) / 45);
                relicTiltPos = ((180-relicArmAngle) * (0.005)) + tiltOffset;
                tilt.setPosition(relicTiltPos);
            }
        }
    }

    public double returnArmAngle(){
        return relicArmAngle;
    }

    public double returnTiltPos(){
        return relicTiltPos;
    }

    public void tiltRelic(boolean condition, double minPos, double maxPos, double increment){
        if(condition){
            tilt.setPosition(tilt.getPosition() + increment);
        }
    }

    @Override
    public void extend(double power, boolean condition) {
        if(condition) {
            relic_extension.setPower(power);
        }else{
            relic_extension.setPower(0);
        }
    }

    @Override
    public void retract(double power, boolean condition) {
        if(condition) {
            relic_extension.setPower(power);
        }else{
            relic_extension.setPower(0);
        }
    }
}
