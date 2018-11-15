package at.sschmid.hcc.sbv1.image;

public final class Image {
  
  private static final String DEFAULT_NAME_FORMAT = "Image %02d";
  private static final int DEFAULT_MAX_COLOR = 255;
  
  private static int totalImages = 0;
  
  public final int maxColor;
  public final int[][] data;
  public final int width;
  public final int height;
  public final int size;
  
  public String name;
  
  private final int imageCount;
  
  public Image(final Image image) {
    this(image, true);
  }
  
  public Image(final Image image, final boolean copyData) {
    this(image.name, null, image.width, image.height, image.maxColor);
    
    if (copyData) {
      for (int x = 0; x < image.width; x++) {
        if (image.height >= 0) {
          System.arraycopy(image.data[x], 0, data[x], 0, image.height);
        }
      }
    }
  }
  
  public Image(final int width, final int height) {
    this(null, null, width, height);
  }
  
  public Image(final String name, final int width, final int height) {
    this(name, null, width, height);
  }
  
  public Image(final int[][] data, final int width, final int height) {
    this(null, data, width, height);
  }
  
  public Image(final String name, final int[][] data, final int width, final int height) {
    this(name, data, width, height, DEFAULT_MAX_COLOR);
  }
  
  public Image(final String name, final int[][] data, final int width, final int height, final int maxColor) {
    this.imageCount = ++totalImages;
    this.maxColor = maxColor;
    this.name = name;
    this.data = data == null ? new int[width][height] : data;
    this.width = width;
    this.height = height;
    this.size = width * height;
  }
  
  public boolean hasName() {
    return name != null && !name.isEmpty();
  }
  
  public String getName() {
    return getName(getDefaultName());
  }
  
  public String getName(final String orElse) {
    return hasName() ? name : orElse;
  }
  
  public Image withName(final String name) {
    this.name = name;
    return this;
  }
  
  public Histogram histogram() {
    return new Histogram(this);
  }
  
  public Transformation transformation() {
    return new Transformation(this);
  }
  
  public Transformation transformation(final String name) {
    return new Transformation(this, name);
  }
  
  public Interpolation interpolation() {
    return new Interpolation(this);
  }
  
  public Checkerboard checkerboard(final Image other) {
    return new Checkerboard(this, other);
  }
  
  public ImageCalculation calculation(final Image other) {
    return new ImageCalculation(this, other);
  }
  
  public boolean sizeEqualsTo(final Image other) {
    return width == other.width && height == other.height;
  }
  
  public void show() {
    show(getName());
  }
  
  public void show(final String label) {
    ImageJUtility.showNewImage(data, width, height, label);
  }
  
  @Override
  public String toString() {
    return new StringBuilder(hasName() ? name : getClass().getSimpleName())
        .append(" (")
        .append(getDefaultName())
        .append(") { w=")
        .append(width)
        .append(", h=")
        .append(height)
        .append(" }")
        .toString();
  }
  
  private String getDefaultName() {
    return String.format(DEFAULT_NAME_FORMAT, imageCount);
  }
  
}
