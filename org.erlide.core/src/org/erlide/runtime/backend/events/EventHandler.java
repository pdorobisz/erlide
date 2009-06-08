package org.erlide.runtime.backend.events;

import java.util.List;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

public abstract class EventHandler {

	public final void handleMsgs(final List<OtpErlangObject> msgs) {
		for (final OtpErlangObject msg : msgs) {
			handleMsg(msg);
		}
	}

	public void handleMsg(final OtpErlangObject msg) {
		if (msg == null) {
			return;
		}
		try {
			doHandleMsg(msg);
		} catch (final Exception e) {
			e.printStackTrace();
			// ignore unrecognized messages
		}
	}

	protected abstract void doHandleMsg(OtpErlangObject msg) throws Exception;

	public OtpErlangObject getStandardEvent(final OtpErlangObject msg,
			final String id) {
		try {
			final OtpErlangTuple t = (OtpErlangTuple) msg;
			OtpErlangObject el0 = t.elementAt(0);
			if (el0 instanceof OtpErlangAtom) {
				if (!((OtpErlangAtom) el0).atomValue().equals("event")) {
					return null;
				}
			}
			el0 = t.elementAt(1);
			if (el0 instanceof OtpErlangAtom) {
				final OtpErlangAtom a = (OtpErlangAtom) el0;
				final String event = a.atomValue();
				if (id.equals(event)) {
					return t.elementAt(2);
				}
			}
		} catch (final Exception e) {
		}
		return null;
	}
}
