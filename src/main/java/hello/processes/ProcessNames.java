package hello.processes;

public enum ProcessNames {
    WRITE ("write"), FORCEWRITE ("forcewrite"), COMMIT ("commit"), ABORT ("abort"),
    ACK ("ack"), CHANGESTATUS ("changestatus"), FORGET ("forget"), YESVOTE ("yesvote"),
    NOVOTE ("novote"), END ("end");

    private final String name;

    private ProcessNames(String s){
        name = s;
    }

    public boolean equalsName(String s){
        return name.equals(s);
    }

    public String toString(){
        return name;
    }
}
