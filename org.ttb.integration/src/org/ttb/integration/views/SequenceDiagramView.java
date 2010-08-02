package org.ttb.integration.views;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.ttb.integration.TtbBackend;
import org.ttb.integration.mvc.controller.CollectedTracesContentProvider;
import org.ttb.integration.mvc.model.CollectedDataList;
import org.ttb.integration.mvc.model.ITraceNodeObserver;
import org.ttb.integration.mvc.model.TracePattern;
import org.ttb.integration.mvc.view.CollectedTracesLabelProvider;

/**
 * Sequence diagram which shows tracing results.
 * 
 * @author Piotr Dorobisz
 * 
 */
public class SequenceDiagramView extends ViewPart implements ITraceNodeObserver {

    private final String CONSOLE_NAME = "ttb console";
    private TreeViewer treeViewer;

    public SequenceDiagramView() {
        TtbBackend.getInstance().addListener(this);
    }

    @Override
    public void dispose() {
        TtbBackend.getInstance().removeListener(this);
        super.dispose();
    }

    @Override
    public void createPartControl(Composite parent) {
        treeViewer = new TreeViewer(parent);
        treeViewer.setContentProvider(new CollectedTracesContentProvider());
        treeViewer.setLabelProvider(new CollectedTracesLabelProvider());
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

    @Override
    public void addPattern(TracePattern tracePattern) {
        // TODO
    }

    @Override
    public void removePattern(TracePattern tracePattern) {
        // TODO
    }

    @Override
    public void updatePattern(TracePattern tracePattern) {
        // TODO Auto-generated method stub
    }

    @Override
    public void startTracing() {
        // TODO Auto-generated method stub
    }

    @Override
    public void stopTracing() {
        CollectedDataList collectedData = TtbBackend.getInstance().getCollectedData();
        treeViewer.setInput(collectedData);
    }
}