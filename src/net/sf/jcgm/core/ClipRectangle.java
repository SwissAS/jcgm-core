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
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.DataInput;
import java.io.IOException;


/**
 * Class=3, Element=5
 * @author xphc
 * @version $Id$
 * @since Jun 12, 2009
 */
public class ClipRectangle extends Command {

	private Shape shape;
	private Point2D.Double p1;
	private Point2D.Double p2;

	public ClipRectangle(int ec, int eid, int l, DataInput in) throws IOException {
		super(ec, eid, l, in);
		
		this.p1 = makePoint();
		this.p2 = makePoint();
		
		this.shape = new Rectangle2D.Double(this.p1.x, this.p1.y, this.p2.x - this.p1.x, this.p2.y - this.p1.y);
		
        // make sure all the arguments were read
        assert (this.currentArg == this.args.length);
	}

	@Override
	public void paint(CGMDisplay d) {
		if (d.getClipFlag()) {
			// only clip if clip flag is set
			Graphics2D g2d = d.getGraphics2D();
			g2d.setClip(this.shape);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ClipRectangle");
		sb.append(" p1=").append(this.p1).append(",");
		sb.append(" p2=").append(this.p2);
		return sb.toString();
	}
}

/*
 * vim:encoding=utf8
 */
