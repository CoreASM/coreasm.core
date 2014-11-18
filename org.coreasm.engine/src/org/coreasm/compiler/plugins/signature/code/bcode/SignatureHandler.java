package org.coreasm.compiler.plugins.signature.code.bcode;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.compiler.plugins.signature.CompilerSignaturePlugin;
import org.coreasm.compiler.plugins.signature.DerivedFunctionEntry;
import org.coreasm.compiler.plugins.signature.EnumBackgroundEntry;
import org.coreasm.compiler.plugins.signature.FunctionEntry;
import org.coreasm.compiler.plugins.signature.UniverseEntry;
import org.coreasm.compiler.plugins.signature.CompilerSignaturePlugin.SignatureEntryType;
import org.coreasm.engine.interpreter.ASTNode;

public class SignatureHandler implements CompilerCodeHandler {
	private CompilerSignaturePlugin parent;

	public SignatureHandler(CompilerSignaturePlugin parent) {
		this.parent = parent;
	}

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		ASTNode root = node.getAbstractChildNodes().get(0);
		if (root.getGrammarRule().equals("UniverseDefinition")) {
			parseUniverse(root);
			return;
		} else if (root.getGrammarRule().equals("EnumerationDefinition")) {
			parseEnum(root);
			return;
		} else if (root.getGrammarRule().equals("FunctionSignature")) {
			parseFunction(root, engine);
			return;
		} else if (root.getGrammarRule().equals("DerivedFunctionDeclaration")) {
			parseDerivedFunction(root, engine);
			return;
		}

	}

	private void parseUniverse(ASTNode node) {
		String name = node.getAbstractChildNodes().get(0).getToken();
		String[] elements = new String[node.getAbstractChildNodes().size() - 1];
		for (int i = 1; i < node.getAbstractChildNodes().size(); i++) {
			elements[i - 1] = node.getAbstractChildNodes().get(i).getToken();
		}

		parent.addEntry(name, parent.new IncludeEntry(
				SignatureEntryType.UNIVERSE, new UniverseEntry(name, elements)));
		// universes.put(name, new UniverseEntry(name, elements));
	}

	private void parseEnum(ASTNode node) {
		String name = node.getAbstractChildNodes().get(0).getToken();
		String[] elements = new String[node.getAbstractChildNodes().size() - 1];
		for (int i = 1; i < node.getAbstractChildNodes().size(); i++) {
			elements[i - 1] = node.getAbstractChildNodes().get(i).getToken();
		}

		parent.addEntry(name, parent.new IncludeEntry(SignatureEntryType.ENUM,
				new EnumBackgroundEntry(name, elements)));
		// enums.put(name, new EnumBackgroundEntry(name, elements));
	}

	private void parseFunction(ASTNode node, CompilerEngine engine) throws CompilerException{
		// first node is either the function id or the function class
		String name = null;
		String fclass = null;
		int pos = 0;
		List<ASTNode> children = node.getAbstractChildNodes();
		if (children.get(0).getGrammarRule().equals("ID")) {
			name = children.get(0).getToken();
			pos = 1;
		} else {
			fclass = children.get(0).getToken();
			String tmp = fclass.substring(0, 1);
			fclass = tmp.toUpperCase() + fclass.substring(1);
			name = children.get(1).getToken();
			pos = 2;
		}

		// next node holds the lefthand side
		List<String> domain = new ArrayList<String>();
		if (children.get(pos).getGrammarRule().equals("UniverseTuple")) {
			for (ASTNode n : children.get(pos).getAbstractChildNodes()) {
				domain.add(n.getToken());
			}
			pos++;
		}
		// righthand side
		String range = children.get(pos).getToken();
		CodeFragment init = null;
		if (children.size() > pos + 1) {
			init = engine.compile(children.get(pos + 1), CodeType.R);
		}

		// add the function element
		parent.addEntry(name, parent.new IncludeEntry(
				SignatureEntryType.FUNCTION, new FunctionEntry(name, fclass,
						domain, range, init)));
		// functions.put(name, new FunctionEntry(name, fclass, domain, range,
		// init));
	}
	
	private void parseDerivedFunction(ASTNode node, CompilerEngine engine) throws CompilerException{
		ASTNode signature = node.getAbstractChildNodes().get(0);
		
		CodeFragment body = null;
		try{
			body = engine.compile(node.getAbstractChildNodes().get(1), CodeType.R);
		}
		catch(Exception e){
			CodeFragment c = engine.compile(node.getAbstractChildNodes().get(1), CodeType.U);
		
			body = new CodeFragment("");
			body.appendFragment(c);
			body.appendLine("@decl(Object,res)=CompilerRuntime.Element.UNDEF;\n");
			body.appendLine("@decl(CompilerRuntime.UpdateList, ulist) = (CompilerRuntime.UpdateList) evalStack.pop();\n");
			body.appendLine("for(@decl(int,i)=0; @i@ < @ulist@.size(); @i@++){\n");
			body.appendLine("if(@ulist@.get(@i@).loc.name.equals(\"result\")){\n");
			body.appendLine("@res@=@ulist@.get(@i@).value;\n");
			body.appendLine("break;\n");
			body.appendLine("}\n");
			body.appendLine("}\n");
			body.appendLine("evalStack.push(@res@);\n");
		}
		
		
		String name = signature.getAbstractChildNodes().get(0).getToken();
		String[] params = new String[signature.getAbstractChildNodes().size() - 1];
		for(int i = 1; i < signature.getAbstractChildNodes().size(); i++){
			params[i - 1] = signature.getAbstractChildNodes().get(i).getToken();
		}
		
		parent.addEntry(name, parent.new IncludeEntry(SignatureEntryType.DERIVED, new DerivedFunctionEntry(name, params, body)));
		//derived.put(name, new DerivedFunctionEntry(name, params, body));
	}
}
