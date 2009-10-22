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

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.*;


/**
 * Class=4, Element=4
 * @author xphc (Philippe Cadé)
 * @author BBNT Solutions
 * @version $Id$
 */
class Text extends Command {
    private Point2D.Double position;
    private String string;
	private Shape shape;

    public Text(int ec, int eid, int l, DataInput in)
            throws IOException {
        super(ec, eid, l, in);
        this.position = makePoint();
        
        int finalNotFinal = makeEnum();
        
        this.string = makeString();
    }

    private void initializeText(CGMDisplay d) {
    	if (this.shape != null) {
    		// already initialized
    		return;
    	}
    	
    	String decodedString = d.useSymbolEncoding() ?
        		SymbolDecoder.decode(this.string) : this.string;
        	
    	Graphics2D g2d = d.getGraphics2D();
    	FontRenderContext fontRenderContext = g2d.getFontRenderContext();
    	
    	Font font = g2d.getFont();
    	FontMetrics fontMetrics = g2d.getFontMetrics();
    	
		GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, decodedString);
    	
    	float[] glyphPositions = glyphVector.getGlyphPositions(0, glyphVector.getNumGlyphs(), null);
    	
		AffineTransform textTransform = d.getTextTransform();
		int screenResolution;
		if (GraphicsEnvironment.isHeadless()) {
			// if we're in a headless environment, assume 96 dots per inch
			// (default setting for Windows XP)
			screenResolution = 96;
		}
		else {
			screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
		}
		double scale = d.getCharacterHeight() / (fontMetrics.getAscent() * 72 / screenResolution);
		
		textTransform.scale(scale, scale);
		
		// update the glyph positions to account for scaling and additional
		// character spacing
		double characterSpacing = d.getCharacterSpacing();
		for (int i = 0; i < glyphPositions.length; i+=2) {
			glyphPositions[i] *= scale;
		}

		for (int i = 0; i < glyphPositions.length; i+=2) {
			if (i > 0) {
				double originalSpace = glyphPositions[i] - glyphPositions[i - 2];
				double additionalSpace = characterSpacing * originalSpace;
				
				for (int j = i; j < glyphPositions.length; j+=2) {
					glyphPositions[j] += additionalSpace;
				}
			}
		}
		
    	for (int i = 0; i < glyphVector.getNumGlyphs(); i++) {
    		glyphVector.setGlyphPosition(i, new Point2D.Double(glyphPositions[2*i], glyphPositions[2*i+1]));
			glyphVector.setGlyphTransform(i, textTransform);
    	}
    	
    	this.shape = glyphVector.getOutline((int)this.position.x, (int)this.position.y);
    }
    
    @Override
	public void paint(CGMDisplay d) {
    	initializeText(d);
    	
        Graphics2D g2d = d.getGraphics2D();
        
        AffineTransform savedTransform = g2d.getTransform();
        
        AffineTransform coordinateSystemTransformation = d.getCoordinateSystemTransformation(
        	this.position,
        	d.getCharacterOrientationBaselineVector(), d.getCharacterOrientationUpVector());
        
        AffineTransform textTransform = d.getTextTransform();
        coordinateSystemTransformation.concatenate(textTransform);

        g2d.transform(coordinateSystemTransformation);
        
        g2d.setColor(d.getTextColor());
        g2d.drawString(this.string, 0, 0);
        
        g2d.setTransform(savedTransform);
    }

    @Override
	public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("Text position=");
    	sb.append(this.position);
    	sb.append(" string=").append(this.string);
        return sb.toString();
    }
}

/*
 * vim:encoding=utf8
 */
