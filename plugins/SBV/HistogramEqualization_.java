import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.ImageTransferFunctions;
import at.sschmid.hcc.sbv1.image.imagej.AbstractPlugIn;

public final class HistogramEqualization_ extends AbstractPlugIn {
  
  public void process(final Image image) {
    int[][] tfs = new int[5][];
    tfs[0] = ImageTransferFunctions.GetHistogramEqualizationTF(image);
    tfs[1] = ImageTransferFunctions.GetHistogramEqualizationTF2(image);
    tfs[2] = ImageTransferFunctions.GetHistogramEqualizationTF3(image);
    tfs[3] = ImageTransferFunctions.GetHistogramEqualizationTF4(image);
    tfs[4] = ImageTransferFunctions.GetHistogramEqualizationTF5(image);
    
    for (int i = 0; i < tfs.length; i++) {
      int[] tf = tfs[i];
      if (tf == null) {
        continue;
      }
  
      final Image resultImg = image.transformation().transfer(tf).getResult();
      addResult(resultImg, String.format("%s #%d", pluginName, i + 1));
    }
    
  }
  
}
