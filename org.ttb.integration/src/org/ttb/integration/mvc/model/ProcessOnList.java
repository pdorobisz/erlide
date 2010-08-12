package org.ttb.integration.mvc.model;

import java.util.HashSet;
import java.util.Set;

import org.ttb.integration.ProcessFlag;

import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangTuple;

/**
 * Process on list of processes.
 * 
 * @author Piotr Dorobisz
 * 
 */
public class ProcessOnList {

    // fields in tuple describing process
    private static final int PID = 0;
    private static final int NAME = 1;
    private static final int INITIAL_CALL = 2;

    private boolean selected;
    private final String name;
    private final OtpErlangPid pid;
    private final String initialCall;
    private final Set<ProcessFlag> flags = new HashSet<ProcessFlag>();

    /**
     * Creates process from tuple. List of tuples describing all processes is
     * returned as a result of <code>erlide_proclist:process_list/0</code> call.
     * 
     * @param tuple
     *            tuple describing process
     */
    public ProcessOnList(OtpErlangTuple tuple) {
        this.name = tuple.elementAt(NAME).toString();
        this.pid = (OtpErlangPid) tuple.elementAt(PID);
        this.initialCall = tuple.elementAt(INITIAL_CALL).toString();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public OtpErlangPid getPid() {
        return pid;
    }

    public String getInitialCall() {
        return initialCall;
    }

    public void setFlag(ProcessFlag flag) {
        flags.add(flag);
    }

    public void unSetFlag(ProcessFlag flag) {
        flags.remove(flag);
    }

    public boolean hasFlag(ProcessFlag flag) {
        return flags.contains(flag);
    }

    public Set<ProcessFlag> getFlags() {
        return flags;
    }
}