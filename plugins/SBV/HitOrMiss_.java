import at.sschmid.hcc.sbv1.image.*;
import ij.gui.GenericDialog;

public final class HitOrMiss_ extends AbstractUserInputPlugIn<HitOrMiss_.Input> {
  
  @Override
  protected void process(final Image image) {
    final int[][] structure = getStructure();
    final Segmentation segmentation = image.segmentation();
    final Image hits = segmentation.hitOrMiss(structure);
    final Image grownHits = segmentation.hitAndGrow(structure, Neighbour.N8, new BinaryThreshold(255, 0, 255));
  
    addResult(hits, String.format("%s - hits (%dx%d)", pluginName, input.rectWidth, input.rectHeight));
    addResult(grownHits, String.format("%s - detected objects (%dx%d)", pluginName, input.rectWidth, input.rectHeight));
  }
  
  @Override
  protected void setupDialog(final GenericDialog dialog) {
    dialog.addSlider("Width of rectangle", 1, 100, 5);
    dialog.addSlider("Height of rectangle", 1, 100, 5);
  }
  
  @Override
  protected Input getInput(final GenericDialog dialog) {
    return new Input((int) dialog.getNextNumber(), (int) dialog.getNextNumber());
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
    
    private final int rectWidth;
    private final int rectHeight;
    
    Input(final int rectWidth, final int rectHeight) {
      this.rectWidth = rectWidth;
      this.rectHeight = rectHeight;
    }
    
    @Override
    public String toString() {
      return String.format("Input {\n  rectWidth=%s,\n  rectHeight=%s\n}", rectWidth, rectHeight);
    }
    
  }
  
}
