package unbbayes.datamining.datamanipulation.test;

import junit.framework.*;

import unbbayes.datamining.datamanipulation.*;

public class TestUtils extends TestCase {

  /** The natural logarithm of 2. */
  private final double LOG2 = Math.log(2);

  /** The small deviation allowed in double comparisons */
  private final double DELTA = 1e-6;

  public TestUtils(String s) {
    super(s);
  }

  protected void setUp() {
  }

  /*protected void tearDown() {
  }

  /*public void testComputeEntropy() {
    InstanceSet data1=  null  /** @todo fill in non-null value */;
    /*try {
      double doubleRet = Utils.computeEntropy(data1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
    /*}
    catch(Exception e) {
      System.err.println("Exception thrown:  "+e);
    }
  }
  public void testComputeGainRatio() {
    InstanceSet data1=  null  /** @todo fill in non-null value */;
    /*Attribute att2=  null  /** @todo fill in non-null value */;
    /*try {
      double doubleRet = Utils.computeGainRatio(data1, att2);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
    /*}
    catch(Exception e) {
      System.err.println("Exception thrown:  "+e);
    }
  }
  public void testComputeInfoGain() {
    InstanceSet data1=  null  /** @todo fill in non-null value */;
    /*Attribute att2=  null  /** @todo fill in non-null value */;
    /*try {
      double doubleRet = Utils.computeInfoGain(data1, att2);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
    /*}
    catch(Exception e) {
      System.err.println("Exception thrown:  "+e);
    }
  }
  public void testDoubleToString() {
    double value1=  0.0;
    int afterDecimalPoint2=  0;
    String stringRet = Utils.doubleToString(value1, afterDecimalPoint2);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testDoubleToString1() {
    double value1=  0.0;
    int width2=  0;
    int afterDecimalPoint3=  0;
    String stringRet = Utils.doubleToString(value1, width2, afterDecimalPoint3);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testEq() {
    double a1=  0.0;
    double b2=  0.0;
    boolean booleanRet = Utils.eq(a1, b2);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testEq1() {
    short a1=  0;
    short b2=  0;
    boolean booleanRet = Utils.eq(a1, b2);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testGetDistribution() {
    double[] values1=  null  /** @todo fill in non-null value */;
    /*double[] double[]Ret = Utils.getDistribution(values1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testGetDistribution1() {
    float[] values1=  null  /** @todo fill in non-null value */;
    /*float[] float[]Ret = Utils.getDistribution(values1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testGetFrequency() {
    double[] values1=  null  /** @todo fill in non-null value */;
    /*double[] double[]Ret = Utils.getFrequency(values1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testGetFrequency1() {
    float[] values1=  null  /** @todo fill in non-null value */;
    /*float[] float[]Ret = Utils.getFrequency(values1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testGr() {
    double a1=  0.0;
    double b2=  0.0;
    boolean booleanRet = Utils.gr(a1, b2);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  //}
  public void testLog2()
  {
    Assert.assertEquals(Utils.log2(0.0),Double.NEGATIVE_INFINITY,DELTA);
    Assert.assertEquals(Utils.log2(-0.0),Double.NEGATIVE_INFINITY,DELTA);
    Assert.assertEquals(Utils.log2(Double.POSITIVE_INFINITY),Double.POSITIVE_INFINITY,DELTA);
    Assert.assertTrue(Double.isNaN(Utils.log2(Double.NEGATIVE_INFINITY)));
    Assert.assertTrue(Double.isNaN(Utils.log2(Double.NaN)));
    Assert.assertTrue(Double.isNaN(Utils.log2(-4.0)));
    Assert.assertEquals(Utils.log2(4.0),2.0,DELTA);
    Assert.assertEquals(Utils.log2(2.0),1.0,DELTA);
    Assert.assertEquals(Utils.log2(1.0),0.0,DELTA);
    Assert.assertEquals(Utils.log2(0.0625),-4.0,DELTA);
    Assert.assertEquals(Utils.log2(0.6),-0.7369655941,DELTA);
    Assert.assertEquals(Utils.log2(1024.0),10.0,DELTA);
    Assert.assertEquals(Utils.log2(1000.0),9.96578428466,DELTA);
  }
  /*public void testMaxIndex() {
    double[] doubles1=  null  /** @todo fill in non-null value */;
    /*int intRet = Utils.maxIndex(doubles1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testMaxIndex1() {
    float[] floats1=  null  /** @todo fill in non-null value */;
    /*int intRet = Utils.maxIndex(floats1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testMaxIndex2() {
    int[] ints1=  null  /** @todo fill in non-null value */;
    /*int intRet = Utils.maxIndex(ints1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testMin() {
    double[] doubles1=  null  /** @todo fill in non-null value */;
    /*double doubleRet = Utils.min(doubles1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testMin1() {
    float[] floats1=  null  /** @todo fill in non-null value */;
    /*float floatRet = Utils.min(floats1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testNormalize() {
    double[] doubles1=  null  /** @todo fill in non-null value */;
    /*Utils.normalize(doubles1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testNormalize1() {
    double[] doubles1=  null  /** @todo fill in non-null value */;
    /*double sum2=  0.0;
    Utils.normalize(doubles1, sum2);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testNormalize2() {
    float[] floats1=  null  /** @todo fill in non-null value */;
    /*Utils.normalize(floats1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testNormalize3() {
    float[] floats1=  null  /** @todo fill in non-null value */;
    /*float sum2=  (float)0.0;
    Utils.normalize(floats1, sum2);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testSort() {
    double[] array1=  null  /** @todo fill in non-null value */;
    /*int[] int[]Ret = Utils.sort(array1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testSort1() {
    short[] array1=  null  /** @todo fill in non-null value */;
    /*int[] int[]Ret = Utils.sort(array1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testSplitData() {
    InstanceSet data1=  null  /** @todo fill in non-null value */;
    /*Attribute att2=  null  /** @todo fill in non-null value */;
    /*InstanceSet[] instanceset[]Ret = Utils.splitData(data1, att2);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testSum() {
    double[] doubles1=  null  /** @todo fill in non-null value */;
    /*double doubleRet = Utils.sum(doubles1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testSum1() {
    float[] floats1=  null  /** @todo fill in non-null value */;
    /*float floatRet = Utils.sum(floats1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}
  public void testSum2() {
    int[] ints1=  null  /** @todo fill in non-null value */;
    /*int intRet = Utils.sum(ints1);
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  /*}*/
}
