import java.io.*;
import java.util.*;
import java.math.*;
import java.util.Scanner;

//this is a class that stores all information for each process
public class Process{

	//these fields keep track of the process number, number of faults and evictions, and the time in residency
	public int pid;
	public int residency = 0;
	public int evictions = 0;
	public int faults = 0;

	//this keeps track of the size of the process and the next reference
	public int next;
	public int size;

	//the constructor keeps track of the process number, size, and initializes the first next reference
	public Process(int size, int pid){

		this.size = size;
		this.pid = pid;

		next = (111*pid) % size;

	}

	//this method sets the next reference
	public void set_next(double A, double B, double C, int r1, rand_int random){

		//this sets the quotient or probability
		double y = ((double) r1)/(Integer.MAX_VALUE + 1d);

		//based on y, set the next reference
		if (y < A) next = (next + 1) % size;
		else if (y < A + B) next = (next - 5 + size) % size;
		else if (y < A + B + C) next = (next + 4) % size;
		else next = (random.nextInt()) % size;
		
	}

}