package net.sf.jcgm.core;

import java.util.List;

/**
 * One entry in the structured data record
 *
 * @author xphc
 * @version $Id:  $
 * @since Oct 5, 2010
 */
public class Member {
	private final StructuredDataRecord.StructuredDataType type;
	private final int count;
	private final List<Object> data;
	
	Member(StructuredDataRecord.StructuredDataType type, int count, List<Object> data) {
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
	
	public StructuredDataRecord.StructuredDataType getType() {
		return this.type;
	}
	
	public int getCount() {
		return this.count;
	}
	
	public List<Object> getData() {
		return this.data;
	}
	
}
