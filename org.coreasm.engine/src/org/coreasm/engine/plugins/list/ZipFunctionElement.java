package org.coreasm.engine.plugins.list;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Signature;
import org.coreasm.engine.plugins.collection.AbstractListElement;

/** 
 * Function element providing the 'zip' function.
 *   
 * @author Marcel Dausend, Michael Stegmaier
 * 
 */
public class ZipFunctionElement extends ListFunctionElement {

	/** suggested name for this function */
	public static final String NAME = "zip";

	protected Signature signature;

	public ZipFunctionElement(ControlAPI capi) {
		super(capi);
		signature = new Signature();
		signature.setDomain(
				ListBackgroundElement.LIST_BACKGROUND_NAME,
				ListBackgroundElement.LIST_BACKGROUND_NAME);
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
			throw new CoreASMError("Illegal arguments for " + NAME + ".");
		List<? extends Element> list0 = ((AbstractListElement) args.get(0)).getList();
		List<? extends Element> list1 = ((AbstractListElement) args.get(1)).getList();
		return new ListElement(zip(list0, list1));
	}

	/**
	 * Implementation of a Haskell like zip function for two lists.
	 * 
	 * @param list0
	 * @param list1
	 * @return list of tuples
	 */
	private List<? extends Element> zip(List<? extends Element> list0, List<? extends Element> list1) {
		ArrayList<Element> result = new ArrayList<Element>();
		for (int i = 0; i < Math.min(list0.size(), list1.size()); i++) {
			ArrayList<Element> tuple = new ArrayList<Element>();
			tuple.add(list0.get(i));
			tuple.add(list1.get(i));
			result.add(new ListElement(tuple));
		}
		return result;
	}

	protected boolean checkArguments(List<? extends Element> args) {
		return (args.size() == 2) 
				&& (args.get(0) != null && args.get(0) instanceof AbstractListElement)
				&& (args.get(1) != null && args.get(1) instanceof AbstractListElement);
	}
}
