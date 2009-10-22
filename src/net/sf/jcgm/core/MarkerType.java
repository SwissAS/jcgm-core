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
 * Class=5, Element=6
 * @author xphc (Philippe Cadé)
 * @version $Id$
 * @since May 13, 2009
 */
class MarkerType extends Command {
	enum Type { DOT, PLUS, ASTERISK, CIRCLE, CROSS }
	private Type type = Type.ASTERISK;

	public MarkerType(int ec, int eid, int l, DataInput in) throws IOException {
		super(ec, eid, l, in);
		
		int typ = makeIndex();
		switch (typ) {
		case 1:
			this.type = Type.DOT;
			break;
		case 2:
			this.type = Type.PLUS;
			break;
		case 3:
			this.type = Type.ASTERISK;
			break;
		case 4:
			this.type = Type.CIRCLE;
			break;
		case 5:
			this.type = Type.CROSS;
			break;
		default:
			this.type = Type.ASTERISK;	
		}
	}

	@Override
	public void paint(CGMDisplay d) {
		d.setMarkerType(this.type);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("MarkerType ").append(this.type);
		return sb.toString();
	}
}

/*
 * vim:encoding=utf8
 */
