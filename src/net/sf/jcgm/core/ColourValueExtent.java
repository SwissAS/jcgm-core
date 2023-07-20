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

import java.io.*;

import net.sf.jcgm.core.ColourModel.Model;


/**
 * Class=1, Element=10
 * @author xphc (Philippe Cadé)
 * @author BBNT Solutions
 * @version $Id$
 */
public class ColourValueExtent extends Command {
	private double firstComponentScale;
	private double secondComponentScale;
	private double thirdComponentScale;
	
	public ColourValueExtent(int ec, int eid, int l, DataInput in, CGM cgm)
            throws IOException {
        super(ec, eid, l, in, cgm);
        
        Model colorModel = cgm.getColourModel();
		if (colorModel.equals(ColourModel.Model.RGB) || colorModel.equals(ColourModel.Model.CMYK)) {
        	int precision = this.cgm.getColourPrecision();
        	
        	if (colorModel.equals(Model.RGB)) {
		        cgm.setMinimumColorValueRGB(new int[]{makeUInt(precision), makeUInt(precision), makeUInt(precision)});
		        cgm.setMaximumColorValueRGB(new int[]{makeUInt(precision), makeUInt(precision), makeUInt(precision)});
        	}
        	else {
        		unsupported("unsupported color model "+colorModel, this.cgm);
        	}
        }
        else if (colorModel.equals(ColourModel.Model.CIELAB) || 
        		colorModel.equals(ColourModel.Model.CIELUV) ||
        		colorModel.equals(ColourModel.Model.RGB_RELATED)) {
        	this.firstComponentScale = makeReal();
        	this.secondComponentScale = makeReal();
        	this.thirdComponentScale = makeReal();
        }
        else {
    		unsupported("unsupported color model "+colorModel, this.cgm);
        }
        
        // make sure all the arguments were read
        assert (this.currentArg == this.args.length);
    }

    @Override
	public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("ColourValueExtent");
	    final Model colourModel = this.cgm.getColourModel();
	    if (ColourModel.Model.RGB.equals(colourModel)) {
		    final int[] minimumColorValueRGB = this.cgm.getMinimumColorValueRGB();
		    sb.append(" min RGB=(").append(minimumColorValueRGB[0]).append(",");
		    sb.append(minimumColorValueRGB[1]).append(",");
		    sb.append(minimumColorValueRGB[2]).append(")");
		    
		    final int[] maximumColorValueRGB = this.cgm.getMaximumColorValueRGB();
		    sb.append(" max RGB=(").append(maximumColorValueRGB[0]).append(",");
        	sb.append(maximumColorValueRGB[1]).append(",");
        	sb.append(maximumColorValueRGB[2]).append(")");
        }
        else if (ColourModel.Model.CMYK.equals(colourModel)) {
        	// unsupported
        }
        else if (ColourModel.Model.CIELAB.equals(colourModel) ||
			    ColourModel.Model.CIELUV.equals(colourModel) ||
			    ColourModel.Model.RGB_RELATED.equals(colourModel)) {
        	sb.append(" first=").append(this.firstComponentScale);
        	sb.append(" second=").append(this.secondComponentScale);
        	sb.append(" third=").append(this.thirdComponentScale);
        }
        return sb.toString();
    }
}

/*
 * vim:encoding=utf8
 */
