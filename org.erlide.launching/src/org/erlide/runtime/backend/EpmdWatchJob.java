/*******************************************************************************
 * Copyright (c) 2005 Vlad Dumitrescu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vlad Dumitrescu
 *******************************************************************************/
package org.erlide.runtime.backend;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.ericsson.otp.erlang.OtpEpmd;

/**
 * Each 2 seconds, query epmd to see if there are any new nodes that have been
 * registered.
 * 
 */
public class EpmdWatchJob extends Job {

	// TODO maybe better to register node names we're interested in, to be
	// notified when they go up/down?

	public EpmdWatchJob() {
		super("Checking EPMD for new backends");

		try {
			addHost(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			addHost("localhost");
		}

		setSystem(true);
		setPriority(SHORT);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		checkEpmd();

		this.schedule(1000);
		return Status.OK_STATUS;
	}

	private List<String> hosts = new ArrayList<String>();
	private Map<String, List<String>> nodeMap = new HashMap<String, List<String>>();
	private List<IEpmdListener> listeners = new ArrayList<IEpmdListener>();

	synchronized public void addHost(String host) {
		if (hosts.contains(host)) {
			return;
		}
		hosts.add(host);
		nodeMap.put(host, new ArrayList<String>());
	}

	synchronized public void removeHost(String host) {
		hosts.remove(host);
		nodeMap.remove(host);
	}

	synchronized private void checkEpmd() {
		for (Entry<String, List<String>> entry : nodeMap.entrySet()) {
			try {
				String host = entry.getKey();
				List<String> nodes = entry.getValue();

				final String[] names = OtpEpmd.lookupNames(InetAddress
						.getByName(host));
				final List<String> labels = Arrays.asList(names);
				clean(labels);

				List<String> started = getDiff(labels, nodes);
				List<String> stopped = getDiff(nodes, labels);

				if (started.size() > 0 || stopped.size() > 0) {
					for (IEpmdListener listener : listeners) {
						listener.updateBackendStatus(host, started, stopped);
					}
				}

				entry.setValue(labels);

			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void addEpmdListener(IEpmdListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeEpmdListener(IEpmdListener listener) {
		listeners.remove(listener);
	}

	public static void clean(List<String> list) {
		for (int i = 0; i < list.size(); i++) {
			String label = list.get(i);
			// label is "name X at port N"
			final String[] parts = label.split(" ");
			if (parts.length == 5) {
				String alabel = parts[1];
				if (alabel.length() == 0) {
					alabel = "??" + label;
				}
				list.set(i, alabel);
			}
		}
	}

	private List<String> getDiff(List<String> list1, List<String> list2) {
		List<String> result = new ArrayList<String>(list1);
		result.removeAll(list2);
		return result;
	}

	public Map<String, List<String>> getData() {
		return nodeMap;
	}

}
