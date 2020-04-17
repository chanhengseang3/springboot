#!/bin/sh
check_port()
{
        ./wait-for-it.sh localhost:8080 -t 30 -s -- sleep 5m && check_port
}
while true
do
        check_port
        nohup java -Xms1024m -Xmx1024m -jar ./build/libs/auction-0.0.1.jar &
        echo restarted instance
done
