
/*
 * bn10_maxTW2_maxVAL31.java
 * Number of nodes:10
 * This network was created on Wed Apr 25 13:43:13 EDT 2012

 * @author : BNGenerator (Random and uniform DAG generator)
 *            and Jaime Ide (for function generation)
 */
import embayes.data.*;

class bn10_maxTW2_maxVAL31 {

  private BayesNet network;
  public BayesNet getNetwork() { return(network); }

  public bn10_maxTW2_maxVAL31() {

	DataFactory factory =
			 embayes.data.impl.DataBasicFactory.getInstance();
	network = factory.newBayesNet();
	network.setName("bn10_maxTW2_maxVAL31");

	CategoricalVariable n0 =
		factory.newCategoricalVariable("n0", new String[] {"state0","state1"});
	CategoricalVariable n1 =
		factory.newCategoricalVariable("n1", new String[] {"state0","state1"});
	CategoricalVariable n2 =
		factory.newCategoricalVariable("n2", new String[] {"state0","state1","state2"});
	CategoricalVariable n3 =
		factory.newCategoricalVariable("n3", new String[] {"state0","state1","state2"});
	CategoricalVariable n4 =
		factory.newCategoricalVariable("n4", new String[] {"state0","state1","state2"});
	CategoricalVariable n5 =
		factory.newCategoricalVariable("n5", new String[] {"state0","state1","state2"});
	CategoricalVariable n6 =
		factory.newCategoricalVariable("n6", new String[] {"state0","state1"});
	CategoricalVariable n7 =
		factory.newCategoricalVariable("n7", new String[] {"state0","state1","state2"});
	CategoricalVariable n8 =
		factory.newCategoricalVariable("n8", new String[] {"state0","state1","state2"});
	CategoricalVariable n9 =
		factory.newCategoricalVariable("n9", new String[] {"state0","state1","state2"});
	CategoricalProbability p0 =
		factory.newCategoricalProbability(n0,
		new CategoricalVariable[] {n1},
		new double[] {0.4681303 ,0.2121373 ,0.53186977 ,0.7878627 });
	CategoricalProbability p1 =
		factory.newCategoricalProbability(n1,
		new double[] {0.1251093 ,0.8748907 });
	CategoricalProbability p2 =
		factory.newCategoricalProbability(n2,
		new CategoricalVariable[] {n7,n8},
		new double[] {0.7264358 ,0.7111648 ,0.1655209 ,0.053694498 ,0.14219713 ,0.07000989 ,0.47718385 ,0.07569453 ,0.33927557 ,0.22796759 ,0.06025321 ,0.15950242 ,0.61078 ,0.7594811 ,0.29317778 ,0.0042776265 ,0.8906031 ,0.4066951 ,0.045596562 ,0.228582 ,0.6749767 ,0.3355255 ,0.09832181 ,0.6368123 ,0.51853853 ,0.03370236 ,0.25402933 });
	CategoricalProbability p3 =
		factory.newCategoricalProbability(n3,
		new CategoricalVariable[] {n9},
		new double[] {0.25075617 ,0.23758468 ,0.1970724 ,0.32276434 ,0.6699448 ,0.4852414 ,0.4264795 ,0.09247048 ,0.3176862 });
	CategoricalProbability p4 =
		factory.newCategoricalProbability(n4,
		new CategoricalVariable[] {n5,n2},
		new double[] {0.22039925 ,0.040393006 ,0.2701945 ,0.07939524 ,0.1372916 ,0.0042114584 ,0.093690485 ,0.36137554 ,0.7104566 ,0.19386622 ,0.45049128 ,0.67398614 ,0.058879912 ,0.5269437 ,0.9105969 ,0.086926185 ,0.24681531 ,0.19427319 ,0.58573455 ,0.5091157 ,0.055819288 ,0.86172485 ,0.3357647 ,0.08519159 ,0.81938326 ,0.39180914 ,0.09527014 });
	CategoricalProbability p5 =
		factory.newCategoricalProbability(n5,
		new double[] {0.40974146 ,0.5272044 ,0.06305415 });
	CategoricalProbability p6 =
		factory.newCategoricalProbability(n6,
		new CategoricalVariable[] {n4,n5},
		new double[] {0.5992323 ,0.29867068 ,0.15956028 ,0.75944316 ,0.7418873 ,0.91711885 ,0.70825875 ,0.5112646 ,0.61648947 ,0.4007677 ,0.7013293 ,0.84043974 ,0.2405569 ,0.2581127 ,0.0828811 ,0.2917413 ,0.4887354 ,0.38351056 });
	CategoricalProbability p7 =
		factory.newCategoricalProbability(n7,
		new double[] {0.10656611 ,0.75859886 ,0.13483505 });
	CategoricalProbability p8 =
		factory.newCategoricalProbability(n8,
		new CategoricalVariable[] {n1},
		new double[] {0.1730745 ,0.20379774 ,0.6492705 ,0.21640304 ,0.17765503 ,0.57979923 });
	CategoricalProbability p9 =
		factory.newCategoricalProbability(n9,
		new CategoricalVariable[] {n6,n5},
		new double[] {0.6758301 ,0.055210087 ,0.48970047 ,0.29843506 ,0.70580596 ,0.108368695 ,0.109510936 ,0.4043727 ,0.37315324 ,0.34414905 ,0.27093193 ,0.58962363 ,0.2146589 ,0.5404172 ,0.13714626 ,0.3574159 ,0.023262132 ,0.30200768 });
network.setVariables( new CategoricalVariable[] {
n0,n1,n2,n3,n4,n5,n6,n7,n8,n9});
network.setProbabilities( new CategoricalProbability[] {
p0,p1,p2,p3,p4,p5,p6,p7,p8,p9});
  } // end of public
}  // end of class
