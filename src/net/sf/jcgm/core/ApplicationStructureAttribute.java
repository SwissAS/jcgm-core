package net.sf.jcgm.core;

import java.io.DataInput;
import java.io.IOException;


/**
 * Class=9, Element=1
 * @author jpprade (Jean-Philippe Prade)
 * @version $Id$
 */
public class ApplicationStructureAttribute extends Command {

	private String attributeType="";

	private StructuredDataRecord structuredDataRecord=null;

	public ApplicationStructureAttribute(int ec, int eid, int l, DataInput in) throws IOException {
		super(ec, eid, l, in);
		this.attributeType = makeString();
		this.structuredDataRecord = makeSDR();
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ApplicationStructureAttribute [");
		sb.append("attributeType " + this.attributeType + " ");
		sb.append("structuredDataRecord " + this.structuredDataRecord );		
		sb.append("]");
		return sb.toString();
	}

}
