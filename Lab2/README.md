This is a java program implementing the four different scheduling algorithms required. 

To compile this program, first compile these four java scripts as such in terminal:

javac Node.java - this is an object representing a process
javac sortbyA.java - this is a comparator to sort processes by arrival times
javac sortbyPID.java - this is another comparator used to determine tie breaks
javac rand_int.java - this is an object that parses the random number file

Now, the scheduling algorithms are under 4 different java files:

FCFS.java (last come, first serve)
RR.java (round robbin)
LCFS.java (last come, first serve)
HPRN.java

To run each scheduling algorithm:

java [program-name WIHTOUT .java extension] [input file]

example: java FCFS input-4.txt

Optional, add -verbose for detailed report:

java [program name WITHOUT .java extension] -verbose [input file]

example java FCFS -verbose input-4.txt 