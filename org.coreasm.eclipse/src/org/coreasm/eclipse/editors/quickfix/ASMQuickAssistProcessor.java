package org.coreasm.eclipse.editors.quickfix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.coreasm.eclipse.editors.ASMDeclarationWatcher;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.Declaration;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.FunctionDeclaration;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.RuleDeclaration;
import org.coreasm.eclipse.editors.SlimEngine;
import org.coreasm.eclipse.editors.errors.AbstractError;
import org.coreasm.eclipse.editors.errors.AbstractQuickFix;
import org.coreasm.eclipse.editors.errors.PluginErrorRecognizer;
import org.coreasm.eclipse.editors.errors.RuleErrorRecognizer;
import org.coreasm.eclipse.editors.errors.SimpleError;
import org.coreasm.eclipse.editors.errors.SyntaxError;
import org.coreasm.eclipse.editors.quickfix.proposals.CreateFunctionProposal;
import org.coreasm.eclipse.editors.quickfix.proposals.CreateRuleProposal;
import org.coreasm.eclipse.editors.quickfix.proposals.CreateUniverseProposal;
import org.coreasm.eclipse.editors.quickfix.proposals.MarkAsLocalProposal;
import org.coreasm.eclipse.editors.quickfix.proposals.UsePluginProposal;
import org.coreasm.eclipse.util.IconManager;
import org.coreasm.engine.Specification.FunctionInfo;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.TextInvocationContext;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * The ASMQuickAssistProcessor handles quick fixes.
 * @author Michael Stegmaier
 *
 */
public class ASMQuickAssistProcessor implements IQuickAssistProcessor {
	
	private static HashSet<FunctionInfo> pluginFunctions = null;

	@Override
	public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
		return false;
	}

	@Override
	public boolean canFix(Annotation annotation) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		collectProposals(annotation, proposals);
		return !proposals.isEmpty();
	}
	
	@Override
	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		ISourceViewer viewer = invocationContext.getSourceViewer();
		TextInvocationContext context = new TextInvocationContext(viewer, invocationContext.getOffset(), (viewer != null ? viewer.getSelectedRange().y : 0));
		IAnnotationModel model = viewer.getAnnotationModel();
		
		if (model == null)
			return null;
		
		List<ICompletionProposal> proposals = computeProposals(context, model);
		
		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}
	
	@SuppressWarnings("unchecked")
	private List<ICompletionProposal> computeProposals(IQuickAssistInvocationContext context, IAnnotationModel model) {
		ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		Iterator<Annotation> iterator = model.getAnnotationIterator();
		while (iterator.hasNext()) {
			Annotation annotation = iterator.next();
			if (canFix(annotation)) {
				if (isAtPosition(context.getOffset(), model.getPosition(annotation)))
					collectProposals(annotation, proposals);
			}
		}
		return proposals;
	}
	
	private boolean isAtPosition(int offset, Position pos) {
		return pos != null && offset >= pos.getOffset() && offset <= pos.getOffset() + pos.getLength();
	}
	
	public static void collectProposals(Annotation annotation, List<ICompletionProposal> proposals) {
		if (annotation instanceof MarkerAnnotation)
			collectProposals(((MarkerAnnotation)annotation).getMarker(), proposals);
	}
	
	public static void collectProposals(IMarker marker, List<ICompletionProposal> proposals) {
		if (MarkerUtilities.getSeverity(marker) == IMarker.SEVERITY_WARNING) {
			String[] data = marker.getAttribute("data", "").split(" ");
			int start = MarkerUtilities.getCharStart(marker);
			int end = MarkerUtilities.getCharEnd(marker);
			if ("UndefinedIdentifier".equals(data[0])) {
				String undefinedIdentifier = data[1];
				int arguments = Integer.parseInt(data[2]);
				for (Declaration declaration : ASMDeclarationWatcher.getDeclarations((IFile)marker.getResource(), true)) {
					if (isSimilar(undefinedIdentifier, declaration.getName())) {
						if (declaration instanceof RuleDeclaration) {
							RuleDeclaration ruleDeclaration = (RuleDeclaration)declaration;
							if (ruleDeclaration.getParams().size() > 0)
								proposals.add(new CompletionProposal(declaration.getName() + "()", start, end - start, declaration.getName().length() + 1, IconManager.getIcon("/icons/editor/bullet.gif"), "Replace with '" + declaration.getName() + "()'", null, null));
							else
								proposals.add(new CompletionProposal(declaration.getName(), start, end - start, declaration.getName().length(), IconManager.getIcon("/icons/editor/bullet.gif"), "Replace with '" + declaration.getName() + "'", null, null));
						}
						else if (declaration instanceof FunctionDeclaration) {
							FunctionDeclaration functionDeclaration = (FunctionDeclaration)declaration;
							if (functionDeclaration.getDomain().size() > 0)
								proposals.add(new CompletionProposal(declaration.getName() + "()", start, end - start, declaration.getName().length() + 1, IconManager.getIcon("/icons/editor/bullet.gif"), "Replace with '" + declaration.getName() + "()'", null, null));
							else
								proposals.add(new CompletionProposal(declaration.getName(), start, end - start, declaration.getName().length(), IconManager.getIcon("/icons/editor/bullet.gif"), "Replace with '" + declaration.getName() + "'", null, null));
						}
						else
							proposals.add(new CompletionProposal(declaration.getName(), start, end - start, declaration.getName().length(), IconManager.getIcon("/icons/editor/bullet.gif"), "Replace with '" + declaration.getName() + "'", null, null));
					}
				}
				for (FunctionInfo function : getPluginFunctions()) {
					if (isSimilar(undefinedIdentifier, function.name)) {
						if (function.name.equals(undefinedIdentifier))
							proposals.add(new UsePluginProposal(function.plugin, IconManager.getIcon("/icons/editor/package.gif")));
						else
							proposals.add(new CompletionProposal(function.name, start, end - start, function.name.length(), IconManager.getIcon("/icons/editor/bullet.gif"), "Replace with '" + function.name + "'", null, null));
					}
				}
				proposals.add(new MarkAsLocalProposal(undefinedIdentifier, start, IconManager.getIcon("/icons/editor/bullet.gif")));
				if (arguments == 0)
					proposals.add(new CreateFunctionProposal(undefinedIdentifier, Collections.<String>emptyList(), IconManager.getIcon("/icons/editor/bullet.gif")));
				else
					proposals.add(new CreateFunctionProposal(undefinedIdentifier, null, IconManager.getIcon("/icons/editor/bullet.gif")));
				if (arguments <= 1)
					proposals.add(new CreateUniverseProposal(undefinedIdentifier, IconManager.getIcon("/icons/editor/bullet.gif")));
				proposals.add(new CreateRuleProposal(undefinedIdentifier, arguments, IconManager.getIcon("/icons/editor/bullet.gif")));
			}
			else if ("NumberOfAgruments".equals(data[0])) {
				String params = "";
				for (int i = 3; i < data.length; i++) {
					if (!params.isEmpty())
						params += ", ";
					params += data[i];
				}
				proposals.add(new CompletionProposal(data[1] + "(" + params + ")", start, end - start, data[1].length() + 1, IconManager.getIcon("/icons/editor/bullet.gif"), "Replace with '" + data[1] + "(" + params + ")'", null, null));
			}
		}
		else {
			AbstractError error = AbstractError.createFromMarker(marker);
			if (error instanceof SimpleError) {
				SimpleError simpleError = (SimpleError)error;
				if (RuleErrorRecognizer.NOT_A_RULE_NAME.equals(simpleError.getErrorID())) {
					try {
						String undefinedRule = error.getDocument().get(error.getPosition(), error.getLength());
						for (Plugin plugin : SlimEngine.getFullEngine().getPlugins()) {
							if (!Kernel.PLUGIN_NAME.equals(plugin.getName()) && plugin instanceof ParserPlugin) {
								ParserPlugin parserPlugin = (ParserPlugin)plugin;
								for (String keyword : parserPlugin.getKeywords()) {
									if (isSimilar(undefinedRule, keyword)) {
										proposals.add(new UsePluginProposal(plugin.getName(), IconManager.getIcon("/icons/editor/package.gif")));
										break;
									}
								}
							}
						}
						for (Declaration declaration : ASMDeclarationWatcher.getDeclarations((IFile)marker.getResource(), true)) {
							if (declaration instanceof RuleDeclaration) {
								if (isSimilar(undefinedRule, declaration.getName())) {
									RuleDeclaration ruleDeclaration = (RuleDeclaration)declaration;
									if (ruleDeclaration.getParams().size() > 0)
										proposals.add(new CompletionProposal(declaration.getName() + "()", error.getPosition(), error.getLength(), declaration.getName().length() + 1, IconManager.getIcon("/icons/editor/bullet.gif"), "Replace with '" + declaration.getName() + "()'", null, null));
									else
										proposals.add(new CompletionProposal(declaration.getName(), error.getPosition(), error.getLength(), declaration.getName().length(), IconManager.getIcon("/icons/editor/bullet.gif"), "Replace with '" + declaration.getName() + "'", null, null));
								}
							}
						}
						proposals.add(new CreateRuleProposal(undefinedRule, 0, IconManager.getIcon("/icons/editor/bullet.gif")));
					} catch (BadLocationException e) {
					}
				}
				else if (RuleErrorRecognizer.NUMBER_OF_ARGUMENTS_DOES_NOT_MATCH.equals(simpleError.getErrorID()))
					proposals.add(new CompletionProposal(error.get("RuleName") + "(" + error.get("Params") + ")", error.getPosition(), error.getLength(), error.get("RuleName").length() + 1, IconManager.getIcon("/icons/editor/bullet.gif"), "Replace with '" + error.get("RuleName") + "(" + error.get("Params") + ")'", null, null));
				else if (PluginErrorRecognizer.NO_PLUGIN.equals(simpleError.getErrorID())) {
					try {
						String undefinedPluginName = error.getDocument().get(error.getPosition(), error.getLength());
						int indexOfPluginSuffix = undefinedPluginName.indexOf("Plugin");
						if (indexOfPluginSuffix >= 0)
							undefinedPluginName = undefinedPluginName.substring(0, indexOfPluginSuffix);
						for (Plugin plugin : SlimEngine.getFullEngine().getPlugins()) {
							if (Kernel.PLUGIN_NAME.equals(plugin.getName()))
								continue;
							String pluginName = plugin.getName().substring(0, plugin.getName().indexOf("Plugin"));
							if (isSimilar(undefinedPluginName, pluginName))
								proposals.add(new CompletionProposal(pluginName, error.getPosition(), error.getLength(), pluginName.length(), IconManager.getIcon("/icons/editor/bullet.gif"), "Replace with '" + pluginName + "'", null, null));
						}
					} catch (BadLocationException e) {
					}
				}
			}
			else if (error instanceof SyntaxError) {
				SyntaxError syntaxError = (SyntaxError)error;
				for (Plugin plugin : SlimEngine.getFullEngine().getPlugins()) {
					if (!Kernel.PLUGIN_NAME.equals(plugin.getName()) && plugin instanceof ParserPlugin) {
						ParserPlugin parserPlugin = (ParserPlugin)plugin;
						for (String keyword : parserPlugin.getKeywords()) {
							if (isSimilar(syntaxError.getEncountered(), keyword)) {
								proposals.add(new UsePluginProposal(plugin.getName(), IconManager.getIcon("/icons/editor/package.gif")));
								break;
							}
						}
					}
				}
			}
			if (error != null) {
				for (AbstractQuickFix fix : error.getQuickFixes())
					fix.collectProposals(error, proposals);
			}
		}
	}
	
	private static HashSet<FunctionInfo> getPluginFunctions() {
		if (pluginFunctions == null) {
			pluginFunctions = new HashSet<FunctionInfo>();
			pluginFunctions.addAll(SlimEngine.getFullEngine().getSpec().getDefinedFunctions());
			pluginFunctions.addAll(SlimEngine.getFullEngine().getSpec().getDefinedUniverses());
			pluginFunctions.addAll(SlimEngine.getFullEngine().getSpec().getDefinedBackgrounds());
		}
		return pluginFunctions;
	}
	
	/**
	 * Returns whether two strings are similar.
	 * @param string first string
	 * @param anotherString another string
	 * @return whether two strings are similar
	 */
	private static boolean isSimilar(String string, String anotherString) {
		String shortString = string.toLowerCase();
		String longString = anotherString.toLowerCase();
		
		if (shortString.length() > longString.length()) {
			String tmp = longString;
			longString = shortString;
			shortString = tmp;
		}
		
		int lengthShortString = shortString.length();
		int lengthLongString = longString.length();
		int lengthDiff = lengthLongString - lengthShortString;
		int min = lengthLongString;
		
		for (int i = 0; i <= lengthDiff; i++) {
			int sum = lengthDiff;
			for (int j = 0; j < lengthShortString; j++) {
				if (longString.charAt(i + j) != shortString.charAt(j)) {
					sum += 2;
					if (sum >= min)
						break;
				}
			}
			if (sum < min) {
				min = sum;
				if (min < lengthShortString)
					return true;
			}
		}
		int sum = lengthDiff;
		for (int i = 0; i < lengthShortString / 2; i++) {
			if (longString.charAt(i) != shortString.charAt(i))
				sum += 2;
			if (longString.charAt(lengthLongString - i - 1) != shortString.charAt(lengthShortString - i - 1))
				sum += 2;
			if (sum >= min)
				break;
		}
		if (sum < min) {
			min = sum;
			if (min < lengthShortString)
				return true;
		}
		
		return false;
	}

	@Override
	public String getErrorMessage() {
		// TODO add implementation for getErrorMessage()
		return null;
	}

}
