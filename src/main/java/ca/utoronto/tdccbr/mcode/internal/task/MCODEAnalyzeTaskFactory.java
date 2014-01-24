package ca.utoronto.tdccbr.mcode.internal.task;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import ca.utoronto.tdccbr.mcode.internal.event.AnalysisCompletedListener;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;

public class MCODEAnalyzeTaskFactory implements TaskFactory {

	private final CyNetwork network;
	private final int analyze;
	private final int resultId;
	private final MCODEAlgorithm alg;
	private final MCODEUtil mcodeUtil;
	private final AnalysisCompletedListener listener;

	public MCODEAnalyzeTaskFactory(final CyNetwork network,
								   final int analyze,
								   final int resultId,
								   final MCODEAlgorithm alg,
								   final MCODEUtil mcodeUtil,
								   final AnalysisCompletedListener listener) {
		this.network = network;
		this.analyze = analyze;
		this.resultId = resultId;
		this.alg = alg;
		this.mcodeUtil = mcodeUtil;
		this.listener = listener;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new MCODEAnalyzeTask(network, analyze, resultId, alg, mcodeUtil, listener));
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return true;
	}
}
