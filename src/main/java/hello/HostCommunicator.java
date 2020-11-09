package hello;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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
}
