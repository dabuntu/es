conf=$1
. ./${conf}

fileCount=0
export IFS=","
for file in $FILES; do
        fileCount=`expr $fileCount + 1`

done

cnt=0
while true
do
        sleep $SLEEPTIME
        cnt=`expr $cnt + 1`
        if [ $cnt -gt $fileCount ]
	then
		cnt=1
	fi
	fileName=$(echo $FILES | cut -d' ' -f${cnt})		
	echo $fileName
	cp -f  $SOURCEFOLDER/$fileName $DESTINATIONFOLDER/$FILENAME	

done


