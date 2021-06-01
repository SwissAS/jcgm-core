package net.sf.jcgm.core;

import java.io.DataInput;
import java.io.IOException;

/**
 * Class=0, Element=9
 * @author jpprade (Jean-Philippe Prade)
 * @version $Id$
 */
public class EndFigure extends Command {

	public EndFigure(int ec, int eid, int l, DataInput in) throws IOException {
		super(ec, eid, l, in); 
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("EndFigure ");
		return sb.toString();
	}

}
