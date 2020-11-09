package hello;

import hello.processes.ProcessNames;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

public class Admin {

    private static Map<String, String> processStatus = new HashMap<>();

    public static List<String> getNonVolMemory() {
        return nonVolMemory;
    }

    private static List<String> nonVolMemory = new ArrayList<>();
    private static List<String> volMemory = new ArrayList<>();
    private static Map<String,String> committedProcesses = new HashMap<>();
    private static Map<String,String> uncommittedProcess = new HashMap<>();

    private static Map<String, Map<String, ProcessNames>> processSubordinates = new HashMap<>();

    public static Map<String, Map<String, ProcessNames>> getProcessSubordinates() {  //{process,{port,yes_vote}}
        return processSubordinates;
    }
    public static void resetProcessSubordinates(String process) {  //{process,{port,yes_vote}}
        processSubordinates.put(process, new HashMap<>());
        return;
    }

    public static void forceWrite(String status, String content){
        nonVolMemory.add("$"+status + ":" + content + "$");
    }

    public static void normalWrite(String status, String content){
        volMemory.add("$"+status + ":" + content + "$");
    }

    public static void changeStatus(String process, String status){
        if(status.equals("forget")){
            processStatus.remove(process);
        }
        else if(status.contains(process)){
            processStatus.replace(process,status);
        } else {
            processStatus.put(process,status);
        }
    }

    public static void appendStatus(String process, String status){
        changeStatus(process, getStatus(process) + " , " + status);
    }

    public static String getStatus(String process){
        return processStatus.getOrDefault(process, "no process with this name");
    }

    public static String getValue(String process){
        return uncommittedProcess.get(process);
    }

    public static void commit(String process){
        if(uncommittedProcess.containsKey(process)) {
            committedProcesses.put(process, uncommittedProcess.get(process));
        }
    }

    public static void abort(String process) { return; }

    public static void forget(String process){
        uncommittedProcess.remove(process);
        processStatus.remove(process);
        processSubordinates.remove(process);
        return;
    }

    public static void newProcess(String process, String value){
        uncommittedProcess.put(process, value);
        processStatus.put(process,"process has made changes to DB");
        processSubordinates.put(process, new HashMap<>());
        // TODO: which subordinates are part of process
    }

    public static void registerVote(String process, String port, ProcessNames vote) {
        Map<String, ProcessNames> map = processSubordinates.getOrDefault(process, null);
        if (map != null) {
            map.put(port, vote);
        }
    }

    public static void ack(String process, String port) {
        Map<String, ProcessNames> map = processSubordinates.getOrDefault(process, null);
        if (map != null) {
            map.put(port, ProcessNames.ACK);

            if (!map.containsValue(ProcessNames.COMMIT) && !map.containsValue(ProcessNames.ABORT)) {
                Admin.normalWrite(process, "end");
                Admin.changeStatus(process, "forget");
            }
        }
    }
}
