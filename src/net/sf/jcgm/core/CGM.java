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
import java.util.*;
import java.awt.Dimension;
import java.awt.geom.Point2D;

import net.sf.jcgm.core.ScalingMode.Mode;

/**
 * The main class for Computer Graphics Metafile support.
 * @author xphc (Philippe Cadé)
 * @author BBNT Solutions
 * @version $Id$
 */
public class CGM implements Cloneable {
    private Vector<Command> commands;
    
    private List<ICommandListener> commandListeners = new ArrayList<ICommandListener>();
    
    public CGM() {
    	// empty constructor. XXX: Remove?
    }
    
	public CGM(File cgmFile) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(cgmFile));
        read(in);
        in.close();
    }

    public void read(DataInput in) throws IOException {
    	reset();
        this.commands = new Vector<Command>();
        while (true) {
            Command c = Command.read(in);
            if (c == null)
                break;
            
            for (ICommandListener listener : commandListeners) {
				listener.commandProcessed(c.getElementClass(), c.getElementCode(), c.toString());
			}
            
            // get rid of all arguments after we read them
            c.cleanUpArguments();
            this.commands.addElement(c);
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
		ColorIndexPrecision.reset();
		ColorModel.reset();
		ColorPrecision.reset();
		ColorSelectionMode.reset();
		ColourValueExtent.reset();
		EdgeWidthSpecificationMode.reset();
		IndexPrecision.reset();
		IntegerPrecision.reset();
		LineWidthSpecificationMode.reset();
		MarkerSizeSpecificationMode.reset();
		RealPrecision.reset();
		RestrictedTextType.reset();
		VDCIntegerPrecision.reset();
		VDCRealPrecision.reset();
		VDCType.reset();
		
		Messages.getInstance().reset();
	}
	
	public List<Message> getMessages() {
		return Messages.getInstance();
	}

	public void paint(CGMDisplay d) {
        Enumeration<Command> e = this.commands.elements();
        while (e.hasMoreElements()) {
            Command c = e.nextElement();
            if (filter(c)) {
            	c.paint(d);
            }
        }
    }

	private boolean filter(Command c) {
		return true;
//		List<Class<?>> classes = new ArrayList<Class<?>>();
//		//classes.add(PolygonElement.class);
//		classes.add(RestrictedText.class);
//		
//		for (Class<?> clazz: classes) {
//			if (clazz.isInstance(c))
//				return true;
//		}
//		
//		return false;
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
    	
    	int width = (int)(Math.abs(extent[1].x - extent[0].x) * factor);
    	int height = (int)(Math.abs(extent[1].y - extent[0].y) * factor);
    	
    	return new Dimension(width, height);
    }

    public Point2D.Double[] extent() {
        Enumeration<Command> e = this.commands.elements();
        while (e.hasMoreElements()) {
            Command c = e.nextElement();
            if (c instanceof VDCExtent) {
                Point2D.Double[] extent = ((VDCExtent) c).extent();
                return extent;
            }
        }
        return null;
    }
    
    private ScalingMode getScalingMode() {
        Enumeration<Command> e = this.commands.elements();
        while (e.hasMoreElements()) {
            Command c = e.nextElement();
            if (c instanceof ScalingMode) {
                return (ScalingMode)c;
            }
        }
        return null;
    }
    
    @Override
	public Object clone() {
        CGM newOne = new CGM();
        //System.out.println("in cgm.clone");
        newOne.commands = new Vector<Command>();
        for (int i = 0; i < this.commands.size(); i++) {
            newOne.commands.addElement((Command)(this.commands.elementAt(i)).clone());
            //System.out.println("Command: " +
            // (Command)newOne.V.elementAt(i));
        }
        return newOne;
    }

    public void showCGMCommands() {
    	showCGMCommands(System.out);
    }

	public void showCGMCommands(PrintStream stream) {
        for (int i = 0; i < this.commands.size(); i++)
            stream.println("Command: " + this.commands.elementAt(i));
	}

}

/*
 * vim:encoding=utf8
 */
