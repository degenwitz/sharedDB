package hello.threads;

import hello.Admin;
import hello.HostCommunicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SendPrepareToClient extends restThreads{

    private String port;

    public SendPrepareToClient(String s, String p){
        super(s);
        port = p;
    }

    public void run(){
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = HostCommunicator.getCI().getAddress() +":"+ port +"/prepare/" + process;
        Admin.changeStatus(process, Admin.getStatus(process) + " : " + fooResourceUrl);
        ResponseEntity<String> response
                = restTemplate.postForEntity(fooResourceUrl, null,  String.class);
    }
}
