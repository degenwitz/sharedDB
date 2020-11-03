package hello;

import java.util.*;

public class Admin {

    private static Map<String, String> processStatus = new HashMap<>();
    private static List<String> nonVolMemory = new ArrayList<>();
    private static List<String> volMemory = new ArrayList<>();
    private static Set<String> commitedProcesses = new HashSet<>();

    public static void forceWrite(String status, String inhalt){
        nonVolMemory.add("$"+status + ":" + inhalt + "$");
    }

    public static void normalWrite(String status, String inhalt){
        volMemory.add("$"+status + ":" + inhalt + "$");
    }

    public static void changeStatus(String process, String status){
        if(status == "forget"){
            processStatus.remove(process);
        }
        else if(status.contains(process)){
            processStatus.replace(process,status);
        } else {
            processStatus.put(process,status);
        }
    }

    public static String getStatus(String process){
        return processStatus.get(process);
    }

    public static void commit(String process){
        commitedProcesses.add(process);
    }

    public static void forget(String process){
        return;
    }

}
