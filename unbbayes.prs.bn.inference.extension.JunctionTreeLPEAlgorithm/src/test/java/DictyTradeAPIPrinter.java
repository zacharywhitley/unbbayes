import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * 
 */

/**
 * @author Shou Matsumoto
 *
 */
public class DictyTradeAPIPrinter extends TestCase {

	/**
	 * @param name
	 */
	public DictyTradeAPIPrinter(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	
	
	public final void test20Teams() {
		int id1st = 808;
		int id2nd = 812;
		int[] teamIds = {809, 810, 811, 813, 814, 815, 816, 817, 818, 819, 820, 821, 822, 823, 824, 825, 826, 827, 828, 829};
		List<Integer> teamX = new ArrayList<Integer>(teamIds.length);
		for (Integer team : teamIds) {
			teamX.add(team);
		}
		
		// P(2nd = x | 1st = x) = 0%, for all 20 teams x        # Set 20 rows of CPT
		for (int state1st = 0; state1st < teamX.size(); state1st++) {
			List<Float> probs = new ArrayList<Float>(teamX.size());
			for (int state2nd = 0; state2nd < teamX.size(); state2nd++) {
				probs.add(((state2nd==state1st)?0f:(1f/(teamX.size()-1))));
			}
			System.out.println(getAPIURL(id2nd, id1st, state1st, probs ));
		}
		
	    // If 2nd=X then TeamX = 2, ie P(TeamX) = 2/21.    # Set 20 Scaled Q values.
		for (int teamIndex = 0; teamIndex < teamX.size(); teamIndex++) {
			for (int state2nd = 0; state2nd < teamX.size(); state2nd++) {
				List<Float> probs = new ArrayList<Float>(teamX.size());
				if (state2nd == teamIndex) {
					// P(TeamX=2|2nd=X) = 1
					for (int stateTeamX = 0; stateTeamX < teamX.size(); stateTeamX++) {
						probs.add((stateTeamX==1)?1f:0f);
					}
				} else {
					// P(TeamX=2|2nd!=X) = 0
					for (int stateTeamX = 0; stateTeamX < teamX.size(); stateTeamX++) {
						probs.add(((stateTeamX==1)?0f:(1f/(teamX.size()-1))));
					}
				}
				System.out.println(getAPIURL(teamX.get(teamIndex), id2nd, state2nd, probs ));
			}
		}
		
		// If 1st=X then TeamX = 1, ie P(TeamX) = 1/21.    # Set 20 Scaled Q values.
		for (int teamIndex = 0; teamIndex < teamX.size(); teamIndex++) {
			for (int state1st = 0; state1st < teamX.size(); state1st++) {
				List<Float> probs = new ArrayList<Float>(teamX.size());
				if (state1st == teamIndex) {
					// P(TeamX=2|2nd=X) = 1
					for (int stateTeamX = 0; stateTeamX < teamX.size(); stateTeamX++) {
						probs.add((stateTeamX==0)?1f:0f);
					}
				} else {
					// P(TeamX=2|2nd!=X) = 0
					for (int stateTeamX = 0; stateTeamX < teamX.size(); stateTeamX++) {
						probs.add(((stateTeamX==0)?0f:(1f/(teamX.size()-1))));
					}
				}
				System.out.println(getAPIURL(teamX.get(teamIndex), id1st, state1st, probs ));
			}
		}
	}
	
	private String getAPIURL(int questionId, int assumptionId, int assumedState, List<Float> probs) {
		
		assertNotNull("P("+questionId+"|"+assumptionId+"="+assumedState+")="+probs,probs);
		float sum = 0;
		for (Float prob : probs) {
			sum += prob;
		}
		assertEquals("P("+questionId+"|"+assumptionId+"="+assumedState+")="+probs, 1f, sum, 0.00005);
		String ret = "https://scicast.org/trades/create?question_id=";
		ret += questionId;
		ret += "&assumptions=";
//		for (int i = 0; i < assumptionId.size(); i++) {
//			ret += assumptionId.get(i) + ":" + assumedState.get(i);
//			if (i+1 < assumptionId.size()) {
//				ret += ",";
//			}
//		}
		ret += assumptionId + ":" + assumedState;
		ret += "&new_value=";
		for (int i = 0; i < probs.size(); i++) {
			ret += probs.get(i);
			if (i+1 < probs.size()) {
				ret += ",";
			}
		}
		ret +="&no_asset_cost=True";
		return ret;
	}

}
