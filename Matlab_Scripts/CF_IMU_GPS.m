clear all
close all
clc
pause(2)
clear all

% To-Do:
% 1- Continuously monitor average value of 'g'. initial 10 sec data is not sufficient.
% 2- Filter LA.

% bearing = GetBearing(21.538032, 39.216676,21.537481, 39.216789)*180/pi
% pause
% 1- Estimating position/velocity using dead-reckoning only is "impossible".
% The best-case error estimate will be in the ball-park figure of tens of meters
% within seconds. This is mainly due to miscalculation of gravity vector
% and linear acceleration. Accelerometer confuses actual linear acceleration with tilts!
% 2- Pitch and Roll can be nicely estimated by fusing Accelero and Gyro. Magneto gives horrible
% errors at times especially close to the floor. 
% 3- Fusing with GPS and/or wheel encoders is a must for accuracy.

% Axis Convention
% x forward (1-roll)
% y left    (2-pitch)
% z up      (3-yaw)
% x east, y north
% Yaw=zero when pointing east
[Acc,Gyr,Mag,GPS,Or,Or_fused,P,T,ts_f,ts_s,XYZ,Vxyz,LA,G] = Read_Log('1588080620982_Sensor_Log.csv');

Or_fused_ref = zeros(size(Or));
LA_ref = zeros(size(LA));
G_ref = zeros(size(G));
g = sqrt(sum(mean(G(1:750,:),1).^2));
XYZ_ref = zeros(size(XYZ));
Vxyz_ref = zeros(size(Vxyz));

i = 2;
k = 2;
dt = 0;
xform_p = eye(3);

while(i<=length(Gyr))
    dt = ts_f(i) - ts_f(i-1);
%1- Estimate current orientation through fusion
%   Fusion function works alright on PC but the same function on Android has a rolloever effect around 90 degrees.
    Or_fused_ref(i,:) = fuse_Or(Gyr(i,:)',Or(i,:)')';
    xform_n = rot_mat(Or_fused_ref(i,:));
%2- Find Linear Acceleration after compensating for gravity vector
% Rotation matrix seems to remove gravity quite effectively at small angles and moderate speeds.
% initial estimate of 'g' during first 10 sec is not sufficient. need to keep on averaging to find better estimate.
%     g = g*0.99 + sqrt(sum(Acc(i,:).^2))*0.01;
    [a,b] = removeG(xform_n,Acc(i,:)',g);
    LA_ref(i,:) = a';
    G_ref(i,:) = b';
% 3- Estimate dead-reckoning odometry (position+velocity)
    [c,d] = XYZ_UVW(xform_p,LA_ref(i-1,:)');
    XYZ_ref(i,:) = c';
    Vxyz_ref(i,:) = d';
    xform_p = xform_n;
    i = i + 1;
end

draw_map(GPS,XYZ_ref,XYZ);

Or_fused = quat2eul(Or_fused);
figure
plot(Or_fused_ref(:,1)*180/pi)
hold on
plot(Or(:,1)*180/pi)
plot(Or_fused(:,2)*180/pi)
title('roll')
figure
plot(Or_fused_ref(:,2)*180/pi)
hold on
plot(Or(:,2)*180/pi)
plot(Or_fused(:,3)*180/pi)
title('pitch')
figure
plot(Or_fused_ref(:,3)*180/pi)
hold on
plot(Or(:,3)*180/pi)
plot(Or_fused(:,1)*180/pi)
title('yaw')

figure
plot(G)
hold on
plot(G_ref)
title('G-vector')

figure
plot(LA_ref(:,2))
hold on
plot(LA(:,2))
plot(G(:,2))
plot(Acc(:,2))
title('Linear Acceleration Y')
figure
plot(LA_ref(:,1))
hold on
plot(LA(:,1))
plot(G(:,1))
plot(Acc(:,1))
title('Linear Acceleration X')
figure
plot(LA_ref(:,3))
hold on
plot(LA(:,3))
plot(G(:,3))
plot(Acc(:,3))
title('Linear Acceleration Z')
figure
plot(XYZ)
hold on
plot(XYZ_ref)
title('XYZ')
figure
plot(Vxyz)
hold on
plot(Vxyz_ref)
title('Linear Velocity')

function draw_map(GPS,XYZ_ref,XYZ)
    orig = [350,350];
    s = [700,700,3];
    map = uint8(zeros(s));
    % Adjustable parameters
    scale_px = 1;
    scale_px_m = 1; %pixels per meter
    [scale_m_lat,scale_m_lon] = GetLatLonScales(GPS(1,1)*pi/180);
%     110724
%     103593

    for k=1:length(GPS)
        xyz_gps = (GPS(k,:)-GPS(1,:));
        xyz_gps(1) = -xyz_gps(1)*scale_m_lat;
        xyz_gps(2)= xyz_gps(2)*scale_m_lon;
        i1 = round(xyz_gps(1)*scale_px_m*scale_px+orig(1));
        i2 = round(xyz_gps(2)*scale_px_m*scale_px+orig(2));
        i1 = min(max(i1,1),s(1));
        i2 = min(max(i2,1),s(2));
%         map = insertShape(map,'FilledCircle',[i2 i1 3],'LineWidth',1,'Color','Green');
        map(i1,i2,:)=[255,255,255];
%         if(rem(k,100)==0)
%             imshow(map)
%             drawnow
%             pause(0.3)
%         end
    end
    
    for i=1:length(XYZ_ref)
        xyz = XYZ_ref(i,:);
        i1 = round(-xyz(1)*scale_px_m*scale_px+orig(1));
        i2 = round(-xyz(2)*scale_px_m*scale_px+orig(2));
        i1 = min(max(i1,1),s(1));
        i2 = min(max(i2,1),s(2));    
%         map = insertShape(map,'FilledCircle',[i1 i2 1],'LineWidth',1,'Color','Red');
        map(i1,i2,:)=[255,0,0];
%         if(rem(i,100)==0)
%             imshow(map)
%             drawnow
%             pause(0.3)
%         end        
    end
    
    for i=1:length(XYZ)
        xyz = XYZ(i,:);
        i1 = round(-xyz(1)*scale_px_m*scale_px+orig(1));
        i2 = round(-xyz(2)*scale_px_m*scale_px+orig(2));
        i1 = min(max(i1,1),s(1));
        i2 = min(max(i2,1),s(2));
        map(i1,i2,:)=[255,0,255];

    end

    imshow(map)
    
    
end

function LA = LA_filter(LA)
    persistent la;
    if(isempty(la))
        la = double([0;0;0]);
    end
    c = double(0.000125);
    la = LA*c+la*(1-c);
    LA = la;
end

function [XYZ,Vxyz] = XYZ_UVW(xform,LA)
persistent xyz; %Position
persistent uvw; %Velocity
    if(isempty(xyz))
        xyz = double([0;0;0]);
        uvw = double([0;0;0]);
    end

% xform = eye(3); %enable for testing only
% LA = [0;0;0];
% LA(1) = 0.1;
% if(abs(LA(1))<0.75) LA(1)=0;end
% if(abs(LA(2))<0.75) LA(2)=0;end
% if(abs(LA(3))<0.75) LA(3)=0;end
%     LA = LA_filter(LA);

XYZ = xyz + (xform*uvw*0.01);  %Update position vector using previous velocity vector
xyz = XYZ;

Vxyz = uvw + LA*0.01;   %Update velocity vector
uvw = Vxyz;

end


function [LA,G] = removeG(xform,Acc,g)
%     persistent A_g;
%         if(isempty(A_g))
%             A_g = double([0;0;9.8]);
%         end
%         c = 0.995;
%     A_g = A_g*c + Acc*(1-c); 
    G = xform'*[0;0;g];
    LA = -(G+Acc);
end

function xform = rot_mat(Or)
    roll = double(Or(1));
    pitch = double(Or(2));
    yaw = double(Or(3));
    
    rz = [cos(yaw) -sin(yaw) 0;sin(yaw) cos(yaw) 0;0 0 1];
    ry = [cos(pitch) 0 sin(pitch);0 1 0;-sin(pitch) 0 cos(pitch)];
    rx = [1 0 0;0 cos(roll) -sin(roll);0 sin(roll) cos(roll)];
    xform = (rz*ry*rx);    %Rotation Matrix
end

function Or_o = fuse_Or(Gyr,Or_i)
    persistent Or_p;
        if(isempty(Or_p))
            Or_p = double([0;0;0]);
        end

    fuse_c = double(0.9); %[0 1] 0-Acc, 1-Gyro
    dt = double(0.01);%Fixed delta-time

    % Complementary Filter
    Or_o = fuse_angles(Or_p + Gyr*dt,Or_i,fuse_c,1-fuse_c);
    Or_p = Or_o;
end

function out = fuse_angles(a,b,aw,bw)
    ca = cos(a);
    cb = cos(b);
    sa = sin(a);
    sb = sin(b);
    co = ca*aw+cb*bw;
    so = sa*aw+sb*bw;
    out = atan2(so,co);
end

function [Sc_lat,Sc_lon] = GetLatLonScales(lat)
    m1 = 111132.92;
    m2 = -559.82;
    m3 = 1.175;
    m4 = -0.0023;
    p1 = 111412.84;
    p2 = -93.5;
    p3 = 0.118;

    Sc_lat = (m1 + (m2 * cos(2 * lat)) + (m3 * cos(4 * lat)) + (m4 * cos(6 * lat)))/1;
    Sc_lon = ((p1 * cos(lat)) + (p2 * cos(3 * lat)) + (p3 * cos(5 * lat)))/1;
end

function bearing = GetBearing(lat1,lon1,lat2,lon2)
    bearing = atan2(cos(lat1)*sin(lat2)-sin(lat1)*cos(lat2)*cos(lon2-lon1),sin(lon2-lon1)*cos(lat2));
end