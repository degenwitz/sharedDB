package hello.processes;

public enum ProcessNames {
	WRITE("write"), FORCEWRITE("forcewrite"), COMMIT("commit"), ABORT("abort"),
	ACK("ack"), CHANGESTATUS("changestatus"), FORGET("forget"), YESVOTE("yesvote"),
	NOVOTE("novote"), END("end"),
	SENDPREPARE("sendprepare"), GETVOTES("getVotes"), SENDCOMMIT("sendcommit"), SENDABORT("sendabort"), GETACK("sendack");

	private final String name;

	private ProcessNames(String s) {
		name = s;
	}

	public boolean equalsName(String s) {
		return name.equals(s);
	}

	public String toString() {
		return name;
	}
}
