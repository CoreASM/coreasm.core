package org.coreasm.compiler.plugins.io.include;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.coreasm.engine.plugins.string.StringElement;

import CompilerRuntime.AggregationHelper;
import CompilerRuntime.AggregationHelper.Flag;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;

import CompilerRuntime.PluginCompositionAPI;
import CompilerRuntime.Rule;

import org.coreasm.engine.absstorage.Update;

import CompilerRuntime.UpdateAggregator;

public class IOAggregator implements UpdateAggregator {
	public static final String PRINT_ACTION = "printAction";
	public static final String OUTPUT_FUNC_NAME = "output";
	public static final Location OUTPUT_FUNC_LOC = new Location(OUTPUT_FUNC_NAME, new ArrayList<Element>());

	@Override
	public void aggregateUpdates(AggregationHelper pluginAgg) {

		// all locations on which contain print actions
		synchronized (this) {
			Set<Location> locsToAggregate = pluginAgg.getLocsWithAnyAction(PRINT_ACTION);
			List<Element> contributingAgents = new ArrayList<Element>();
			
			// for all locations to aggregate
			for (Location l : locsToAggregate) {
				if (l.equals(OUTPUT_FUNC_LOC)) {
					String outputResult = "";
					
					// if regular update affects this location
					if (pluginAgg.regularUpdatesAffectsLoc(l)) {
						pluginAgg.handleInconsistentAggregationOnLocation(l,this);
					} else {
						for (Update update: pluginAgg.getLocUpdates(l)) {
							if (update.action.equals(PRINT_ACTION)) {
								outputResult += update.value.toString() + "\n";
								// flag update aggregation as successful for this update
								pluginAgg.flagUpdate(update, Flag.SUCCESSFUL, this);
								contributingAgents.addAll(update.agents);
							}
						}
					}
					pluginAgg.addResultantUpdate(
							new Update(
									OUTPUT_FUNC_LOC, 
									new	StringElement(outputResult),  
									Update.UPDATE_ACTION,
									new HashSet<Element>(contributingAgents), null
							), 
							this
					);
				}
			}
		}
	}

	@Override
	public void compose(PluginCompositionAPI compAPI) {

		synchronized (this) {
			String outputResult1 = "";
			String outputResult2 = "";
			List<Element> contributingAgents = new ArrayList<Element>();
			
			// First, add all the updates in the second set
			for (Update u: compAPI.getLocUpdates(2, OUTPUT_FUNC_LOC)) {
				if (u.action.equals(PRINT_ACTION)) {
					if (!outputResult2.isEmpty())
						outputResult2 += '\n';
					outputResult2 += u.value.toString();
					contributingAgents.addAll(u.agents);
				}
				else
					compAPI.addComposedUpdate(u, "IOPlugin");
			}
			
			// if the second set does not have a basic update, 
			// add all the updates from the first set as well
			if (!compAPI.isLocUpdatedWithActions(2, OUTPUT_FUNC_LOC, Update.UPDATE_ACTION)) {
				for (Update u: compAPI.getLocUpdates(1, OUTPUT_FUNC_LOC)) {
					if (u.action.equals(PRINT_ACTION)) {
						if (!outputResult1.isEmpty())
							outputResult1 += '\n';
						outputResult1 += u.value.toString();
						contributingAgents.addAll(u.agents);
					}
					else
						compAPI.addComposedUpdate(u, "IOPlugin");
				}
			}
			if (!outputResult1.isEmpty() || !outputResult2.isEmpty()) {
				String outputResult = outputResult1;
				if (outputResult.isEmpty())
					outputResult = outputResult2;
				else if (!outputResult2.isEmpty())
					outputResult = outputResult1 + '\n' + outputResult2;
				compAPI.addComposedUpdate(new Update(OUTPUT_FUNC_LOC, 
						new StringElement(outputResult), 
						PRINT_ACTION, new HashSet<Element>(contributingAgents), null), "IOPlugin");
			}
		}
	}

}
