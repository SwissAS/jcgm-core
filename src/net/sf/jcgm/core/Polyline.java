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
import java.awt.geom.Point2D;
import java.io.*;


/**
 * Class=4, Element=1
 * @author xphc (Philippe Cadé)
 * @author BBNT Solutions
 * @version $Id$
 */
class Polyline extends Command {
	private int[] x;
	private int[] y;
	private Point2D.Double[] points;

    public Polyline(int ec, int eid, int l, DataInput in)
            throws IOException {
        super(ec, eid, l, in);
        
        int n = this.args.length / sizeOfPoint();
        this.points = new Point2D.Double[n];
        for (int i = 0; i < n; i++) {
        	this.points[i] = makePoint();
        }
        
        // make sure all the arguments were read
        assert (this.currentArg == this.args.length);
    }

    @Override
	public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("Polyline [");
        for (int i = 0; i < this.points.length; i++) {
        	sb.append("(");
        	sb.append(this.points[i].x).append(",");
        	sb.append(this.points[i].y);
        	sb.append(")");
        }
        sb.append("]");
        return sb.toString();
    }

	private void initPoints() {
        this.x = new int[this.points.length];
        this.y = new int[this.points.length];
        for (int i = 0; i < this.points.length; i++) {
            this.x[i] = (int)(this.points[i].x);
            this.y[i] = (int)(this.points[i].y);
        }
    }

    @Override
	public void paint(CGMDisplay d) {
    	if (this.x == null || this.y == null) {
    		initPoints();
    	}
    	
        Graphics2D g2d = d.getGraphics2D();
		g2d.setColor(d.getLineColor());
		g2d.setStroke(d.getLineStroke());
        g2d.drawPolyline(this.x, this.y, this.x.length);
    }
}

/*
 * vim:encoding=utf8
 */
