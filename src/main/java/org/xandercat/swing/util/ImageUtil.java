package org.xandercat.swing.util;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * General purpose Image utility class.
 * 
 * @author Scott Arnold
 */
public class ImageUtil {

	private static final Logger log = LogManager.getLogger(ImageUtil.class);
	
	// image formats that are safe to use with ImageIO
	public static final Set<String> VALID_IMAGE_FORMATS = new HashSet<String>();
	
	public static class SizeObserver extends Thread implements ImageObserver {

		private Image image;
		private CountDownLatch countDownLatch;
		private volatile int width = -1;
		private volatile int height = -1;
		private volatile boolean started;
		
		private SizeObserver(Image image) {
			this.image = image;
			this.countDownLatch = new CountDownLatch(2);
		}
		
		public void run() {
			this.started = true;
			int width = image.getWidth(this);
			if (width >= 0) {
				this.width = width;
				this.countDownLatch.countDown();
			}
			int height = image.getHeight(this);
			if (height >= 0) {
				this.height = height;
				this.countDownLatch.countDown();
			}
		}
		
		public Dimension getImageSize() throws InterruptedException {
			if (!started) {
				start();
			}
			this.countDownLatch.await();
			return new Dimension(width, height);
		}
		
		public boolean imageUpdate(Image image, int infoflags, int x, int y, int width, int height) {
			log.debug("image update: " + infoflags);
			if ((infoflags & ImageObserver.WIDTH) > 0) {
				this.width = width;
				this.countDownLatch.countDown();
			} 
			if ((infoflags & ImageObserver.HEIGHT) > 0) {
				this.height = height;
				this.countDownLatch.countDown();
			}
			return width < 0 || height < 0;
		}
		
	}
	
	static {
		VALID_IMAGE_FORMATS.add("jpg");
		VALID_IMAGE_FORMATS.add("png");
		VALID_IMAGE_FORMATS.add("jpeg");
		VALID_IMAGE_FORMATS.add("gif");
		VALID_IMAGE_FORMATS.add("bmp");
		VALID_IMAGE_FORMATS.add("tiff");
	}
	
	/**
	 * Returns the size of an image.  Method call will not return until the size is
	 * available or the image load is interrupted.  If image load is interrupted,
	 * a value of null is returned.
	 * 
	 * @param image			image to get size of
	 * 
	 * @return				size of image, or null if image load interrupted
	 */
	public static Dimension getImageSize(Image image) {
		SizeObserver sizeObserver = new SizeObserver(image);
		try {
			return sizeObserver.getImageSize();
		} catch (InterruptedException ie) {
			log.warn("Getting image size interrupted", ie);
			return null;
		}
		
	}
	
	/**
	 * Convert the given image into a buffered image.
	 * 
	 * @param image		image to convert
	 * 
	 * @return			buffered image of the given image
	 */
	public static BufferedImage convertToBufferedImage(Image image) {
		BufferedImage bufferedImage = null;
		int imageType = BufferedImage.TYPE_INT_ARGB;
		if (image instanceof BufferedImage) {
			int oldImageType = ((BufferedImage) image).getType();
			if (oldImageType != 0) {	// type 0 is for custom image and cannot be used
				imageType = oldImageType;
			}
		}
		Dimension imageSize = getImageSize(image);
		try {
			bufferedImage = new BufferedImage(imageSize.width, imageSize.height, imageType);
		} catch (IllegalArgumentException iae) {
			log.debug("Unable to create new buffered image of type " + imageType + "; using default type");
			bufferedImage = new BufferedImage(imageSize.width, imageSize.height, BufferedImage.TYPE_INT_ARGB);
		}
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.drawImage(image, 0, 0, null);		//TODO: Probably should use an image observer here
		g2d.dispose();
		return bufferedImage;
	}
	
	/**
	 * Return a scaled image of the given image scaled to the given size.  If preserving aspect,
	 * the returned image will still be of the new size indicated, but with the image centered
	 * in the scaled image.  
	 * 
	 * @param image				image to scale
	 * @param newSize			size scaled image should be
	 * @param preserveAspect	whether or not to preserve the aspect ratio of the original image
	 * 
	 * @return					scaled image
	 */
	public static BufferedImage scaleImage(Image image, Dimension newSize, boolean preserveAspect) {
		// note: if you specify type ARGB and it's a JPEG and then it gets written out using ImageIO,
		// the image can appear red.  To avoid this, we just use the same image type as the input image
		// which I believe will work in pretty much all cases.
		BufferedImage scaledImage = null;
		int imageType = BufferedImage.TYPE_INT_ARGB;
		if (image instanceof BufferedImage) {
			int oldImageType = ((BufferedImage) image).getType();
			if (oldImageType != 0) {	// type 0 is custom format that cannot be used in the creation of a new buffered image
				imageType = oldImageType;
			}
		}
		try {
			scaledImage = new BufferedImage(newSize.width, newSize.height, imageType);
		} catch (Exception e) {
			log.warn("Unable to create new buffered image of type " + imageType + "; using default type", e);
			scaledImage = new BufferedImage(newSize.width, newSize.height, BufferedImage.TYPE_INT_ARGB);
		}
		Graphics2D g2d = scaledImage.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		int a = 0, b = 0;
		int x = newSize.width;
		int y = newSize.height;
		Dimension imageSize = getImageSize(image);
		if (imageSize != null) {
			int imageWidth = imageSize.width;
			int imageHeight = imageSize.height;
			if (preserveAspect) {
				double imageAspect = (double) imageWidth / (double) imageHeight;
				double scaledAspect = newSize.getWidth() / newSize.getHeight();
				if (imageAspect > scaledAspect) {
					int reducedHeight = Math.round((float) imageHeight * (float) newSize.getWidth() / (float) imageWidth); 
					b = (newSize.height - reducedHeight) / 2;
					y -= b;
				} else {
					int reducedWidth = Math.round((float) imageWidth * (float) newSize.getHeight() / (float) imageHeight);
					a = (newSize.width - reducedWidth) / 2;
					x -= a;
				}
			}
			g2d.drawImage(image, a, b, x, y, 0, 0, imageWidth, imageHeight, null);
			g2d.dispose();
		}
		return scaledImage;
	}
	
	/**
	 * Returns a new buffered image that is the given buffered image rotated by the given number of degrees.
	 * 
	 * @param image			input image
	 * @param degrees		degrees to rotate image
	 * 
	 * @return				new image that is the original image rotated the specified number of degrees
	 */
	public static BufferedImage rotateImage(BufferedImage image, double degrees) {
		BufferedImage outputImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = outputImage.createGraphics();
		g2d.rotate(Math.toRadians(degrees), image.getWidth()/2d, image.getHeight()/2d);
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();	
		return outputImage;
	}
	
	/**
	 * Returns a new ImageIcon created by overlaying the baseIcon with the overlayIcon.
	 * 
	 * @param baseIcon			base icon
	 * @param overlayIcon		overlay icon
	 * 
	 * @return					combined icon
	 */
	public static ImageIcon overlayIcon(Icon baseIcon, Icon overlayIcon) {
		BufferedImage workImage = new BufferedImage(baseIcon.getIconWidth(), baseIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = workImage.createGraphics();
		baseIcon.paintIcon(null, g2d, 0, 0);
		overlayIcon.paintIcon(null, g2d, 
				baseIcon.getIconWidth() - overlayIcon.getIconWidth(), 
				baseIcon.getIconHeight() - overlayIcon.getIconHeight());
		g2d.dispose();
		return new ImageIcon(workImage);		
	}
}
