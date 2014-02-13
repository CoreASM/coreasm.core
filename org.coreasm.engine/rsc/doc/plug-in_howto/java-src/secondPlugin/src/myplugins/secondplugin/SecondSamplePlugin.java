package myplugins.secondplugin;

import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.coreasm.engine.absstorage.*;
import java.util.*;

public class SecondSamplePlugin extends Plugin implements VocabularyExtender{

    public static final VersionInfo verInfo = new VersionInfo(0, 2, 1, "alpha");
    
    private Map<String,FunctionElement> functions = null;
    private FunctionElement hashValueFunction;
    private final Set<String> dependencySet;

    public SecondSamplePlugin() {
        super();
        dependencySet = new HashSet<String>();
        dependencySet.add("StringPlugin");
        dependencySet.add("NumberPlugin");
    }

    @Override
    public void initialize() {
        hashValueFunction = new HashValueFunction();
    }

    public VersionInfo getVersionInfo() {
        return verInfo;
    }

    @Override
    public Set<String> getDependencyNames() {
        return this.dependencySet;
    }
	
    public Map<String,FunctionElement> getFunctions() {
        if (functions == null) {
            functions = new HashMap<String,FunctionElement>();
            functions.put("hashValue", hashValueFunction);
        }
        return functions;
    }

    public Set<String> getFunctionNames() {
        return functions.keySet();
    }

    public Map<String,UniverseElement> getUniverses() {
        return Collections.emptyMap();
    }

    public Set<String> getBackgroundNames() {
        return Collections.emptySet();
    }

    public Map<String,BackgroundElement> getBackgrounds() {
        return Collections.emptyMap();
    }

    public Set<String> getUniverseNames() {
        return Collections.emptySet();
    }

}
