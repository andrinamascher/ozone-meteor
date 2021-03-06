/***********************************************************************************************************************
 *
 * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/
package eu.stratosphere.meteor.expression;

import junit.framework.Assert;

import org.junit.Test;

import eu.stratosphere.meteor.MeteorTest;
import eu.stratosphere.sopremo.base.Projection;
import eu.stratosphere.sopremo.expressions.ArithmeticExpression;
import eu.stratosphere.sopremo.expressions.ArithmeticExpression.ArithmeticOperator;
import eu.stratosphere.sopremo.expressions.FunctionCall;
import eu.stratosphere.sopremo.expressions.InputSelection;
import eu.stratosphere.sopremo.expressions.ObjectCreation;
import eu.stratosphere.sopremo.function.JavaMethod;
import eu.stratosphere.sopremo.io.Sink;
import eu.stratosphere.sopremo.io.Source;
import eu.stratosphere.sopremo.operator.SopremoPlan;
import eu.stratosphere.sopremo.type.IJsonNode;

/**
 * @author Arvid Heise
 */
public class FunctionTest extends MeteorTest {

	@Test
	public void testFunctionDefinition() {
		final SopremoPlan actualPlan = this.parseScript("square = fn(x) { x * x };\n" +
			"$input = read from 'file://input.json';\n" +
			"$result = transform $input into { squared: square($input) };\n" +
			"write $result to 'file://output.json'; ");

		final SopremoPlan expectedPlan = new SopremoPlan();
		final Source input = new Source("file://input.json");
		final Projection projection = new Projection().
			withInputs(input).
			withResultProjection(new ObjectCreation(
				new ObjectCreation.FieldAssignment("squared",
					new ArithmeticExpression(new InputSelection(0), ArithmeticOperator.MULTIPLICATION,
						new InputSelection(0)))));
		final Sink sink = new Sink("file://output.json").withInputs(projection);
		expectedPlan.setSinks(sink);

		Assert.assertEquals("unexpectedPlan", expectedPlan, actualPlan);
	}

	@Test
	public void testFunctionImport() throws SecurityException, NoSuchMethodException {
		final SopremoPlan actualPlan = this
			.parseScript("testudf = javaudf('" + this.getClass().getName() + ".udfTest');\n" +
				"$input = read from 'file://input.json';\n" +
				"$result = transform $input into { squared: testudf($input) };\n" +
				"write $result to 'file://output.json'; ");

		final SopremoPlan expectedPlan = new SopremoPlan();
		final Source input = new Source("file://input.json");
		final JavaMethod javaMethod = new JavaMethod("testudf");
		javaMethod.addSignature(this.getClass().getMethod("udfTest", IJsonNode[].class));
		final Projection projection =
			new Projection().
				withInputs(input).
				withResultProjection(new ObjectCreation(
					new ObjectCreation.FieldAssignment("squared", new FunctionCall("testudf", javaMethod,
						new InputSelection(0)))));
		final Sink sink = new Sink("file://output.json").withInputs(projection);
		expectedPlan.setSinks(sink);

		Assert.assertEquals("unexpectedPlan", expectedPlan, actualPlan);
	}

	public static IJsonNode udfTest(final IJsonNode... nodes) {
		return nodes[0];
	}
}
