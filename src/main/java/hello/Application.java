package hello;

import hello.threads.Commit;
import hello.threads.Prepare;
import org.graalvm.compiler.core.CompilerThread;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@SpringBootApplication
@RestController
public class Application {

    @RequestMapping("/")
    public String home() {
        return "Hello Docker World";
    }

    @RequestMapping("/status")
    public String status(@RequestBody String process){
        return Admin.getStatus(process);
    }

    @RequestMapping("/prepare")
    public void prepare(@RequestBody String process){
        Thread prepareThread = new Prepare(process);
        prepareThread.start();
        return;
    }

    @RequestMapping("/commit")
    public void commit(@RequestBody String status){
        Thread commitThread = new Commit(status);
        commitThread.start();
        return;
    }



    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
