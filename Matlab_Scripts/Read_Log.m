function [Acc,Gyr,Mag,GPS,Or,Or_fused,P,T,ts_f,ts_s,XYZ,Vxyz,LA,G] = Read_Log(file_name)
A = csvread(file_name);

A(1:1020,:) = [];

B = A(:,1);
C = (B(:,1)~=1);
A(C,:) = [];
ts_f = A(:,2)/1000;
Or_fused = A(:,3:6);
Acc = A(:,7:9);
Gyr = A(:,10:12);
Mag = A(:,13:15);
XYZ = A(:,16:18);
Vxyz = A(:,19:21);
LA = A(:,22:24);
Or = A(:,25:27); 
G = A(:,28:30); 

A = csvread(file_name);

B = A(:,1);
C = (B(:,1)~=0);
A(C,:) = [];
ts_s = A(:,2)/1000;
GPS = A(:,3:5);
T = A(:,6);
P = A(:,7);



