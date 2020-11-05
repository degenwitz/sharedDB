package hello;

import hello.threads.Abort;
import hello.threads.Commit;
import hello.threads.Prepare;
import org.graalvm.compiler.core.CompilerThread;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@SpringBootApplication
@RestController
public class Application {

    @RequestMapping("/status/{process}")
    public String status(@PathVariable("process") String process){
        return Admin.getStatus(process);
    }

    @PostMapping("/process/{process}")
    public void status(@PathVariable("process") String process, @RequestBody String content){
        Admin.newProcess(process, content);
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

    //just for testing
    @PostMapping("/acktest/{process}")
    public void ackTest(@PathVariable("process") String process){
        Admin.changeStatus(process, "ackWorked");
        return;
    }

    //just for testing
    @PostMapping("/test/{process}")
    public String ackTest2(@PathVariable("process") String process){
        return Host.ack2(process);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
