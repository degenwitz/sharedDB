package hello;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class Host {

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
                = restTemplate.postForEntity(fooResourceUrl + "/" + process, clientInfo.getMyPort(),  String.class);
    }

    public static void ack(String process){
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = "http:/localhost:"+clientInfo.getHostPort()+"/ack/"+process;
        System.out.println(fooResourceUrl);
        ResponseEntity<String> response
                = restTemplate.postForEntity(fooResourceUrl + "/" + process, clientInfo.getMyPort(),  String.class);
        return;
    }
}
