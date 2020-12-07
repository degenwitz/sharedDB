package hello.threads;

import hello.Admin;
import hello.HostCommunicator;
import hello.processes.ProcessNames;
import hello.processes.ServerStatus;
import hello.processes.ThreadName;

public class Abort extends restThreads{

    private static ThreadName tn = ThreadName.ABORT;

    public Abort (String process){
        super(process);
    }

    public void run(){
        ServerStatus.serverAvailableElseSleep(tn, ProcessNames.CHANGESTATUS);
        Admin.changeStatus(process,"abort");
        ServerStatus.serverAvailableElseSleep(tn, ProcessNames.FORCEWRITE);
        Admin.forceWrite(process, "abort");
        ServerStatus.serverAvailableElseSleep(tn, ProcessNames.ACK);
        HostCommunicator.ack(process);
        ServerStatus.serverAvailableElseSleep(tn, ProcessNames.ABORT);
        Admin.abort(process);
        ServerStatus.serverAvailableElseSleep(tn, ProcessNames.FORGET);
        Admin.forget(process);
        ServerStatus.serverAvailableElseSleep(tn, ProcessNames.END);
    }
}
