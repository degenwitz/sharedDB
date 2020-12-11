package hello.threads;

import hello.Admin;
import hello.HostCommunicator;
import hello.processes.ProcessNames;
import hello.processes.ServerStatus;
import hello.processes.ThreadName;

public class Commit extends restThreads {

    private static ThreadName tn = ThreadName.COMMIT;

    public Commit(String process){
        super(process);
    }

    public void run(){
        Admin.__forcewrite("trying to commit: " + process,"in process" + HostCommunicator.getCI().getMyPort(),Admin.WriteReason.DEBUGGING);

        if(!Admin.getUncommittedProcess().containsKey(process)){
            Admin.__forcewrite("recovering process: " + process, "already forgotten", Admin.WriteReason.DEBUGGING);
            HostCommunicator.ack(process);
            return;
        }
        ServerStatus.serverAvailableElseSleep(tn, ProcessNames.CHANGESTATUS);
        Admin.changeStatus(process,"commit");
        ServerStatus.serverAvailableElseSleep(tn, ProcessNames.FORCEWRITE);
        Admin.forceWrite(process, "commit");
        ServerStatus.serverAvailableElseSleep(tn, ProcessNames.ACK);
        HostCommunicator.ack(process);
        ServerStatus.serverAvailableElseSleep(tn, ProcessNames.COMMIT);
        Admin.commit(process);
        ServerStatus.serverAvailableElseSleep(tn, ProcessNames.FORGET);
        Admin.forget(process);
        ServerStatus.serverAvailableElseSleep(tn, ProcessNames.END);
    }
}
