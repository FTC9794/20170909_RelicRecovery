package org.firstinspires.ftc.teamcode.Autonomous;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.lynx.LynxI2cColorRangeSensor;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuMarkInstanceId;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.teamcode.Subsystems.ColorSensor.IColorSensor;
import org.firstinspires.ftc.teamcode.Subsystems.ColorSensor.LynxColorRangeSensor;
import org.firstinspires.ftc.teamcode.Subsystems.Drivetrain.OmniDirectionalDrive;
import org.firstinspires.ftc.teamcode.Subsystems.Glyph.DualWheelIntake;
import org.firstinspires.ftc.teamcode.Subsystems.IMU.BoschIMU;
import org.firstinspires.ftc.teamcode.Subsystems.IMU.IIMU;
import org.firstinspires.ftc.teamcode.Subsystems.Jewel.TwoPointJewelArm;
import org.firstinspires.ftc.teamcode.Subsystems.LED;
import org.firstinspires.ftc.teamcode.Subsystems.Relic.ClawThreePoint;
import org.firstinspires.ftc.teamcode.Subsystems.UltrasonicSensor.IUltrasonic;
import org.firstinspires.ftc.teamcode.Subsystems.UltrasonicSensor.MRRangeSensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sarthak on 1/24/2018.
 */
@Autonomous(name = "Red Stone 2 Java", group = "Autonomous")
public class RedStone2 extends LinearOpMode {

    BNO055IMU boschIMU;
    DualWheelIntake intake;
    ClawThreePoint relic;
    IColorSensor color;
    IColorSensor floor_color;
    TwoPointJewelArm jewel;
    MRRangeSensor jewel_us;
    MRRangeSensor back_us;
    LED led;

    VuforiaLocalizer vuforia;

    Servo pan, tilt;
    CRServo rightWheel1, rightWheel2, leftWheel1, leftWheel2;
    Servo spin;
    DcMotor rf, rb, lf, lb;
    List<DcMotor> motors;
    DcMotor lift;
    DcMotor relic_extension;
    Servo relic_claw, relic_arm, relic_tilt;
    DigitalChannel glyphLimit;
    LynxI2cColorRangeSensor lynx, lynx_floor;
    IIMU imu;
    OmniDirectionalDrive drive;
    ModernRoboticsI2cRangeSensor ultrasonic_jewel;
    ModernRoboticsI2cRangeSensor ultrasonic_back;
    DcMotor leds;

    ElapsedTime timer;
    String vumarkSeen = "";
    double vuMarkDistance = 36;

    final double GRIP_OPEN1 = .5;
    final double GRIP_OPEN2 = .5;
    final double GRIP_CLOSE1 = 0;
    final double GRIP_CLOSE2 = 0;

    final double SPIN_START = 0;
    final double SPIN_ROTATED = .95;

    final double RELIC_CLAW_CLOSED = 1;
    final double RELIC_CLAW_OPENED = 0;

    final double RELIC_TILT_ORIGIN = 1;

    final double RELIC_ARM_ORIGIN = 0;

    final double COUNTS_PER_INCH = 45;
    final double ENCODER_OFFSET = 30;

    double imuAngle, encoderAverage, powerChange = 0;

    final int BLUE_LINE_VALUE = 150;
    final double POWER_CHANGE_GAIN = 0.003;

    final double LIFT_POWER_UP = 1;
    final double LIFT_POWER_DOWN = -1;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addData("Init", "Starting Vuforia");
        telemetry.update();
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);

        // OR...  Do Not Activate the Camera Monitor View, to save power
        // VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        /*
         * IMPORTANT: You need to obtain your own license key to use Vuforia. The string below with which
         * 'parameters.vuforiaLicenseKey' is initialized is for illustration only, and will not function.
         * A Vuforia 'Development' license key, can be obtained free of charge from the Vuforia developer
         * web site at https://developer.vuforia.com/license-manager.
         *
         * Vuforia license keys are always 380 characters long, and look as if they contain mostly
         * random data. As an example, here is a example of a fragment of a valid key:
         *      ... yIgIzTqZ4mWjk9wd3cZO9T1axEqzuhxoGlfOOI2dRzKS4T0hQ8kT ...
         * Once you've obtained a license key, copy the string from the Vuforia web site
         * and paste it in to your code onthe next line, between the double quotes.
         */
        parameters.vuforiaLicenseKey = "ATXxmRr/////AAAAGdfeAuU6SEoFkpmhG616inkbnBHHQ/Ti5DMPAVykTBdmQS8ImGtoIBRRuboa+oIyuvQW1nIychXXxROjGLssEzSFF8yOYE36GqhVtRfI6lw8/HAoJpO1XgIF5Gy1vPx4KFPNInK6CJdZomYyWV8rGnb7ceLJ9Z+g0sl+VcVPKl5DAI84K+06pEZnw+Em7sThhzyzj2p4QbPhXh7fEtNGhFCqey9rcg3h9RfNebyWvJW9z7mGkaJljZy1x3lK7viLbFKyFcAaspZZi1+JzUmeuXxV0r+8hrCgFLPsvKQHlnYumazP9FEtm/FjCpRFF23Et77325/vuD2LRSPzve9ef4zqe6MivrLs9s8lUgd7Eo9W";

        /*
         * We also indicate which camera on the RC that we wish to use.
         * Here we chose the back (HiRes) camera (for greater range), but
         * for a competition robot, the front camera might be more convenient.
         */
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.FRONT;
        this.vuforia = ClassFactory.createVuforiaLocalizer(parameters);

        /**
         * Load the data set containing the VuMarks for Relic Recovery. There's only one trackable
         * in this data set: all three of the VuMarks in the game were created from this one template,
         * but differ in their instance id information.
         * @see VuMarkInstanceId
         */
        VuforiaTrackables relicTrackables = this.vuforia.loadTrackablesFromAsset("RelicVuMark");
        VuforiaTrackable relicTemplate = relicTrackables.get(0);
        relicTemplate.setName("relicVuMarkTemplate"); // can help in debugging; otherwise not necessary

        telemetry.addData("Init", "Finished Starting Vuforia");
        telemetry.update();

        //initialize Right Motors
        rf = hardwareMap.dcMotor.get("right_front");
        rb = hardwareMap.dcMotor.get("right_back");

        //initialize left motors
        lf = hardwareMap.dcMotor.get("left_front");
        lb = hardwareMap.dcMotor.get("left_back");
        rf.setDirection(DcMotorSimple.Direction.REVERSE);
        rb.setDirection(DcMotorSimple.Direction.REVERSE);
        lf.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lb.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rf.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rb.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lift = hardwareMap.dcMotor.get("glyph_lift");

        lynx = (LynxI2cColorRangeSensor) hardwareMap.get("jewel_color");
        lynx_floor = (LynxI2cColorRangeSensor) hardwareMap.get("floor_color");
        pan = hardwareMap.servo.get("jewel_pan");
        tilt = hardwareMap.servo.get("jewel_tilt");

        rightWheel1 = hardwareMap.crservo.get("right_glyph1");
        leftWheel1 = hardwareMap.crservo.get("left_glyph1");
        //leftWheel1.setDirection(DcMotorSimple.Direction.REVERSE);
        rightWheel2 = hardwareMap.crservo.get("right_glyph2");
        leftWheel2 = hardwareMap.crservo.get("left_glyph2");

        spin = hardwareMap.servo.get("spin_grip");

        relic_extension = hardwareMap.dcMotor.get("relic_extension");
        relic_extension.setDirection(DcMotorSimple.Direction.REVERSE);
        relic_extension.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        relic_extension.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        relic_extension.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        relic_extension.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        relic_arm = hardwareMap.servo.get("relic_arm");
        relic_claw = hardwareMap.servo.get("relic_claw");
        relic_tilt = hardwareMap.servo.get("relic_tilt");


        color = new LynxColorRangeSensor(lynx);
        floor_color = new LynxColorRangeSensor(lynx_floor);
        jewel = new TwoPointJewelArm(pan, tilt, color, telemetry);
        relic = new ClawThreePoint(relic_extension, relic_arm, relic_tilt, relic_claw);
        relic.setTiltPosition(1);
        telemetry.addData("Init", "Jewel, Relic Hardware Initialized");
        telemetry.update();
        jewel.setPanTiltPos(0.5, 1);
        telemetry.addData("Init", "Jewel Servos Set");
        telemetry.update();

        lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        glyphLimit = hardwareMap.digitalChannel.get("glyph_limit");
        intake = new DualWheelIntake(rightWheel1, rightWheel2, leftWheel1, leftWheel2, spin, lift, glyphLimit, telemetry);
        lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        telemetry.addData("Init", "Initialized Intake System");
        telemetry.update();

        //create array list of motors
        motors = new ArrayList<>();
        motors.add(rf);
        motors.add(rb);
        motors.add(lf);
        motors.add(lb);


        leds = hardwareMap.dcMotor.get("leds");
        led = new LED(leds);
        /*boolean aligned = false;
        ultrasonic_jewel = (ModernRoboticsI2cRangeSensor) hardwareMap.get("jewel_us");
        jewel_us = new MRRangeSensor(ultrasonic_jewel);
        while(!aligned){
            telemetry.addData("Jewel Ultrasonic", ultrasonic_jewel.cmUltrasonic());
            if(ultrasonic_back.cmUltrasonic() == 37){
                telemetry.addData("Alinged", "True");
                led.turnOn();
            }else{
                telemetry.addData("Aligned", "False");
                led.turnOff();
            }
            telemetry.update();
            if(isStopRequested()){
                aligned = true;
            }else if(gamepad1.a || gamepad2.a){
                aligned = true;
            }
            telemetry.update();
        }*/

        boolean aligned = false;
        ultrasonic_jewel = (ModernRoboticsI2cRangeSensor) hardwareMap.get("jewel_us");
        ultrasonic_back = (ModernRoboticsI2cRangeSensor) hardwareMap.get("back_us");
        while(!aligned){
            telemetry.addData("Jewel US", ultrasonic_jewel.cmUltrasonic());
            if(ultrasonic_jewel.cmUltrasonic() == 36){
                led.turnOn();
            }else{
                telemetry.addData("Aligned", "False");
                led.turnOff();
            }
            telemetry.update();
            if(isStopRequested()){
                aligned = true;
            }else if(gamepad1.a || gamepad2.a){
                aligned = true;
            }
            telemetry.update();
        }
        jewel_us = new MRRangeSensor(ultrasonic_jewel);
        led.turnOff();
        telemetry.update();

        //set motor modes and zero power behavior
        for (DcMotor motor : motors) {
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }
        telemetry.addData("Init", "Set Drivetrain mode");
        telemetry.update();

        //Calibrate IMU
        telemetry.addData("Init", "IMU Calibrating");
        telemetry.update();
        boschIMU = hardwareMap.get(BNO055IMU.class, "imu");
        imu = new BoschIMU(boschIMU);
        imu.initialize();
        imu.setOffset(0);
        telemetry.addData("Init", "IMU Instantiated");
        telemetry.update();

        //initialize drivetrain
        drive = new OmniDirectionalDrive(motors, imu, telemetry);
        drive.resetEncoders();
        telemetry.addData("Init", "Drivetrain and IMU Initialized");
        telemetry.update();

        //Finish init
        timer = new ElapsedTime();
        telemetry.addData("Init", "Timer Initialized");
        telemetry.addData("Init", "Completed");
        telemetry.update();
        led.setLEDPower(0.5);
        waitForStart();

        lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        led.turnOff();
        relicTrackables.activate();
        timer.reset();
        //Reset drive encoders
        drive.resetEncoders();

        //Intake Glyph and raise lift
        intake.secureGlyph();
        intake.setLiftTargetPosition(100, 1);

        //Move jewel to position and read color
        jewel.setPanTiltPos(0.5, 0.21);
        timer.reset();
        while(timer.milliseconds() < 1000){
            telemetry.addData("Jewel", "Moving to Read Position");
            telemetry.addData("Timer", timer.milliseconds());
            telemetry.update();
        }
        jewel.readColor(5);
        intake.setLiftTargetPosition(500, 1);
        //Knock off jewel
        jewel.knockOffJewel("red");
        jewel.setPanTiltPos(0.5, 1);
        telemetry.addData("Jewel", "Done");

        //Read VuMark and determine drive distance and column
        telemetry.addData("VuMark", "Reading");
        telemetry.update();
        RelicRecoveryVuMark vuMark = RelicRecoveryVuMark.from(relicTemplate);
        if (vuMark != RelicRecoveryVuMark.UNKNOWN) {

                /* Found an instance of the template. In the actual game, you will probably
                 * loop until this condition occurs, then move on to act accordingly depending
                 * on which VuMark was visible. */
            telemetry.addData("VuMark", vuMark.toString());
            telemetry.update();
            vumarkSeen = vuMark.toString();

                /* For fun, we also exhibit the navigational pose. In the Relic Recovery game,
                 * it is perhaps unlikely that you will actually need to act on this pose information, but
                 * we illustrate it nevertheless, for completeness. */
            OpenGLMatrix pose = ((VuforiaTrackableDefaultListener)relicTemplate.getListener()).getPose();

                /* We further illustrate how to decompose the pose into useful rotational and
                 * translational components */
            if (pose != null) {
                            /*

                             */
            }
        }
        //Determine VuMark distances
        if(vumarkSeen.equals("LEFT")){
            vuMarkDistance = 25;
        }else if (vumarkSeen.equals("RIGHT")){
            vuMarkDistance = 10;
        }else {
            vuMarkDistance = 17;
        }
        telemetry.addData("VuMark", "Finished");
        telemetry.update();

        //Drive off of balancing stone
        while(drive.averageEncoders() < 7*COUNTS_PER_INCH && opModeIsActive()){
            drive.moveNoIMU(0, 0.4, true, 0);
            telemetry.addData("Encoder Count", encoderAverage);
            telemetry.update();
        }
        drive.softResetEncoder();

        //Drive to desired cryptobox column
        powerChange = (vuMarkDistance*COUNTS_PER_INCH) - drive.averageEncoders();
        while(drive.averageEncoders() < vuMarkDistance*COUNTS_PER_INCH && opModeIsActive()){
            drive.moveIMU(0.7, 0.5, powerChange, POWER_CHANGE_GAIN, -90, 0.008, 0.001, 0,
                    false, 1000);
            powerChange = (vuMarkDistance*COUNTS_PER_INCH) - drive.averageEncoders();
        }
        drive.setPowerZero();
        drive.softResetEncoder();

        //Lower lift to deposit glyph
        lift.setTargetPosition(100);
        lift.setPower(1);
        timer.reset();
        while(timer.milliseconds() < 500){
            //Wait for lift
        }

        //Drive to deposit glyph
        powerChange = (3*COUNTS_PER_INCH) - drive.averageEncoders();
        while(drive.averageEncoders() < 3*COUNTS_PER_INCH && opModeIsActive()){
            drive.moveIMU(0.3, 0.1, powerChange, POWER_CHANGE_GAIN, 0, 0.008, 0.001, 0,
                    false, 1000);
            powerChange = (3*COUNTS_PER_INCH) - drive.averageEncoders();
        }
        drive.setPowerZero();
        drive.softResetEncoder();

        //Deposit glyph
        intake.dispenseGlyph();
        timer.reset();
        while(timer.milliseconds() < 1500){
            //Wait for glyph to be scored
        }

        //Back away from cryptobox
        powerChange = (5*COUNTS_PER_INCH) - drive.averageEncoders();
        while(drive.averageEncoders() < 5*COUNTS_PER_INCH && opModeIsActive()){
            drive.moveIMU(0.3, 0.1, powerChange, POWER_CHANGE_GAIN, 180, 0.008, 0.001, 0,
                    false, 1000);
            powerChange = (5*COUNTS_PER_INCH) - drive.averageEncoders();
        }
        drive.setPowerZero();
        drive.softResetEncoder();
        intake.setIntakePowerZero();

        //Slide towards middle of the field
        powerChange = (152.4 - jewel_us.cmDistance());
        while(jewel_us.cmDistance() < 152.4 && opModeIsActive()){
            drive.moveIMU(0.7, 0.5, powerChange, POWER_CHANGE_GAIN, -90, 0.008, 0.001, 0,
                    false, 1000);
            powerChange = (152.4 - jewel_us.cmDistance());
        }
        drive.setPowerZero();
        drive.softResetEncoder();

        //Pivot to face glyph pit
        while(drive.moveIMU(0.5, 0.2, 0, 0, 0, 0, 0.005, -140, true, 500) && opModeIsActive()){
            telemetry.addData("Move", "Pivot");
            telemetry.update();
        }
        drive.setPowerZero();
        drive.softResetEncoder();

        //Slide to glyph pit
        lift.setTargetPosition(5);
        lift.setPower(1);
        powerChange = (43*COUNTS_PER_INCH) - drive.averageEncoders();
        while(drive.averageEncoders() < 43*COUNTS_PER_INCH && opModeIsActive()){
            drive.moveIMU(0.6, 0.5, powerChange, POWER_CHANGE_GAIN, 180, 0.008, 0.001, -140,
                    false, 1000);
            powerChange = (43*COUNTS_PER_INCH) - drive.averageEncoders();
        }
        drive.setPowerZero();
        drive.softResetEncoder();

        //Intake glyph
        intake.secureGlyph();
        powerChange = (24*COUNTS_PER_INCH - drive.averageEncoders());
        timer.reset();
        while(floor_color.red() < 50 && opModeIsActive() && timer.milliseconds() < 1500){
            drive.moveIMU(0.4, 0.3, powerChange, POWER_CHANGE_GAIN, -140, 0.008, 0.001, -140,
                    false, 1000);
            powerChange = (24*COUNTS_PER_INCH) - drive.averageEncoders();
        }
        timer.reset();
        while(drive.averageEncoders() < 30 * COUNTS_PER_INCH && opModeIsActive() && timer.milliseconds() < 750){
            drive.moveIMU(0.6, 0.5, powerChange, POWER_CHANGE_GAIN, -140, 0.008, 0.001, -140,
                    false, 1000);
            powerChange = (30*COUNTS_PER_INCH) - drive.averageEncoders();
        }
        timer.reset();
        drive.setPowerZero();
        drive.softResetEncoder();
        while(timer.milliseconds() < 500){

        }

        //Back away from glyph pit
        lift.setTargetPosition(75);
        lift.setPower(1);
        powerChange = (30*COUNTS_PER_INCH - drive.averageEncoders());
        while(drive.averageEncoders() < 30 * COUNTS_PER_INCH && opModeIsActive() && timer.milliseconds() < 750){
            drive.moveIMU(0.4, 0.3, powerChange, POWER_CHANGE_GAIN, 40, 0.008, 0.001, -140,
                    false, 1000);
            powerChange = (30*COUNTS_PER_INCH) - drive.averageEncoders();
        }
        drive.setPowerZero();
        drive.softResetEncoder();

        //Pivot to face cryptobox wall
        while(drive.moveIMU(0.5, 0.2, 0, 0, 0, 0, 0.005, 0, true, 500) && opModeIsActive()){
            telemetry.addData("Move", "Pivot");
            telemetry.update();
        }
        drive.setPowerZero();
        drive.softResetEncoder();

        //Drive towards cryptobox
        powerChange = (40*COUNTS_PER_INCH - drive.averageEncoders());
        while(drive.averageEncoders() < 40 * COUNTS_PER_INCH && opModeIsActive()){
            drive.moveIMU(0.3, 0.2, powerChange, POWER_CHANGE_GAIN, 0, 0.008, 0.001, 0,
                    false, 1000);
            powerChange = (40*COUNTS_PER_INCH) - drive.averageEncoders();
        }
        drive.softResetEncoder();
        lift.setTargetPosition(500);
        lift.setPower(1);

        //Slide to cryptobox column
        powerChange = (26*COUNTS_PER_INCH) - drive.averageEncoders();
        while(drive.averageEncoders() < 26*COUNTS_PER_INCH && opModeIsActive()){
            drive.moveIMU(0.5, 0.4, powerChange, POWER_CHANGE_GAIN, 90, 0.008, 0.001, 0,
                    false, 1000);
            powerChange = (26*COUNTS_PER_INCH) - drive.averageEncoders();
        }
        drive.setPowerZero();
        drive.softResetEncoder();

        //Deposit Glyph
        powerChange = (3*COUNTS_PER_INCH) - drive.averageEncoders();
        while(drive.averageEncoders() < 3*COUNTS_PER_INCH && opModeIsActive()){
            drive.moveIMU(0.3, 0.1, powerChange, POWER_CHANGE_GAIN, 0, 0.008, 0.001, 0,
                    false, 1000);
            powerChange = (3*COUNTS_PER_INCH) - drive.averageEncoders();
        }
        drive.setPowerZero();
        drive.softResetEncoder();

        //Deposit glyph
        intake.dispenseGlyph();
        timer.reset();
        while(timer.milliseconds() < 1500){
            //Wait for glyph to be scored
        }

        //Back away from cryptobox
        powerChange = (5*COUNTS_PER_INCH) - drive.averageEncoders();
        while(drive.averageEncoders() < 5*COUNTS_PER_INCH && opModeIsActive()){
            drive.moveIMU(0.3, 0.1, powerChange, POWER_CHANGE_GAIN, 180, 0.008, 0.001, 0,
                    false, 1000);
            powerChange = (5*COUNTS_PER_INCH) - drive.averageEncoders();
        }
        drive.setPowerZero();
        drive.softResetEncoder();


        //End of program
        while(opModeIsActive()){
            telemetry.addData("Program", "Finished");
            telemetry.addData("VuMark Seen", vumarkSeen);
            telemetry.update();
        }
    }
}