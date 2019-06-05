import java.util.*;
import java.math.*;
import java.util.Scanner;

//this class represents the frametable with a LIFO replacement algorithm
public class LIFOtable implements Table{

	//these fields keep track of the number of frames and the table
	public int num_frames;
	public ArrayList<int[]> table;

	//this constructors sets the subsequent data fields
	public LIFOtable(int M, int P){

		this.num_frames = M/P;
		this.table = new ArrayList<int[]>();
	}
	
	//this method replaces the frame using LIFO
	public void replacement(Process[] processes, int page, int pid, int cycle){

		//now find the frame to evict if faulted
		if(num_frames == table.size()){
			int[] evict = table.remove(0);
			Process removed = processes[evict[1]];
			removed.evictions++;
			removed.residency += cycle - evict[2];
		}

		//not a free frame is found to fill
		int[] n = {page, pid, cycle};
		table.add(0, n);
	}



	//this method checks a fault
	public boolean fault(int page, int pid, int cycle){

		//see if there is a hit for the frame
		for(int i = 0; i < table.size(); i++){

			int[] pageFrame = table.get(i);

			if((pageFrame[0] == page) && (pageFrame[1] == pid)) return false;
		}

		//if not, we fault
		return true;
	}
}