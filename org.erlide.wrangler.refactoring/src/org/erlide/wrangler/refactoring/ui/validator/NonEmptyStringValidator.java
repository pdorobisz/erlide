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
package org.erlide.wrangler.refactoring.ui.validator;

/**
 * Validate a string which is not empty
 * 
 * @author Gyorgy Orosz
 * @version %I%, %G%
 */
public class NonEmptyStringValidator implements IValidator {

	public boolean isValid(final String text) {
		return !text.equals("");
	}

}
