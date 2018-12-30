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
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.CommentRemover;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.OperatorRule;
import org.coreasm.engine.parser.OperatorRule.OpType;
import org.coreasm.engine.plugin.ExtensionPointPlugin;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.signature.DerivedFunctionElement;

public class OperatorPlugin extends Plugin implements ExtensionPointPlugin, OperatorProvider,
    ParserPlugin {

  public static final String PLUGIN_NAME = OperatorPlugin.class.getSimpleName();
  private static final String INFIXL_KEYWORD = "infixl";
  private static final String INFIXN_KEYWORD = "infixn";
  private static final String INFIXR_KEYWORD = "infixr";
  private static final String PREFIX_KEYWORD = "prefix";
  private static final String POSTFIX_KEYWORD = "postfix";
  private static final String CLOSED_KEYWORD = "closed";

  private static final Pattern typedOperatorDefinitionGrammar = Pattern.compile(
      "^\\s*operator\\s+(?<fixity>infixl|infixn|infixr|prefix|postfix|closed)\\s+"
          + "((?<precedence>\\d+)\\s+)?(?<delimiter>#*)'(?<op>.+)'\\k<delimiter>"
          + "(\\s+on(?<universes>\\s+[A-z_][A-z_0-9]*(\\s+\\*\\s+[A-z_][A-z_0-9]*)*))?"
          + "\\s+=\\s+(?<rule>[A-z_][A-z_0-9]*)\\s*$");
  private final Multimap<OperatorKey, OperatorValue> opStore = LinkedListMultimap.create();

  enum Fixity {
    INFIX,
    PREFIX,
    POSTFIX,
    CLOSED,
  }

  enum Associativity {
    LEFT,
    NONE,
    RIGHT
  }

  static class OperatorKey {

    private final Fixity fixity;
    private final String operatorSymbols;

    OperatorKey(Fixity fixity, String operatorSymbols) {
      this.fixity = fixity;
      this.operatorSymbols = operatorSymbols;
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

      if (fixity != that.fixity) {
        return false;
      }
      return operatorSymbols != null ? operatorSymbols.equals(that.operatorSymbols)
          : that.operatorSymbols == null;
    }

    @Override
    public int hashCode() {
      int result = fixity != null ? fixity.hashCode() : 0;
      result = 31 * result + (operatorSymbols != null ? operatorSymbols.hashCode() : 0);
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
  public void initialize() {
    opStore.clear();
  }

  /**
   * Returns the version information of this module.
   */
  @Override
  public VersionInfo getVersionInfo() {
    return new VersionInfo(0, 1, 1, "alpha");
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
          int p = -1;
          try {
            p = Integer.valueOf(m.group("precedence"));
          } catch (NumberFormatException ignored) {}
          if (handleOpDefinition(m.group("fixity"), p, m.group("rule"), 
              m.group("universes"), m.group("op"))) {
            filteredLines.add(new SpecLine("", l.fileName, l.line));
          } else
            filteredLines.add(l);
        } else {
          filteredLines.add(l);
        }
      }
      this.capi.getSpec().updateLines(filteredLines);
    }
  }

  private boolean handleOpDefinition(String fixity, int precedence, String functionName,
      String universes, String opString) {
    String[] universe = universes != null ? universes.trim().replaceAll("\\s+", "").split("\\*") : new String[0];
    opString = opString.trim().replaceAll("\\s+", OperatorRule.OPERATOR_DELIMITER);
    String[] operatorSymbols = opString.split(OperatorRule.OPERATOR_DELIMITER);
    if (CLOSED_KEYWORD.equals(fixity) && precedence >= 0)
      capi.warning(PLUGIN_NAME, "Precedence is ignored for closed operator " + opString + ".");
    else if (!CLOSED_KEYWORD.equals(fixity) && precedence < 0) {
      capi.error("Precedence is required for " + fixity + " operator " + opString + ".");
      return false;
    }
    switch (fixity) {
      case INFIXL_KEYWORD:
        if (universe.length > 0) {
          if (universe.length != operatorSymbols.length + 1) {
            capi.error("Arity of signature and operator does not match in definition of operator " + opString + ".");
            return false;
          }
          opStore.put(new OperatorKey(Fixity.INFIX, opString),
              new OperatorValue(Associativity.LEFT, precedence, functionName,
                  Arrays.asList(universe)));
        } else {
          opStore.put(new OperatorKey(Fixity.INFIX, opString),
              new OperatorValue(Associativity.LEFT, precedence, functionName, null));
        }
        break;
      case INFIXN_KEYWORD:
        if (universe.length > 0) {
          if (universe.length != operatorSymbols.length + 1) {
            capi.error("Arity of signature and operator does not match in definition of operator " + opString + ".");
            return false;
          }
          opStore.put(new OperatorKey(Fixity.INFIX, opString),
              new OperatorValue(Associativity.NONE, precedence, functionName,
                  Arrays.asList(universe)));
        } else {
          opStore.put(new OperatorKey(Fixity.INFIX, opString),
              new OperatorValue(Associativity.NONE, precedence, functionName, null));
        }
        break;
      case INFIXR_KEYWORD:
        if (universe.length > 0) {
          if (universe.length != operatorSymbols.length + 1) {
            capi.error("Arity of signature and operator does not match in definition of operator " + opString + ".");
            return false;
          }
          opStore.put(new OperatorKey(Fixity.INFIX, opString),
              new OperatorValue(Associativity.RIGHT, precedence, functionName,
                  Arrays.asList(universe)));
        } else {
          opStore.put(new OperatorKey(Fixity.INFIX, opString),
              new OperatorValue(Associativity.RIGHT, precedence, functionName, null));
        }
        break;
      case PREFIX_KEYWORD:
        if (universe.length > 0) {
          if (universe.length != operatorSymbols.length) {
            capi.error("Arity of signature and operator does not match in definition of operator " + opString + ".");
            return false;
          }
          opStore.put(new OperatorKey(Fixity.PREFIX, opString),
              new OperatorValue(Associativity.NONE, precedence, functionName,
                  Arrays.asList(universe)));
        } else {
          opStore.put(new OperatorKey(Fixity.PREFIX, opString),
              new OperatorValue(Associativity.NONE, precedence, functionName, null));
        }
        break;
      case POSTFIX_KEYWORD:
        if (universe.length > 0) {
          if (universe.length != operatorSymbols.length) {
            capi.error("Arity of signature and operator does not match in definition of operator " + opString + ".");
            return false;
          }
          opStore.put(new OperatorKey(Fixity.POSTFIX, opString),
              new OperatorValue(Associativity.NONE, precedence, functionName,
                  Arrays.asList(universe)));
        } else {
          opStore.put(new OperatorKey(Fixity.POSTFIX, opString),
              new OperatorValue(Associativity.NONE, precedence, functionName, null));
        }
        break;
      case CLOSED_KEYWORD:
        if (universe.length > 0) {
          if (universe.length != operatorSymbols.length - 1) {
            capi.error("Arity of signature and operator does not match in definition of operator " + opString + ".");
            return false;
          }
          opStore.put(new OperatorKey(Fixity.CLOSED, opString),
              new OperatorValue(Associativity.NONE, 999, functionName,
                  Arrays.asList(universe)));
        } else {
          opStore.put(new OperatorKey(Fixity.CLOSED, opString),
              new OperatorValue(Associativity.NONE, 999, functionName, null));
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
      if (!op.getValue().stream().map(OperatorValue::getAssociativity).allMatch(opVal.getAssociativity()::equals)) {
        throw new IllegalStateException("Multiple syntactically identical operators (" + 
            op.getKey().operatorSymbols + ") with different associativities (" +
            op.getValue().stream().map(v -> v.getAssociativity().toString()).distinct().collect(
                Collectors.joining(",")) + ") are not allowed!");
      }
      switch (op.getKey().fixity) {
        case INFIX:
          switch (opVal.getAssociativity()) {
            case LEFT:
              opRules.add(new OperatorRule(op.getKey().operatorSymbols, OpType.INFIX_LEFT,
                  opVal.getPrecedence(), PLUGIN_NAME));
              break;
            case NONE:
              opRules.add(new OperatorRule(op.getKey().operatorSymbols, OpType.INFIX_NON,
                  opVal.getPrecedence(), PLUGIN_NAME));
              break;
            case RIGHT:
              opRules.add(new OperatorRule(op.getKey().operatorSymbols, OpType.INFIX_RIGHT,
                  opVal.getPrecedence(), PLUGIN_NAME));
              break;
          }
          break;
        case PREFIX:
          opRules.add(new OperatorRule(op.getKey().operatorSymbols, OpType.PREFIX,
              opVal.getPrecedence(), PLUGIN_NAME));
          break;
        case POSTFIX:
          opRules.add(new OperatorRule(op.getKey().operatorSymbols, OpType.POSTFIX,
              opVal.getPrecedence(), PLUGIN_NAME));
          break;
        case CLOSED:
          opRules.add(new OperatorRule(op.getKey().operatorSymbols, OpType.CLOSED,
              opVal.getPrecedence(), PLUGIN_NAME));
          break;
        default:
          assert false;
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
    switch (opNode.getGrammarClass()) {
      case ASTNode.BINARY_OPERATOR_CLASS:
        f = Fixity.INFIX;
        args = new ASTNode[opNode.getToken().split(OperatorRule.OPERATOR_DELIMITER).length + 1];
        break;
      case ASTNode.UNARY_OPERATOR_CLASS:
        if (opNode.unparseTree().startsWith(opNode.getToken())) {
          f = Fixity.PREFIX;
        } else {
          f = Fixity.POSTFIX;
        }
        args = new ASTNode[opNode.getToken().split(OperatorRule.OPERATOR_DELIMITER).length];
        break;
      case ASTNode.CLOSED_OPERATOR_CLASS:
        f = Fixity.CLOSED;
        args = new ASTNode[opNode.getToken().split(OperatorRule.OPERATOR_DELIMITER).length - 1];
        break;
      default:
        throw new InterpreterException("Invalid grammar class: " + opNode.getGrammarClass());
    }
    args[0] = opNode.getFirst();
    for (int i = 1; i < args.length; ++i) args[i] = args[i - 1].getNext();
    final ASTNode finalOpNode = opNode;
    return opStore.get(new OperatorKey(f, opNode.getToken()))
        .stream().map(op -> {
          String opFunName = op.getFunName();
          FunctionElement fun = capi.getStorage().getFunction(opFunName);
          if (errorChecks(fun, args.length, finalOpNode.getToken(), opFunName)) {
            List<Element> evaluatedArgs = new ArrayList<>(args.length);
            ASTNode oldPos = interpreter.getPosition();
            for (ASTNode arg : args) {
              if (arg == null) {
                evaluatedArgs.add(Element.UNDEF);
                continue;
              }
              if (arg.isEvaluated()) evaluatedArgs.add(arg.getValue());
              else throw new IllegalStateException("Unevaluated child node. Kernel should have "
                  + "evaluated this beforehand!");
            }
            interpreter.setPosition(oldPos);
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
    for (Entry<OperatorKey, Collection<OperatorValue>> op : opStore.asMap().entrySet()) {
      assert !op.getValue().isEmpty();
      OperatorValue opVal = op.getValue().iterator().next();
      if (!op.getValue().stream().map(OperatorValue::getAssociativity).allMatch(opVal.getAssociativity()::equals)) {
        throw new IllegalStateException("Multiple syntactically identical operators (" + 
            op.getKey().operatorSymbols + ") with different associativities (" +
            op.getValue().stream().map(v -> v.getAssociativity().toString()).distinct().collect(
                Collectors.joining(",")) + ") are not allowed!");
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
    return opStore.keySet().stream().map(k -> k.operatorSymbols)
        .flatMap(o -> Arrays.stream(o.split(OperatorRule.OPERATOR_DELIMITER)))
        .toArray(String[]::new);
  }
}
