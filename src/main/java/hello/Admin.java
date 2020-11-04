package hello;

import java.util.*;

public class Admin {

    private static Map<String, String> processStatus = new HashMap<>();
    private static List<String> nonVolMemory = new ArrayList<>();
    private static List<String> volMemory = new ArrayList<>();
    private static Map<String,String> commitedProcesses = new HashMap<>();
    private static Map<String,String> uncommitedProcess = new HashMap<>();

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
        if(processStatus.containsKey(process)){
            return processStatus.get(process);
        } else {
            return "no process with this name";
        }
    }

    public static void commit(String process){
        if(uncommitedProcess.containsKey(process)) {
            commitedProcesses.put(process, uncommitedProcess.get(process));
        }
    }

    public static void abbort(String process) { return; }

    public static void forget(String process){
        uncommitedProcess.remove(process);
        processStatus.remove(process);
        return;
    }

    public static void newProcess(String process, String value){
        uncommitedProcess.put(process, value);
        processStatus.put(process,"process has maid changes to DB");
    }
}
