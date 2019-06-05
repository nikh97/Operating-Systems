import java.util.*;
import java.math.*;
import java.util.Scanner;

//this class represents the frametable with a LRU replacement algorithm
public class LRUtable implements Table{

	//these fields keep track of the number of frames and the table
	public int num_frames;
	public int[][] table;

	//this constructors sets the subsequent data fields
	public LRUtable(int M, int P){

		this.num_frames = M/P;
		this.table = new int[num_frames][4];
	}

	//this method replaces the frame using LRU
	public void replacement(Process[] processes, int page, int pid, int cycle){

		int least_used = cycle;
		int n = 0;

		for(int i = num_frames - 1; i >= 0; i--){

			//if there is a free frame then fill it
			if((table[i][0] == 0) && (table[i][1] == 0)){

				table[i][0] = page;
				table[i][1] = pid + 1;
				table[i][2] = cycle;
				table[i][3] = cycle;

				return;
			}

			//now find least recently used frame to evict
			if(least_used > table[i][2]){

				n = i;
				least_used = table[i][2];
			}
		}

		//now evict the frame and updates
		int evicted = table[n][1];
		Process removed = processes[evicted - 1];
		removed.evictions++;
		removed.residency += cycle - table[n][3];

		table[n][0] = page;
		table[n][1] = pid + 1;
		table[n][2] = table[n][3] = cycle;
	}



	//this method checks a fault
	public boolean fault(int page, int pid, int cycle){

		//see if there is a hit for the frame, updates recently used time
		for(int i = 0; i < num_frames; i++){

			int[] pageFrame = table[i];

			if((pageFrame[0] == page) && (pageFrame[1] == pid + 1)){ 
				table[i][2] = cycle;	
				return false;
			}
		}

		//if not, we fault
		return true;
	}
}