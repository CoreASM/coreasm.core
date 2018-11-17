package org.coreasm.engine.plugins.operator;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.codehaus.jparsec.Parser;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.EngineException;
import org.coreasm.engine.SpecLine;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.AbstractUniverse;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.OperatorRule;
import org.coreasm.engine.parser.OperatorRule.OpType;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.ExtensionPointPlugin;
import org.coreasm.engine.plugin.InitializationFailedException;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.list.ListElement;
import org.coreasm.engine.plugins.signature.DerivedFunctionElement;

public class OperatorPlugin extends Plugin implements ExtensionPointPlugin, OperatorProvider,
    ParserPlugin, InterpreterPlugin {

  public static final String PLUGIN_NAME = OperatorPlugin.class.getSimpleName();
  private static final String INFIXL_KEYWORD = "infixl";
  private static final String INFIXN_KEYWORD = "infixn";
  private static final String INFIXR_KEYWORD = "infixr";
  private static final String PREFIX_KEYWORD = "prefix";
  private static final String POSTFIX_KEYWORD = "postfix";
  private static final String INDEX_KEYWORD = "index";
  private static final String TERNARY_KEYWORD = "ternary";
  private static final String PAREN_KEYWORD = "paren";
  private static final String COMP_KEYWORD = "comp";
  private static final String GR_COMP = "Comprehension";
  private static final String GR_COMPRES = "ComprehensionResult";
  private static final String GR_GEN = "Genartor";
  private static final String GR_GENLIST = "GenartorList";
  private static final String GR_ENUMELEM = "EnumerableElement";

  /*private static final Pattern typedOperatorDefinitionGrammar = Pattern.compile(
      "^\\s*operator\\s+((?<fixity>infixl|infixn|infixr|prefix|postfix)\\s+"
          + "(?<precedence>0|[1-9][0-9]?[0-9]?|1000)|(?<fixity2>index|ternary|comp)\\s+"
          + "(?<precedence2>0|[1-9][0-9]?[0-9]?|1000)\\s+(?<op2>\\S+)|(?<fixity3>paren)\\s+"
          + "(?<op3>\\S+))\\s+(?<op>\\S+)(\\s+on\\s+(?<universe>[A-z_][A-z_0-9]*)"
          + "(\\s+\\*\\s+(?<universe2>[A-z_][A-z_0-9]*)(\\s+\\*\\s+(?<universe3>[A-z_][A-z_0-9]*))?)?)?\\s+"
          + "=\\s+(?<rule>[A-z_][A-z_0-9]*)\\s*$");*/
  private static final Pattern typedOperatorDefinitionGrammar = Pattern.compile(
      "^\\s*operator\\s+((?<fixity>infixl|infixn|infixr|prefix|postfix)\\s+"
          + "(?<precedence>0|[1-9][0-9]?[0-9]?|1000)|(?<fixity2>index|ternary)\\s+"
          + "(?<precedence2>0|[1-9][0-9]?[0-9]?|1000)\\s+(?<op2>\\S+)|(?<fixity3>paren|comp)\\s+"
          + "(?<op3>\\S+))\\s+(?<op>\\S+)(\\s+on\\s+(?<universe>[A-z_][A-z_0-9]*)"
          + "(\\s+\\*\\s+(?<universe2>[A-z_][A-z_0-9]*)(\\s+\\*\\s+(?<universe3>[A-z_][A-z_0-9]*))?)?)?\\s+"
          + "=\\s+(?<rule>[A-z_][A-z_0-9]*)\\s*$");
  private final Multimap<OperatorKey, OperatorValue> opStore = LinkedListMultimap.create();

  enum Fixity {
    INFIX,
    PREFIX,
    POSTFIX,
    INDEX,
    TERNARY,
    PAREN,
    COMP
  }

  enum Associativity {
    LEFT,
    NONE,
    RIGHT
  }

  static class OperatorKey {

    private final Fixity fixity;
    private final String[] operatorSymbols;

    OperatorKey(Fixity fixity, String[] operatorSymbols) {
      this.fixity = fixity;
      this.operatorSymbols = operatorSymbols;
    }

    Fixity getFixity() {
      return fixity;
    }

    String[] getOperatorSymbols() {
      return operatorSymbols;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      OperatorKey that = (OperatorKey) o;

      if (getFixity() != that.getFixity()) {
        return false;
      }
      return Arrays.equals(getOperatorSymbols(), that.getOperatorSymbols());
    }

    @Override
    public int hashCode() {
      int result = getFixity() != null ? getFixity().hashCode() : 0;
      result = 31 * result + Arrays.hashCode(getOperatorSymbols());
      return result;
    }
  }

  static class OperatorValue {

    private final Associativity associativity;
    private final int precedence;
    private String funName;
    private List<String> universes;

    OperatorValue(Associativity associativity, int precedence, String funName,
        List<String> universes) {
      this.associativity = associativity;
      this.precedence = precedence;
      this.funName = funName;
      this.universes = universes;
    }

    Associativity getAssociativity() {
      return associativity;
    }

    int getPrecedence() {
      return precedence;
    }

    String getFunName() {
      return funName;
    }
  }

  @Override
  public void initialize() throws InitializationFailedException {
    opStore.clear();
  }

  /**
   * Returns the version information of this module.
   */
  @Override
  public VersionInfo getVersionInfo() {
    return new VersionInfo(0, 0, 1, "alpha");
  }

  /**
   * Returns a map of engine modes to call priorities;
   * upon transition of
   * the engine mode to any of these modes, the given
   * plug-in must be notified.
   *
   * Zero (0) is the lowest priority and 100 is the highest calling
   * priority. The engine will consider this priority when
   * calling plug-ins at extension point transitions.
   * All plug-ins with the same priority level will
   * be called in a non-deterministic order.
   * Default call priority is {@link #DEFAULT_PRIORITY}.
   *
   * @return a map of engine modes to priorities
   */
  @Override
  public Map<EngineMode, Integer> getTargetModes() {
    Map<EngineMode, Integer> targetModes = new HashMap<>();
    targetModes.put(EngineMode.emParsingSpec, 80);
    return targetModes;
  }

  /**
   * Returns a map of engine modes to call priorities;
   * upon transition of
   * the engine mode from any of these modes, the given
   * plug-in must be notified.
   *
   * Zero (0) is the lowest priority and 100 is the highest calling
   * priority. The engine will consider this priority when
   * calling plug-ins at extension point transitions.
   * All plug-ins with the same priority level will
   * be called in a non-deterministic order.
   * Default call priority is {@link #DEFAULT_PRIORITY}.
   *
   * @return a map of engine modes to priorities
   */
  @Override
  public Map<EngineMode, Integer> getSourceModes() {
    Map<EngineMode, Integer> sourceModes = new HashMap<>();
    sourceModes.put(EngineMode.emLoadingPlugins, 80);
    return sourceModes;
  }

  /**
   * Is called by the engine whenever the engine mode is changed
   * from <code>source</code> to <code>target</code>.
   *
   * @param source the source mode
   * @param target the target mode
   */
  @Override
  public void fireOnModeTransition(EngineMode source, EngineMode target) throws EngineException {
    if (getSourceModes().containsKey(source) && getTargetModes().containsKey(target)) {
      CommentRemover cr = new CommentRemover();
      ArrayList<SpecLine> filteredLines = new ArrayList<>();
      for (SpecLine l : this.capi.getSpec().getLines()) {
        Matcher m = typedOperatorDefinitionGrammar.matcher(cr.append(l.text));
        if (m.matches()) {
          if (m.group("fixity") != null) {
            if (!handleOpDefinition(m.group("fixity"), Integer.valueOf(m.group("precedence")),
                m.group("rule"), m.group("universe"), m.group("universe2"), m.group("universe3"),
                m.group("op"))) {
              filteredLines.add(l);
            }
          } else if (m.group("fixity2") != null) {
            //NOTE: op2 being in front of op is NOT a typo, see regex definition.
            if (!handleOpDefinition(m.group("fixity2"), Integer.valueOf(m.group("precedence2")),
                m.group("rule"), m.group("universe"), m.group("universe2"), m.group("universe3"),
                m.group("op2"), m.group("op"))) {
              filteredLines.add(l);
            }
          } else {
            //NOTE: op3 being in front of op is NOT a typo, see regex definition.
            if (!handleOpDefinition(m.group("fixity3"), 100, //arbitrary value, not important
                m.group("rule"), m.group("universe"), m.group("universe2"), m.group("universe3"),
                m.group("op3"), m.group("op"))) {
              filteredLines.add(l);
            }
          }
          filteredLines.add(new SpecLine("", l.fileName, l.line));
        } else {
          filteredLines.add(l);
        }
      }
      this.capi.getSpec().updateLines(filteredLines);
    }
  }

  private boolean handleOpDefinition(String fixity, int precedence, String ruleName,
      String universe, String universe2, String universe3, String... operatorSymbols) {
    switch (fixity) {
      case INFIXL_KEYWORD:
        if (universe != null) {
          if (universe2 == null || universe3 != null) {
            capi.error("Arity of signature and operator does not match in definition of operator " + Arrays.toString(operatorSymbols) + ".");
            return false;
          }
          opStore.put(new OperatorKey(Fixity.INFIX, operatorSymbols),
              new OperatorValue(Associativity.LEFT, precedence, ruleName,
                  Arrays.asList(universe, universe2)));
        } else {
          opStore.put(new OperatorKey(Fixity.INFIX, operatorSymbols),
              new OperatorValue(Associativity.LEFT, precedence, ruleName, null));
        }
        break;
      case INFIXN_KEYWORD:
        if (universe != null) {
          if (universe2 == null || universe3 != null) {
            capi.error("Arity of signature and operator does not match in definition of operator " + Arrays.toString(operatorSymbols) + ".");
            return false;
          }
          opStore.put(new OperatorKey(Fixity.INFIX, operatorSymbols),
              new OperatorValue(Associativity.NONE, precedence, ruleName,
                  Arrays.asList(universe, universe2)));
        } else {
          opStore.put(new OperatorKey(Fixity.INFIX, operatorSymbols),
              new OperatorValue(Associativity.NONE, precedence, ruleName, null));
        }
        break;
      case INFIXR_KEYWORD:
        if (universe != null) {
          if (universe2 == null || universe3 != null) {
            capi.error("Arity of signature and operator does not match in definition of operator " + Arrays.toString(operatorSymbols) + ".");
            return false;
          }
          opStore.put(new OperatorKey(Fixity.INFIX, operatorSymbols),
              new OperatorValue(Associativity.RIGHT, precedence, ruleName,
                  Arrays.asList(universe, universe2)));
        } else {
          opStore.put(new OperatorKey(Fixity.INFIX, operatorSymbols),
              new OperatorValue(Associativity.RIGHT, precedence, ruleName, null));
        }
        break;
      case PREFIX_KEYWORD:
        if (universe != null) {
          if (universe2 != null || universe3 != null) {
            capi.error("Arity of signature and operator does not match in definition of operator " + Arrays.toString(operatorSymbols) + ".");
            return false;
          }
          opStore.put(new OperatorKey(Fixity.PREFIX, operatorSymbols),
              new OperatorValue(Associativity.NONE, precedence, ruleName,
                  Collections.singletonList(universe)));
        } else {
          opStore.put(new OperatorKey(Fixity.PREFIX, operatorSymbols),
              new OperatorValue(Associativity.NONE, precedence, ruleName, null));
        }
        break;
      case POSTFIX_KEYWORD:
        if (universe != null) {
          if (universe2 != null || universe3 != null) {
            capi.error("Arity of signature and operator does not match in definition of operator " + Arrays.toString(operatorSymbols) + ".");
            return false;
          }
          opStore.put(new OperatorKey(Fixity.POSTFIX, operatorSymbols),
              new OperatorValue(Associativity.NONE, precedence, ruleName,
                  Collections.singletonList(universe)));
        } else {
          opStore.put(new OperatorKey(Fixity.POSTFIX, operatorSymbols),
              new OperatorValue(Associativity.NONE, precedence, ruleName, null));
        }
        break;
      case INDEX_KEYWORD:
        if (universe != null) {
          if (universe2 == null || universe3 != null) {
            capi.error("Arity of signature and operator does not match in definition of operator " + Arrays.toString(operatorSymbols) + ".");
            return false;
          }
          opStore.put(new OperatorKey(Fixity.INDEX, operatorSymbols),
              new OperatorValue(Associativity.NONE, precedence, ruleName,
                  Arrays.asList(universe, universe2)));
        } else {
          opStore.put(new OperatorKey(Fixity.INDEX, operatorSymbols),
              new OperatorValue(Associativity.NONE, precedence, ruleName, null));
        }
        break;
      case TERNARY_KEYWORD:
        if (universe != null) {
          if (universe2 == null || universe3 == null) {
            capi.error("Arity of signature and operator does not match in definition of operator " + Arrays.toString(operatorSymbols) + ".");
            return false;
          }
          opStore.put(new OperatorKey(Fixity.TERNARY, operatorSymbols),
              new OperatorValue(Associativity.NONE, precedence, ruleName,
                  Arrays.asList(universe, universe2, universe3)));
        } else {
          opStore.put(new OperatorKey(Fixity.TERNARY, operatorSymbols),
              new OperatorValue(Associativity.NONE, precedence, ruleName, null));
        }
        break;
      case PAREN_KEYWORD:
        if (universe != null) {
          if (universe2 != null || universe3 != null) {
            capi.error("Arity of signature and operator does not match in definition of operator " + Arrays.toString(operatorSymbols) + ".");
            return false;
          }
          opStore.put(new OperatorKey(Fixity.PAREN, operatorSymbols),
              new OperatorValue(Associativity.NONE, precedence, ruleName,
                  Collections.singletonList(universe)));
        } else {
          opStore.put(new OperatorKey(Fixity.PAREN, operatorSymbols),
              new OperatorValue(Associativity.NONE, precedence, ruleName, null));
        }
        break;
      case COMP_KEYWORD:
        if (universe != null || universe2 != null || universe3 != null) {
          capi.error("Arity of signature and operator does not match in definition of operator " + Arrays.toString(operatorSymbols) + ".");
          return false;
        } else {
          opStore.put(new OperatorKey(Fixity.COMP, operatorSymbols),
              new OperatorValue(Associativity.NONE, -1, null, null));
        }
        break;
      default:
        assert false;
    }
    return true;
  }

  /**
   * Returns the operator rules, and thus operator syntax, provided by
   * this plugin as a list of operator rules.
   *
   * @return a collection of operator rule information in <code>OperatorRule</code>s
   */
  @Override
  public Collection<OperatorRule> getOperatorRules() {
    List<OperatorRule> opRules = new LinkedList<>();
    for (Entry<OperatorKey, Collection<OperatorValue>> op : opStore.asMap().entrySet()) {
      assert !op.getValue().isEmpty();
      OperatorValue opVal = op.getValue().iterator().next();
      if (!op.getValue().stream().map(v -> v.getAssociativity()).allMatch(opVal.getAssociativity()::equals)) {
        throw new IllegalStateException("Multiple syntactically identical operators (" + Arrays
            .toString(op.getKey().operatorSymbols) + ") with different associativities (" +
            op.getValue().stream().map(v -> v.getAssociativity().toString()).distinct().collect(
                Collectors.joining(",")) + ") are not allowed!");
      }
      switch (op.getKey().fixity) {
        case INFIX:
          switch (opVal.getAssociativity()) {
            case LEFT:
              opRules.add(new OperatorRule(op.getKey().operatorSymbols[0], OpType.INFIX_LEFT,
                  opVal.getPrecedence(), PLUGIN_NAME));
              break;
            case NONE:
              opRules.add(new OperatorRule(op.getKey().operatorSymbols[0], OpType.INFIX_NON,
                  opVal.getPrecedence(), PLUGIN_NAME));
              break;
            case RIGHT:
              opRules.add(new OperatorRule(op.getKey().operatorSymbols[0], OpType.INFIX_RIGHT,
                  opVal.getPrecedence(), PLUGIN_NAME));
              break;
          }
          break;
        case PREFIX:
          opRules.add(new OperatorRule(op.getKey().operatorSymbols[0], OpType.PREFIX,
              opVal.getPrecedence(), PLUGIN_NAME));
          break;
        case POSTFIX:
          opRules.add(new OperatorRule(op.getKey().operatorSymbols[0], OpType.POSTFIX,
              opVal.getPrecedence(), PLUGIN_NAME));
          break;
        case INDEX:
          opRules.add(
              new OperatorRule(op.getKey().operatorSymbols[0], op.getKey().operatorSymbols[1],
                  OpType.INDEX, opVal.getPrecedence(), PLUGIN_NAME));
          break;
        case TERNARY:
          opRules.add(
              new OperatorRule(op.getKey().operatorSymbols[0], op.getKey().operatorSymbols[1],
                  OpType.TERNARY, opVal.getPrecedence(), PLUGIN_NAME));
          break;
        case PAREN:
          opRules.add(
              new OperatorRule(op.getKey().operatorSymbols[0], op.getKey().operatorSymbols[1],
                  OpType.PAREN, opVal.getPrecedence(), PLUGIN_NAME));
          break;
      }
    }
    return opRules;
  }

  /**
   * Holds all behaviors of every operator provided by a plugin.
   * Based on information stored in the node passed to the plugin here,
   * the correct operator behavior is used to evaluate the node.
   * - If there is a valid semantics for the operator and given operands, the
   * resultant element should be returned.
   * - If there is NO semantics for the operator and operands, a
   * value of <code>null</code> should be returned.
   * - If there is a problem with the operands provided, an
   * <code>InterpreterException</code> should be thrown.
   * <p>
   * <b>NOTE:</b> Any implementation of this method must be thread-safe, since
   * it may be called simultaneously by more than one thread during the simulation.
   *
   * @param interpreter the interpreter instance that calls this method
   * @param opNode      an AST <code>Node</code> for the given operator
   *                    which should have a behavior provided by this plugin.
   * @return an <code>Element</code> which is the result of an
   * operator behavior evaluating a node.
   * @throws <code>Interpreter</code> Exception if the result of
   *                                  interpreting this node is an error.
   */
  @Override
  public Element interpretOperatorNode(Interpreter interpreter, ASTNode opNode)
      throws InterpreterException {
    Fixity f;
    ASTNode[] args;
    opNode = (ASTNode) opNode.cloneTree();
    switch (opNode.getGrammarClass()) {
      case ASTNode.BINARY_OPERATOR_CLASS:
        f = Fixity.INFIX;
        args = new ASTNode[2];
        args[0] = opNode.getFirst();
        args[1] = args[0].getNext();
        break;
      case ASTNode.UNARY_OPERATOR_CLASS:
        if (opNode.unparseTree().startsWith(opNode.getToken())) {
          f = Fixity.PREFIX;
          args = new ASTNode[1];
          args[0] = opNode.getFirst();
        } else {
          f = Fixity.POSTFIX;
          args = new ASTNode[1];
          args[0] = opNode.getFirst();
        }
        break;
      case ASTNode.INDEX_OPERATOR_CLASS:
        f = Fixity.INDEX;
        args = new ASTNode[2];
        args[0] = opNode.getFirst();
        args[1] = args[0].getNext();
        break;
      case ASTNode.TERNARY_OPERATOR_CLASS:
        f = Fixity.TERNARY;
        args = new ASTNode[3];
        args[0] = opNode.getFirst();
        args[1] = args[0].getNext();
        args[2] = args[1].getNext();
        break;
      case ASTNode.PAREN_OPERATOR_CLASS:
        f = Fixity.PAREN;
        args = new ASTNode[1];
        args[0] = opNode.getFirst();
        break;
      default:
        throw new InterpreterException("Invalid plugin name: " + opNode.getPluginName());
    }
    final ASTNode finalOpNode = opNode;
    return opStore.get(new OperatorKey(f, opNode.getToken().split(OperatorRule.OPERATOR_DELIMITER)))
        .stream().map(op -> {
          String opFunName = op.getFunName();
          FunctionElement fun = capi.getStorage().getFunction(opFunName);
          if (errorChecks(fun, args.length, finalOpNode.getToken(), opFunName)) {
            List<Element> evaluatedArgs = new ArrayList<>(args.length);
            for (ASTNode arg : args) {
              if (arg == null) {
                evaluatedArgs.add(Element.UNDEF);
                continue;
              }
              try {
                interpreter.setPosition(arg);
                do {
                  interpreter.executeTree();
                } while (!arg.isEvaluated());
                evaluatedArgs.add(arg.getValue());
              } catch (InterpreterException e) {
                evaluatedArgs.add(null);
              }
            }
            for (int i = 0; i < evaluatedArgs.size(); ++i) {
              if (op.universes != null) {
                String domName = op.universes.get(i);
                if (domName != null) {
                  AbstractUniverse domain = capi.getStorage().getUniverse(domName);
                  if (domain != null && !domain.member(evaluatedArgs.get(i))) {
                    return null;
                  }
                }
              }
            }
            return fun.getValue(evaluatedArgs);
          } else {
            return null;
          }
        }).distinct().filter(Objects::nonNull)
        .collect(Collectors.collectingAndThen(Collectors.toList(),
            list -> {
              if (list.size() != 1) {
                return null;
              } else {
                return list.get(0);
              }
            }));
  }

  private boolean errorChecks(FunctionElement f, int nArgs, String opToken, String fName) {
    if (f != null && f != Element.UNDEF) {
      if (f.getSignature() != null) {
        if (f.getSignature().getDomain().size() == nArgs) {
          return true;
        } else {
          capi.error("Arity of the function " + fName + " and operator " + opToken
              + " does not match in definition of operator " + opToken + ".");
          return false;
        }
      }
      try {
        DerivedFunctionElement df = (DerivedFunctionElement) f;
        if (df.getParams().size() == nArgs) {
          return true;
        } else {
          capi.error("Arity of the derived function " + fName + " and operator " + opToken
              + " does not match in definition of operator " + opToken + ".");
          return false;
        }
      } catch (ClassCastException cce) {
        capi.error("Function " + fName + " in definition of operator " + opToken
            + " is not a derived function and does not have a signature.");
        return false;
      }
    } else {
      capi.error("Undefined function " + fName + " in definition of operator " + opToken + ".");
      return false;
    }
  }

  /**
   * Returns the pieces of Lexer provided by this plug-in.
   * The returned value should not be null.
   *
   * This method should only be called by the Kernel and
   * other plug-ins should NOT call this method.
   *
   * @return a set of lexers
   * @see Parser
   */
  @Override
  public Set<Parser<? extends Object>> getLexers() {
    return Collections.emptySet();
  }

  /**
   * Returns the grammar rules provided by
   * this plugin as a map of nonterminals to grammar rules.
   * This method should only be called by the Kernel and
   * other plug-ins should NOT call this method and should
   * use {@link #getParser(String)} if they are looking for a specific
   * parser provided by this plugin.
   *
   * @return a map of nonterminals to grammar rules
   */
  @Override
  public Map<String, GrammarRule> getParsers() {
    Map<String, GrammarRule> m = new HashMap<>();
    final ParserTools parserTools = ParserTools.getInstance(capi);
    final Kernel kernelPlugin = (Kernel)capi.getPlugin("Kernel");
    final KernelServices kernel = (KernelServices)kernelPlugin.getPluginInterface();
    final Parser<Node> termParser = kernel.getTermParser();
    for (Entry<OperatorKey, Collection<OperatorValue>> op : opStore.asMap().entrySet()) {
      assert !op.getValue().isEmpty();
      OperatorValue opVal = op.getValue().iterator().next();
      if (!op.getValue().stream().map(OperatorValue::getAssociativity).allMatch(opVal.getAssociativity()::equals)) {
        throw new IllegalStateException("Multiple syntactically identical operators (" + Arrays
            .toString(op.getKey().operatorSymbols) + ") with different associativities (" +
            op.getValue().stream().map(v -> v.getAssociativity().toString()).distinct().collect(
                Collectors.joining(",")) + ") are not allowed!");
      }
      switch (op.getKey().fixity) {
        case COMP:
          m.put(Kernel.GR_FUNCTION_RULE_TERM, new GrammarRule(Arrays.toString(op.getKey().operatorSymbols), "",
              parserTools.seq(
                  parserTools.seq(
                  parserTools.seq(
                      parserTools.getIdParser(),
                      parserTools.getOprParser(op.getKey().operatorSymbols[0]),
                      termParser
                  ).map(ob -> {
                    Node n = new ASTNode(
                        PLUGIN_NAME, GR_GEN, "",
                        "",
                        ((Node) ob[0]).getScannerInfo());
                    for (Object o : ob) if (o != null) n.addChild((Node) o);
                    return n;
                  }),
                  parserTools.seq(
                      parserTools.getOprParser(","),
                      parserTools.getIdParser(),
                      parserTools.getOprParser(op.getKey().operatorSymbols[0]),
                      termParser
                      ).map(ob -> {
                        Node n = new ASTNode(
                            PLUGIN_NAME, GR_GEN, "",
                            "",
                            ((Node) ob[0]).getScannerInfo());
                        for (Object o : ob) if (o != null) n.addChild((Node) o);
                        return n;
                      }).many()
                  ).map(obArr -> {
                    List<Object> ob = new ArrayList<>();
                    ob.add(obArr[0]);
                    ob.addAll((List) obArr[1]);
                    Node n = new ASTNode(
                        PLUGIN_NAME, GR_GENLIST, "",
                        "",
                        ((Node) ob.get(0)).getScannerInfo());
                    for (Object o : ob) if (o instanceof Node) n.addChild((Node) o);
                    return n;
                  }),
                  parserTools.getOprParser(op.getKey().operatorSymbols[1]),
                  termParser
              ).map(ob -> {
                Node n = new ASTNode(
                    PLUGIN_NAME, GR_COMP, "",
                    op.getKey().operatorSymbols[0] + OperatorRule.OPERATOR_DELIMITER + op.getKey().operatorSymbols[1],
                    ((Node) ob[0]).getScannerInfo());
                for (Object o : ob) if (o != null) n.addChild((Node) o);
                return n;
              }), PLUGIN_NAME));
          break;
      }
    }
    return m;
  }

  /**
   * Provides a hook to a parser that this plug-in provides
   * for the given nonterminal. This is basically used by other
   * plug-ins in creating their own parser which is composed of
   * other plugin's parsers.
   *
   * @param nonterminal name of the nonterminal
   * @return a JParsec parser object
   * @see Parser
   */
  @Override
  public Parser<Node> getParser(String nonterminal) {
    return null;
  }

  /**
   * Returns the list of keywords this plugin provides.
   * The returned value should not be null.
   *
   * @return a String array of keywords.
   */
  @Override
  public String[] getKeywords() {
    return new String[0];
  }

  /**
   * Returns the list of operators this plugin provides.
   * The returned value should not be null.
   *
   * @return a String array of operators.
   */
  @Override
  public String[] getOperators() {
    return opStore.keySet().stream().flatMap(k -> Arrays.stream(k.operatorSymbols))
        .toArray(String[]::new);
  }

  static class GeneratorListElement extends Element {
    private final Map<String, Enumerable> generators;

    GeneratorListElement(Map<String, Enumerable> generators) {
      this.generators = generators;
    }

    Map<String, Enumerable> getGenerators() {
      return generators;
    }
  }

  /**
   * This method is the interpreter rule of this plugin.
   * This method gets the value of <i>pos</i> and returns
   * a new value for <i>pos</i>. This is the implementation
   * of the <i>pluginRule</i> function.
   * <p>
   * This method should NOT return <code>null</code>. If this method
   * cannot interpret <code>pos</code>, it should return <code>pos</code>.
   * <p>
   * <b>NOTE:</b> Any implementation of this method must be thread-safe, since
   * it may be called simultaneously by more than one thread during the simulation.
   *
   * @param interpreter the parent interpreter (most likely, component of the engine)
   * @param pos         the value of <i>pos</i>
   * @return new value of <i>pos</i>
   */
  @Override
  public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
    switch (pos.getGrammarClass()) {
      case GR_COMP:
        if (!pos.getFirst().isEvaluated()) return pos.getFirst();
        List<List<EASTNode>> argLists = ((GeneratorListElement) pos.getFirst().getValue())
            .getGenerators().entrySet().stream().map(gen ->
                gen.getValue().enumerate().stream()
                .map(e ->
                    new EASTNode(PLUGIN_NAME, GR_ENUMELEM, "", e.toString(),
                        pos.getScannerInfo(), e, gen.getKey())
                ).collect(Collectors.toList())
            ).collect(Collectors.toList());
        EASTNode[][] args = new EASTNode[argLists.stream().map(List::size).reduce(1, (a, b) -> a * b)][argLists.size()];
        int m = 1;
        for (int i = 0; i < argLists.size(); ++i) {
          int idx = 0;
          while (idx < args.length)
            for (int j = 0; j < argLists.get(i).size(); ++j)
              for (int k = 0; k < m; ++k)
                args[idx++][i] = argLists.get(i).get(j);
          m *= argLists.get(i).size();
        }
        ASTNode fun = pos.getAbstractChildNodes().get(pos.getAbstractChildNodes().size() - 1);
        ASTNode res = new ASTNode(PLUGIN_NAME, GR_COMPRES, "", "", pos.getScannerInfo());
        res.setParent(pos);
        for (EASTNode[] argList : args) {
          ASTNode newChild = interpreter.copyTreeSub(fun,
              Arrays.stream(argList).map(EASTNode::getKey).collect(Collectors.toList()),
              Arrays.asList(argList));
          newChild.setParent(res);
          res.addChild(newChild);
        }
        return res;

      case GR_COMPRES:
        Optional<ASTNode> u = pos.getAbstractChildNodes().stream().filter(n -> !n.isEvaluated()).findFirst();
        if (u.isPresent()) return u.get();
        ListElement resVal = new ListElement(pos.getAbstractChildNodes().stream()
            .map(ASTNode::getValue).collect(Collectors.toList()));
        pos.setNode(null, null, resVal);
        pos.getParent().setNode(null, null, resVal);
        return pos.getParent().getParent();

      case GR_GENLIST:
        Optional<ASTNode> x = pos.getAbstractChildNodes().stream().filter(n -> !n.isEvaluated()).findFirst();
        if (x.isPresent()) return x.get();
        pos.setNode(null, null, new GeneratorListElement(
            pos.getAbstractChildNodes().stream()
                .filter(n -> GR_GEN.equals(n.getGrammarClass()))
                .collect(Collectors.toMap(Node::getToken, gen -> (Enumerable) gen.getValue()))));
        return pos.getParent();

      case GR_GEN:
        if (!pos.getAbstractChildNodes().get(pos.getAbstractChildNodes().size() - 1).isEvaluated())
          return pos.getAbstractChildNodes().get(pos.getAbstractChildNodes().size() - 1);
        pos.setNode(null, null,
            pos.getAbstractChildNodes().get(pos.getAbstractChildNodes().size() - 1).getValue());
        if (!(pos.getValue() instanceof Enumerable))
          throw new InterpreterException("Right hand side of generator is not enumerable.");
        pos.setToken(pos.getFirst().getToken());
        return pos.getParent();

      case GR_ENUMELEM:
        pos.setNode(null, null, ((EASTNode) pos).getVal());
        return pos.getParent();
    }
    return null;
  }
}
