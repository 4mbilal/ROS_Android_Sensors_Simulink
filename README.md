# ROS_Android_Sensors_Simulink
Implementation of a ROS node on Android to publish sensor and odometry messages. It publishes the following topics:  
/android/imu  
/android/gps  
/android/mag  
/android/temp  
/android/pressure  
/android/odom  

Originally dervied from a Simulink example which published only IMU data (Accelerometer, Gyro and Orientation).  
https://uk.mathworks.com/help/supportpkg/android/examples/visualize-sensor-data-from-an-android-device-in-rviz-example.html  
This version extends the support to all the commonly avaailable sensors on Smartphones i.e. IMU, GPS, Magnetometer, Pressure and Temperature.

The latest version also implements fusion of Orientation and Gyroscope sensors using Complementary Filter. A rough implementation of double integration of Linear Acceleration to generate dead-reckoning data is also implemented. This is, however, far from being accurate and should serve only as a reference. Anyone interested in extending the project to include Kalman Filtering, please contact me at 4mbilal@gmail.com.    
IMU is published at a rate of 100 Hz while all others at 1 Hz. The Simulink model 'ROS_Android_Sensors.slx' can be used to update the code.