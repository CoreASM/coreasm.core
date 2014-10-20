package org.coreasm.eclipse.editors.outlining;

import java.net.URL;

import org.coreasm.eclipse.util.OutlineContentProvider;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.FrameworkUtil;

public class StandardOutlineContentProvider implements OutlineContentProvider {

	@Override
	public URL getImage(String grammarRule) {
		if ("CoreASM".equals(grammarRule) || "CoreModule".equals(grammarRule))
			return FileLocator.find(FrameworkUtil.getBundle(getClass()), new Path("/icons/icon16.gif"), null);
		if ("UseClauses".equals(grammarRule))
			return FileLocator.find(FrameworkUtil.getBundle(getClass()), new Path("/icons/editor/package.gif"), null);
		if ("Initialization".equals(grammarRule))
			return FileLocator.find(FrameworkUtil.getBundle(getClass()), new Path("/icons/editor/init.gif"), null);
		if ("RuleDeclaration".equals(grammarRule))
			return FileLocator.find(FrameworkUtil.getBundle(getClass()), new Path("/icons/editor/rule.gif"), null);
		if ("Signature".equals(grammarRule))
			return FileLocator.find(FrameworkUtil.getBundle(getClass()), new Path("/icons/editor/sign.gif"), null);
		if ("PropertyOption".equals(grammarRule))
			return FileLocator.find(FrameworkUtil.getBundle(getClass()), new Path("/icons/editor/option.gif"), null);
		if ("Include".equals(grammarRule))
			return FileLocator.find(FrameworkUtil.getBundle(getClass()), new Path("/icons/editor/module.gif"), null);
		return null;
	}

	@Override
	public URL getGroupImage(String group) {
		if ("Used Plugins".equals(group))
			return FileLocator.find(FrameworkUtil.getBundle(getClass()), new Path("/icons/editor/packagefolder.gif"), null);
		return null;
	}

	@Override
	public String getGroup(String grammarRule) {
		if ("UseClauses".equals(grammarRule))
			return "Used Plugins";
		if ("Initialization".equals(grammarRule))
			return "Initialization";
		if ("RuleDeclaration".equals(grammarRule))
			return "Rule Declarations";
		if ("Signature".equals(grammarRule))
			return "Signatures";
		if ("PropertyOption".equals(grammarRule))
			return "Options";
		if ("Include".equals(grammarRule))
			return "Included Files";
		return null;
	}

	@Override
	public String getSuffix(String grammarRule, String description) {
		if ("CoreModule".equals(grammarRule))
			return "CoreModule";
		if ("PropertyOption".equals(grammarRule))
			return description;
		if ("Include".equals(grammarRule))
			return description;
		return null;
	}

	@Override
	public boolean hasChildren(String grammarRule) {
		return "CoreASM".equals(grammarRule) || "CoreModule".equals(grammarRule);
	}
}
