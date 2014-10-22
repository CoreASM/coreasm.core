package CompilerRuntime;

import java.util.concurrent.ExecutionException;

public class Scheduler {
	private CompilerRuntime.Runtime runtime;
	private CompilerRuntime.Rule initRule;
	private CompilerRuntime.SchedulingPolicy policy;
	private java.util.Iterator<java.util.Set<CompilerRuntime.Rule>> schedule;
	
	private CompilerRuntime.UpdateList updateInstructions;
	private CompilerRuntime.UpdateList updateSet;
	
	private int stepCount;
	
	private java.util.Set<CompilerRuntime.Rule> lastSelectedAgents;
	
	private java.util.Set<CompilerRuntime.Rule> agentSet;
	private java.util.Set<CompilerRuntime.Rule> selectedAgentSet;
	
	private java.util.concurrent.ExecutorService threadPool;
	
	
	public Scheduler(CompilerRuntime.Rule initRule, CompilerRuntime.SchedulingPolicy policy){
		runtime = CompilerRuntime.RuntimeProvider.getRuntime();
		if(runtime == null) System.out.println("runtime is null");
		this.initRule = initRule;
		
		this.policy = policy;
		
		updateInstructions = new CompilerRuntime.UpdateList();
		updateSet = new CompilerRuntime.UpdateList();
		
		agentSet = new java.util.HashSet<Rule>();
		selectedAgentSet = new java.util.HashSet<Rule>();
		
		int cores = java.lang.Runtime.getRuntime().availableProcessors();
		threadPool = java.util.concurrent.Executors.newFixedThreadPool(cores);
	}
	
	public CompilerRuntime.UpdateList getUpdateSet(){
		return this.updateSet;
	}
	
	public CompilerRuntime.UpdateList getUpdateInstructions(){
		return this.updateInstructions;
	}
	
	public java.util.Collection<CompilerRuntime.Rule> getAgentSet(){
		return this.agentSet;
	}
	
	public java.util.Collection<CompilerRuntime.Rule> getSelectedAgentSet(){
		return this.selectedAgentSet;
	}
	
	public java.util.Collection<CompilerRuntime.Rule> getLastSelectedAgents(){
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
    	CompilerRuntime.AbstractStorage storage = runtime.getStorage();
    	
    	CompilerRuntime.FunctionElement agentSetFlat = storage
    			.getUniverse(CompilerRuntime.AbstractStorage.AGENTS_UNIVERSE_NAME);
    
    	/*if(stepCount < 1){
    		//first step, add initial agent to the agent set
    		agentSet = new java.util.HashSet<CompilerRuntime.Rule>();
    		agentSet.add(initRule);
    	}
    	else*/{
    		//otherwise retrieve all current agents from the abstract storage
    		agentSet = new java.util.HashSet<CompilerRuntime.Rule>();
    		
    		for(CompilerRuntime.Element agent : ((CompilerRuntime.Enumerable) agentSetFlat).enumerate()){
    			java.util.ArrayList<CompilerRuntime.Element> tmp = new java.util.ArrayList<CompilerRuntime.Element>();
    			tmp.add(agent);
    			
    			CompilerRuntime.Location loc = new CompilerRuntime.Location(CompilerRuntime.AbstractStorage.PROGRAM_FUNCTION_NAME, tmp);
    			
				try {
	    			CompilerRuntime.Element rule = storage.getValue(loc);
					
					if(!rule.equals(CompilerRuntime.Element.UNDEF)){
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
			java.util.Set<CompilerRuntime.Rule> agents = new java.util.HashSet<CompilerRuntime.Rule>();
			for(CompilerRuntime.Update u : inUp){
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
		java.util.ArrayList<CompilerRuntime.Rule> agentsList = new java.util.ArrayList<CompilerRuntime.Rule>(selectedAgentSet);
		
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
