package hello.processes;

import hello.Admin;
import hello.HostCommunicator;

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

    public static void serverAvailableElseSleep(ThreadName tn, ProcessNames pn){
        if( serverStatus == null){
            return;
        }
        if(serverDown){
            try{
                Thread.sleep(HostCommunicator.getCI().getSleepTimer());
                return;
            } catch(java.lang.InterruptedException e){
                System.out.println(e);
            }
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
            try{
                Thread.sleep(HostCommunicator.getCI().getSleepTimer());
            } catch(java.lang.InterruptedException e){
                System.out.println(e);
            }
            return;
        }
        return;
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
