package jart.info;

import jart.utils.CTypes;
import jart.utils.Mangling;
import soot.SootField;

public class FieldInfo {
	public final SootField field;
	public final String mangledName;
	public final String cType;
	
	public FieldInfo(SootField field) {
		this.field = field;
		this.mangledName = Mangling.mangle(field);
		cType = CTypes.toCType(field.getType());
	}
}
