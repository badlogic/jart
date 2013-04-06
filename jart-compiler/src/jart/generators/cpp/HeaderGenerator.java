package jart.generators.cpp;

import jart.info.ClassInfo;
import jart.info.FieldInfo;
import jart.info.MethodInfo;
import jart.info.SyntheticMethodInfo;
import jart.utils.FileDescriptor;
import jart.utils.SourceWriter;
import jart.utils.TypeConverter;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;

/**
 * Takes a {@link SootClass} instance and a corresponding {@link ClassInfo}
 * instance and outputs a C++ header file
 * @author mzechner
 *
 */
public class HeaderGenerator {
	private static CppMangler mangler = new CppMangler();
	private static TypeConverter typeConverter = new CppTypeConverter();
	final SootClass clazz;
	final ClassInfo info;
	final String fileName;
	final SourceWriter writer;
	
	public HeaderGenerator(SootClass clazz, ClassInfo info, String fileName) {
		this.clazz = clazz;
		this.info = info;
		this.fileName = fileName;
		this.writer = new SourceWriter();
	}

	public void generate() {
		System.out.println("generating header for " + clazz.getName());
		
		// include guards
		writer.wl("#ifndef " + info.mangledName + "_h");
		writer.wl("#define " + info.mangledName + "_h");
		writer.wl("");
		
		// if this is java.lang.Object, we need to output 
		// header includes for the Boehm GC
		if(clazz.getName().equals("java.lang.Object")) {
			writer.wl("#define GC_THREADS");
			writer.wl("#define GC_NOT_DLL");
			writer.wl("#include <gc_cpp.h>");
		}
		
		// include common headers
		writer.wl("#include \"vm/types.h\"");		
		writer.wl("");
		
		// emit forward declarations
		for(String forwardDecl: info.forwardedClasses) {
			writer.wl("class " + forwardDecl + ";");
		}
		writer.wl("template <class T> class Array;");
		
		// emit superclass, interface and covariant return type includes
		for(String includedClass: info.includedClasses) {
			writer.wl("#include \"classes/" + includedClass + ".h\"");
		}
		writer.wl("");
		
		// emit class signature
		String signature = "class " + info.mangledName + ": ";
		signature += "public virtual " + info.superClass;
		for(String itf: info.interfaces) {
			signature += ", public virtual " + itf;
		}
		writer.wl(signature + " {");
		
		writer.push();
		writer.wl("public:" );
		writer.push();
		
		// emit fields (in the order they appear in the class file)
		for(SootField field: clazz.getFields()) {
			FieldInfo fieldInfo = info.fieldInfos.get(field);
			writer.wl((field.isStatic()?"static ": "") + fieldInfo.cType + " " + fieldInfo.mangledName + ";");
		}
		
		// add in a static field keeping track of whether clinit was called
		// and another static field for the class.
		writer.wl("static java_lang_Class* clazz;");
		writer.wl("static bool clinit;");
		writer.wl("");
		
		// emit methods (in the order they appear in the class file)
		for(SootMethod method: clazz.getMethods()) {
			MethodInfo methodInfo = info.methodInfos.get(method);
			if(methodInfo == null) {
				System.out.println("skipping method " + method);
				continue;
			}
			
			generateMethod(method, methodInfo);
		}
		
		// emit synthetic methods
		for(SyntheticMethodInfo syntheticMethod: info.syntheticMethods) {
			generateSyntheticMethod(syntheticMethod);
		}
		
		// emit getClass() implementation
		writer.wl("virtual java_lang_Class* m_getClass() { return " + mangler.mangle(clazz) + "::clazz; }");
		
		// if we don't have a <clinit> declaration, create one!
		if(!info.hasClinit) {
			writer.wl("static void m_clinit();");
		}
				
		
		writer.pop();
		writer.pop();
		writer.wl("};");
		writer.wl("#endif");
		
		// only update header if content changed
		if(needsUpdate(fileName, writer.toString())) {
			new FileDescriptor(fileName).writeString(writer.toString(), false);
		} else {
			System.out.println("Skipped '" + fileName + "', up-to-date");
		}
	}
	
	private boolean needsUpdate(String fileName, String newContent) {
		FileDescriptor file = new FileDescriptor(fileName);
		if(!file.exists()) return true;
		return !file.readString().equals(newContent);
	}
	
	private void generateMethod(SootMethod method, MethodInfo info) {
		SootClass clazz = method.getDeclaringClass();
		String methodSig = "";
		
		if(method.isStatic()) {
			methodSig +="static ";
		} else {
			methodSig +="virtual ";
		}
		
		methodSig += typeConverter.toType(method.getReturnType());
		methodSig += " " + mangler.mangle(method) + "(";
		
		int i = 0;
		for(Object paramType: method.getParameterTypes()) {
			if(i > 0) methodSig += ", ";
			methodSig += typeConverter.toType((Type)paramType);
			methodSig += " param" + i;
			i++;
		}
		
		methodSig +=")";
		if(clazz.isInterface() || method.isAbstract()) methodSig += " = 0";
		methodSig += ";";
		writer.wl(methodSig);
	}
	
	public void generateSyntheticMethod(SyntheticMethodInfo syntheticMethod) {
		String methodSig = "";
		SootMethod baseMethod = syntheticMethod.baseMethod;
		
		methodSig +="virtual ";		
		methodSig += syntheticMethod.returnType;
		methodSig += " " + mangler.mangle(baseMethod) + "(";
		
		int i = 0;
		for(Object paramType: baseMethod.getParameterTypes()) {
			if(i > 0) methodSig += ", ";
			methodSig += typeConverter.toType((Type)paramType);
			methodSig += " param" + i;
			i++;
		}
		boolean hasReturnType = !(baseMethod.getReturnType() instanceof VoidType);
		if(!syntheticMethod.isPure) {
			methodSig +=") { " + (hasReturnType?"return ":"") + mangler.mangle(baseMethod.getDeclaringClass()) + "::" + mangler.mangle(baseMethod) + "(";
			for(i = 0; i < baseMethod.getParameterTypes().size(); i++) {
				if(i > 0) methodSig += ", ";
				methodSig += " param" + i;
			}
			methodSig += "); };";
		} else {
			methodSig +=") = 0;";
		}
		writer.wl(methodSig);
	}
}
