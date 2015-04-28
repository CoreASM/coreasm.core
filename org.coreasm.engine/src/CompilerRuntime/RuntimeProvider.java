package CompilerRuntime;

public class RuntimeProvider {
	private static Runtime runtime;
	
	public static Runtime getRuntime(){
		return RuntimeProvider.runtime;
	}
	
	public static void setRuntime(Runtime r){
		if(r == null) System.out.println("r is null");
		RuntimeProvider.runtime = r;
		if(RuntimeProvider.runtime == null) System.out.println("r is null 2");
	}
}
