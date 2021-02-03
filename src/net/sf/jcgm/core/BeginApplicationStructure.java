package net.sf.jcgm.core;

import java.io.DataInput;
import java.io.IOException;

/**
 * Class=0, Element=21
 * @author jpprade (Jean-Philippe Prade)
 * @version $Id$
 */
public class BeginApplicationStructure extends Command {

	private final String identifier;
	private final String type;

	public BeginApplicationStructure(int ec, int eid, int l, DataInput in) throws IOException {
		super(ec, eid, l, in);
		this.identifier = makeString();
		this.type = makeString();
		//TODO read flags 
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("BeginApplicationStructure [");
		sb.append("identifier " + this.identifier + " ");
		sb.append("type " + this.type );		
		sb.append("]");
		return sb.toString();
	}


	public String getIdentifier() {
		return this.identifier;
	}


	public String getType() {
		return this.type;
	}



}
