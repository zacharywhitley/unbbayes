package io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class FileToCommandLineArgumentWrapperTest {

	public FileToCommandLineArgumentWrapperTest() {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link io.FileToCommandLineArgumentWrapper#buildArgs()}.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	@Test
	public final void testBuildArgs() throws IOException, URISyntaxException, InterruptedException {
		Debug.setDebug(true);
		
		FileToCommandLineArgumentWrapper wrapper = new FileToCommandLineArgumentWrapper();
		
		wrapper.readWrapperFile(new File(getClass().getResource("ContingencyMatrixUserActivitySimulator.template").toURI()));
		
		Map<String, String> properties = wrapper.getProperties();
		assertNotNull(properties);
		
		assertTrue(properties.containsKey("i"));
		assertEquals("inputRCP15", properties.get("i"));
		assertTrue(properties.containsKey("o"));
		assertEquals("RCP15_data", properties.get("o"));
		assertTrue(properties.containsKey("usr"));
		assertEquals("1000", properties.get("usr"));
		assertTrue(properties.containsKey("total"));
		assertEquals("3388", properties.get("total"));
		assertTrue(properties.containsKey("org"));
		assertEquals("5", properties.get("org"));
		assertTrue(properties.containsKey("prefix"));
		assertEquals("", properties.get("prefix"));
		assertTrue(properties.containsKey("suffix"));
		assertEquals(".csv", properties.get("suffix"));
		assertTrue(properties.containsKey("varSep"));
		assertEquals("", properties.get("varSep"));
		assertTrue(properties.containsKey("target"));
		assertEquals("Other,LineManagement", properties.get("target"));
		assertTrue(properties.containsKey("targetSep"));
		assertEquals("_", properties.get("targetSep"));
		assertTrue(properties.containsKey("discrete"));
		assertEquals("", properties.get("discrete"));
		assertTrue(properties.containsKey("continuous"));
		assertEquals("det001a,det001b,det001c,det002a,det002b,det002c,det003a,det003b,det003c,det004a,det004b,det004c,det005a,det005b,det005c,det006a,det006b,det006c,det007a,det007b,det007c,det008a,det008b,det008c,det009a,det009b,det009c,det010a,det010b,det010c,det011a,det011b,det011c,det012a,det012b,det012c,det013a,det013b,det013c,det014a,det015a,det016a,det016b,det016c,det016d,det016e,det017a,det017b,det017c,det017d,det018a,det018b,det018c,det018d,det018e,det019a,det019b,det019c,det019d,det020a,det020b,det020c,det020d,det020e,det021a,det021b,det021c,det021d,det022a,det022b,det022c,det022d,det022e,det023a,det023b,det023c,det023d,det024a,det025a,det026a,det027a,det028a,det029a,det030a,det031a,det032a,det033a,det034a,det035a,det036a,det037a,det038a,det039a,det040a,det041a,det042a,det043a,det044a,det045a,det046a,det046b,det046c,det046d,det047a,det047b,det047c,det047d,det048a,det048b,det048c,det048d,det049a,det049b,det049c,det049d,det050a,det050b,det050c,det050d,det051a,det051b,det051c,det051d,det052a,det052b,det052c,det052d,det053a,det053b,det053c,det053d,det054a,det055a,det056a,det057a,det058a,det059a,det060a,det061a,det062a,det063a,det064a", properties.get("continuous"));
		assertTrue(properties.containsKey("inputTimeSlices"));
		assertEquals("Week48,Week49,Week50,Week51", properties.get("inputTimeSlices"));
		assertTrue(properties.containsKey("partialInputTimeSlices"));
		assertEquals("Week9,Week10,Week11,Week12,Week13,Week14,Week15,Week16,Week17,Week18,Week19,Week20,Week21,Week22,Week23,Week24,Week25,Week26,Week27,Week28,Week29,Week30,Week31,Week32,Week33,Week34,Week35,Week36,Week37,Week38,Week39,Week40,Week41,Week42,Week43,Week44,Week45,Week46,Week47", properties.get("partialInputTimeSlices"));
		assertTrue(properties.containsKey("allTimeSlices"));
		assertEquals("Week9,Week10,Week11,Week12", properties.get("allTimeSlices"));
		assertTrue(properties.containsKey("virtCountCoefs"));
		assertEquals("1,1,1,1", properties.get("virtCountCoefs"));
		assertTrue(properties.containsKey("initCount"));
		assertEquals("1", properties.get("initCount"));
		assertTrue(properties.containsKey("inputDist"));
		assertEquals(".25,.25,.25,.25", properties.get("inputDist"));
		assertTrue(properties.containsKey("partialInputDist"));
		assertEquals("0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026,0.025641026", properties.get("partialInputDist"));
		assertTrue(properties.containsKey("numeric"));
		assertNull(properties.get("numeric"));
		assertNull(properties.get("d"));
		
		String args = wrapper.buildArgs();
		
		
		assertNotNull(args);
		assertTrue(args.contains("-d"));
		assertTrue(args.contains("-numeric"));
		
	}

}
