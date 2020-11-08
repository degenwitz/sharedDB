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
        if (!ServerStatus.serverAvailable(tn, ProcessNames.CHANGESTATUS)) return;
        Admin.changeStatus(process,"abort");
        if (!ServerStatus.serverAvailable(tn, ProcessNames.FORCEWRITE)) return;
        Admin.forceWrite(process, "abort");
        if (!ServerStatus.serverAvailable(tn, ProcessNames.ACK)) return;
        HostCommunicator.ack(process);
        if (!ServerStatus.serverAvailable(tn, ProcessNames.ABORT)) return;
        Admin.abort(process);
        if (!ServerStatus.serverAvailable(tn, ProcessNames.FORGET)) return;
        Admin.forget(process);
        if (!ServerStatus.serverAvailable(tn, ProcessNames.END)) return;
    }
}
