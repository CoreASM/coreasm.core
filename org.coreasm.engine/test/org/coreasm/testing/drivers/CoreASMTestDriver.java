package org.coreasm.testing.drivers;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.plugin.InitializationFailedException;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.testing.TestCase;
import org.coreasm.testing.value.ParameterProvider;
import org.coreasm.testing.value.StorageProvider;
import org.mockito.Mockito;

public class CoreASMTestDriver {
	public void init(){
		
	}
	
	public InterpreterResult execute(TestCase test, ASTNode root){
		InterpreterResult result = new InterpreterResult();
		Plugin plugin = test.testPlugin;
		
		if(plugin == null || !(plugin instanceof InterpreterPlugin)){
			result.error = new Exception("wrong plugin type");
			result.messages.add("wrong plugin type");
		}
		
		InterpreterPlugin iplugin = (InterpreterPlugin) plugin;
		
		Interpreter mockInterpreter = Mockito.mock(Interpreter.class);
		AbstractStorage mockStorage = Mockito.mock(AbstractStorage.class);
		ControlAPI mockCapi = Mockito.mock(ControlAPI.class);
		Mockito.doReturn(mockStorage).when(mockCapi).getStorage();
		
		try {
			plugin.initialize(mockCapi);
		} catch (InitializationFailedException e1) {
			result.error = e1;
			result.messages.add("Initialization of the plugin failed");
			return result;
		}
		
		try{
			for(int i = 0; i < test.storageValues.size(); i++){
				StorageProvider c = test.storageValues.get(i);
				Mockito.doReturn(c.val).when(mockStorage).getValue(Mockito.eq(c.loc.interpreterValue()));
			}
		}
		catch(Exception e){
			result.error = e;
			result.messages.add("failed setting up the storage");
			return result;
		}
		
		try{
			ASTNode current = root;
			root.setNode(null, null, null);
			do{
				current = iplugin.interpret(mockInterpreter, root);
				if(current.getGrammarRule().equals("JUNIT")){
					ParameterProvider param = test.parameters.get(current.getToken());
					current.setNode(param.interpreterLocation(), param.interpreterUpdate(), param.interpreterElement());
				}
			}while(!root.isEvaluated());
		}
		catch(Exception e){
			result.error = e;
			result.messages.add("interpreter failed evaluating the node");
			return result;
		}
		if(!test.nodeResult.equalsInterpreter(root)){
			System.out.println(root.getLocation());
			System.out.println(root.getUpdates());
			System.out.println(root.getValue());
			result.messages.add("Interpreter result does not match the expected value");
			result.error = new Exception("Mismatching result");
		}
		
		return result;
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}
}
