package org.erlide.ui.internal.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.erlide.core.erlang.util.ErlideUtil;
import org.erlide.core.erlang.util.ResourceUtil;
import org.erlide.core.search.ErlangSearchPattern;
import org.erlide.ui.util.ErlModelUtils;

public class ErlangSearchResult extends AbstractTextSearchResult implements
		IEditorMatchAdapter, IFileMatchAdapter {

	private List<ErlangSearchElement> result;
	private final ErlSearchQuery query;

	public ErlangSearchResult(final ErlSearchQuery query) {
		this.query = query;
		result = new ArrayList<ErlangSearchElement>();
	}

	@Override
	public void removeAll() {
		result = new ArrayList<ErlangSearchElement>();
		super.removeAll();
	}

	@Override
	public void removeMatch(final Match match) {
		final Object element = match.getElement();
		if (getMatchCount(element) == 1) {
			removeElement(element);
		}
		super.removeMatch(match);
	}

	@Override
	public void removeMatches(final Match[] matches) {
		for (final Match match : matches) {
			final Object element = match.getElement();
			int matchCount = getMatchCount(element);
			if (matchCount == 1) {
				removeElement(element);
			} else if (matchCount == countMatches(element, matches)) {
				removeElement(element);
			}
		}
		super.removeMatches(matches);
	}

	private synchronized void removeElement(final Object element) {
		result.remove(element);
	}

	private int countMatches(final Object element, final Match[] matches) {
		int result = 0;
		for (Match match : matches) {
			if (match.getElement().equals(element)) {
				result++;
			}
		}
		return result;
	}

	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return this;
	}

	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		return this;
	}

	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel() {
		final int matchCount = getMatchCount();
		final String occurences = getOccurencesLabel(matchCount);
		final String scope = query.getScopeDecsription();
		return query.getLabel() + " - " + matchCount + " " + occurences
				+ " in " + scope + ".";
	}

	private String getOccurencesLabel(final int matchCount) {
		int limitTo = query.getPattern().getLimitTo();
		if (limitTo == ErlangSearchPattern.ALL_OCCURRENCES) {
			return matchCount == 1 ? "occurence" : "occurences";
		} else if (limitTo == ErlangSearchPattern.REFERENCES) {
			return matchCount == 1 ? "reference" : "references";
		} else {
			return matchCount == 1 ? "definition" : "definitions";
		}
	}

	public ISearchQuery getQuery() {
		return query;
	}

	public String getTooltip() {
		// TODO Auto-generated method stub
		return null;
	}

	public synchronized List<ErlangSearchElement> getResult() {
		return new ArrayList<ErlangSearchElement>(result);
	}

	public synchronized void setResult(final List<ErlangSearchElement> result) {
		this.result = result;
	}

	private static final Match[] NO_MATCHES = new Match[0];

	public Match[] computeContainedMatches(
			final AbstractTextSearchResult aResult, final IEditorPart editor) {
		// TODO: copied from JavaSearchResult:
		final IEditorInput editorInput = editor.getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			final IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
			return computeContainedMatches(aResult, fileEditorInput.getFile());
		}
		return NO_MATCHES;
	}

	public boolean isShownInEditor(final Match match, final IEditorPart editor) {
		final ErlangSearchElement ese = (ErlangSearchElement) match
				.getElement();
		final IFile file = ResourceUtil
				.getFileFromLocation(ese.getModuleName());
		if (editor instanceof ITextEditor) {
			final ITextEditor textEditor = (ITextEditor) editor;
			return ErlModelUtils.getModule(textEditor).getResource().equals(
					file);
		}
		return false;
	}

	public Match[] computeContainedMatches(
			final AbstractTextSearchResult aResult, final IFile file) {
		final ErlangSearchResult esr = (ErlangSearchResult) aResult;
		final List<Match> l = new ArrayList<Match>();
		final List<ErlangSearchElement> eses = esr.getResult();
		final String name = file.getName();
		if (eses == null || !ErlideUtil.hasModuleExtension(name)) {
			return NO_MATCHES;
		}
		for (final ErlangSearchElement ese : eses) {
			final String moduleName = new Path(ese.getModuleName())
					.lastSegment();
			if (moduleName.equals(name)) {
				final Match[] matches = getMatches(ese);
				for (final Match match : matches) {
					l.add(match);
				}
			}
		}
		return l.toArray(new Match[l.size()]);
	}

	public IFile getFile(final Object element) {
		if (element instanceof ErlangSearchElement) {
			final ErlangSearchElement ese = (ErlangSearchElement) element;
			return ResourceUtil.getFileFromLocation(ese.getModuleName());
		}
		return null;
	}
}
