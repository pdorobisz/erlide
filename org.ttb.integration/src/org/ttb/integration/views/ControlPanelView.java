package org.ttb.integration.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.erlide.core.erlang.ErlangCore;
import org.erlide.runtime.backend.BackendManager;
import org.erlide.runtime.backend.ErlideBackend;
import org.ttb.integration.TtbBackend;
import org.ttb.integration.mvc.controller.CellModifier;
import org.ttb.integration.mvc.controller.TracePatternContentProvider;
import org.ttb.integration.mvc.model.ITraceNodeObserver;
import org.ttb.integration.mvc.model.TracePattern;
import org.ttb.integration.mvc.view.Columns;
import org.ttb.integration.mvc.view.TracePatternLabelProvider;

/**
 * A control panel for tracing.
 * 
 * @author Piotr Dorobisz
 * 
 */
public class ControlPanelView extends ViewPart implements ITraceNodeObserver {

    private TableViewer tableViewer;
    private Button startButton;
    private Combo backendNameCombo;

    public ControlPanelView() {
        TtbBackend.getInstance().addListener(this);
    }

    @Override
    public void dispose() {
        TtbBackend.getInstance().removeListener(this);
        super.dispose();
    }

    @Override
    public void createPartControl(Composite parent) {
        addChildren(parent);
    }

    private void addChildren(Composite parent) {
        // Create a composite to hold the children
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_BOTH);
        parent.setLayoutData(gridData);
        //
        // // Set numColumns to 3 for the buttons
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 4;

        parent.setLayout(layout);
        // parent.setLayout(new RowLayout(SWT.VERTICAL));

        createPatternButtonsPanel(parent);
        createTable(parent);
        createStartStopPanel(parent);

    }

    private void createStartStopPanel(Composite parent) {
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 4;
        parent.setLayout(layout);

        Composite composite = parent;
        // Composite composite = new Composite(parent, 0);
        // RowLayout layout = new RowLayout(SWT.HORIZONTAL);
        // composite.setLayout(layout);

        backendNameCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        backendNameCombo.setItems(getBackendNames());

        Button refreshButton = new Button(composite, SWT.PUSH | SWT.CENTER);
        refreshButton.setText("Refresh");
        refreshButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                backendNameCombo.setItems(getBackendNames());
            }
        });

        // "Start/Stop" button
        startButton = new Button(composite, SWT.PUSH | SWT.CENTER);
        startButton.setText(TtbBackend.getInstance().isStarted() ? "Stop" : "Start");
        startButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (TtbBackend.getInstance().isStarted()) {
                    TtbBackend.getInstance().stop();
                } else {
                    TtbBackend.getInstance().start(ErlangCore.getBackendManager().getByName(backendNameCombo.getText()));
                }
            }
        });
    }

    private String[] getBackendNames() {
        Collection<ErlideBackend> backends = BackendManager.getDefault().getAllBackends();
        List<String> backendNames = new ArrayList<String>();
        for (ErlideBackend erlideBackend : backends) {
            backendNames.add(erlideBackend.getName());
        }
        backendNames.add("test abcdefgh");// TODO remove it
        Collections.sort(backendNames);
        return backendNames.toArray(new String[backendNames.size()]);
    }

    private void createPatternButtonsPanel(Composite parent) {
        Composite composite = parent;
        // Composite composite = new Composite(parent, 0);
        // RowLayout layout = new RowLayout(SWT.HORIZONTAL);
        // composite.setLayout(layout);

        // "Add" button
        Button button = new Button(composite, SWT.PUSH | SWT.CENTER);
        button.setText("Add");
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TtbBackend.getInstance().addTracePattern(new TracePattern());
            }
        });

        // "Remove" button
        button = new Button(composite, SWT.PUSH | SWT.CENTER);
        button.setText("Remove");
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TracePattern tracePattern = (TracePattern) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
                if (tracePattern != null) {
                    TtbBackend.getInstance().removeTracePattern(tracePattern);
                }
            }
        });
    }

    private void createTable(Composite parent) {
        int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalSpan = 3;

        tableViewer = new TableViewer(parent, style);
        tableViewer.setUseHashlookup(true);

        // table
        Table table = tableViewer.getTable();
        table.setLayoutData(gridData);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // columns
        String columnProperties[] = new String[Columns.values().length];
        for (Columns column : Columns.values()) {
            TableColumn tableColumn = new TableColumn(table, SWT.CENTER, column.ordinal());
            tableColumn.setResizable(true);
            tableColumn.setMoveable(true);
            tableColumn.setWidth(column.getWidth());
            tableColumn.setText(column.getName());
            columnProperties[column.ordinal()] = column.name();
        }
        tableViewer.setColumnProperties(columnProperties);

        // providers
        tableViewer.setLabelProvider(new TracePatternLabelProvider());
        tableViewer.setContentProvider(new TracePatternContentProvider());

        // editors
        CellEditor[] editors = new CellEditor[Columns.values().length];
        editors[Columns.ENABLED.ordinal()] = new CheckboxCellEditor(table);
        editors[Columns.MODULE_NAME.ordinal()] = new TextCellEditor(table);
        editors[Columns.FUNCTION_NAME.ordinal()] = new TextCellEditor(table);
        tableViewer.setCellEditors(editors);
        tableViewer.setCellModifier(new CellModifier());
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub
    }

    @Override
    public void addPattern(TracePattern tracePattern) {
        tableViewer.add(tracePattern);
    }

    @Override
    public void removePattern(TracePattern tracePattern) {
        tableViewer.remove(tracePattern);
    }

    @Override
    public void updatePattern(TracePattern tracePattern) {
        tableViewer.update(tracePattern, null);
    }

    @Override
    public void startTracing() {
        startButton.setText("Stop");
    }

    @Override
    public void stopTracing() {
        startButton.setText("Start");
    }
}