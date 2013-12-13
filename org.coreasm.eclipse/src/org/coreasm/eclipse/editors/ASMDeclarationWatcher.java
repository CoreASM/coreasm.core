package org.coreasm.eclipse.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.coreasm.eclipse.editors.ASMParser.ParsingResult;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.plugins.signature.DerivedFunctionNode;
import org.coreasm.engine.plugins.signature.EnumerationElement;
import org.coreasm.engine.plugins.signature.EnumerationNode;
import org.coreasm.engine.plugins.signature.FunctionNode;
import org.coreasm.engine.plugins.signature.UniverseNode;
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
		
		protected Declaration(String name) {
			if (name == null)
				throw new IllegalArgumentException("Name must not be null!");
			this.name = name;
		}

		public String getName() {
			return name;
		}
		
		public static Declaration decode(String declaration) {
			String type = declaration.substring(0, declaration.indexOf(':'));
			declaration = declaration.substring(type.length() + 2);
			if ("Function".equals(type))
				return new FunctionDeclaration(declaration);
			else if ("Universe".equals(type))
				return new UniverseDeclaration(declaration);
			else if ("Enumeration".equals(type))
				return new EnumerationDeclaration(declaration);
			else if ("Derived Function".equals(type))
				return new DerivedFunctionDeclaration(declaration);
			else if ("Rule".equals(type))
				return new RuleDeclaration(declaration);
			return null;
		}
	}
	public static class FunctionDeclaration extends Declaration {
		private final String[] domain;
		private final String range;
		private String comment;
		
		private FunctionDeclaration(FunctionNode node, String comment) {
			super(node.getName());
			this.domain = node.getDomain().toArray(new String[node.getDomain().size()]);
			this.range = node.getRange();
			this.comment = comment;
		}
		
		private FunctionDeclaration(String declaration) {
			super(declaration.substring(0, declaration.indexOf(':')));
			int index = declaration.indexOf("->");
			String declarationDomain = declaration.substring(name.length() + 3, index - 2);
			String[] domain = declarationDomain.split(", ");
			if (domain[0].length() > 0)
				this.domain = domain;
			else
				this.domain = new String[0];
			int indexOfNewLine = declaration.indexOf('\n');
			if (indexOfNewLine >= 0) {
				comment = declaration.substring(indexOfNewLine + 2);
				range = declaration.substring(index + 3, indexOfNewLine);
			}
			else
				range = declaration.substring(index + 3);
		}
		
		public String[] getDomain() {
			return domain;
		}

		public String getRange() {
			return range;
		}
		
		@Override
		public String toString() {
			return "Function: " + name + ": " + Arrays.toString(domain) + " -> " + range + (comment != null ? "\n\n" + comment : "");
		}
	}
	public static class UniverseDeclaration extends Declaration {
		public static class Member extends Declaration {
			private UniverseDeclaration parent;
			
			private Member(UniverseDeclaration parent, String name) {
				super(name);
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
			super(node.getName());
			for (ASTNode member = node.getFirst().getNext(); member != null; member = member.getNext())
				members.add(new Member(this, member.getToken()));
			this.comment = comment;
		}
		
		private UniverseDeclaration(String declaration) {
			super(declaration.substring(0, declaration.indexOf('=') - 1));
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
				super(name);
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
			super(node.getName());
			for (EnumerationElement member : node.getMembers())
				members.add(new Member(this, member.getName()));
			this.comment = comment;
		}
		
		private EnumerationDeclaration(String declaration) {
			super(declaration.substring(0, declaration.indexOf('=') - 1));
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
		
		private DerivedFunctionDeclaration(DerivedFunctionNode node, String comment) {
			super(node.getNameSignatureNode().getFirst().getToken());
			for (ASTNode param = node.getNameSignatureNode().getFirst().getNext(); param != null; param = param.getNext()) 
				params.add(param.getToken());
			this.comment = comment;
		}
		
		private DerivedFunctionDeclaration(String declaration) {
			super(findName(declaration));
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
			String declaration = "Derived Function: " + name + "(";
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
	public static class RuleDeclaration extends Declaration {
		private List<String> params = new ArrayList<String>();
		private String comment;
		
		private RuleDeclaration(ASTNode node, String comment) {
			super(node.getFirst().getFirst().getToken());
			if (!Kernel.GR_RULEDECLARATION.equals(node.getGrammarRule()))
				throw new IllegalArgumentException("Illegal GrammarRule: " + node.getGrammarRule());
			for (ASTNode param = node.getFirst().getFirst().getNext(); param != null; param = param.getNext())
				params.add(param.getToken());
			this.comment = comment;
		}
		
		private RuleDeclaration(String declaration) {
			super(findName(declaration));
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
	
	public static List<Declaration> getDeclarations(IFile file, boolean includedDeclarations) {
		List<Declaration> declarations = new ArrayList<Declaration>();
		collectDeclarations(file, includedDeclarations, declarations);
		return declarations;
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
							Declaration decodedDeclaration = Declaration.decode(declaration);
							if (decodedDeclaration != null)
								declarations.add(decodedDeclaration);
						}
					}
				}
				if (includedDeclarations) {
					for (IFile includedFile : ASMIncludeWatcher.getIncludedFiles(file, true))
						collectDeclarations(includedFile, true, declarations);
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<Declaration> getDeclarations(ASMDocument document) {
		List<Declaration> declarations = new ArrayList<Declaration>();
		for (ASTNode node = ((ASTNode)document.getRootnode()).getFirst(); node != null; node = node.getNext()) {
			if (ASTNode.DECLARATION_CLASS.equals(node.getGrammarClass())) {
				if ("Signature".equals(node.getGrammarRule())) {
					for (ASTNode signature = node.getFirst(); signature != null; signature = signature.getNext()) {
						if (signature instanceof EnumerationNode) {
							EnumerationDeclaration enumeration = new EnumerationDeclaration((EnumerationNode)signature, parseComment(document, node));
							declarations.add(enumeration);
							for (EnumerationDeclaration.Member member : enumeration.getMembers())
								declarations.add(member);
						}
						else if (signature instanceof FunctionNode)
							declarations.add(new FunctionDeclaration((FunctionNode)signature, parseComment(document, node)));
						else if (signature instanceof UniverseNode) {
							UniverseDeclaration universe = new UniverseDeclaration((UniverseNode)signature, parseComment(document, node));
							declarations.add(universe);
							for (UniverseDeclaration.Member member : universe.getMembers())
								declarations.add(member);
						}
						else if (signature instanceof DerivedFunctionNode)
							declarations.add(new DerivedFunctionDeclaration(((DerivedFunctionNode)signature), parseComment(document, node)));
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
}
