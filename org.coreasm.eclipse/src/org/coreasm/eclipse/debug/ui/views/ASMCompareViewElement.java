package org.coreasm.eclipse.debug.ui.views;

public class ASMCompareViewElement {
	private String name;
	private String[] values;
	private boolean difference;
	
	public ASMCompareViewElement(String name, String[] values) {
		super();
		this.name = name;
		this.values = values;
		difference = false;
		for (String value1 : values) {
			for (String value2 : values) {
				if (value1 != null && !value1.equals(value2)) {
					difference = true;
					return;
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public String[] getValues() {
		return values;
	}
	
	public boolean hasDifference() {
		return difference;
	}
}
