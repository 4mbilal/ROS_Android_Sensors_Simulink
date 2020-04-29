close all
clear all
clc

rosshutdown
rosinit('192.168.1.17')
rostopic list

sub_imu = rossubscriber('/android/imu')
msg_imu = rosmessage(sub_imu);
sub_imu.NewMessageFcn = @(~,msg_imu) process_imu(msg_imu)

function process_imu(msg_imu)
N = 10;
    persistent AccX;
    persistent AccY;
    persistent AccZ;
    persistent OrX;
    persistent OrY;
    persistent OrZ;
    if(isempty(AccX))
        AccX = zeros(1,N);
        AccY = zeros(1,N);
        AccZ = zeros(1,N);
        OrX = zeros(1,N);
        OrY = zeros(1,N);
        OrZ = zeros(1,N);
    end
    AccX = circshift(AccX,-1);
    AccX(N) = msg_imu.LinearAcceleration.X;
    AccY = circshift(AccY,-1);
    AccY(N) = msg_imu.LinearAcceleration.Y;
    AccZ = circshift(AccZ,-1);
    AccZ(N) = msg_imu.LinearAcceleration.Z;
    
    Or = quat2eul([msg_imu.Orientation.X,msg_imu.Orientation.Y,msg_imu.Orientation.Z,msg_imu.Orientation.W]);
    OrX = circshift(OrX,-1);
    OrX(N) = Or(1)*180/pi;
    OrY = circshift(OrY,-1);
    OrY(N) = Or(2)*180/pi;
    OrZ = circshift(OrZ,-1);
    OrZ(N) = Or(3)*180/pi;

%     subplot(3,1,1)
%     plot(OrX);title('OrZ')
%     subplot(3,1,2)
%     plot(OrY);title('OrX')
%     subplot(3,1,3)
%     plot(OrZ);title('OrY')    
    subplot(3,1,1)
    plot(AccX);title('AccX')
    axis([0 N -10 10])
    subplot(3,1,2)
    plot(AccY);title('AccY')
    axis([0 N -10 10])
    subplot(3,1,3)
    plot(AccZ);title('AccZ')
    axis([0 N -10 10])
    drawnow
end