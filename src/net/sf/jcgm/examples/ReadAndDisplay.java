package net.sf.jcgm.examples;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import net.sf.jcgm.core.CGM;
import net.sf.jcgm.core.CGMDisplay;
import net.sf.jcgm.core.CGMPanel;

/**
 * Example on how to read and display a CGM file.
 * @version $Id: $
 * @author Philippe Cadé
 * @since Oct 15, 2009
 */
public class ReadAndDisplay {
	public static void main(String[] args) {
		try {
			JFrame frame = new JFrame("CGM Read And Display");
			File cgmFile = new File("samples/allelm01.cgm");

			InputStream inputStream;
			if (cgmFile.getName().endsWith(".cgmz") || cgmFile.getName().endsWith(".cgm.gz")) {
				inputStream = new GZIPInputStream(new FileInputStream(cgmFile));
			}
			else {
				inputStream = new FileInputStream(cgmFile);
			}
			
			// create an input stream for the CGM file
			DataInputStream in = new DataInputStream(new BufferedInputStream(inputStream));

			// read the CGM file
			CGM cgm = new CGM();
			cgm.read(in);
			in.close();

			// display the CGM file
			CGMDisplay display = new CGMDisplay(cgm);
			final CGMPanel cgmPanel = new CGMPanel(display);
			cgmPanel.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						cgmPanel.zoomIn();
					}
					else {
						cgmPanel.zoomOut();
					}
					cgmPanel.revalidate();
				}
			});

			JScrollPane scrollPane = new JScrollPane(cgmPanel);
			frame.getContentPane().add(scrollPane);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.pack();
			frame.setVisible(true);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
