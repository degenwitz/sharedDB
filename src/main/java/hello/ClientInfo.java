package hello;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ClientInfo {

	public static String getPrefix() {
		return "&&setup&&";
	}

	private String hostPort = "8080";

	private String address = "http://172.17.0.1";

	private String myPort = "8090";

	private List<String> subPorts;

	private boolean isCoordinator = false;

	private int sleepTimer = 0; //in miliseconds

	public int getSleepTimer() {
		return sleepTimer;
	}

	public void setSleepTimer(int sleepTimer) {
		this.sleepTimer = sleepTimer;
	}

	public List<String> getSubPorts() {
		return subPorts;
	}

	public void setSubPorts(List<String> subPorts) {
		this.subPorts = subPorts;
	}

	public String getHostPort() {
		return hostPort;
	}

	public void setHostPort(String hostPort) {
		this.hostPort = hostPort;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getMyPort() {
		return myPort;
	}

	public void setMyPort(String myPort) {
		this.myPort = myPort;
	}

	public boolean getIsCoordinator() {
		return isCoordinator;
	}

	public void setIsCoordinator(boolean isCoordinator) {
		this.isCoordinator = isCoordinator;
	}

	public void storeToFile() {
		Admin.forceWrite(getPrefix() + "hostPort", hostPort);
		Admin.forceWrite(getPrefix() + "address", address);
		Admin.forceWrite(getPrefix() + "myPort", myPort);
		Admin.forceWrite(getPrefix() + "subPort", subPorts.toString().replaceAll("\\s+", ""));
		Admin.forceWrite(getPrefix() + "isCoordinator", Boolean.toString(isCoordinator));
		Admin.forceWrite(getPrefix() + "sleepTimer", Integer.toString(sleepTimer));
	}

	public void setUpFromMemory(Map<String, List<String>> memory) {
		hostPort = memory.get(getPrefix() + "hostPort").get(0);
		address = memory.get(getPrefix() + "address").get(0);
		myPort = memory.get(getPrefix() + "myPort").get(0);
		subPorts = Arrays.asList(memory.get(getPrefix() + "subPort").get(0).replace("[", "").replace("]", "").split(","));
		sleepTimer = Integer.parseInt(memory.get(getPrefix() + "sleepTimer").get(0));
		isCoordinator = Boolean.parseBoolean(memory.get(getPrefix() + "isCoordinator").get(0));
	}
}
