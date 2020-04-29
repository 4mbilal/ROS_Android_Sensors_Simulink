clear all
close all
clc

rosshutdown
rosinit('192.168.1.17')
rostopic list

sub_imu = rossubscriber('/android/imu');

sub_imu.NewMessageFcn = @(~,msg_imu) imu_process(msg_imu)


function imu_process(msg_imu)
persistent time_prev;
persistent yaw;
persistent cnt;
if isempty(yaw)
    yaw = 0;
end
if isempty(cnt)
    cnt = 0;
end
time_now = msg_imu.Header.Stamp.Sec*1e9 + msg_imu.Header.Stamp.Nsec;
dt = time_now-time_prev;
eul = quat2eul([msg_imu.Orientation.X msg_imu.Orientation.Y msg_imu.Orientation.Z msg_imu.Orientation.W]);
yaw = wrapTo360(yaw + msg_imu.AngularVelocity.Z*dt*1e-9*180/pi);
time_prev = msg_imu.Header.Stamp.Sec*1e9 + msg_imu.Header.Stamp.Nsec;
cnt = cnt + 1;
% disp(yaw)
if(cnt>10)
    cnt = 0;
    disp(yaw)
end
end