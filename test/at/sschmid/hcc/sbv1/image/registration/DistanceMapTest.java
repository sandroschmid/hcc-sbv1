package at.sschmid.hcc.sbv1.image.registration;

import at.sschmid.hcc.sbv1.image.Image;
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
    final DistanceMap distanceMap = image.distanceMap(DistanceMetric.Manhattan);
    
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
    final DistanceMap distanceMap = image.distanceMap(DistanceMetric.Euklid);
  
    final double sqrt2 = Math.sqrt(2);
    final double[][] expectedMap = {
        { 1, 0, 1, 0, 1 },
        { 1, 0, 1, 0, 1 },
        { 1, 0, 0, 0, 1 },
        { 1, 0, 1, 1, sqrt2 },
        { 1, 0, 1, 2, 1 + sqrt2 },
        { sqrt2, 1, sqrt2, 1 + sqrt2, 2 + sqrt2 },
    };
    
    final double[][] result = distanceMap.calculate();
    Assert.assertArrayEquals(expectedMap, result);
  }
  
}
