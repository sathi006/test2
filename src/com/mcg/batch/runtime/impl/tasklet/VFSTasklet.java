/**
 * 
 */
package com.mcg.batch.runtime.impl.tasklet;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.mcg.batch.adapter.impl.VFSAdapter;
import com.mcg.batch.adapters.impl.support.vfs.BatchVFSParameters;
import com.mcg.batch.utils.FileOperation;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class VFSTasklet implements Tasklet {
	/**
	 * Logger to be used by this class.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(VFSTasklet.class);

	private VFSAdapter adapter;
	private BatchVFSParameters batchParameters;
	private LinkedHashMap<String, String> batchFileParameters;
	private FileOperation operation;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.step.tasklet.Tasklet#execute(org.
	 * springframework.batch.core.StepContribution,
	 * org.springframework.batch.core.scope.context.ChunkContext)
	 */
	@Override
	public RepeatStatus execute(StepContribution arg0, ChunkContext arg1)
			throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("VFSTasklet.execute() started");
		}
		try {
			adapter.invoke("performOperation", null, operation, batchParameters);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("VFSTasklet.execute() completed");
			}
		}

		return RepeatStatus.FINISHED;
	}

	/**
	 * @return the adapter VFSAdapter
	 */
	public VFSAdapter getAdapter() {
		return adapter;
	}

	/**
	 * @param adapter
	 *            VFSAdapter
	 */
	public void setAdapter(VFSAdapter adapter) {
		this.adapter = adapter;
	}

	/**
	 * @return the batchParameters BatchVFSParameters
	 */
	public BatchVFSParameters getBatchParameters() {
		return batchParameters;
	}

	/**
	 * @return the batchFileParameters LinkedHashMap<String,String>
	 */
	public LinkedHashMap<String, String> getBatchFileParameters() {
		return batchFileParameters;
	}

	/**
	 * @param batchFileParameters
	 *            BatchFileParameters
	 */
	public void setBatchFileParameters(
			LinkedHashMap<String, String> batchFileParameters) {
		this.batchParameters = new BatchVFSParameters(batchFileParameters);
	}

	/**
	 * @return the operation VFSOperation
	 */
	public FileOperation getOperation() {
		return operation;
	}

	/**
	 * @param operation
	 *            VFSOperation
	 */
	public void setOperation(FileOperation operation) {
		this.operation = operation;
	}

}
