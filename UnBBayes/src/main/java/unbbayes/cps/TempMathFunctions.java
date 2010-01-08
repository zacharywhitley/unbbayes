package unbbayes.cps;

// TODO - YOUNG use unbbayes.util.ProbabilityMath

public class TempMathFunctions {

	public TempMathFunctions()
	{
		
	}
	
	double triangular(double a, double b, double c)
	{ 
		if( a < c && c < b ) 
		{
			double U = Math.random();
			if(U <= (c-a)/(b-a))
				return a + Math.sqrt((c-a)*(b-a)*U);
			else // if( (c-a)/(b-a) < U <= 1)
				return b - Math.sqrt((b-a)*(b-c)*(1-U));
		}
		else 
		{
			System.out.println("Invalid input !\n");
			return 0;
		}
	}

	double uniform(double a, double b) 
	{
		return (Math.random() * (b - a) + a);
	}

	double exponential(double m) 
	{
		return (-m * Math.log(Math.random()));
	}

	double normal(double m, double s) 
	{
		return m + (Math.sqrt (-2.0 * Math.log (Math.random())) * 
				Math.cos (Math.PI * (2.0 * Math.random() - 1.0))) * s;
	}
}
