package hello.threads;

import hello.HostCommunicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SendCommitToClient extends restThreads{

    private String port;

    public SendCommitToClient(String s, String p){
        super(s);
        port = p;
    }

    public void run(){
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = HostCommunicator.getCI().getAddress() +":"+ port +"/commit/" + process;
        ResponseEntity<String> response
                = restTemplate.postForEntity(fooResourceUrl, null,  String.class);
    }
}
