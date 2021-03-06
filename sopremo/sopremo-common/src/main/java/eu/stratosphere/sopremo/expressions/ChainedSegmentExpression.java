/***********************************************************************************************************************
 *
 * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
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
package eu.stratosphere.sopremo.expressions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import eu.stratosphere.sopremo.pact.SopremoUtil;
import eu.stratosphere.sopremo.type.IJsonNode;

/**
 * Successively evaluates the given expressions on the node.
 */
public class ChainedSegmentExpression extends PathSegmentExpression {
	private final List<EvaluationExpression> expressions;

	/**
	 * Initializes ChainedSegmentExpression.
	 * 
	 * @param expressions
	 */
	public ChainedSegmentExpression(final EvaluationExpression... expressions) {
		this(Arrays.asList(expressions));
	}

	/**
	 * Initializes ChainedSegmentExpression.
	 * 
	 * @param expressions
	 */
	public ChainedSegmentExpression(final Collection<? extends EvaluationExpression> expressions) {
		this.expressions = new ArrayList<EvaluationExpression>(expressions);
	}

	/**
	 * Initializes InputSelection.
	 */
	public ChainedSegmentExpression() {
		this.expressions = new ArrayList<EvaluationExpression>();
	}

	public void addExpression(EvaluationExpression expression) {
		this.expressions.add(expression);
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.stratosphere.sopremo.expressions.PathSegmentExpression#segmentHashCode()
	 */
	@Override
	protected int segmentHashCode() {
		return this.expressions.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * eu.stratosphere.sopremo.expressions.PathSegmentExpression#equalsSameClass(eu.stratosphere.sopremo.expressions
	 * .PathSegmentExpression)
	 */
	@Override
	public boolean equalsSameClass(PathSegmentExpression other) {
		return this.expressions.equals(((ChainedSegmentExpression) other).expressions);
	}

	@Override
	protected IJsonNode evaluateSegment(final IJsonNode node) {
		IJsonNode result = node;
		for (EvaluationExpression expression : this.expressions)
			result = expression.evaluate(result);
		return result;
	}

	/**
	 * Returns the expressions.
	 * 
	 * @return the expressions
	 */
	public List<EvaluationExpression> getExpressions() {
		return this.expressions;
	}

	@Override
	public void appendAsString(final Appendable appendable) throws IOException {
		this.appendInputAsString(appendable);
		SopremoUtil.append(appendable, "[", this.expressions, "]");
	}
}