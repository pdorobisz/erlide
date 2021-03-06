/*******************************************************************************
 * Copyright (c) 2009 Vlad Dumitrescu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available
 * at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vlad Dumitrescu
 *******************************************************************************/
package org.erlide.runtime.backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.erlide.core.erlang.util.ErlideUtil;
import org.erlide.core.util.Tuple;
import org.erlide.jinterface.util.ErlLogger;
import org.osgi.framework.Bundle;

import com.google.common.collect.Lists;

public class CodeBundle {

	public static enum CodeContext {
		ANY, COMMON, BUILDER, IDE, DEBUGGER
	}

	private final Bundle bundle;
	private final Collection<Tuple<String, CodeContext>> paths;
	private final Tuple<String, String> init;

	public CodeBundle(Bundle b, Collection<Tuple<String, CodeContext>> paths,
			Tuple<String, String> init) {
		this.bundle = b;
		this.paths = Lists.newArrayList(paths);
		this.init = init;
	}

	public Bundle getBundle() {
		return bundle;
	}

	public Collection<String> getEbinDirs() {
		List<String> result = Lists.newArrayList();
		for (Tuple<String, CodeContext> path : paths) {
			String myPath = ErlideUtil.getPath(path.o1, bundle);
			if (myPath != null) {
				result.add(myPath);
			} else {
				ErlLogger.warn("Can't access path %s, "
						+ "erlide plugins may be incorrectly built", path.o1);
			}
		}
		return result;
	}

	public Collection<String> getPluginCode() {
		List<String> result = new ArrayList<String>();
		for (Tuple<String, CodeContext> dir : paths) {
			final Path path = new Path(dir.o1);
			if (path.getFileExtension() != null
					&& "beam".compareTo(path.getFileExtension()) == 0) {
				final String m = path.removeFileExtension().lastSegment();
				result.add(m);
			}
		}
		return result;
	}

	public Collection<Tuple<String, CodeContext>> getPaths() {
		return Collections.unmodifiableCollection(paths);
	}

	public Tuple<String, String> getInit() {
		if (init == null) {
			return null;
		}
		return new Tuple<String, String>(init);
	}

}
