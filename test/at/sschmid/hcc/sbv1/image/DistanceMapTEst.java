package at.sschmid.hcc.sbv1.image;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public final class DistanceMapTest {
  
  private Image image;
  
  @Before
  public void setup() {
    final int[][] imageData = {
        { 0, 255, 0, 255, 0 },
        { 0, 255, 0, 255, 0 },
        { 0, 255, 255, 255, 0 },
        { 0, 255, 0, 0, 0 },
        { 0, 255, 0, 0, 0 },
        { 0, 0, 0, 0, 0, 0 }
    };
    
    image = new Image(imageData, 6, 5);
  }
  
  @Test
  public void testManhattan() {
    final DistanceMap distanceMap = image.distanceMap(DistanceMap.DistanceMetric.Manhattan);
    
    final double[][] expectedMap = {
        { 1, 0, 1, 0, 1 },
        { 1, 0, 1, 0, 1 },
        { 1, 0, 0, 0, 1 },
        { 1, 0, 1, 1, 2 },
        { 1, 0, 1, 2, 3 },
        { 2, 1, 2, 3, 4 },
    };
    
    final double[][] result = distanceMap.calculate();
    Assert.assertArrayEquals(expectedMap, result);
  }
  
  @Test
  public void testEuklid() {
    final DistanceMap distanceMap = image.distanceMap(DistanceMap.DistanceMetric.Euklid);
    
    final double[][] expectedMap = {
        { 1, 0, 1, 0, 1 },
        { 1, 0, 1, 0, 1 },
        { 1, 0, 0, 0, 1 },
        { 1, 0, 1, 1, 1.41 },
        { 1, 0, 1, 2, 2.41 },
        { 1.41, 1, 1.41, 2.41, 3.41 },
    };
    
    final double[][] result = distanceMap.calculate();
    Assert.assertArrayEquals(expectedMap, result);
  }
  
}
