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
        try {
            RestTemplate restTemplate = new RestTemplate();
            String fooResourceUrl
                    = HostCommunicator.getCI().getAddress() +":"+ port +"/prepare/" + process;
                ResponseEntity<String> response
                        = restTemplate.postForEntity(fooResourceUrl, null, String.class);
        } catch (org.springframework.web.client.ResourceAccessException e){
            Admin.__forcewrite("commiting process: " + process,"Coudn't reach: "+port,Admin.WriteReason.DEBUGGING);
        }
    }
}
