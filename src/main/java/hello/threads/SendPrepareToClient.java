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
        for(int i = 0; i < 10 ; ++i) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String fooResourceUrl
                        = HostCommunicator.getCI().getAddress() + ":" + port + "/prepare/" + process;
                ResponseEntity<String> response
                        = restTemplate.postForEntity(fooResourceUrl, null, String.class);
            } catch (org.springframework.web.client.ResourceAccessException e) {
                Admin.__forcewrite("preparing process: " + process, "Couldn't reach: " + port, Admin.WriteReason.DEBUGGING);
                try {
                    sleep(100);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }
    }
}
