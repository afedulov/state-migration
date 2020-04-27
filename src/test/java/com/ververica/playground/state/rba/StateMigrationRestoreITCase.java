/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ververica.playground.state.rba;

import static org.apache.flink.test.checkpointing.utils.StatefulJobSavepointMigrationITCase.ExecutionMode.PERFORM_SAVEPOINT;
import static org.apache.flink.test.checkpointing.utils.StatefulJobSavepointMigrationITCase.ExecutionMode.VERIFY_SAVEPOINT;

import com.ververica.playground.state.StreamingJob;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;
import org.apache.flink.runtime.state.StateBackendLoader;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.runtime.state.memory.MemoryStateBackend;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.test.checkpointing.utils.MigrationTestUtils;
import org.apache.flink.test.checkpointing.utils.SavepointMigrationTestBase;
import org.apache.flink.test.checkpointing.utils.StatefulJobSavepointMigrationITCase;
import org.junit.Test;

/**
 * Migration ITCases for a stateful job. The tests are parameterized to cover
 * migrating for multiple previous Flink versions, as well as for different state backends.
 */
public class StateMigrationRestoreITCase extends SavepointMigrationTestBase {

	public StateMigrationRestoreITCase() throws Exception {
		super();
	}

	@Test
	public void testSavepoint() throws Exception {
		String savepointPath = "src/test/resources/statemigration";
//		String stateBackend = StateBackendLoader.MEMORY_STATE_BACKEND_NAME;
		String stateBackend = StateBackendLoader.ROCKSDB_STATE_BACKEND_NAME;
//		String stateBackend = StateBackendLoader.FS_STATE_BACKEND_NAME;
		int savepointParallelism = 2;
		int restoreParallelism = 2;
//		int restoreParallelism = 3;

		ClusterClient<?> client = miniClusterResource.getClusterClient();
		Tuple2<JobID, String> result =
				execute(PERFORM_SAVEPOINT, stateBackend, savepointPath, savepointParallelism);
		JobID jobId = result.f0;
		Thread.sleep(30000);
		client.cancel(jobId);
		jobId = execute(VERIFY_SAVEPOINT, stateBackend, result.f1, restoreParallelism).f0;
		client.cancel(jobId);
	}

	private Tuple2<JobID, String> execute(
			StatefulJobSavepointMigrationITCase.ExecutionMode executionMode,
			String testStateBackend,
			String savepointPath,
			int parallelism) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setRestartStrategy(RestartStrategies.noRestart());
		env.setParallelism(parallelism);

		switch (testStateBackend) {
			case StateBackendLoader.ROCKSDB_STATE_BACKEND_NAME:
				env.setStateBackend(new RocksDBStateBackend(new MemoryStateBackend()));
				break;
			case StateBackendLoader.MEMORY_STATE_BACKEND_NAME:
				env.setStateBackend(new MemoryStateBackend());
				break;
			case StateBackendLoader.FS_STATE_BACKEND_NAME:
				env.setStateBackend(new FsStateBackend(TEMP_FOLDER.newFolder().toURI()));
				break;
			default:
				throw new UnsupportedOperationException();
		}

		StreamingJob.setupJob(ParameterTool.fromArgs(new String[] {}), env)
			.addSink(new MigrationTestUtils.AccumulatorCountingSink<>());

		if (executionMode == PERFORM_SAVEPOINT) {
			return executeAndSavepoint(
				env,
				savepointPath,
				new Tuple2<>(MigrationTestUtils.AccumulatorCountingSink.NUM_ELEMENTS_ACCUMULATOR, 1));
		} else if (executionMode == VERIFY_SAVEPOINT) {
			JobID jobID = restoreAndExecute(
				env,
				savepointPath,
				new Tuple2<>(MigrationTestUtils.AccumulatorCountingSink.NUM_ELEMENTS_ACCUMULATOR, 1));
			return Tuple2.of(jobID, null);
		} else {
			throw new IllegalStateException("Unknown ExecutionMode " + executionMode);
		}
	}
}