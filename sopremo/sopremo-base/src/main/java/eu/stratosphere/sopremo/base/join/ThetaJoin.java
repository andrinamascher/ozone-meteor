package eu.stratosphere.sopremo.base.join;

import eu.stratosphere.sopremo.expressions.BooleanExpression;
import eu.stratosphere.sopremo.expressions.ComparativeExpression;
import eu.stratosphere.sopremo.expressions.InputSelection;
import eu.stratosphere.sopremo.operator.InputCardinality;
import eu.stratosphere.sopremo.pact.JsonCollector;
import eu.stratosphere.sopremo.pact.SopremoCross;
import eu.stratosphere.sopremo.type.ArrayNode;
import eu.stratosphere.sopremo.type.BooleanNode;
import eu.stratosphere.sopremo.type.IArrayNode;
import eu.stratosphere.sopremo.type.IJsonNode;

@InputCardinality(min = 2, max = 2)
public class ThetaJoin extends TwoSourceJoinBase<ThetaJoin> {
	private BooleanExpression condition = new ComparativeExpression(new InputSelection(0),
		ComparativeExpression.BinaryOperator.EQUAL, new InputSelection(1));

	public BooleanExpression getCondition() {
		return this.condition;
	}

	public void setCondition(BooleanExpression condition) {
		if (condition == null)
			throw new NullPointerException("condition must not be null");

		this.condition = condition;
	}

	public ThetaJoin withCondition(BooleanExpression condition) {
		this.setCondition(condition);
		return this;
	}

	public static class Implementation extends SopremoCross {
		private transient final IArrayNode<IJsonNode> inputs = new ArrayNode<IJsonNode>();

		private BooleanExpression condition;

		@Override
		protected void cross(IJsonNode value1, IJsonNode value2, JsonCollector out) {
			this.inputs.set(0, value1);
			this.inputs.set(1, value2);
			if (this.condition.evaluate(this.inputs) == BooleanNode.TRUE)
				out.collect(this.inputs);
		}
	}
}