package com.example.IMUControl;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import geometry_msgs.Pose;
import sensor_msgs.Imu;
import sensor_msgs.NavSatFix;

public class PublishNodeGPS extends AbstractNodeMain {
    private String topic_name;
    private String message_type;
    Publisher<NavSatFix> publisher;
    public PublishNodeGPS(String topic_name, String message_type) {
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
            NavSatFix nsf = publisher.newMessage();
            nsf.getHeader().setFrameId("gps");
            nsf.setLatitude(data[10]);
            nsf.setLongitude(data[11]);
            nsf.setAltitude(data[12]);
            long msecs = System.currentTimeMillis();
            int secs = (int)(msecs/1000);//TimeUnit.MILLISECONDS.toSeconds(msecs);
            nsf.getHeader().getStamp().secs = secs;
            nsf.getHeader().getStamp().nsecs = (int)((msecs-secs*1000)*1000);
            publisher.publish(nsf);
        }
    }
}