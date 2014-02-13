package myplugins.firstplugin;

import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.plugin.Plugin;

public class FirstSamplePlugin extends Plugin {

    public static final VersionInfo verInfo = new VersionInfo(0, 1, 1, "alpha");
    
    @Override
    public void initialize() {
        // do nothing
    }

    public VersionInfo getVersionInfo() {
        return verInfo;
    }

}
