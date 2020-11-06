package hello.processes;

import java.util.HashMap;
import java.util.Map;

public class ServerStatus {

    private static ServerStatus serverStatus;
    private static boolean serverDown = false;
    private static ThreadName currentThread;
    private static Map<ProcessNames,Integer> appearancesInThread;

    public static boolean isServerDown(){
        return serverDown;
    }

    public static boolean serverAvailable(ThreadName tn, ProcessNames pn){
        if( serverDown ){
            return false;
        }
        if( serverStatus == null){
            return true;
        }
        if(currentThread != tn){
            currentThread = tn;
            appearancesInThread = new HashMap<>();
            appearancesInThread.put(pn, 1);
        } else {
            if(appearancesInThread.containsKey(pn)){
                appearancesInThread.replace(pn, appearancesInThread.get(pn)+1);
            } else {
                appearancesInThread.put(pn,1);
            }
        }

        if(currentThread.equalsName(serverStatus.getThreadToStopAt()) && pn.equalsName(serverStatus.processToStopAt) && appearancesInThread.get(pn) == serverStatus.appearancesToStopAt){
            serverDown = true;
            currentThread = null;
            return false;
        }
        return true;
    }

    public static void setup(ServerStatus ss) {
        serverStatus = ss;
        serverDown = false;
    }

    public static void resetServer(){
        serverStatus = null;
        serverDown = false;
    }

    private String threadToStopAt;
    private String processToStopAt;
    private int appearancesToStopAt;

    public String getThreadToStopAt() {
        return threadToStopAt;
    }

    public void setThreadToStopAt(String threadToStopAt) {
        this.threadToStopAt = threadToStopAt;
    }

    public String getProcessToStopAt() {
        return processToStopAt;
    }

    public void setProcessToStopAt(String processToStopAt) {
        this.processToStopAt = processToStopAt;
    }

    public int getAppearancesToStopAt() {
        return appearancesToStopAt;
    }

    public void setAppearancesToStopAt(int appearancesToStopAt) {
        this.appearancesToStopAt = appearancesToStopAt;
    }
}
