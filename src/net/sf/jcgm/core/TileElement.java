/*
 * Copyright (c) 2010, Swiss AviationSoftware Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Swiss AviationSoftware Ltd. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.sf.jcgm.core;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import net.sf.jcgm.core.Message.Severity;

/**
 * Common code for {@link Tile} and {@link BitonalTile}.
 * 
 * @version $Id: $
 * @author xphc
 * @since Oct 6, 2010
 */
abstract class TileElement extends Command {

	protected CompressionType compressionType;
	protected int rowPaddingIndicator;
	protected StructuredDataRecord sdr;
	protected BufferedImage bufferedImage = null;
	protected ByteBuffer bytes;

	protected TileElement(int ec, int eid, int l, DataInput in) throws IOException {
		super(ec, eid, l, in);
	}

	protected void readSdrAndBitStream() throws IOException {
		// the kind of information contained in the structured data record will
		// depend on the compression type
		this.sdr = makeSDR();

		if (this.compressionType.getFormatName() != null) {
			// we'll try to read the image with ImageIO
			Iterator<ImageReader> imageReaders = ImageIO
					.getImageReadersByFormatName(this.compressionType.getFormatName());
			if (imageReaders.hasNext()) {
				// the first one will do
				ImageReader reader = imageReaders.next();

				ByteBuffer buffer = readBytes();				


				if(this.compressionType == CompressionType.PNG) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] pngSignature = new byte[] { (byte)0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a };
					baos.write(pngSignature);
					//the PNG chunks are a in the SDR as bit stream we have to merge it
					List<Member> members = this.sdr.getMembers();
					if(members!=null) {
						for(Member member: members) {
							for(int i = 0; i < member.getCount();i++) {
								Object data = member.getData().get(i);
								if(data!=null && data instanceof byte[]) {
									baos.write((byte[])data);
								}
							}
						}
					}
					baos.write(buffer.array());
					buffer = ByteBuffer.allocate(baos.size());
					buffer.put(baos.toByteArray());

				}


				//debugByte(buffer.array(),"img","png");

				ByteArrayInputStream input = new ByteArrayInputStream(buffer.array());
				ImageInputStream imageInputStream = ImageIO.createImageInputStream(input);
				reader.setInput(imageInputStream);
				this.bufferedImage = reader.read(0);
			}
		} else {
			switch (this.compressionType) {
			case BITMAP:
				readBitmap();
				break;
			case T6:
				readT6();
				break;
			default:
				unsupported("unsupported compression type " + this.compressionType);
			}
		}
	}

	protected ByteBuffer readBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(this.args.length - this.currentArg);
		while (this.currentArg < this.args.length) {
			buffer.put(makeByte());
		}
		return buffer;
	}

	private void readT6() {
		this.bytes = readBytes();
	}

	abstract protected void readBitmap();

	@Override
	public void paint(CGMDisplay d) {
		Graphics2D g2d = d.getGraphics2D();
		TileArrayInfo tileArrayInfo = d.getTileArrayInfo();

		if (tileArrayInfo == null) {
			// malformed CGM file, a BEGIN TILE ARRAY should have been
			// encountered before
			return;
		}

		Point2D.Double position = tileArrayInfo.getCurrentTilePosition();
		// the tile bounds
		// g2d.setColor(Color.RED);
		// g2d.draw(new Rectangle2D.Double(position.x, position.y
		// - tileArrayInfo.getTileSizeInLineDirection(), tileArrayInfo
		// .getTileSizeInPathDirection(), tileArrayInfo
		// .getTileSizeInLineDirection()));

		if (this.bufferedImage == null) {
			readImageDelayed(tileArrayInfo);
		}
		if (this.bufferedImage != null) {
			int imageWidth = this.bufferedImage.getWidth();
			int imageHeight = this.bufferedImage.getHeight();

			// create a transformation to map the image to our tile bounds. Y
			// coordinate inverted because of inverted AWT Y axis
			AffineTransform xform = AffineTransform.getTranslateInstance(position.x, position.y);
			xform.scale(tileArrayInfo.getTileSizeInPathDirection() / imageWidth,
					-tileArrayInfo.getTileSizeInLineDirection() / imageHeight);
			g2d.drawImage(this.bufferedImage, xform, null);
		}

		// advance to the next tile
		tileArrayInfo.nextTile();
	}

	/**
	 * Wraps the previously read bytes into a TIFF file and uses ImageIO to read
	 * that file.
	 * 
	 * @param tileArrayInfo Provides image information such as size
	 */
	private void readImageDelayed(TileArrayInfo tileArrayInfo) {
		try {
			if (this.bytes == null || this.compressionType != CompressionType.T6) {
				return;
			}
			TiffWriter writer = new TiffWriter();
			writer.setImageWidth(tileArrayInfo.getNCellsPerTileInPathDirection());
			writer.setImageHeight(tileArrayInfo.getNCellsPerTileInLineDirection());
			writer.setCompressionType(CompressionType.T6);
			writer.setImageData(this.bytes.array());
			byte[] tiffBytes = writer.writeImage();

			Iterator<ImageReader> imageReaders = ImageIO.getImageReadersByFormatName("tiff");
			if (imageReaders.hasNext()) {
				// the first one will do
				ImageReader reader = imageReaders.next();

				try (ByteArrayInputStream input = new ByteArrayInputStream(tiffBytes)) {
					ImageInputStream imageInputStream = ImageIO.createImageInputStream(input);
					reader.setInput(imageInputStream);
					this.bufferedImage = reader.read(0);
				}
			}else {
				Messages.getInstance()
				.add(new Message(Severity.UNSUPPORTED, getElementClass(), getElementCode(), "No tiff support found add jai-core to classpath", toString()));
			}
		} catch (IOException | CgmException e) {
			Messages.getInstance()
			.add(new Message(Severity.FATAL, getElementClass(), getElementCode(), e.getMessage(), toString()));
		} finally {
			// we will only try once to read the image. Since this method is called over and
			// over again in the paint method, clear the bytes to not try again.
			this.bytes = null;
		}
	}

	@Override
	protected Object bitStream() {

		if(this.compressionType == CompressionType.PNG) {
			//translated in java from : https://github.com/BhaaLseN/CgmInfo			

			byte[] dataLength = new byte[4];
			for (int i = 0; i < dataLength.length; i++)
				dataLength[i] = makeByte();

			int length = 0;

			//length = java.nio.ByteBuffer.wrap(dataLength).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
			length = java.nio.ByteBuffer.wrap(dataLength).getInt();

			byte[] type = new byte[4];
			for (int i = 0; i < dataLength.length; i++)
				type[i] = makeByte();

			byte[] data = new byte[length];
			for (int i = 0; i < data.length; i++)
				data[i] = makeByte();

			byte[] crc = new byte[4];
			for (int i = 0; i < dataLength.length; i++)
				crc[i] = makeByte();

			// bitstream is a series of unsigned integer at fixed 16-bit precision [ISO/IEC 8632-3 7, Table 1, BS / Note 15]
			// 16 bits per entry is chosen for portability reasons and need not be filled completely; the remainder is set to 0.
			// we'll have to advance the stream to be aligned at a 16-bit boundary before we leave.
			if (length % 2 != 0)
			{
				byte shouldBeZero = makeByte();

			}

			byte[] result = new byte[dataLength.length + type.length + data.length + crc.length];

			System.arraycopy(dataLength, 0, result, 0, dataLength.length);
			System.arraycopy(type, 0, result, dataLength.length, type.length);
			System.arraycopy(data, 0, result, dataLength.length + type.length, data.length);
			System.arraycopy(crc, 0, result, dataLength.length + type.length + data.length, crc.length);

			return result;
		}else {
			return null;
		}
	}


	/*private void debugByte(byte[] data, String name,String type) {
		try (FileOutputStream fos = new FileOutputStream("c:/temp/name_"+UUID.randomUUID().toString()+"."+type)) {
			fos.write(data);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

}