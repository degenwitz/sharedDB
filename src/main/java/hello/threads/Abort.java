package hello.threads;

import hello.Admin;
import hello.Host;
import hello.processes.ProcessNames;
import hello.processes.ServerStatus;
import hello.processes.ThreadName;

public class Abort extends restThreads{

    private static ThreadName tn = ThreadName.ABORT;

    public Abort (String process){
        super(process);
    }

    public void run(){
        if (!ServerStatus.serverAvaliable(tn, ProcessNames.CHANGESTATUS)) return;
        Admin.changeStatus(process,"abort");
        if (!ServerStatus.serverAvaliable(tn, ProcessNames.FORCEWRITE)) return;;
        Admin.forceWrite(process, "abort");
        if (!ServerStatus.serverAvaliable(tn, ProcessNames.ACK)) return;;
        Host.ack(process);
        if (!ServerStatus.serverAvaliable(tn, ProcessNames.ABORT)) return;;
        Admin.abbort(process);
        if (!ServerStatus.serverAvaliable(tn, ProcessNames.FORGET)) return;;
        Admin.forget(process);
        if (!ServerStatus.serverAvaliable(tn, ProcessNames.END)) return;;
    }
}
