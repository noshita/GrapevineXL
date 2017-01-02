cd /home/usernam/GroIMP1.5/

libs=/home/username/GroIMP1.5/ext/gluegen-rt.jar:/home/username/GroIMP1.5/ext/jogl.jar:/home/username/GroIMP1.5/ext/jocl.jar:/home/username/GroIMP1.5/ext/commons-math3-3.5.jar

java -Xmx15000m -Djava.library.path=/home/username/GroIMP1.5/ext -classpath $libs -jar core.jar --headless --debug=INFO $1 2> logfile.txt


