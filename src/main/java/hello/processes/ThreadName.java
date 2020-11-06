package hello.processes;

public enum ThreadName {
    COMMIT("commit"), PREPARE("prepare"), ABORT("abort"), ;

    private final String name;

    private ThreadName(String s){
        name = s;
    }

    public boolean equalsName(String s){
        return name.equals(s);
    }

    public String toString(){
        return name;
    }
}
