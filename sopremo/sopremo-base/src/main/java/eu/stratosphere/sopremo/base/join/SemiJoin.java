package eu.stratosphere.sopremo.base.join;

import eu.stratosphere.sopremo.pact.JsonCollector;
import eu.stratosphere.sopremo.pact.SopremoCoGroup;
import eu.stratosphere.sopremo.type.ArrayNode;
import eu.stratosphere.sopremo.type.IArrayNode;
import eu.stratosphere.sopremo.type.IJsonNode;
import eu.stratosphere.sopremo.type.IStreamNode;
import eu.stratosphere.sopremo.type.NullNode;

public class SemiJoin extends TwoSourceJoinBase<SemiJoin> {
	/**
	 * Initializes SemiJoin.
	 */
	public SemiJoin() {
		this.setKeyExpressions(0, ALL_KEYS);
		this.setKeyExpressions(1, ALL_KEYS);
	}

	public static class Implementation extends SopremoCoGroup {
		private IArrayNode<IJsonNode> result = new ArrayNode<IJsonNode>(NullNode.getInstance());

		@Override
		protected void coGroup(IStreamNode<IJsonNode> values1, IStreamNode<IJsonNode> values2, JsonCollector out) {
			if (!values2.isEmpty())
				for (final IJsonNode value : values1) {
					this.result.set(0, value);
					out.collect(this.result);
				}
		}
	}
}