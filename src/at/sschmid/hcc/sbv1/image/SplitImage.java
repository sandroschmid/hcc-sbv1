package at.sschmid.hcc.sbv1.image;

import java.util.*;

public final class SplitImage {
  
  private final int splitWidth;
  private final Set<Image> images;
  
  public SplitImage(final Image image) {
    this(image, 2);
  }
  
  public SplitImage(final Image image, final int n) {
    this.images = new LinkedHashSet<>(n, 1.1f);
    this.splitWidth = image.width / n;
    
    Image currentSplit;
    for (int i = 0; i < n; i++) {
      currentSplit = new Image(splitWidth, image.height);
      
      final int startX = i * splitWidth;
      final int endX = startX + splitWidth;
      for (int x = startX; x < endX; x++) {
        if (image.height >= 0) {
          System.arraycopy(image.data[x], 0, currentSplit.data[x - startX], 0, image.height);
        }
      }
      
      images.add(currentSplit);
    }
  }
  
  public int splitWidth() {
    return splitWidth;
  }
  
  public int count() {
    return images.size();
  }
  
  public boolean isEmpty() {
    return images.isEmpty();
  }
  
  public Optional<Image> first() {
    final Iterator<Image> iterator = images.iterator();
    return Optional.ofNullable(iterator.hasNext() ? iterator.next() : null);
  }
  
  public Optional<Image> last() {
    final Iterator<Image> iterator = images.iterator();
    Image lastImage = null;
    while (iterator.hasNext()) {
      lastImage = iterator.next();
    }
    return Optional.ofNullable(lastImage);
  }
  
  public Collection<Image> images() {
    return images;
  }
  
  public void show() {
    images.forEach(Image::show);
  }
  
}
