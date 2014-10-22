package CompilerRuntime;


public interface AbstractStorage extends State{
	/** 'program' function name */
	public static final String PROGRAM_FUNCTION_NAME = "program";
	/** 'Agents' universe name */
	public static final String AGENTS_UNIVERSE_NAME = "Agents";
	/** 'functionElement' function name */
	public static final String FUNCTION_ELEMENT_FUNCTION_NAME = "functionElement";
	/** 'universeElement' function name */
	public static final String UNIVERSE_ELEMENT_FUNCTION_NAME = "universeElement";
	
	public void initAbstractStorage(CompilerRuntime.Rule initRule);
	
	public void fireUpdateSet(UpdateList updates) throws InvalidLocationException;
	
	public UpdateList performAggregation(UpdateList updateInsts);
	
	public void aggregateUpdates();
	
	public UpdateList compose(UpdateList updateSet1, UpdateList updateSet2);
	
	public boolean isConsistent(UpdateList updateSet);
	
	public Element getNewElement();
	
	public void pushState();
	
	public void popState();
	
	public void apply(UpdateList u);
	
	public void clearState();
	
	public UpdateList getLastInconsistentUpdate();
}
