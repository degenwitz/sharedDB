package hello.threads;

public class restThreads extends Thread {

	String process = "null";

	public restThreads(String process) {
		super();
		this.process = process;
	}
}
