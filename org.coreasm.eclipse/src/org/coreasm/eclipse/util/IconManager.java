package org.coreasm.eclipse.util;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Static helper class for loading image files. The class stores each images it
 * has loaded into a Map, from which an image is reused if it is requested a
 * second time. 
 * 
 * Added URL so that we can create images from plugin icons
 * 
 * @author Markus Müller, Tobias
 */
public class IconManager
{
	private static Map<String, ImageDescriptor> DESCR_MAP;
	private static Map<String, Image> IMAGE_MAP;
	private static Map<URL, ImageDescriptor> DESCR_MAP_URL;
	private static Map<URL, Image> IMAGE_MAP_URL;
	
	static {
		DESCR_MAP = new HashMap<String, ImageDescriptor>();
		IMAGE_MAP = new HashMap<String, Image>();
		DESCR_MAP_URL = new HashMap<URL, ImageDescriptor>();
		IMAGE_MAP_URL = new HashMap<URL, Image>();
	}
	
	/**
	 * Returns the ImageDescriptor object of an image given by its filename.
	 * @param filename
	 * @return
	 */
	public static synchronized ImageDescriptor getDescriptor(String filename)
	{
		ImageDescriptor descr = DESCR_MAP.get(filename);
		if (descr == null) {
			descr = ImageDescriptor.createFromFile(IconManager.class, filename);
			if (descr != null)
				DESCR_MAP.put(filename, descr);
		}
		return descr;
	}
	
	/**
	 * Returns the Image object of an image given by its filename.
	 * @param filename
	 * @return
	 */
	public static synchronized Image getIcon(String filename)
	{
		Image icon = IMAGE_MAP.get(filename);
		if (icon == null) {
			ImageDescriptor descr = getDescriptor(filename);
			if (descr != null) {
				icon = descr.createImage();
				IMAGE_MAP.put(filename, icon);
			}
		}
		return icon;
	}
	
	/**
	 * @param filename	The URL of the icon
	 * @return			
	 * 
	 * Returns the ImageDescriptor object of an image given by its filename.
	 */
	public static synchronized ImageDescriptor getDescriptor(URL filename)
	{
		ImageDescriptor descr = DESCR_MAP_URL.get(filename);
		if (descr == null) {
			descr = ImageDescriptor.createFromURL(filename);
			if (descr != null)
				DESCR_MAP_URL.put(filename, descr);
		}
		return descr;
	}
	
	/**
	 * @param filename	The URL of the icon
	 * @return			
	 * 
	 * Returns the ImageDescriptor object of an image given by its filename.
	 */
	public static synchronized Image getIcon(URL filename)
	{
		Image icon = IMAGE_MAP_URL.get(filename);
		if (icon == null) {
			ImageDescriptor descr = getDescriptor(filename);
			if (descr != null) {
				icon = descr.createImage();
				IMAGE_MAP_URL.put(filename, icon);
			}
		}
		return icon;
	}
}
