/*******************************************************************************
 * Copyright (c) 2010 György Orosz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     György Orosz - initial API and implementation
 ******************************************************************************/
package org.erlide.wrangler.refactoring.duplicatedcode.ui.elements;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.erlide.wrangler.refactoring.util.WranglerUtils;

/**
 * Treeview element, which represents a single code part string
 * 
 * @author Gyorgy Orosz
 * 
 */
public class DuplicatedCodeInstanceElement extends AbstractResultTreeObject {

	private int startOffset;
	private int endOffset;
	private String codePartString;
	private final IFile containingFile;
	private final int startLine;

	// private ITextEditor containingEditor;

	/**
	 * Constructor
	 * 
	 * @param containingFile
	 *            file which contains the code part string
	 * @param startLine
	 *            starting line of the selection
	 * @param startColumn
	 *            starting column of the selection
	 * @param endLine
	 *            ending line of the selection
	 * @param endColumn
	 *            ending column of the selection
	 */
	public DuplicatedCodeInstanceElement(IFile containingFile, int startLine,
			int startColumn, int endLine, int endColumn) {

		this.containingFile = containingFile;
		this.startLine = startLine;
		IDocument doc = WranglerUtils.getDocument(containingFile);
		try {
			this.startOffset = WranglerUtils.calculateOffsetFromPosition(
					startLine, startColumn, doc);
			this.endOffset = WranglerUtils.calculateOffsetFromPosition(endLine,
					endColumn, doc);
			this.codePartString = WranglerUtils.getTextSegment(startOffset,
					endOffset, doc);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Return the starting offset of the represented selection
	 * 
	 * @return starting offset of the selection
	 */
	public int getStartOffset() {
		return startOffset;
	}

	/**
	 * Return the ending offset of the represented selection
	 * 
	 * @return ending offset of the selection
	 */
	public int getEndOffset() {
		return endOffset;
	}

	/*
	 * public ITextEditor getEditor() { return containingEditor; }
	 */

	/**
	 * Returns the file which contains the selection
	 * 
	 * @return file which contains the selection
	 */
	public IFile getContainingFile() {
		return containingFile;
	}

	/**
	 * Returns the string which is represented
	 * 
	 * @return code part string
	 */
	public String getCodePartString() {
		return simplifyCodePartString(codePartString);
	}

	@Override
	public String getName() {
		return startLine + ": \"" + getCodePartString() + "\"";
	}

	protected String simplifyCodePartString(String input) {
		return input;// input.replace("\n", " ").replace("\t", "").replace("\r",
		// "");

	}
}
