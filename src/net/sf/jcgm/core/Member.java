package net.sf.jcgm.core;

import java.util.List;

import net.sf.jcgm.core.StructuredDataRecord.StructuredDataType;

/**
 * One entry in the structured data record
 * @version $Id:  $ 
 * @author  xphc
 * @since Oct 5, 2010
 */
public class Member {
	private final StructuredDataType type;
	private final int count;
	private final List<Object> data;

	Member(StructuredDataType type, int count, List<Object> data) {
		this.type = type;
		this.count = count;
		this.data = data;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[type=");
		builder.append(this.type);
		builder.append(", count=");
		builder.append(this.count);
		builder.append(", data=");
		builder.append(this.data);
		builder.append("]");
		return builder.toString();
	}

	public StructuredDataType getType() {
		return this.type;
	}

	public int getCount() {
		return this.count;
	}

	public List<Object> getData() {
		return this.data;
	}
}
