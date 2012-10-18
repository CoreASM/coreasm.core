package org.coreasm.engine.plugins.tree;

import java.util.LinkedList;
import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.ScannerInfo;

public class InternalUpdate {
	
	public static final String DEFAULT_ACTION = Update.UPDATE_ACTION;
	
	public final Location loc;
	public final Element value;
	
	
	public InternalUpdate(Location loc, Element value) {
		this.loc = loc;
		this.value = value;
		// System.err.println("Generating an update:\n Loc " + loc + "\n Value " + value);
	}
//
//	public static InternalUpdate updateParent(TreeNodeElement child, TreeNodeElement newParent) {
//		InternalUpdate u = new InternalUpdate(
//				new Location(TreeNodeElement.TREE_PARENT, parlist(child)),
//				newParent);
//		return u;
//	} // updateParent
//	
//	
//	public static InternalUpdate updateFirst(TreeNodeElement parent, TreeNodeElement newFirstChild) {
//		InternalUpdate u = new InternalUpdate(
//				new Location(TreeNodeElement.TREE_FIRST, parlist(parent)),
//				newFirstChild);
//		return u;
//	} // updateFirst
//	
//	public static InternalUpdate updateNext(TreeNodeElement node, TreeNodeElement newSibling) {
//		InternalUpdate u = new InternalUpdate(
//				new Location(TreeNodeElement.TREE_NEXT, parlist(node)),
//				newSibling);
//		return u;
//	} // updateNext
//	
//	public static InternalUpdate updateValue(TreeNodeElement node, Element newValue) {
//		InternalUpdate u = new InternalUpdate(
//				new Location(TreeNodeElement.TREE_VALUE, parlist(node)),
//				newValue);
//		return u;
//	} // updateNext
	
	protected static List<Element> parlist(Element param) {
		List<Element> list = new LinkedList<Element>();
		list.add(param);
		return list;
	} // parlist
	
	public static Location buildValueLocation(TreeNodeElement node) {
		return new Location(TreeNodeElement.TREE_VALUE, parlist(node));
	}
	
	public static Location buildParentLocation(TreeNodeElement node) {
		return new Location(TreeNodeElement.TREE_PARENT, parlist(node));
	}
	
	public static Location buildFirstLocation(TreeNodeElement node) {
		return new Location(TreeNodeElement.TREE_FIRST, parlist(node));
	}
	
	public static Location buildNextLocation(TreeNodeElement node) {
		return new Location(TreeNodeElement.TREE_NEXT, parlist(node));
	}
	
	
	protected static List<Update> processInternalUpdates(List<InternalUpdate> listOfUpdates, 
			Interpreter interpreter, ScannerInfo info) {
		List<Update> result = new LinkedList<Update>();

//		System.err.println("------------------\n processInternalUpdates");
//		int i=1;
		for (InternalUpdate iu : listOfUpdates) {
//			System.err.println("** UPDATE - " + (i++));
//			System.err.println("loc: " + iu.loc);
//			System.err.println("value: " + iu.value);
//			System.err.println("interpreter: " + interpreter.getSelf());
//			System.err.println("scannerinfo: " + info);
			Element value = iu.value == null? Element.UNDEF : iu.value;
			Update update = new Update(iu.loc, value, Update.UPDATE_ACTION, interpreter.getSelf(), info);
			result.add(update);
		}
//		System.err.println("-------------------------------");
		return result;
	} // processInternalUpdates

	
	
}
