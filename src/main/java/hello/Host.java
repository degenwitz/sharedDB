package hello;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class Host {

    private static String getHostPort(){
        return "8080";
    }

    private static String adress(){ return "http://172.17.0.1";}

    private static String myPortNumber(){
        return "8090";
    }

    public static void yesVote(String process){
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = adress() +":"+getHostPort()+"/yes_vote/" + process;
        ResponseEntity<String> response
                = restTemplate.postForEntity(fooResourceUrl + "/" + process, myPortNumber(),  String.class);
    }

    public static void ack(String process){
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = "http:/localhost:"+getHostPort()+"/ack/"+process;
        System.out.println(fooResourceUrl);
        ResponseEntity<String> response
                = restTemplate.postForEntity(fooResourceUrl + "/" + process, myPortNumber(),  String.class);
        return;
    }
}
