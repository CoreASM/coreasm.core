package CompilerRuntime;

/**
 * Aggregates updates
 * @author Markus Brenner
 *
 */
public interface UpdateAggregator {
	void aggregateUpdates(AggregationHelper ah);
	void compose(PluginCompositionAPI compAPI);
}
