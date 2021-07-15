package net.sf.jcgm.core;

import java.awt.geom.Point2D.Double;
import java.io.DataInput;
import java.io.IOException;

/**
 * Class=4, Element=6
 * @author jpprade (Jean-Philippe Prade)
 * @version $Id$
 */
public class AppendText extends TextCommand {

	protected int isFinal = 0;

	public AppendText(int ec, int eid, int l, DataInput in) throws IOException {
		super(ec, eid, l, in);		
		this.isFinal = makeInt();
		this.string = makeString();

	}

	@Override
	Double getTextOffset(CGMDisplay d) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This element is painted in the preceeding TextElement
	 */
	@Override
	public void paint(CGMDisplay d) {
		return;
	}

}
