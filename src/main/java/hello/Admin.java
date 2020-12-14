package hello;

import hello.processes.ProcessNames;
import hello.processes.ServerStatus;
import hello.processes.ThreadName;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import java.util.*;

public class Admin {

	private static Map<String, String> processStatus = new HashMap<>();
	private static List<String> nonVolMemory = new ArrayList<>();
	private static List<String> volMemory = new ArrayList<>();
	private static Map<String, String> handledProcesses = new HashMap<>();
	private static Map<String, String> uncommittedProcess = new HashMap<>();
	private static Map<String, Map<String, ProcessNames>> processSubordinates = new HashMap<>();

	public static List<String> getNonVolMemory() {
		File f = new File("/logs/log.txt");
		nonVolMemory.clear();
		if (f.exists()) {
			try {
				BufferedReader fileReader = new BufferedReader(new FileReader(f));
				while (true) {
					String line = fileReader.readLine();
					if (line == null) break;
					nonVolMemory.add(line);
				}
				fileReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return nonVolMemory;
	}

	public static Map<String, Map<String, ProcessNames>> getProcessSubordinates() {  //{process,{port,yes_vote}}
		return processSubordinates;
	}

	public static void resetProcessSubordinates(String process) {  //{process,{port,yes_vote}}
		processSubordinates.put(process, new HashMap<>());
		return;
	}

	public static void forceWrite(String status, String content) {
		__forcewrite(status, content, WriteReason.FORCEWRITE);
	}

	public static void forceWriteUncommited(String process, String content) {
		__forcewrite(process, content, WriteReason.UNCOMMITEDPROCESSES);
	}

	public static void forceWriteCommited(String process, String content) {
		__forcewrite(process, content, WriteReason.COMMITEDPROCESSES);
	}

	public static void __forcewrite(String status, String content, WriteReason wr) {
		try {
			File file = new File("/logs/log.txt");
			file.createNewFile();
			FileWriter fw = new FileWriter(file, true);
			fw.write(wr.getSeparator() + status + ":" + content + wr.getSeparator() + "\n");
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void normalWrite(String status, String content) {
		volMemory.add("$" + status + ":" + content + "$");
	}

	public static void changeStatus(String process, String status) {
	    if(!processStatus.containsKey(process)){
	        processStatus.put(process,status);
	        return;
        }
		if (status.equals("forget")) {
			processStatus.remove(process);
		} else if (status.contains(process)) {
			processStatus.replace(process, status);
		} else {
			processStatus.put(process, status);
		}
	}

	public static void appendStatus(String process, String status) {
		changeStatus(process, getStatus(process) + " , " + status);
	}

	public static String getStatus(String process) {
		return processStatus.getOrDefault(process, "no process with this name");
	}

	public static Map<String, String> getUncommittedProcess() {
		if (uncommittedProcess.size() == 0) {
			Map<String, List<String>> writtenMemory = RecoveryService.getStringListMap(getNonVolMemory(), WriteReason.UNCOMMITEDPROCESSES);
			for (Map.Entry<String, List<String>> entry : writtenMemory.entrySet()) {
				String process = entry.getKey();
				String value = entry.getValue().get(entry.getValue().size() - 1);
				if (!value.equals("forgotten")) {
					uncommittedProcess.put(process, value);
				}
			}
		}
		return uncommittedProcess;
	}

	public static Map<String, String> getHandledProcesses() {
		if (handledProcesses.size() == 0) {
			Map<String, List<String>> writtenMemory = RecoveryService.getStringListMap(getNonVolMemory(), WriteReason.COMMITEDPROCESSES);
			for (Map.Entry<String, List<String>> entry : writtenMemory.entrySet()) {
				String process = entry.getKey();
				String value = entry.getValue().get(entry.getValue().size() - 1);
				handledProcesses.put(process, value);
			}
		}
		return handledProcesses;
	}

	public static String getValue(String process) {
		uncommittedProcess = getUncommittedProcess();
		return uncommittedProcess.get(process);
	}

	public static void commit(String process) {
		if (uncommittedProcess.containsKey(process)) {
			handledProcesses.put(process, "commited");
			forceWriteCommited(process, "commited");
		} else {
			Admin.__forcewrite("commiting: " + process, "already forgotten", Admin.WriteReason.DEBUGGING);
		}
	}

	public static void abort(String process) {
		if (uncommittedProcess.containsKey(process)) {
			handledProcesses.put(process, "aborted");
			forceWriteCommited(process, "aborted");
		}
	}

	public static void forget(String process) {
		uncommittedProcess.remove(process);
		forceWriteUncommited(process, "forgotten");
		processStatus.remove(process);
		processSubordinates.remove(process);
		return;
	}

	public static void newProcess(String process, String value) {
		uncommittedProcess.put(process, value);
		forceWriteUncommited(process, value);
		processStatus.put(process, "process has made changes to DB");
		processSubordinates.put(process, new HashMap<>());
		// TODO: which subordinates are part of process
	}

	public static synchronized void registerVote(String process, String port, ProcessNames vote) {
		Map<String, ProcessNames> map = processSubordinates.getOrDefault(process, null);
		if (map != null) {
			if(vote == ProcessNames.NOVOTE || vote == ProcessNames.YESVOTE){
				if(!map.containsKey(port)){
					map.put(port, vote);
				}
			} else {
				map.put(port, vote);
			}
		}
	}

	public static void ack(String process, String port) {
		try {
			Thread.sleep(HostCommunicator.getCI().getSleepTimer());
		} catch (Exception e) {
		}
		Map<String, ProcessNames> map = processSubordinates.getOrDefault(process, null);
		if (map != null) {
			map.put(port, ProcessNames.ACK);

			if (!map.containsValue(ProcessNames.COMMIT) && !map.containsValue(ProcessNames.ABORT) && !map.containsValue(ProcessNames.YESVOTE) && !map.containsValue(ProcessNames.NOVOTE)) {
				ServerStatus.serverAvailableElseSleep(ThreadName.COMMIT, ProcessNames.FORCEWRITE);
				ServerStatus.serverAvailableElseSleep(ThreadName.ABORT, ProcessNames.FORCEWRITE);
				Admin.forceWrite(process, "end");
				ServerStatus.serverAvailableElseSleep(ThreadName.COMMIT, ProcessNames.FORGET);
				ServerStatus.serverAvailableElseSleep(ThreadName.ABORT, ProcessNames.FORGET);
				Admin.changeStatus(process, "forget");
			}
		}
	}

	public enum WriteReason {
		FORCEWRITE('$'), COMMITEDPROCESSES('£'), UNCOMMITEDPROCESSES('§'), DEBUGGING('|');

		private final char separator;

		private WriteReason(char s) {
			separator = s;
		}

		public char getSeparator() {
			return separator;
		}
	}
}
