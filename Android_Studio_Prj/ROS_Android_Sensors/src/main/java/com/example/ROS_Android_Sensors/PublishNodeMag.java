package com.example.ROS_Android_Sensors;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import sensor_msgs.MagneticField;

public class PublishNodeMag extends AbstractNodeMain {
    private String topic_name;
    private String message_type;
    Publisher<MagneticField> publisher;
    public PublishNodeMag(String topic_name, String message_type) {
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
            MagneticField mag = publisher.newMessage();
            mag.getHeader().setFrameId("magnet");
            mag.getMagneticField().setX(data[10]);
            mag.getMagneticField().setY(data[11]);
            mag.getMagneticField().setZ(data[12]);
            long msecs = System.currentTimeMillis();
            int secs = (int)(msecs/1000);//TimeUnit.MILLISECONDS.toSeconds(msecs);
            mag.getHeader().getStamp().secs = secs;
            mag.getHeader().getStamp().nsecs = (int)((msecs-secs*1000)*1000);
            publisher.publish(mag);
        }
    }
}