package jart.utils;

import soot.ArrayType;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;

public interface Mangler {
	/**
	 * Mangles a field name, prepending {@link #FIELD_PREFIX} 
	 * @param field the {@link SootField}
	 * @return the mangled name, prefixed with {@link #FIELD_PREFIX}
	 */
	public String mangle(SootField field);
	
	/**
	 * Mangles a method name. <code>&lt;clinit></code> and <code>&lt;init></code>
	 * are mangled to <code>clinit</code> and <code>init</code> plus the {@link #METHOD_PREFIX}
	 * @param method the {@link SootMethod}
	 * @return the mangled name, prefixed with {@link #METHOD_PREFIX}
	 */
	public String mangle(SootMethod method);
	
	/**
	 * Mangles a method name. <code>&lt;clinit></code> and <code>&lt;init></code>
	 * are mangled to <code>clinit</code> and <code>init</code> plus the {@link #METHOD_PREFIX}
	 * @param method the {@link SootMethodRef}
	 * @return the mangled name, prefixed with {@link #METHOD_PREFIX}
	 */
	public String mangle(SootMethodRef methodRef);
	
	/**
	 * Mangles a class name. 
	 * @param method the {@link SootClass}
	 * @return the mangled name
	 */	
	public String mangle(SootClass clazz);
	
	/**
	 * Mangles a {@link Type} name. This includes class names, array names
	 * and primitive type names. 
	 * @param type the Type to mangle
	 * @return the mangled type name
	 */
	public String mangle(Type type);
	
	/**
	 * Mangles float literals. Infinity and NaN are translated to a corresponding constant.
	 * @param numeric the float literal
	 * @return the mangled float literal
	 */
	public String mangleFloat(String numeric);
	
	/**
	 * Mangles double literals. Infinity and NaN are translated to a corresponding constant.
	 * @param numeric the double literal
	 * @return the mangled double literal
	 */
	public String mangleDouble(String numeric);
}
