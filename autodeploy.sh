./gitpull.sh
kill $(cat ./bin/shutdown.pid)
./gradlew bootJar
nohup java -Xms1024m -Xmx1024m -jar ./build/libs/auction-0.0.1.jar &
