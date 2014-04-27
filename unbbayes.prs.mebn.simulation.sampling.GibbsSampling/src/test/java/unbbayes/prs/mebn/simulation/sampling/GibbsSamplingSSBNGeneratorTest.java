package unbbayes.prs.mebn.simulation.sampling;

import java.io.File;
import java.util.Collections;
import java.util.Random;

import junit.framework.TestCase;
import unbbayes.TextModeRunner;
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.exception.CategoricalStateDoesNotExistException;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator;
import unbbayes.simulation.montecarlo.sampling.IMonteCarloSampling;
import unbbayes.simulation.montecarlo.sampling.SampleGenerationListener;
import unbbayes.simulation.sampling.GibbsSampling;

public class GibbsSamplingSSBNGeneratorTest extends TestCase {

	public GibbsSamplingSSBNGeneratorTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Generate a sequence of SSBN, get the execution time in Junction Tree algorithm, 
	 * keep the marginals, run the gibbs sampler until we approximate to the marginals
	 * we got from Junction Tree, and keep the execution time of gibbs sampler.
	 * The format of trace is: 
	 * <pre>
	 * numEntities , timeJT , timeGibbs
	 * </pre>
	 */
	public final void testGenerateSSBN() {
		// declare basic parameters
		long seed = System.currentTimeMillis();
		Random random = new Random(seed);
		
		// a difference smaller than this value in marginal probability (compared to JT) will make the gibbs sampler to stop sampling
		final float marginalDiff = .015f;
		
		int initialInstanceNum = 5;	// how many entity instances to use in performance comparison in initial iteration
		int maxInstanceNum = 20;	// maximum entity instances to use in performance comparison 
		
		
		System.out.println("*******************************");
		System.out.println("Seed = " + seed);
		System.out.println("*******************************\n");
		
		// load mebn
		MultiEntityBayesianNetwork mebn = null;	// the netToCompare to be used
		try {
			mebn = (MultiEntityBayesianNetwork) UbfIO.getInstance().load(new File(getClass().getClassLoader().getResource("2Groups.ubf").toURI()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		assertNotNull(mebn);
		
		// instantiate SSBN generator that uses Junction tree
		// initialize laskey algorithm using default parameter values
		LaskeyAlgorithmParameters param = new LaskeyAlgorithmParameters();
		param.setParameterValue(LaskeyAlgorithmParameters.DO_INITIALIZATION, "true");
		param.setParameterValue(LaskeyAlgorithmParameters.DO_BUILDER, "true"); 
		param.setParameterValue(LaskeyAlgorithmParameters.DO_PRUNE, "true"); 
		param.setParameterValue(LaskeyAlgorithmParameters.DO_CPT_GENERATION, "true"); 
		LaskeySSBNGenerator laskey = new LaskeySSBNGenerator(param);
		
		// instantiate SSBN generator that uses gibbs sampler
		GibbsSamplingSSBNGenerator gibbs = new GibbsSamplingSSBNGenerator();
		
		// extract the entity where we'll insert instances
		ObjectEntity entity = mebn.getObjectEntityContainer().getObjectEntityByName("MyEntity");
		assertNotNull(entity);
		
		// before starting performance comparison, create initial entity instances
		for (int i = 0; i < initialInstanceNum; i++) {
			// create a new entity instance
			ObjectEntityInstance instance = null;
			try {
				instance = entity.addInstance(entity.getName() + "_" + i);
				mebn.getObjectEntityContainer().addEntityInstance(instance);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			assertNotNull(instance);
			assertNotNull(mebn.getObjectEntityContainer().getEntityInstanceByName(instance.getName()));
			mebn.getNamesUsed().add(instance.getName());
		}
		
		// instantiate FOL database
		PowerLoomKB knowledgeBase = PowerLoomKB.getNewInstanceKB();
		// fill knowledgeBase with elements of mebn, including the entity instances we just created
		new TextModeRunner().createKnowledgeBase(knowledgeBase, mebn);	
		
		// extract the node where we'll add finding
		ResidentNode parentResidentNode = mebn.getDomainResidentNode("Parent");
		assertNotNull(parentResidentNode);
		
		// extract the node where we'll query
		ResidentNode childResidentNode = mebn.getDomainResidentNode("Child");
		assertNotNull(childResidentNode);
		
		// start iteration for performance evaluation. Iterate over number of instances of MyEntity (the more instances, the more parents)
		for (int instanceNum = initialInstanceNum; instanceNum < maxInstanceNum; instanceNum++) {
			System.out.print(mebn.getObjectEntityContainer().getListEntityInstances().size());
			
			
			// create a new entity instance for this iteration
			ObjectEntityInstance instance = null;
			try {
				instance = entity.addInstance(entity.getName() + "_" + instanceNum);
				mebn.getObjectEntityContainer().addEntityInstance(instance);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			assertNotNull(instance);
			assertNotNull(mebn.getObjectEntityContainer().getEntityInstanceByName(instance.getName()));
			mebn.getNamesUsed().add(instance.getName());
			
			// add entity instance to knowledge base
			knowledgeBase.insertEntityInstance(instance); 
			
			// randomly add finding
//			ObjectEntityInstance findingArg = new ObjectEntityInstance(entity.getName() + "_" + random.nextInt(instanceNum+1) , entity);
			ObjectEntityInstance findingArg = entity.getInstanceByName(entity.getName() + "_" + random.nextInt(instanceNum+1));
			assertNotNull(findingArg);
			
			CategoricalStateEntity state = null;
			try {
				state = mebn.getCategoricalStatesEntityContainer().getCategoricalState("State0");
			} catch (CategoricalStateDoesNotExistException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} 
			assertNotNull(state);
			RandomVariableFinding finding = new RandomVariableFinding(parentResidentNode, new ObjectEntityInstance[]{findingArg}, state, mebn);
			parentResidentNode.addRandomVariableFinding(finding);
			knowledgeBase.insertRandomVariableFinding(finding); 
			
			// prepare the argument of query
			OrdinaryVariable ov = parentResidentNode.getOrdinaryVariableByIndex(0);
			assertNotNull(ov);
			OVInstance queryArg = OVInstance.getInstance(ov, LiteralEntityInstance.getInstance(entity.getName() + "_" + random.nextInt(instanceNum+1), ov.getValueType()));
			Query query = new Query(childResidentNode, Collections.singletonList(queryArg));
			
			// generate ssbn in JT
			long timeBeforeExecution = System.currentTimeMillis();
			SSBN ssbn;
			try {
				ssbn = laskey.generateSSBN(Collections.singletonList(query), knowledgeBase);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			System.out.print(" , " + (System.currentTimeMillis() - timeBeforeExecution));
			assertNotNull(ssbn);
			assertNotNull(ssbn.getNetwork());
			assertEquals(instanceNum+1, ssbn.getNetwork().getNodeCount());
			
			// generate ssbn in gibbs
			AbsoluteErrorSampleGenerationListener stopCondition = new AbsoluteErrorSampleGenerationListener(marginalDiff, (ProbabilisticNetwork)ssbn.getNetwork());
			gibbs.getGibbsSampler().addSampleGenerationListener(stopCondition);
			timeBeforeExecution = System.currentTimeMillis();
			try {
				ssbn = gibbs.generateSSBN(Collections.singletonList(query), knowledgeBase);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			System.out.println(" , " + (System.currentTimeMillis() - timeBeforeExecution - stopCondition.getTotalExecutionTime()));
			assertNotNull(ssbn);
			assertNotNull(ssbn.getNetwork());
			assertEquals(instanceNum+1, ssbn.getNetwork().getNodeCount());
			
			// reset findings, so that we have a clean KB next iteration
			parentResidentNode.removeRandomVariableFinding(finding);
			knowledgeBase.clearFindings();
		}
	}
	
	
}
