/**
 * 
 */
package unbbayes.prs.mebn.entity.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityConteiner;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.entity.TypeContainer;

/**
 * @author Shou Matsumoto
 *
 */
public class ObjectEntityConteinerTest {

	MultiEntityBayesianNetwork mebn = null;
	ObjectEntityConteiner container = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.mebn = new MultiEntityBayesianNetwork("ObjectEntityConteinerTest");		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}


	/**
	 * Test method for {@link unbbayes.prs.mebn.entity.ObjectEntityConteiner#clearAllInstances(unbbayes.prs.mebn.entity.ObjectEntity)}.
	 */
	@Test
	public final void testClearAllInstances() {
		ObjectEntityConteiner container = this.mebn.getObjectEntityContainer();
		ObjectEntity entity1 = null;
		ObjectEntity entity2 = null;
		try {
			entity1 = container.createObjectEntity("ObjectEntityConteinerTest1");
			entity2 = container.createObjectEntity("ObjectEntityConteinerTest2");
		} catch (Exception e) {
			fail(e.getMessage());
		}
		ObjectEntityInstance instance1 = null;
		ObjectEntityInstance instance2 = null;
		ObjectEntityInstance instance3 = null;
		ObjectEntityInstance instance4 = null;
		
		try {
			instance1 = entity1.addInstance("instance1");
			instance2 = entity1.addInstance("instance2");
			instance3 = entity1.addInstance("instance3");
			instance4 = entity2.addInstance("instance4");
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		try {
			container.addEntityInstance(instance1);
			container.addEntityInstance(instance2);
			container.addEntityInstance(instance3);
			container.addEntityInstance(instance4);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertEquals(instance1, container.getEntityInstanceByName(instance1.getName()));
		assertEquals(instance2, container.getEntityInstanceByName(instance2.getName()));
		assertEquals(instance3, container.getEntityInstanceByName(instance3.getName()));
		assertEquals(3 , entity1.getInstanceList().size());
		assertEquals(4, container.getListEntityInstances().size());
		
		container.clearAllInstances(entity1);
		
		assertEquals(0 , entity1.getInstanceList().size());
		assertEquals(1, container.getListEntityInstances().size());
		assertNull(container.getEntityInstanceByName(instance1.getName()));
		assertNull(container.getEntityInstanceByName(instance2.getName()));
		assertNull(container.getEntityInstanceByName(instance3.getName()));
		
		assertTrue(entity1.getInstanceList().isEmpty());
		
	}


}
