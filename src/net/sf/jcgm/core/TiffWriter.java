package net.sf.jcgm.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class can write a basic TIFF file to be further processed by ImageIO.
 * @author XPHC
 */
class TiffWriter {

	private static final int HEADER_LENGTH = 8;
	private static final int IFD_COUNT_LENGTH = 2;
	private static final int IFD_ENTRY_LENGTH = 12;
	private static final int IFD_END_LENGTH = 4;

	private enum TiffTag {
		NEWSUBFILETYPE(0xFE),
		IMAGE_WIDTH(0x100),
		IMAGE_LENGTH(0x101),
		BITS_PER_SAMPLE(0x102),
		COMPRESSION(0x103),
		PHOTOMETRIC_INTERPRETATION(0x106),
		STRIP_OFFSETS(0x111),
		ROWS_PER_STRIP(0x116),
		STRIP_BYTE_COUNTS(0x117),
		X_RESOLUTION(0x11A),
		Y_RESOLUTION(0x11B),
		;

		private final int code;

		TiffTag(int code) {
			this.code = code;
		}
	}

	private enum DataType {
		/** 1 byte */
		BYTE(1), 
		ASCII(2), 
		/** SHORT, 2 bytes */
		WORD(3), 
		/** LONG, 4 bytes */
		DWORD(4), 
		RATIONAL(5);

		private final int code;

		DataType(int code) {
			this.code = code;
		}
	}

	/**
	 * An Image File Directory (IFD) entry
	 * @author XPHC
	 */
	private static class IfdEntry {
		private final TiffTag tag;
		private final DataType type;
		private final int count;
		private final int value;

		IfdEntry(TiffTag tag, DataType type, int count, int value) {
			this.tag = tag;
			this.type = type;
			this.count = count;
			this.value = value;
		}
	}

	private final ByteArrayOutputStream outputStream;
	private byte[] imageData;
	private int width;
	private int height;
	private CompressionType compressionType;

	public TiffWriter() {
		this.outputStream = new ByteArrayOutputStream();
	}

	/**
	 * Defines the bytes to use for the TIFF
	 * @param imageData
	 */
	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}

	public void setImageWidth(int width) {
		this.width = width;
	}

	public void setImageHeight(int height) {
		this.height = height;
	}

	public void setCompressionType(CompressionType compressionType) {
		this.compressionType = compressionType;
	}

	/**
	 * Writes the header: 8 bytes
	 */
	private void writeHeader() throws IOException {
		// CGM are big endian
		write('M');
		write('M');
		// 002a (TIFF)
		write(0);
		write(0x2a);
		// next 4 bytes are offset to image file directory (IFD)
		write4(HEADER_LENGTH);
	}

	/**
	 * Writes the image file directory (IFD).
	 */
	private void writeBeginIfd(int numIfdEntries) {
		write2(numIfdEntries);
	}

	/**
	 * Writes the end of the image file directory (IFD)
	 */
	private void writeEndIfd() throws IOException {
		write4(0); // zero, we do not have more than one IFD
	}

	/**
	 * Collects IDF entries
	 */
	private List<IfdEntry> getIfdEntries() {
		List<IfdEntry> entries = new ArrayList<>();
		// OK
		entries.add(new IfdEntry(TiffTag.NEWSUBFILETYPE, DataType.DWORD, 1, 0));
		entries.add(new IfdEntry(TiffTag.IMAGE_WIDTH, DataType.DWORD, 1, this.width));
		entries.add(new IfdEntry(TiffTag.IMAGE_LENGTH, DataType.DWORD, 1, this.height));
		entries.add(new IfdEntry(TiffTag.COMPRESSION, DataType.WORD, 1, getValueForCompressionType()));
		entries.add(new IfdEntry(TiffTag.PHOTOMETRIC_INTERPRETATION, DataType.WORD, 1, 0));
		// assume one strip
		entries.add(new IfdEntry(TiffTag.STRIP_BYTE_COUNTS, DataType.DWORD, 1, this.imageData.length));
		entries.add(new IfdEntry(TiffTag.ROWS_PER_STRIP, DataType.DWORD, 1, this.height));
		// do this one at last since it relies on the number of IFD entries
		entries.add(new IfdEntry(TiffTag.STRIP_OFFSETS, DataType.DWORD, 1, HEADER_LENGTH+IFD_COUNT_LENGTH+(entries.size()+1)*IFD_ENTRY_LENGTH+IFD_END_LENGTH));

		Collections.sort(entries, (a, b) -> a.tag.code - b.tag.code);
		return entries;
	}

	/**
	 * Write IFD entries
	 * @param ifdEntries 
	 */
	private void writeIfdEntries(List<IfdEntry> ifdEntries) throws IOException {
		for (IfdEntry ifdEntry : ifdEntries) {
			writeIfdEntry(ifdEntry.tag, ifdEntry.type, ifdEntry.count, ifdEntry.value);
		}
	}

	private int getValueForCompressionType() {
		if (this.compressionType == CompressionType.T6) {
			return 4;
		}
		// assume no compression
		return 1;
	}

	/**
	 * Writes an IFD entry (12 bytes)
	 */
	private void writeIfdEntry(TiffTag tag, DataType dataType, int count, int value) throws IOException {
		// tag (2 bytes)
		write2(tag.code);
		// field type (2 bytes)
		write2(dataType.code);
		// number of values
		write4(count);
		// value (4 bytes)
		switch (dataType) {
		case WORD:
			write2(value);
			write2(0); // padding
			break;
		case DWORD:
			write4(value);
			break;
		default:
			throw new IllegalArgumentException("unsupported data type "+dataType);
		}
	}

	byte[] writeImage() throws CgmException {
		try {
			writeHeader();
			List<IfdEntry> ifdEntries = getIfdEntries();
			writeBeginIfd(ifdEntries.size());
			writeIfdEntries(ifdEntries);
			writeEndIfd();
			writeImageBytes();
			return this.outputStream.toByteArray();
		} catch (IOException e) {
			throw new CgmException(e);
		}
	}

	private void writeImageBytes() throws IOException {
		this.outputStream.write(this.imageData);
	}

	/**
	 * Writes a single byte
	 */
	private void write(int b) {
		this.outputStream.write(b);
	}

	private byte[] intToBytes(Integer value) {
		// java integers are 32 bits according to the Java Language Reference
		byte[] ret = new byte[4];
		for (int i = 0; i < 4; i++) {
			ret[i] = (byte)(value & 0xFF);
			value >>>= 8;
		}
		return ret;
	}

	/**
	 * Writes the given integer with a padding to make it 4 bytes
	 */
	private void write4(int b) {
		byte[] bytes = intToBytes(b);
		for (int i = 3; i >=0; i--) {
			this.outputStream.write(bytes[i]);
		}
	}

	/**
	 * Writes the given integer with a padding to make it 2 bytes
	 */
	private void write2(int b) {
		if (b >= 1<<16) {
			throw new IllegalArgumentException("can't write "+b+" on two bytes");
		}
		byte[] bytes = intToBytes(b);
		write(bytes[1]);
		write(bytes[0]);
	}

	public static void main(String[] args) {
		try {
			boolean useCgmImage = true;
			byte[] imageData;
			TiffWriter writer = new TiffWriter();
			if (useCgmImage) {
				imageData = Files.readAllBytes(Paths.get("d:/Users/xphc/Desktop/out.tiff"));
				writer.setImageWidth(904);
				writer.setImageHeight(739);
				writer.setCompressionType(CompressionType.T6);
			}
			else {
				imageData = new byte[2];
				imageData[0] = (byte)0xAA;
				imageData[1] = (byte)0xA0;
				writer.setImageWidth(4);
				writer.setImageHeight(3);
				writer.setCompressionType(CompressionType.BITMAP);
			}
			writer.setImageData(imageData);
			byte[] image = writer.writeImage();
			int i = 0;
			for (byte b: image) {
				String s = String.format("%8s", Integer.toBinaryString(b & 0xFF));
				System.out.print(s.replace(' ', '0')+" ");
				if (i % 8 == 0) {
					System.out.println();
				}
			}
			System.out.println();
			for (byte b: image) {
				String h = String.format("%2s", Integer.toHexString(b & 0xFF));
				System.out.print(h.replace(' ', '0'));
				if (++i % 16 == 0) {
					System.out.println();
				}
				else if (i % 2 == 0) {
					System.out.print(' ');
				}
			}
			Files.write(Paths.get("d:/Users/xphc/Desktop/hope.tiff"), image);
		}
		catch (CgmException | IOException e) {
			e.printStackTrace();
		}
	}

}
