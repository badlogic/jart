package jart.generators.cpp;

import jart.info.ClassInfo;
import jart.utils.SourceWriter;
import jart.utils.TypeConverter;

import java.util.List;

import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.tagkit.Tag;

/**
 * Generates the body of a native method.
 * @author mzechner
 *
 */
public class NativeMethodGenerator {
	private static CppMangler mangler = new CppMangler();
	private static TypeConverter typeConverter = new CppTypeConverter();
	private final SourceWriter writer;
	private final ClassInfo info;
	private final SootMethod method;
	
	public NativeMethodGenerator(SourceWriter writer, ClassInfo info, SootMethod method) {
		this.writer = writer;
		this.info = info;
		this.method = method;
	}
	
	private boolean isDirect(List<Tag> tags) {
		for(Tag tag: tags) {
			if(tag.toString().contains("DirectNative")) return true;
		}
		return false;
	}
	
	public void generate() {
		// if this method or it's class is tagged with
		// @DirectNative, omit generation of the native
		// method wrapper.
		if(isDirect(method.getTags()) || isDirect(method.getDeclaringClass().getTags())) return;
		
		// output the signature
		String methodSig = "";
		methodSig += typeConverter.toType(method.getReturnType());
		methodSig += " " + info.mangledName + "::" + mangler.mangle(method) + "(";
		
		int i = 0;
		for(Object paramType: method.getParameterTypes()) {
			if(i > 0) methodSig += ", ";
			methodSig += typeConverter.toType((Type)paramType);
			methodSig += " param" + i;
			i++;
		}
		
		methodSig +=") {";
		writer.wl(methodSig);
		
		// FIXME JNI, add function pointer loading and invocation
		// output the body
		info.dependencies.add(Scene.v().getSootClass("java.lang.UnsupportedOperationException"));
		writer.push();
		writer.wl("throw new java_lang_UnsupportedOperationException();");
		writer.pop();
		writer.wl("}");
	}
}
