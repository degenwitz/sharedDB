package hello;

import com.sun.org.apache.xpath.internal.operations.Bool;
import hello.processes.ProcessNames;
import hello.processes.ServerStatus;
import hello.processes.ThreadName;
import hello.threads.Abort;
import hello.threads.Commit;
import hello.threads.HostCommit;
import hello.threads.Prepare;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import sun.reflect.generics.tree.Tree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SpringBootApplication
@RestController
public class Application {

    @PostMapping("/prepare/{process}")
    public void prepare(@PathVariable("process") String process){
        Thread prepareThread = new Prepare(process);
        prepareThread.start();
    }

    @PostMapping("/commit/{process}")
    public void commit(@PathVariable("process") String process){
        if (HostCommunicator.getCI().getIsCoordinator()){
            Thread commitThread = new HostCommit(process);
            commitThread.start();
        } else {
            Thread commitThread = new Commit(process);
            commitThread.start();
        }
    }

    @PostMapping("/abort/{process}")
    public void abort(@PathVariable("process") String process){
        if(HostCommunicator.getCI().getIsCoordinator()){

        } else {
            Thread abortThread = new Abort(process);
            abortThread.start();
        }
        return;
    }

    @PostMapping("/yes_vote/{process}")
    public void yesVote(@PathVariable("process") String process, @RequestBody String subport) {
        ServerStatus.serverAvailableElseSleep(ThreadName.PREPARE, ProcessNames.GETVOTES);
        if (HostCommunicator.getCI().getIsCoordinator()) {
            Admin.registerVote(process, subport, ProcessNames.YESVOTE);
        }
        try {
            ThreadName prepared = ThreadName.PREPARE;
            List<String> ports = HostCommunicator.getCI().getSubPorts();
            ServerStatus.serverAvailableElseSleep(prepared, ProcessNames.GETVOTES);
            Boolean readyToCommit = null;
            Map<String, ProcessNames> map;
            map = Admin.getProcessSubordinates().getOrDefault(process, null);
            readyToCommit = !map.containsValue(ProcessNames.NOVOTE);
            for (String port : ports) {
                if (!map.containsKey(port)) {
                    return;
                }
            }

            if (readyToCommit) {
                ThreadName commit = ThreadName.COMMIT;
                ServerStatus.serverAvailableElseSleep(commit, ProcessNames.FORCEWRITE);
                Admin.forceWrite(process, "commit ".concat(Arrays.toString(ports.toArray())));
                ServerStatus.serverAvailableElseSleep(commit, ProcessNames.SENDCOMMIT);
                for (String p: ports) {
                    Boolean looping = true;
                    for(int k = 0; k < 10 && looping; ++k) {
                        try {
                            RestTemplate restTemplate = new RestTemplate();
                            String fooResourceUrl
                                    = HostCommunicator.getCI().getAddress() + ":" + p + "/commit/" + process;
                            ResponseEntity<String> r = restTemplate.postForEntity(fooResourceUrl, null, String.class);
                            map.put(p, ProcessNames.COMMIT);
                            looping = false;
                        }catch (Exception e){
                            Admin.__forcewrite("trying to send commit: " + process + " : " + p,"error: " + e.toString(),Admin.WriteReason.DEBUGGING);
                            Thread.sleep(3000);
                        }
                    }
                }
            }
            else {
                ThreadName abort = ThreadName.ABORT;
                ServerStatus.serverAvailableElseSleep(abort, ProcessNames.FORCEWRITE);
                Admin.forceWrite(process, "abort");
                ServerStatus.serverAvailableElseSleep(abort, ProcessNames.SENDABORT);
                for (String p: ports) {
                    if (map.get(p) != ProcessNames.NOVOTE) {
                        RestTemplate restTemplate = new RestTemplate();
                        String fooResourceUrl
                                = HostCommunicator.getCI().getAddress() +":"+ p +"/abort/" + process;
                        ResponseEntity<String> r = restTemplate.postForEntity(fooResourceUrl, null, String.class);
                        map.put(p, ProcessNames.ABORT);
                    }
                }

            }
        } catch (Exception e){}
    }

    @PostMapping("/no_vote/{process}")
    public void noVote(@PathVariable("process") String process, @RequestBody String subport) {
        ServerStatus.serverAvailableElseSleep(ThreadName.PREPARE, ProcessNames.GETVOTES);
        if (HostCommunicator.getCI().getIsCoordinator()) {
            Admin.registerVote(process, subport, ProcessNames.NOVOTE);
        }
        try {
        ThreadName prepared = ThreadName.PREPARE;
        List<String> ports = HostCommunicator.getCI().getSubPorts();
        ServerStatus.serverAvailableElseSleep(prepared, ProcessNames.GETVOTES);
        Boolean readyToCommit = null;
        Map<String, ProcessNames> map;
        map = Admin.getProcessSubordinates().getOrDefault(process, null);
        readyToCommit = !map.containsValue(ProcessNames.NOVOTE);
        for (String port : ports) {
            if (!map.containsKey(port)) {
                return;
            }
        }

        if (readyToCommit) {
            ThreadName commit = ThreadName.COMMIT;
            ServerStatus.serverAvailableElseSleep(commit, ProcessNames.FORCEWRITE);
            Admin.forceWrite(process, "commit ".concat(Arrays.toString(ports.toArray())));
            ServerStatus.serverAvailableElseSleep(commit, ProcessNames.SENDCOMMIT);
            for (String p: ports) {
                Boolean looping = true;
                for(int k = 0; k < 10 && looping; ++k) {
                    try {
                        RestTemplate restTemplate = new RestTemplate();
                        String fooResourceUrl
                                = HostCommunicator.getCI().getAddress() + ":" + p + "/commit/" + process;
                        ResponseEntity<String> r = restTemplate.postForEntity(fooResourceUrl, null, String.class);
                        map.put(p, ProcessNames.COMMIT);
                        looping = false;
                    }catch (Exception e){
                        Admin.__forcewrite("trying to send commit: " + process + " : " + p,"error: " + e.toString(),Admin.WriteReason.DEBUGGING);
                        Thread.sleep(3000);
                    }
                }
            }
        }
        else {
            ThreadName abort = ThreadName.ABORT;
            ServerStatus.serverAvailableElseSleep(abort, ProcessNames.FORCEWRITE);
            Admin.forceWrite(process, "abort");
            ServerStatus.serverAvailableElseSleep(abort, ProcessNames.SENDABORT);
            for (String p: ports) {
                if (map.get(p) != ProcessNames.NOVOTE) {
                    RestTemplate restTemplate = new RestTemplate();
                    String fooResourceUrl
                            = HostCommunicator.getCI().getAddress() +":"+ p +"/abort/" + process;
                    ResponseEntity<String> r = restTemplate.postForEntity(fooResourceUrl, null, String.class);
                    map.put(p, ProcessNames.ABORT);
                }
            }

        }
    } catch (Exception e){}
    }

    @PostMapping("/ack/{process}")
    public void ack(@PathVariable("process") String process, @RequestBody String port) {
        ServerStatus.serverAvailableElseSleep(ThreadName.ABORT, ProcessNames.GETACK);
        ServerStatus.serverAvailableElseSleep(ThreadName.COMMIT, ProcessNames.GETACK);
        if (HostCommunicator.getCI().getIsCoordinator()) {
            Admin.ack(process, port);
        }
    }

    //for setup and similar things

    @PostMapping("/client/setup")
    public void setupClient(@RequestBody ClientInfo ci){
        HostCommunicator.setup(ci);
        ci.storeToFile();
    }

    @PostMapping("/process/{process}")
    public void status(@PathVariable("process") String process, @RequestBody String content){
        Admin.newProcess(process, content);
    }

    //for recovery-services
    @GetMapping("/process/{process}/recovery/prepared")
    public String recoverAfterPrepare(@PathVariable("process") String process){
        Map<String,List<String>> processMemories = RecoveryService.getStringListMap(Admin.getNonVolMemory(), Admin.WriteReason.FORCEWRITE);
        if(!processMemories.containsKey(process)){
            return "prepare";
        }
        List<String> l = processMemories.get(process);
        for( String s: l) {
            if (s.contains("commit")) {
                return "commit";
            }
            if (s.contains("abort")) {
                return "abort";
            }
        }
        return "system error";
    }


    @GetMapping("/process/{process}/recovery/preprepare")
    public String beforePrepare(@PathVariable("process") String process){
        System.out.println("smth");
        Map<String,List<String>> processMemories = RecoveryService.getStringListMap(Admin.getNonVolMemory(), Admin.WriteReason.FORCEWRITE);
        if(!processMemories.containsKey(process)){
            return RecoveryService.askSubsAboutProcess(process);
        }
        List<String> l = processMemories.get(process);
        for( String s: l) {
            if (s.contains("commit")) {
                return "commit";
            }
            if (s.contains("abort")) {
                return "abort";
            }
        }
        return "system error";
    }

    @GetMapping("/process/{process}/recovery/sub/status")
    public String recoverStatusSub(@PathVariable("process") String process){
        Map<String,List<String>> processMemories = RecoveryService.getStringListMap(Admin.getNonVolMemory(), Admin.WriteReason.FORCEWRITE);
        if(! processMemories.containsKey(process)){
            return "nothing";
        }
        List<String> l = processMemories.get(process);
        for(String s: l) {
            if (s.contains("commit")) {
                return "commit";
            }
            if (s.contains("abort")) {
                return "abort";
            }
            if (s.contains("prepare")) {
                return "prepare";
            }
        }
        return "system error";
    }

    //manipulating tools
    @PostMapping("/process/{process}/abort")
    public void prepareToAbort(@PathVariable("process") String process){
        Admin.newProcess(process, "defective");
    }

    @PostMapping("/serverstatus/setstop")
    public void serverStop(@RequestBody ServerStatus serverStatus){
        ServerStatus.setup(serverStatus);
    }

    //Monitoring services
    @RequestMapping("/client/setup")
    public ClientInfo getCI(){
        return HostCommunicator.getCI();
    }

    @RequestMapping("/status/{process}")
    public String status(@PathVariable("process") String process){
        if(ServerStatus.isServerDown()){ return "server is down";}
        return Admin.getStatus(process);
    }

    @RequestMapping("/votes/{process}")
    public Map<String, ProcessNames> votes(@PathVariable("process") String process){
        return Admin.getProcessSubordinates().get(process);
    }

    @RequestMapping("/memory/nonVol")
    public List<String>  nonVolMemory(){
        return Admin.getNonVolMemory();
    }

    @RequestMapping("/processes/uncommited")
    public Map<String, String> unCommited(){
        return new TreeMap<String, String>(Admin.getUncommittedProcess());
    }

    @RequestMapping("/processes/handled")
    public Map<String, String> handled(){
        return new TreeMap<String, String>(Admin.getHandledProcesses());
    }



    public static void main(String[] args) {
        File log = new File("/logs/log.txt");
        if (log.exists()) {
            Thread recover = new RecoveryService();
            recover.start();
        }
        SpringApplication.run(Application.class, args);
    }
}
