package CompilerRuntime;

public class ControlAPI {
	public AbstractStorage getStorage(){
		return RuntimeProvider.getRuntime().getStorage();
	}
	
	public Interpreter getInterpreter(){
		return new Interpreter();
	}
	
	public void error(CoreASMError c){
		
	}
	
	public int getStepCount(){
		return RuntimeProvider.getRuntime().getScheduler().getStepCount();
	}
}
