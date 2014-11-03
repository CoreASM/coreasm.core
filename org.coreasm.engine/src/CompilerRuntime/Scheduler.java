package CompilerRuntime;

import java.util.concurrent.ExecutionException;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.InvalidLocationException;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.scheduler.SchedulingPolicy;

public class Scheduler {
	private CompilerRuntime.Runtime runtime;
	private CompilerRuntime.Rule initRule;
	private SchedulingPolicy policy;
	private java.util.Iterator<java.util.Set<Element>> schedule;
	
	private CompilerRuntime.UpdateList updateInstructions;
	private CompilerRuntime.UpdateList updateSet;
	
	private int stepCount;
	
	private java.util.Set<Element> lastSelectedAgents;
	
	private java.util.Set<Element> agentSet;
	private java.util.Set<Element> selectedAgentSet;
	
	private java.util.concurrent.ExecutorService threadPool;
	
	
	public Scheduler(CompilerRuntime.Rule initRule, SchedulingPolicy policy){
		runtime = CompilerRuntime.RuntimeProvider.getRuntime();
		if(runtime == null) System.out.println("runtime is null");
		this.initRule = initRule;
		
		this.policy = policy;
		
		updateInstructions = new CompilerRuntime.UpdateList();
		updateSet = new CompilerRuntime.UpdateList();
		
		agentSet = new java.util.HashSet<Element>();
		selectedAgentSet = new java.util.HashSet<Element>();
		
		int cores = java.lang.Runtime.getRuntime().availableProcessors();
		threadPool = java.util.concurrent.Executors.newFixedThreadPool(cores);
	}
	
	public CompilerRuntime.UpdateList getUpdateSet(){
		return this.updateSet;
	}
	
	public CompilerRuntime.UpdateList getUpdateInstructions(){
		return this.updateInstructions;
	}
	
	public java.util.Collection<Element> getAgentSet(){
		return this.agentSet;
	}
	
	public java.util.Collection<Element> getSelectedAgentSet(){
		return this.selectedAgentSet;
	}
	
	public java.util.Collection<Element> getLastSelectedAgents(){
		if(this.lastSelectedAgents != null)
			return java.util.Collections.unmodifiableCollection(lastSelectedAgents);
		else
			return java.util.Collections.emptyList();
	}
    
	public void startStep(){
    	updateInstructions = new CompilerRuntime.UpdateList();
    	updateSet = new CompilerRuntime.UpdateList();
    	
    	agentSet = null;
    	selectedAgentSet.clear();
    }
    
    public void retrieveAgents() throws CoreASMCException{
    	AbstractStorage storage = runtime.getStorage();
    	
    	FunctionElement agentSetFlat = storage
    			.getUniverse(CompilerRuntime.AbstractStorage.AGENTS_UNIVERSE_NAME);
    
    	/*if(stepCount < 1){
    		//first step, add initial agent to the agent set
    		agentSet = new java.util.HashSet<CompilerRuntime.Rule>();
    		agentSet.add(initRule);
    	}
    	else*/{
    		//otherwise retrieve all current agents from the abstract storage
    		agentSet = new java.util.HashSet<Element>();
    		
    		for(Element agent : ((Enumerable) agentSetFlat).enumerate()){
    			java.util.ArrayList<Element> tmp = new java.util.ArrayList<Element>();
    			tmp.add(agent);
    			
    			Location loc = new Location(CompilerRuntime.AbstractStorage.PROGRAM_FUNCTION_NAME, tmp);
    			
				try {
	    			Element rule = storage.getValue(loc);
					
					if(!rule.equals(Element.UNDEF)){
						agentSet.add((CompilerRuntime.Rule)rule);
						((CompilerRuntime.Rule) rule).setAgent(agent);
					}
				} catch (InvalidLocationException e) {
					throw new CompilerRuntime.CoreASMCException("invalid agent found");
				}
    		}
    	}
    	
    	schedule = policy.getNewSchedule(policy, agentSet);
    	
    }
    
    public boolean selectAgents(){
    	if(agentsCombinationExists()){
    		selectedAgentSet = schedule.next();
    		lastSelectedAgents = java.util.Collections.unmodifiableSet(selectedAgentSet);
    		return true;
    	}
    	else{
    		selectedAgentSet = java.util.Collections.emptySet();
    		lastSelectedAgents = selectedAgentSet;
    		return false;
    	}
    }
    public void handleFailedUpdate(){
    	//dont do anything
    }
	public boolean isSingleAgentInconsistent(){
		CompilerRuntime.UpdateList inUp = runtime.getStorage().getLastInconsistentUpdate();
		boolean result = false;
		
		if(inUp != null){
			java.util.Set<Element> agents = new java.util.HashSet<Element>();
			for(Update u : inUp){
				if(u.agents != null){			
					agents.addAll(u.agents);
				}
			}
			
			if(agents.size() == 1)
				result = true;
		}
		
		return result;
	}
	
    public boolean agentsCombinationExists(){
    	return schedule.hasNext();
    }
    
	public void executeAgentPrograms() throws CompilerRuntime.CoreASMCException{
		java.util.ArrayList<CompilerRuntime.Rule> agentsList = new java.util.ArrayList<Rule>();//>(selectedAgentSet);
		for(Element e : selectedAgentSet){
			agentsList.add((Rule) e);
		}
		
		
		for(CompilerRuntime.Rule r : agentsList){
			r.clearResults();
			r.initRule(new java.util.ArrayList<CompilerRuntime.RuleParam>(), null);
		}
		
		java.util.List<java.util.concurrent.Future<CompilerRuntime.RuleResult>> tmpUpdateList = null;
				
		try {
			tmpUpdateList = threadPool.invokeAll(agentsList);
		} catch (InterruptedException e) {
			throw new CompilerRuntime.CoreASMCException("Error: rule execution"
					+ " was interrupted by " + e.getMessage());
		}
		
		updateInstructions = new CompilerRuntime.UpdateList();
		
		for(java.util.concurrent.Future<CompilerRuntime.RuleResult> f : tmpUpdateList){			
			try {
				updateInstructions.addAll(f.get().updates);
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new CompilerRuntime.CoreASMCException("Rule execution error: " + e.getMessage());
			} catch(ExecutionException e){
				e.printStackTrace();
				throw new CompilerRuntime.CoreASMCException("Rule execution error: " + e.getMessage());
			}
		}
	}
	
    public CompilerRuntime.Rule getInitAgent(){
    	return initRule;
    }
    
    public void setStepCount(int count){
    	this.stepCount = count;
    }
    
    public int getStepCount(){
    	return this.stepCount;
    }
    public void incrementStepCount(){
    	this.stepCount++;
    }
}
