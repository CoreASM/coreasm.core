/*
 * JasminePlugin.java 		$Revision: 130 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified on $Date: 2010-03-31 01:27:47 +0200 (Mi, 31 Mrz 2010) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.jasmine.plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Token;
import org.jparsec.Tokens;
import org.jparsec.Tokens.Fragment;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.EngineError;
import org.coreasm.engine.Specification;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.InvalidLocationException;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.PluginAggregationAPI;
import org.coreasm.engine.absstorage.PluginCompositionAPI;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.OperatorRule;
import org.coreasm.engine.parser.OperatorRule.OpType;
import org.coreasm.engine.parser.ParseMap;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.Aggregator;
import org.coreasm.engine.plugin.ExtensionPointPlugin;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.coreasm.engine.plugins.string.StringElement;
import org.coreasm.jasmine.plugin.JasmineUpdateElement.Type;
import org.coreasm.util.HashMultiset;
import org.coreasm.util.Logger;
import org.coreasm.util.Multiset;

/**
 * 
 * The JASMine Plug-in provides access to Java objects from CoreASM specifications. 
 *   
 * @author Roozbeh Farahbod
 *
 */

public class JasminePlugin extends Plugin implements ParserPlugin,
		InterpreterPlugin, Aggregator, VocabularyExtender, OperatorProvider, ExtensionPointPlugin {

	/** plugin name */
	public static final String PLUGIN_NAME = JasminePlugin.class.getSimpleName();
	
	/** version info */
	public static final VersionInfo version = new VersionInfo(1, 1, 5, "beta");
	
	/** JASMine update action */
	public static final String JASMINE_UPDATE_ACTION = "JASMineUpdate";
	
	/** location of ('jasmChannel', []) */
	public final Location channelLocation;
	
	/** Two conversion modes of the plug-in */
	public static enum ConversionMode {explicitConversion, implicitConversion};
	
	/**
	 * The name of the JASMine.ConversionMode property. The value of this property can
	 * be either "implicit", "explicit", or "default". The default value is "implicit". 
	 */
	public static final String CONVERSION_MODE_PROPERTY = "ConversionMode";
	
	private HashMap<String, GrammarRule> parsers = null;

	private Set<String> dependencies = null;
	private Map<String, FunctionElement> functions = null;

	private Collection<OperatorRule> operatorRules;

	private static final String FIELD_ACCESS_OPERATOR = "->";
	private static final String[] operators = {".", FIELD_ACCESS_OPERATOR}; //, "->"};
	private static final String[] keywords = {"import", "native", "into", 
											  "store", "invoke", "result"};

	private static final String[] UPDATE_ACTIONS = {JASMINE_UPDATE_ACTION};
	
	public static final String JASMINE_CLASSPATH__SYSTEM = "JASMINE_CLASSPATH";
	public static final String JASMINE_CLASSPATH__ENGINE = "JASMine.ClassPath";
	
	private static final Set<String> options = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] { CONVERSION_MODE_PROPERTY, JASMINE_CLASSPATH__ENGINE })));
	
	private ClassLoader loader = null;
	private boolean classPathUpdatedThroughOptions = false;

	private Parser<Node> basicJavaIdParser = null;
	
	/* Temporary! begins */
	Map<EngineMode, Integer> targetModes = null;
    /* Temporary! ends */

	public JasminePlugin() {
		channelLocation = new Location("jasmChannel", ElementList.NO_ARGUMENT);
	}
	
	/**
	 * Returns the current conversion mode of the plug-in.
	 * 
	 * @see ConversionMode
	 */
	public ConversionMode getConversionMode() {
		String mode = getOptionValue(CONVERSION_MODE_PROPERTY);
		if (mode == null)
			mode = "implicit";
		if (mode.equals("explicit"))
			return ConversionMode.explicitConversion;
		else
			return ConversionMode.implicitConversion;
	}
	
	/**
	 * @return <code>true</code> if this JASMine plug-in is in 
	 * the implicit conversion mode. 
	 * 
	 *  @see #getConversionMode()
	 */
	public boolean isImplicitConversionMode() {
		return getConversionMode().equals(ConversionMode.implicitConversion);
	}
	
	/**
	 * @return <code>true</code> if this JASMine plug-in is in 
	 * the explicit conversion mode. 
	 * 
	 *  @see #getConversionMode()
	 */
	public boolean isExplicitConversionMode() {
		return getConversionMode().equals(ConversionMode.explicitConversion);
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Plugin#initialize()
	 */
	@Override
	public void initialize() {
		/*
		if (capi.getClassLoader() != null)
			this.loader = capi.getClassLoader();
		else
			this.loader = this.getClass().getClassLoader();
		*/
		this.loader = this.getClass().getClassLoader();
		
		updateClassPath(System.getenv(JASMINE_CLASSPATH__SYSTEM));
		//updateClassPath(getOptionValue(JASMINE_CLASSPATH__ENGINE));
		classPathUpdatedThroughOptions = false;
	}

	/*
	 * adds the additional class paths to JASMine's classpath.
	 */
	private void updateClassPath(String classPath) {
		if (classPath != null) {
			if (classPath.endsWith("\"") && classPath.startsWith("\"") && classPath.length() > 2)
				classPath = classPath.substring(1, classPath.length() - 1);
			List<URL> urls = new ArrayList<URL>();
			URL url = null;
			StringTokenizer tokenizer = new StringTokenizer(classPath, ":");
			
			Specification spec = capi.getSpec();
			String specDir = spec.getFileDir();

			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (!token.toLowerCase().endsWith(".jar") && !token.endsWith("/"))
					token = token + "/";
				
				// Resolve relative path names using the specification's directory
				if (specDir != null) {
					File f = new File(token);
					if (!f.isAbsolute())
						token = specDir + File.separator + token; 
				}
				
				try {
					url = new URL("file://" + token);
					Logger.log(Logger.INFORMATION, Logger.plugins,
							"JASMine plugin adds '" + token + "' to its classpath.");
				} catch (MalformedURLException e1) {
					Logger.log(Logger.WARNING, Logger.plugins,
							"JASMine plugin ignores '" + token + "' from its classpath.");
					continue;
				}
				urls.add(url);
			}
			URL[] urlArray = new URL[0];
			if (this.loader != null)
				this.loader = new URLClassLoader(urls.toArray(urlArray), this.loader);
			else
				this.loader = new URLClassLoader(urls.toArray(urlArray), this.getClass().getClassLoader());
		}
	}
	
	public ClassLoader getClassLoader(){
		return this.loader;
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getKeywords()
	 */
	public String[] getKeywords() {
		return keywords;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getOperators()
	 */
	public String[] getOperators() {
		return operators;
	}
	
	@Override
	public Set<String> getOptions() {
		return options;
	}

	private Parser<Node> getBasicJavaIdParser() {
		if (basicJavaIdParser == null)
		{
			ParserTools.getInstance(capi);
			Parser<Token> tokp = Parsers.ANY_TOKEN.token();
			
			basicJavaIdParser = tokp.map(from -> {
                if (from.value() instanceof Fragment) {
                    Fragment frag = (Fragment) from.value();
                    if (frag.tag() == Tokens.Tag.IDENTIFIER || frag.tag() == Tokens.Tag.RESERVED) {
                        return new ASTNode(
                            "Jasemine",
                            ASTNode.ID_CLASS,
                            "BasicJavaID",
                            from.toString(),
                            new ScannerInfo(from),
                            Node.OTHER_NODE
                        );
                    } // else...
                } // else...
                return null;
            });
		}
		
		return basicJavaIdParser;
	}
	
	/*private Parser<Node> getBasicJavaIdParser() {
		if (basicJavaIdParser == null) {
			basicJavaIdParser = Parsers.token(new FromToken<Node>() {

				public Node fromToken(Tok tok) {
					Object token = tok.getToken();
					if (token instanceof TypedToken) {
						if (((TypedToken)token).getType().equals(TokenType.Word)
								|| ((TypedToken)token).getType().equals(TokenType.Reserved)) 
							return new ASTNode(
									"Jasmine", 
									ASTNode.ID_CLASS, 
									"BasicJavaID", 
									tok.toString(),
									new ScannerInfo(tok),
									Node.OTHER_NODE
									);
						else
							return null;
					} else
						return null;
				}
				
			});
			
		}
		return basicJavaIdParser;
	}*/

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getParsers()
	 */
	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel")
					.getPluginInterface();

			Parser<Node> termParser = kernel.getTermParser();

			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> idParser = pTools.getIdParser();
			Parser<Node> tupleTermParser = kernel.getTupleTermParser();
			Parser<Node> basicExprParser = kernel.getBasicExprParser();
			
			// TODO uncomment this line and change the rest of the code accordingly when the used feature becomes available
			//Parser<Node> tupleTermParser = kernel.getBasicExprParser();
						
			Parser<Object[]> repeated =
					pTools.many(
							pTools.seq(
									pTools.getOprParser("."),
									idParser
							)
					);
			
			Parser<Node> javaIdParser = 
					pTools.seq("JavaIdParser", getBasicJavaIdParser(), repeated).map(
					new JavaIdParseMap());
			
			Parser<Node> importRuleParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser("import", PLUGIN_NAME),
						pTools.getKeywParser("native", PLUGIN_NAME),
						javaIdParser,
						tupleTermParser.optional(null),
						pTools.getKeywParser("into", PLUGIN_NAME),
						termParser
					}).map(
					new NativeImportParseMap());
			
			parsers.put(importRuleParser.toString(),
					new GrammarRule(importRuleParser.toString(),
							"'import' 'native' FunctionRuleTerm 'into' Term", importRuleParser, PLUGIN_NAME));

			Parser<Node> storeRuleParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser("store", PLUGIN_NAME),
						termParser,
						pTools.getKeywParser("into", PLUGIN_NAME),
						basicExprParser,
						pTools.getOprParser("->"),
						basicJavaIdParser
					}).map(
					new StoreParseMap());
			
			parsers.put(storeRuleParser.toString(),
					new GrammarRule(storeRuleParser.toString(),
							"'store' Term 'into' Term '->' ID", storeRuleParser, PLUGIN_NAME));

			Parser<Node> invokeRuleParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser("invoke", PLUGIN_NAME),
						basicExprParser,
						pTools.getOprParser("->"),
						basicJavaIdParser,
						tupleTermParser,
						pTools.seq(
								pTools.getKeywParser("result", PLUGIN_NAME),
								pTools.getKeywParser("into", PLUGIN_NAME),
								termParser
						).optional(null)
					}).map(
					new InvokeParseMap());
			
			parsers.put(invokeRuleParser.toString(),
					new GrammarRule(invokeRuleParser.toString(),
							"'invoke' Term '->' ID TupeTerm 'result' 'into' Term", 
							invokeRuleParser, PLUGIN_NAME));

			parsers.put("Rule", 
					new GrammarRule("Rule", "JasmineInvokeRule | JasmineStoreRule | JasmineImportRule", 
							Parsers.or(invokeRuleParser, importRuleParser, storeRuleParser), PLUGIN_NAME));
						
			/*
			Parser<Node> fieldReadParser = Parsers.mapn("JasmineFieldReadExp", 
					new Parser[] {
						pTools.getOprParser("<<<"),
						termParser,
						spaceTab,
						pTools.getOprParser("-->"),
						spaceTab,
						idParser},
					new ParseMapN<Node>(PLUGIN_NAME) {

						public Node map(Object... nodes) {
							for (Object n: nodes) 
								System.out.println(n);
							ASTNode node = new FieldReadNode(((Node)nodes[0]).getScannerInfo());
							addChildren(node, nodes);
							return node;
						}
					}
			);
			
			parsers.put(fieldReadParser.toString(),
					new GrammarRule(fieldReadParser.toString(), 
							"Term '->' ID", fieldReadParser, PLUGIN_NAME));
			parsers.put("BasicTerm", 
					new GrammarRule("JASMineBasicTerm", fieldReadParser.toString(), 
							fieldReadParser, PLUGIN_NAME));
			/**/
		}

		return parsers;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.InterpreterPlugin#interpret(org.coreasm.engine.interpreter.ASTNode)
	 */
	public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {

		if (!classPathUpdatedThroughOptions) {
			classPathUpdatedThroughOptions = true;
			updateClassPath(getOptionValue(JASMINE_CLASSPATH__ENGINE));
		}
		
		// import native ...
		if (pos instanceof NativeImportRuleNode) {
			NativeImportRuleNode node = (NativeImportRuleNode)pos;
			ASTNode location = (ASTNode)pos.getChildNode("beta");
			
			if (!location.isEvaluated())
				return location;
			
			// if location is evaluated, check to see if it has a location
			if (location.getLocation() == null) {
				capi.error("Cannot import into a non location.", location, interpreter);
				return location;
			}
			
			String x = node.getClassName().trim(); 
			
			Class<? extends Object> c = null;
			try {
				c = JasmineUtil.getJavaClass(x, this.loader);
			} catch (Exception e) {
				capi.error("Java class '" + x + "' not found.", node, interpreter);
				return node;
			}
			
			List<Node> argsNode = node.getChildNodes("lambda");
			
			// pattern: 'import' 'native' x 'into' l
			if (argsNode.size() == 0) {
				try {
					c.getConstructor();
				} catch (Exception e) {
					capi.error("Constructor not found.", node, interpreter);
					Logger.log(Logger.ERROR, Logger.plugins, e.getMessage());
					return pos;
				}
				
				evaluateImport(pos, interpreter.getSelf(), pos.getScannerInfo(), location.getLocation(), x, Collections.emptyList());

			} else {
			// pattern: 'import' 'native' x(...) 'into' l
				for (Node a: argsNode) {
					if (a instanceof ASTNode)
						if (!((ASTNode)a).isEvaluated()) 
							return (ASTNode)a;
				}
				// TODO improve the above code
				
				// every argument is evaluated now
				// get the class list
				List<Object> argsInJava = new ArrayList<Object>();
				for (Node a: argsNode)
					if (a instanceof ASTNode) {
						Element v = ((ASTNode)a).getValue();
						argsInJava.add(jValue(v));
						/*
						if (v instanceof JObjectElement)
							argsInJava.add(((JObjectElement)v).object);
						else
							argsInJava.add(v);
						*/
					}
				
				try {
					findConstructor(c, argsInJava);
				} catch (Exception e) {
					capi.error("Constructor not found.", node, interpreter);
					Logger.log(Logger.ERROR, Logger.plugins, e.getMessage());
					return pos;
				}
				
				evaluateImport(pos, interpreter.getSelf(), pos.getScannerInfo(), location.getLocation(), x, argsInJava);
				
			}
			
		}
		
		// store into ...
		else if (pos instanceof StoreRuleNode) {
			StoreRuleNode node = (StoreRuleNode)pos;
			ASTNode valueNode = node.getFirst();
			ASTNode objectNode = node.getFirst().getNext();
			String fieldName = objectNode.getNext().getToken();
			
			if (!valueNode.isEvaluated()) 
				return valueNode;
			
			if (!objectNode.isEvaluated())
				return objectNode;
			
			Element objectElement = objectNode.getValue();
			if (objectElement != null && objectElement instanceof JObjectElement) {
				JObjectElement jobj = (JObjectElement)objectElement;
				// get the field
				try {
					jobj.jType().getField(fieldName);
				} catch (Exception e) {
					capi.error("Field '" + fieldName + "' not found.", objectNode.getNext(), interpreter);
					Logger.log(Logger.ERROR, Logger.plugins, e.getMessage());
					return pos;
				}
				
				if (valueNode.getValue() == null) 
					capi.error("There is no value.", valueNode, interpreter);
				else {
					Object v = jValue(valueNode.getValue());
					/*
					Object v = valueNode.getValue();
					if (v instanceof JObjectElement)
						v = ((JObjectElement)v).object;
					*/
					Update u = createDefUpdate(Type.Store, interpreter.getSelf(), pos.getScannerInfo(), jobj, fieldName, v);
					pos.setNode(null, new UpdateMultiset(u), null);
				}
				
			} else 
				capi.error("Not a Java object.", node, interpreter);
		}
		
		// invoke v->x(...) ...
		if (pos instanceof InvokeRuleNode) {
			InvokeRuleNode node = (InvokeRuleNode) pos;
			ASTNode jnode = node.getFirst();
			
			// evaluate the object
			if (!jnode.isEvaluated()) 
				return node.getFirst();
			
			Element jobj = jnode.getValue();
			
			if (jobj != null && jobj instanceof JObjectElement) {

				List<ASTNode> argsNode = node.getAbstractChildNodes("lambda");
				ASTNode locNode = (ASTNode)node.getChildNode("gamma");
				Location loc = null;

				// evaluate the arguments
				for (ASTNode lambda: argsNode)
					if (!lambda.isEvaluated())
						return lambda;
				
				// if it's a void method invocation
				if (!node.isVoidInvocation()) {
					// evaluate the location node
					if (!locNode.isEvaluated())
						return locNode;
					
					// make sure it has a location
					if (locNode.getLocation() == null) {
						capi.error("Cannot update a non-location.", locNode, interpreter);
						return pos;
					}
					loc = locNode.getLocation();
				}
				
				Class<?> clazz = ((JObjectElement)jobj).object.getClass();
				String methodName = jnode.getNext().getToken();
				
				List<Object> argsInJava = new ArrayList<Object>();
				
				for (Node a: argsNode)
					if (a instanceof ASTNode) {
						Element v = ((ASTNode)a).getValue();
						argsInJava.add(jValue(v));
						/*
						if (v instanceof JObjectElement)
							argsInJava.add(((JObjectElement)v).object);
						else
							argsInJava.add(v);
						*/
					}
				
				try {
					findMethod(clazz, methodName, argsInJava);
				} catch (Exception e) {
					capi.error("Java method '" + methodName + "' not found.", node, interpreter);
					Logger.log(Logger.ERROR, Logger.plugins, e.getMessage());
					return pos;
				}
				
				Update u = createDefUpdate(Type.Invoke, interpreter.getSelf(), pos.getScannerInfo(), loc, jobj, methodName, argsInJava);
				pos.setNode(null, new UpdateMultiset(u), null);
			} else 
				capi.error("Not a Java object.", jnode, interpreter);
				
		}
		return pos;
	}

	/*
	 * Finds a method with the given name that matches the given list of arguments.
	 * TODO if more than one method match the arguments, it picks the first one it finds.
	 */
	private Method findMethod(Class<?> clazz, String name, List<? extends Object> arguments) throws NoSuchMethodException {
		Class<?>[] classes = new Class[arguments.size()];
		int i = 0;
		for (Object obj: arguments) {
			if (obj == null)
				classes[i] = null;
			else
				classes[i] = obj.getClass();
			i++;
		}
		
		Object[] values = arguments.toArray();

		Method[] methods = clazz.getMethods();
		for (Method m: methods) {
			if (m.getName().equals(name)) {
				Class<?>[] paramClasses = m.getParameterTypes();
				if (JasmineUtil.classesMatch(paramClasses, classes, values))
					return m;
			}
		}
		
		throw new NoSuchMethodException("No matching method found.");
	}
	
	/*
	 * Finds a constructor of clazz that matches the given arguments. 
	 * TODO if more than one constructor match the arguments, it picks the first one it finds.
	 */
	private Constructor<?> findConstructor(Class<?> clazz, List<? extends Object> arguments) throws SecurityException, NoSuchMethodException {
		// if looking for the default constructor
		if (arguments.size() == 0) 
			return clazz.getConstructor();
	
		// otherwise
		Class<?>[] classes = new Class[arguments.size()];
		int i = 0;
		for (Object obj: arguments) {
			if (obj == null)
				classes[i] = null;
			else
				classes[i] = obj.getClass();
			i++;
		}
		
		Object[] values = arguments.toArray();
		
		Constructor<?>[] constructors = clazz.getConstructors();
		for (Constructor<?> cons: constructors) {
			Class<?>[] paramClasses = cons.getParameterTypes();
			if (JasmineUtil.classesMatch(paramClasses, classes, values))
				return cons;
		}
		
		throw new NoSuchMethodException("No suitable constructor found.");
	}
	
	/*
	 * Checks whether the Classes in the subClasses array
	 * are sub-classes of those in the superClasses array.
	 * If a subclass is null, it considers it as a match to anything. 
	 *
	private boolean matchClasses(Class[] superClasses, Class[] subClasses, List<? extends Object> arguments) {
		if (superClasses.length != subClasses.length)
			return false;
		
		for (int i=0; i < superClasses.length; i++) 
			try { 
				if (subClasses[i] != null) {

					// if the value is Integer
					if (subClasses[i].equals(Integer.class))
						if (superClasses[i].equals(Integer.TYPE) || superClasses[i].equals(Integer.class)
								|| superClasses[i].equals(Long.TYPE) || superClasses[i].equals(Long.class)
								|| superClasses[i].equals(Float.TYPE) || superClasses[i].equals(Float.class)
								|| superClasses[i].equals(Double.TYPE) || superClasses[i].equals(Double.class))
							continue;
					
					// if the value is Long
					if (subClasses[i].equals(Long.class))
						if (superClasses[i].equals(Long.TYPE) || superClasses[i].equals(Long.class)
								|| superClasses[i].equals(Float.TYPE) || superClasses[i].equals(Float.class)
								|| superClasses[i].equals(Double.TYPE) || superClasses[i].equals(Double.class))
							continue;
					
					// if the value is Float
					if (subClasses[i].equals(Float.class))
						if (superClasses[i].equals(Float.TYPE) || superClasses[i].equals(Float.class)
								|| superClasses[i].equals(Double.TYPE) || superClasses[i].equals(Double.class))
							continue;
					
					// if the value is Double
					if (subClasses[i].equals(Double.class)) {
						if (superClasses[i].equals(Double.TYPE) || superClasses[i].equals(Double.class))
							continue;
						if (superClasses[i].equals(Float.TYPE) || superClasses[i].equals(Float.class)) {
							double d = ((Double)arguments.get(i)).doubleValue();
							if (d < Float.MAX_VALUE && d > Float.MIN_VALUE)
								continue;
						} 
					}
					
					subClasses[i].asSubclass(superClasses[i]);
				}
			} catch (Exception e) {
				return false;
			}
			
		return true;
	}
	*/
	
	/*
	 * @param arguments a list of Java object
	 */
	private void evaluateImport(ASTNode pos, Element self, ScannerInfo sinfo, Location l, String className, List<Object> arguments) {
		/*
		List args = new ArrayList();
		if (arguments != null) {
			// convert the values
			for (Element e: arguments) 
				args.add(toJava(e));
		}
		*/
		Update u = createDefUpdate(Type.Create, self, sinfo, l, className, arguments, self);
		pos.setNode(null, new UpdateMultiset(u), null);
	}
	
	/*
	 * Creates a deferred update.
	 */
	private Update createDefUpdate(Type type, Element self, ScannerInfo info, Object...args) {
		JasmineUpdateElement value = new JasmineUpdateElement(self, type, info, args);
		return new Update(channelLocation, value, JASMINE_UPDATE_ACTION, self, info);
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Aggregator#aggregateUpdates(org.coreasm.engine.absstorage.PluginAggregationAPI)
	 */
	public void aggregateUpdates(PluginAggregationAPI pluginAgg) {
		
		// TODO the channel should be agent-dependent
		
		UpdateMultiset channelUpdates = pluginAgg.getLocUpdates(channelLocation);
		if (channelUpdates == null)
			return;
		
		for (Update u: channelUpdates) {
			// channel should not be updated by others 
			if (!u.action.equals(JASMINE_UPDATE_ACTION)) {
				pluginAgg.handleInconsistentAggregationOnLocation(channelLocation, this);
				Logger.log(Logger.ERROR, Logger.plugins, "JASMine Plugin: JasmineChannel should not be updated by the user.");
				return;
			}
		}
		
		// The following block takes care of mid-step aggregations 
		// (e.g., aggregation at the end of TurboASM sequence steps)
		
		// FIXME I don't think this is a nice way to handle the problem
		/**/
		if (!capi.getEngineMode().equals(EngineMode.emAggregation)) {
			// ignore all JASMine updates 
			for (Update u: channelUpdates)
				pluginAgg.flagUpdate(u, PluginAggregationAPI.Flag.SUCCESSFUL, this);
			return;
		}
		/**/
		
		// Load the updates into an update explorer
		UpdateExplorer ue = new UpdateExplorer(channelUpdates);

		// A) Check for consistency

		// A-1) CREATE
		for (Entry<Location, Multiset<JasmineUpdateElement>> pair: ue.createLocations.entrySet()) {
			// No other create
			if (pair.getValue().size() > 1) {
				pluginAgg.handleInconsistentAggregationOnLocation(channelLocation, this);
				Logger.log(Logger.ERROR, Logger.plugins, "JASMine Plugin: Cannot have two import rules on the same location (" + pair.getKey() + ").");
				return;
			}
			// no invoke
			if (ue.invokeLocations.get(pair.getKey()) != null) {
				pluginAgg.handleInconsistentAggregationOnLocation(channelLocation, this);
				Logger.log(Logger.ERROR, Logger.plugins, "JASMine Plugin: Cannot have import together with store or invoke on the same location (" + pair.getKey() + ").");
				return;
			}
			// we don't need to check for conflicts with 
			// other regular updates, as if there is such 
			// a conflict, it will show up at the end when
			// the engine is checking for inconsistency
		}
		
		// A-2) STORE
		for (Entry<JObjectElement, Multiset<JasmineUpdateElement>> pair: ue.storeLocations.entrySet()) {
			
			// if multiple STOREs are performed on the same field of the
			// same object, they must all assign the same value.
			if (pair.getValue().size() > 1) {
				Map<String, Object> fieldValues = new HashMap<String, Object>();
				for (JasmineUpdateElement jue: pair.getValue()) {
					String field = jue.getStoreField();
					Object value = jue.getStoreValue();
					
					if (fieldValues.get(field) == null)
						fieldValues.put(field, value);
					else 
						if (!fieldValues.get(field).equals(value)) {
							pluginAgg.handleInconsistentAggregationOnLocation(channelLocation, this);
							Logger.log(Logger.ERROR, Logger.plugins, "JASMine Plugin: Inconsistent update to the same field.");
							return;
						}
				}
			}
		}
		
		// A-3) INVOKE
		for (Entry<Location, Multiset<JasmineUpdateElement>> pair: ue.invokeLocations.entrySet()) {
			// No other invoke on the same location
			if (pair.getValue().size() > 1 && pair.getKey() != null) {
				pluginAgg.handleInconsistentAggregationOnLocation(channelLocation, this);
				Logger.log(Logger.ERROR, Logger.plugins, "JASMine Plugin: Cannot have two invoke rules on the same location (" + pair.getKey() + ").");
				return;
			}
			
			// we don't need to check for conflicts with 
			// other regular updates, as if there is such 
			// a conflict, it will show up at the end when
			// the engine is checking for inconsistency
		}
		
		
		// B) Aggregation
		for (JasmineUpdateElement jue: ue.updates) {
			
			// B-1) CREATE
			if (jue.type == Type.Create) {
				Location l = jue.getCoreASMLocation();
				String className = (String)jue.arguments.get(1);  // the 'x' in (l, x, <...>)
				List<?> args = (List<?>)jue.arguments.get(2);  // the '<...>' in (l, x, <...>)

				// get the class object
				Class<?> c = null;
				try {
					c = JasmineUtil.getJavaClass(className, this.loader);
				} catch (ClassNotFoundException e) {
					Logger.log(Logger.ERROR, Logger.plugins, "Java class '" + className + "' not found.");
					// This should not happen at this point
					pluginAgg.handleInconsistentAggregationOnLocation(channelLocation, this);
					return;
				}
				Constructor<?> cons = null;
				Object result = null;
				
				// if there is no argument
				if (args.size() == 0) {
					try {
						cons = c.getConstructor();
					} catch (Exception e) {
						Logger.log(Logger.ERROR, Logger.plugins, e.getMessage());
						// This should not happen at this point
						pluginAgg.handleInconsistentAggregationOnLocation(channelLocation, this);
						return;
					}
					// get the instance
					try {
						result = cons.newInstance();
					} catch (Exception e) {
						Logger.log(Logger.ERROR, Logger.plugins, e.getMessage());
						pluginAgg.handleInconsistentAggregationOnLocation(channelLocation, this);
						return;
					}
					
				} else {
					// if there are arguments
					
					// get the constractor
					try {
						cons = findConstructor(c, args);
					} catch (Exception e) {
						Logger.log(Logger.ERROR, Logger.plugins, e.getMessage());
						// This should not happen at this point
						pluginAgg.handleInconsistentAggregationOnLocation(channelLocation, this);
						return;
					}

					// get the instance
					try {
						Object[] array = args.toArray();
						Class<?>[] classes = cons.getParameterTypes();
						JasmineUtil.adjustArgumentTypes(classes, array);
						result = cons.newInstance(array);
					} catch (Exception e) {
						Logger.log(Logger.ERROR, Logger.plugins, e.getMessage());
						pluginAgg.handleInconsistentAggregationOnLocation(channelLocation, this);
						return;
					}
					
				}

				// create a new JObject and an update to assign it to the requested location
				JObjectElement jobject = new JObjectElement(result);
				Update newUpdate = new Update(l, jobject, Update.UPDATE_ACTION, jue.agent, jue.sinfo);
				
				// add the resultant update to the results
				pluginAgg.addResultantUpdate(newUpdate, this);
			}
				
			// B-2) STORE
			if (jue.type == Type.Store) {
				JObjectElement jobj = jue.getStoreObject();
				
				String fieldName = jue.getStoreField();
				Object value = jue.getStoreValue();
				
				Field field = null;
				
				// get the field
				try {
					field = jobj.jType().getField(fieldName);
				} catch (Exception e) {
					Logger.log(Logger.ERROR, Logger.plugins, e.getMessage());
					// This should not happen at this point
					pluginAgg.handleInconsistentAggregationOnLocation(channelLocation, this);
					return;
				}

				value = JasmineUtil.specialTypeCast(field.getType(), value);

				try {
					field.set(jobj.object, value);
				} catch (Exception e) {
					Logger.log(Logger.ERROR, Logger.plugins, "JASMine Plugin: Error storing value in a field. " + e.getMessage());
					pluginAgg.handleInconsistentAggregationOnLocation(channelLocation, this);
					return;
				}
			}

			// B-3) INVOKE
			if (jue.type == Type.Invoke) {
				Location l = jue.getCoreASMLocation();
				Object obj = ((JObjectElement)jue.arguments.get(1)).object; // 'value(alpha)' 
				String methodName = (String)jue.arguments.get(2);  // the 'x'
				List<? extends Object> args = (List<?>)jue.arguments.get(3);  // method arguments 
				
				Method method = null;
				Object result = null;
				
				// get the method
				try {
					method = findMethod(obj.getClass(), methodName, args);
				} catch (Exception e) {
					Logger.log(Logger.ERROR, Logger.plugins, e.getMessage());
					// This should not happen at this point
					pluginAgg.handleInconsistentAggregationOnLocation(channelLocation, this);
					return;
				}

				// call the method
				try {
					Object[] array = args.toArray();
					Class<?>[] classes = method.getParameterTypes();
					JasmineUtil.adjustArgumentTypes(classes, array);
					result = method.invoke(obj, array);
				} catch (InvocationTargetException e) {
					Logger.log(Logger.ERROR, Logger.plugins, "JASMine Plugin: Exception thrown by method " +
							"\"" + methodName + "\". (" + e.getCause().getMessage() + "). Exception details: \n");
					e.getCause().printStackTrace(System.err);
					pluginAgg.handleInconsistentAggregationOnLocation(channelLocation, this);
					// TODO Inconsistent with the spec.
					return;
				} catch (Exception e) {
					Logger.log(Logger.ERROR, Logger.plugins, "JASMine Plugin: Error occured while " +
							"invoking method \"" + methodName + "\". (" + e.getMessage() + ")");
					pluginAgg.handleInconsistentAggregationOnLocation(channelLocation, this);
					// TODO Inconsistent with the spec.
					return;
				}
				
				// create the final result
				if (l != null) {
					Element finalResult;
					if (isImplicitConversionMode()) {
						// convert the value if we are in implicit conversion mode
						finalResult = JasmineUtil.toCoreASM(result);
					} else {
						// encapsulate the value in a JObject element if otherwise
						finalResult = new JObjectElement(result);
					}
					Update newUpdate = new Update(l, finalResult, Update.UPDATE_ACTION, jue.agent, jue.sinfo);
					pluginAgg.addResultantUpdate(newUpdate, this);
				}
			}
		}

		// flag all the updates
		for (Update u: channelUpdates)
			pluginAgg.flagUpdate(u, PluginAggregationAPI.Flag.SUCCESSFUL, this);

	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Aggregator#compose(org.coreasm.engine.absstorage.PluginCompositionAPI)
	 */
	public void compose(PluginCompositionAPI compAPI) {
		Collection<JasmineAbstractUpdateElement> set1 = new HashMultiset<JasmineAbstractUpdateElement>();
		Collection<JasmineAbstractUpdateElement> set2 = new HashMultiset<JasmineAbstractUpdateElement>();
		JasmineUpdateContainer batch1 = new JasmineUpdateContainer(set1);
		JasmineUpdateContainer batch2 = new JasmineUpdateContainer(set2);

		// get all the updates of the first step
		for (Update u: compAPI.getLocUpdates(1, channelLocation))
			if (u.value instanceof JasmineAbstractUpdateElement)
				set1.add((JasmineAbstractUpdateElement)u.value);

		// get all the updates of the second step
		for (Update u: compAPI.getLocUpdates(2, channelLocation))
			if (u.value instanceof JasmineAbstractUpdateElement)
				set2.add((JasmineAbstractUpdateElement)u.value);
		
		if (set1.size() == 0 && set2.size() == 0)
			return;
		
		// put both sets in a list
		ArrayList<JasmineAbstractUpdateElement> orderedList = new ArrayList<JasmineAbstractUpdateElement>();
		orderedList.add(batch1);
		orderedList.add(batch2);
		
		// add the resultant update
		JasmineUpdateContainer composed = new JasmineUpdateContainer(orderedList);
		compAPI.addComposedUpdate(new Update(channelLocation, composed, JASMINE_UPDATE_ACTION, composed.getAgents(), composed.getScannerInfos()), this);
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Aggregator#getUpdateActions()
	 */
	public String[] getUpdateActions() {
		return UPDATE_ACTIONS;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getBackgroundNames()
	 */
	public Set<String> getBackgroundNames() {
		return Collections.emptySet();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getBackgrounds()
	 */
	public Map<String, BackgroundElement> getBackgrounds() {
		return Collections.emptyMap();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getFunctionNames()
	 */
	public Set<String> getFunctionNames() {
		return getFunctions().keySet();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getFunctions()
	 */
	public Map<String, FunctionElement> getFunctions() {
		if (functions == null) {
			functions = new HashMap<String, FunctionElement>();
			
			functions.put(JasmineConvertorFunctionElement.FROM_JAVA_NAME, 
					new JasmineConvertorFunctionElement(
							JasmineConvertorFunctionElement.Type.fromJava));

			functions.put(JasmineConvertorFunctionElement.TO_JAVA_NAME, 
					new JasmineConvertorFunctionElement(
							JasmineConvertorFunctionElement.Type.toJava));
			
			functions.put(FieldReadFunctionElement.SUGGESTED_NAME,
					new FieldReadFunctionElement(this));
			
			functions.put(JavaEqualityFunctionElement.SUGGESTED_NAME,
					new JavaEqualityFunctionElement());
		}
		return functions;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getUniverseNames()
	 */
	public Set<String> getUniverseNames() {
		return Collections.emptySet();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getUniverses()
	 */
	public Map<String, UniverseElement> getUniverses() {
		return Collections.emptyMap();
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.VersionInfoProvider#getVersionInfo()
	 */
	public VersionInfo getVersionInfo() {
		return version;
	}

	public Parser<Node> getParser(String nonterminal) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}

	public Set<String> getRuleNames() {
		return Collections.emptySet();
	}

	public Map<String, RuleElement> getRules() {
		return Collections.emptyMap();
	}

	@Override
	public Set<String> getDependencyNames() {
		if (dependencies == null) {
			dependencies = new HashSet<String>();
			dependencies.add("StringPlugin");
			dependencies.add("NumberPlugin");
			dependencies.add("CollectionPlugin");
		}
		return dependencies;
	}

	/*
	 * Adjust the type of the given arguments to the correct types. 
	 * It basically solve the casting problem of 
	 * numbers in Java.
	 * 
	 * NOT NEEDED ANYMORE
	 * 
	private Object[] adjustArguments(Class[] expectedClasses, Object[] arguments) {
		//System.out.println("----V");
		for (int i=0; i < arguments.length; i++) {
			//System.out.println("expecting " + expectedClasses[i] + ", getting " + arguments[i].getClass());
			if (expectedClasses[i].equals(Double.class) && !arguments[i].getClass().equals(Double.class)) {
				arguments[i] = new Double(((Number)arguments[i]).doubleValue());
			}
			else
			if (expectedClasses[i].equals(Float.class) && !arguments[i].getClass().equals(Float.class)) {
				arguments[i] = new Float(((Number)arguments[i]).floatValue());
			}
			else // I am not sure why I need the following line, but it is needed! :-)
			if (expectedClasses[i].equals(Float.TYPE) && !arguments[i].getClass().equals(Float.TYPE)) {
				arguments[i] = new Float(((Number)arguments[i]).floatValue());
			}
			else
			if (expectedClasses[i].equals(Long.class) && !arguments[i].getClass().equals(Long.class)) {
				arguments[i] = new Long(((Number)arguments[i]).longValue());
			}
			else
			if (expectedClasses[i].equals(Integer.class) && !arguments[i].getClass().equals(Integer.class)) {
				arguments[i] = new Integer(((Number)arguments[i]).intValue());
			}
			else
			if (expectedClasses[i].equals(Short.class) && !arguments[i].getClass().equals(Short.class)) {
				arguments[i] = new Short(((Number)arguments[i]).shortValue());
			}
			else
			if (expectedClasses[i].equals(Byte.class) && !arguments[i].getClass().equals(Byte.class)) {
				arguments[i] = new Byte(((Number)arguments[i]).byteValue());
			}
		}
		//System.out.println("----^");
		return arguments;
	}
	*/
	
	// ------- Private Classes ----------------------------
	
	private static class JavaIdParseMap extends ParseMap<Object[], Node> {
		
		public JavaIdParseMap() { 
			super(PLUGIN_NAME);
		}

		@Override
		public Node apply(Object[] nodes) {
			ASTNode node = new ASTNode(
					PLUGIN_NAME, 
					ASTNode.ID_CLASS, 
					"JavaId", 
					"", 
					((Node)nodes[0]).getScannerInfo());
			addChildren(node, nodes);
			return node;
		}
		
		private void addChildren(Node parent, Object[] children) {
			
			for (Object child: children) {
				if (child != null) {
					if (child instanceof Object[])
						addChildren(parent, (Object[])child);
					else
					// otherwise child should be a Node!
						buildName(parent, (Node)child);
				}
			}
		}
		
		private void buildName(Node root, Node child) {
			root.setToken(root.getToken() + child.getToken());
		}
	}
	
	public Collection<OperatorRule> getOperatorRules() {
		//return Collections.emptySet();
		/**/
		if (operatorRules == null) {
			operatorRules = new HashSet<OperatorRule>();
			
			operatorRules.add(new OperatorRule(
					FIELD_ACCESS_OPERATOR, OpType.INFIX_LEFT, 875, PLUGIN_NAME));
			
		}
		return operatorRules;
		/**/
	}

	public Element interpretOperatorNode(Interpreter interpreter, ASTNode opNode) throws InterpreterException {
		Element result = null;
		
		// v -> x
		if (opNode.getToken().equals(FIELD_ACCESS_OPERATOR)) {
			
			ASTNode left = opNode.getFirst();
			ASTNode right = left.getNext();
			ASTNode termNode = left;
			
			// TODO this whole operator thing is strange
			if ((right.getFirst() == null || right.getFirst().getToken() == null) && !(right.getValue() instanceof StringElement))
				throw new InterpreterException("Right hand side of '" + 
						FIELD_ACCESS_OPERATOR + 
						"' is not a Java field.");
			
			String fieldName = null;
			// if field is an id
			if (right.getFirst() != null)
				fieldName = right.getFirst().getToken();
			else
				// if field is a String element
				fieldName = right.getValue().toString();
			
			if (termNode.getValue() instanceof JObjectElement) {
				JObjectElement jobj = (JObjectElement)termNode.getValue();
				
				Field field = null;
				
				// get the field
				try {
					field = jobj.jType().getField(fieldName);
				} catch (Exception e) {
					Logger.log(Logger.ERROR, Logger.plugins, e.getMessage());
					throw new InterpreterException("Field '" + fieldName + "' not found.");
				}
				
				try {
					field.get(jobj.object);
				} catch (Exception e) {
					Logger.log(Logger.ERROR, Logger.plugins, e.getMessage());
					throw new InterpreterException("Field '" + fieldName + "' is not accessible.");
				}
				
				// the above tasks are done for error checking
				// to get the benefit of caching, we get the value
				// from a monitored function
				try {
					result = capi.getStorage().getValue(
							new Location(FieldReadFunctionElement.SUGGESTED_NAME, 
									new ElementList(new StringElement(fieldName), jobj)));
				} catch (InvalidLocationException e) {
					throw new EngineError(e);
					// should not happen.
				}
				
				/*
				if (isImplicitConversionMode())
					result = JasmineUtil.asmValue(fieldValue);
				else
					result = new JObjectElement(fieldValue);
				*/
				
			} else {
				throw new InterpreterException("The left operand is not a JObject element.");
			}
		}
		
		return result;
	}

	/*
	 * Converts the given element to a Java object considering 
	 * the conversion mode of the plugin.
	 */
	private Object jValue(Element e) {
		if (isExplicitConversionMode()) {
			if (e instanceof JObjectElement)
				return ((JObjectElement)e).object;
			else
				return e;
		}
		else
			return JasmineUtil.javaValue(e).object;
	}

	/*
	 * Parse map for JASMine invoke rule.
	 */
	private static class InvokeParseMap extends ParserTools.ArrayParseMap  {

		boolean resultSeen = false;
		
		public InvokeParseMap() {
			super(PLUGIN_NAME);
		}

		@Override
		public Node apply(Object[] vals) {
			resultSeen = false;
			Node node = new InvokeRuleNode(((Node)vals[0]).getScannerInfo());
			addChildren(node, vals);
			return node;
		}

		@Override
		public void addChild(Node parent, Node child) {
			if (child instanceof ASTNode && 
					((ASTNode)child).getGrammarRule().equals("TupleTerm")) {
				for (Node n: child.getChildNodes())
					if (n instanceof ASTNode) 
						parent.addChild("lambda", n);
					else 
						parent.addChild(n);

				//parent.addChild("alpha", child);
			}
			if (child instanceof ASTNode && resultSeen)
				parent.addChild("gamma", child);
			else
				parent.addChild(child);
			if (child.getConcreteNodeType().equals(Node.KEYWORD_NODE) 
					&& child.getToken().equals("into"))
				resultSeen = true;
		}
		
	}
	/*
	 * Parse map for JASMine store rule.
	 */
	private static class StoreParseMap extends ParserTools.ArrayParseMap {
		public StoreParseMap() {
			super(PLUGIN_NAME);
		}

		@Override
		public Node apply(Object[] nodes) {
			Node node = new StoreRuleNode(((Node)nodes[0]).getScannerInfo());
			addChildren(node, nodes);
			return node;
		}
	}
	
	/*
	 * Parse map for JASMine native import rule.
	 */
	private static class NativeImportParseMap extends ParserTools.ArrayParseMap  {

		boolean nextIsLocation = false;
		
		public NativeImportParseMap() {
			super(PLUGIN_NAME);
		}

		@Override
		public Node apply(Object[] vals) {
			nextIsLocation = false;
			Node node = new NativeImportRuleNode(((Node)vals[0]).getScannerInfo());
			addChildren(node, vals);
			return node;
		}

		@Override
		public void addChild(Node parent, Node child) {
			if (child instanceof ASTNode) { 
				if (((ASTNode)child).getGrammarRule().equals("TupleTerm")) {
					for (Node n: child.getChildNodes())
						if (n instanceof ASTNode) 
							parent.addChild("lambda", n);
						else 
							parent.addChild(n);

					//parent.addChild("alpha", child);
				}
				else
				if (nextIsLocation)
					parent.addChild("beta", child);
				else
					parent.addChild(child);
			} else {
				parent.addChild(child);
				if (child.getConcreteNodeType().equals(Node.KEYWORD_NODE) && child.getToken().equals("into"))
					nextIsLocation = true;
			}
			
		}
		
	}

	public void fireOnModeTransition(EngineMode source, EngineMode target) {
		/* TODO This whole method is temporary. */
		if (target.equals(EngineMode.emParsingSpec)) {
			//basicExpParserArray[0] = ((ParserPlugin)capi.getPlugin("Kernel")).getParsers().get("BasicExpr").parser;
			//Parser<Node> p = ((ParserPlugin)capi.getPlugin("Kernel")).getParsers().get("BasicExpr").parser;
			//refBasicExpParser.set(p);
		}
	}

	public Map<EngineMode, Integer> getSourceModes() {
		return Collections.emptyMap();
	}

	public Map<EngineMode, Integer> getTargetModes() {
		if (targetModes == null) {
			targetModes = new HashMap<EngineMode, Integer>();
			targetModes.put(EngineMode.emParsingSpec, ExtensionPointPlugin.DEFAULT_PRIORITY);
		}
		return targetModes;
	}


}

