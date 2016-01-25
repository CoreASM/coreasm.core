package org.coreasm.eclipse.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.coreasm.eclipse.editors.ASMParser.ParsingResult;
import org.coreasm.eclipse.util.Utilities;
import org.coreasm.engine.EngineException;
import org.coreasm.engine.absstorage.Signature;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.plugins.bag.BagCompNode;
import org.coreasm.engine.plugins.chooserule.ChooseRuleNode;
import org.coreasm.engine.plugins.chooserule.PickExpNode;
import org.coreasm.engine.plugins.extendrule.ExtendRuleNode;
import org.coreasm.engine.plugins.forallrule.ForallRuleNode;
import org.coreasm.engine.plugins.foreachrule.ForeachRuleNode;
import org.coreasm.engine.plugins.letrule.LetRuleNode;
import org.coreasm.engine.plugins.list.ListCompNode;
import org.coreasm.engine.plugins.predicatelogic.ExistsExpNode;
import org.coreasm.engine.plugins.predicatelogic.ForallExpNode;
import org.coreasm.engine.plugins.set.SetCompNode;
import org.coreasm.engine.plugins.signature.DerivedFunctionNode;
import org.coreasm.engine.plugins.signature.EnumerationElement;
import org.coreasm.engine.plugins.signature.EnumerationNode;
import org.coreasm.engine.plugins.signature.FunctionNode;
import org.coreasm.engine.plugins.signature.UniverseNode;
import org.coreasm.engine.plugins.turboasm.LocalRuleNode;
import org.coreasm.engine.plugins.turboasm.ReturnRuleNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;

/**
 * The <code>ASMDeclarationWatcher</code> manages the markers for declarations.
 * @author Michael Stegmaier
 *
 */
public class ASMDeclarationWatcher implements Observer {
	private static final String DECLARATION_SEPERATOR = "\u25c9";
	
	public static abstract class Declaration {
		protected final String name;
		protected final IFile file;
		
		protected Declaration(String name, IFile file) {
			if (name == null)
				throw new IllegalArgumentException("Name must not be null!");
			this.name = name;
			this.file = file;
		}

		public String getName() {
			return name;
		}
		
		public IFile getFile() {
			return file;
		}
		
		public static Declaration decode(String declaration, IFile file) {
			String type = declaration.substring(0, declaration.indexOf(':'));
			declaration = declaration.substring(type.length() + 2);
			if ("Function".equals(type))
				return new FunctionDeclaration(declaration, file);
			else if ("Universe".equals(type))
				return new UniverseDeclaration(declaration, file);
			else if ("Enumeration".equals(type))
				return new EnumerationDeclaration(declaration, file);
			else if ("Derived Function".equals(type))
				return new DerivedFunctionDeclaration(declaration, file);
			else if ("Rule".equals(type))
				return new RuleDeclaration(declaration, file);
			return null;
		}
		
		public static Declaration from(ASTNode signature) {
			return from(signature, null);
		}
		
		public static Declaration from(ASTNode signature, String comment) {
			if (signature instanceof EnumerationNode)
				return new EnumerationDeclaration((EnumerationNode)signature, comment);
			if (signature instanceof FunctionNode)
				return new FunctionDeclaration((FunctionNode)signature, comment);
			if (signature instanceof UniverseNode)
				return new UniverseDeclaration((UniverseNode)signature, comment);
			if (signature instanceof DerivedFunctionNode)
				return new DerivedFunctionDeclaration(((DerivedFunctionNode)signature), comment);
			if (Kernel.GR_RULEDECLARATION.equals(signature.getGrammarRule()))
				return new RuleDeclaration(signature, comment);
			return null;
		}
	}
	public static class FunctionDeclaration extends Declaration {
		private final Signature signature;
		private String comment;
		
		private FunctionDeclaration(FunctionNode node, String comment) {
			super(node.getName(), null);
			this.signature = new Signature();
			this.signature.setDomain(node.getDomain());
			this.signature.setRange(node.getRange());
			this.comment = comment;
		}
		
		private FunctionDeclaration(String declaration, IFile file) {
			super(declaration.substring(0, declaration.indexOf(':')), file);
			int index = declaration.indexOf("->");
			String declarationDomain = declaration.substring(name.length() + 2, index - 1);
			String[] domain = declarationDomain.split(" x ");
			signature = new Signature();
			if (domain.length > 0 && !domain[0].isEmpty())
				signature.setDomain(domain);
			int indexOfNewLine = declaration.indexOf('\n');
			if (indexOfNewLine >= 0) {
				comment = declaration.substring(indexOfNewLine + 2);
				signature.setRange(declaration.substring(index + 3, indexOfNewLine));
			}
			else
				signature.setRange(declaration.substring(index + 3));
		}
		
		public List<String> getDomain() {
			return signature.getDomain();
		}

		public String getRange() {
			return signature.getRange();
		}
		
		public Signature getSignature() {
			return signature;
		}
		
		@Override
		public String toString() {
			return "Function: " + name + ": " + signature + (comment != null ? "\n\n" + comment : "");
		}
	}
	public static class UniverseDeclaration extends Declaration {
		public static class Member extends Declaration {
			private UniverseDeclaration parent;
			
			private Member(UniverseDeclaration parent, String name) {
				super(name, parent.getFile());
				this.parent = parent;
			}
			
			@Override
			public String toString() {
				return "Universe member: " + parent.getName() + "(" + name + ")";
			}
		}
		private List<Member> members = new ArrayList<Member>();
		private String comment;
		
		private UniverseDeclaration(UniverseNode node, String comment) {
			super(node.getName(), null);
			for (ASTNode member = node.getFirst().getNext(); member != null; member = member.getNext())
				members.add(new Member(this, member.getToken()));
			this.comment = comment;
		}
		
		private UniverseDeclaration(String declaration, IFile file) {
			super(declaration.substring(0, declaration.indexOf('=') - 1), file);
			int indexOfMembers = declaration.indexOf('{');
			if (indexOfMembers > 0) {
				for (String member : declaration.substring(indexOfMembers + 2, declaration.indexOf('}') - 1).split(", "))
					members.add(new Member(this, member));
			}
			int indexOfNewLine = declaration.indexOf('\n');
			if (indexOfNewLine >= 0)
				comment = declaration.substring(indexOfNewLine + 2);
		}
		
		public List<Member> getMembers() {
			return members;
		}

		@Override
		public String toString() {
			String declaration = "Universe: " + name;
			declaration += " = { ";
			for (Member member : members) {
				if (!declaration.endsWith("{ "))
					declaration += ", ";
				declaration += member.getName();
			}
			declaration += " }";
			if (comment != null)
				declaration += "\n\n" + comment;
			return declaration;
		}
	}
	public static class EnumerationDeclaration extends Declaration {
		public static class Member extends Declaration {
			private EnumerationDeclaration parent;
			
			private Member(EnumerationDeclaration parent, String name) {
				super(name, parent.getFile());
				this.parent = parent;
			}
			
			@Override
			public String toString() {
				return "Enumeration member: " + parent.getName() + "(" + name + ")";
			}
		}
		private List<Member> members = new ArrayList<Member>();
		private String comment;
		
		private EnumerationDeclaration(EnumerationNode node, String comment) {
			super(node.getName(), null);
			for (EnumerationElement member : node.getMembers())
				members.add(new Member(this, member.getName()));
			this.comment = comment;
		}
		
		private EnumerationDeclaration(String declaration, IFile file) {
			super(declaration.substring(0, declaration.indexOf('=') - 1), file);
			int indexOfMembers = declaration.indexOf('{');
			if (indexOfMembers > 0) {
				for (String member : declaration.substring(indexOfMembers + 2, declaration.indexOf('}') - 1).split(", "))
					members.add(new Member(this, member));
			}
			int indexOfNewLine = declaration.indexOf('\n');
			if (indexOfNewLine >= 0)
				comment = declaration.substring(indexOfNewLine + 2);
		}
		
		public List<Member> getMembers() {
			return members;
		}

		@Override
		public String toString() {
			String declaration = "Enumeration: " + name + " = { ";
			for (Member member : members) {
				if (!declaration.endsWith("{ "))
					declaration += ", ";
				declaration += member.getName();
			}
			declaration += " }";
			if (comment != null)
				declaration += "\n\n" + comment;
			return declaration;
		}
	}
	public static class DerivedFunctionDeclaration extends Declaration {
		private List<String> params = new ArrayList<String>();
		private String comment;
		private String returnExpression;
		
		private DerivedFunctionDeclaration(DerivedFunctionNode node, String comment) {
			super(node.getNameSignatureNode().getFirst().getToken(), null);
			for (ASTNode param = node.getNameSignatureNode().getFirst().getNext(); param != null; param = param.getNext()) 
				params.add(param.getToken());
			if (node.getFirst().getNext() instanceof ReturnRuleNode)
				returnExpression = node.getFirst().getNext().getFirst().unparseTree().replace("  ", " ");
			this.comment = comment;
		}
		
		private DerivedFunctionDeclaration(String declaration, IFile file) {
			super(findName(declaration), file);
			declaration = declaration.substring(name.length());
			if (declaration.startsWith("("))
				params = Arrays.asList(declaration.substring(1, declaration.indexOf(')')).split(", "));
			int indexOfNewLine = declaration.indexOf('\n');
			if (indexOfNewLine >= 0 && declaration.charAt(indexOfNewLine + 1) != '\n') {
				int beginIndex = indexOfNewLine + "\nreturns".length();
				indexOfNewLine = declaration.indexOf('\n', beginIndex);
				if (indexOfNewLine >= 0)
					returnExpression = declaration.substring(beginIndex, indexOfNewLine);
				else
					returnExpression = declaration.substring(beginIndex);
			}
			if (indexOfNewLine >= 0)
				comment = declaration.substring(indexOfNewLine + 2);
		}
		
		private static String findName(String declaration) {
			int indexOfNewline = declaration.indexOf('\n');
			if (indexOfNewline >= 0)
				declaration = declaration.substring(0, indexOfNewline);
			int indexOfBracket = declaration.indexOf('(');
			if (indexOfBracket >= 0)
				declaration = declaration.substring(0, indexOfBracket);
			return declaration;
		}
		
		public List<String> getParams() {
			return params;
		}
		
		@Override
		public String toString() {
			String declaration = "Derived Function: " + name + "(";
			for (String param : params) {
				if (!declaration.endsWith("("))
					declaration += ", ";
				declaration += param;
			}
			declaration = (declaration + ")").replace("()", "");
			if (returnExpression != null)
				declaration += "\nreturns " + returnExpression;
			if (comment != null)
				declaration += "\n\n" + comment;
			return declaration;
		}
	}
	public static class RuleDeclaration extends Declaration {
		private List<String> params = new ArrayList<String>();
		private String comment;
		
		private RuleDeclaration(ASTNode node, String comment) {
			super(node.getFirst().getFirst().getToken(), null);
			if (!Kernel.GR_RULEDECLARATION.equals(node.getGrammarRule()))
				throw new IllegalArgumentException("Illegal GrammarRule: " + node.getGrammarRule());
			for (ASTNode param = node.getFirst().getFirst().getNext(); param != null; param = param.getNext())
				params.add(param.getToken());
			this.comment = comment;
		}
		
		private RuleDeclaration(String declaration, IFile file) {
			super(findName(declaration), file);
			declaration = declaration.substring(name.length());
			if (declaration.startsWith("("))
				params = Arrays.asList(declaration.substring(1, declaration.indexOf(')')).split(", "));
			int indexOfNewLine = declaration.indexOf('\n');
			if (indexOfNewLine >= 0)
				comment = declaration.substring(indexOfNewLine + 2);
		}
		
		private static String findName(String declaration) {
			int indexOfNewline = declaration.indexOf('\n');
			if (indexOfNewline >= 0)
				declaration = declaration.substring(0, indexOfNewline);
			int indexOfBracket = declaration.indexOf('(');
			if (indexOfBracket >= 0)
				declaration = declaration.substring(0, indexOfBracket);
			return declaration;
		}
		
		public List<String> getParams() {
			return params;
		}
		
		@Override
		public String toString() {
			String declaration = "Rule: " + name + "(";
			for (String param : params) {
				if (!declaration.endsWith("("))
					declaration += ", ";
				declaration += param;
			}
			declaration = (declaration + ")").replace("()", "");
			if (comment != null)
				declaration += "\n\n" + comment;
			return declaration;
		}
	}
	public static class Call {
		private final ASTNode declarationNode;
		private final ASTNode callerNode;
		private final IFile file;
		
		public Call(ASTNode declarationNode, ASTNode callerNode, IFile file) {
			this.declarationNode = declarationNode;
			this.callerNode = callerNode;
			this.file = file;
		}
		
		public Call(IFile file) {
			this(null, null, file);
		}

		public ASTNode getDeclarationNode() {
			return declarationNode;
		}

		public ASTNode getCallerNode() {
			return callerNode;
		}

		public IFile getFile() {
			return file;
		}
	}
	
	private final ASMEditor editor;
	
	public ASMDeclarationWatcher(ASMEditor editor) {
		this.editor = editor;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (o != editor.getParser() || !(arg instanceof ParsingResult))
			return;
		ParsingResult result = (ParsingResult)arg;
		if (result.wasSuccessful) {
			String declarations = "";
			for (Declaration declaration : getDeclarations(result.document)) {
				if (declaration instanceof UniverseDeclaration.Member || declaration instanceof EnumerationDeclaration.Member)
					continue;
				if (!declarations.isEmpty())
					declarations += DECLARATION_SEPERATOR;
				declarations += declaration;
			}
			editor.createDeclarationsMark(declarations);
		}
	}
	
	public static List<Call> getCallers(ASTNode referenceNode, IFile referenceFile) {
		List<Call> callers = new ArrayList<Call>();
		ASTNode node = referenceNode;
		Declaration declaration = Declaration.from(referenceNode);
		String declarationName;
		if (declaration == null) {
			if (node != null) {
				while (node.getFirst() != null)
					node = node.getFirst();
			}
			declarationName = node.getToken();
		}
		else
			declarationName = declaration.getName();
		if (declarationName != null) {
			if (referenceFile != null) {
				IFile[] files = ASMIncludeWatcher.getInvolvedFiles(referenceFile);
				LinkedList<ASTNode> fringe = new LinkedList<ASTNode>();
				for (IFile file : files) {
					ASMEditor editor = (ASMEditor)Utilities.getEditor(file);
					if (editor == null) {
						callers.add(new Call(file));
						continue;
					}
					ASMDocument document = (ASMDocument)editor.getDocumentProvider().getDocument(editor.getInput());
					ASTNode rootNode = (ASTNode)document.getRootnode();
					if (rootNode != null) {
						fringe.add(rootNode);
						while (!fringe.isEmpty()) {
							node = fringe.removeFirst();
							if (ASTNode.FUNCTION_RULE_CLASS.equals(node.getGrammarClass()) && node instanceof FunctionRuleTermNode) {
								FunctionRuleTermNode frNode = (FunctionRuleTermNode)node;
								if (frNode.hasName()) {
									if (declarationName.equals(frNode.getName()))
										callers.add(new Call(ASMDocument.getSurroundingDeclaration(frNode), frNode.getParent(), document.getNodeFile(frNode)));
								}
							}
							fringe.addAll(node.getAbstractChildNodes());
						}
					}
				}
			}
		}
		return callers;
	}
	
	public static List<Declaration> getDeclarations(IFile file, boolean includedDeclarations) {
		List<Declaration> declarations = new ArrayList<Declaration>();
		collectDeclarations(file, includedDeclarations, declarations);
		return declarations;
	}
	
	public static Declaration findDeclaration(String name, IFile contextfile) {
		for (IFile file : ASMIncludeWatcher.getInvolvedFiles(contextfile)) {
			for (Declaration declaration : getDeclarations(file, false)) {
				if (name.equals(declaration.getName()))
					return declaration;
			}
		}
		return null;
	}
	
	public static ASTNode findDeclarationNode(String name, ASMDocument document) {
		for (ASTNode node = ((ASTNode)document.getRootnode()).getFirst(); node != null; node = node.getNext()) {
			if (ASTNode.DECLARATION_CLASS.equals(node.getGrammarClass())) {
				if ("Signature".equals(node.getGrammarRule())) {
					for (ASTNode signature = node.getFirst(); signature != null; signature = signature.getNext()) {
						Declaration declaration = Declaration.from(signature);
						if (declaration != null && declaration.getName().equals(name))
							return signature;
						if (declaration instanceof EnumerationDeclaration) {
							EnumerationDeclaration enumeration = (EnumerationDeclaration)declaration;
							for (EnumerationDeclaration.Member member : enumeration.getMembers()) {
								if (member.getName().equals(name))
									return signature;
							}
						}
						else if (declaration instanceof UniverseDeclaration) {
							UniverseDeclaration universe = (UniverseDeclaration)declaration;
							for (UniverseDeclaration.Member member : universe.getMembers()) {
								if (member.getName().equals(name))
									return signature;
							}
						}
					}
				}
				else if (Kernel.GR_RULEDECLARATION.equals(node.getGrammarRule()) && new RuleDeclaration(node, null).getName().equals(name))
					return node;
			}
		}
		return null;
	}
	
	private static void collectDeclarations(IFile file, boolean includedDeclarations, List<Declaration> declarations) {
		if (file == null)
			return;
		try {
			if (file.exists()) {
				IMarker[] declarationMarker = file.findMarkers(ASMEditor.MARKER_TYPE_DECLARATIONS, false, IResource.DEPTH_ZERO);
				if (declarationMarker.length > 0) {
					String markerDeclarations = declarationMarker[0].getAttribute("declarations", "");
					if (!markerDeclarations.isEmpty()) {
						for (String declaration : markerDeclarations.split(DECLARATION_SEPERATOR)) {
							Declaration decodedDeclaration = Declaration.decode(declaration, file);
							if (decodedDeclaration != null)
								declarations.add(decodedDeclaration);
							if (decodedDeclaration instanceof EnumerationDeclaration) {
								EnumerationDeclaration enumeration = (EnumerationDeclaration)decodedDeclaration;
								for (EnumerationDeclaration.Member member : enumeration.getMembers())
									declarations.add(member);
							}
							else if (decodedDeclaration instanceof UniverseDeclaration) {
								UniverseDeclaration universe = (UniverseDeclaration)decodedDeclaration;
								for (UniverseDeclaration.Member member : universe.getMembers())
									declarations.add(member);
							}
						}
					}
				}
				if (includedDeclarations) {
					for (IFile includedFile : ASMIncludeWatcher.getIncludedFiles(file, true))
						collectDeclarations(includedFile, false, declarations);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private List<Declaration> getDeclarations(ASMDocument document) {
		List<Declaration> declarations = new ArrayList<Declaration>();
		for (ASTNode node = ((ASTNode)document.getRootnode()).getFirst(); node != null; node = node.getNext()) {
			if (ASTNode.DECLARATION_CLASS.equals(node.getGrammarClass())) {
				if ("Signature".equals(node.getGrammarRule())) {
					for (ASTNode signature = node.getFirst(); signature != null; signature = signature.getNext()) {
						Declaration declaration = Declaration.from(signature, parseComment(document, node));
						if (declaration != null)
							declarations.add(declaration);
						if (declaration instanceof EnumerationDeclaration) {
							EnumerationDeclaration enumeration = (EnumerationDeclaration)declaration;
							for (EnumerationDeclaration.Member member : enumeration.getMembers())
								declarations.add(member);
						}
						else if (declaration instanceof UniverseDeclaration) {
							UniverseDeclaration universe = (UniverseDeclaration)declaration;
							for (UniverseDeclaration.Member member : universe.getMembers())
								declarations.add(member);
						}
					}
				}
				else if (Kernel.GR_RULEDECLARATION.equals(node.getGrammarRule()))
					declarations.add(new RuleDeclaration(node, parseComment(document, node)));
			}
		}
		return declarations;
	}
	
	private static String parseComment(ASMDocument document, ASTNode node) {
		try {
			String comment = "";
			String line;
			int lineNumber = document.getLineOfNode(node);
			boolean blockComment = false;
			do {
				lineNumber--;
				line = document.get(document.getLineOffset(lineNumber), document.getLineLength(lineNumber)).trim();
				if (line.startsWith("//"))
					comment = line.substring(2).trim() + "\n" + comment;
				else if (line.contains("/*") && line.contains("*/")) {
					comment = line.replace("/*", "").replace("*/", "").trim() + "\n" + comment;
					blockComment = false;
				}
				else if (line.contains("*/")) {
					comment = line.replace("*/", "").trim() + "\n" + comment;
					blockComment = true;
				}
				else if (line.contains("/*")) {
					comment = line.replace("/*", "").trim() + "\n" + comment;
					blockComment = false;
				}
				else if (blockComment)
					comment = line.replace("*", "").trim() + "\n" + comment;
			} while (line.startsWith("//") || blockComment);
			comment = comment.replace("*", "").trim();
			if (comment.isEmpty())
				return null;
			return comment;
		} catch (BadLocationException e) {
		}
		return null;
	}
	
	public static boolean isLocalFunction(FunctionRuleTermNode frNode) {
		for (LocalRuleNode localRuleNode = getParentLocalRuleNode(frNode); localRuleNode != null; localRuleNode = getParentLocalRuleNode(localRuleNode)) {
			if (localRuleNode.getFunctionNames().contains(frNode.getName()))
				return true;
		}
		if (isReturnRuleExpression(frNode))
			return true;
		return false;
	}
	
	private static LocalRuleNode getParentLocalRuleNode(ASTNode node) {
		ASTNode localRuleNode = node.getParent();
		while (localRuleNode != null && !(localRuleNode instanceof LocalRuleNode))
			localRuleNode = localRuleNode.getParent();
		if (localRuleNode instanceof LocalRuleNode)
			return (LocalRuleNode)localRuleNode;
		return null;
	}
	
	private static boolean isReturnRuleExpression(FunctionRuleTermNode frNode) {
		for (ReturnRuleNode returnRuleNode = getParentReturnRuleNode(frNode); returnRuleNode != null; returnRuleNode = getParentReturnRuleNode(returnRuleNode)) {
			ASTNode expression = returnRuleNode.getExpressionNode();
			if (expression instanceof FunctionRuleTermNode && ((FunctionRuleTermNode)expression).getName().equals(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private static ReturnRuleNode getParentReturnRuleNode(ASTNode node) {
		ASTNode returnRuleNode = node.getParent();
		while (returnRuleNode != null && !(returnRuleNode instanceof ReturnRuleNode))
			returnRuleNode = returnRuleNode.getParent();
		if (returnRuleNode instanceof ReturnRuleNode)
			return (ReturnRuleNode)returnRuleNode;
		return null;
	}
	
	public static boolean isEnvironmentVariable(FunctionRuleTermNode frNode) {
		if (isParam(frNode))
			return true;
		if (isInLetVariableMap(frNode))
			return true;
		if (isForallRuleVariable(frNode))
			return true;
		if (isForallExpVariable(frNode))
			return true;
		if (isForeachRuleVariable(frNode))
			return true;
		if (isExistsExpVariable(frNode))
			return true;
		if (isChooseVariable(frNode))
			return true;
		if (isPickExpVariable(frNode))
			return true;
		if (isExtendRuleVariable(frNode))
			return true;
		if (isSetComprehensionVariable(frNode))
			return true;
		if (isBagComprehensionVariable(frNode))
			return true;
		if (isListComprehensionVariable(frNode))
			return true;
		if (isImportRuleVariable(frNode))
			return true;
		return false;
	}
	
	private static boolean isParam(FunctionRuleTermNode frNode) {
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
	
	private static ASTNode getParentRuleNode(ASTNode node) {
		ASTNode parentRuleNode = node.getParent();
		while (parentRuleNode != null && !Kernel.GR_RULEDECLARATION.equals(parentRuleNode.getGrammarRule()) && !"DerivedFunctionDeclaration".equals(parentRuleNode.getGrammarRule()))
			parentRuleNode = parentRuleNode.getParent();
		return parentRuleNode;
	}
	
	private static boolean isInLetVariableMap(FunctionRuleTermNode frNode) {
		for (LetRuleNode letRuleNode = getParentLetRuleNode(frNode); letRuleNode != null; letRuleNode = getParentLetRuleNode(letRuleNode)) {
			try {
				if (letRuleNode.getVariableMap().containsKey(frNode.getName()))
					return true;
			} catch (Exception e) {
			}
		}
		return false;
	}

	private static LetRuleNode getParentLetRuleNode(ASTNode node) {
		ASTNode letRuleNode = node.getParent();
		while (letRuleNode != null && !(letRuleNode instanceof LetRuleNode))
			letRuleNode = letRuleNode.getParent();
		if (letRuleNode instanceof LetRuleNode)
			return (LetRuleNode)letRuleNode;
		return null;
	}
	
	private static boolean isForallRuleVariable(FunctionRuleTermNode frNode) {
		for (ForallRuleNode forallRuleNode = getParentForallRuleNode(frNode); forallRuleNode != null; forallRuleNode = getParentForallRuleNode(forallRuleNode)) {
			if (forallRuleNode.getVariableMap().containsKey(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private static ForallRuleNode getParentForallRuleNode(ASTNode node) {
		ASTNode forallRuleNode = node.getParent();
		while (forallRuleNode != null && !(forallRuleNode instanceof ForallRuleNode))
			forallRuleNode = forallRuleNode.getParent();
		if (forallRuleNode instanceof ForallRuleNode)
			return (ForallRuleNode)forallRuleNode;
		return null;
	}
	
	private static boolean isForallExpVariable(FunctionRuleTermNode frNode) {
		for (ForallExpNode forallExpNode = getParentForallExpNode(frNode); forallExpNode != null; forallExpNode = getParentForallExpNode(forallExpNode)) {
			if (forallExpNode.getVariableMap().containsKey(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private static ForallExpNode getParentForallExpNode(ASTNode node) {
		ASTNode forallExpNode = node.getParent();
		while (forallExpNode != null && !(forallExpNode instanceof ForallExpNode))
			forallExpNode = forallExpNode.getParent();
		if (forallExpNode instanceof ForallExpNode)
			return (ForallExpNode)forallExpNode;
		return null;
	}
	
	private static boolean isForeachRuleVariable(FunctionRuleTermNode frNode) {
		for (ForeachRuleNode foreachRuleNode = getParentForeachRuleNode(frNode); foreachRuleNode != null; foreachRuleNode = getParentForeachRuleNode(foreachRuleNode)) {
			if (foreachRuleNode.getVariableMap().containsKey(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private static ForeachRuleNode getParentForeachRuleNode(ASTNode node) {
		ASTNode foreachRuleNode = node.getParent();
		while (foreachRuleNode != null && !(foreachRuleNode instanceof ForeachRuleNode))
			foreachRuleNode = foreachRuleNode.getParent();
		if (foreachRuleNode instanceof ForeachRuleNode)
			return (ForeachRuleNode)foreachRuleNode;
		return null;
	}
	
	private static boolean isExistsExpVariable(FunctionRuleTermNode frNode) {
		for (ExistsExpNode existsExpNode = getParentExistsExpNode(frNode); existsExpNode != null; existsExpNode = getParentExistsExpNode(existsExpNode)) {
			if (existsExpNode.getVariableMap().containsKey(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private static ExistsExpNode getParentExistsExpNode(ASTNode node) {
		ASTNode existsExpNode = node.getParent();
		while (existsExpNode != null && !(existsExpNode instanceof ExistsExpNode))
			existsExpNode = existsExpNode.getParent();
		if (existsExpNode instanceof ExistsExpNode)
			return (ExistsExpNode)existsExpNode;
		return null;
	}
	
	private static boolean isChooseVariable(FunctionRuleTermNode frNode) {
		for (ChooseRuleNode chooseRuleNode = getParentChooseRuleNode(frNode); chooseRuleNode != null; chooseRuleNode = getParentChooseRuleNode(chooseRuleNode)) {
			if (chooseRuleNode.getVariableMap().containsKey(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private static ChooseRuleNode getParentChooseRuleNode(ASTNode node) {
		ASTNode chooseRuleNode = node.getParent();
		while (chooseRuleNode != null && !(chooseRuleNode instanceof ChooseRuleNode))
			chooseRuleNode = chooseRuleNode.getParent();
		if (chooseRuleNode instanceof ChooseRuleNode)
			return (ChooseRuleNode)chooseRuleNode;
		return null;
	}
	
	private static boolean isPickExpVariable(FunctionRuleTermNode frNode) {
		for (PickExpNode pickExpNode = getParentPickExpNode(frNode); pickExpNode != null; pickExpNode = getParentPickExpNode(pickExpNode)) {
			if (pickExpNode.getVariable().getToken().equals(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private static PickExpNode getParentPickExpNode(ASTNode node) {
		ASTNode pickExpNode = node.getParent();
		while (pickExpNode != null && !(pickExpNode instanceof PickExpNode))
			pickExpNode = pickExpNode.getParent();
		if (pickExpNode instanceof PickExpNode)
			return (PickExpNode)pickExpNode;
		return null;
	}
	
	private static boolean isExtendRuleVariable(FunctionRuleTermNode frNode) {
		for (ExtendRuleNode extendRuleNode = getParentExtendRuleNode(frNode); extendRuleNode != null; extendRuleNode = getParentExtendRuleNode(extendRuleNode)) {
			if (extendRuleNode.getIdNode().getToken().equals(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private static ExtendRuleNode getParentExtendRuleNode(ASTNode node) {
		ASTNode extendRuleNode = node.getParent();
		while (extendRuleNode != null && !(extendRuleNode instanceof ExtendRuleNode))
			extendRuleNode = extendRuleNode.getParent();
		if (extendRuleNode instanceof ExtendRuleNode)
			return (ExtendRuleNode)extendRuleNode;
		return null;
	}
	
	private static boolean isSetComprehensionVariable(FunctionRuleTermNode frNode) {
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
	
	private static SetCompNode getParentSetCompNode(ASTNode node) {
		ASTNode setCompNode = node.getParent();
		while (setCompNode != null && !(setCompNode instanceof SetCompNode))
			setCompNode = setCompNode.getParent();
		if (setCompNode instanceof SetCompNode)
			return (SetCompNode)setCompNode;
		return null;
	}
	
	private static boolean isBagComprehensionVariable(FunctionRuleTermNode frNode) {
		for (BagCompNode bagCompNode = getParentBagCompNode(frNode); bagCompNode != null; bagCompNode = getParentBagCompNode(bagCompNode)) {
			try {
				if (bagCompNode.getVarBindings().containsKey(frNode.getName()))
					return true;
			} catch (EngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private static BagCompNode getParentBagCompNode(ASTNode node) {
		ASTNode bagCompNode = node.getParent();
		while (bagCompNode != null && !(bagCompNode instanceof BagCompNode))
			bagCompNode = bagCompNode.getParent();
		if (bagCompNode instanceof BagCompNode)
			return (BagCompNode)bagCompNode;
		return null;
	}
	
	private static boolean isListComprehensionVariable(FunctionRuleTermNode frNode) {
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
	
	private static ListCompNode getParentListCompNode(ASTNode node) {
		ASTNode listCompNode = node.getParent();
		while (listCompNode != null && !(listCompNode instanceof ListCompNode))
			listCompNode = listCompNode.getParent();
		if (listCompNode instanceof ListCompNode)
			return (ListCompNode)listCompNode;
		return null;
	}
	
	private static boolean isImportRuleVariable(FunctionRuleTermNode frNode) {
		for (ASTNode importRuleNode = getParentImportRuleNode(frNode); importRuleNode != null; importRuleNode = getParentImportRuleNode(importRuleNode)) {
			if (importRuleNode.getFirst().getToken().equals(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private static ASTNode getParentImportRuleNode(ASTNode node) {
		ASTNode importRuleNode = node.getParent();
		while (importRuleNode != null && !"ImportRule".equals(importRuleNode.getGrammarRule()))
			importRuleNode = importRuleNode.getParent();
		if (importRuleNode != null && "ImportRule".equals(importRuleNode.getGrammarRule()))
			return importRuleNode;
		return null;
	}
}
