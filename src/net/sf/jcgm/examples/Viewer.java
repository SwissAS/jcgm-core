package net.sf.jcgm.examples;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.sf.jcgm.core.CGM;
import net.sf.jcgm.core.CGMPanel;

/**
 * Program to view CGM files and compare them to reference files.
 * @version $Id$
 * @author Philippe Cadé
 * @since Oct 15, 2009
 */
public class Viewer extends JFrame {
	private static final long serialVersionUID = 331859096071575308L;

	private File testDir;
	private final File referenceDir;
	private int currentIndex;
	private File[] cgmFiles;
	private JTextField testDirField;
	private JTextField currentFileField;
	private JTextField referenceDirField;
	private JTextField indexField;
	private CGMPanel cgmLabel;
	private JLabel referenceLabel;
	private JTextField dumpCommandFileField;
	private JButton prevButton;
	private JButton nextButton;

	public Viewer() {
		this.testDir = new File("d:/software/webcgm21-ts/static10");
		this.referenceDir = new File("d:/software/webcgm21-ts/static10/images");
		this.currentIndex = 0;

		initCgmFiles();

		buildUI();

		this.testDirField.setText(this.testDir.getPath());
		this.testDirField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Viewer.this.testDir = new File(Viewer.this.testDirField.getText());
				initCgmFiles();
				loadImage();
			}
		});

		this.currentFileField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				findCGMFile(Viewer.this.currentFileField.getText());
				loadImage();
			}
		});

		this.referenceDirField.setText(this.referenceDir.getPath());
		this.indexField.setText(String.valueOf(this.currentIndex));

		loadImage();
	}

	/**
	 * Looks for the given file name in the list of CGM files
	 * @param name
	 */
	private void findCGMFile(String name) {
		if (this.cgmFiles == null)
			return;

		int i = 0;
		while (i < this.cgmFiles.length) {
			if (this.cgmFiles[i].getName().compareToIgnoreCase(name) == 0) {
				this.currentIndex = i;
				break;
			}
			i++;
		}
	}

	private void loadImage() {
		if (this.cgmFiles == null || this.cgmFiles.length == 0)
			return;

		File cgmFile = this.cgmFiles[this.currentIndex];
		try {
			this.cgmLabel.open(cgmFile);
			this.cgmLabel.repaint();

			String refFileName = cgmFile.getName().replaceAll("\\.cgm$", ".png");
			File refFile = new File(this.referenceDir + File.separator + refFileName);
			if (refFile.exists()) {
				this.referenceLabel.setIcon(new ImageIcon(ImageIO.read(refFile)));
			}

			this.indexField.setText(String.valueOf(this.currentIndex));
			this.currentFileField.setText(cgmFile.getName());
		}
		catch (IOException e) {
			System.err.println(e.getMessage()+" "+cgmFile.getAbsolutePath());
		}
	}

	protected void initCgmFiles() {
		this.cgmFiles = this.testDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".cgm") || name.endsWith(".cgm.gz");
			}
		});

		if (this.cgmFiles == null || this.cgmFiles.length == 0) {
			System.err.println("Couldn't find any CGM files in given folder");
		}
		else {
			this.currentIndex = 0;
		}
	}

	private void buildUI() {
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gb = new GridBagConstraints();
		gb.weightx = 1; gb.weighty = 1;
		gb.gridx = 0; gb.gridy = 0;
		gb.fill = GridBagConstraints.BOTH;

		JPanel topPanel = new JPanel(new GridBagLayout());

		this.testDirField = new JTextField(50);
		topPanel.add(new JLabel("Test Folder"), gb);

		gb.weightx = 100;
		gb.gridx++;
		topPanel.add(this.testDirField, gb);

		gb.weightx = 1;
		gb.gridx++;
		JButton dumpCommands = new JButton("Dump Commands to File");
		dumpCommands.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dumpCommands();
			}
		});
		topPanel.add(dumpCommands, gb);

		gb.weightx = 100;
		gb.gridx++;
		this.dumpCommandFileField = new JTextField(50);
		this.dumpCommandFileField.setText("c:\\Documents and Settings\\xphc\\Desktop\\ammcommands.txt");
		topPanel.add(this.dumpCommandFileField, gb);

		gb.weightx = 1;
		gb.gridx = 0; gb.gridy++;
		topPanel.add(new JLabel("Current File"), gb);

		gb.weightx = 100;
		gb.gridx++;
		this.currentFileField = new JTextField(50);
		topPanel.add(this.currentFileField, gb);

		gb.weightx = 1;
		gb.gridx = 0; gb.gridy++;
		topPanel.add(new JLabel("Reference Folder"), gb);

		gb.weightx = 100;
		this.referenceDirField = new JTextField(50);
		gb.gridx++;
		topPanel.add(this.referenceDirField, gb);

		gb = new GridBagConstraints();
		gb.gridx = 0; gb.gridy = 0;
		gb.fill = GridBagConstraints.BOTH;
		gb.gridwidth = 2;
		mainPanel.add(topPanel, gb);

		this.cgmLabel = new CGMPanel();
		this.cgmLabel.setBackground(Color.RED);
		this.cgmLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					// zoom in
					Viewer.this.cgmLabel.zoomIn();
					Viewer.this.cgmLabel.revalidate();
				}
				else if (e.getButton() == MouseEvent.BUTTON3) {
					// zoom out
					Viewer.this.cgmLabel.zoomOut();
					Viewer.this.cgmLabel.revalidate();
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(this.cgmLabel);

		gb.gridwidth = 1;
		gb.weightx = 1;
		gb.weighty = 1;
		gb.fill = GridBagConstraints.BOTH;
		gb.gridx = 0;
		gb.gridy++;
		mainPanel.add(scrollPane, gb);

		this.referenceLabel = new JLabel();
		gb.weightx = 0.1;
		gb.gridx++;
		mainPanel.add(this.referenceLabel, gb);

		JPanel buttonPanel = new JPanel();

		this.prevButton = new JButton("Prev");
		this.prevButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				previousImage();
			}
		});
		buttonPanel.add(this.prevButton);

		this.indexField = new JTextField(10);
		this.indexField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					goToImage(Integer.parseInt(((JTextField)e.getSource()).getText()));
				}
				catch (NumberFormatException e1) {
					// ignore
				}
			}
		});
		buttonPanel.add(this.indexField);

		this.nextButton = new JButton("Next");
		this.nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextImage();
			}
		});
		buttonPanel.add(this.nextButton);

		gb.weighty = 0.01;
		gb.fill = GridBagConstraints.HORIZONTAL;
		gb.anchor = GridBagConstraints.CENTER;
		gb.gridwidth = 2;
		gb.gridx = 0;
		gb.gridy++;
		mainPanel.add(buttonPanel, gb);

		setContentPane(mainPanel);
	}

	/**
	 * @param number
	 */
	protected void goToImage(Integer number) {
		if (number.intValue() != this.currentIndex) {
			this.currentIndex = number;
			loadImage();
		}
	}

	private void previousImage() {
		if (this.cgmFiles == null)
			return;

		if (this.currentIndex > 0) {
			this.currentIndex--;
		}
		else {
			this.currentIndex = this.cgmFiles.length - 1;
		}

		loadImage();
	}

	private void nextImage() {
		if (this.cgmFiles == null)
			return;

		if (this.currentIndex < this.cgmFiles.length - 1) {
			this.currentIndex++;
		}
		else {
			this.currentIndex = 0;
		}

		loadImage();
	}

	protected void dumpCommands() {
		if (this.cgmFiles == null)
			return;

		File out = new File(this.dumpCommandFileField.getText());
		PrintStream fileWriter = null;
		try {
			// int cnt = 0;
			fileWriter = new PrintStream(out);
			for (File cgmFile : this.cgmFiles) {
				CGM cgm = new CGM(cgmFile);
				cgm.showCGMCommands(fileWriter);

				// setProgress(cgmFile.getName(), cnt++);
			}
		}
		catch (IOException e) {
			System.err.println(e);
		}
		finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
	}

	public static void main(String[] args) {
		Viewer readAndDisplay = new Viewer();
		readAndDisplay.pack();
		readAndDisplay.setVisible(true);
		readAndDisplay.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
