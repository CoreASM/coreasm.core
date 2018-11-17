package org.coreasm.engine.plugins.operator;


import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

public class EASTNode extends ASTNode {

  private final Element element;
  private final String key;

  /**
   * Creates a new abstract node.
   *  @param pluginName   name of the plugin creating this node
   * @param grammarClass grammar class (will NOT be <code>null</code>)
   * @param grammarRule  grammar rule (will NOT be <code>null</code>)
   * @param token        token
   * @param scannerInfo  information returned by the scanner
   * @param element      evaluated result of this node
   * @param key
   */
  EASTNode(String pluginName, String grammarClass, String grammarRule, String token,
      ScannerInfo scannerInfo, Element element, String key) {
    super(pluginName, grammarClass, grammarRule, token, scannerInfo);
    this.element = element;
    this.key = key;
  }

  public EASTNode(EASTNode e) {
    super(e);
    this.element = e.getVal();
    this.key = e.getKey();
  }

  Element getVal() {
    return this.element;
  }

  String getKey() {
    return this.key;
  }
}
