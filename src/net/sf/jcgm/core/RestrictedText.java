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

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.io.DataInput;
import java.io.IOException;

import net.sf.jcgm.core.TextAlignment.HorizontalAlignment;
import net.sf.jcgm.core.TextAlignment.VerticalAlignment;


/**
 * Class=4, Element=5
 * @author xphc (Philippe Cadé)
 * @author BBNT Solutions
 * @version $Id$
 */
public class RestrictedText extends TextCommand {
	private final double deltaWidth;
	private final double deltaHeight;
	private final RestrictedTextType.Type type;

	public RestrictedText(int ec, int eid, int l, DataInput in, CGM cgm)
			throws IOException {
		super(ec, eid, l, in, cgm);

		// (2VDC)
		this.deltaWidth = makeVdc();
		this.deltaHeight = makeVdc();
		
		// point (P)
		this.position = makePoint();
		// flag (E)
		this.finalFlag = makeEnum() >= 1;
		setStringComplete(this.finalFlag);
		// string (S)
		this.string = makeString();
		
		this.type = cgm.getRestrictedTextType();

		// make sure all the arguments were read
		assert (this.currentArg == this.args.length);
	}

	@Override
	public String toString() {
		return "RestrictedText \"" + this.string + "\" deltaWidth=" + this.deltaWidth +
				" deltaHeight=" + this.deltaHeight + " textPosition.x=" + this.position.x + 
				" textPosition.y=" + this.position.y;

	}

	@Override
	protected Point2D.Double getTextOffset(CGMDisplay d) {
		// the location of the bounding box depends on the alignment and the text path
		TextPath.Type textPath = d.getTextPath();

		if (TextPath.Type.UP.equals(textPath)) {
			double xPos = 0;
			HorizontalAlignment horizontalAlignment = d.getHorizontalTextAlignment();
			if (HorizontalAlignment.NORMAL_HORIZONTAL.equals(horizontalAlignment)) {
				xPos = -this.deltaWidth / 2;
			}
			else if (HorizontalAlignment.LEFT.equals(horizontalAlignment)) {
				xPos = 0;
			}
			else if (HorizontalAlignment.CENTRE.equals(horizontalAlignment)) {
				xPos = -this.deltaWidth/2;
			}
			else if (HorizontalAlignment.RIGHT.equals(horizontalAlignment)) {
				xPos = -this.deltaWidth;
			}
			else if (HorizontalAlignment.CONTINOUS_HORIZONTAL.equals(horizontalAlignment)) {
				xPos = -this.deltaWidth * d.getContinuousHorizontalAlignment();
			}

			double yPos = 0;
			VerticalAlignment verticalAlignment = d.getVerticalTextAlignment();
			if (VerticalAlignment.NORMAL_VERTICAL.equals(verticalAlignment) ||
					VerticalAlignment.BASE.equals(verticalAlignment)) {
				yPos = 0;
			}
			else if (VerticalAlignment.TOP.equals(verticalAlignment)) {
				yPos = this.deltaHeight;
			}
			else if (VerticalAlignment.CAP.equals(verticalAlignment)) {
				yPos = this.deltaHeight;
			}
			else if (VerticalAlignment.HALF.equals(verticalAlignment)) {
				yPos = this.deltaHeight/2;
			}
			else if (VerticalAlignment.CONTINOUS_VERTICAL.equals(verticalAlignment)) {
				yPos = this.deltaHeight * d.getContinuousVerticalAlignment();
			}
			return new Point2D.Double(xPos, yPos);
		}

		if (TextPath.Type.DOWN.equals(textPath)) {
			double xPos = 0;
			HorizontalAlignment horizontalAlignment = d.getHorizontalTextAlignment();
			if (HorizontalAlignment.NORMAL_HORIZONTAL.equals(horizontalAlignment)) {
				xPos = -this.deltaWidth / 2;
			}
			else if (HorizontalAlignment.LEFT.equals(horizontalAlignment)) {
				xPos = 0;
			}
			else if (HorizontalAlignment.CENTRE.equals(horizontalAlignment)) {
				xPos = -this.deltaWidth/2;
			}
			else if (HorizontalAlignment.RIGHT.equals(horizontalAlignment)) {
				xPos = -this.deltaWidth;
			}
			else if (HorizontalAlignment.CONTINOUS_HORIZONTAL.equals(horizontalAlignment)) {
				xPos = -this.deltaWidth * d.getContinuousHorizontalAlignment();
			}

			double yPos = 0;
			VerticalAlignment verticalAlignment = d.getVerticalTextAlignment();
			if (VerticalAlignment.NORMAL_VERTICAL.equals(verticalAlignment)) {
				yPos = this.deltaHeight;
			}
			else if (VerticalAlignment.BASE.equals(verticalAlignment)) {
				yPos = 0;
			}
			else if (VerticalAlignment.TOP.equals(verticalAlignment)) {
				yPos = this.deltaHeight;
			}
			else if (VerticalAlignment.CAP.equals(verticalAlignment)) {
				yPos = this.deltaHeight;
			}
			else if (VerticalAlignment.HALF.equals(verticalAlignment)) {
				yPos = this.deltaHeight/2;
			}
			else if (VerticalAlignment.CONTINOUS_VERTICAL.equals(verticalAlignment)) {
				yPos = this.deltaHeight * d.getContinuousVerticalAlignment();
			}
			return new Point2D.Double(xPos, yPos);
		}

		if (TextPath.Type.LEFT.equals(textPath)) {
			double xPos = 0;
			HorizontalAlignment horizontalAlignment = d.getHorizontalTextAlignment();
			if (HorizontalAlignment.NORMAL_HORIZONTAL.equals(horizontalAlignment)) {
				xPos = -this.deltaWidth;
			}
			else if (HorizontalAlignment.LEFT.equals(horizontalAlignment)) {
				xPos = 0;
			}
			else if (HorizontalAlignment.CENTRE.equals(horizontalAlignment)) {
				xPos = -this.deltaWidth/2;
			}
			else if (HorizontalAlignment.RIGHT.equals(horizontalAlignment)) {
				xPos = -this.deltaWidth;
			}
			else if (HorizontalAlignment.CONTINOUS_HORIZONTAL.equals(horizontalAlignment)) {
				xPos = -this.deltaWidth * d.getContinuousHorizontalAlignment();
			}

			double yPos = 0;
			VerticalAlignment verticalAlignment = d.getVerticalTextAlignment();
			if (VerticalAlignment.NORMAL_VERTICAL.equals(verticalAlignment) ||
					VerticalAlignment.BASE.equals(verticalAlignment)) {
				yPos = 0;
			}
			else if (VerticalAlignment.TOP.equals(verticalAlignment)) {
				yPos = this.deltaHeight;
			}
			else if (VerticalAlignment.CAP.equals(verticalAlignment)) {
				// TODO
			}
			else if (VerticalAlignment.HALF.equals(verticalAlignment)) {
				yPos = this.deltaHeight/2;
			}
			else if (VerticalAlignment.CONTINOUS_VERTICAL.equals(verticalAlignment)) {
				yPos = this.deltaHeight * d.getContinuousVerticalAlignment();
			}
			return new Point2D.Double(xPos, yPos);
		}

		double xPos = 0;
		HorizontalAlignment horizontalAlignment = d.getHorizontalTextAlignment();
		if (HorizontalAlignment.NORMAL_HORIZONTAL.equals(horizontalAlignment)) {
			xPos = 0;
		}
		else if (HorizontalAlignment.LEFT.equals(horizontalAlignment)) {
			xPos = 0;
		}
		else if (HorizontalAlignment.CENTRE.equals(horizontalAlignment)) {
			xPos = -this.deltaWidth/2;
		}
		else if (HorizontalAlignment.RIGHT.equals(horizontalAlignment)) {
			xPos = -this.deltaWidth;
		}
		else if (HorizontalAlignment.CONTINOUS_HORIZONTAL.equals(horizontalAlignment)) {
			xPos = -this.deltaWidth * d.getContinuousHorizontalAlignment();
		}

		double yPos = 0;
		VerticalAlignment verticalAlignment = d.getVerticalTextAlignment();
		if (VerticalAlignment.NORMAL_VERTICAL.equals(verticalAlignment) ||
				VerticalAlignment.BASE.equals(verticalAlignment)) {
			yPos = 0;
		}
		else if (VerticalAlignment.TOP.equals(verticalAlignment)) {
			yPos = this.deltaHeight;
		}
		else if (VerticalAlignment.CAP.equals(verticalAlignment)) {
			// TODO
		}
		else if (VerticalAlignment.HALF.equals(verticalAlignment)) {
			yPos = this.deltaHeight/2;
		}
		else if (VerticalAlignment.CONTINOUS_VERTICAL.equals(verticalAlignment)) {
			yPos = this.deltaHeight * d.getContinuousVerticalAlignment();
		}
		return new Point2D.Double(xPos, yPos);
	}

	@Override
	protected void scaleText(CGMDisplay d, FontMetrics fontMetrics, GlyphVector glyphVector, double width, double height) {
		Graphics2D g2d = d.getGraphics2D();

		double scaleX = 1;
		double scaleY = 1;

		if (TextPath.Type.DOWN.equals(d.getTextPath()) || TextPath.Type.UP.equals(d.getTextPath())) {
			Point2D glyphPosition = glyphVector.getGlyphPosition(1);
			scaleX = this.deltaWidth / glyphPosition.getX();
			scaleY = this.deltaHeight / 100;
		}
		else {
			if (this.type.equals(RestrictedTextType.Type.BASIC)) {
				// only use the character height and do an even scaling
				scaleX = width != 0 ? this.deltaWidth / width : 1;
				scaleY = height != 0 ? this.deltaHeight / height : 1;
			}
			else if (this.type.equals(RestrictedTextType.Type.BOXED_ALL)) {
				scaleX = width != 0 ? this.deltaWidth / width : 1;
				scaleY = height != 0 ? this.deltaHeight / height : 1;
			}
			else if (this.type.equals(RestrictedTextType.Type.BOXED_CAP)) {
				//height -= fontMetrics.getDescent();
				scaleX = width != 0 ? this.deltaWidth / width : 1;
				scaleY = height != 0 ? this.deltaHeight / height : 1;
			}
			else if (this.type.equals(RestrictedTextType.Type.ISOTROPIC_ALL)) {
				double min = Math.min(width != 0 ? this.deltaWidth / width : 1, height != 0 ? this.deltaHeight /
						height : 1);
				scaleX = scaleY = min;
			}
			else if (this.type.equals(RestrictedTextType.Type.ISOTROPIC_CAP)) {
				height -= fontMetrics.getDescent();
				double min = Math.min(width != 0 ? this.deltaWidth / width : 1, height != 0 ? this.deltaHeight /
						height : 1);
				scaleX = scaleY = min;
			}
			else if (this.type.equals(RestrictedTextType.Type.JUSTIFIED)) {
			}
		}

		g2d.scale(scaleX, scaleY);
	}

	@Override
	protected Font getAdjustedFont(Font font, CGMDisplay d) {
		// FIXME: remove those magic values
		// adjust the size of the font depending on the extent. If the extent is
		// very big, having small font sizes may create problems
		Point2D.Double[] extent = d.getExtent();
		if (Math.abs(extent[0].y - extent[1].y) > 1000) {
			return font.deriveFont((float) (Math.abs(extent[0].y - extent[1].y) / 100));
		}
		
		return font;
	}

}

/*
 * vim:encoding=utf8
 */
