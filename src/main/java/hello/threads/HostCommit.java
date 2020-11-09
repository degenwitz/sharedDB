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
    public HostCommit(String p){
        super(p);
    }

    public void run(){
        Admin.changeStatus(process, "is coord, starting comitting");
        Admin.resetProcessSubordinates(process);
        List<String> ports = HostCommunicator.getCI().getSubPorts();
        for(String port:ports){
            Thread t = new SendPrepareToClient(process,port);
            t.start();
        }
        Admin.appendStatus(process, "sent all prepares");
        try {
            Thread.sleep(10000);
            Map<String, ProcessNames> map = Admin.getProcessSubordinates().getOrDefault(process, null);
            boolean readyToCommit = !map.containsValue(ProcessNames.NOVOTE);
            for(String port:ports){
                if(!map.containsKey(port)){readyToCommit = false; break; }
            }
            if (readyToCommit) {
                Admin.forceWrite(process, "commit ".concat(Arrays.toString(ports.toArray())));
                Admin.appendStatus(process, "all votes yes, will commit now ");
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
                Admin.changeStatus(process, "not enough yes votes, will abort now");
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
        Admin.appendStatus(process, "done");
    }
}
