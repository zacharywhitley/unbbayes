package unbbayes.datamining.datamanipulation.test;

import java.io.*;

import junit.framework.*;

import unbbayes.datamining.datamanipulation.*;

public class TestUtils extends TestCase {

  /** The natural logarithm of 2. */
  private final double LOG2 = Math.log(2);
  /** The small deviation allowed in double comparisons */
  private final double DELTA = 1e-6;
  private final double DELTA2 = 1e-3;
  public static File CONTACT_LENCES_FILE = new File("examples/contact-lenses.txt");
  public static File WEATHER_NOMINAL_FILE = new File("examples/weather.nominal.arff");
  public static File WEATHER_NUMERIC_CUT_FILE = new File("examples/weather.cut.txt");
  private InstanceSet contactInst;
  private InstanceSet weatherInst;
  private InstanceSet weatherCutInst;

  public TestUtils(String s) {
    super(s);
  }

  protected void setUp() throws Exception{
      Loader loader = new TxtLoader(CONTACT_LENCES_FILE);
      while (loader.getInstance())
      {}

      if (loader instanceof TxtLoader)
      {   ((TxtLoader)loader).checkNumericAttributes();
      }

      contactInst = loader.getInstances();
      contactInst.setClass(contactInst.getAttribute(contactInst.numAttributes()-1));

      loader = new ArffLoader(WEATHER_NOMINAL_FILE);
      while (loader.getInstance())
      {}

      weatherInst = loader.getInstances();
      weatherInst.setClass(weatherInst.getAttribute(weatherInst.numAttributes()-1));

      loader = new TxtLoader(WEATHER_NUMERIC_CUT_FILE);
      while (loader.getInstance())
      {}

      if (loader instanceof TxtLoader)
      {   ((TxtLoader)loader).checkNumericAttributes();
      }

      weatherCutInst = loader.getInstances();
      weatherCutInst.setClass(weatherCutInst.getAttribute(weatherCutInst.numAttributes()-1));

  }

  protected void tearDown() {
    contactInst = null;
    weatherInst = null;
    weatherCutInst = null;
  }

  public void testComputeEntropy() {
    try {
      Id3Utils utils = new Id3Utils();
      // contact
      Assert.assertEquals(utils.computeEntropy(contactInst),1.326,DELTA2);
      // weather
      Assert.assertEquals(utils.computeEntropy(weatherInst),0.940,DELTA2);
      // weather cut
      Assert.assertEquals(utils.computeEntropy(weatherCutInst),0.971,DELTA2);
    }
    catch(Exception e) {
      Assert.fail("Exception thrown: "+e);
    }
  }

  public void testComputeGainRatio() {
    try {
      Id3Utils utils = new Id3Utils();
      // weather
      Assert.assertEquals(utils.computeGainRatio(weatherInst, weatherInst.getAttribute(0)),0.156,DELTA2);
      Assert.assertEquals(utils.computeGainRatio(weatherInst, weatherInst.getAttribute(1)),0.018,DELTA2);
      Assert.assertEquals(utils.computeGainRatio(weatherInst, weatherInst.getAttribute(2)),0.152,DELTA2);
      Assert.assertEquals(utils.computeGainRatio(weatherInst, weatherInst.getAttribute(3)),0.049,DELTA2);
      // contact
      Assert.assertEquals(utils.computeGainRatio(contactInst, contactInst.getAttribute(0)),0.025,DELTA2);
      Assert.assertEquals(utils.computeGainRatio(contactInst, contactInst.getAttribute(1)),0.04,DELTA2);
      Assert.assertEquals(utils.computeGainRatio(contactInst, contactInst.getAttribute(2)),0.377,DELTA2);
      Assert.assertEquals(utils.computeGainRatio(contactInst, contactInst.getAttribute(3)),0.549,DELTA2);
    }
    catch(Exception e) {
      Assert.fail("Exception thrown: "+e);
    }
  }

  public void testComputeInfoGain() {
    try {
      Id3Utils utils = new Id3Utils();
      // weather
      Assert.assertEquals(utils.computeInfoGain(weatherInst, weatherInst.getAttribute(0)),0.247,DELTA2);
      Assert.assertEquals(utils.computeInfoGain(weatherInst, weatherInst.getAttribute(1)),0.029,DELTA2);
      Assert.assertEquals(utils.computeInfoGain(weatherInst, weatherInst.getAttribute(2)),0.152,DELTA2);
      Assert.assertEquals(utils.computeInfoGain(weatherInst, weatherInst.getAttribute(3)),0.048,DELTA2);
      // contact
      Assert.assertEquals(utils.computeInfoGain(contactInst, contactInst.getAttribute(0)),0.039,DELTA2);
      Assert.assertEquals(utils.computeInfoGain(contactInst, contactInst.getAttribute(1)),0.04,DELTA2);
      Assert.assertEquals(utils.computeInfoGain(contactInst, contactInst.getAttribute(2)),0.377,DELTA2);
      Assert.assertEquals(utils.computeInfoGain(contactInst, contactInst.getAttribute(3)),0.549,DELTA2);
      // weather cut
      //Assert.assertEquals(utils.computeInfoGain(weatherCutInst, weatherCutInst.getAttribute(0)),0.420,DELTA2);
      //Assert.assertEquals(utils.computeInfoGain(weatherCutInst, weatherCutInst.getAttribute(1)),0.971,DELTA2);
      Assert.assertEquals(utils.computeInfoGain(weatherCutInst, weatherCutInst.getAttribute(2)),0.02,DELTA2);
    }
    catch(Exception e) {
      Assert.fail("Exception thrown: "+e);
    }
  }
  /*public void testDoubleToString() {
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
    Id3Utils utils = new Id3Utils();
    Assert.assertEquals(utils.log2(0.0),Double.NEGATIVE_INFINITY,DELTA);
    Assert.assertEquals(utils.log2(-0.0),Double.NEGATIVE_INFINITY,DELTA);
    Assert.assertEquals(utils.log2(Double.POSITIVE_INFINITY),Double.POSITIVE_INFINITY,DELTA);
    Assert.assertTrue(Double.isNaN(utils.log2(Double.NEGATIVE_INFINITY)));
    Assert.assertTrue(Double.isNaN(utils.log2(Double.NaN)));
    Assert.assertTrue(Double.isNaN(utils.log2(-4.0)));
    Assert.assertEquals(utils.log2(4.0),2.0,DELTA);
    Assert.assertEquals(utils.log2(2.0),1.0,DELTA);
    Assert.assertEquals(utils.log2(1.0),0.0,DELTA);
    Assert.assertEquals(utils.log2(0.0625),-4.0,DELTA);
    Assert.assertEquals(utils.log2(0.6),-0.7369655941,DELTA);
    Assert.assertEquals(utils.log2(1024.0),10.0,DELTA);
    Assert.assertEquals(utils.log2(1000.0),9.96578428466,DELTA);
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
