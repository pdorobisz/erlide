/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.erlide.ui.internal.search;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkingSet;
import org.erlide.core.erlang.IErlElement;
import org.erlide.ui.editors.erl.ErlangEditor;

/**
 * Finds references of the selected element in working sets. The action is
 * applicable to selections representing a Java element.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
public class FindImplementorsInWorkingSetAction extends FindImplementorsAction {

	@SuppressWarnings("unused")
	private final IWorkingSet[] fWorkingSets;

	/**
	 * Creates a new <code>FindReferencesInWorkingSetAction</code>. The action
	 * requires that the selection provided by the site's selection provider is
	 * of type <code>org.eclipse.jface.viewers.IStructuredSelection</code>. The
	 * user will be prompted to select the working sets.
	 * 
	 * @param site
	 *            the site providing context information for this action
	 */
	public FindImplementorsInWorkingSetAction(final IWorkbenchSite site) {
		this(site, null);
	}

	/**
	 * Creates a new <code>FindReferencesInWorkingSetAction</code>. The action
	 * requires that the selection provided by the site's selection provider is
	 * of type <code>org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param site
	 *            the site providing context information for this action
	 * @param workingSets
	 *            the working sets to be used in the search
	 */
	public FindImplementorsInWorkingSetAction(final IWorkbenchSite site,
			final IWorkingSet[] workingSets) {
		super(site);
		fWorkingSets = workingSets;
	}

	/**
	 * Note: This constructor is for internal use only. Clients should not call
	 * this constructor.
	 * 
	 * @param editor
	 *            the Java editor
	 */
	public FindImplementorsInWorkingSetAction(final ErlangEditor editor) {
		this(editor, null);
	}

	/**
	 * Note: This constructor is for internal use only. Clients should not call
	 * this constructor.
	 * 
	 * @param editor
	 *            the Java editor
	 * @param workingSets
	 *            the working sets to be used in the search
	 */
	public FindImplementorsInWorkingSetAction(final ErlangEditor editor,
			final IWorkingSet[] workingSets) {
		super(editor);
		fWorkingSets = workingSets;
	}

	@Override
	void init() {
		setText("Working set");
		setToolTipText("Find declarations in working set");
		// FIXME setImageDescriptor(JavaPluginImages.DESC_OBJS_SEARCH_REF);
		// FIXME PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
		// IJavaHelpContextIds.FIND_REFERENCES_IN_WORKING_SET_ACTION);
	}

	@Override
	protected List<IResource> getScope() {
		// return SearchUtil.getWorkingSetsScope(fWorkingSets);
		return null;
	}

	@Override
	protected String getScopeDescription() {
		return SearchUtil.getWorkingSetsScopeDescription(fWorkingSets);
	}

	@Override
	public void run(final IErlElement element) {
		try {
			super.performNewSearch(element, getWorkingSetsScope(fWorkingSets));
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void run(final ITextSelection selection) {
		try {
			performNewSearch(selection, getWorkingSetsScope(fWorkingSets));
		} catch (InterruptedException e) {
		}
	}
}
