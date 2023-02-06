package org.xandercat.swing.panel;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.xandercat.swing.util.ImageUtil;

/**
 * JPanel with background images for a more designer look.  Any number of background images
 * can be specified.  Background images are rendered before other content and will be rendered 
 * in the order they are added to the panel.  Since background images are rendered before
 * other content, you may need to make some or all components within the panel non-opaque.
 * 
 * @author Scott Arnold
 */
public class DesignerPanel extends JPanel {

	private static final long serialVersionUID = 2010072101L;
	
	private static class ImageDatum {
		private Image image;
		private BufferedImage processedImage;
		private RescaleOp rescaleOp;
		private float opacity;
		private boolean scaleToFit;		// whether or not to scale image to fit panel size
		private boolean preserveAspect;	// whether or not to preserve aspect ratio when scaling image
		private boolean tiled;			// whether or not to tile image
		private int location;			// valid only when not tiled and not perfectly scaled to fit
		private Dimension offset;		// additional offset from computed location
	}
	
	private Dimension processedSize;
	private List<ImageDatum> imageData = new ArrayList<ImageDatum>();
	
	public DesignerPanel() {
		super();
		setOpaque(false);
	}

	public DesignerPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		setOpaque(false);
	}

	public DesignerPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		setOpaque(false);
	}

	public DesignerPanel(LayoutManager layout) {
		super(layout);
		setOpaque(false);
	}
	
	/**
	 * Adds a tiled image to the panel background.
	 * 
	 * @param image				image to tile in background
	 * @param opacity			opacity
	 */
	public void addTiledImage(Image image, float opacity) {
		ImageDatum imageDatum = new ImageDatum();
		imageDatum.image = image;
		imageDatum.opacity = opacity;
		imageDatum.tiled = true;
		this.imageData.add(imageDatum);
		processImages();
	}
	
	/**
	 * Adds a tiled image to the panel background.
	 * 
	 * @param imageURL			URL for image to tile in background
	 * @param opacity			opacity from 0.0 to 1.0
	 * 
	 * @throws IOException
	 */	
	public void addTiledImage(URL imageURL, float opacity) throws IOException {
		BufferedImage image = ImageIO.read(imageURL);
		addTiledImage(image, opacity);
	}

	/**
	 * Adds a single image to the panel background scaled to the given size.
	 * 
	 * @param image				image
	 * @param scaleToSize		size to scale image to
	 * @param preserveAspect	whether or not to preserve image aspect when scaling
	 * @param location			location as defined by a directional constant in the SwingConstants class
	 * @param opacity			opacity from 0.0 to 1.0
	 */
	public void addSingleImage(Image image, Dimension scaleToSize, boolean preserveAspect, int location, float opacity) {
		image = ImageUtil.scaleImage(image, scaleToSize, preserveAspect);
		addSingleImage(image, false, true, location, opacity, null);
	}
	
	/**
	 * Adds a single image to the panel background scaled to the given size.
	 * 
	 * @param imageURL			URL for image
	 * @param scaleToSize		size to scale image to
	 * @param preserveAspect	whether or not to preserve image aspect when scaling
	 * @param location			location as defined by a directional constant in the SwingConstants class
	 * @param opacity			opacity from 0.0 to 1.0
	 * 
	 * @throws IOException
	 */	
	public void addSingleImage(URL imageURL, Dimension scaleToSize, boolean preserveAspect, int location, float opacity) throws IOException {
		BufferedImage image = ImageIO.read(imageURL);
		addSingleImage(image, scaleToSize, preserveAspect, location, opacity, null);
	}
	
	/**
	 * Adds a single image to the panel background scaled to the given size.
	 * 
	 * @param image				image
	 * @param scaleToSize		size to scale image to
	 * @param preserveAspect	whether or not to preserve image aspect when scaling
	 * @param location			location as defined by a directional constant in the SwingConstants class
	 * @param opacity			opacity from 0.0 to 1.0
	 * @param offset			additional offset for image
	 */
	public void addSingleImage(Image image, Dimension scaleToSize, boolean preserveAspect, int location, float opacity, Dimension offset) {
		image = ImageUtil.scaleImage(image, scaleToSize, preserveAspect);
		addSingleImage(image, false, true, location, opacity, offset);
	}

	/**
	 * Adds a single image to the panel background scaled to the given size.
	 * 
	 * @param imageURL			URL for image
	 * @param scaleToSize		size to scale image to
	 * @param preserveAspect	whether or not to preserve image aspect when scaling
	 * @param location			location as defined by a directional constant in the SwingConstants class
	 * @param opacity			opacity from 0.0 to 1.0
	 * @param offset			additional offset for image
	 * 
	 * @throws IOException
	 */
	public void addSingleImage(URL imageURL, Dimension scaleToSize, boolean preserveAspect, int location, float opacity, Dimension offset) throws IOException {
		BufferedImage image = ImageIO.read(imageURL);
		addSingleImage(image, scaleToSize, preserveAspect, location, opacity, offset);
	}
	
	/**
	 * Adds a single image to the panel background.
	 * 
	 * @param image				image
	 * @param scaleToFit		whether or not to scale image to fit panel
	 * @param preserveAspect	whether or not to preserve image aspect when scaling to fit
	 * @param location			location as defined by a directional constant in the SwingConstants class
	 * @param opacity			opacity from 0.0 to 1.0
	 * @param offset			additional offset for image
	 */
	public void addSingleImage(Image image, boolean scaleToFit, boolean preserveAspect, int location, float opacity, Dimension offset) {
		ImageDatum imageDatum = new ImageDatum();
		imageDatum.image = image;
		imageDatum.preserveAspect = preserveAspect;
		imageDatum.scaleToFit = scaleToFit;
		imageDatum.location = location;
		imageDatum.opacity = opacity;
		if (offset != null) {
			imageDatum.offset = new Dimension(offset);
		}
		this.imageData.add(imageDatum);
		processImages();
	}

	/**
	 * Adds a single image to the panel background.
	 * 
	 * @param image				image
	 * @param scaleToFit		whether or not to scale image to fit panel
	 * @param preserveAspect	whether or not to preserve image aspect when scaling to fit
	 * @param location			location as defined by a directional constant in the SwingConstants class
	 * @param opacity			opacity from 0.0 to 1.0
	 */
	public void addSingleImage(Image image, boolean scaleToFit, boolean preserveAspect, int location, float opacity) {
		addSingleImage(image, scaleToFit, preserveAspect, location, opacity, null);
	}

	/**
	 * Adds a single image to the panel background.
	 * 
	 * @param imageURL			image URL
	 * @param scaleToFit		whether or not to scale image to fit panel
	 * @param preserveAspect	whether or not to preserve image aspect when scaling to fit
	 * @param location			location as defined by a directional constant in the SwingConstants class
	 * @param opacity			opacity from 0.0 to 1.0
	 * @param offset			additional offset for image
	 * 
	 * @throws IOException
	 */
	public void addSingleImage(URL imageURL, boolean scaleToFit, boolean preserveAspect, int location, float opacity, Dimension offset) throws IOException {
		BufferedImage image = ImageIO.read(imageURL);
		addSingleImage(image, scaleToFit, preserveAspect, location, opacity, offset);
	}
	
	/**
	 * Adds a single image to the panel background.
	 * 
	 * @param imageURL			image URL
	 * @param scaleToFit		whether or not to scale image to fit panel
	 * @param preserveAspect	whether or not to preserve image aspect when scaling to fit
	 * @param location			location as defined by a directional constant in the SwingConstants class
	 * @param opacity			opacity from 0.0 to 1.0
	 */
	public void addSingleImage(URL imageURL, boolean scaleToFit, boolean preserveAspect, int location, float opacity) throws IOException {
		addSingleImage(imageURL, scaleToFit, preserveAspect, location, opacity, null);
	}
	
	private void processImages() {
		this.processedSize = new Dimension(getSize());
		for (ImageDatum imageDatum : this.imageData) {
			if (!imageDatum.tiled) { 
				if (imageDatum.scaleToFit && this.processedSize.height > 0 && this.processedSize.width > 0) {
					imageDatum.processedImage = ImageUtil.scaleImage(imageDatum.image, this.processedSize, imageDatum.preserveAspect);
				} else {
					imageDatum.processedImage = ImageUtil.convertToBufferedImage(imageDatum.image);
				}
				float[] scales = new float[] { 1f, 1f, 1f, imageDatum.opacity };
				float[] offsets = new float[4];
				imageDatum.rescaleOp = new RescaleOp(scales, offsets, null); 
			} 
		}
	}
	
	@Override
	protected void paintComponent(Graphics graphics) {
		if (!getSize().equals(this.processedSize)) {
			processImages();
		}
		Graphics2D g2d = (Graphics2D) graphics;
		for (ImageDatum imageDatum : this.imageData) {
			if (imageDatum.tiled) {
				Composite oldComp = g2d.getComposite();
				Composite alphaComp = AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, imageDatum.opacity);
				g2d.setComposite(alphaComp);
				int width = getWidth();
				int height = getHeight();
				int imageW = imageDatum.image.getWidth(this);
				int imageH = imageDatum.image.getHeight(this);
			    for (int x = 0; x < width; x += imageW) {
			        for (int y = 0; y < height; y += imageH) {
			            graphics.drawImage(imageDatum.image, x, y, this);
			        }
			    }				
			    g2d.setComposite(oldComp);
			} else {
				int x = 0;
				int y = 0;
				int offsetX = (imageDatum.offset == null)? 0 : imageDatum.offset.width;
				int offsetY = (imageDatum.offset == null)? 0 : imageDatum.offset.height;
				if (!imageDatum.scaleToFit || imageDatum.preserveAspect) {
					int imageWidth = imageDatum.processedImage.getWidth();
					int imageHeight = imageDatum.processedImage.getHeight();
					switch (imageDatum.location) {
					case SwingConstants.NORTH:
					case SwingConstants.CENTER:
					case SwingConstants.SOUTH:
						x = (this.processedSize.width - imageWidth) / 2;
						break;
					case SwingConstants.NORTH_EAST:
					case SwingConstants.EAST:
					case SwingConstants.SOUTH_EAST:
						x = this.processedSize.width - imageWidth;
						break;
					}
					switch (imageDatum.location) {
					case SwingConstants.WEST:
					case SwingConstants.CENTER:
					case SwingConstants.EAST:
						y = (this.processedSize.height - imageHeight) / 2;
						break;						
					case SwingConstants.SOUTH:
					case SwingConstants.SOUTH_EAST:
					case SwingConstants.SOUTH_WEST:
						y = this.processedSize.height - imageHeight;
						break;
					}
				}
				x += offsetX;
				y += offsetY;
				g2d.drawImage(imageDatum.processedImage, imageDatum.rescaleOp, x, y);
			}
		}
		super.paintComponent(graphics);
	}
}
