package at.sschmid.hcc.sbv1;

public class Checkerboard {

	private static final byte DEFAULT_SEGMENT_SIZE = 4;

	private final byte segmentSize;
	private final int[][] imgA;
	private final int[][] imgB;
	private final int width;
	private final int height;

	private int[][] result;

	public Checkerboard(final int[][] imgA, final int[][] imgB, final int width, final int height) {
		this(imgA, imgB, width, height, DEFAULT_SEGMENT_SIZE);
	}

	public Checkerboard(final int[][] imgA, final int[][] imgB, final int width, final int height, byte segmentSize) {
		super();
		this.imgA = imgA;
		this.imgB = imgB;
		this.width = width;
		this.height = height;
		this.segmentSize = segmentSize;
	}

	public int[][] getCheckerboard() {
		return result;
	}

	public void generate() {
		final int[][] checkerboard = new int[width][height];

		final int segmentWidth = getSegmentSizeQuotient(width);
		final int segmentHeight = getSegmentSizeQuotient(height);

//		System.out.println("Checkerboard");
//		System.out.println(" > Image-Size:    " + width + " x " + height);
//		System.out.println(" > Segments-Size: " + segmentWidth + " x " + segmentHeight);

//		int segmentIndex = 0;
		int currentSegWidth = 0;
		int currentSegHeight = 0;
		int[][] currentImg = imgA;
		for (int x = 0; x < width; x++) {			
			for (int y = 0; y < height; y++) {				
				checkerboard[x][y] = currentImg[x][y]; // segmentIndex % 2 != 0 ? imgA[x][y] : imgB[x][y];

//				if (y % segmentHeight == 0) {
//					segmentIndex++;
//				}
				if (++currentSegHeight >= segmentHeight) {
					currentSegHeight = 0;
//					segmentIndex++;
					currentImg = currentImg == imgA ? imgB : imgA;
				}
			}

//			if (x % segmentWidth == 0) {
//				segmentIndex++;
//			}
			if (++currentSegWidth >= segmentWidth)  {
				currentSegWidth = 0;
//				segmentIndex++;
				currentImg = currentImg == imgA ? imgB : imgA;
			}
		}

		result = checkerboard;
	}

	public void show() {
		if (result != null) {
			ImageJUtility.showNewImage(result, width, height, "Checkerboard");
		} else {
			throw new IllegalStateException("The checkboard is not yet available");
		}
	}

	private int getSegmentSizeQuotient(int size) {
		while (size % segmentSize != 0) {
			size--;
		}

		return size / segmentSize;
	}

}
