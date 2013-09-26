package org.coreasm.eclipse.debug.ui.views;


import java.util.ArrayList;
import java.util.Set;

import org.coreasm.eclipse.CoreASMPlugin;
import org.coreasm.eclipse.debug.core.model.ASMDebugTarget;
import org.coreasm.eclipse.debug.core.model.ASMStackFrame;
import org.coreasm.eclipse.debug.util.ASMDebugUtils;
import org.coreasm.eclipse.engine.debugger.EngineDebugger;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.util.Tools;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * Implementation of the ASM Update View
 * @author Michael Stegmaier
 *
 */
public class ASMUpdateView extends ViewPart implements IDebugContextListener {
	private TableViewer viewer;
	private Action doubleClickAction;
	private Action filterAllAgentsAction;
	private Element filterAgent = null;
	private Set<? extends Element> agents;
	private Set<ASMUpdate> updates;
	private Object[] elements;
	private final Image IMAGE_UPDATE = CoreASMPlugin.getImageDescriptor("icons/CoreASM-Logo.png").createImage();
	private final Image IMAGE_BREAKPOINT = CoreASMPlugin.getImageDescriptor("icons/stepping_mode.png").createImage();

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
		public Image getImage(Object obj) {
			if (obj instanceof ASMUpdate) {
				if (((ASMUpdate) obj).isOnBreakpoint()) {
					return IMAGE_BREAKPOINT;
				}
				return IMAGE_UPDATE;
			}
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR);
		}
	}
	class NameSorter extends ViewerSorter {
	}
	
	public ASMUpdateView() {
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
				hookLocalPullDown();
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
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).addDebugContextListener(this);
		makeActions();
		hookDoubleClickAction();
		hookLocalPullDown();
	}

	private void hookLocalPullDown() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();
		manager.add(filterAllAgentsAction);
		manager.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				manager.removeAll();
				manager.add(filterAllAgentsAction);
				filterAllAgentsAction.setChecked(true);
				if (EngineDebugger.getRunningInstance() != null) {
					manager.add(new Separator());
					for (final Element agent : agents) {
						Action action = new Action() {
							public void run() {
								filterAgent = agent;
								ArrayList<ASMUpdate> filteredUpdates = new ArrayList<ASMUpdate>();
								for (ASMUpdate update : updates) {
									if (update.getAgents().contains(filterAgent))
										filteredUpdates.add(update);
								}
								elements = filteredUpdates.toArray();
								refresh();
							}
						};
						action.setText("Only show updates from agent: " + agent.denotation());
						action.setChecked(filterAgent != null && agent.equals(filterAgent));
						if (action.isChecked())
							filterAllAgentsAction.setChecked(false);
						manager.add(action);
					}
				}
			}
		});
	}

	private void makeActions() {
		filterAllAgentsAction = new Action() {
			public void run() {
				filterAgent = null;
				if (EngineDebugger.getRunningInstance() != null)
					elements = updates.toArray();
				refresh();
			}
		};
		filterAllAgentsAction.setText("Show updates from all agents");
		filterAllAgentsAction.setChecked(true);
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				ASMDebugUtils.openEditor((ASMUpdateViewElement) obj);
			}
		};
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
			Object element = ((IStructuredSelection)context).getFirstElement();
			if (element instanceof ASMStackFrame) {
				ASMStackFrame frame = (ASMStackFrame)element;
				updates = frame.getUpdates();
				if (!updates.isEmpty()) {
					agents = frame.getAgents();
					if (!agents.contains(filterAgent))
						filterAgent = null;
					if (filterAgent != null) {
						ArrayList<ASMUpdate> filteredUpdates = new ArrayList<ASMUpdate>();
						for (ASMUpdate update : updates) {
							if (update.getAgents().contains(filterAgent))
								filteredUpdates.add(update);
						}
						elements = filteredUpdates.toArray();
					}
					else
						elements = updates.toArray();
				}
				else
					elements = updates.toArray();
				refresh();
			}
			else if (element instanceof ASMDebugTarget) {
				ASMDebugTarget debugTarget = (ASMDebugTarget)element;
				if (debugTarget.isTerminated()) {
					if (debugTarget.isUpdateFailed()) {
						ArrayList<ASMUpdateViewElement> errors = new ArrayList<ASMUpdateViewElement>();
						String[] errorLines = debugTarget.getStepFailedMsg().replaceAll("\t", " ").split(Tools.getEOL());
						String reason = errorLines[0];
						
						for (int i = 0; i < errorLines.length; i++) {
							if (errorLines[i].endsWith(":")) {
								errors.add(new ASMUpdateViewElement(reason + errorLines[i].trim() + errorLines[i + 1].trim()));
								i++;
							}
						}
						
						elements = errors.toArray();
						
						refresh();
					}
					else {
						CoreASMError lastError = debugTarget.getLastError();
						if (lastError != null) {
							elements = new ASMUpdateViewElement[] { new ASMUpdateViewElement(lastError.showError().replaceAll("\r|\n", " ")) };
							refresh();
						}
					}
				}
				else {
					elements = new Object[0];
					refresh();
				}
			}
		}
	}
}