package hello;

import hello.processes.ProcessNames;
import hello.processes.ServerStatus;
import hello.threads.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecoveryService {

    public static void handleSubPrepare(String process){
        String resp = HostCommunicator.recoverySubPrepare(process);
        if (resp.equals("commit")){
            Thread commit = new Commit(process);
            commit.start();
        } else if(resp.equals("abort")){
            Thread abort = new Abort(process);
            abort.start();
        } else if(resp.equals("prepare")){
            Admin.changeStatus(process,"prepare");
            if(!Admin.getValue(process).equals("defective")){
                HostCommunicator.yesVote(process);
            } else {
                HostCommunicator.noVote(process);
            }
        }
    }

    public static void handleSubCommit(String process){
        HostCommunicator.ack(process);
        Admin.commit(process);
        Admin.forget(process);
    }

    public static void handleSubAbbort(String process){
        HostCommunicator.ack(process);
        Admin.abort(process);
        Admin.forget(process);
    }

    public static void recover(){
        List<String> nonVolMemory = Admin.getNonVolMemory();
        Map<String,String> uncommitedProcesses = Admin.getUncommittedProcess();

        Map<String, List<String>> writtenMemory = getStringListMap(nonVolMemory);

        ClientInfo ci = new ClientInfo();
        ci.setUpFromMemory(writtenMemory);
        HostCommunicator.setup(ci);

        if(! ci.getIsCoordinator()){
            for(Map.Entry<String,String> proc: uncommitedProcesses.entrySet()){
                String process = proc.getKey();
                if(writtenMemory.get(process) == null){
                    continue;
                }
                if(writtenMemory.get(process).contains("commit")){
                    handleSubCommit(process);
                } else if(writtenMemory.get(process).contains("abort")){
                    handleSubAbbort(process);
                } else if(writtenMemory.get(process).contains("prepare")){
                    handleSubPrepare(process);
                } else {
                }
            }
        } else {
            for(Map.Entry<String,String> proc: uncommitedProcesses.entrySet()){
                String process = proc.getKey();
                if(writtenMemory.get(process) == null){

                } else if(writtenMemory.get(process).contains("end")){
                    continue;
                } else if(writtenMemory.get(process).contains("commit")){
                    for(String port:HostCommunicator.getCI().getSubPorts()){
                        Thread commit = new SendCommitToClient(process,port);
                        commit.start();
                    }
                } else if(writtenMemory.get(process).contains("abort")){
                    for(String port:HostCommunicator.getCI().getSubPorts()){
                        Thread commit = new SendAbortToClient(process,port);
                        commit.start();
                    }
                } else {
                    List<String> statuses= HostCommunicator.recoveryGetSubStatus(process);
                    if( statuses.contains("prepare")){
                        Thread restart = new HostCommit(process);
                        restart.start();
                    }
                }
            }
        }

    }

    public static Map<String, List<String>> getStringListMap(List<String> nonVolMemory) {
        Map<String,List<String>> writtenMemory = new HashMap<>();
        for(String s: nonVolMemory){
            String process, content;
            int splitter = s.indexOf(":");
            process = s.substring(1,splitter);
            content = s.substring(splitter+1, s.length()-1);
            if(writtenMemory.containsKey(process)){
                writtenMemory.get(process).add(content);
            } else {
                List l = new ArrayList<String>();
                l.add(content);
                writtenMemory.put(process,l);
            }
        }
        return writtenMemory;
    }
}
