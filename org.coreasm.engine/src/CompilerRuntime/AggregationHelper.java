
package CompilerRuntime;

import java.util.Set;

public interface AggregationHelper {
	public enum Flag {SUCCESSFUL, FAILED};
	
	Set<Location> getLocsWithAnyAction(String ... actions);
	
	Set<Location> getLocsWithActionOnly(String action);
	
	UpdateList getLocUpdates(Location loc);
	
	boolean regularUpdatesAffectsLoc(Location loc);
	
	boolean inconsistentRegularUpdatesOnLoc(Location loc);
	
	void flagUpdate(Update update, Flag flag, UpdateAggregator agg);
	
	void handleInconsistentAggregationOnLocation(Location loc, UpdateAggregator plugin);
	
	void addResultantUpdate(Update update, UpdateAggregator plugin);
}
