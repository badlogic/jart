package jart.info;

import jart.utils.Mangler;
import jart.utils.TypeConverter;
import soot.SootField;

public class FieldInfo {
	public final SootField field;
	public final String mangledName;
	public final String cType;
	
	public FieldInfo(Mangler mangler, TypeConverter converter, SootField field) {
		this.field = field;
		this.mangledName = mangler.mangle(field);
		cType = converter.toType(field.getType());
	}
}
