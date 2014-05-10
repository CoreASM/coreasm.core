package org.coreasm.engine.plugins.list;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementBackgroundElement;
import org.coreasm.engine.absstorage.Signature;
import org.coreasm.engine.plugins.number.NumberBackgroundElement;
import org.coreasm.engine.plugins.number.NumberElement;

/** 
 * Function element providing the 'replicate' function.
 *   
 * @author Michael Stegmaier
 * 
 */
public class ReplicateFunctionElement extends ListFunctionElement {

	/** suggested name for this function */
	public static final String NAME = "replicate";

	protected Signature signature;

	public ReplicateFunctionElement(ControlAPI capi) {
		super(capi);
		signature = new Signature();
		signature.setDomain(
				ElementBackgroundElement.ELEMENT_BACKGROUND_NAME,
				NumberBackgroundElement.NUMBER_BACKGROUND_NAME);
		signature.setRange(ListBackgroundElement.LIST_BACKGROUND_NAME);
	}

	@Override
	public Signature getSignature() {
		return signature;
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		if (!checkArguments(args))
			throw new CoreASMError("Illegal arguments for replicate.");
		NumberElement n = (NumberElement)args.get(1);
		return new ListElement(replicate(args.get(0), n));
	}

	/**
	 * Implementation of a Haskell like replicate function
	 * @param x element to replicate
	 * @param n number of desired replications
	 * @return list
	 */
	private List<? extends Element> replicate(Element x, NumberElement n) {
		ArrayList<Element> result = new ArrayList<Element>();
		for (int i = 0; i < n.getValue(); i++)
			result.add(x);
		return result;
	}

	protected boolean checkArguments(List<? extends Element> args) {
		return (args.size() == 2)
				&& (args.get(1) != null && args.get(1) instanceof NumberElement && ((NumberElement)args.get(1)).isInteger() && ((NumberElement)args.get(1)).getValue() >= 0);
	}
}
