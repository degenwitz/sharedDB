package hello.threads;

import hello.Admin;
import hello.Host;

public class Prepare extends restThreads {

    public Prepare (String process){
        super(process);
    }

    public void run(){
        Admin.forceWrite(process, "prepare");
        Admin.changeStatus(process,"prepare");
        Host.yesVote(process);
    }
}
