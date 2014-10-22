package org.coreasm.compiler;

/**
 * This enum describes, what a compiled piece of code will do.
 * <p>
 * BASIC is the only special code here, as it will not create
 * a piece of code, but one or more library entries.
 * <p>
 * Other code types will place objects corresponding to the
 * code type on the stack, after the code has been executed.
 * In case of mix code types, the object represented by the
 * first letter will be the first object to be pushed on the stack.
 * <p>
 * A piece of LR code will leave an update as the topmost entry
 * and a location right below it on the stack
 * @author Markus Brenner
 *
 */
public enum CodeType{/**
 * right-hand side code, will leave the result of an expression
 * on the stack
 */
R, /**
 * left-hand side code, will leave a location on the stack
 */
L, /**
 * rule code, will produce an update and leave it on the stack
 */
U, /**
 * left and right hand side code, will leave a location and the result
 * of an expression on the stack
 */
LR, /**
 * left hand side and rule code, will leave a location and an update on
 * the stack
 */
LU, /**
 * rule and right hand side code, will leave an update and the result of
 * an expression on the stack
 */
UR, /**
 * all three types combined, will leave a location, an update and the result
 * of an expression on the stack
 */
LUR, /**
 * basic code used for the compilation of root objects in the parse tree.
 * will create a library entry or produce similar results, but won't directly
 * return code
 */
BASIC
}