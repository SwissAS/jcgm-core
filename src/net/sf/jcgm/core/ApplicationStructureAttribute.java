package net.sf.jcgm.core;

import java.io.DataInput;
import java.io.IOException;


/**
 * Class=9, Element=1
 * @author jpprade (Jean-Philippe Prade)
 * @author BBNT Solutions
 * @version $Id$
 */
public class ApplicationStructureAttribute extends Command {

	private String attributeType="";

	private StructuredDataRecord structuredDataRecord=null;

	public ApplicationStructureAttribute(int ec, int eid, int l, DataInput in) throws IOException {
		super(ec, eid, l, in);

		int length = this.args[this.currentArg];

		if(length == 255) {
			//non géré
		}else {

			for(int i=0;i<length;i++) {
				this.currentArg++;
				this.attributeType = this.attributeType + (char)this.args[this.currentArg];
			}
			this.currentArg++;


			this.structuredDataRecord = makeSDR();
		}



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
