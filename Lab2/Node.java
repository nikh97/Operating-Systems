public class Node{

	public int pid;
	public int a;
	public int b;
	public int c;
	public int m;

	public String status = "unstarted";
	public int burst_time;
	public int io_time;
	public int time_remaining;
	public int time_running = 0;

	public int finishing = 0;
	public int turnaround = 0;
	public int io = 0;
	public int waiting = 0;

	public Node(int a, int b, int c, int m){

		this.time_remaining = c;

		this.a = a;
		this.b = b;
		this.c = c;
		this.m = m;
	}

	public String toString(){

		return "(" + a + " " + b + " " + c + " " + m + ")";
	}
}