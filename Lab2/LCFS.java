import java.io.*;
import java.util.*;
import java.math.*;
import java.util.Scanner;


public class LCFS{

	public static void main(String[] args) throws FileNotFoundException{

		boolean detailed;
		File filename;

		if (args.length < 1){
				System.out.println();

				System.err.printf("Error: incorrect usage.\n");
				System.err.printf("Usage Error: the program expects at least a file name as an argument.\n");
				System.out.println();

				System.exit(1);
		}

		if (args[0].equals("-verbose")){

			detailed = true;
			filename = new File(args[1]);

		} else{

			detailed = false;
			filename = new File(args[0]);
		}

		if (!filename.exists() || !filename.canRead()){

			System.out.println();

			System.err.printf("Error: the file cannot be opened.\n");
			System.out.println();

			System.exit(1);
		}	

	    Scanner input = new Scanner(filename);

	    int num_processes = input.nextInt();

	    ArrayList<Node> processes = new ArrayList<Node>();

	    for(int i = 0; i < num_processes; i++){

	    	int a = input.nextInt();
	    	int b = input.nextInt();
	    	int c = input.nextInt();
	    	int m = input.nextInt();

	    	Node p = new Node(a, b, c, m);

	    	processes.add(p);
	    }

	    System.out.print("The original input was: " + num_processes + " ");
	    for(int i = 0; i < processes.size(); i++) System.out.print(processes.get(i).toString() + " ");
	    System.out.print("\n");

	    processes.sort(new sortbyA());

	    for(int i = 0; i < processes.size(); i++){
	    	processes.get(i).pid = i;
	    }

	    System.out.print("The (sorted) input is:  " + num_processes + " ");
	    for(int i = 0; i < processes.size(); i++) System.out.print(processes.get(i).toString() + " ");
	    System.out.print("\n");
		System.out.println();

	    LCFS(processes, detailed);
	}

	public static void LCFS(ArrayList<Node> processes, boolean detailed){

		int terminated = 0;
		int arrived_yet = 0;
		int cycle = 0;
		int CPU_time = 0;
		int IO_time = 0;

		Node running = null;
		ArrayList<Node> ready = new ArrayList<Node>();
		ArrayList<Node> blocked = new ArrayList<Node>();
		ArrayList<Node> b_to_r = new ArrayList<Node>();

		rand_int random = new rand_int();

		while(terminated < processes.size()){

			if (detailed) status(cycle, processes);

			for (int i = 0; i < blocked.size(); i++){

				blocked.get(i).io_time--;
				blocked.get(i).io++;
			}

			for(int i = 0; i < ready.size(); i++){

				ready.get(i).waiting++;
			}

			if(running != null){

				CPU_time++;
				running.time_running++;
				running.time_remaining--;
			}


			if(!blocked.isEmpty()) IO_time++;

			if(running != null){

				if(running.time_remaining == 0){

					running.status = "terminated";
					terminated++;
					running.finishing = cycle;
					running.turnaround = running.finishing - running.a;
					running = null;
				}

				else if(running.burst_time == running.time_running){

					running.status = "blocked";
					running.io_time = running.burst_time*running.m;
					running.time_running = 0;
					blocked.add(running);
					running = null;

				}
			}

			int j = 0;

			for (int i = arrived_yet; i < processes.size(); i++){

				if(processes.get(i).a == cycle){

					processes.get(i).status = "ready";
					b_to_r.add(processes.get(i));
					arrived_yet++;
				}
			}

			while(j < blocked.size()){

				if(blocked.get(j).io_time == 0){

					Node b = blocked.remove(j);
					b.status = "ready";
					b_to_r.add(b);
				} else j++;

			}

			if(b_to_r.size() == 1){
				
				ready.add(b_to_r.remove(0));
				
			}

			if(b_to_r.size() > 1){

				b_to_r.sort(new sortbyPID());

				while(!b_to_r.isEmpty()){

					ready.add(b_to_r.remove(b_to_r.size() - 1));
				}
			}

			if(running == null && !ready.isEmpty()){

				running = ready.remove(ready.size() - 1);
				running.status = "running";
				running.burst_time = random.randomOS(running.b);
			}

			cycle++;
		}

		System.out.println("The scheduling algorithm used was First Come First Served");
		System.out.println();

		for(int i = 0; i < processes.size(); i++) p_summary(processes.get(i));
		System.out.println();

		d_sum((float)processes.size(), --cycle, (float)CPU_time, (float)IO_time, average(processes, 'w'), average(processes, 't'));

	}

	public static void status(int cycle, ArrayList<Node> processes){

		StringBuilder str = new StringBuilder("Before cycle" + String.format("%5d", cycle) + ":");
		for(int i = 0; i < processes.size(); i++){

			Node p = processes.get(i);

			if (p.status.equals("running"))
				str.append(String.format("%11s", p.status) + String.format("%5d", p.burst_time - p.time_running));

			if (p.status.equals("blocked"))
				str.append(String.format("%11s", p.status) + String.format("%5d", p.io_time));

			if (p.status.equals("ready") || p.status.equals("terminated") || p.status.equals("unstarted"))
				str.append(String.format("%11s", p.status) + String.format("%5d", 0));

		}

		str.append(".");

		System.out.println(str.toString());
	}

	public static void p_summary(Node process){

		System.out.println("Process " + process.pid + ":");
		System.out.println("\t(A,B,C,M) = " + process.toString());
		System.out.println("\tFinishing time: " + process.finishing);
		System.out.println("\tTurnaround time: " + process.turnaround);
		System.out.println("\tI/O time: " + process.io);
		System.out.println("\tWaiting time: " + process.waiting);
		
	}

	public static void d_sum(float num_processes, int cycle, float CPU_time, float IO_time, float avg_w, float avg_t){

		float cpu_u = CPU_time/cycle;
		float io_u = IO_time/ cycle;
		float throughput = (num_processes/cycle)*100;

		System.out.println("Summary Data:");
		System.out.println("\tFinishing time: " + cycle);
		System.out.printf("\tCPU Utilization: %.6f\n", cpu_u);
		System.out.printf("\tI/O Utilization: %.6f\n", io_u);
		System.out.printf("\tThroughput: %.6f", throughput);
		System.out.println(" processes per hundred cycles");
		System.out.printf("\tAverage turnaround time: %.6f\n", avg_t);
		System.out.printf("\tAverage waiting time: %.6f\n", avg_w);

	}

	public static float average(ArrayList<Node> processes, char metric){

		int total = 0;
		int num_processes = processes.size();

		for(int i = 0; i < num_processes; i++){

			if(metric == 'w') total += processes.get(i).waiting;

			if(metric == 't') total += processes.get(i).turnaround;
		}

		return (((float) total)/((float) num_processes));
	}
}

