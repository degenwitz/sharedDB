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
        Admin.forceWrite(process, "prepare");
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Admin.changeStatus(process,"prepare");
        if(!Admin.getValue(process).equals("defective")){
            HostCommunicator.yesVote(process);
        } else {
            HostCommunicator.noVote(process);
            Admin.abort(process);
            Admin.forget(process);
        }
        if (!ServerStatus.serverAvailable(tn, ProcessNames.END)) return;
    }
}
