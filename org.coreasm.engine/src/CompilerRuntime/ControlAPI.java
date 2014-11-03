package CompilerRuntime;

public class ControlAPI {
	public AbstractStorage getStorage(){
		return RuntimeProvider.getRuntime().getStorage();
	}
}
