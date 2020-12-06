package hello.threads;

import hello.Admin;
import hello.ClientInfo;
import hello.HostCommunicator;
import hello.processes.ProcessNames;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HostCommit extends restThreads{
    int sleepTime;
    public HostCommit(String p){
        super(p);
        sleepTime = HostCommunicator.getCI().getSleepTimer();
    }

    public void run(){
        sleep();
        Admin.resetProcessSubordinates(process);
        List<String> ports = HostCommunicator.getCI().getSubPorts();
        for(String port:ports){
            try {
                Thread t = new SendPrepareToClient(process, port);
                t.start();
            } catch (Exception e){
            }
        }
        sleep();
        try {
            Boolean readyToCommit = null;
            Map<String, ProcessNames> map;
            do {
                map = Admin.getProcessSubordinates().getOrDefault(process, null);
                readyToCommit = !map.containsValue(ProcessNames.NOVOTE);
                for (String port : ports) {
                    if (!map.containsKey(port)) {
                        readyToCommit = null;
                        break;
                    }
                }
                sleep(100);
            } while (readyToCommit == null);
            sleep();
            if (readyToCommit) {
                Admin.forceWrite(process, "commit ".concat(Arrays.toString(ports.toArray())));
                sleep();
                for (String p: ports) {
                    RestTemplate restTemplate = new RestTemplate();
                    String fooResourceUrl
                            = HostCommunicator.getCI().getAddress() +":"+ p +"/commit/" + process;
                    ResponseEntity<String> r = restTemplate.postForEntity(fooResourceUrl, null, String.class);
                    map.put(p, ProcessNames.COMMIT);
                }
            }
            else {
                Admin.forceWrite(process, "abort");
                sleep();
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

    void sleep(){
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
