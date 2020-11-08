package hello.threads;

import hello.Admin;
import hello.HostCommunicator;
import hello.processes.ProcessNames;
import hello.processes.ServerStatus;
import hello.processes.ThreadName;

public class Prepare extends restThreads {

    private static ThreadName tn = ThreadName.PREPARE;

    public Prepare (String process){
        super(process);
    }

    public void run(){
        if (!ServerStatus.serverAvailable(tn, ProcessNames.FORCEWRITE)) return;
        Admin.forceWrite(process, "prepare");
        if (!ServerStatus.serverAvailable(tn, ProcessNames.CHANGESTATUS)) return;
        Admin.changeStatus(process,"prepare");
        if (!ServerStatus.serverAvailable(tn, ProcessNames.YESVOTE)) return;
        HostCommunicator.yesVote(process);
        if (!ServerStatus.serverAvailable(tn, ProcessNames.END)) return;
    }
}
