/*******************************************************************************
 * Copyright (c) 2008 Vlad Dumitrescu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vlad Dumitrescu
 *******************************************************************************/
package org.erlide.runtime.backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.erlide.core.ErlangPlugin;
import org.erlide.runtime.ErlLogger;
import org.erlide.runtime.PreferencesUtils;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public final class RuntimeInfoManager implements IPreferenceChangeListener {

	private RuntimeInfo erlideRuntime;

	private RuntimeInfoManager() {
		getRootPreferenceNode().addPreferenceChangeListener(this);
		load();
	}

	@SuppressWarnings("synthetic-access")
	private static class LazyRuntimeInfoManagerHolder {
		public static final RuntimeInfoManager instance = new RuntimeInfoManager();
	}

	public static synchronized RuntimeInfoManager getDefault() {
		return LazyRuntimeInfoManagerHolder.instance;
	}

	private final Map<String, RuntimeInfo> fRuntimes = new HashMap<String, RuntimeInfo>();
	private String defaultRuntimeName = "";

	private List<RuntimeInfoListener> fListeners = new ArrayList<RuntimeInfoListener>();

	public Collection<RuntimeInfo> getRuntimes() {
		return new ArrayList<RuntimeInfo>(fRuntimes.values());
	}

	public synchronized void store() {
		IEclipsePreferences root = getRootPreferenceNode();
		try {
			root.removePreferenceChangeListener(this);
			root.removeNode();
			root = getRootPreferenceNode();

			for (RuntimeInfo rt : fRuntimes.values()) {
				rt.store(root);
			}
			if (defaultRuntimeName != null) {
				root.put("default", defaultRuntimeName);
			}
			if (erlideRuntime != null) {
				root.put("erlide", erlideRuntime.getName());
			}
			try {
				root.flush();
			} catch (BackingStoreException e) {
				ErlLogger.warn(e);
			}
			root.addPreferenceChangeListener(this);
		} catch (BackingStoreException e) {
			ErlLogger.warn(e);
		}
	}

	public synchronized void load() {
		fRuntimes.clear();

		loadDefaultPrefs();

		// TODO remove this later
		final String OLD_NAME = "erts";
		IEclipsePreferences old = new InstanceScope()
				.getNode("org.erlide.basic/");
		String oldVal = old.get("otp_home", null);
		if (oldVal != null) {
			ErlLogger.debug("** converting old workspace Erlang settings");

			RuntimeInfo rt = new RuntimeInfo();
			rt.setOtpHome(oldVal);
			rt.setName(OLD_NAME);
			rt.setNodeName(rt.getName());
			addRuntime(rt);
			setDefaultRuntime(OLD_NAME);
			old.remove("otp_home");
			try {
				old.flush();
			} catch (Exception e) {
				ErlLogger.warn(e);
			}
			store();
		}
		//

		// TODO remove this later
		IEclipsePreferences root = new InstanceScope()
				.getNode("org.erlide.launching/runtimes");
		loadPrefs(root);
		try {
			Preferences p = root.parent();
			root.removeNode();
			p.flush();
		} catch (Exception e) {
			ErlLogger.warn(e);
		}
		//

		root = getRootPreferenceNode();
		loadPrefs(root);
	}

	private void loadDefaultPrefs() {
		IPreferencesService ps = Platform.getPreferencesService();
		final String DEFAULT_ID = "org.erlide";

		String defName = ps.getString(DEFAULT_ID, "default_name", null, null);
		final RuntimeInfo runtime = getRuntime(defName);
		if (defName != null && runtime == null) {
			RuntimeInfo rt = new RuntimeInfo();
			rt.setName(defName);
			String path = ps.getString(DEFAULT_ID, "default_"
					+ RuntimeInfo.CODE_PATH, "", null);
			rt.setCodePath(PreferencesUtils.unpackList(path));
			rt.setOtpHome(ps.getString(DEFAULT_ID, "default_"
					+ RuntimeInfo.HOME_DIR, "", null));
			rt.setArgs(ps.getString(DEFAULT_ID, "default_" + RuntimeInfo.ARGS,
					"", null));
			String wd = ps.getString(DEFAULT_ID, "default_"
					+ RuntimeInfo.WORKING_DIR, "", null);
			if (wd.length() != 0) {
				rt.setWorkingDir(wd);
			}
			rt.setManaged(ps.getBoolean(DEFAULT_ID, "default_"
					+ RuntimeInfo.MANAGED, true, null));
			addRuntime(rt);
		}
		defaultRuntimeName = defName;
	}

	private void loadPrefs(IEclipsePreferences root) {
		String defrt = root.get("default", null);
		if (defrt != null) {
			defaultRuntimeName = defrt;
		}

		String[] children;
		try {
			children = root.childrenNames();
			for (String name : children) {
				RuntimeInfo rt = new RuntimeInfo();
				rt.load(root.node(name));
				fRuntimes.put(name, rt);
			}
		} catch (BackingStoreException e) {
			ErlLogger.warn(e);
		}

		if (getDefaultRuntime() == null) {
			if (defaultRuntimeName == null && fRuntimes.size() > 0) {
				defaultRuntimeName = fRuntimes.values().iterator().next()
						.getName();
			}
		}
		final RuntimeInfo rt = getRuntime(root.get("erlide", null));
		setErlideRuntime(rt == null ? getDefaultRuntime() : rt);
	}

	protected IEclipsePreferences getRootPreferenceNode() {
		return new InstanceScope()
				.getNode(ErlangPlugin.PLUGIN_ID + "/runtimes");
	}

	public void setRuntimes(Collection<RuntimeInfo> elements) {
		fRuntimes.clear();
		for (RuntimeInfo rt : elements) {
			fRuntimes.put(rt.getName(), rt);
		}
		notifyListeners();
	}

	public void addRuntime(RuntimeInfo rt) {
		if (!fRuntimes.containsKey(rt.getName())) {
			fRuntimes.put(rt.getName(), rt);
		}
		notifyListeners();
	}

	public Collection<String> getRuntimeNames() {
		return fRuntimes.keySet();
	}

	public boolean isDuplicateName(String name) {
		for (RuntimeInfo vm : fRuntimes.values()) {
			if (vm.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public RuntimeInfo getRuntime(String name) {
		final RuntimeInfo rt = fRuntimes.get(name);
		return rt;
	}

	public void removeRuntime(String name) {
		fRuntimes.remove(name);
		notifyListeners();
	}

	public String getDefaultRuntimeName() {
		return this.defaultRuntimeName;
	}

	public void setDefaultRuntime(String name) {
		this.defaultRuntimeName = name;
		notifyListeners();
	}

	public void setErlideRuntime(RuntimeInfo runtime) {
		if (runtime != null) {
			runtime.setNodeName("erlide");
		}
		RuntimeInfo old = this.erlideRuntime;
		if (old == null || !old.equals(runtime)) {
			this.erlideRuntime = runtime;
			notifyListeners();
			// this creates infinite recursion!
			// BackendManager.getDefault().getIdeBackend().stop();
		}
	}

	public RuntimeInfo getErlideRuntime() {
		if (erlideRuntime == null) {
			RuntimeInfo ri = null;
			Iterator<RuntimeInfo> iterator = getRuntimes().iterator();
			if (iterator.hasNext()) {
				ri = iterator.next();
			}
			if (ri != null) {
				setErlideRuntime(ri);
			} else {
				ErlLogger.error("There is no erlideRuntime defined!");
			}
		}
		return erlideRuntime;
	}

	public RuntimeInfo getDefaultRuntime() {
		return getRuntime(getDefaultRuntimeName());
	}

	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getNode().absolutePath().contains("org.erlide")) {
			load();
		}
	}

	public void addListener(RuntimeInfoListener listener) {
		if (!fListeners.contains(listener)) {
			fListeners.add(listener);
		}
	}

	public void removeListener(RuntimeInfoListener listener) {
		fListeners.remove(listener);
	}

	private void notifyListeners() {
		for (RuntimeInfoListener listener : fListeners) {
			listener.infoChanged();
		}
	}

	/**
	 * Locate runtimes with this version or newer. If exact matches exists, they
	 * are first in the result list. A null or empty version returns all
	 * runtimes.
	 */
	public List<RuntimeInfo> locateVersion(String version) {
		RuntimeVersion vsn = new RuntimeVersion(version);
		return locateVersion(vsn);
	}

	public List<RuntimeInfo> locateVersion(RuntimeVersion vsn) {
		List<RuntimeInfo> result = new ArrayList<RuntimeInfo>();
		for (RuntimeInfo info : getRuntimes()) {
			RuntimeVersion v = info.getVersion();
			if (v.equals(vsn)) {
				result.add(info);
			}
		}
		// add even newer versions, but at the end
		for (RuntimeInfo info : getRuntimes()) {
			RuntimeVersion v = info.getVersion();
			if (!result.contains(info) && v.compareTo(vsn) > 0) {
				result.add(info);
			}
		}
		return result;
	}

	public RuntimeInfo getRuntime(RuntimeVersion runtimeVersion,
			String runtimeName) {
		List<RuntimeInfo> vsns = locateVersion(runtimeVersion);
		if (vsns.size() == 0) {
			return null;
		} else if (vsns.size() == 1) {
			return vsns.get(0);
		} else {
			for (RuntimeInfo ri : vsns) {
				if (ri.getName().equals(runtimeName)) {
					return ri;
				}
			}
			return vsns.get(0);
		}
	}
}
