package hello.threads;

import hello.Admin;
import hello.Host;
import hello.processes.ProcessNames;
import hello.processes.ServerStatus;
import hello.processes.ThreadName;

public class Commit extends restThreads {

    private static ThreadName tn = ThreadName.COMMIT;

    public Commit(String process){
        super(process);
    }

    public void run(){
        if (!ServerStatus.serverAvaliable(tn, ProcessNames.CHANGESTATUS)) return;
        Admin.changeStatus(process,"commit");
        if (!ServerStatus.serverAvaliable(tn, ProcessNames.FORCEWRITE)) return;
        Admin.forceWrite(process, "commit");
        if (!ServerStatus.serverAvaliable(tn, ProcessNames.ACK)) return;
        Host.ack(process);
        if (!ServerStatus.serverAvaliable(tn, ProcessNames.COMMIT)) return;
        Admin.commit(process);
        if (!ServerStatus.serverAvaliable(tn, ProcessNames.FORGET)) return;
        Admin.forget(process);
        if (!ServerStatus.serverAvaliable(tn, ProcessNames.END)) return;
    }
}
