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

import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.io.*;


/**
 * Class=4, Element=14
 * @author xphc (Philippe Cadé)
 * @author BBNT Solutions
 * @version $Id$
 */
public class CircularArc3PointClose extends CircularArc3Point {
	private int type;
	
    public CircularArc3PointClose(int ec, int eid, int l, DataInput in, CGM cgm)
            throws IOException {
        super(ec, eid, l, in, cgm);
        
        int type = makeEnum();
        if (type == 0) {
        	this.type = Arc2D.PIE;
        }
        else if (type == 1) {
        	this.type = Arc2D.CHORD;
        }
        else {
        	unsupported("unsupported closure type "+this.type, cgm);
        	this.type = Arc2D.CHORD;
        }
    }

	@Override
	protected int getClosureType() {
		return this.type;
	}

	@Override
	public void paint(CGMDisplay d) {
		if (this.shape == null)
			initShape(d);
		
    	Graphics2D g2d = d.getGraphics2D();
    	
    	d.fill(this.shape);
    	
    	if (d.drawEdge()) {
    		g2d.setColor(d.getEdgeColor());
    		g2d.setStroke(d.getEdgeStroke());
    		g2d.draw(this.shape);
    	}
	}

    @Override
	public String toString() {
        return "CircularArc3PointClose";
    }
}

/*
 * vim:encoding=utf8
 */
