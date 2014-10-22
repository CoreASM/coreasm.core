package CompilerRuntime;

import java.util.Collection;

public interface EngineAggregationHelper {
	void setUpdateInstructions(UpdateList updates);
	boolean isConsistent();
	Collection<Update> getFailedInstructions();
	Collection<Update> getUnprocessedInstructions();
	UpdateList getResultantUpdates();

}
