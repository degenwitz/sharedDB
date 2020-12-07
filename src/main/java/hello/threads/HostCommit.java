package hello.threads;

import hello.Admin;
import hello.ClientInfo;
import hello.HostCommunicator;
import hello.processes.ProcessNames;
import hello.processes.ServerStatus;
import hello.processes.ThreadName;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HostCommit extends restThreads{
    int sleepTime;

    private static ThreadName prepared = ThreadName.PREPARE;
    private static ThreadName commit = ThreadName.COMMIT;
    private static ThreadName abort = ThreadName.ABORT;

    public HostCommit(String p){
        super(p);
        sleepTime = HostCommunicator.getCI().getSleepTimer();
    }

    public void run(){
        ServerStatus.serverAvailableElseSleep(prepared, ProcessNames.SENDPREPARE);
        Admin.resetProcessSubordinates(process);
        List<String> ports = HostCommunicator.getCI().getSubPorts();
        for(String port:ports){
            try {
                Thread t = new SendPrepareToClient(process, port);
                t.start();
            } catch (Exception e){
            }
        }
        try {
            ServerStatus.serverAvailableElseSleep(prepared, ProcessNames.GETVOTES);
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

            if (readyToCommit) {
                ServerStatus.serverAvailableElseSleep(commit, ProcessNames.FORCEWRITE);
                Admin.forceWrite(process, "commit ".concat(Arrays.toString(ports.toArray())));
                ServerStatus.serverAvailableElseSleep(commit, ProcessNames.SENDCOMMIT);
                for (String p: ports) {
                    RestTemplate restTemplate = new RestTemplate();
                    String fooResourceUrl
                            = HostCommunicator.getCI().getAddress() +":"+ p +"/commit/" + process;
                    ResponseEntity<String> r = restTemplate.postForEntity(fooResourceUrl, null, String.class);
                    map.put(p, ProcessNames.COMMIT);
                }
            }
            else {
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
}
