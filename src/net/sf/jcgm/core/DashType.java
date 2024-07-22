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

/**
 * @author xphc (Philippe Cadé)
 * @version $Id$
 * @since Feb 23, 2009
 */
public class DashType {
	public static final int SOLID = 1;
	public static final int DASH = 2;
	public static final int DOT = 3;
	public static final int DASH_DOT = 4;
	public static final int DASH_DOT_DOT = 5;
	// line types later registered with the ISO/IEC 9973 Items Register
	public final static int SINGLE_ARROW = 6;
	public final static int SINGLE_DOT = 7;
	public final static int DOUBLE_ARROW = 8;
	public final static int STITCH_LINE = 9;
	public final static int CHAIN_LINE = 10;
	public final static int CENTER_LINE = 11;
	public final static int HIDDEN_LINE = 12;
	public final static int PHANTOM_LINE = 13;
	public final static int BREAK_LINE_1_FREE_HAND = 14;
	public final static int BREAK_LINE_1_ZIG_ZAG = 15;
}

/*
 * vim:encoding=utf8
 */
