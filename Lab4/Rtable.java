import java.util.*;
import java.math.*;
import java.util.Scanner;
	
//this class represents the frametable with a random replacement algorithm
public class Rtable implements Table{

	//these fields keep track of the number of frames, the table, and the random number to be used
	public int num_frames;
	public int[][] table;
	public rand_int random;

	//this constructors sets the subsequent data fields
	public Rtable(int M, int P, rand_int random){

		this.num_frames = M/P;
		this.table = new int[num_frames][3];
		this.random = random;
	}

	//this method replaces the frame at random
	public void replacement(Process[] processes, int page, int pid, int cycle){

		//this for loop finds an open frame to fill
		for(int i = num_frames - 1; i >= 0; i--){

			if((table[i][0] == 0) && (table[i][1] == 0)){

				table[i][0] = page;
				table[i][1] = pid + 1;
				table[i][2] = cycle;

				return;
			}
		}

		//at random select the frame
		int ranNum = random.nextInt();
		int evicted = ranNum % num_frames;
		int index = table[evicted][1];

		Process removed = processes[index - 1];
		removed.evictions++;
		removed.residency += cycle - table[evicted][2];

		table[evicted][0] = page;
		table[evicted][1] = pid + 1;
		table[evicted][2] = cycle;
	}



	//this method checks a fault
	public boolean fault(int page, int pid, int cycle){

		//see if there is a hit for the frame
		for(int i = 0; i < num_frames; i++){

			int[] pageFrame = table[i];

			if((pageFrame[0] == page) && (pageFrame[1] == pid + 1)){	
				return false;
			}
		}

		//if not, we fault
		return true;
	}
}