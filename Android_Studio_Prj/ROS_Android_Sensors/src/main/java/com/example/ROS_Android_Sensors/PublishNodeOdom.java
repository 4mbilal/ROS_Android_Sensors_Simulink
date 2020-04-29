package com.example.ROS_Android_Sensors;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import geometry_msgs.Point;
import geometry_msgs.Quaternion;
import geometry_msgs.Twist;
import nav_msgs.Odometry;


public class PublishNodeOdom extends AbstractNodeMain {
    private String topic_name;
    private String message_type;
    Publisher<Odometry> publisher;
    public PublishNodeOdom(String topic_name, String message_type) {
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
            Odometry odom = publisher.newMessage();
            odom.getHeader().setFrameId("odometry");

            Point position = odom.getPose().getPose().getPosition();
            Quaternion orientation = odom.getPose().getPose().getOrientation();
            Twist twist = odom.getTwist().getTwist();
            position.setX(data[13]);
            position.setY(data[14]);
            position.setZ(data[15]);
            orientation.setX(data[0]);
            orientation.setY(data[1]);
            orientation.setZ(data[2]);
            orientation.setW(data[3]);
            twist.getLinear().setX(data[16]);
            twist.getLinear().setY(data[17]);
            twist.getLinear().setZ(data[18]);
            twist.getAngular().setX(data[7]);
            twist.getAngular().setY(data[8]);
            twist.getAngular().setZ(data[9]);

            long msecs = System.currentTimeMillis();
            int secs = (int)(msecs/1000);//TimeUnit.MILLISECONDS.toSeconds(msecs);
            odom.getHeader().getStamp().secs = secs;
            odom.getHeader().getStamp().nsecs = (int)((msecs-secs*1000)*1000);
            publisher.publish(odom);
        }
    }
}