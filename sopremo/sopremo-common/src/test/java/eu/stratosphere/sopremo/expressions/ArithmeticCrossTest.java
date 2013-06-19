package eu.stratosphere.sopremo.expressions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.stratosphere.sopremo.expressions.ArithmeticExpression.ArithmeticOperator;
import eu.stratosphere.sopremo.expressions.ArithmeticExpression.DivisionEvaluator;
import eu.stratosphere.sopremo.type.BigIntegerNode;
import eu.stratosphere.sopremo.type.DecimalNode;
import eu.stratosphere.sopremo.type.IJsonNode;
import eu.stratosphere.sopremo.type.INumericNode;
import eu.stratosphere.sopremo.type.JsonUtil;

@RunWith(Parameterized.class)
public class ArithmeticCrossTest {
	private final Number left, right, expected;

	private final ArithmeticExpression.ArithmeticOperator operator;

	public ArithmeticCrossTest(final Number left, final ArithmeticExpression.ArithmeticOperator operator,
			final Number right, final Number expected) {
		this.left = left;
		this.right = right;
		this.operator = operator;
		this.expected = expected;
	}

	@Test
	public void shouldPerformTheOperationAndCoercionAsExpected() {
		final ArithmeticExpression arithmetic = new ArithmeticExpression(new InputSelection(0), this.operator,
			new InputSelection(1));
		final IJsonNode result = arithmetic.evaluate(
			JsonUtil.asArray(JsonUtil.OBJECT_MAPPER.valueToTree(this.left),
				JsonUtil.OBJECT_MAPPER.valueToTree(this.right)));

		final IJsonNode expectedNode = JsonUtil.OBJECT_MAPPER.valueToTree(this.expected);
		Assert.assertEquals(
			String.format("%s%s%s/%s%s%s",
				this.left.getClass().getSimpleName(), this.operator, this.right.getClass().getSimpleName(),
				this.left, this.operator, this.right),
			expectedNode.getType(), result.getType());
		// workaround for BigIntegerNode bug
		if (expectedNode instanceof BigIntegerNode)
			Assert.assertEquals(
				String.format("%s%s%s", this.left, this.operator, this.right,
					result, expectedNode), ((BigIntegerNode) expectedNode).getBigIntegerValue(),
				((BigIntegerNode) result).getBigIntegerValue());
		else if (expectedNode instanceof DecimalNode)
			Assert.assertEquals(
				String.format("%s%s%s", this.left, this.operator, this.right,
					result, expectedNode), expectedNode, result);
		else
			Assert
				.assertEquals(
					String.format("%s%s%s", this.left, this.operator, this.right,
						result, expectedNode), this.expected.doubleValue(), ((INumericNode) result).getDoubleValue(),
					0.0001);
	}

	@Parameters
	public static List<Object[]> combinations() {
		// the cross validation is generated by following groovy script

		// def leftNumbers = [ "3", "4L", "BigInteger.valueOf(5)", "3.5f", "4.5d", /new BigDecimal("5.5")/ ]
		// def rightNumbers = [ "6", "7L", "BigInteger.valueOf(8)", "6.5f", "7.5d", /new BigDecimal("8.5")/ ]
		//
		// def unwrap(number) {
		// number.replaceAll(/.*valueOf\((.*)\)/, /$1/).replaceAll(/new .*\("(.*)"\)/, /$1/)
		// }
		//
		// def wrap(number, clazz) {
		// if(Eval.me(number).class == clazz)
		// return number
		// "${clazz.simpleName}.valueOf(${unwrap(number)})"
		// }
		// def opEnums = ["+": "ADDITION", "-": "SUBTRACTION", "/": "DIVISION", "*": "MULTIPLICATION"]
		// def pairs = leftNumbers.inject([]) { list, left ->
		// list + rightNumbers.inject([]) { list2, right ->
		// list2 + ["+": "add", "-": "subtract", "*": "multiply", "/": "divide"].collect { op, opName ->
		// def result = Eval.me("$left $op $right")
		// switch(result.class) {
		// case Long:
		// result += 'L'
		// break
		// case Double:
		// case Float:
		// result = unwrap(left) + "$op" + unwrap(right)
		// break
		// case BigInteger:
		// result = "${wrap(left, BigInteger)}.$opName(${wrap(right, BigInteger)})"
		// break
		// case BigDecimal:
		// if(op == "/")
		// result = "DivisionEvaluator.divideImpl(${wrap(left, BigDecimal)}, ${wrap(right, BigDecimal)})"
		// else result = "${wrap(left, BigDecimal)}.$opName(${wrap(right, BigDecimal)})"
		// break
		// }
		// [left, "ArithmeticOperator.${opEnums[op]}", right, result]
		// }
		// }
		// }.toString().replaceAll(/\[/, "{").replaceAll(/]/, "}")
		return Arrays
			.asList(new Object[][] {
				{ 3, ArithmeticOperator.ADDITION, 6, 9 },
				{ 3, ArithmeticOperator.SUBTRACTION, 6, -3 },
				{ 3, ArithmeticOperator.MULTIPLICATION, 6, 18 },
				{ 3, ArithmeticOperator.DIVISION, 6,
					DivisionEvaluator.divideImpl(BigDecimal.valueOf(3), BigDecimal.valueOf(6)) },
				{ 3, ArithmeticOperator.ADDITION, 7L, 10L },
				{ 3, ArithmeticOperator.SUBTRACTION, 7L, -4L },
				{ 3, ArithmeticOperator.MULTIPLICATION, 7L, 21L },
				{ 3, ArithmeticOperator.DIVISION, 7L,
					DivisionEvaluator.divideImpl(BigDecimal.valueOf(3), BigDecimal.valueOf(7L)) },
				{ 3, ArithmeticOperator.ADDITION, BigInteger.valueOf(8),
					BigInteger.valueOf(3).add(BigInteger.valueOf(8)) },
				{ 3, ArithmeticOperator.SUBTRACTION, BigInteger.valueOf(8),
					BigInteger.valueOf(3).subtract(BigInteger.valueOf(8)) },
				{ 3, ArithmeticOperator.MULTIPLICATION, BigInteger.valueOf(8),
					BigInteger.valueOf(3).multiply(BigInteger.valueOf(8)) },
				{ 3, ArithmeticOperator.DIVISION, BigInteger.valueOf(8),
					DivisionEvaluator.divideImpl(BigDecimal.valueOf(3), BigDecimal.valueOf(8)) },
				{ 3, ArithmeticOperator.ADDITION, 6.5f, 3 + 6.5f },
				{ 3, ArithmeticOperator.SUBTRACTION, 6.5f, 3 - 6.5f },
				{ 3, ArithmeticOperator.MULTIPLICATION, 6.5f, 3 * 6.5f },
				{ 3, ArithmeticOperator.DIVISION, 6.5f, 3 / 6.5f },
				{ 3, ArithmeticOperator.ADDITION, 7.5d, 3 + 7.5d },
				{ 3, ArithmeticOperator.SUBTRACTION, 7.5d, 3 - 7.5d },
				{ 3, ArithmeticOperator.MULTIPLICATION, 7.5d, 3 * 7.5d },
				{ 3, ArithmeticOperator.DIVISION, 7.5d, 3 / 7.5d },
				{ 3, ArithmeticOperator.ADDITION, new BigDecimal("8.5"),
					BigDecimal.valueOf(3).add(new BigDecimal("8.5")) },
				{ 3, ArithmeticOperator.SUBTRACTION, new BigDecimal("8.5"),
					BigDecimal.valueOf(3).subtract(new BigDecimal("8.5")) },
				{ 3, ArithmeticOperator.MULTIPLICATION, new BigDecimal("8.5"),
					BigDecimal.valueOf(3).multiply(new BigDecimal("8.5")) },
				{ 3, ArithmeticOperator.DIVISION, new BigDecimal("8.5"),
					DivisionEvaluator.divideImpl(BigDecimal.valueOf(3), new BigDecimal("8.5")) },
				{ 4L, ArithmeticOperator.ADDITION, 6, 10L },
				{ 4L, ArithmeticOperator.SUBTRACTION, 6, -2L },
				{ 4L, ArithmeticOperator.MULTIPLICATION, 6, 24L },
				{ 4L, ArithmeticOperator.DIVISION, 6,
					DivisionEvaluator.divideImpl(BigDecimal.valueOf(4L), BigDecimal.valueOf(6)) },
				{ 4L, ArithmeticOperator.ADDITION, 7L, 11L },
				{ 4L, ArithmeticOperator.SUBTRACTION, 7L, -3L },
				{ 4L, ArithmeticOperator.MULTIPLICATION, 7L, 28L },
				{ 4L, ArithmeticOperator.DIVISION, 7L,
					DivisionEvaluator.divideImpl(BigDecimal.valueOf(4L), BigDecimal.valueOf(7L)) },
				{ 4L, ArithmeticOperator.ADDITION, BigInteger.valueOf(8),
					BigInteger.valueOf(4L).add(BigInteger.valueOf(8)) },
				{ 4L, ArithmeticOperator.SUBTRACTION, BigInteger.valueOf(8),
					BigInteger.valueOf(4L).subtract(BigInteger.valueOf(8)) },
				{ 4L, ArithmeticOperator.MULTIPLICATION, BigInteger.valueOf(8),
					BigInteger.valueOf(4L).multiply(BigInteger.valueOf(8)) },
				{ 4L, ArithmeticOperator.DIVISION, BigInteger.valueOf(8),
					DivisionEvaluator.divideImpl(BigDecimal.valueOf(4L), BigDecimal.valueOf(8)) },
				{ 4L, ArithmeticOperator.ADDITION, 6.5f, 4L + 6.5f },
				{ 4L, ArithmeticOperator.SUBTRACTION, 6.5f, 4L - 6.5f },
				{ 4L, ArithmeticOperator.MULTIPLICATION, 6.5f, 4L * 6.5f },
				{ 4L, ArithmeticOperator.DIVISION, 6.5f, 4L / 6.5f },
				{ 4L, ArithmeticOperator.ADDITION, 7.5d, 4L + 7.5d },
				{ 4L, ArithmeticOperator.SUBTRACTION, 7.5d, 4L - 7.5d },
				{ 4L, ArithmeticOperator.MULTIPLICATION, 7.5d, 4L * 7.5d },
				{ 4L, ArithmeticOperator.DIVISION, 7.5d, 4L / 7.5d },
				{ 4L, ArithmeticOperator.ADDITION, new BigDecimal("8.5"),
					BigDecimal.valueOf(4L).add(new BigDecimal("8.5")) },
				{ 4L, ArithmeticOperator.SUBTRACTION, new BigDecimal("8.5"),
					BigDecimal.valueOf(4L).subtract(new BigDecimal("8.5")) },
				{ 4L, ArithmeticOperator.MULTIPLICATION, new BigDecimal("8.5"),
					BigDecimal.valueOf(4L).multiply(new BigDecimal("8.5")) },
				{ 4L, ArithmeticOperator.DIVISION, new BigDecimal("8.5"),
					DivisionEvaluator.divideImpl(BigDecimal.valueOf(4L), new BigDecimal("8.5")) },
				{ BigInteger.valueOf(5), ArithmeticOperator.ADDITION, 6,
					BigInteger.valueOf(5).add(BigInteger.valueOf(6)) },
				{ BigInteger.valueOf(5), ArithmeticOperator.SUBTRACTION, 6,
					BigInteger.valueOf(5).subtract(BigInteger.valueOf(6)) },
				{ BigInteger.valueOf(5), ArithmeticOperator.MULTIPLICATION, 6,
					BigInteger.valueOf(5).multiply(BigInteger.valueOf(6)) },
				{ BigInteger.valueOf(5), ArithmeticOperator.DIVISION, 6,
					DivisionEvaluator.divideImpl(BigDecimal.valueOf(5), BigDecimal.valueOf(6)) },
				{ BigInteger.valueOf(5), ArithmeticOperator.ADDITION, 7L,
					BigInteger.valueOf(5).add(BigInteger.valueOf(7L)) },
				{ BigInteger.valueOf(5), ArithmeticOperator.SUBTRACTION, 7L,
					BigInteger.valueOf(5).subtract(BigInteger.valueOf(7L)) },
				{ BigInteger.valueOf(5), ArithmeticOperator.MULTIPLICATION, 7L,
					BigInteger.valueOf(5).multiply(BigInteger.valueOf(7L)) },
				{ BigInteger.valueOf(5), ArithmeticOperator.DIVISION, 7L,
					DivisionEvaluator.divideImpl(BigDecimal.valueOf(5), BigDecimal.valueOf(7L)) },
				{ BigInteger.valueOf(5), ArithmeticOperator.ADDITION, BigInteger.valueOf(8),
					BigInteger.valueOf(5).add(BigInteger.valueOf(8)) },
				{ BigInteger.valueOf(5), ArithmeticOperator.SUBTRACTION, BigInteger.valueOf(8),
					BigInteger.valueOf(5).subtract(BigInteger.valueOf(8)) },
				{ BigInteger.valueOf(5), ArithmeticOperator.MULTIPLICATION, BigInteger.valueOf(8),
					BigInteger.valueOf(5).multiply(BigInteger.valueOf(8)) },
				{ BigInteger.valueOf(5), ArithmeticOperator.DIVISION, BigInteger.valueOf(8),
					DivisionEvaluator.divideImpl(BigDecimal.valueOf(5), BigDecimal.valueOf(8)) },
				{ BigInteger.valueOf(5), ArithmeticOperator.ADDITION, 6.5f, 5 + 6.5f },
				{ BigInteger.valueOf(5), ArithmeticOperator.SUBTRACTION, 6.5f, 5 - 6.5f },
				{ BigInteger.valueOf(5), ArithmeticOperator.MULTIPLICATION, 6.5f, 5 * 6.5f },
				{ BigInteger.valueOf(5), ArithmeticOperator.DIVISION, 6.5f, 5 / 6.5f },
				{ BigInteger.valueOf(5), ArithmeticOperator.ADDITION, 7.5d, 5 + 7.5d },
				{ BigInteger.valueOf(5), ArithmeticOperator.SUBTRACTION, 7.5d, 5 - 7.5d },
				{ BigInteger.valueOf(5), ArithmeticOperator.MULTIPLICATION, 7.5d, 5 * 7.5d },
				{ BigInteger.valueOf(5), ArithmeticOperator.DIVISION, 7.5d, 5 / 7.5d },
				{ BigInteger.valueOf(5), ArithmeticOperator.ADDITION, new BigDecimal("8.5"),
					BigDecimal.valueOf(5).add(new BigDecimal("8.5")) },
				{ BigInteger.valueOf(5), ArithmeticOperator.SUBTRACTION, new BigDecimal("8.5"),
					BigDecimal.valueOf(5).subtract(new BigDecimal("8.5")) },
				{ BigInteger.valueOf(5), ArithmeticOperator.MULTIPLICATION, new BigDecimal("8.5"),
					BigDecimal.valueOf(5).multiply(new BigDecimal("8.5")) },
				{ BigInteger.valueOf(5), ArithmeticOperator.DIVISION, new BigDecimal("8.5"),
					DivisionEvaluator.divideImpl(BigDecimal.valueOf(5), new BigDecimal("8.5")) },
				{ 3.5f, ArithmeticOperator.ADDITION, 6, 3.5f + 6 },
				{ 3.5f, ArithmeticOperator.SUBTRACTION, 6, 3.5f - 6 },
				{ 3.5f, ArithmeticOperator.MULTIPLICATION, 6, 3.5f * 6 },
				{ 3.5f, ArithmeticOperator.DIVISION, 6, 3.5f / 6 },
				{ 3.5f, ArithmeticOperator.ADDITION, 7L, 3.5f + 7L },
				{ 3.5f, ArithmeticOperator.SUBTRACTION, 7L, 3.5f - 7L },
				{ 3.5f, ArithmeticOperator.MULTIPLICATION, 7L, 3.5f * 7L },
				{ 3.5f, ArithmeticOperator.DIVISION, 7L, 3.5f / 7L },
				{ 3.5f, ArithmeticOperator.ADDITION, BigInteger.valueOf(8), 3.5f + 8 },
				{ 3.5f, ArithmeticOperator.SUBTRACTION, BigInteger.valueOf(8), 3.5f - 8 },
				{ 3.5f, ArithmeticOperator.MULTIPLICATION, BigInteger.valueOf(8), 3.5f * 8 },
				{ 3.5f, ArithmeticOperator.DIVISION, BigInteger.valueOf(8), 3.5f / 8 },
				{ 3.5f, ArithmeticOperator.ADDITION, 6.5f, 3.5f + 6.5f },
				{ 3.5f, ArithmeticOperator.SUBTRACTION, 6.5f, 3.5f - 6.5f },
				{ 3.5f, ArithmeticOperator.MULTIPLICATION, 6.5f, 3.5f * 6.5f },
				{ 3.5f, ArithmeticOperator.DIVISION, 6.5f, 3.5f / 6.5f },
				{ 3.5f, ArithmeticOperator.ADDITION, 7.5d, 3.5f + 7.5d },
				{ 3.5f, ArithmeticOperator.SUBTRACTION, 7.5d, 3.5f - 7.5d },
				{ 3.5f, ArithmeticOperator.MULTIPLICATION, 7.5d, 3.5f * 7.5d },
				{ 3.5f, ArithmeticOperator.DIVISION, 7.5d, 3.5f / 7.5d },
				{ 3.5f, ArithmeticOperator.ADDITION, new BigDecimal("8.5"),
					new BigDecimal(3.5f).add(new BigDecimal("8.5")) },
				{ 3.5f, ArithmeticOperator.SUBTRACTION, new BigDecimal("8.5"),
					new BigDecimal(3.5f).subtract(new BigDecimal("8.5")) },
				{ 3.5f, ArithmeticOperator.MULTIPLICATION, new BigDecimal("8.5"),
					new BigDecimal(3.5f).multiply(new BigDecimal("8.5")) },
				{ 3.5f, ArithmeticOperator.DIVISION, new BigDecimal("8.5"),
					DivisionEvaluator.divideImpl(new BigDecimal(3.5f), new BigDecimal("8.5")) },
				{ 4.5d, ArithmeticOperator.ADDITION, 6, 4.5d + 6 },
				{ 4.5d, ArithmeticOperator.SUBTRACTION, 6, 4.5d - 6 },
				{ 4.5d, ArithmeticOperator.MULTIPLICATION, 6, 4.5d * 6 },
				{ 4.5d, ArithmeticOperator.DIVISION, 6, 4.5d / 6 },
				{ 4.5d, ArithmeticOperator.ADDITION, 7L, 4.5d + 7L },
				{ 4.5d, ArithmeticOperator.SUBTRACTION, 7L, 4.5d - 7L },
				{ 4.5d, ArithmeticOperator.MULTIPLICATION, 7L, 4.5d * 7L },
				{ 4.5d, ArithmeticOperator.DIVISION, 7L, 4.5d / 7L },
				{ 4.5d, ArithmeticOperator.ADDITION, BigInteger.valueOf(8), 4.5d + 8 },
				{ 4.5d, ArithmeticOperator.SUBTRACTION, BigInteger.valueOf(8), 4.5d - 8 },
				{ 4.5d, ArithmeticOperator.MULTIPLICATION, BigInteger.valueOf(8), 4.5d * 8 },
				{ 4.5d, ArithmeticOperator.DIVISION, BigInteger.valueOf(8), 4.5d / 8 },
				{ 4.5d, ArithmeticOperator.ADDITION, 6.5f, 4.5d + 6.5f },
				{ 4.5d, ArithmeticOperator.SUBTRACTION, 6.5f, 4.5d - 6.5f },
				{ 4.5d, ArithmeticOperator.MULTIPLICATION, 6.5f, 4.5d * 6.5f },
				{ 4.5d, ArithmeticOperator.DIVISION, 6.5f, 4.5d / 6.5f },
				{ 4.5d, ArithmeticOperator.ADDITION, 7.5d, 4.5d + 7.5d },
				{ 4.5d, ArithmeticOperator.SUBTRACTION, 7.5d, 4.5d - 7.5d },
				{ 4.5d, ArithmeticOperator.MULTIPLICATION, 7.5d, 4.5d * 7.5d },
				{ 4.5d, ArithmeticOperator.DIVISION, 7.5d, 4.5d / 7.5d },
				{ 4.5d, ArithmeticOperator.ADDITION, new BigDecimal("8.5"),
					new BigDecimal(4.5d).add(new BigDecimal("8.5")) },
				{ 4.5d, ArithmeticOperator.SUBTRACTION, new BigDecimal("8.5"),
					new BigDecimal(4.5d).subtract(new BigDecimal("8.5")) },
				{ 4.5d, ArithmeticOperator.MULTIPLICATION, new BigDecimal("8.5"),
					new BigDecimal(4.5d).multiply(new BigDecimal("8.5")) },
				{ 4.5d, ArithmeticOperator.DIVISION, new BigDecimal("8.5"),
					DivisionEvaluator.divideImpl(new BigDecimal(4.5d), new BigDecimal("8.5")) },
				{ new BigDecimal("5.5"), ArithmeticOperator.ADDITION, 6,
					new BigDecimal("5.5").add(BigDecimal.valueOf(6)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.SUBTRACTION, 6,
					new BigDecimal("5.5").subtract(BigDecimal.valueOf(6)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.MULTIPLICATION, 6,
					new BigDecimal("5.5").multiply(BigDecimal.valueOf(6)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.DIVISION, 6,
					DivisionEvaluator.divideImpl(new BigDecimal("5.5"), BigDecimal.valueOf(6)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.ADDITION, 7L,
					new BigDecimal("5.5").add(BigDecimal.valueOf(7L)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.SUBTRACTION, 7L,
					new BigDecimal("5.5").subtract(BigDecimal.valueOf(7L)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.MULTIPLICATION, 7L,
					new BigDecimal("5.5").multiply(BigDecimal.valueOf(7L)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.DIVISION, 7L,
					DivisionEvaluator.divideImpl(new BigDecimal("5.5"), BigDecimal.valueOf(7L)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.ADDITION, BigInteger.valueOf(8),
					new BigDecimal("5.5").add(BigDecimal.valueOf(8)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.SUBTRACTION, BigInteger.valueOf(8),
					new BigDecimal("5.5").subtract(BigDecimal.valueOf(8)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.MULTIPLICATION, BigInteger.valueOf(8),
					new BigDecimal("5.5").multiply(BigDecimal.valueOf(8)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.DIVISION, BigInteger.valueOf(8),
					DivisionEvaluator.divideImpl(new BigDecimal("5.5"), BigDecimal.valueOf(8)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.ADDITION, 6.5f,
					new BigDecimal("5.5").add(new BigDecimal(6.5f)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.SUBTRACTION, 6.5f,
					new BigDecimal("5.5").subtract(new BigDecimal(6.5f)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.MULTIPLICATION, 6.5f,
					new BigDecimal("5.5").multiply(new BigDecimal(6.5f)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.DIVISION, 6.5f,
					DivisionEvaluator.divideImpl(new BigDecimal("5.5"), new BigDecimal(6.5f)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.ADDITION, 7.5d,
					new BigDecimal("5.5").add(new BigDecimal(7.5d)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.SUBTRACTION, 7.5d,
					new BigDecimal("5.5").subtract(new BigDecimal(7.5d)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.MULTIPLICATION, 7.5d,
					new BigDecimal("5.5").multiply(new BigDecimal(7.5d)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.DIVISION, 7.5d,
					DivisionEvaluator.divideImpl(new BigDecimal("5.5"), new BigDecimal(7.5d)) },
				{ new BigDecimal("5.5"), ArithmeticOperator.ADDITION, new BigDecimal("8.5"),
					new BigDecimal("5.5").add(new BigDecimal("8.5")) },
				{ new BigDecimal("5.5"), ArithmeticOperator.SUBTRACTION, new BigDecimal("8.5"),
					new BigDecimal("5.5").subtract(new BigDecimal("8.5")) },
				{ new BigDecimal("5.5"), ArithmeticOperator.MULTIPLICATION, new BigDecimal("8.5"),
					new BigDecimal("5.5").multiply(new BigDecimal("8.5")) },
				{ new BigDecimal("5.5"), ArithmeticOperator.DIVISION, new BigDecimal("8.5"),
					DivisionEvaluator.divideImpl(new BigDecimal("5.5"), new BigDecimal("8.5")) } });
	}
}
