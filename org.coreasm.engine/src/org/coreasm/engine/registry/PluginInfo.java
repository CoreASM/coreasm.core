/**
 * 
 */
package org.coreasm.engine.registry;


/**
 * Structure to hold plugin information. 
 * 
 * @author Roozbeh Farahbod
 * 
 */
public class PluginInfo {
	public final String author;
	public final String version;
	public final String description;
	
	public PluginInfo(String author, String version, String description) {
		this.author = author;
		this.version = version;
		this.description = description;
	}
}
