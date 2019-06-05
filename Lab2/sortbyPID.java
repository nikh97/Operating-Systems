import java.util.*; 
import java.lang.*; 
import java.io.*;

class sortbyPID implements Comparator<Node>{

	public int compare(Node one, Node two){

		return one.pid - two.pid;
	}
}