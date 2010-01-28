/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.datamining.utils;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 05/02/2007
 */
public class Statistics {
	  /** Some constants */
	  protected static final double MACHEP =  1.11022302462515654042E-16;
	  protected static final double MAXLOG =  7.09782712893383996732E2;
	  protected static final double MINLOG = -7.451332191019412076235E2;
	  protected static final double MAXGAM = 171.624376956302725;
	  protected static final double SQTPI  =  2.50662827463100050242E0;
	  protected static final double SQRTH  =  7.07106781186547524401E-1;
	  protected static final double LOGPI  =  1.14472988584940017414;
	  
	  protected static final double big    =  4.503599627370496e15;
	  protected static final double biginv =  2.22044604925031308085e-16;

	  /**
	   * Returns the area under the Normal (Gaussian) probability density
	   * function, integrated from minus infinity to <tt>x</tt>
	   * (assumes mean is zero, variance is one).
	   * <pre>
	   *                            x
	   *                             -
	   *                   1        | |          2
	   *  normal(x)  = ---------    |    exp( - t /2 ) dt
	   *               sqrt(2pi)  | |
	   *                           -
	   *                          -inf.
	   *
	   *             =  ( 1 + erf(z) ) / 2
	   *             =  erfc(z) / 2
	   * </pre>
	   * where <tt>z = x/sqrt(2)</tt>.
	   * Computation is via the functions <tt>errorFunction</tt> and <tt>errorFunctionComplement</tt>.
	   *
	   * @param a the z-value
	   * @return the probability of the z value according to the normal pdf
	   */
	  public static double normalProbability(double a) { 

	    double x, y, z;
	 
	    x = a * SQRTH;
	    z = Math.abs(x);
	 
	    if (z < SQRTH) {
	    	y = 0.5 + 0.5 * errorFunction(x);
	    } else {
	    	y = 0.5 * errorFunctionComplemented(z);
	    	if (x > 0) {
	    		y = 1.0 - y;
	    	}
	    }
	    
	    return y;
	  }

	  /**
	   * Returns the error function of the normal distribution.
	   * The integral is
	   * <pre>
	   *                           x 
	   *                            -
	   *                 2         | |          2
	   *   erf(x)  =  --------     |    exp( - t  ) dt.
	   *              sqrt(pi)   | |
	   *                          -
	   *                           0
	   * </pre>
	   * <b>Implementation:</b>
	   * For <tt>0 <= |x| < 1, erf(x) = x * P4(x**2)/Q5(x**2)</tt>; otherwise
	   * <tt>erf(x) = 1 - erfc(x)</tt>.
	   * <p>
	   * Code adapted from the <A HREF="http://www.sci.usq.edu.au/staff/leighb/graph/Top.html">
	   * Java 2D Graph Package 2.4</A>,
	   * which in turn is a port from the
	   * <A HREF="http://people.ne.mediaone.net/moshier/index.html#Cephes">Cephes 2.2</A>
	   * Math Library (C).
	   *
	   * @param a the argument to the function.
	   */
	  static double errorFunction(double x) { 
	    double y, z;
	    final double T[] = {
	      9.60497373987051638749E0,
	      9.00260197203842689217E1,
	      2.23200534594684319226E3,
	      7.00332514112805075473E3,
	      5.55923013010394962768E4
	    };
	    final double U[] = {
	      //1.00000000000000000000E0,
	      3.35617141647503099647E1,
	      5.21357949780152679795E2,
	      4.59432382970980127987E3,
	      2.26290000613890934246E4,
	      4.92673942608635921086E4
	    };
	  
	    if( Math.abs(x) > 1.0 ) return( 1.0 - errorFunctionComplemented(x) );
	    z = x * x;
	    y = x * polevl( z, T, 4 ) / p1evl( z, U, 5 );
	    return y;
	  }

	  /**
	   * Returns the complementary Error function of the normal distribution.
	   * <pre>
	   *  1 - erf(x) =
	   *
	   *                           inf. 
	   *                             -
	   *                  2         | |          2
	   *   erfc(x)  =  --------     |    exp( - t  ) dt
	   *               sqrt(pi)   | |
	   *                           -
	   *                            x
	   * </pre>
	   * <b>Implementation:</b>
	   * For small x, <tt>erfc(x) = 1 - erf(x)</tt>; otherwise rational
	   * approximations are computed.
	   * <p>
	   * Code adapted from the <A HREF="http://www.sci.usq.edu.au/staff/leighb/graph/Top.html">
	   * Java 2D Graph Package 2.4</A>,
	   * which in turn is a port from the
	   * <A HREF="http://people.ne.mediaone.net/moshier/index.html#Cephes">Cephes 2.2</A>
	   * Math Library (C).
	   *
	   * @param a the argument to the function.
	   */
	  static double errorFunctionComplemented(double a) { 
	    double x,y,z,p,q;
	  
	    double P[] = {
	      2.46196981473530512524E-10,
	      5.64189564831068821977E-1,
	      7.46321056442269912687E0,
	      4.86371970985681366614E1,
	      1.96520832956077098242E2,
	      5.26445194995477358631E2,
	      9.34528527171957607540E2,
	      1.02755188689515710272E3,
	      5.57535335369399327526E2
	    };
	    double Q[] = {
	      //1.0
	      1.32281951154744992508E1,
	      8.67072140885989742329E1,
	      3.54937778887819891062E2,
	      9.75708501743205489753E2,
	      1.82390916687909736289E3,
	      2.24633760818710981792E3,
	      1.65666309194161350182E3,
	      5.57535340817727675546E2
	    };
	  
	    double R[] = {
	      5.64189583547755073984E-1,
	      1.27536670759978104416E0,
	      5.01905042251180477414E0,
	      6.16021097993053585195E0,
	      7.40974269950448939160E0,
	      2.97886665372100240670E0
	    };
	    double S[] = {
	      //1.00000000000000000000E0, 
	      2.26052863220117276590E0,
	      9.39603524938001434673E0,
	      1.20489539808096656605E1,
	      1.70814450747565897222E1,
	      9.60896809063285878198E0,
	      3.36907645100081516050E0
	    };
	  
	    if( a < 0.0 )   x = -a;
	    else            x = a;
	  
	    if( x < 1.0 )   return 1.0 - errorFunction(a);
	  
	    z = -a * a;
	  
	    if( z < -MAXLOG ) {
	      if( a < 0 )  return( 2.0 );
	      else         return( 0.0 );
	    }
	  
	    z = Math.exp(z);
	  
	    if( x < 8.0 ) {
	      p = polevl( x, P, 8 );
	      q = p1evl( x, Q, 8 );
	    } else {
	      p = polevl( x, R, 5 );
	      q = p1evl( x, S, 6 );
	    }
	  
	    y = (z * p)/q;
	  
	    if( a < 0 ) y = 2.0 - y;
	  
	    if( y == 0.0 ) {
	      if( a < 0 ) return 2.0;
	      else        return( 0.0 );
	    }
	    return y;
	  }
	  
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
	   *
	   * @param x argument to the polynomial.
	   * @param coef the coefficients of the polynomial.
	   * @param N the degree of the polynomial.
	   */
	  static double p1evl( double x, double coef[], int N ) {
	  
	    double ans;
	    ans = x + coef[0];
	  
	    for(int i=1; i<N; i++) ans = ans*x+coef[i];
	  
	    return ans;
	  }

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
	   *
	   * @param x argument to the polynomial.
	   * @param coef the coefficients of the polynomial.
	   * @param N the degree of the polynomial.
	   */
	  static double polevl( double x, double coef[], int N ) {

	    double ans;
	    ans = coef[0];
	  
	    for(int i=1; i<=N; i++) ans = ans*x+coef[i];
	  
	    return ans;
	  }

	public static double computeFProb(int dfn, int dfd, double f) {
		if (dfn >.01 && dfd > .01 && f > .0000000001) {
			return probf(dfn,dfd,f);
		} else {
			return -1;
		}
	}

	private static double probf(int dfn, int dfd, double f) {
		int iv = 0;
		
		int dfnAux = dfn;
		int dfdAux = dfd;
		
		if (Math.floor(dfn / 2) * 2 == dfn) {
			/* The numerator df is even */
			return l401(dfn, f, dfd, iv);
		} else if (Math.floor(dfd / 2) * 2 != dfd) {
			/* The denominator df is odd */
			return l504(dfn, f, dfd, iv);
		}
		
		f = 1 / f;
		iv = 1;
		dfn = dfdAux;
		dfd = dfnAux;
		
		return l401(dfn, f, dfd, iv);
	}

	private static double l504(int a, double f,int b, int iv) {
		double q = a*f/(a*f+b);
		double sa = Math.sqrt(q);
		double sl = Math.log(sa);
		double ca = Math.sqrt(1-q);
		double cl = Math.log(ca);
		double al = Math.atan(sa/Math.sqrt(-sa*sa+1));
		double fp = 1-2*al/Math.PI;
		double r=0;
		if (b!=1) {
			double c = Math.log(2*sa/Math.PI);
			fp-=Math.exp(c+cl);
			if (b!=3) {
				double n = Math.floor((b-3)/2);
				double x;
				double rr;
				for (int i=1;i<=n;i++) {
					x = 2*i+1;
					r+=Math.log((x-1)/x);
					rr=r+cl*x+c;
					if (rr>-78.4) {
						fp-=Math.exp(rr);
					}
				}
			}
		}
		
		if (a!=1) {
			double c = r;
			
			if (b>1) {
				c+=Math.log(b-1);
			}
			
			c+=Math.log(2/Math.PI) + sl + cl*b;
			
			if (c>-78.4) {
				fp+=Math.exp(c);
			}
			
			if (a!=3) {
				double n = Math.floor((a-3)/2);
				r=0;
				double x;
				double rr;
				for (int i=1;i<=n;i++) {
					x=i*2+1;
					r+=Math.log((b+x-2)/x);
					rr=r+sl*(x-1)+c;
					if (rr>-78.4) {
						fp+=Math.exp(rr);
					}
				}
			}
		}	
		return fp;
	}

	private static double l401(int a, double f,int b, int iv) {
		double q = a*f/(a*f+b);
		double ql=Math.log(q);
		double fp=0;
		double c = Math.log(1-q)*b/2;
		if (c>-78.4) {
			fp = Math.exp(c);
		}
		
		if (a != 2) {
			double n = Math.floor(a/2-1);
			double r=0;
			double x;
			for (int i=1;i<=n;i++) {
				x=2*i;
				r+=Math.log(b+x-2)-Math.log(x) + ql;
				if (r+c> -78.4) {
					fp+=Math.exp(r+c);
				}
			 }
			}
			
			if (iv==1) {
				fp = 1-fp;
			}
	
		return fp;
	}


}

