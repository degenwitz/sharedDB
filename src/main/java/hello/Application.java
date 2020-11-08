package hello;

import hello.processes.ServerStatus;
import hello.threads.Abort;
import hello.threads.Commit;
import hello.threads.Prepare;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class Application {

    @PostMapping("/client/setup")
    public void setupClient(@RequestBody ClientInfo ci){
        HostCommunicator.setup(ci);
    }

    @RequestMapping("/client/setup")
    public ClientInfo getCI(){
        return HostCommunicator.getCI();
    }

    @RequestMapping("/status/{process}")
    public String status(@PathVariable("process") String process){
        if(ServerStatus.isServerDown()){ return "server is down";}
        return Admin.getStatus(process);
    }

    @PostMapping("/process/{process}")
    public void status(@PathVariable("process") String process, @RequestBody String content){
        Admin.newProcess(process, content);
    }

    @PostMapping("/serverstatus/setstop")
    public void serverStop(@RequestBody ServerStatus serverStatus){
        ServerStatus.setup(serverStatus);
    }

    @PostMapping("/serverstatus/reset")
    public void resetServer(){
        ServerStatus.resetServer();
    }

    @PostMapping("/prepare/{process}")
    public void prepare(@PathVariable("process") String process){
        Thread prepareThread = new Prepare(process);
        prepareThread.start();
        return;
    }

    @PostMapping("/commit/{process}")
    public void commit(@PathVariable("process") String status){
        Thread commitThread = new Commit(status);
        commitThread.start();
        return;
    }

    @PostMapping("/abort/{process}")
    public void abort(@PathVariable("process") String process){
        Thread abortThread = new Abort(process);
        abortThread.start();
        return;
    }

    @PostMapping("/yes_vote/{process}")
    public void yesVote(@PathVariable("process") String process, @RequestBody String port) {
        // TODO
    }

    @PostMapping("/no_vote/{process}")
    public void noVote(@PathVariable("process") String process) {
        // TODO
    }

    //just for testing
    @PostMapping("/acktest/{process}")
    public void ackTest(@PathVariable("process") String process){
        Admin.changeStatus(process, "ackWorked");
        return;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
