package com.example.ROS_Android_Sensors;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import sensor_msgs.Temperature;

public class PublishNodeTemp extends AbstractNodeMain {
    private String topic_name;
    private String message_type;
    Publisher<Temperature> publisher;
    public PublishNodeTemp(String topic_name, String message_type) {
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
            Temperature tmp = publisher.newMessage();
            tmp.getHeader().setFrameId("temp");
            tmp.setTemperature(data[6]);
            long msecs = System.currentTimeMillis();
            int secs = (int)(msecs/1000);//TimeUnit.MILLISECONDS.toSeconds(msecs);
            tmp.getHeader().getStamp().secs = secs;
            tmp.getHeader().getStamp().nsecs = (int)((msecs-secs*1000)*1000);
            publisher.publish(tmp);
        }
    }
}