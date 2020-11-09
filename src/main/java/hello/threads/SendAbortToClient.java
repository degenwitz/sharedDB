package hello.threads;

import hello.HostCommunicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SendAbortToClient extends restThreads{

    private String port;

    public SendAbortToClient(String s, String p){
        super(s);
        port = p;
    }

    public void run(){
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = HostCommunicator.getCI().getAddress() +":"+ port +"/abort/" + process;
        ResponseEntity<String> response
                = restTemplate.postForEntity(fooResourceUrl, null,  String.class);
    }
}
