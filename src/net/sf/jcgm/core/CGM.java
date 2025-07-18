/*
 * <copyright> Copyright 1997-2003 BBNT Solutions, LLC under sponsorship of the
 * Defense Advanced Research Projects Agency (DARPA).
 * Copyright 2009 Swiss AviationSoftware Ltd.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the Cougaar Open Source License as published by DARPA on
 * the Cougaar Open Source Website (www.cougaar.org).
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.sf.jcgm.core;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

import net.sf.jcgm.core.ScalingMode.Mode;

/**
 * The main class for Computer Graphics Metafile support.
 * @author xphc (Philippe Cadé)
 * @author BBNT Solutions
 * @version $Id$
 */
public class CGM implements Cloneable {
	private List<Command> commands;
	
	/**
	 * Acc. to ISO/IEC 8632-1:19999(E) §6.1:<br> 
	 * "The [...] END METAFILE elements [...] occur exactly once in a complete metafile [...]".
	 */
	private boolean endMetafileReached = false;
	
	private final ArrayList<Message> messages = new ArrayList<>();
	private int colourIndexPrecision = 8;
	private int colourPrecision = 8;
	private ColourModel.Model colourModel = ColourModel.Model.RGB;
	private ColourSelectionMode.Type colourSelectionMode = ColourSelectionMode.Type.INDEXED;
	private int[] minimumColorValueRGB = new int[] { 0, 0, 0 };
	private int[] maximumColorValueRGB = new int[] { 255, 255, 255 };
	private SpecificationMode edgeWidthSpecificationMode = SpecificationMode.ABSOLUTE;
	private SpecificationMode lineWidthSpecificationMode = SpecificationMode.ABSOLUTE;
	private SpecificationMode markerSizeSpecificationMode = SpecificationMode.ABSOLUTE;
	private int indexPrecision = 16;
	private int integerPrecision = 16;
	private int namePrecision = 16;
	private int vdcIntegerPrecision = 16;
	private VDCRealPrecision.Type vdcRealPrecision = VDCRealPrecision.Type.FIXED_POINT_32BIT;
	private VDCType.Type vdcType = VDCType.Type.INTEGER;
	private RealPrecision.Precision realPrecision = RealPrecision.Precision.FIXED_32;
	/** Flag to tell us whether a real precision command has already been processed or not. */
	private boolean realPrecisionProcessed = false;
	private RestrictedTextType.Type restrictedTextType = RestrictedTextType.Type.BASIC;
	private DeviceViewportSpecificationMode.Mode deviceViewportSpecificationMode = DeviceViewportSpecificationMode.Mode.FractionOfDrawingSurface;
	
	private final List<ICommandListener> commandListeners = new ArrayList<ICommandListener>();

	private final static int INITIAL_NUM_COMMANDS = 500;

	private boolean shouldFlattenCurve = false;

	public CGM() {
		loadSystemProperties();
	}

	public CGM(File cgmFile) throws IOException {
		if (cgmFile == null)
			throw new NullPointerException("unexpected null parameter");

		InputStream inputStream;
		String cgmFilename = cgmFile.getName();
		if (cgmFilename.endsWith(".cgm.gz") || cgmFilename.endsWith(".cgmz")) {
			inputStream = new GZIPInputStream(new FileInputStream(cgmFile));
		}
		else {
			inputStream = new FileInputStream(cgmFile);
		}
		DataInputStream in = new DataInputStream(new BufferedInputStream(inputStream));

		loadSystemProperties();
		
		read(in);
		in.close();
	}

	private void loadSystemProperties() {
		this.shouldFlattenCurve =  Boolean.getBoolean("jcgm.core.flattencurve");
	}
	
	public void read(DataInput in) throws IOException {
		reset();
		this.commands = new ArrayList<Command>(INITIAL_NUM_COMMANDS);
		while (!this.endMetafileReached) {
			Command c = Command.read(in, this);
			if (c == null)
				break;

			for (ICommandListener listener : this.commandListeners) {
				listener.commandProcessed(c);
			}

			// get rid of all arguments after we read them
			c.cleanUpArguments();
			this.commands.add(c);
		}
		
		// ignore trailing data for now: it only happened once with an A320 ASM sample;
		// TODO: integrate proper logger and log whenever there is unexpected trailing data
		// StringBuilder trailingData = new StringBuilder();
		// try {
		// 	String line = in.readLine();
		// 	while (line != null) {
		// 		trailingData.append(line);
		// 		line = in.readLine();
		// 	}
		// } catch (Exception e) {
		// 	 throw new CgmException("Failed to read the trailing data of the CGM.");
		// } 
		// // most of (all?) CGM samples can have a trailing character that can be ignored
		// if (trailingData.length() > 1) {
		// 	 throw new CgmException("The CGM contains additional data that was not processed: [" + trailingData + "].");
		// }
	}

	/**
	 * Splits a CGM file containing several CGM files into pieces. Each single
	 * CGM file will be extracted to an own file. The name of that file is
	 * provided by the given {@code extractor}.
	 * 
	 * @param cgmFile
	 *            The CGM file to split
	 * @param outputDir
	 *            The output directory to use. Must exist and be writable.
	 * @param extractor
	 *            The extractor in charge of naming the split CGM files
	 * @throws IOException
	 *             If the given CGM file could not be read or there was an error
	 *             splitting the file
	 */
	public static void split(File cgmFile, File outputDir,
			IBeginMetafileNameExtractor extractor) throws IOException {
		if (cgmFile == null || outputDir == null || extractor == null)
			throw new NullPointerException("unexpected null argument");

		if (!outputDir.isDirectory())
			throw new IllegalArgumentException("outputDir must be a directory");

		if (!outputDir.canWrite())
			throw new IllegalArgumentException("outputDir must be writable");

		RandomAccessFile randomAccessFile = null;
		try {
			randomAccessFile = new RandomAccessFile(cgmFile, "r");
			FileChannel channel = randomAccessFile.getChannel();

			Command c;
			long startPosition = 0;
			long currentPosition = 0;
			String currentFileName = null;

			CGM dummyCgm = new CGM();
			while ((c = Command.read(randomAccessFile, dummyCgm)) != null) {
				if (c instanceof BeginMetafile) {
					// the CGM files will be cut at the begin meta file command
					if (currentFileName != null) {
						dumpToFile(outputDir, extractor, channel, startPosition,
								currentPosition, currentFileName);
					}
					startPosition = currentPosition;
					BeginMetafile beginMetafile = (BeginMetafile) c;
					currentFileName = beginMetafile.getFileName();
				}
				currentPosition = randomAccessFile.getFilePointer();
			}

			if (currentFileName != null) {
				dumpToFile(outputDir, extractor, channel, startPosition,
						currentPosition, currentFileName);
			}
		}
		finally {
			if (randomAccessFile != null) {
				randomAccessFile.close();
			}
		}
	}

	private static void dumpToFile(File outputDir,
			IBeginMetafileNameExtractor extractor, FileChannel channel,
			long startPosition, long currentPosition, String currentFileName)
					throws IOException {
		// dump the CGM file
		MappedByteBuffer byteBuffer = channel.map(
				FileChannel.MapMode.READ_ONLY, startPosition,
				currentPosition - startPosition);
		writeFile(byteBuffer, outputDir, extractor
				.extractFileName(currentFileName));
	}

	/**
	 * Splits a CGM file containing several CGM files into pieces. The given
	 * extractor is in charge of dealing with the extracted CGM file.
	 * 
	 * @param cgmFile
	 *            The CGM file to split
	 * @param extractor
	 *            An extractor that knows what to do with the extracted CGM file
	 * @throws IOException
	 *             If an error happened reading the CGM file
	 * @throws CgmException
	 *             If an error happened during the handling of the extracted CGM
	 *             file
	 */
	public static void split(File cgmFile, ICgmExtractor extractor)
			throws IOException, CgmException {
		if (cgmFile == null || extractor == null)
			throw new NullPointerException("unexpected null argument");

		RandomAccessFile randomAccessFile = null;
		try {
			randomAccessFile = new RandomAccessFile(cgmFile, "r");
			FileChannel channel = randomAccessFile.getChannel();

			Command c;
			long startPosition = 0;
			long currentPosition = 0;
			String currentFileName = null;
			
			CGM dummyCgm = new CGM();
			while ((c = Command.read(randomAccessFile, dummyCgm)) != null) {
				if (c instanceof BeginMetafile) {
					// the CGM files will be cut at the begin meta file command
					if (currentFileName != null) {
						dumpToStream(extractor, channel, startPosition,
								currentPosition, currentFileName);
					}
					startPosition = currentPosition;
					BeginMetafile beginMetafile = (BeginMetafile) c;
					currentFileName = beginMetafile.getFileName();
				}
				currentPosition = randomAccessFile.getFilePointer();
			}

			// don't forget to also dump the last file
			if (currentFileName != null) {
				dumpToStream(extractor, channel, startPosition,
						currentPosition, currentFileName);
			}
		}
		finally {
			if (randomAccessFile != null) {
				randomAccessFile.close();
			}
		}
	}

	private static void dumpToStream(ICgmExtractor extractor,
			FileChannel channel, long startPosition, long currentPosition,
			String currentFileName) throws IOException, CgmException {
		// dump the CGM file
		MappedByteBuffer byteBuffer = channel.map(
				FileChannel.MapMode.READ_ONLY, startPosition, currentPosition
				- startPosition);

		byte[] byteArray = new byte[(int) (currentPosition - startPosition)];
		byteBuffer.get(byteArray);
		extractor.handleExtracted(extractor.extractFileName(currentFileName),
				new ByteArrayInputStream(byteArray), byteArray.length);
	}

	/**
	 * Writes the given bytes to a file
	 * 
	 * @param byteBuffer
	 *            The bytes to write to the file
	 * @param outputDir
	 *            The output directory to use, assumed to be existing and
	 *            writable
	 * @param fileName
	 *            The file name to use
	 * @throws IOException
	 *             On I/O error
	 */
	private static void writeFile(ByteBuffer byteBuffer, File outputDir,
			String fileName) throws IOException {
		File outputFile = new File(outputDir, fileName);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outputFile);
			FileChannel channel = out.getChannel();
			channel.write(byteBuffer);
			out.close();
		}
		finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Adds the given listener to the list of command listeners
	 * @param listener The listener to add
	 */
	public void addCommandListener(ICommandListener listener) {
		this.commandListeners.add(listener);
	}

	/**
	 * All the command classes with static data need to be reset here
	 */
	private void reset() {
		resetColourIndexPrecision();
		resetColourModel();
		resetColourPrecision();
		resetColourSelectionMode();
		resetColourValueExtents();
		resetEdgeWidthSpecificationMode();
		resetIndexPrecision();
		resetIntegerPrecision();
		resetLineWidthSpecificationMode();
		resetMarkerSizeSpecificationMode();
		resetRealPrecision();
		resetRestrictedTextType();
		resetVdcIntegerPrecision();
		resetVdcRealPrecision();
		resetVdcType();
		resetNamePrecision();
		resetDeviceViewportSpecificationMode();
	
		resetMessages();
	}

	// region MESSAGES
	public List<Message> getMessages() {
		return Collections.unmodifiableList(this.messages);
	}
	
	void addMessage(Message m) {
		this.messages.add(m);
	}
	
	public void resetMessages() {
		this.messages.clear();
	}
	// endregion

	// region COLOUR INDEX PRECISION
	int getColourIndexPrecision() {
		return this.colourIndexPrecision;
	}
	
	void setColourIndexPrecision(int colourIndexPrecision) {
		this.colourIndexPrecision = colourIndexPrecision;
	}
	
	private void resetColourIndexPrecision() {
		setColourIndexPrecision(8);
	}
	// endregion
	
	// region COLOUR MODEL
	ColourModel.Model getColourModel() {
		return this.colourModel;
	}
	
	void setColourModel(ColourModel.Model colourModel) {
		this.colourModel = colourModel;
	}
	
	private void resetColourModel() {
		this.colourModel = ColourModel.Model.RGB;
	}
	// endregion
	
	// region COLOUR PRECISION
	int getColourPrecision() {
		return this.colourPrecision;
	}
	
	void setColourPrecision(int colourPrecision) {
		this.colourPrecision = colourPrecision;
	}
	
	private void resetColourPrecision() {
		setColourPrecision(8);
	}
	// endregion
	
	// region COLOUR SELECTION MODE
	ColourSelectionMode.Type getColourSelectionMode() {
		return this.colourSelectionMode;
	}
	
	void setColourSelectionMode(ColourSelectionMode.Type colourSelectionMode) {
		this.colourSelectionMode = colourSelectionMode;
	}
	
	private void resetColourSelectionMode() {
		setColourSelectionMode(ColourSelectionMode.Type.INDEXED);
	}
	// endregion
	
	// region COLOUR VALUE EXTENT
	int[] getMinimumColorValueRGB() {
		return this.minimumColorValueRGB;
	}
	
	void setMinimumColorValueRGB(int[] minimumColorValueRGB) {
		this.minimumColorValueRGB = minimumColorValueRGB;
	}
	
	int[] getMaximumColorValueRGB() {
		return this.maximumColorValueRGB;
	}
	
	void setMaximumColorValueRGB(int[] maximumColorValueRGB) {
		this.maximumColorValueRGB = maximumColorValueRGB;
	}
	
	private void resetColourValueExtents() {
		setMinimumColorValueRGB(new int[]{0, 0, 0});
		setMaximumColorValueRGB(new int[]{255, 255, 255});
	}
	// endregion
	
	// region EDGE WIDTH SPECIFICATION MODE
	SpecificationMode getEdgeWidthSpecificationMode() {
		return this.edgeWidthSpecificationMode;
	}
	
	void setEdgeWidthSpecificationMode(SpecificationMode edgeWidthSpecificationMode) {
		this.edgeWidthSpecificationMode = edgeWidthSpecificationMode;
	}
	
	private void resetEdgeWidthSpecificationMode() {
		setEdgeWidthSpecificationMode(SpecificationMode.ABSOLUTE);
	}
	// endregion
	
	// region LINE WIDTH SPECIFICATION MODE
	SpecificationMode getLineWidthSpecificationMode() {
		return this.lineWidthSpecificationMode;
	}
	
	void setLineWidthSpecificationMode(SpecificationMode lineWidthSpecificationMode) {
		this.lineWidthSpecificationMode = lineWidthSpecificationMode;
	}
	
	private void resetLineWidthSpecificationMode() {
		setLineWidthSpecificationMode(SpecificationMode.ABSOLUTE);
	}
	// endregion
	
	// region MARKER SIZE SPECIFICATION MODE
	SpecificationMode getMarkerSizeSpecificationMode() {
		return this.markerSizeSpecificationMode;
	}
	
	void setMarkerSizeSpecificationMode(SpecificationMode markerSizeSpecificationMode) {
		this.markerSizeSpecificationMode = markerSizeSpecificationMode;
	}
	
	private void resetMarkerSizeSpecificationMode() {
		setMarkerSizeSpecificationMode(SpecificationMode.ABSOLUTE);
	}
	// endregion
	
	// region INDEX PRECISION
	int getIndexPrecision() {
		return this.indexPrecision;
	}
	
	void setIndexPrecision(int indexPrecision) {
		this.indexPrecision = indexPrecision;
	}
	
	private void resetIndexPrecision() {
		setIndexPrecision(16);
	}
	// endregion
	
	// region INTEGER PRECISION
	int getIntegerPrecision() {
		return this.integerPrecision;
	}
	
	void setIntegerPrecision(int integerPrecision) {
		this.integerPrecision = integerPrecision;
	}
	
	private void resetIntegerPrecision() {
		setIntegerPrecision(16);
	}
	// endregion
	
	// region NAME PRECISION
	int getNamePrecision() {
		return this.namePrecision;
	}
	
	void setNamePrecision(int namePrecision) {
		this.namePrecision = namePrecision;
	}
	
	private void resetNamePrecision() {
		setNamePrecision(16);
	}
	// endregion
	
	// region INTEGER PRECISION
	int getVdcIntegerPrecision() {
		return this.vdcIntegerPrecision;
	}
	
	void setVdcIntegerPrecision(int vdcIntegerPrecision) {
		this.vdcIntegerPrecision = vdcIntegerPrecision;
	}
	
	private void resetVdcIntegerPrecision() {
		setVdcIntegerPrecision(16);
	}
	// endregion
	
	// region VDC REAL PRECISION
	public VDCRealPrecision.Type getVdcRealPrecision() {
		return this.vdcRealPrecision;
	}
	
	public void setVdcRealPrecision(VDCRealPrecision.Type vdcRealPrecision) {
		this.vdcRealPrecision = vdcRealPrecision;
	}
	
	private void resetVdcRealPrecision() {
		setVdcRealPrecision(VDCRealPrecision.Type.FIXED_POINT_32BIT);
	}
	// endregion
	
	// region VDC TYPE
	VDCType.Type getVdcType() {
		return this.vdcType;
	}
	
	void setVdcType(VDCType.Type vdcType) {
		this.vdcType = vdcType;
	}
	
	private void resetVdcType() {
		setVdcType(VDCType.Type.INTEGER);
	}
	// endregion
	
	// region REAL PRECISION
	RealPrecision.Precision getRealPrecision() {
		return this.realPrecision;
	}
	
	void setRealPrecision(RealPrecision.Precision realPrecision) {
		this.realPrecision = realPrecision;
	}
	
	boolean hasRealPrecisionBeenProcessed() {
		return this.realPrecisionProcessed;
	}
	
	void setRealPrecisionProcessed(boolean realPrecisionProcessed) {
		this.realPrecisionProcessed = realPrecisionProcessed;
	}
	
	private void resetRealPrecision() {
		setRealPrecision(RealPrecision.Precision.FIXED_32);
		setRealPrecisionProcessed(false);
	}
	// endregion
	
	// region RESTRICTED TEXT TYPE
	RestrictedTextType.Type getRestrictedTextType() {
		return this.restrictedTextType;
	}
	
	void setRestrictedTextType(RestrictedTextType.Type restrictedTextType) {
		this.restrictedTextType = restrictedTextType;
	}
	
	private void resetRestrictedTextType() {
		setRestrictedTextType(RestrictedTextType.Type.BASIC);	
	}
	// endregion
	
	// region DEVICE VIEWPORT SPECIFICATION MODE
	DeviceViewportSpecificationMode.Mode getDeviceViewportSpecificationMode() {
		return this.deviceViewportSpecificationMode;
	}
	
	void setDeviceViewportSpecificationMode(DeviceViewportSpecificationMode.Mode deviceViewportSpecificationMode) {
		this.deviceViewportSpecificationMode = deviceViewportSpecificationMode;
	}
	
	private void resetDeviceViewportSpecificationMode() {
		setDeviceViewportSpecificationMode(DeviceViewportSpecificationMode.Mode.FractionOfDrawingSurface);
	}
	// endregion
	
	public void paint(CGMDisplay d) {
		for (Command c : this.commands) {
			if (filter(c)) {
				c.paint(d);
			}
		}
	}

	private boolean filter(Command c) {
		return true;
		//		List<Class<?>> classes = new ArrayList<Class<?>>();
		//		//classes.add(PolygonElement.class);
		//		classes.add(Text.class);
		//		//classes.add(CircleElement.class);
		//		
		//		for (Class<?> clazz: classes) {
		//			if (clazz.isInstance(c))
		//				return false;
		//		}
		//		
		//		return true;
	}

	/**
	 * Returns the size of the CGM graphic.
	 * @return The dimension or null if no {@link VDCExtent} command was found.
	 */
	public Dimension getSize() {
		// default to 96 DPI which is the Microsoft Windows default DPI setting
		return getSize(96);
	}

	/**
	 * Returns the size of the CGM graphic taking into account a specific DPI setting
	 * @param dpi The DPI value to use
	 * @return The dimension or null if no {@link VDCExtent} command was found.
	 */
	public Dimension getSize(double dpi) {
		Point2D.Double[] extent = extent();
		if (extent == null)
			return null;

		double factor = 1;

		ScalingMode scalingMode = getScalingMode();
		if (scalingMode != null) {
			Mode mode = scalingMode.getMode();
			if (ScalingMode.Mode.METRIC.equals(mode)) {
				double metricScalingFactor = scalingMode.getMetricScalingFactor();
				if (metricScalingFactor != 0) {
					// 1 inch = 25,4 millimeter
					factor = (dpi * metricScalingFactor) / 25.4;
				}
			}
		}

		int width = (int)Math.ceil((Math.abs(extent[1].x - extent[0].x) * factor));
		int height = (int)Math.ceil((Math.abs(extent[1].y - extent[0].y) * factor));

		return new Dimension(width, height);
	}

	public Point2D.Double[] extent() {
		for (Command c : this.commands) {
			if (c instanceof VDCExtent) {
				Point2D.Double[] extent = ((VDCExtent) c).extent();
				return extent;
			}
		}
		return null;
	}

	private ScalingMode getScalingMode() {
		for (Command c : this.commands) {
			if (c instanceof ScalingMode) {
				return (ScalingMode)c;
			}
		}
		return null;
	}

	public void showCGMCommands() {
		showCGMCommands(System.out);
	}

	public void showCGMCommands(PrintStream stream) {
		for (Command c : this.commands) {
			stream.println("Command: " + c);
		}
	}

	public List<Command> getCommands() {
		return Collections.unmodifiableList(this.commands);
	}
	
	public void setEndMetafileReached() {
		this.endMetafileReached = true;
	}

	public boolean shouldFlattenCurve() {
		return this.shouldFlattenCurve;
	}
}

/*
 * vim:encoding=utf8
 */
