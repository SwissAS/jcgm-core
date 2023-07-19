package net.sf.jcgm.core;

import java.io.DataInput;
import java.io.IOException;

/**
 * Class=0, Element=23
 * @version $Id:  $ 
 * @author  xphc
 * @since Oct 12, 2010
 */
public class EndApplicationStructure extends Command {

	EndApplicationStructure(int ec, int eid, int l, DataInput in, CGM cgm)
			throws IOException {
		super(ec, eid, l, in, cgm);
		// no arguments

		// make sure all the arguments were read
		assert (this.currentArg == this.args.length);
	}

	@Override
	public void paint(CGMDisplay d) {
		d.setWithinApplicationStructureBody(false);
	}

	@Override
	public String toString() {
		return "EndApplicationStructure";
	}

}
