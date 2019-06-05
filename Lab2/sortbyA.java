import java.util.*; 
import java.lang.*; 
import java.io.*;

class sortbyA implements Comparator<Node>{

	public int compare(Node one, Node two){

		return one.a - two.a;
	}
}