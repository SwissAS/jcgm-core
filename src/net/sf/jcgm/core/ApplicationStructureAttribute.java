package net.sf.jcgm.core;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.DataInput;
import java.io.IOException;
import java.util.List;



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




	public String getAttributeType() {
		return this.attributeType;
	}

	public StructuredDataRecord getStructuredDataRecord() {
		return this.structuredDataRecord;
	}

	


}
