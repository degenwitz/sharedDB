package hello.threads;

import hello.Admin;
import hello.Host;

public class Abort extends restThreads{

    public Abort (String process){
        super(process);
    }

    public void run(){
        Admin.changeStatus(process,"abort");
        Admin.forceWrite(process, "abort");
        Host.ack(process);
        Admin.abbort(process);
        Admin.forget(process);
    }
}
