# ROS_Android_Sensors_Simulink
Implementation of a ROS node on Android for all the commonly available sensors. Derived from Simulink example

Mathworks has provided an example to create a ROS node for Android platform in Simulink. The example, however, publishes only IMU data (Accelerometer, Gyro and Orientation).
https://uk.mathworks.com/help/supportpkg/android/examples/visualize-sensor-data-from-an-android-device-in-rviz-example.html
This is an extension of this example to access all the commonly avaailable sensors on Smartphones i.e. IMU, GPS, Magnetometer, Pressure and Temperature.
It publishes the following topics:

/android/imu
/android/gps
/android/mag
/android/temp
android/pressure

IMU is published at a rate of 100 Hz while all others at 1 Hz. The Simulink model 'ROS_Android_Sensors.slx' can be used to update the code.