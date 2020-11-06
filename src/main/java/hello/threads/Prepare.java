package hello.threads;

import hello.Admin;
import hello.Host;
import hello.processes.ProcessNames;
import hello.processes.ServerStatus;
import hello.processes.ThreadName;

public class Prepare extends restThreads {

    private static ThreadName tn = ThreadName.PREPARE;

    public Prepare (String process){
        super(process);
    }

    public void run(){
        if (!ServerStatus.serverAvaliable(tn, ProcessNames.FORCEWRITE)) return;
        Admin.forceWrite(process, "prepare");
        if (!ServerStatus.serverAvaliable(tn, ProcessNames.CHANGESTATUS)) return;
        Admin.changeStatus(process,"prepare");
        if (!ServerStatus.serverAvaliable(tn, ProcessNames.YESVOTE)) return;
        Host.yesVote(process);
        if (!ServerStatus.serverAvaliable(tn, ProcessNames.END)) return;
    }
}
