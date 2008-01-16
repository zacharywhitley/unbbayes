package unbbayes.datamining.datamanipulation;

import java.util.ArrayList;

import unbbayes.datamining.classifiers.decisiontree.Leaf;
import unbbayes.datamining.classifiers.decisiontree.Node;
import unbbayes.datamining.classifiers.decisiontree.NominalNode;
import unbbayes.datamining.classifiers.decisiontree.NumericNode;

/** 
 * Class containing functions to prune a tree. 
 * Source: WEKA (http://www.cs.waikato.ac.nz/~ml).   
 * 
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 */
public class PrunningUtils
{
	/** class attribute of the instance set relative to the tree */
	private Attribute classAttribute;
	
	/*************************************************
	 *    COEFFICIENTS FOR METHOD  normalInverse()   *	 
	 *************************************************/
	/* approximation for 0 <= |y - 0.5| <= 3/8 */
	private final double P0[] = 
	{
	  	-5.99633501014107895267E1,
	  	9.80010754185999661536E1,
	  	-5.66762857469070293439E1,
	  	1.39312609387279679503E1,
	  	-1.23916583867381258016E0,
	};
	
	private final double Q0[] = 
	{
	  	1.95448858338141759834E0,
	  	4.67627912898881538453E0,
	  	8.63602421390890590575E1,
	  	-2.25462687854119370527E2,
	  	2.00260212380060660359E2,
	  	-8.20372256168333339912E1,
	  	1.59056225126211695515E1,
	  	-1.18331621121330003142E0,
	};
  
	/* Approximation for interval z = sqrt(-2 log y ) between 2 and 8
	 * i.e., y between exp(-2) = .135 and exp(-32) = 1.27e-14.
	 */
	private final double P1[] = 
	{
	  	4.05544892305962419923E0,
	  	3.15251094599893866154E1,
	  	5.71628192246421288162E1,
	  	4.40805073893200834700E1,
	  	1.46849561928858024014E1,
	  	2.18663306850790267539E0,
	  	-1.40256079171354495875E-1,
	  	-3.50424626827848203418E-2,
	  	-8.57456785154685413611E-4,
	};
	
	private final double Q1[] = 
	{
	  	1.57799883256466749731E1,
	  	4.53907635128879210584E1,
	  	4.13172038254672030440E1,
	  	1.50425385692907503408E1,
	  	2.50464946208309415979E0,
	  	-1.42182922854787788574E-1,
	  	-3.80806407691578277194E-2,
	  	-9.33259480895457427372E-4,
	};
	
	/* Approximation for interval z = sqrt(-2 log y ) between 8 and 64
	 * i.e., y between exp(-32) = 1.27e-14 and exp(-2048) = 3.67e-890.
	 */
	private final double  P2[] = 
	{
		3.23774891776946035970E0,
		6.91522889068984211695E0,
		3.93881025292474443415E0,
		1.33303460815807542389E0,
		2.01485389549179081538E-1,
		1.23716634817820021358E-2,
		3.01581553508235416007E-4,
		2.65806974686737550832E-6,
		6.23974539184983293730E-9,
	};
		
	private final double  Q2[] = 
	{
		6.02427039364742014255E0,
		3.67983563856160859403E0,
		1.37702099489081330271E0,
		2.16236993594496635890E-1,
		1.34204006088543189037E-2,
		3.28014464682127739104E-4,
		2.89247864745380683936E-6,
		6.79019408009981274425E-9,
	};
	
	//-------------------------------------------------------------------------//
	
	/**
	 * Prunes the tree 
	 * 
	 * @param root root of the tree
	 * @param classAttribute the class attribute relative to the tree
	 * @return root of the pruned tree
	 */
	public Node pruneTree(Node root, Attribute classAttribute)
	{
		this.classAttribute = classAttribute;
		int numClasses = classAttribute.getDistinticNominalValues().length;
		Node rootClone = cloneTree(root);
		pruneTree(rootClone,new float[numClasses]);
		return rootClone;
	}
		
	//-------------------------------------------------------------------------//
	
	/**
	 * Internal recursive function used to prune the tree 
	 * 
	 * @param child node to be tested
	 * @param distribution distribution relative to the node (set by the function)
	 * @return error obtained with the distribution
	 */
	private double pruneTree(Node child, float[] distribution)
	{
		double N;
		double e;
		int numClasses = distribution.length;
		float confidence = Options.getInstance().getConfidenceLevel();
		ArrayList grandChildren = child.getChildren();
		 
		//if a node has a leaf as a child... 
		if(grandChildren.get(0) instanceof Leaf)
		{
			Leaf leaf = (Leaf)grandChildren.get(0);
			float[] leafDistribution = leaf.getDistribution();
			if(leafDistribution==null)
			{
				e = 0;
				N = 0;
				return addErrs(N, e, confidence);				
			}
			else
			{
				System.arraycopy(leafDistribution,0,distribution,0,distribution.length);
				e = ClassifierUtils.sumNonClassDistribution(leafDistribution,leaf.getClassValue());
				N = e + distribution[leaf.getClassValue()];
				return addErrs(N, e, confidence);
			}		
		}
		
		//otherwise...
		else
		{
			int classValue;
			double error;
			double currentError = 0;
			double prunningError;
			float[] distributionSum = new float[numClasses];
			float[] distributionTemp;
			Node node;
			
			//for each child...
			for(int i=0;i<grandChildren.size();i++)
			{
				node = (Node)grandChildren.get(i);
				distributionTemp = new float[numClasses];
				error = pruneTree(node,distributionTemp);
				currentError += error/Utils.sum(distributionTemp);
				distributionSum = Utils.arraysSum(distributionSum,distributionTemp); 				
			}
			
			//compute the prunning error
			classValue = Utils.maxIndex(distributionSum);
			e = ClassifierUtils.sumNonClassDistribution(distributionSum,classValue);
			N = e + distributionSum[classValue];
			prunningError = addErrs(N,e,confidence);
			
			//if prunning error < current error, we prune this node
			System.arraycopy(distributionSum,0,distribution,0,distribution.length);
			if(prunningError<currentError)
			{
				child.removeChildren();
				child.add(new Leaf(classAttribute,distributionSum, -1,0));
				return prunningError;
			}
			//otherwise, there is no prunning 
			else
			{
				return currentError;
			}
		}
	}
	
	//-------------------------------------------------------------------------//
	
	/**
	 * Clones a tree (only with essencial data) 
	 * 
	 * @param parent root of the tree to be cloned
	 * @return root of the cloned tree
	 */
	private Node cloneTree(Node parent) {
		Leaf leaf;
	  	Node node, nodeClone;
	  	NumericNode numericNode;
	  	NominalNode nominalNode;
		ArrayList children = parent.getChildren();
		ArrayList<Object> newChildren = new ArrayList<Object>();
		
		if(children.get(0) instanceof Leaf) {
			//leaf
		  	leaf = (Leaf)children.get(0);
			newChildren.add(new Leaf(classAttribute,leaf.getDistribution(), -1,0));
		} else {
			//node
			
			//for each node child - recursive call
			for(int i=0;i<children.size();i++) {
				node = (Node)children.get(i);
				nodeClone = cloneTree(node);
				newChildren.add(nodeClone);				
			}
		}
		
		//create node clone according to the original node type
		if(parent instanceof NumericNode) {
			numericNode = (NumericNode) parent;
			return new NumericNode(numericNode, newChildren);
		} else if(parent instanceof NominalNode) {
			nominalNode = (NominalNode) parent;
			return new NominalNode(nominalNode, newChildren);
		} else {
			return new Node(parent.getAttribute(),newChildren);
		}
	}
		
	//-------------------------------------------------------------------------//
	
	/**
	 * Computes estimated extra error for given total number of instances
	 * and error using normal approximation to binomial distribution
	 * (and continuity correction). Source: WEKA. 
	 *
	 * @param N number of instances
	 * @param e observed error
	 * @param CF confidence value
	 */
	 private double addErrs(double N, double e, float CF)
	 {
		// Ignore stupid values for CF
		//if (CF > 0.5) 
		//{
		//	System.err.println("WARNING: confidence value for pruning " +
		//		   " too high. Error estimate not modified.");
		//	return 0;
		//}

		// Check for extreme cases at the low end because the
		// normal approximation won't work
		if (e < 1) 
		{
			// Base case (i.e. e == 0) from documenta Geigy Scientific
			// Tables, 6th edition, page 185
			double base = N * (1 - Math.pow(CF, 1 / N)); 
			if (e == 0) 
			{
			  return base; 
			}
    
			// Use linear interpolation between 0 and 1 like C4.5 does
			return base + e * (addErrs(N, 1, CF) - base);
		}
    
		// Use linear interpolation at the high end (i.e. between N - 0.5
		// and N) because of the continuity correction
		if (e + 0.5 >= N) 
		{
			// Make sure that we never return anything smaller than zero
			return Math.max(N - e, 0);
		}

		// Get z-score corresponding to CF
		double z = normalInverse(1 - CF);

		// Compute upper limit of confidence interval
		double  f = (e + 0.5) / N;
		double r = 	(f + (z * z) / (2 * N) + z * Math.sqrt((f / N) - (f * f / N) +
					(z * z / (4 * N * N)))) / (1 + (z * z) / N);

		return (r * N) - e;
	}
  
	//-------------------------------------------------------------------------//
	
	/** Source: WEKA. */
	private double normalInverse(double y0) 
	{ 
	  	double x, y, z, y2, x0, x1;
	  	int code;

		final double s2pi = Math.sqrt(2.0*Math.PI);
		
		if( y0 <= 0.0 ) throw new IllegalArgumentException();
		if( y0 >= 1.0 ) throw new IllegalArgumentException();
		code = 1;
		y = y0;
		if( y > (1.0 - 0.13533528323661269189) ) 
		{
			y = 1.0 - y;
			code = 0;
		}

		if( y > 0.13533528323661269189 ) 
		{
			y = y - 0.5;
			y2 = y * y;
			x = y + y * (y2 * polevl( y2, P0, 4)/p1evl( y2, Q0, 8 ));
			x = x * s2pi; 
			return(x);
		}

		x = Math.sqrt( -2.0 * Math.log(y) );
		x0 = x - Math.log(x)/x;

		z = 1.0/x;
		if( x < 8.0 ) 
			x1 = z * polevl( z, P1, 8 )/p1evl( z, Q1, 8 );
		else
			x1 = z * polevl( z, P2, 8 )/p1evl( z, Q2, 8 );
		  
		x = x0 - x1;
		if( code != 0 )
			x = -x;
		
		return( x );
	}
	
	//-------------------------------------------------------------------------//
	  
	/**
	 * Evaluates the given polynomial of degree <tt>N</tt> at <tt>x</tt>.
	 * Evaluates polynomial when coefficient of N is 1.0.
	 * Otherwise same as <tt>polevl()</tt>.
	 * <pre>
	 *                     2          N
	 * y  =  C  + C x + C x  +...+ C x
	 *        0    1     2          N
	 *
	 * Coefficients are stored in reverse order:
	 *
	 * coef[0] = C  , ..., coef[N] = C  .
	 *            N                   0
	 * </pre>
	 * The function <tt>p1evl()</tt> assumes that <tt>coef[N] = 1.0</tt> and is
	 * omitted from the array.  Its calling arguments are
	 * otherwise the same as <tt>polevl()</tt>.
	 * <p>
	 * In the interest of speed, there are no checks for out of bounds arithmetic.
	 * Source: WEKA.
	 *
	 * @param x argument to the polynomial.
	 * @param coef the coefficients of the polynomial.
	 * @param N the degree of the polynomial. 	 */
	private double p1evl( double x, double coef[], int N ) 
	{
  	  	double ans;
		ans = x + coef[0];
  
		for(int i=1; i<N; i++) ans = ans*x+coef[i];
  
		return ans;
	}
	
	//-------------------------------------------------------------------------//

	/**
	* Evaluates the given polynomial of degree <tt>N</tt> at <tt>x</tt>.
	* <pre>
	*                     2          N
	* y  =  C  + C x + C x  +...+ C x
	*        0    1     2          N
	*
	* Coefficients are stored in reverse order:
	*
	* coef[0] = C  , ..., coef[N] = C  .
	*            N                   0
	* </pre>
	* In the interest of speed, there are no checks for out of bounds arithmetic.
	* Source: WEKA.
	*
	* @param x argument to the polynomial.
	* @param coef the coefficients of the polynomial.
	* @param N the degree of the polynomial.
	*/
	private double polevl( double x, double coef[], int N ) 
	{
	  	double ans;
	  	ans = coef[0];
  
		for(int i=1; i<=N; i++) ans = ans*x+coef[i];
  
		return ans;
	}
}