package at.sschmid.hcc.sbv1.image;

class MathematicMorphology {
  
  private final Neighbour neighbour;
  
  MathematicMorphology(final Neighbour neighbour) {
    this.neighbour = neighbour;
  }
  
  Image erosion(final Image image) {
    final Image result = new Image(image.width, image.height);
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        boolean allNeighbours = true;
        for (int k = x - neighbour.r; k <= x + neighbour.r && allNeighbours; k++) {
          if (k >= 0 && k < image.width) {
            for (int l = y - neighbour.r; l <= y + neighbour.r && allNeighbours; l++) {
              if (l >= 0 && l < image.height && neighbour.value[k - (x - neighbour.r)][l - (y - neighbour.r)] == 1) {
                allNeighbours = image.data[k][l] == 255;
              }
            }
          }
        }
        
        if (allNeighbours) {
          result.data[x][y] = image.maxColor;
        }
      }
    }
    
    return result;
  }
  
  Image dilation(final Image image) {
    final Image result = new Image(image.width, image.height);
    for (int x = 0; x < image.width; x++) {
      for (int y = 0; y < image.height; y++) {
        boolean anyNeighbour = false;
        for (int k = x - neighbour.r; k <= x + neighbour.r && !anyNeighbour; k++) {
          if (k >= 0 && k < image.width) {
            for (int l = y - neighbour.r; l <= y + neighbour.r && !anyNeighbour; l++) {
              if (l >= 0 && l < image.height && neighbour.value[k - (x - neighbour.r)][l - (y - neighbour.r)] == 1) {
                anyNeighbour = image.data[k][l] == 255;
              }
            }
          }
        }
        
        if (anyNeighbour) {
          result.data[x][y] = image.maxColor;
        }
      }
    }
    
    return result;
  }
  
}
