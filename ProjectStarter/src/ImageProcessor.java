import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.*;

public class ImageProcessor {

	// Create a clone of a buffered image
	// (The BufferedImage class describes an Image with an accessible buffer of
	// image data.)
	public static BufferedImage copy(BufferedImage img) {
		BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return bi;
	}

	// Create a clone of a buffered image
	// (Another implementation)
	/*
	 * public static BufferedImage copy(BufferedImage img) { ColorModel cm =
	 * img.getColorModel(); boolean isAlphaPremultiplied =
	 * cm.isAlphaPremultiplied(); WritableRaster raster = img.copyData(null); return
	 * new BufferedImage(cm, raster, isAlphaPremultiplied, null); }
	 */

	// Convert an input color image to grayscale image
	public static BufferedImage convertToGrayScale(BufferedImage src) {
		// Make a copy of the source image as the target image
		BufferedImage target = copy(src);
		int width = target.getWidth();
		int height = target.getHeight();

		// Scan through each row of the image
		for (int j = 0; j < height; j++) {
			// Scan through each column of the image
			for (int i = 0; i < width; i++) {
				// Get an integer pixel in the default RGB color model
				int pixel = target.getRGB(i, j);
				// Convert the single integer pixel value to RGB color
				Color oldColor = new Color(pixel);

				int red = oldColor.getRed(); // get red value
				int green = oldColor.getGreen(); // get green value
				int blue = oldColor.getBlue(); // get blue value

				// Convert RGB to grayscale using formula
				// gray = 0.299 * R + 0.587 * G + 0.114 * B
				double grayVal = 0.299 * red + 0.587 * green + 0.114 * blue;

				// Assign each channel of RGB with the same value
				Color newColor = new Color((int) grayVal, (int) grayVal, (int) grayVal);

				// Get back the integer representation of RGB color and assign it back to the
				// original position
				target.setRGB(i, j, newColor.getRGB());
			}
		}
		// return the resulting image in BufferedImage type
		return target;
	}

	// Invert the color of an input image
	public static BufferedImage invertColor(BufferedImage src) {
		BufferedImage target = copy(src);
		int width = target.getWidth();
		int height = target.getHeight();

		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				int pixel = target.getRGB(i, j);
				Color oldColor = new Color(pixel);

				int red = oldColor.getRed();
				int green = oldColor.getGreen();
				int blue = oldColor.getBlue();

				// invert the color of each channel
				Color newColor = new Color(255 - red, 255 - green, 255 - blue);

				target.setRGB(i, j, newColor.getRGB());
			}
		}
		return target;
	}

	// Adjust the brightness of an input image
	public static BufferedImage adjustBrightness(BufferedImage src, int amount) {
		BufferedImage target = copy(src);
		int width = target.getWidth();
		int height = target.getHeight();

		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				int pixel = target.getRGB(i, j);
				Color oldColor = new Color(pixel);

				int red = oldColor.getRed();
				int green = oldColor.getGreen();
				int blue = oldColor.getBlue();

				int newRed = (red + amount > 255) ? 255 : red + amount;
				int newGreen = (green + amount > 255) ? 255 : green + amount;
				int newBlue = (blue + amount > 255) ? 255 : blue + amount;

				newRed = (newRed < 0) ? 0 : newRed;
				newGreen = (newGreen < 0) ? 0 : newGreen;
				newBlue = (newBlue < 0) ? 0 : newBlue;

				Color newColor = new Color(newRed, newGreen, newBlue);

				target.setRGB(i, j, newColor.getRGB());
			}
		}
		return target;
	}

	// Apply a blur effect to an input image by random pixel movement
	public static BufferedImage blur(BufferedImage src, int offset) {

		// Make a copy of the source image as the target image
		BufferedImage target = copy(src);
		int width = target.getWidth();
		int height = target.getHeight();

		// Scan through each row of the image
		for (int j = 0; j < height; j++) {
			// Scan through each column of the image
			for (int i = 0; i < width; i++) {
				int pixel = target.getRGB(i, j);
				Color oldColor = new Color(pixel);

				// The formula of blur
				int offsetX = (int) (Math.random() * offset) - offset / 2;
				int offsetY = (int) (Math.random() * offset) - offset / 2;

				// Get the image of blur
				if (i + offsetX < width && j + offsetY < height && i + offsetX > 0 && j + offsetY > 0)
					target.setRGB(i + offsetX, j + offsetY, oldColor.getRGB());

			}
		}

		// Return the resulting image in BufferedImage type
		return target;
	}

	// Scale (resize) an image
	public static BufferedImage scale(BufferedImage src, int tWidth, int tHeight) {

		// Make a copy of the source image as the target image
		BufferedImage target = copy(src);
		int width = target.getWidth();
		int height = target.getHeight();

		// Set the size of the image
		target = new BufferedImage(tWidth, tHeight, src.getType());

		// Create a new scale image
		Graphics2D g = target.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(src, 0, 0, tWidth, tHeight, 0, 0, width, height, null);
		g.dispose();

		// return the resulting image in BufferedImage type
		return target;
	}

	// Rotate an image by angle degrees clockwise
	public static BufferedImage rotate(BufferedImage src, double angle) {

		// Make a copy of the source image as the target image
		BufferedImage target = copy(src);
		int width = target.getWidth();
		int height = target.getHeight();

		// the angle of rotation
		angle = Math.toRadians(-angle);
		// the center of the image
		double x = 0.5 * (width);
		double y = 0.5 * (height);

		// Make a copy of the source image as the target2 image to do rotation
		BufferedImage target2 = copy(src);

		// Scan through each row of the image
		for (int j = 0; j < height; j++) {
			// Scan through each column of the image
			for (int i = 0; i < width; i++) {

				// The formula of rotation
				int a = (int) ((i - x) * Math.cos(angle) - (j - y) * Math.sin(angle) + x);
				int b = (int) ((i - x) * Math.sin(angle) + (j - y) * Math.cos(angle) + y);

				// Set the background be black color
				Color color = new Color(0, 0, 0);
				target.setRGB(i, j, color.getRGB());

				// Get the image of rotation when the pixel is same
				if (a >= 0 && a < width && b >= 0 && b < height) {
					target.setRGB(i, j, target2.getRGB(a, b));
				}
			}
		}
		// Return the resulting image in BufferedImage type
		return target;
	}

	// Apply a swirl effect to an input image
	public static BufferedImage swirl(BufferedImage src, double degree) {

		// Make a copy of the source image as the target image
		BufferedImage target = copy(src);
		int width = target.getWidth();
		int height = target.getHeight();
		
		//Compute the midpoint coordinate of the image
		double midX = width / 2;
		double midY = height / 2;
		
		// Make a copy of the source image as the target2 image to do swirl
		BufferedImage target2 = copy(src);
		
		// Scan through each row of the image
		for (int j = 0; j < height; j++) {
			// Scan through each column of the image
			for (int i = 0; i < width; i++) {

				// The formula of swirl
				float dx = (float) (i - midX);
				float dy = (float) (j - midY);
				float theta = (float) Math.atan2(dx, dy);
				float radius = (float) Math.sqrt((dx * dx) + (dy * dy));
				int x = (int) (midX + radius * Math.cos(theta + degree * radius));
				int y = (int) (midX + radius * Math.sin(theta + degree * radius));
				
				// Get the image of rotation when the pixel is same
				if (x >= 0 && x < width && y >= 0 && y < height) {
					target.setRGB(i, j, target2.getRGB(x, y));
				}
			}
		}
		// Return the resulting image in BufferedImage type
		return target;
	}

	// Apply an effect to preserve partial colors of an image
	public static BufferedImage preserveColor(BufferedImage src, boolean[][] mask, int colorVal, int rgValue,
			int gbValue, int brValue) {

		// Make a copy of the source image as the target image
		BufferedImage target = copy(src);
		int width = target.getWidth();
		int height = target.getHeight();
		System.out.print(colorVal);

		// Scan through each row of the image
		for (int j = 0; j < height; j++) {
			// Scan through each column of the image
			for (int i = 0; i < width; i++) {

				// Get an integer pixel in the default RGB color model
				int pixel = target.getRGB(i, j);

				// Convert the single integer pixel value to RGB color
				Color oldColor = new Color(pixel);

				// Get the number of original colour values
				int red = oldColor.getRed();
				int green = oldColor.getGreen();
				int blue = oldColor.getBlue();
				Color preservedColor = new Color(colorVal);

				// Obtain colorVal via a mouse click
				int redP = preservedColor.getRed();
				int greenP = preservedColor.getGreen();
				int blueP = preservedColor.getBlue();

				// Compute a number of colour values
				int diffRG, diffGB, diffBR;
				diffRG = redP - greenP;
				diffGB = greenP - blueP;
				diffBR = blueP - redP;

				int RGlow = diffRG - rgValue;
				int RGhigh = diffRG + rgValue;
				int GBlow = diffGB - gbValue;
				int GBhigh = diffGB + gbValue;
				int BRlow = diffBR - brValue;
				int BRhigh = diffBR + brValue;

				// If the pixel (x,y)¡¦s color falls into the color range similar to the selected
				// pixel (colorVal)
				if (red - green > RGlow && red - green < RGhigh && green - blue > GBlow && green - blue < GBhigh
						&& blue - red > BRlow && blue - red < BRhigh) {
					mask[i][j] = true;
					target.setRGB(i, j, pixel);
				}
				// Show the grey colour in non-click parts
				else {
					int grayVal = (int) ((int) 0.299 * red + 0.587 * green + 0.114 * blue);
					int greyRP = grayVal;
					int greyGP = grayVal;
					int greyBP = grayVal;
					int greyScaleRGB = new Color(greyRP, greyGP, greyBP).getRGB();
					target.setRGB(i, j, greyScaleRGB);
				}
			}
		}

		// Return the resulting image in BufferedImage type
		return target;
	}

	// Perform edge detection for an input image
	public static BufferedImage detectEdges(BufferedImage src) {

		return null; // temporary for passing compilation (remove it after added your code)
	}

	public static BufferedImage compressImage(BufferedImage inputImage) {
		// TODO Auto-generated method stub
		return null;
	}
}