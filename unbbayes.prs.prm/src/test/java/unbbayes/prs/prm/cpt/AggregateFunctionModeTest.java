package unbbayes.prs.prm.cpt;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.prm.AttributeValue;
import unbbayes.prs.prm.IAttributeValue;
import junit.framework.TestCase;

/**
 * Test case for {@link AggregateFunctionMode}
 * @author Shou Matsumoto
 *
 */
public class AggregateFunctionModeTest extends TestCase {

	private AggregateFunctionMode aggregateFunctionToTest;

	/**
	 * Default constructor
	 * @param name
	 */
	public AggregateFunctionModeTest(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.aggregateFunctionToTest = AggregateFunctionMode.newInstance(null);
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link AggregateFunctionMode#evaluate(java.util.Collection)}
	 */
	public final void testEvaluate() {
		
		// return of "evaluate"
		IAttributeValue ret = null;
		
		// test null entry
		ret = this.aggregateFunctionToTest.evaluate(null);
		assertNull("It must return null.", ret);
		
		// test empty entry
		ret = this.aggregateFunctionToTest.evaluate(new ArrayList<IAttributeValue>());
		assertNull("It must return null.", ret);
		
		// argument input
		List<IAttributeValue> inputs = null;
		IAttributeValue auxValue = null;
		

		// test 1 null value element
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue(null);
		inputs = new ArrayList<IAttributeValue>();
		inputs.add(auxValue);
		ret = this.aggregateFunctionToTest.evaluate(inputs);
		assertNull("It must ignore null values.", ret);
		
		// test 1 element
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("true");
		inputs = new ArrayList<IAttributeValue>();
		inputs.add(auxValue);
		ret = this.aggregateFunctionToTest.evaluate(inputs);
		assertNotNull("It must not return null.", ret);
		assertEquals("Value must be the mode", "true", ret.getValue());
		
		// test 2 elements: true true
		inputs = new ArrayList<IAttributeValue>();
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("true");
		inputs.add(auxValue);
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("true");
		inputs.add(auxValue);
		ret = this.aggregateFunctionToTest.evaluate(inputs);
		assertNotNull("It must not return null.", ret);
		assertEquals("Value must be the mode", "true", ret.getValue());
		
		// test 2 elements: true false
		inputs = new ArrayList<IAttributeValue>();
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("true");
		inputs.add(auxValue);
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("false");
		inputs.add(auxValue);
		ret = this.aggregateFunctionToTest.evaluate(inputs);
		assertNotNull("It must not return null.", ret);
		assertEquals("If no mode is present, use the last one (if input is a list).", "false", ret.getValue());
		
		// test 3 elements: true true false
		inputs = new ArrayList<IAttributeValue>();
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("true");
		inputs.add(auxValue);
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("true");
		inputs.add(auxValue);
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("false");
		inputs.add(auxValue);
		ret = this.aggregateFunctionToTest.evaluate(inputs);
		assertNotNull("It must not return null.", ret);
		assertEquals("Value must be mode.", "true", ret.getValue());
		
		// test 3 elements: true false false
		inputs = new ArrayList<IAttributeValue>();
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("true");
		inputs.add(auxValue);
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("false");
		inputs.add(auxValue);
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("false");
		inputs.add(auxValue);
		ret = this.aggregateFunctionToTest.evaluate(inputs);
		assertNotNull("It must not return null.", ret);
		assertEquals("Value must be mode.", "false", ret.getValue());
		
		// test 4 elements: true false, true, false
		inputs = new ArrayList<IAttributeValue>();
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("true");
		inputs.add(auxValue);
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("false");
		inputs.add(auxValue);
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("true");
		inputs.add(auxValue);
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("false");
		inputs.add(auxValue);
		ret = this.aggregateFunctionToTest.evaluate(inputs);
		assertNotNull("It must not return null.", ret);
		assertEquals("If no mode is present, use the last one (if input is a list).", "false", ret.getValue());
		
		// test 4 elements: all true
		inputs = new ArrayList<IAttributeValue>();
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("true");
		inputs.add(auxValue);
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("true");
		inputs.add(auxValue);
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("true");
		inputs.add(auxValue);
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("true");
		inputs.add(auxValue);
		ret = this.aggregateFunctionToTest.evaluate(inputs);
		assertNotNull("It must not return null.", ret);
		assertEquals("Value must be mode.", "true", ret.getValue());
		

		// test 4 elements: 1 true 3 null
		inputs = new ArrayList<IAttributeValue>();
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue("true");
		inputs.add(auxValue);
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue(null);
		inputs.add(auxValue);
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue(null);
		inputs.add(auxValue);
		auxValue = AttributeValue.newInstance(null, null);
		auxValue.setValue(null);
		inputs.add(auxValue);
		ret = this.aggregateFunctionToTest.evaluate(inputs);
		assertNotNull("It must not return null.", ret);
		assertEquals("The mode is true, because null values must be ignored.", "true", ret.getValue());
	}

}
