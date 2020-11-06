package hello;

import java.util.*;

public class Admin {

    private static Map<String, String> processStatus = new HashMap<>();
    private static List<String> nonVolMemory = new ArrayList<>();
    private static List<String> volMemory = new ArrayList<>();
    private static Map<String,String> committedProcesses = new HashMap<>();
    private static Map<String,String> uncommittedProcess = new HashMap<>();

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

    public static String getStatus(String process){
        return processStatus.getOrDefault(process, "no process with this name");
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
        return;
    }

    public static void newProcess(String process, String value){
        uncommittedProcess.put(process, value);
        processStatus.put(process,"process has made changes to DB");
    }
}
