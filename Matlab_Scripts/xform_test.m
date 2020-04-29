clear all
close all
clc

yaw = 0*pi/180;
roll = 0*pi/180;
pitch = 270*pi/180;

rz = [cos(yaw) -sin(yaw) 0;sin(yaw) cos(yaw) 0;0 0 1];
ry = [cos(pitch) 0 sin(pitch);0 1 0;-sin(pitch) 0 cos(pitch)];
rx = [1 0 0;0 cos(roll) -sin(roll);0 sin(roll) cos(roll)];
xform = (rz*ry*rx)    %forward rotation xform

Vxyz = [1;1;1];
dxyz = xform*Vxyz