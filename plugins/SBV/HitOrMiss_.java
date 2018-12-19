import at.sschmid.hcc.sbv1.image.Image;
import at.sschmid.hcc.sbv1.image.imagej.AbstractUserInputPlugIn;
import at.sschmid.hcc.sbv1.image.segmentation.BinaryThreshold;
import at.sschmid.hcc.sbv1.image.segmentation.Neighbour;
import at.sschmid.hcc.sbv1.image.segmentation.Segmentation;
import ij.gui.GenericDialog;

public final class HitOrMiss_ extends AbstractUserInputPlugIn<HitOrMiss_.Input> {
  
  @Override
  protected void process(final Image image) {
    final int[][] structure = getStructure();
    final Segmentation segmentation = image.segmentation();
    final Image hits = segmentation.hitOrMiss(structure, input.quality);
    final Image grownHits = segmentation.hitAndGrow(structure,
        input.quality,
        Neighbour.N8,
        new BinaryThreshold(255, 0, 255));
    
    addResult(hits, String.format("%s - hits (%dx%d)", pluginName, input.rectWidth, input.rectHeight));
    addResult(grownHits, String.format("%s - detected objects (%dx%d)", pluginName, input.rectWidth, input.rectHeight));
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addNumericField("Quality", 0.9, 1);
    dialog.addSlider("Width of rectangle", 1, 100, 3);
    dialog.addSlider("Height of rectangle", 1, 100, 9);
  }
  
  @Override
  protected Input getInput(final GenericDialog dialog) {
    return new Input(dialog.getNextNumber(), (int) dialog.getNextNumber(), (int) dialog.getNextNumber());
  }
  
  private int[][] getStructure() {
    final int width = input.rectWidth + 2;
    final int height = input.rectHeight + 2;
    final int[][] structure = new int[width][height];
    System.out.println(String.format("%dx%d-Structure", input.rectWidth, input.rectHeight));
    for (int x = 0; x < width; x++) {
      System.out.print("|");
      for (int y = 0; y < height; y++) {
        structure[x][y] = x == 0 || x == width - 1 || y == 0 || y == height - 1 ? 0 : 1;
        System.out.print(structure[x][y] + "|");
      }
      System.out.println();
    }
    
    return structure;
  }
  
  static final class Input {
  
    private final double quality;
    private final int rectWidth;
    private final int rectHeight;
  
    Input(final double quality, final int rectWidth, final int rectHeight) {
      this.quality = quality;
      this.rectWidth = rectWidth;
      this.rectHeight = rectHeight;
    }
    
    @Override
    public String toString() {
      return String.format("Hit or Miss {\n  quality=%.2f,\n  rectWidth=%s,\n  rectHeight=%s\n}",
          quality,
          rectWidth,
          rectHeight);
    }
    
  }
  
}
