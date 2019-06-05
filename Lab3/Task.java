import java.io.*;
import java.util.*;
import java.math.*;
import java.util.Scanner;


//This is a class that stores all the information for each task
public class Task{

	//this keeps track of the activity each task is at
	public int cmd = 0;

	//this is the task number
	public int tasknum;

	//the following data structures store in order: the number of resources claimed and the number allocated for each type
	public ArrayList<Integer> claims = new ArrayList<Integer>();
	public ArrayList<Integer> allocation = new ArrayList<Integer>(); 

	//these store the activity, the resource to be dealt with, and the number of the resource type
	public ArrayList<String> actions = new ArrayList<String>();
	public ArrayList<Integer> res_needed = new ArrayList<Integer>();
	public ArrayList<Integer> req_num = new ArrayList<Integer>();

	//these keep track of the current state of each task, if one is true all others should be false naturally
	public boolean computing = false;
	public boolean terminated = false;
	public boolean running = false;
	public boolean aborted = false;
	public boolean waiting = false;
	
	//this keeps track of the time taken to complete a task and the time in the waiting state
	public int cycle_term = 0;
	public int time_wait = 0;

	//this is used for the computing state to see how many cycles to delay
	public int compute_cycles;

	//this is a constructor that initiates the task_number, and the allocation and claims arrays
	public Task(int i, int R){

		tasknum = i;
		allocation = new ArrayList<Integer>();

		for(int j = 0; j < R; j++){

			allocation.add(0);
			claims.add(0);
		}
	}

	//this constructor essentially creates a true copy of the task passed in
	public Task(Task T){

		cmd = T.cmd;
		tasknum = T.tasknum;
		terminated = T.terminated;
		cycle_term = T.cycle_term;
		aborted = T.aborted;
		waiting = T.waiting;
		time_wait = T.time_wait;
		running = T.running;

		for (int i = 0; i < T.actions.size(); i++){

			actions.add(T.actions.get(i));
			res_needed.add(T.res_needed.get(i));
			req_num.add(T.req_num.get(i));
		}

		for(int i = 0; i < T.claims.size(); i++){

			claims.add(T.claims.get(i));
			allocation.add(T.allocation.get(i));
		}

	}
}

