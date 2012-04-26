
/*
 * example1.java
 * Number of nodes:20
 * This network was created on Wed Apr 25 12:52:41 EDT 2012

 * @author : BNGenerator (Random and uniform DAG generator)
 *            and Jaime Ide (for function generation)
 */
import embayes.data.*;

class example1 {

  private BayesNet network;
  public BayesNet getNetwork() { return(network); }

  public example1() {

	DataFactory factory =
			 embayes.data.impl.DataBasicFactory.getInstance();
	network = factory.newBayesNet();
	network.setName("example1");

	CategoricalVariable n0 =
		factory.newCategoricalVariable("n0", new String[] {"state0","state1"});
	CategoricalVariable n1 =
		factory.newCategoricalVariable("n1", new String[] {"state0","state1"});
	CategoricalVariable n2 =
		factory.newCategoricalVariable("n2", new String[] {"state0","state1"});
	CategoricalVariable n3 =
		factory.newCategoricalVariable("n3", new String[] {"state0","state1"});
	CategoricalVariable n4 =
		factory.newCategoricalVariable("n4", new String[] {"state0","state1"});
	CategoricalVariable n5 =
		factory.newCategoricalVariable("n5", new String[] {"state0","state1"});
	CategoricalVariable n6 =
		factory.newCategoricalVariable("n6", new String[] {"state0","state1"});
	CategoricalVariable n7 =
		factory.newCategoricalVariable("n7", new String[] {"state0","state1"});
	CategoricalVariable n8 =
		factory.newCategoricalVariable("n8", new String[] {"state0","state1"});
	CategoricalVariable n9 =
		factory.newCategoricalVariable("n9", new String[] {"state0","state1"});
	CategoricalVariable n10 =
		factory.newCategoricalVariable("n10", new String[] {"state0","state1"});
	CategoricalVariable n11 =
		factory.newCategoricalVariable("n11", new String[] {"state0","state1"});
	CategoricalVariable n12 =
		factory.newCategoricalVariable("n12", new String[] {"state0","state1"});
	CategoricalVariable n13 =
		factory.newCategoricalVariable("n13", new String[] {"state0","state1"});
	CategoricalVariable n14 =
		factory.newCategoricalVariable("n14", new String[] {"state0","state1"});
	CategoricalVariable n15 =
		factory.newCategoricalVariable("n15", new String[] {"state0","state1"});
	CategoricalVariable n16 =
		factory.newCategoricalVariable("n16", new String[] {"state0","state1"});
	CategoricalVariable n17 =
		factory.newCategoricalVariable("n17", new String[] {"state0","state1"});
	CategoricalVariable n18 =
		factory.newCategoricalVariable("n18", new String[] {"state0","state1"});
	CategoricalVariable n19 =
		factory.newCategoricalVariable("n19", new String[] {"state0","state1"});
	CategoricalProbability p0 =
		factory.newCategoricalProbability(n0,
		new CategoricalVariable[] {n5,n6},
		new double[] {0.21032964 ,0.3820418 ,0.72549343 ,0.8371653 ,0.78967035 ,0.6179582 ,0.27450657 ,0.16283467 });
	CategoricalProbability p1 =
		factory.newCategoricalProbability(n1,
		new CategoricalVariable[] {n4},
		new double[] {0.15322779 ,0.4611901 ,0.8467722 ,0.5388099 });
	CategoricalProbability p2 =
		factory.newCategoricalProbability(n2,
		new CategoricalVariable[] {n6},
		new double[] {0.5494814 ,0.041587606 ,0.45051864 ,0.95841247 });
	CategoricalProbability p3 =
		factory.newCategoricalProbability(n3,
		new CategoricalVariable[] {n7,n17},
		new double[] {0.44838032 ,0.48708573 ,0.9600471 ,0.69332606 ,0.55161965 ,0.51291424 ,0.03995286 ,0.30667397 });
	CategoricalProbability p4 =
		factory.newCategoricalProbability(n4,
		new double[] {0.18554837 ,0.81445163 });
	CategoricalProbability p5 =
		factory.newCategoricalProbability(n5,
		new CategoricalVariable[] {n12},
		new double[] {0.5206644 ,0.35086063 ,0.47933558 ,0.6491394 });
	CategoricalProbability p6 =
		factory.newCategoricalProbability(n6,
		new CategoricalVariable[] {n14},
		new double[] {0.27977574 ,0.455632 ,0.7202242 ,0.544368 });
	CategoricalProbability p7 =
		factory.newCategoricalProbability(n7,
		new CategoricalVariable[] {n4},
		new double[] {0.602258 ,0.34345523 ,0.39774206 ,0.6565448 });
	CategoricalProbability p8 =
		factory.newCategoricalProbability(n8,
		new CategoricalVariable[] {n17},
		new double[] {0.22088768 ,0.004223949 ,0.7791123 ,0.9957761 });
	CategoricalProbability p9 =
		factory.newCategoricalProbability(n9,
		new CategoricalVariable[] {n12,n4},
		new double[] {0.21162753 ,0.965342 ,0.5746511 ,0.23775154 ,0.78837246 ,0.03465805 ,0.42534888 ,0.7622485 });
	CategoricalProbability p10 =
		factory.newCategoricalProbability(n10,
		new CategoricalVariable[] {n3,n18},
		new double[] {0.16724524 ,0.5639406 ,0.92661667 ,0.08888065 ,0.83275473 ,0.4360594 ,0.073383324 ,0.91111934 });
	CategoricalProbability p11 =
		factory.newCategoricalProbability(n11,
		new CategoricalVariable[] {n2},
		new double[] {0.4381248 ,0.78363025 ,0.56187516 ,0.21636981 });
	CategoricalProbability p12 =
		factory.newCategoricalProbability(n12,
		new double[] {0.5349569 ,0.46504313 });
	CategoricalProbability p13 =
		factory.newCategoricalProbability(n13,
		new CategoricalVariable[] {n18,n12,n14},
		new double[] {0.10672551 ,0.65828973 ,0.60894656 ,0.19761059 ,0.2987334 ,0.61391664 ,0.94609857 ,0.82109755 ,0.89327455 ,0.34171027 ,0.3910534 ,0.80238944 ,0.7012666 ,0.3860834 ,0.053901415 ,0.17890248 });
	CategoricalProbability p14 =
		factory.newCategoricalProbability(n14,
		new double[] {0.45674297 ,0.54325706 });
	CategoricalProbability p15 =
		factory.newCategoricalProbability(n15,
		new CategoricalVariable[] {n0},
		new double[] {0.74146974 ,0.23807769 ,0.25853026 ,0.76192236 });
	CategoricalProbability p16 =
		factory.newCategoricalProbability(n16,
		new CategoricalVariable[] {n1},
		new double[] {0.9274623 ,0.5186292 ,0.07253772 ,0.48137084 });
	CategoricalProbability p17 =
		factory.newCategoricalProbability(n17,
		new double[] {0.50129426 ,0.49870577 });
	CategoricalProbability p18 =
		factory.newCategoricalProbability(n18,
		new CategoricalVariable[] {n9},
		new double[] {0.6094651 ,0.72240967 ,0.3905349 ,0.27759036 });
	CategoricalProbability p19 =
		factory.newCategoricalProbability(n19,
		new CategoricalVariable[] {n5,n14},
		new double[] {0.6219627 ,0.61729085 ,0.71804965 ,0.3482292 ,0.3780373 ,0.38270918 ,0.28195038 ,0.65177083 });
network.setVariables( new CategoricalVariable[] {
n0,n1,n2,n3,n4,n5,n6,n7,n8,n9,n10,n11,n12,n13,n14,n15,n16,n17,n18,n19});
network.setProbabilities( new CategoricalProbability[] {
p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,p17,p18,p19});
  } // end of public
}  // end of class
