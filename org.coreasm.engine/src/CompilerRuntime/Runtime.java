package CompilerRuntime;

import java.util.Set;

/**
 * An interface providing access to the runtime of the compiled
 * CoreASM specification. The runtime includes an implementation of
 * the abstract storage and a scheduler.
 * @author Markus Brenner
 *
 */
public interface Runtime {
	public AbstractStorage getStorage();
	public Scheduler getScheduler();
	
	public void stopEngine();
	public Set<UpdateAggregator> getAggregators();
	public void error(String string);
	public void error(Exception e);
	public void warning(String string, String msg);
	public int randInt(int max);
	
	public Element getSelf(Thread t);
	public void setSelf(Thread t, Element e);
}
