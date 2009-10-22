package net.sf.jcgm.examples;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Example program to convert a CGM file to another graphic format.
 * @version $Id:  $ 
 * @author  xphc
 * @since Oct 15, 2009
 */
public class Convert {
	public static void main(String[] args) {
		try {
			File cgmFile = new File("samples/allelm01.cgm");
			File outFile = new File("samples/allelm01.png");
			BufferedImage image = ImageIO.read(cgmFile);
			ImageIO.write(image, "PNG", outFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
