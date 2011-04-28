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

import java.io.DataInput;
import java.io.IOException;


/**
 * Class=2, Element=17
 * @author xphc (Philippe Cadé)
 * @author BBNT Solutions
 * @version $Id$
 */
class LineAndEdgeTypeDefinition extends Command {
    private final int lineType;
	private final double dashCycleRepeatLength;
	private final int[] dashElements;

	public LineAndEdgeTypeDefinition(int ec, int eid, int l, DataInput in)
            throws IOException {
        super(ec, eid, l, in);
        
        this.lineType = makeIndex();
        assert(this.lineType <= 0);
        
        this.dashCycleRepeatLength = Math.abs(makeSizeSpecification(LineWidthSpecificationMode.getMode()));
        this.dashElements = new int[(this.args.length-this.currentArg)/sizeOfInt()];
        
        for (int i = 0; i < this.dashElements.length; i++) {
        	this.dashElements[i] = makeInt();
        }
        
        // make sure all the arguments were read
        assert (this.currentArg == this.args.length);
    }
	
	@Override
	public void paint(CGMDisplay d) {
		d.addLineType(this.lineType, this.dashElements, this.dashCycleRepeatLength);
	}

	@Override
	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LineAndEdgeTypeDefinition");
        sb.append(" lineType=").append(this.lineType);
        sb.append(" dashCycleRepeatLength=").append(this.dashCycleRepeatLength);
        sb.append(" [");
        for (int i = 0; i < this.dashElements.length; i++) {
        	sb.append(this.dashElements[i]).append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}

/*
 * vim:encoding=utf8
 */
