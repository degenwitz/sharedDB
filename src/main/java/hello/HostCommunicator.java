package hello;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class HostCommunicator {

    private static ClientInfo clientInfo;

    public static ClientInfo getCI(){
        return clientInfo;
    }

    public static void setup(ClientInfo ci){
        clientInfo = ci;
    }

    public static void yesVote(String process){
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = clientInfo.getAddress() +":"+ clientInfo.getHostPort()+"/yes_vote/" + process;
        ResponseEntity<String> response
                = restTemplate.postForEntity(fooResourceUrl, clientInfo.getMyPort(),  String.class);
    }

    public static void noVote(String process){
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = clientInfo.getAddress() +":"+ clientInfo.getHostPort()+"/no_vote/" + process;
        ResponseEntity<String> response
                = restTemplate.postForEntity(fooResourceUrl, clientInfo.getMyPort(),  String.class);
    }

    public static void ack(String process){
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = clientInfo.getAddress()+":"+clientInfo.getHostPort()+"/ack/"+process;
        ResponseEntity<String> response
                = restTemplate.postForEntity(fooResourceUrl, clientInfo.getMyPort(),  String.class);
        return;
    }

    public static String recoverySubPrepare(String process){
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = clientInfo.getAddress()+":"+clientInfo.getHostPort()+"/process/"+process+"/recovery/prepared";
        ResponseEntity<String> response
                = restTemplate.getForEntity(fooResourceUrl, String.class);
        return response.getBody();
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
        for(String sub: clientInfo.getSubPorts()){
            RestTemplate restTemplate = new RestTemplate();
            String fooResourceUrl
                    = clientInfo.getAddress()+":"+sub+"/process/"+process+"/recovery/sub/status";
            ResponseEntity<String> response
                    = restTemplate.getForEntity(fooResourceUrl,  String.class);

            if(response.getStatusCode() == HttpStatus.OK){
                statuses.add(response.getBody());
            }
        }
        return statuses;
    }
}
