/**
 * 
 */
package unbbayes.util;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cc.mallet.types.Dirichlet;

/**
 * @author src-shou.matsumoto
 *
 */
public class DirichletSamplerTest {

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
	 * Test method for {@link unbbayes.util.DirichletSampler#sample()}.
	 */
	@Test
	public void testSample() {
		
		int numSamples = 2000;
		double[] alpha = new double[numSamples];
		
		// randomly generate alpha parameter
		for (int i = 0; i < alpha.length; i++) {
			alpha[i] = (Math.random() * numSamples);
		}
		
		Dirichlet dirMallet = new Dirichlet(Arrays.copyOf(alpha, alpha.length));
		DirichletSampler dirSampler = new DirichletSampler(Arrays.copyOf(alpha, alpha.length));
		
		// calculate normalized alpha
		double sum = 0;
		for (int i = 0; i < alpha.length; i++) {
			sum += alpha[i];
		}
		assertNotEquals(0f, sum, 0.000005);
		for (int i = 0; i < alpha.length; i++) {
			alpha[i] /= sum;
		}
		
		
		// sample mallet, calculate average, count time 
		double [] sumDirichletSample = new double[alpha.length];
		Arrays.fill(sumDirichletSample, 0d);
		double timeBeforeExecution = System.currentTimeMillis();
		for (int i = 0; i < numSamples; i++) {
			double[] dist = dirMallet.nextDistribution();
			// sum distribution, so that later we can check that average is close to normalized alpha
			for (int j = 0; j < dist.length; j++) {
				sumDirichletSample[j] += dist[j];
			}
		}
		// collect execution time
		double executionTimeMallet = System.currentTimeMillis() - timeBeforeExecution;
		
		// check that average is near (not larger than some error margin) normalized alpha
		for (int i = 0; i < sumDirichletSample.length; i++) {
			sumDirichletSample[i] /= numSamples;
			assertEquals("Index = " + i, alpha[i], sumDirichletSample[i], 0.01);
		}
		
		// sample from DirichletSampler, calculate average, count time 
		Arrays.fill(sumDirichletSample, 0d);
		timeBeforeExecution = System.currentTimeMillis();
		for (int i = 0; i < numSamples; i++) {
			double[] dist = dirSampler.sample();
			// sum distribution, so that later we can check that average is close to normalized alpha
			for (int j = 0; j < dist.length; j++) {
				sumDirichletSample[j] += dist[j];
			}
		}
		// collect execution time
		double executionTimeDirichletSampler = System.currentTimeMillis() - timeBeforeExecution;
		

		// check that average is near (not larger than some error margin) normalized alpha
		for (int i = 0; i < sumDirichletSample.length; i++) {
			sumDirichletSample[i] /= numSamples;
			assertEquals("Index = " + i, alpha[i], sumDirichletSample[i], 0.01);
		}
		
		// check that time of DirichletSampler is at least as fast of mallet +- 0.5 seconds
		assertTrue("Time (millils) dirichletsampler/mallet: " + executionTimeDirichletSampler + "/" + executionTimeMallet, executionTimeDirichletSampler <= (executionTimeMallet + 500));
		
	}

}
