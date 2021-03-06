package nl.idgis.publisher.monitor.messages;

import java.io.Serializable;

public class ValueNode extends Node implements Serializable {
		
	private static final long serialVersionUID = -8986247890293001497L;
	
	protected final Object value;

	public ValueNode(String name, Object value) {
		super(name);
 
		this.value = value;
	}
	
	public Object getValue() {
		return value;
	}
	
	@Override
	StringBuilder toStringBuilder(int depth) {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < depth; i++) {
			sb.append("\t");
		}
		
		sb.append("- ");
		sb.append(name);
		
		return sb;
	}	
}
