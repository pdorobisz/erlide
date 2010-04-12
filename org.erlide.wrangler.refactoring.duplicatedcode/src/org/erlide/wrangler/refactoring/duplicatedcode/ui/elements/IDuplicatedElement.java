package org.erlide.wrangler.refactoring.duplicatedcode.ui.elements;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;

public interface IDuplicatedElement extends IAdaptable {

	public abstract int getStartOffset();

	public abstract int getEndOffset();

	public abstract IFile getContainingFile();

}
