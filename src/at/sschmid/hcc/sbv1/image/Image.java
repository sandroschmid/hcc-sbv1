package at.sschmid.hcc.sbv1.image;

import at.sschmid.hcc.sbv1.image.imagej.ImageJUtility;
import at.sschmid.hcc.sbv1.image.registration.DistanceMap;
import at.sschmid.hcc.sbv1.image.registration.DistanceMetric;
import at.sschmid.hcc.sbv1.image.resampling.Interpolation;
import at.sschmid.hcc.sbv1.image.resampling.Transformation;
import at.sschmid.hcc.sbv1.image.segmentation.BinaryThreshold;
import at.sschmid.hcc.sbv1.image.segmentation.Segment;
import at.sschmid.hcc.sbv1.image.segmentation.Segmentation;
import at.sschmid.hcc.sbv1.utility.Point;
import at.sschmid.hcc.sbv1.utility.Utility;

import java.util.ArrayList;
import java.util.List;

public final class Image {
  
  private static final String DEFAULT_NAME_FORMAT = "Image %02d";
  private static final int DEFAULT_MAX_COLOR = 255;
  
  private static int totalImages = 0;
  
  public final int maxColor;
  public final int[][] data;
  public final int width;
  public final int height;
  public final int size;
  
  private final int imageCount;
  
  private String name;
  
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
  
  public Image(final double[][] data, final int width, final int height) {
    this(ImageJUtility.convertToIntArr2D(data, width, height), width, height);
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
  
  public Image binary(final BinaryThreshold binaryThreshold) {
    final int[] tf = binaryThreshold.getTransformFunction(maxColor);
    final Image result = transformation().transfer(tf).getResult();
    
    return result.withName(String.format("%s - (binary)", getName()));
  }
  
  public Image edges() {
    final double[][] inDataArrDbl = ImageJUtility.convertToDoubleArr2D(this);
    final double[][] resultEdgeImage = ConvolutionFilter.ApplySobelEdgeDetection(inDataArrDbl, width, height);
    int[][] resultData = ImageJUtility.convertToIntArr2D(resultEdgeImage, width, height);
    
    return new Image(String.format("%s - Sobel Edges", getName()), resultData, width, height);
  }
  
  public DistanceMap distanceMap(final DistanceMetric distanceMetric) {
    return new DistanceMap(this, distanceMetric);
  }
  
  public Histogram histogram() {
    return new Histogram(this);
  }
  
  public Histogram2d histogram2d(final Image other) {
    return new Histogram2d(this, other);
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
  
  public Segmentation segmentation() {
    return new Segmentation(this);
  }
  
  public Checkerboard checkerboard(final Image other) {
    return new Checkerboard(this, other);
  }
  
  public ImageCalculation calculation(final Image other) {
    return new ImageCalculation(this, other);
  }
  
  public Segment.Builder segment() {
    return Segment.create(this);
  }
  
  public List<Segment> getSegments(final int segmentWidth, final int segmentHeight) {
    final List<Segment> segments = new ArrayList<>();
    final Segment.Builder segmentBuilder = segment().width(segmentWidth).height(segmentHeight);
    for (int x = 0; x < width; x += segmentWidth) {
      for (int y = 0; y < height; y += segmentHeight) {
        segments.add(segmentBuilder.origin(new Point(x, y)).build());
      }
    }
    
    return segments;
  }
  
  public boolean sizeEqualsTo(final Image other) {
    return width == other.width && height == other.height;
  }
  
  public double entropy() {
    final double[] probabilities = histogram().getProbabilities();
    double sum = .0d;
    for (final double probability : probabilities) {
      if (probability > 0) {
        sum += probability * Utility.binLog(probability);
      }
    }
    
    return -sum;
  }
  
  public double entropy2d(final Image other) {
    final double[][] probabilities = histogram2d(other).getProbabilities();
    double sum = 0d;
    for (int colorX = 0; colorX < probabilities.length; colorX++) {
      for (int colorY = 0; colorY < probabilities[colorX].length; colorY++) {
        final double probability = probabilities[colorX][colorY];
        if (probability > 0) {
          sum += probability * Utility.binLog(probability);
        }
      }
    }
    
    return -sum;
  }
  
  public SplitImage split() {
    return new SplitImage(this);
  }
  
  public SplitImage split(final int n) {
    return new SplitImage(this, n);
  }
  
  public void show() {
    show(getName());
  }
  
  public void show(final String label) {
    ImageJUtility.showNewImage(data, width, height, label);
  }
  
  @Override
  public String toString() {
    return new StringBuilder(hasName() ? getName() : getClass().getSimpleName())
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
