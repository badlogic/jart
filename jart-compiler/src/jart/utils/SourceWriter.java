package jart.utils;

/**
 * Stores indentation levels that can be pushed/popped and
 * allows to output lines.
 * @author mzechner
 *
 */
public class SourceWriter {
	int indent;
	boolean idented = false;;
	final StringBuffer buffer = new StringBuffer();
	
	public SourceWriter() {		
	}

	private String i() {
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < indent; i++) {
			buffer.append("\t");
		}
		return buffer.toString();
	}
	
	public void wl() {
		buffer.append("\n");
		idented = false;
	}
	
	public void wl(String message) {
		buffer.append(i());
		buffer.append(message);
		buffer.append("\n");
		idented = false;
	}
	
	public void w(String message) {
		if(!idented) {
			buffer.append(i());
			idented = true;
		}
		buffer.append(message);
	}
	
	public void push() {
		indent++;
	}
	
	public void pop() {
		indent--;
		if(indent < 0) indent = 0;
	}
	
	public String toString() {
		return buffer.toString();
	}
}
