package hello;

import java.util.List;

public class ClientInfo {

    private String hostPort = "8080";

    private String address = "http://172.17.0.1";

    private String myPort = "8090";

    private List<String> subPorts;

    public List<String> getSubPorts() {
        return subPorts;
    }

    public void setSubPorts(List<String> subPorts) {
        this.subPorts = subPorts;
    }

    private boolean isCoordinator = false;

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

    public boolean getIsCoordinator() {return isCoordinator;}

    public void setIsCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }
}
