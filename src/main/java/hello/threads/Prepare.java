package hello.threads;

import hello.Admin;
import hello.HostCommunicator;
import hello.processes.ProcessNames;
import hello.processes.ServerStatus;
import hello.processes.ThreadName;

public class Prepare extends restThreads {

	private static ThreadName tn = ThreadName.PREPARE;

	public Prepare(String process) {
		super(process);
	}


	public void run() {

		ServerStatus.serverAvailableElseSleep(tn, ProcessNames.FORCEWRITE);
		Admin.forceWrite(process, "prepare");
		ServerStatus.serverAvailableElseSleep(tn, ProcessNames.CHANGESTATUS);
		Admin.changeStatus(process, "prepare");
		if (!Admin.getValue(process).equals("defective")) {
			ServerStatus.serverAvailableElseSleep(tn, ProcessNames.YESVOTE);
			HostCommunicator.yesVote(process);
		} else {
			ServerStatus.serverAvailableElseSleep(tn, ProcessNames.NOVOTE);
			HostCommunicator.noVote(process);
			ServerStatus.serverAvailableElseSleep(tn, ProcessNames.ABORT);
			Admin.abort(process);
			ServerStatus.serverAvailableElseSleep(tn, ProcessNames.FORGET);
			Admin.forget(process);
		}
		ServerStatus.serverAvailableElseSleep(tn, ProcessNames.END);
	}
}
