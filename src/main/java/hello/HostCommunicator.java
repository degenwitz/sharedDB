package hello;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HostCommunicator {

    private static ClientInfo clientInfo;

    public static ClientInfo getCI(){
        if(clientInfo == null){
            List<String> nonVolMemory = Admin.getNonVolMemory();
            Map<String, List<String>> writtenMemory = RecoveryService.getStringListMap(nonVolMemory, Admin.WriteReason.FORCEWRITE);
            ClientInfo ci = new ClientInfo();
            ci.setUpFromMemory(writtenMemory);
        }
        return clientInfo;
    }

    public static void setup(ClientInfo ci){
        clientInfo = ci;
    }

    public static void yesVote(String process){
        Boolean looping = true;
        for(int i = 0; i < 5 && looping; ++i) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String fooResourceUrl
                        = clientInfo.getAddress() + ":" + clientInfo.getHostPort() + "/yes_vote/" + process;
                ResponseEntity<String> response
                        = restTemplate.postForEntity(fooResourceUrl, clientInfo.getMyPort(), String.class);
                looping = false;
            } catch (org.springframework.web.client.ResourceAccessException e) {
                Admin.__forcewrite("YesVote on process: " + process, "Coudn't reach: " + clientInfo.getHostPort(), Admin.WriteReason.DEBUGGING);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }
    }

    public static void noVote(String process){
        Boolean looping = true;
        for(int i = 0; i < 5 && looping; ++i) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String fooResourceUrl
                        = clientInfo.getAddress() +":"+ clientInfo.getHostPort()+"/no_vote/" + process;
                ResponseEntity<String> response
                        = restTemplate.postForEntity(fooResourceUrl, clientInfo.getMyPort(),  String.class);
                looping = false;
                } catch (org.springframework.web.client.ResourceAccessException e) {
                Admin.__forcewrite("NoVote on process: " + process, "Coudn't reach: " + clientInfo.getHostPort(), Admin.WriteReason.DEBUGGING);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }
    }

    public static void ack(String process){
        Boolean looping = true;
        for(int i = 0; i < 5 && looping; ++i) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String fooResourceUrl
                        = clientInfo.getAddress() + ":" + clientInfo.getHostPort() + "/ack/" + process;
                ResponseEntity<String> response
                        = restTemplate.postForEntity(fooResourceUrl, clientInfo.getMyPort(), String.class);
                looping = false;
            } catch (org.springframework.web.client.ResourceAccessException e) {
                Admin.__forcewrite("Ack on process: " + process, "Coudn't reach: " + clientInfo.getHostPort(), Admin.WriteReason.DEBUGGING);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }
        return;
    }

    public static String recoverySubPrepare(String process){
        try{
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = clientInfo.getAddress()+":"+clientInfo.getHostPort()+"/process/"+process+"/recovery/prepared";
        ResponseEntity<String> response
                = restTemplate.getForEntity(fooResourceUrl, String.class);
            return response.getBody();
        } catch (org.springframework.web.client.ResourceAccessException e){
            Admin.__forcewrite("Recovering Sub prepare process: " + process,"Coudn't reach: "+clientInfo.getHostPort(),Admin.WriteReason.DEBUGGING);
            return "no response";
        }
    }

    //to host
    public static String recoverySubPrePrepare(String process){
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = clientInfo.getAddress()+":"+clientInfo.getHostPort()+"/process/"+process+"/recovery/preprepare";
        ResponseEntity<String> response
                = restTemplate.getForEntity(fooResourceUrl, String.class);
        return response.getBody();
    }

    //from coordinator to subordinates
    public static String getStatusForPreprepare(String process, String port) throws Exception{
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = clientInfo.getAddress()+":"+port+"/process/"+process+"/recovery/sub/status";
        ResponseEntity<String> response
                = restTemplate.getForEntity(fooResourceUrl, String.class);
        return response.getBody();
    }

    public static List<String> recoveryGetSubStatus(String process){
        List<String> statuses = new ArrayList<>();
        for (String sub : clientInfo.getSubPorts()) {
            RestTemplate restTemplate = new RestTemplate();
            String fooResourceUrl
                    = clientInfo.getAddress() + ":" + sub + "/process/" + process + "/recovery/sub/status";
            ResponseEntity<String> response
                    = restTemplate.getForEntity(fooResourceUrl, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                statuses.add(response.getBody());
            }
        }
        return statuses;
    }
}
