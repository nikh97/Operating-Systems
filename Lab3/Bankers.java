import java.io.*;
import java.util.*;
import java.math.*;
import java.util.Scanner;

public class Bankers{

	public static void main(String[] args) throws FileNotFoundException{

		//First check that the user has inputed a filename in the commandline
		if (args.length < 1){
			System.out.println();

			System.err.printf("Error: incorrect usage.\n");
			System.err.printf("Usage Error: the program expects at least a file name as an argument.\n");
			System.out.println();

			System.exit(1);
		}

		//read in commandline argument as a File object
		File filename = new File(args[0]);

		//Now check that the file exists and can be opened
		if (!filename.exists() || !filename.canRead()){

			System.out.println();

			System.err.printf("Error: the file cannot be opened.\n");
			System.out.println();

			System.exit(1);
		}	

		//start Scanner object to read the file
	    Scanner input = new Scanner(filename);

	    //T stores the number of tasks, while R stores the number of resources
	    int T = input.nextInt();
	    int R = input.nextInt();

	    //Major data structures
	    //original_res stores the resource capacity within the system for each type of resource
	    ArrayList<Integer> original_res = new ArrayList<Integer>(R);

	    //Both tasks1 and tasks2 store information about the tasks
	    //Create two instances for each task in order to be passed to bankers and fifo seperately
	    ArrayList<Task> tasks1 = new ArrayList<Task>(T);
	    ArrayList<Task> tasks2 = new ArrayList<Task>(T);

	    //fill in original_res with information provided in first line of input
	    for(int i = 0; i < R; i++) original_res.add(input.nextInt());

	    //now fill tasks arraylist with instances of the tasks
	  	for(int i = 0; i < T; i++){

	  		tasks1.add(new Task(i, R));
	  		tasks2.add(new Task(i, R));

	  	}

	  	//this while loop stores the activities of each task within each instance of the tasks
	  	//see Task.java for clarification on these data fields
	  	while(input.hasNext()){

	  		String action = input.next();
	  		int index = input.nextInt() - 1;
	  		int type = input.nextInt();
	  		int count = input.nextInt();

	  		tasks1.get(index).actions.add(action);
	  		tasks1.get(index).res_needed.add(type);
	  		tasks1.get(index).req_num.add(count);

	  		tasks2.get(index).actions.add(action);
	  		tasks2.get(index).res_needed.add(type);
	  		tasks2.get(index).req_num.add(count);
	  	}


	  	//*for debugging purposes only*
	  	// for(int i = 0; i < T; i++){

	  	// 	System.out.println(tasks.get(i).actions.toString());
	  	// 	System.out.println(tasks.get(i).res_needed.toString());
	  	// 	System.out.println(tasks.get(i).req_num.toString());
	  	// }

	  	//This are method calls to run the banker's simulation, the fifo simulation, and to make the end report
	  	System.out.println();
	  	fifo(T, R, original_res, tasks1);
	  	bankers(T, R, original_res, tasks2);
	  	report(tasks1, tasks2);
	  	System.out.println();
	  	
	  	
	}

	//This is a method that runs the banker's simulation
	//Params: number of tasks T, number of resources R, list of resource type and capacity original_res, list of task objects tasks
	private static void bankers(int T, int R, ArrayList<Integer> original_res, ArrayList<Task> tasks){

		//these variables count the cycles and the number of tasks terminated, which determines when to end the simulation
		int cycle = 0;
		int num_terminated = 0;

		//sorted keeps track of which tasks to run in order, blocked tasks will be put into the front of the list
		//nextRound keeps track of which tasks are blocked within each cycle
		//sorted becomes nextRound at the end of each cycle
		ArrayList<Integer> sorted = new ArrayList<Integer>();
		ArrayList<Integer> nextRound = new ArrayList<Integer>();

		//next two lines, creates a true copy of the resources as to not change the original for the fifo simulation
		ArrayList<Integer> resources = new ArrayList<Integer>();

		for(int i = 0; i < R; i++) resources.add(original_res.get(i));

		//run simulation until all the tasks have been terminated 
		while(num_terminated < T){

			//now add tasks that are running to sorted in order of task number
			//blocked or waiting tasks would have been added through nextRound in the previous cycle at the front of the list
			for(int i = 0; i < T; i++){ 
				if(!sorted.contains(i)){
					sorted.add(i);
				}
			}

			//this keeps track of the amount of each resource type released in a cycle
			//makes sure the resource is available at the next cycle rather than immediately
			int[] res_release = new int[R];

			//now go through each task in sorted during the cycle
			for(Integer i : sorted){

				//get task object to be looked at
				Task task = tasks.get(i);

				//skip task if it is aborted or terminated already
				if(task.terminated || task.aborted) continue;

				//get the activity number, the specific activity type, the resource used, and the quantity of that reources req, initiated, released, etc.
				int cmd = task.cmd;
				String command = task.actions.get(cmd);
				int resIndex = task.res_needed.get(cmd) - 1;
				int req = task.req_num.get(cmd);

				//this takes care of the compute activity
				if(command.equals("compute")){

					//put the task in a compute state and get the number of cycles it is delayed
					if(!task.computing){

						task.compute_cycles = resIndex + 1;
						task.computing = true;
						task.running = false;
						task.waiting = false;
					}

					//decrement cycle delays 
					task.compute_cycles--;

					//once delay is over, put task back in a running state
					if(task.compute_cycles == 0){

						task.computing = false;
						task.running = true;
					}
				}

				//this takes care of the initiate activity
				else if(command.equals("initiate")){

					//if the initiate is greater than the max number of the resource type, banker's aborts it
					if(req > original_res.get(resIndex)){

						task.aborted = true;
						System.out.println("  Banker aborts task " + (i+1) + " before run begins:");
						System.out.println("       claim for resource " + (resIndex+1) + " (" + req + ")" + " exceeds number of units present (" + original_res.get(resIndex) + ")");
						System.out.println();

						num_terminated++;

						//break out of loop to next task
						continue;
					}

					//System.out.println(cycle + " - " + (cycle + 1) + ": initiate task" + (i+1));

					//if valid initiate, then set the claims for the task, and put the task in a running state
					task.claims.set(resIndex, req);
					task.running = true;
				}

				//this handles the request activity
				else if(command.equals("request")){

					//call isSafe to check if request if safe according to banker's algorithm
					boolean safe = isSafe(tasks, i, resources, resIndex, req);

					//if the task is aborted during the safety check, handle it
					if (task.aborted){

						//increment num_terminated, and put task in an aborting state
						num_terminated++;
						task.terminated = false;
						task.running = false;
						task.waiting = false;

						//now release all the resources it has 
						for(int j = 0; j < task.allocation.size(); j++){

							res_release[j] += task.allocation.get(j);
						}

						//print message to user
						System.out.println("  During cycle " + cycle + "-" + (cycle+1) + " of Banker's algorithms");
						System.out.println("     Task " + (i+1) + " \'s request exceeds its claim; aborted; " + task.allocation.get(resIndex) + " units available next cycle");
						System.out.println();

						continue;
					}	

					//if safe do this
					if(safe && req <= resources.get(resIndex)){

						//System.out.println(cycle + " - " + (cycle + 1) + ": fill request task" + (i+1));

						//allocate the resource and take away from resource avaliablility
						task.allocation.set(resIndex, task.allocation.get(resIndex) + req);
						resources.set(resIndex, resources.get(resIndex) - req);

						//make sure task is in running state
						task.running = true;
						task.waiting = false;
					} else{

						//System.out.println(cycle + " - " + (cycle + 1) + ": block task" + (i+1));

						//here, the task is not safe and is therefore, put in a waiting state, and is pushed to the nextRound list
						//in order to be processed first in the next cycle
						task.waiting = true;
						task.running = false;
						task.time_wait++;

						nextRound.add(i);
					}
				}

				//this handles the release activity
				else if(command.equals("release")){

					//System.out.println(cycle + " - " + (cycle + 1) + ": release task" + (i+1));

					//deallocate the resource type and add it to res_release to become avaliable in the next cycle
					task.allocation.set(resIndex, task.allocation.get(resIndex) - req);
					res_release[resIndex] += req;
				}

				//now increment the total time taken for the task
				task.cycle_term++;

				//if the state is running, then increment cmd to the next activity
				if(task.running){
					//System.out.println(cycle + " - " + (cycle + 1) + ": increment cmd task" + (i+1));
					task.cmd++;
				}

				//since we do not need an extra cycle to terminate
				//check to see if task terminates after cmd is incremented
				if(task.actions.get(task.cmd).equals("terminate")){
					//System.out.println(cycle + " - " + (cycle + 1) + ": terminate task" + (i+1));

					task.terminated = true;
					task.running = false;
					task.waiting = false;

					num_terminated++;
				}
			}

			//now add back available resources for use in the next cycle
			for(int g = 0; g < res_release.length; g++){

				resources.set(g, resources.get(g) + res_release[g]);
			}

			//sorted becomes nextRound, and nextRound is instantiated again
			sorted = nextRound;
			nextRound = new ArrayList<Integer>();

			//increment cycle
			cycle++;


		}

		// for(int n = 0; n < tasks.size(); n++){

		// 	if(tasks.get(n).aborted) System.out.println((n+1) + ": " + "aborted");

		// 	else System.out.println((n+1) + ": " + tasks.get(n).cycle_term + " " + tasks.get(n).time_wait);
		// }

		// System.out.println(cycle);

	}

	//this is banker's algorithm, checks if the request is safe for the system
	//Params: tasks list org_task, task number making request task index, resources org_res, resource requested resIndex, amount requested req
	private static boolean isSafe(ArrayList<Task> org_tasks, int task_index, ArrayList<Integer> org_res, int resIndex, int req){

		//next 4 lines, creates true copies of tasks and resources in order to pretend to grant resources without
		//changing values within the simulation
		ArrayList<Task> tasks = new ArrayList<Task>();
		ArrayList<Integer> resources = new ArrayList<Integer>();

		for(int i = 0; i < org_tasks.size(); i++) tasks.add(new Task(org_tasks.get(i)));	

		for(int i = 0; i < org_res.size(); i++) resources.add(org_res.get(i));

		//tracks that every task can finish
		ArrayList<Boolean> finish = new ArrayList<Boolean>();

		//if a task is already terminated or aborted, ignore it, by saying it will finish
		for(int i = 0; i < tasks.size(); i++){

			if(tasks.get(i).terminated || tasks.get(i).aborted) finish.add(true);
			else finish.add(false);
		}

		//now pretend to allocate the request to the task
		tasks.get(task_index).allocation.set(resIndex, tasks.get(task_index).allocation.get(resIndex) + req);

		//check if that allocation brings the task over its claims, if so abort the task
		//as it lied about its claims
		int amountHeld = tasks.get(task_index).allocation.get(resIndex);
		int amountClaim = tasks.get(task_index).claims.get(resIndex);

		if (amountHeld > amountClaim){
			org_tasks.get(task_index).aborted = true;
			//System.out.println("The banker has aborted task " + (task_index + 1) + " because it lied in its initial claims.");
			return false;
		}

		//now reduce availiability of resouce
		resources.set(resIndex, resources.get(resIndex) - req);

		//now check if the state is safe, if so every task will finish
		while(finish.contains(false)){

			//this keeps track of which task can finish during the subsequent cycle
			int t1 = -1;

			//go through each task to see you can find one that finishes, if one is found stop the loop
			for(int i = 0; i < tasks.size() && t1 == -1; i++){

				//if already true, skip the task
				if (finish.get(i)) continue;

				//keeps track if current task is safe
				boolean safe = true;

				//go through each resource, and grant the rest of the claim for the task
				for(int j = 0; j < tasks.get(i).claims.size() && safe; j++){

					int claimed = tasks.get(i).claims.get(j);
					int allocated = tasks.get(i).allocation.get(j);

					int max_req = claimed - allocated;

					//if the max allowable grant is greater than the number of resources available, the task will not finish
					if(max_req > resources.get(j)) safe = false;
				}

				//if task is still safe, a task has been found to finish
				if (safe) t1 = i;
			}

			//if no task is found, then the state is unsafe, deny the request
			if(t1 == -1){ 
				return false;
			}
			else{

				//else say task will finish, and now release all the resources it has
				finish.set(t1, true);
				for(int i = 0; i < tasks.get(t1).allocation.size(); i++){

					resources.set(i, resources.get(i) + tasks.get(t1).allocation.get(i));
				}
			}
		}

		//once we reach this point, all tasks will finish, the state is safe
		return true;
	}

	//this runs the optimistic manager or fifo simulation
	//Params: same as the banker's params
	private static void fifo(int T, int R, ArrayList<Integer> original_res, ArrayList<Task> tasks){

		//these variables count the cycles and the number of tasks terminated, which determines when to end the simulation
		int cycle = 0;
		int num_terminated = 0;

		//sorted keeps track of which tasks to run in order, blocked tasks will be put into the front of the list
		//nextRound keeps track of which tasks are blocked within each cycle
		//sorted becomes nextRound at the end of each cycle
		ArrayList<Integer> sorted = new ArrayList<Integer>();
		ArrayList<Integer> nextRound = new ArrayList<Integer>();

		//next two lines, creates a true copy of the resources as to not change the original for the fifo simulation
		ArrayList<Integer> resources = new ArrayList<Integer>();

		for(int i = 0; i < R; i++) resources.add(original_res.get(i));

		//run simulation until all the tasks have been terminated
		while(num_terminated < T){

			//now add tasks that are running to sorted in order of task number
			//blocked or waiting tasks would have been added through nextRound in the previous cycle at the front of the list
			for(int i = 0; i < T; i++){ 
				if(!sorted.contains(i)){
					sorted.add(i);
				}
			}

			//these two track the number of states that cannot proceed
			//as well as the number of terminated tasks at the end of the cycle
			int numDeadlocked = 0;
			int term_at_end_of_cycle = 0;

			//this keeps track of the amount of each resource type released in a cycle
			//makes sure the resource is available at the next cycle rather than immediately
			int[] res_release = new int[R];

			//now go through each task in sorted during the cycle
			for(Integer i : sorted){

				//get task object to be looked at
				Task task = tasks.get(i);

				//skip task if it is aborted or terminated already
				if(task.terminated || task.aborted) continue;

				//get the activity number, the specific activity type, the resource used, and the quantity of that reources req, initiated, released, etc.
				int cmd = task.cmd;
				String command = task.actions.get(cmd);
				int resIndex = task.res_needed.get(cmd) - 1;
				int req = task.req_num.get(cmd);

				//this takes care of the compute activity
				if(command.equals("compute")){

					//put the task in a compute state and get the number of cycles it is delayed
					if(!task.computing){

						task.compute_cycles = resIndex + 1;
						task.computing = true;
						task.running = false;
						task.waiting = false;
					}

					//decrement cycle delays
					task.compute_cycles--;

					//once delay is over, put task back in a running state
					if(task.compute_cycles == 0){

						task.computing = false;
						task.running = true;
					}
				}

				else if(command.equals("initiate")){

					//System.out.println(cycle + " - " + (cycle + 1) + ": initiate task" + (i+1));
					task.claims.set(resIndex, req);
					task.running = true;
				}

				//this takes care of the initiate activity
				else if(command.equals("request")){

					//if the request can be made with the avaliable resources, grant it
					//unlike banker's, we do not need to check safety
					if(req <= resources.get(resIndex)){

						//System.out.println(cycle + " - " + (cycle + 1) + ": fill request task" + (i+1));

						task.allocation.set(resIndex, task.allocation.get(resIndex) + req);
						resources.set(resIndex, resources.get(resIndex) - req);

						task.running = true;
						task.waiting = false;
					} else{

						//System.out.println(cycle + " - " + (cycle + 1) + ": block task" + (i+1));

						//if the request cannot be grant, the task waits
						task.waiting = true;
						task.running = false;
						task.time_wait++;

						nextRound.add(i);

						numDeadlocked++;
					}
				}

				//this handles the release activity
				else if(command.equals("release")){

					//System.out.println(cycle + " - " + (cycle + 1) + ": release task" + (i+1));

					task.allocation.set(resIndex, task.allocation.get(resIndex) - req);
					res_release[resIndex] += req; //resources.set(resIndex, resources.get(resIndex) + req);
				}

				//now increment the total time taken for the task
				task.cycle_term++;

				//if the state is running, then increment cmd to the next activity
				if(task.running){
					//System.out.println(cycle + " - " + (cycle + 1) + ": increment cmd task" + (i+1));
					task.cmd++;
				}

				//since we do not need an extra cycle to terminate
				//check to see if task terminates after cmd is incremented
				if(task.actions.get(task.cmd).equals("terminate")){
					//System.out.println(cycle + " - " + (cycle + 1) + ": terminate task" + (i+1));
					task.terminated = true;
					task.running = false;
					task.waiting = false;

					term_at_end_of_cycle++;
				}

				//this checks if the state has been deadlocked
				if(numDeadlocked == T - num_terminated){

					boolean deadlocked = true;

					//keep aborting tasks until the state is not deadlocked
					while(deadlocked) {

						int abort = 0;

						while(!tasks.get(abort).waiting){ 
							abort++;
						}

						Task t = tasks.get(abort);

						t.aborted = true;
						t.running = false;
						t.waiting = false;

						for(int j = 0; j < t.allocation.size(); j++){

							resources.set(j, resources.get(j) + t.allocation.get(j));
						}

						num_terminated++;

						System.out.println("The Optimistic Manager has detected a deadlock, Task " + (abort+1) + " has been aborted");
						System.out.println();

						if(req <= resources.get(resIndex)){
						 	deadlocked = false;
						}

					}

					//break out of loop and go on to next task
					break;

				}
			}

			//now add back available resources for use in the next cycle
			for(int g = 0; g < res_release.length; g++){

					resources.set(g, resources.get(g) + res_release[g]);
			}

			//add the number of terminated tasks in the cycle
			num_terminated += term_at_end_of_cycle;

			sorted = nextRound;
			nextRound = new ArrayList<Integer>();

			cycle++;

		}

		// for(int n = 0; n < tasks.size(); n++){

		// 	if(tasks.get(n).aborted) System.out.println((n+1) + ": " + "aborted");

		// 	else System.out.println((n+1) + ": " + tasks.get(n).cycle_term + " " + tasks.get(n).time_wait);
		// }

		// System.out.println(cycle);
	}

	//this creates the final report for both simulation
	//Params: tasks1 and tasks2, from each simulation
	private static void report(ArrayList<Task> task1, ArrayList<Task> task2){

		//these variables count the cycles and the number of tasks terminated, which determines when to end the simulation
		System.out.println(String.format("%18s", "FIFO") + String.format("%35s", "BANKER\'S"));

		//these variables keep track of the total time taken and waiting for each simulation
		int totalTime1 = 0;
		int totalWait1 = 0;

		int totalTime2 = 0;
		int totalWait2 = 0;

		//everything else prints and formats all information
		for(int i = 0; i < task1.size(); i++){

			float p1 = (((float) task1.get(i).time_wait)/((float)task1.get(i).cycle_term)) * 100;
			float p2 = (((float) task2.get(i).time_wait)/((float)task2.get(i).cycle_term)) * 100;

			System.out.print(String.format("%11s", "Task " + (i+1)));

			if(task1.get(i).aborted){
				System.out.print(String.format("%11s", "aborted"));
				System.out.print(String.format("%13s", ""));

			}else{
				System.out.print(String.format("%5d", task1.get(i).cycle_term));
				System.out.print(String.format("%5d", task1.get(i).time_wait));
				System.out.print(String.format("%6.0f%%", p1));

				totalTime1 += task1.get(i).cycle_term;
				totalWait1 += task1.get(i).time_wait;

				System.out.print(String.format("%7s", ""));
			}

			System.out.print(String.format("%7s", "Task " + (i+1)));

			if(task2.get(i).aborted){
				System.out.println(String.format("%11s", "aborted"));

			}else{
				System.out.print(String.format("%5d", task2.get(i).cycle_term));
				System.out.print(String.format("%5d", task2.get(i).time_wait));
				System.out.println(String.format("%6.0f%%", p2));

				totalTime2 += task2.get(i).cycle_term;
				totalWait2 += task2.get(i).time_wait;
			}
		}

		float p3 = (((float) totalWait1)/((float)totalTime1)) * 100;
		float p4 = (((float) totalWait2)/((float)totalTime2)) * 100;

		System.out.print(String.format("%10s", "total"));
		System.out.print(String.format("%6d", totalTime1));
		System.out.print(String.format("%5d", totalWait1));
		System.out.print(String.format("%6.0f%%", p3));

		System.out.print(String.format("%13s", "total"));
		System.out.print(String.format("%6d", totalTime2));
		System.out.print(String.format("%5d", totalWait2));
		System.out.println(String.format("%6.0f%%", p4));


	}
}