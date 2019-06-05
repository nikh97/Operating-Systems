
//this is an interface to be used by the different types of tables
public interface Table{

	public void replacement(Process[] processes, int page, int pid, int cycle);
	public boolean fault(int page, int pid, int cycle);
}