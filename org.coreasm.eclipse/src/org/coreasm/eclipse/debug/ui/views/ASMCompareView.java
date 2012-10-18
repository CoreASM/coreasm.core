package org.coreasm.eclipse.debug.ui.views;


import java.util.ArrayList;

import org.coreasm.eclipse.CoreASMPlugin;
import org.coreasm.eclipse.debug.core.model.ASMStackFrame;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;

/**
 * Implementation of the ASM Compare View
 * @author Michael Stegmaier
 *
 */
public class ASMCompareView extends ViewPart implements IDebugContextListener {
	private TableViewer viewer;
	private Action showDifferencesOnlyAction;
	private Action doubleClickAction;
	private ASMCompareViewElement[] compareViewElements;
	private Object[] elements;
	private ArrayList<TableViewerColumn> columns = new ArrayList<TableViewerColumn>();
	private boolean differencesOnly;
	private final Image IMAGE = CoreASMPlugin.getImageDescriptor("icons/CoreASM-Logo.png").createImage();

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
			viewer = null;
		}
		public Object[] getElements(Object parent) {
			if (elements == null)
				return new Object[0];
			return elements;
		}
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		@Override
		public Image getImage(Object obj) {
			return IMAGE;
		}
	}
	class NameSorter extends ViewerSorter {
	}
	
	public ASMCompareView() {
	}
	
	@Override
	public void dispose() {
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).removeDebugContextListener(this);
		super.dispose();
	}
	
	public void refresh() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (viewer != null) {
					viewer.setInput(elements);
					viewer.refresh();
				}
			}
		});
	}

	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
//		viewer.setSorter(new NameSorter());
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).addDebugContextListener(this);
		createNameColumn();
		makeActions();
		hookDoubleClickAction();
		hookLocalPullDown();
	}
	
	private void createNameColumn() {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(100);
		column.getColumn().setText("Name");
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ASMCompareViewElement)element).getName();
			}
			
			@Override
			public Color getBackground(Object element) {
				if (((ASMCompareViewElement)element).hasDifference())
					return Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
				return super.getBackground(element);
			}
		});
	}
	
	private TableViewerColumn createColumn(int step, final int index) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText("Step " + step);
		column.getColumn().setWidth(100);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ASMCompareViewElement)element).getValues()[index];
			}

			@Override
			public Color getBackground(Object element) {
				if (((ASMCompareViewElement)element).hasDifference())
					return Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
				return super.getBackground(element);
			}
		});
		return column;
	}
	
	private void clearColumns() {
		for (TableViewerColumn column : columns)
			column.getColumn().dispose();
		columns.clear();
	}

	private void hookLocalPullDown() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();
		manager.add(showDifferencesOnlyAction);
	}

	private void makeActions() {
		showDifferencesOnlyAction = new Action() {
			public void run() {
				differencesOnly = !differencesOnly;
				showDifferencesOnlyAction.setChecked(differencesOnly);
				if (differencesOnly) {
					ArrayList<ASMCompareViewElement> elementsWithDifferences = new ArrayList<ASMCompareViewElement>();
					for (ASMCompareViewElement element : compareViewElements) {
						if (element.hasDifference())
							elementsWithDifferences.add(element);
					}
					elements = elementsWithDifferences.toArray();
				}
				else
					elements = compareViewElements;
				refresh();
			}
		};
		showDifferencesOnlyAction.setText("Show differences only");
		showDifferencesOnlyAction.setChecked(false);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		ISelection context = event.getContext();
		if (context instanceof IStructuredSelection) {
			clearColumns();
			IVariable[][] variables = new IVariable[((IStructuredSelection)context).size()][];
			int selectedSteps = 0;
			for (Object element : ((IStructuredSelection)context).toList()) {
				if (element instanceof ASMStackFrame) {
					ASMStackFrame frame = (ASMStackFrame)element;
					try {
						columns.add(createColumn(frame.getStep(), selectedSteps));
						variables[selectedSteps] = frame.getVariables();
					}
					catch (DebugException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					selectedSteps++;
				}
			}
			String prevVariableName = "";
			ArrayList<String> variableNames = new ArrayList<String>();
			if (variables.length > 0 && variables[0] != null) {
				for (int i = 0; i < selectedSteps; i++) {
					for (IVariable var : variables[i]) {
						try {
							String name = var.getName();
							if (!variableNames.contains(name))
								variableNames.add(variableNames.indexOf(prevVariableName) + 1, name);
							prevVariableName = name;
						} catch (DebugException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			compareViewElements = new ASMCompareViewElement[variableNames.size()];
			for (int i = 0; i < compareViewElements.length; i++) {
				String name = variableNames.get(i);
				try {
					String[] values = new String[selectedSteps];
					for (int j = 0; j < selectedSteps; j++) {
						for (IVariable var : variables[j]) {
							if (name.equals(var.getName())) {
								values[j] = var.getValue().getValueString();
								break;
							}
						}
					}
					compareViewElements[i] = new ASMCompareViewElement(name, values);
				} catch (DebugException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (differencesOnly) {
				ArrayList<ASMCompareViewElement> elementsWithDifferences = new ArrayList<ASMCompareViewElement>();
				for (ASMCompareViewElement element : compareViewElements) {
					if (element.hasDifference())
						elementsWithDifferences.add(element);
				}
				elements = elementsWithDifferences.toArray();
			}
			else
				elements = this.compareViewElements;
			refresh();
		}
	}
}