package hello.threads;

import hello.Admin;
import hello.Host;

public class Commit extends restThreads {

    public Commit(String process){
        super(process);
    }

    public void run(){
        Admin.changeStatus(process,"commit");
        Admin.forceWrite(process, "commit");
        Host.ack(process);
        Admin.commit(process);
        //Admin.forget(process);
    }
}
