/* Copyright 2018 The MathWorks, Inc. */
package com.example.IMUControl;


import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import java.util.concurrent.TimeUnit;

import geometry_msgs.Pose;
import sensor_msgs.Imu;
import sensor_msgs.NavSatFix;

public class PublishNode extends AbstractNodeMain {
    private String topic_name;
    private String message_type;
    Publisher<Imu> publisher;
    public PublishNode(String topic_name, String message_type) {
        this.topic_name = topic_name;
        this.message_type = message_type;
    }

    public GraphName getDefaultNodeName() {
        return GraphName.of("rosindependent/publish_node");
    }

    public void onStart(ConnectedNode connectedNode) {
        publisher = connectedNode.newPublisher(this.topic_name, this.message_type);

    }
    public void publishData(float[] data) {
        if(publisher != null) {
            Imu imu = publisher.newMessage();
            imu.getHeader().setFrameId("imu");
            imu.getOrientation().setX(data[0]);
            imu.getOrientation().setY(data[1]);
            imu.getOrientation().setZ(data[2]);
            imu.getOrientation().setW(data[3]);
            imu.getLinearAcceleration().setX(data[4]);
            imu.getLinearAcceleration().setY(data[5]);
            imu.getLinearAcceleration().setZ(data[6]);
            imu.getAngularVelocity().setX(data[7]);
            imu.getAngularVelocity().setY(data[8]);
            imu.getAngularVelocity().setZ(data[9]);
            long msecs = System.currentTimeMillis();
            int secs = (int)(msecs/1000);//TimeUnit.MILLISECONDS.toSeconds(msecs);
            imu.getHeader().getStamp().secs = secs;
            imu.getHeader().getStamp().nsecs = (int)((msecs-secs*1000)*1000);
            publisher.publish(imu);
        }
    }
}

