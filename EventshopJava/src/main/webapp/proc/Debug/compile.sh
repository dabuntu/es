#!/bin/sh
cd /home/abhisekmohanty/Work/eventshop/source_code/eventshop/EventshopJava/target/eventshoplinux/proc/Debug
#cd /home/eventshop/apache-tomcat-2/webapps/eventshoplinux/proc/Debug
g++ -I/usr/local/include/opencv -I/usr/local/include/opencv2 -I/usr/include -I/usr/local/include -O0 -g3 -Wall -c -fmessage-length=0 -o src/$1.o ../src/$1.cc -lopencv_core -lopencv_highgui -lopencv_imgproc -lpthread
g++ -L/usr/lib -L/usr/local/lib/pkgconfig -L/usr/local/include/opencv -L/lib64 -o $2 src/temporalPatternTemplate.o src/temporalPattern.o src/spatialchar.o src/spatialPattern.o src/op.o src/message.pb.o src/$1.o src/grouping.o src/filter.o src/emage.o src/aggregation.o -lopencv_core -lopencv_highgui -lprotobuf -lprotoc -lopencv_imgproc -lpthread

