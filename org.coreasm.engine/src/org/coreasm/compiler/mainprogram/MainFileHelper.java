package org.coreasm.compiler.mainprogram;

import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.mainprogram.statemachine.EngineState;
import org.coreasm.compiler.mainprogram.statemachine.StateMachine;

/**
 * Helper class to build the basic state machine of the main file.
 * @author Markus Brenner
 *
 */
public class MainFileHelper {
	/**
	 * Builds the state machine of the main file
	 * @param sm A state machine object which will be populated
	 */
	public static void populateStateMachine(StateMachine sm){
		//list of engine modes in the original core asm implementation:
		/*
		 * emIdle, 
		 * emInitKernel, 
		 * emLoadingCatalog,
		 * emLoadingCorePlugins,
		 * emParsingHeader,
		 * emLoadingPlugins,
		 * emParsingSpec,
		 * emInitializingState,
		 * emPreparingInitialState,
		 * emStartingStep, 
		 * emSelectingAgents, 
		 * emRunningAgents,
		 * emStepSucceeded, 
		 * emStepFailed, 
		 * emUpdateFailed, 
		 * emAggregation,
		 * emTerminating,
		 * emTerminated,
		 * emError
		 * */
		
		//TODO: insert error handling in the state code

		CodeFragment logCode = new CodeFragment("");
		if (CoreASMCompiler.getEngine().getOptions().logEndOfStep)
			logCode.appendLine("\t\t\t\tSystem.out.println(\"--End of step \" + scheduler.getStepCount() + \"--\");\n");
		if (CoreASMCompiler.getEngine().getOptions().logUpdatesAfterStep)
			logCode.appendLine("\t\t\t\tSystem.out.println(\"Updates at step \" + scheduler.getStepCount() + \":\\n\" + scheduler.getUpdateSet());\n");
		if (CoreASMCompiler.getEngine().getOptions().logStateAfterStep)
			logCode.appendLine("\t\t\t\tSystem.out.println(\"State at step \" + scheduler.getStepCount() + \":\\n\" + storage);\n");
		if (CoreASMCompiler.getEngine().getOptions().logAgentSetAfterStep)
			logCode.appendLine("\t\t\t\tSystem.out.println(\"Last selected agents at step \" + scheduler.getStepCount() + \":\\n\" + scheduler.getLastSelectedAgents());\n");
		
		CodeFragment termCode = new CodeFragment("");
		//if(Main.getEngine().getOptions().terminateOnFailedUpdate)
		//	termCode.appendLine("");
		if(CoreASMCompiler.getEngine().getOptions().terminateOnEmptyUpdate)
			termCode.appendLine("\t\t\t\tif(scheduler.getUpdateSet().size() <= 0){System.out.println(\"Execution terminated: Update set is empty\");System.exit(0);}\n");
		if(CoreASMCompiler.getEngine().getOptions().terminateOnSameUpdate)
			termCode.appendLine("\t\t\t\tif(scheduler.getUpdateSet().equals(prevupdates)){\n"
					+ "\t\t\t\tSystem.out.println(\"Execution terminated: Update set didn't change\");\n"
					+ "\t\t\t\tSystem.exit(0);\n"
					+ "\t\t\t\t}\n"
					+ "\t\t\t\tprevupdates = scheduler.getUpdateSet();\n");
		if(CoreASMCompiler.getEngine().getOptions().terminateOnUndefAgent)
			termCode.appendLine("\t\t\t\tif(scheduler.getAgentSet().size() < 1){\n"
					+ "\t\t\t\tSystem.out.println(\"Execution terminated: No runnable agents\");\n"
					+ "\t\t\t\tSystem.exit(0);\n"
					+ "\t\t\t\t}\n");
		if(CoreASMCompiler.getEngine().getOptions().terminateOnStepCount >= 0)
			termCode.appendLine("\t\t\t\tif(scheduler.getStepCount() >= " + CoreASMCompiler.getEngine().getOptions().terminateOnStepCount + "){\n"
					+ "\t\t\t\tSystem.out.println(\"Execution terminated: Max step count reached\");\n"
					+ "\t\t\t\tSystem.exit(0);\n"
					+ "\t\t\t\t}\n");
	
		EngineState emIdle = new EngineState("emIdle");
		emIdle.appendCode("\t\t\t\tif(lastError != null){\n");
		emIdle.appendCode(sm.makeTransit("emIdle", "emError"));
		emIdle.appendCode("\t\t\t\t} else {\n");
		emIdle.appendCode(sm.makeTransit("emIdle", "emStartingStep"));
		emIdle.appendCode("\t\t\t\t}\n");
		//TODO: handle idle state
		sm.addState(emIdle);
		
		EngineState emStartingStep = new EngineState("emStartingStep");		
		emStartingStep.appendCode("\t\t\t\tscheduler.startStep();\n");
		emStartingStep.appendCode("\t\t\t\ttry{\n");
		emStartingStep.appendCode("\t\t\t\tscheduler.retrieveAgents();\n");
		emStartingStep.appendCode("\t\t\t\t}\n");
		emStartingStep.appendCode("\t\t\t\tcatch(CompilerRuntime.CoreASMCException e){\n");
		emStartingStep.appendCode("\t\t\t\tSystem.out.println(e.toString());\n\t\t\t\tSystem.exit(0);\n");
		emStartingStep.appendCode("\t\t\t\t}\n");
		emStartingStep.appendCode(sm.makeTransit("emStartingStep", "emSelectingAgents"));
		sm.addState(emStartingStep);
		
		//TODO: error state is still a mess
		EngineState emError = new EngineState("emError");
		emError.appendCode("\t\t\t\tSystem.out.println(lastError);\n");
		emError.appendCode("\t\t\t\tif(abortProgram){\n");
		emError.appendCode("System.exit(0);\n");
		emError.appendCode("\t\t\t\t}\n\t\t\t\telse{\n");
		emError.appendCode("\t\t\t\tlastError = null;\n");
		emError.appendCode(sm.makeTransit("emError", "emIdle"));
		emError.appendCode("\t\t\t\t}\n");
		sm.addState(emError);
		
		EngineState emSelectingAgents = new EngineState("emSelectingAgents");
		emSelectingAgents.appendCode("\t\t\t\tif(scheduler.selectAgents()){\n");
		emSelectingAgents.appendCode(sm.makeTransit("emSelectingAgents", "emRunningAgents"));
		emSelectingAgents.appendCode("\n\t\t\t\t}\n\t\t\t\telse{\n");
		emSelectingAgents.appendCode(sm.makeTransit("emSelectingAgents", "emStepSucceeded"));
		emSelectingAgents.appendCode("\t\t\t\t}\n");
		sm.addState(emSelectingAgents);
		
		EngineState emRunningAgents = new EngineState("emRunningAgents");
		emRunningAgents.appendCode("\t\t\t\tif (scheduler.getSelectedAgentSet().size() == 0){\n");
		emRunningAgents.appendCode(sm.makeTransit("emRunningAgents", "emAggregation"));
		emRunningAgents.appendCode("\t\t\t\t}\n\t\t\t\telse {\n");
		emRunningAgents.appendCode("\t\t\t\ttry{\n");
		emRunningAgents.appendCode("\t\t\t\tscheduler.executeAgentPrograms();\n");
		emRunningAgents.appendCode("\t\t\t\t}\n\t\t\t\tcatch(CompilerRuntime.CoreASMCException e){\n");
		emRunningAgents.appendCode("\t\t\t\tSystem.out.println(e.toString());\n\t\t\t\tSystem.exit(0);\n");
		emRunningAgents.appendCode("\t\t\t\t}\n");
		emRunningAgents.appendCode(sm.makeTransit("emRunningAgents", "emAggregation"));
		emRunningAgents.appendCode("\n\t\t\t\t}\n");
		sm.addState(emRunningAgents);
		
		EngineState emAggregation = new EngineState("emAggregation");
		emAggregation.appendCode("\t\t\t\tstorage.aggregateUpdates();\n");
		emAggregation.appendCode("\t\t\t\tif (storage.isConsistent(scheduler.getUpdateSet())) {\n");
		emAggregation.appendCode("\t\t\t\ttry{\n");
		emAggregation.appendCode("\t\t\t\t	storage.fireUpdateSet(scheduler.getUpdateSet());\n");
		emAggregation.appendCode("\t\t\t\t}\n\t\t\t\tcatch(CompilerRuntime.InvalidLocationException e){\n");
		emAggregation.appendCode("\t\t\t\tSystem.out.println(e.toString());\n\t\t\t\tSystem.exit(0);\n");
		emAggregation.appendCode("\t\t\t\t}\n");
		emAggregation.appendCode(sm.makeTransit("emAggregation", "emStepSucceeded"));
		emAggregation.appendCode("\n\t\t\t\t} else{\n");
		emAggregation.appendCode(sm.makeTransit("emAggregation", "emUpdateFailed"));
		emAggregation.appendCode("\t\t\t\t}\n");
		sm.addState(emAggregation);
		
		EngineState emUpdateFailed = new EngineState("emUpdateFailed");
		emUpdateFailed.appendCode("\t\t\t\tif (scheduler.isSingleAgentInconsistent()){\n");
		emUpdateFailed.appendCode(sm.makeTransit("emUpdateFailed", "emStepFailed"));
		emUpdateFailed.appendCode("\t\t\t\t}\n\t\t\t\telse {\n");
		emUpdateFailed.appendCode("\t\t\t\tscheduler.handleFailedUpdate();\n");
		emUpdateFailed.appendCode("\t\t\t\tif (scheduler.agentsCombinationExists()){\n");
		emUpdateFailed.appendCode(sm.makeTransit("emUpdateFailed", "emSelectingAgents"));
		emUpdateFailed.appendCode("\t\t\t\t}\n\t\t\t\telse{\n");
		emUpdateFailed.appendCode(sm.makeTransit("emUpdateFailed", "emStepFailed"));
		emUpdateFailed.appendCode("\t\t\t\t}\n\t\t\t\t}\n");
		sm.addState(emUpdateFailed);
		
		EngineState emStepFailed = new EngineState("emStepFailed");
		emStepFailed.appendCode(logCode);
		emStepFailed.appendCode(termCode);
		if(CoreASMCompiler.getEngine().getOptions().terminateOnFailedUpdate)
			emStepFailed.appendCode("System.out.println(\"Execution terminated: Update failed\");System.exit(0);\n");
		emStepFailed.appendCode(sm.makeTransit("emStepFailed", "emIdle"));
		sm.addState(emStepFailed);
		
		EngineState emStepSucceeded = new EngineState("emStepSucceeded");
		emStepSucceeded.appendCode("\t\t\t\tscheduler.incrementStepCount();\n");
		emStepSucceeded.appendCode(logCode);
		emStepSucceeded.appendCode(termCode);
		emStepSucceeded.appendCode(sm.makeTransit("emStepSucceeded", "emIdle"));
		sm.addState(emStepSucceeded);
		
		
		
	}
}
