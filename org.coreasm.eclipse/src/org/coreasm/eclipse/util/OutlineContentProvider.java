package org.coreasm.eclipse.util;

import java.net.URL;

public interface OutlineContentProvider {
	public URL getImage(String grammarRule);
	public URL getGroupImage(String group);
	public String getGroup(String grammarRule);
	public String getSuffix(String grammarRule, String description);
	public boolean hasDeclarations(String grammarRule);
}
