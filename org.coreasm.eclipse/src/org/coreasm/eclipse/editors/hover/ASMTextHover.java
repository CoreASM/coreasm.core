package org.coreasm.eclipse.editors.hover;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.coreasm.eclipse.debug.core.model.ASMStackFrame;
import org.coreasm.eclipse.debug.core.model.ASMStorage;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.Declaration;
import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.ASMIncludeWatcher;
import org.coreasm.eclipse.editors.quickfix.ASMQuickAssistProcessor;
import org.coreasm.eclipse.engine.debugger.EngineDebugger;
import org.coreasm.engine.EngineException;
import org.coreasm.engine.Specification.FunctionInfo;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.kernel.RuleOrFuncElementNode;
import org.coreasm.engine.plugins.chooserule.ChooseRuleNode;
import org.coreasm.engine.plugins.extendrule.ExtendRuleNode;
import org.coreasm.engine.plugins.forallrule.ForallRuleNode;
import org.coreasm.engine.plugins.letrule.LetRuleNode;
import org.coreasm.engine.plugins.list.ListCompNode;
import org.coreasm.engine.plugins.modularity.IncludeNode;
import org.coreasm.engine.plugins.predicatelogic.ExistsExpNode;
import org.coreasm.engine.plugins.predicatelogic.ForallExpNode;
import org.coreasm.engine.plugins.set.SetCompNode;
import org.coreasm.engine.plugins.turboasm.LocalRuleNode;
import org.coreasm.engine.plugins.turboasm.ReturnRuleNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension2;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * This annotation hover shows the description of the selected ASM annotation.
 * @author Michael Stegmaier
 */
public class ASMTextHover
implements ITextHover, ITextHoverExtension, ITextHoverExtension2, IDebugContextListener
{
	private static class HoverInfo {
		public final String text;
		
		public HoverInfo(String text) {
			this.text = text;
		}
	}
	private static class AnnotationHoverInfo extends HoverInfo {
		public final Annotation annotation;
		public final Position position;
		public final ITextViewer viewer;
		
		public AnnotationHoverInfo(Annotation annotation, Position position, ITextViewer viewer) {
			super(annotation.getText());
			this.annotation = annotation;
			this.position = position;
			this.viewer = viewer;
		}
	}
	private static class HoverControlCreator extends AbstractReusableInformationControlCreator {
		private final IInformationControlCreator informationPresenterControlCreator;
		
		public HoverControlCreator(IInformationControlCreator informationPresenterControlCreator) {
			this.informationPresenterControlCreator = informationPresenterControlCreator;
		}
		
		@Override
		protected IInformationControl doCreateInformationControl(Shell parent) {
			return new InformationControl(parent) {
				@Override
				public IInformationControlCreator getInformationPresenterControlCreator() {
					return informationPresenterControlCreator;
				}
			};
		}
	}
	private ASMEditor editor;
	private ASMStorage selectedState;
	private IInformationControlCreator hoverControlCreator;
	private DefaultMarkerAnnotationAccess annotationAccess = new DefaultMarkerAnnotationAccess();

	public ASMTextHover(ASMEditor editor) {
		this.editor = editor;
		DebugUITools.getDebugContextManager().getContextService(PlatformUI.getWorkbench().getActiveWorkbenchWindow()).addDebugContextListener(this);
	}
	
	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection = textViewer.getSelectedRange();
		if (selection.x <= offset && offset < selection.x + selection.y)
			return new Region(selection.x, selection.y);
		return new Region(offset, 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion)
	{
		IAnnotationModel model = null;
		if (textViewer instanceof ISourceViewer)
			model= ((ISourceViewer)textViewer).getAnnotationModel();
		if (model != null) {
			Iterator<Annotation> iterator;
			if (model instanceof IAnnotationModelExtension2)
				iterator = ((IAnnotationModelExtension2)model).getAnnotationIterator(hoverRegion.getOffset(), hoverRegion.getLength(), true, true);
			else
				iterator = model.getAnnotationIterator();
			
			int layer = -1;
			Annotation annotation = null;
			Position position = null;
			while (iterator.hasNext()) {
				Annotation a = iterator.next();
				if (!(a instanceof MarkerAnnotation))
					continue;
				Position p = model.getPosition(a);
				int l = annotationAccess.getLayer(a);
				
				if (l > layer && p != null && p.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
					String text = a.getText();
					if (text != null && text.trim().length() > 0) {
						layer = l;
						annotation = a;
						position = p;
					}
				}
			}
			if (layer > -1)
				return new AnnotationHoverInfo(annotation, position, textViewer);
		}
		try {
			if (hoverRegion.getLength() > 0) {
				String value = getExpressionValue(textViewer.getDocument(), hoverRegion.getOffset(), hoverRegion.getLength());
				if (value != null)
					return new HoverInfo(value);
			}
			ASMDocument document = (ASMDocument)textViewer.getDocument();
			ASTNode hoverNode = null;
			List<ASTNode> nodes = document.getASTNodesOnLineOfOffset(hoverRegion.getOffset());
			for (ASTNode node : nodes) {
				int offset = document.getNodePosition(node);
				if (hoverRegion.getOffset() >= offset) {
					if (hoverNode == null || offset >= document.getNodePosition(hoverNode))
						hoverNode = node;
					if (node instanceof FunctionRuleTermNode) {
						FunctionRuleTermNode frNode = (FunctionRuleTermNode)node;
						if (frNode.hasName() && hoverRegion.getOffset() + hoverRegion.getLength() < offset + frNode.getName().length()) {
							String value = getExpressionValue(textViewer.getDocument(), frNode.getName());
							if (isEnvironmentVariable(frNode))
								return new HoverInfo("Environment Variable: " + frNode.getName() + (value != null ? " = " + value : "") + "\nParser Info: " + frNode);
							if (isLocalFunction(frNode))
								return new HoverInfo("Local function: " + frNode.getName() + (value != null ? " = " + value : "") + "\nParser Info: " + frNode);
							FunctionInfo pluginFunction = getPluginFunction(frNode.getName());
							if (pluginFunction != null)
								return new HoverInfo("Plugin Function: " + pluginFunction.plugin + "." + pluginFunction.name + (pluginFunction.signature != null ? ": " + pluginFunction.signature : "") + (value != null ? " = " + value : "") + "\nParser Info: " + frNode);
							String declaration = getDeclaration(document, frNode.getName());
							if (declaration == null)
								return new HoverInfo("Plugin: " + frNode.getPluginName() + "\nParser Info: " + frNode);
							return new HoverInfo(declaration + (value != null ? " = " + value : "") + "\nParser Info: " + frNode);
						}
					}
					else if (node instanceof RuleOrFuncElementNode) {
						RuleOrFuncElementNode ruleOrFuncElementNode = (RuleOrFuncElementNode)node;
						FunctionInfo pluginFunction = getPluginFunction(ruleOrFuncElementNode.getElementName());
						if (pluginFunction != null)
							return new HoverInfo("Plugin Function: " + pluginFunction.plugin + "." + pluginFunction.name + (pluginFunction.signature != null ? ": " + pluginFunction.signature : "") + "\nParser Info: " + ruleOrFuncElementNode);
						String declaration = getDeclaration(document, ruleOrFuncElementNode.getElementName());
						if (declaration == null)
							return new HoverInfo("Plugin: " + ruleOrFuncElementNode.getPluginName() + "\nParser Info: " + ruleOrFuncElementNode);
						return new HoverInfo(declaration + "\nParser Info: " + ruleOrFuncElementNode);
					}
				}
			}
			if (hoverNode == null || hoverNode instanceof FunctionRuleTermNode)
				return null;
			return new HoverInfo("Plugin: " + hoverNode.getPluginName() + "\nParser Info: " + hoverNode);
		} catch (BadLocationException e) {
		}
		return null;
	}
	
	private String getExpressionValue(IDocument doc, int offset, int length) {
		try {
			return getExpressionValue(doc, doc.get(offset, length));
		} catch (BadLocationException e) {
		}
		return null;
	}
	
	private String getExpressionValue(IDocument doc, String expression) {
		if (EngineDebugger.getRunningInstance() != null && selectedState != null) {
			try {
				Element value = EngineDebugger.getRunningInstance().evaluateExpression(expression, selectedState);
				if (value instanceof Enumerable && value.toString().isEmpty())
					return ((Enumerable)value).enumerate().toString();
				return value.toString();
			} catch (Exception e) {
			}
		}
		return null;
	}
	
	private String getDeclaration(ASMDocument document, String functionName) {
		for (Declaration declaration : ASMDeclarationWatcher.getDeclarations(editor.getInputFile(), false)) {
			if (declaration.getName().equals(functionName))
				return declaration.toString();
		}
		IPath relativePath = editor.getInputFile().getProjectRelativePath().removeLastSegments(1);
		IProject project = editor.getInputFile().getProject();
		for (Node node = document.getRootnode().getFirstCSTNode(); node != null; node = node.getNextCSTNode()) {
			if (node instanceof IncludeNode) {
				String includedFunctionDeclaration = getIncludedDeclaration(project, project.getFile(relativePath.append(((IncludeNode)node).getFilename())), functionName, new HashSet<IFile>());
				if (includedFunctionDeclaration != null)
					return includedFunctionDeclaration;
			}
		}
		return null;
	}
	
	private String getIncludedDeclaration(IProject project, IFile file, String functionName, Set<IFile> includedFiles) {
		if (includedFiles.contains(file))
			return null;
		includedFiles.add(file);
		if (file != null) {
			for (Declaration declaration : ASMDeclarationWatcher.getDeclarations(file, false)) {
				if (declaration.getName().equals(functionName))
					return file.getProjectRelativePath() + "\n\n" + declaration;
			}
			for (IFile include : ASMIncludeWatcher.getIncludedFiles(file, false)) {
				String includedFunctionDeclaration = getIncludedDeclaration(project, include, functionName, includedFiles);
				if (includedFunctionDeclaration != null)
					return includedFunctionDeclaration;
			}
		}
		return null;
	}
	
	private boolean isEnvironmentVariable(FunctionRuleTermNode frNode) {
		if (isParam(frNode))
			return true;
		if (isInLetVariableMap(frNode))
			return true;
		if (isForallRuleVariable(frNode))
			return true;
		if (isForallExpVariable(frNode))
			return true;
		if (isExistsExpVariable(frNode))
			return true;
		if (isChooseVariable(frNode))
			return true;
		if (isExtendRuleVariable(frNode))
			return true;
		if (isSetComprehensionConstrainerVariable(frNode))
			return true;
		if (isListComprehensionVariable(frNode))
			return true;
		if (isImportRuleVariable(frNode))
			return true;
		return false;
	}
	
	private boolean isParam(FunctionRuleTermNode frNode) {
		final ASTNode ruleNode = getParentRuleNode(frNode);
		if (ruleNode != null) {
			final ASTNode idNode = ruleNode.getFirst().getFirst();
			for (ASTNode paramNode = idNode.getNext(); paramNode != null; paramNode = paramNode.getNext()) {
				if (paramNode.getToken().equals(frNode.getName()))
					return true;
			}
		}
		return false;
	}
	
	private ASTNode getParentRuleNode(ASTNode node) {
		ASTNode parentRuleNode = node.getParent();
		while (parentRuleNode != null && !Kernel.GR_RULEDECLARATION.equals(parentRuleNode.getGrammarRule()) && !"DerivedFunctionDeclaration".equals(parentRuleNode.getGrammarRule()))
			parentRuleNode = parentRuleNode.getParent();
		return parentRuleNode;
	}
	
	private boolean isInLetVariableMap(FunctionRuleTermNode frNode) {
		for (LetRuleNode letRuleNode = getParentLetRuleNode(frNode); letRuleNode != null; letRuleNode = getParentLetRuleNode(letRuleNode)) {
			try {
				if (letRuleNode.getVariableMap().containsKey(frNode.getName()))
					return true;
			} catch (Exception e) {
			}
		}
		return false;
	}

	private LetRuleNode getParentLetRuleNode(ASTNode node) {
		ASTNode letRuleNode = node.getParent();
		while (letRuleNode != null && !(letRuleNode instanceof LetRuleNode))
			letRuleNode = letRuleNode.getParent();
		if (letRuleNode instanceof LetRuleNode)
			return (LetRuleNode)letRuleNode;
		return null;
	}
	
	private boolean isLocalFunction(FunctionRuleTermNode frNode) {
		for (LocalRuleNode localRuleNode = getParentLocalRuleNode(frNode); localRuleNode != null; localRuleNode = getParentLocalRuleNode(localRuleNode)) {
			if (localRuleNode.getFunctionNames().contains(frNode.getName()))
				return true;
		}
		if (isReturnRuleExpression(frNode))
			return true;
		return false;
	}
	
	private LocalRuleNode getParentLocalRuleNode(ASTNode node) {
		ASTNode localRuleNode = node.getParent();
		while (localRuleNode != null && !(localRuleNode instanceof LocalRuleNode))
			localRuleNode = localRuleNode.getParent();
		if (localRuleNode instanceof LocalRuleNode)
			return (LocalRuleNode)localRuleNode;
		return null;
	}
	
	private boolean isForallRuleVariable(FunctionRuleTermNode frNode) {
		for (ForallRuleNode forallRuleNode = getParentForallRuleNode(frNode); forallRuleNode != null; forallRuleNode = getParentForallRuleNode(forallRuleNode)) {
			if (forallRuleNode.getVariableMap().containsKey(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private ForallRuleNode getParentForallRuleNode(ASTNode node) {
		ASTNode forallRuleNode = node.getParent();
		while (forallRuleNode != null && !(forallRuleNode instanceof ForallRuleNode))
			forallRuleNode = forallRuleNode.getParent();
		if (forallRuleNode instanceof ForallRuleNode)
			return (ForallRuleNode)forallRuleNode;
		return null;
	}
	
	private boolean isForallExpVariable(FunctionRuleTermNode frNode) {
		for (ForallExpNode forallExpNode = getParentForallExpNode(frNode); forallExpNode != null; forallExpNode = getParentForallExpNode(forallExpNode)) {
			if (forallExpNode.getVariable().getToken().equals(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private ForallExpNode getParentForallExpNode(ASTNode node) {
		ASTNode forallExpNode = node.getParent();
		while (forallExpNode != null && !(forallExpNode instanceof ForallExpNode))
			forallExpNode = forallExpNode.getParent();
		if (forallExpNode instanceof ForallExpNode)
			return (ForallExpNode)forallExpNode;
		return null;
	}
	
	private boolean isExistsExpVariable(FunctionRuleTermNode frNode) {
		for (ExistsExpNode existsExpNode = getParentExistsExpNode(frNode); existsExpNode != null; existsExpNode = getParentExistsExpNode(existsExpNode)) {
			if (existsExpNode.getVariable().getToken().equals(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private ExistsExpNode getParentExistsExpNode(ASTNode node) {
		ASTNode existsExpNode = node.getParent();
		while (existsExpNode != null && !(existsExpNode instanceof ExistsExpNode))
			existsExpNode = existsExpNode.getParent();
		if (existsExpNode instanceof ExistsExpNode)
			return (ExistsExpNode)existsExpNode;
		return null;
	}
	
	private boolean isChooseVariable(FunctionRuleTermNode frNode) {
		for (ChooseRuleNode chooseRuleNode = getParentChooseRuleNode(frNode); chooseRuleNode != null; chooseRuleNode = getParentChooseRuleNode(chooseRuleNode)) {
			if (chooseRuleNode.getVariableMap().containsKey(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private ChooseRuleNode getParentChooseRuleNode(ASTNode node) {
		ASTNode chooseRuleNode = node.getParent();
		while (chooseRuleNode != null && !(chooseRuleNode instanceof ChooseRuleNode))
			chooseRuleNode = chooseRuleNode.getParent();
		if (chooseRuleNode instanceof ChooseRuleNode)
			return (ChooseRuleNode)chooseRuleNode;
		return null;
	}
	
	private boolean isExtendRuleVariable(FunctionRuleTermNode frNode) {
		for (ExtendRuleNode extendRuleNode = getParentExtendRuleNode(frNode); extendRuleNode != null; extendRuleNode = getParentExtendRuleNode(extendRuleNode)) {
			if (extendRuleNode.getIdNode().getToken().equals(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private ExtendRuleNode getParentExtendRuleNode(ASTNode node) {
		ASTNode extendRuleNode = node.getParent();
		while (extendRuleNode != null && !(extendRuleNode instanceof ExtendRuleNode))
			extendRuleNode = extendRuleNode.getParent();
		if (extendRuleNode instanceof ExtendRuleNode)
			return (ExtendRuleNode)extendRuleNode;
		return null;
	}
	
	private boolean isSetComprehensionConstrainerVariable(FunctionRuleTermNode frNode) {
		for (SetCompNode setCompNode = getParentSetCompNode(frNode); setCompNode != null; setCompNode = getParentSetCompNode(setCompNode)) {
			try {
				if (setCompNode.getVarBindings().containsKey(frNode.getName()))
					return true;
			} catch (EngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private SetCompNode getParentSetCompNode(ASTNode node) {
		ASTNode setCompNode = node.getParent();
		while (setCompNode != null && !(setCompNode instanceof SetCompNode))
			setCompNode = setCompNode.getParent();
		if (setCompNode instanceof SetCompNode)
			return (SetCompNode)setCompNode;
		return null;
	}
	
	private boolean isListComprehensionVariable(FunctionRuleTermNode frNode) {
		for (ListCompNode listCompNode = getParentListCompNode(frNode); listCompNode != null; listCompNode = getParentListCompNode(listCompNode)) {
			try {
				if (listCompNode.getVarBindings().containsKey(frNode.getName()))
					return true;
			} catch (EngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private ListCompNode getParentListCompNode(ASTNode node) {
		ASTNode listCompNode = node.getParent();
		while (listCompNode != null && !(listCompNode instanceof ListCompNode))
			listCompNode = listCompNode.getParent();
		if (listCompNode instanceof ListCompNode)
			return (ListCompNode)listCompNode;
		return null;
	}
	
	private boolean isReturnRuleExpression(FunctionRuleTermNode frNode) {
		for (ReturnRuleNode returnRuleNode = getParentReturnRuleNode(frNode); returnRuleNode != null; returnRuleNode = getParentReturnRuleNode(returnRuleNode)) {
			if (returnRuleNode.getExpressionNode().getFirst().getToken().equals(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private ReturnRuleNode getParentReturnRuleNode(ASTNode node) {
		ASTNode returnRuleNode = node.getParent();
		while (returnRuleNode != null && !(returnRuleNode instanceof ReturnRuleNode))
			returnRuleNode = returnRuleNode.getParent();
		if (returnRuleNode instanceof ReturnRuleNode)
			return (ReturnRuleNode)returnRuleNode;
		return null;
	}
	
	private boolean isImportRuleVariable(FunctionRuleTermNode frNode) {
		for (ASTNode importRuleNode = getParentImportRuleNode(frNode); importRuleNode != null; importRuleNode = getParentImportRuleNode(importRuleNode)) {
			if (importRuleNode.getFirst().getToken().equals(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private ASTNode getParentImportRuleNode(ASTNode node) {
		ASTNode importRuleNode = node.getParent();
		while (importRuleNode != null && !"ImportRule".equals(importRuleNode.getGrammarRule()))
			importRuleNode = importRuleNode.getParent();
		if (importRuleNode != null && "ImportRule".equals(importRuleNode.getGrammarRule()))
			return importRuleNode;
		return null;
	}
	
	private FunctionInfo getPluginFunction(String functionName) {
		Map<String, FunctionInfo> pluginFunctions = new HashMap<String, FunctionInfo>();
		for (FunctionInfo functionInfo : editor.getParser().getSlimEngine().getSpec().getDefinedFunctions())
			pluginFunctions.put(functionInfo.name, functionInfo);
		for (FunctionInfo functionInfo : editor.getParser().getSlimEngine().getSpec().getDefinedUniverses())
			pluginFunctions.put(functionInfo.name, functionInfo);
		for (FunctionInfo functionInfo : editor.getParser().getSlimEngine().getSpec().getDefinedBackgrounds())
			pluginFunctions.put(functionInfo.name, functionInfo);
		return pluginFunctions.get(functionName);
	}
	
	@Override
	public IInformationControlCreator getHoverControlCreator() 
	{
		if (hoverControlCreator == null)
			hoverControlCreator = new HoverControlCreator(new HoverControlCreator(null));
		return hoverControlCreator;
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
	{
		return null;
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		selectedState = null;
		ISelection context = event.getContext();
		if (context instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection)context).getFirstElement();
			if (element instanceof ASMStackFrame) {
				ASMStackFrame frame = (ASMStackFrame)element;
				selectedState = frame.getState();
			}
		}
	}
	
	private static class InformationControl extends AbstractInformationControl implements IInformationControlExtension2 {
		private Composite parent;
		private DefaultMarkerAnnotationAccess markerAnnotationAccess;
		private HoverInfo input;

		public InformationControl(Shell parentShell) {
			super(parentShell, false);
			markerAnnotationAccess = new DefaultMarkerAnnotationAccess();
			create();
		}

		@Override
		public boolean hasContents() {
			return input != null;
		}

		@Override
		public void setInput(Object input) {
			if (!(input instanceof HoverInfo))
				return;
			
			this.input = (HoverInfo)input;
			
			for (Control child : parent.getChildren())
				child.dispose();
			
			createHeader(parent, this.input);
			if (input instanceof AnnotationHoverInfo) {
				ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
				ASMQuickAssistProcessor.collectProposals(((AnnotationHoverInfo)input).annotation, proposals);
				if (!proposals.isEmpty())
					createProposalList(parent, proposals);
			}
			applyParentDesign(parent);
			parent.layout(true);
		}

		@Override
		protected void createContent(Composite parent) {
			this.parent = parent;
			GridLayout layout = new GridLayout(1, false);
			layout.verticalSpacing = 0;
			layout.marginLeft = 0;
			layout.marginHeight = 0;
			parent.setLayout(layout);
		}
		
		@Override
		public Point computeSizeHint()
		{		
			Point size = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			Point constraints = getSizeConstraints();
			if (constraints == null)
				return size;
			Rectangle trim = getShell().computeTrim(0, 0, 0, 0);
			Point constrainedSize = getShell().computeSize(constraints.x - trim.width, SWT.DEFAULT, true);
			return new Point(Math.min(size.x, constrainedSize.x), Math.max(size.y, constrainedSize.y));
		}
		
		private void createHeader(Composite parent, final HoverInfo info) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			GridLayout layout = new GridLayout(2, false);
			layout.horizontalSpacing = 0;
			composite.setLayout(layout);
			
			if (info instanceof AnnotationHoverInfo) {
				final Canvas canvas = new Canvas(composite, SWT.NO_FOCUS);
				GridData gridData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
				gridData.widthHint = 17;
				gridData.heightHint = 16;
				canvas.setLayoutData(gridData);
				canvas.addPaintListener(new PaintListener() {
					
					@Override
					public void paintControl(PaintEvent e) {
						e.gc.setFont(null);
						markerAnnotationAccess.paint(((AnnotationHoverInfo)info).annotation, e.gc, canvas, new Rectangle(0, 0, 16, 16));
					}
				});
			}
				
			StyledText text = new StyledText(composite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			text.setLayoutData(data);
			if (info.text != null)
				text.setText(info.text);
		}
		
		private void createProposalList(Composite parent, List<ICompletionProposal> proposals) {
			new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			scrolledComposite.setLayoutData(gridData);
			scrolledComposite.setExpandHorizontal(false);
			scrolledComposite.setExpandVertical(false);
			
			Composite composite = new Composite(scrolledComposite, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridLayout layout = new GridLayout(2, false);
			layout.verticalSpacing = 0;
			composite.setLayout(layout);
			for (ICompletionProposal proposal : proposals)
				createProposalLink(composite, proposal);
			scrolledComposite.setContent(composite);
			
			Point size = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			composite.setSize(size);
			gridData.heightHint = size.y;
		}
		
		private void createProposalLink(Composite parent, final ICompletionProposal proposal) {
			Label imageLabel = new Label(parent, SWT.NONE);
			imageLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			imageLabel.setImage(proposal.getImage());
			imageLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					if (e.button == 1)
						applyProposal(proposal);
				}
			});
			
			Link link = new Link(parent, SWT.WRAP);
			link.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			link.setText("<a>" + proposal.getDisplayString() + "</a>");
			link.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					applyProposal(proposal);
				}
			});
		}
		
		private void applyParentDesign(Control control) {
			control.setForeground(parent.getForeground());
			control.setBackground(parent.getBackground());
			control.setFont(parent.getFont());
			if (control instanceof Composite) {
				for (Control child : ((Composite)control).getChildren())
					applyParentDesign(child);
			}
		}
		
		private void applyProposal(ICompletionProposal proposal) {
			if (!(input instanceof AnnotationHoverInfo))
				return;
			AnnotationHoverInfo info = ((AnnotationHoverInfo)input);
			dispose();
			if (proposal instanceof ICompletionProposalExtension)
				((ICompletionProposalExtension)proposal).apply(info.viewer.getDocument(), (char)0, info.position.offset);
			else
				proposal.apply(info.viewer.getDocument());
			
			Point selection = proposal.getSelection(info.viewer.getDocument());
			if (selection != null) {
				info.viewer.setSelectedRange(selection.x, selection.y);
				info.viewer.revealRange(selection.x, selection.y);
			}
		}
	}
}