package net.sf.jcgm.core;

import java.io.DataInput;
import java.io.IOException;

/**
 * Class=0, Element=8
 * @author jpprade (Jean-Philippe Prade)
 * @version $Id$
 */
public class BeginFigure extends Command {

	public BeginFigure(int ec, int eid, int l, DataInput in) throws IOException {
		super(ec, eid, l, in); 
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("BeginFigure ");
		return sb.toString();
	}

}
