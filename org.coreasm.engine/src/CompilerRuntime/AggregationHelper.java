
package CompilerRuntime;

import java.util.Set;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.Update;

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
