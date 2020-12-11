package hello;

import hello.processes.ProcessNames;
import hello.processes.ServerStatus;
import hello.threads.*;

import java.util.*;

public class RecoveryService extends Thread{

    public static void handlePrePrepare(String process){
        String resp = HostCommunicator.recoverySubPrePrepare(process);
        Admin.__forcewrite("recovering process: " + process,"recived from coord: "+resp,Admin.WriteReason.DEBUGGING);
        if(!resp.equals("nothing")){
            if (resp.equals("commit")){
                Admin.__forcewrite("recovering process: " + process,"trying to commit again",Admin.WriteReason.DEBUGGING);
                Thread commit = new Commit(process);
                commit.start();
            } else if(resp.equals("abort")){
                Thread abort = new Abort(process);
                abort.start();
            } else if(resp.equals("prepare")){
                Admin.__forcewrite("recovering process: " + process,"trying to prepare again",Admin.WriteReason.DEBUGGING);
                Thread t = new Prepare(process);
                t.start();
            }
        }
    }

    public static void handleSubPrepare(String process){
        String resp = HostCommunicator.recoverySubPrepare(process);
        Admin.__forcewrite("recovering process: " + process,"Recovery Sub prepare response Admin: "+resp,Admin.WriteReason.DEBUGGING);
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

    public void run(){
        List<String> nonVolMemory = Admin.getNonVolMemory();
        Map<String,String> uncommitedProcesses = Admin.getUncommittedProcess();

        Map<String, List<String>> writtenMemory = getStringListMap(nonVolMemory, Admin.WriteReason.FORCEWRITE);

        ClientInfo ci = new ClientInfo();
        ci.setUpFromMemory(writtenMemory);
        HostCommunicator.setup(ci);

        Admin.__forcewrite("recovering", ci.getMyPort(), Admin.WriteReason.DEBUGGING);

        if(! ci.getIsCoordinator()){
            for(Map.Entry<String,String> proc: uncommitedProcesses.entrySet()){
                String process = proc.getKey();
                if(!writtenMemory.containsKey(process)){
                    Admin.__forcewrite("recovering process: " + process,"no record",Admin.WriteReason.DEBUGGING);
                    handlePrePrepare(process);
                }
                else if(writtenMemory.get(process).contains("commit")){
                    Admin.__forcewrite("recovering process: " + process,"contains commit",Admin.WriteReason.DEBUGGING);
                    handleSubCommit(process);
                } else if(writtenMemory.get(process).contains("abort")){
                    Admin.__forcewrite("recovering process: " + process,"contains abbort",Admin.WriteReason.DEBUGGING);
                    handleSubAbbort(process);
                } else if(writtenMemory.get(process).contains("prepare")){
                    Admin.__forcewrite("recovering process: " + process,"contains prepare",Admin.WriteReason.DEBUGGING);
                    handleSubPrepare(process);
                } else {
                    Admin.__forcewrite("recovering process: " + process,"internal server error",Admin.WriteReason.DEBUGGING);
                }
            }
        } else {
            for(Map.Entry<String,String> proc: uncommitedProcesses.entrySet()){
                try {
                    String process = proc.getKey();
                    String status = "nothing";
                    if (writtenMemory.containsKey(process)) {
                        for (String s : writtenMemory.get(process)) {
                            if (s.contains("end")) status = "end";
                            else if (s.contains("commit") && status != "end") status = "commit";
                            else if (s.contains("abort") && status != "end") status = "abort";
                        }
                    }
                    if (status == "end") {
                        Admin.__forcewrite("recovering process: " + process, "processes already handled", Admin.WriteReason.DEBUGGING);
                        continue;
                    } else if (status == "commit") {
                        Admin.__forcewrite("recovering process: " + process, "process should be comitted", Admin.WriteReason.DEBUGGING);
                        for (String port : HostCommunicator.getCI().getSubPorts()) {
                            Thread commit = new SendCommitToClient(process, port);
                            commit.start();
                        }
                        Admin.forceWrite(process, "end");
                    } else if (status == "abort") {
                        Admin.__forcewrite("recovering process: " + process, "process should be aborted", Admin.WriteReason.DEBUGGING);
                        for (String port : HostCommunicator.getCI().getSubPorts()) {
                            Thread commit = new SendAbortToClient(process, port);
                            commit.start();
                        }
                    } else {
                        Admin.__forcewrite("recovering process: " + process, "coordinator knows nothing", Admin.WriteReason.DEBUGGING);
                        List<String> statuses = HostCommunicator.recoveryGetSubStatus(process);
                        if (statuses.contains("prepare")) {
                            Thread restart = new HostCommit(process);
                            restart.start();
                        }
                    }
                }catch (Exception e){
                    Admin.__forcewrite("recovering process when crashed: ", e.toString() , Admin.WriteReason.DEBUGGING);
                }
            }
        }

    }

    public static Map<String, List<String>> getStringListMap(List<String> nonVolMemory, Admin.WriteReason wr) {
        Map<String,List<String>> writtenMemory = new HashMap<>();
        for(String s: nonVolMemory){
            if(s.charAt(0) == wr.getSeparator()) {
                String process, content;
                int splitter = s.indexOf(":");
                process = s.substring(1, splitter);
                content = s.substring(splitter + 1, s.length() - 1);
                if (writtenMemory.containsKey(process)) {
                    writtenMemory.get(process).add(content);
                } else {
                    List l = new ArrayList<String>();
                    l.add(content);
                    writtenMemory.put(process, l);
                }
            }
        }
        return writtenMemory;
    }

    public static String askSubsAboutProcess(String process){
        String status = "nothing";
        for( String port: HostCommunicator.getCI().getSubPorts()){
            try {
                String r = HostCommunicator.getStatusForPreprepare(process, port);
                if( r.equals("commit") || r.equals("abort")){
                    status = r;
                } else if(!(status.equals("commit") || status.equals("abort")) && r.equals("prepare")){
                    status = r;
                }
            } catch (Exception e){
                continue;
            }
        }
        return status;
    }
}
