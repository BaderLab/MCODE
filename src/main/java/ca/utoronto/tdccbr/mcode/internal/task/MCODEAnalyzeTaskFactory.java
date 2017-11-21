package ca.utoronto.tdccbr.mcode.internal.task;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResultsManager;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;

public class MCODEAnalyzeTaskFactory implements TaskFactory {

	private final CyNetwork network;
	private final int mode;
	private final int resultId;
	private final MCODEAlgorithm alg;
	private final MCODEResultsManager resultsMgr;
	private final MCODEUtil mcodeUtil;

	public MCODEAnalyzeTaskFactory(
			CyNetwork network,
			int mode,
			int resultId,
			MCODEAlgorithm alg,
			MCODEResultsManager resultsMgr,
			MCODEUtil mcodeUtil
	) {
		this.network = network;
		this.mode = mode;
		this.resultId = resultId;
		this.alg = alg;
		this.resultsMgr = resultsMgr;
		this.mcodeUtil = mcodeUtil;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new MCODEAnalyzeTask(network, mode, resultId, alg, resultsMgr, mcodeUtil));
	}

	@Override
	public boolean isReady() {
		return true;
	}
}
