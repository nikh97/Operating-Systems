import java.io.*;
import java.util.*;
import java.math.*;
import java.util.Scanner;

public class Paging{

	public static void main(String[] args){

		//First check that the user has inputed the correct number of arguments
		if (args.length != 6){
			System.out.println();

			System.err.printf("Error: incorrect usage.\n");
			System.err.printf("Usage Error: the program expects 6 commandline arguments.\n");
			System.out.println();
			System.exit(1);
		}

		//initialize the scanner object to read the random number file
		rand_int random = new rand_int();

		//initialize information about the machine and simulation
		int M = Integer.parseInt(args[0]);
		int P = Integer.parseInt(args[1]);
		int S = Integer.parseInt(args[2]);
		int J = Integer.parseInt(args[3]);
		int N = Integer.parseInt(args[4]);
		String R = args[5].trim();

		//initialze the table
		Table table = null;

		//based on replacement algorithm, assign right type of table
		if(R.equals("lifo")) table = new LIFOtable(M, P);
		else if(R.equals("lru")) table = new LRUtable(M, P);
		else if(R.equals("random")) table = new Rtable(M, P, random);
		else{

			//else the user has not asked to implement the right replacement algorithm
			System.out.println("The replacement algorithm must be either lifo, lru, or random.");
			System.exit(0);
		}

		//an array to hold the processes
		Process[] processes = null;

		//initialize A, B and C for each process
		double[] A = null;
		double[] B = null;
		double[] C = null;

		//this initializes the processes and A, B, C arrays based on job mix
		if(J == 1){
			processes = new Process[1];
		 	processes[0] = new Process(S, 1);


		 	A = new double[1];
		 	B = new double[1];
		 	C = new double[1];

		}else if (J >= 2 && J <= 4){

		 	processes = new Process[4];
		 	for(int i = 0; i < 4; i++){

		 		processes[i] = new Process(S, i+1);
		 	}

		 	A = new double[4];
		 	B = new double[4];
		 	C = new double[4];

		} else{

			System.out.println("Error: Job mix should be 1-4");
			System.exit(0);
		}

		//now run the simulation
		job_mix(A, B, C, J);
		int clock = simulation(processes, A, B, C, J, N, P, random, table);
		report(processes, M, P, S, J, N, R);

	}

	//this method actually sets the values for the A, B, C arrays
	public static void job_mix(double[] A, double[] B, double[] C, int J){

		if(J == 1 || J == 2){

			for(int i = 0; i < A.length; i++){

				A[i] = 1;
				B[i] = 0;
				C[i] = 0;
			}
		}

		else if(J == 3){

			for(int i = 0; i < A.length; i++){

				A[i] = 0;
				B[i] = 0;
				C[i] = 0;
			}
		} else{

			A[0] = 0.75;
			B[0] = 0.25;
			C[0] = 0;

			A[1] = 0.75;
			B[1] = 0;
			C[1] = 0.25;

			A[2] = 0.75;
			B[2] = 0.125;
			C[2] = 0.125;

			A[3] = 0.5;
			B[3] = 0.125;
			C[3] = 0.125;
		}

	}

	//method to run the simulation
	public static int simulation(Process[] processes, double[] A, double[] B, double[] C, int J, int N, int P, rand_int random, Table table){

		//keeps track of total time
		int clock = 0;

		if(J == 1){

			for(int i = 0; i < N; i++){

				int page = processes[0].next / P;

				if(table.fault(page, 0, clock)){

					table.replacement(processes, page, 0, i);
					processes[0].faults++;
				}

				processes[0].set_next(A[0], B[0], C[0], random.nextInt(), random);
				clock++;
			}
		} else{

			int q = 3;

			int tot_refs = N / q;

			for(int i = 0; i <= tot_refs; i++){

				for(int j = 0; j < 4; j++){

					int ref;

					if(i != tot_refs) ref = q;
					else ref = N % q;

					for(int r = 0; r < ref; r++){

						int page = processes[j].next / P;

						if(table.fault(page, j, clock)){

							table.replacement(processes, page, j, clock);
							processes[j].faults++;
						}

						processes[j].set_next(A[j], B[j], C[j], random.nextInt(), random);
						clock++;

					}


				}
			}
		}

		return clock;

	}

	//this method takes care of the output
	public static void report(Process[] processes, int M, int P, int S, int J, int N, String R){

		print("");
		print("The machine size is " + M + ".");
		print("The page size is " + P + ".");
		print("The process size is " + S + ".");
		print("The job mix number is " + J + ".");
		print("The number of references per process is " + N + ".");
		print("The replacement algorithm is " + R + ".");

		int total_faults = 0;
		int total_res = 0;
		int total_evict = 0;

		print("");

		for(int i = 0; i < processes.length; i++){

			if(processes[i].evictions == 0) print("Process " + (i+1) + " had " + processes[i].faults + " faults.\n\t With no evictions, the average residency is undefined.");
			else print("Process " + (i+1) + " had " + processes[i].faults + " faults and " + ((double)processes[i].residency)/((double)processes[i].evictions) + " average residency.");

			total_faults += processes[i].faults;
			total_res += processes[i].residency;
			total_evict += processes[i].evictions;
		}

		print("");

		if (total_evict == 0) 
			print("The total number of faults is " + total_faults + ".\n\t With no evictions, the average residency is undefined.");
		else 
			print("The total number of faults is " + total_faults + " and the overall average residency is " + ((double)total_res)/((double)total_evict) + ".");

		print("");
	}

	public static void print(String s){

		System.out.println(s);
	}
		
}


























