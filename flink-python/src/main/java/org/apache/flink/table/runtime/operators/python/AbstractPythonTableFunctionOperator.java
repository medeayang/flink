/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.runtime.operators.python;

import org.apache.flink.annotation.Internal;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.table.functions.python.PythonEnv;
import org.apache.flink.table.functions.python.PythonFunctionInfo;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.util.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * @param <IN>     Type of the input elements.
 * @param <OUT>    Type of the output elements.
 * @param <UDTFIN> Type of the UDTF input type.
 */
@Internal
public abstract class AbstractPythonTableFunctionOperator<IN, OUT, UDTFIN>
	extends AbstractStatelessFunctionOperator<IN, OUT, UDTFIN> {

	private static final long serialVersionUID = 1L;

	/**
	 * The Python {@link TableFunction} to be executed.
	 */
	protected final PythonFunctionInfo tableFunction;

	public AbstractPythonTableFunctionOperator(
		Configuration config,
		PythonFunctionInfo tableFunction,
		RowType inputType,
		RowType outputType,
		int[] udtfInputOffsets) {
		super(config, inputType, outputType, udtfInputOffsets);
		this.tableFunction = Preconditions.checkNotNull(tableFunction);
	}

	@Override
	public void open() throws Exception {
		List<RowType.RowField> udtfOutputDataFields = new ArrayList<>(
			outputType.getFields().subList(inputType.getFieldCount(), outputType.getFieldCount()));
		userDefinedFunctionOutputType = new RowType(udtfOutputDataFields);
		super.open();
	}

	@Override
	public PythonEnv getPythonEnv() {
		return tableFunction.getPythonFunction().getPythonEnv();
	}

	/**
	 * The received udtf execution result is a finish message when it is a byte with value 0x00.
	 */
	protected boolean isFinishResult(byte[] rawUdtfResult) {
		return rawUdtfResult.length == 1 && rawUdtfResult[0] == 0x00;
	}
}
