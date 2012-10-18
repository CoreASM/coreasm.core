package org.coreasm.eclipse.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorManager {

	protected static Map<RGB,Color> fColorTable = new HashMap<RGB,Color>(10);

	public static void dispose() {
		Iterator<Color> e = fColorTable.values().iterator();
		while (e.hasNext())
			 e.next().dispose();
	}
	
	public static Color getColor(RGB rgb) {
		// The following line is changed by Roozbeh Farahbod, 29-Aug-2006
		// This was causing problem as the color in the table gets disposed
		
		Color color = null; //(Color) fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}
}
