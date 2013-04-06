package jart.generators.dart;

import jart.info.ClassInfo;
import jart.info.FieldInfo;
import jart.utils.FileDescriptor;
import jart.utils.JavaSourceProvider;
import jart.utils.Mangler;
import jart.utils.SourceWriter;
import jart.utils.TypeConverter;

import java.util.Map;
import java.util.Set;

import soot.SootClass;
import soot.SootField;

/**
 * Compiles the JVM bytecode to Dart classes. Classes are put into a library, the library file is called javalib.dart. Each
 * individual class is written to its own dart file. 
 * 
 * FIXME Should be optimized to a single file so a server doesn't need to get hit a gazillion times.
 * 
 * @author badlogic
 *
 */
public class DartCompiler implements jart.Compiler {
	DartMangler mangler = new DartMangler();
	TypeConverter converter = new DartTypeConverter();
	
	@Override
	public void compile(String outputPath, JavaSourceProvider sourceProvider, Set<SootClass> classes, Map<SootClass, ClassInfo> classInfos) {
		new FileDescriptor(outputPath).deleteDirectory();
		new FileDescriptor(outputPath).mkdirs();
		generateLibrary(outputPath, classes);
		
		for(SootClass clazz: classes) {
			FileDescriptor file = new FileDescriptor(outputPath).child(mangler.mangle(clazz) + ".dart");
			generateClass(file, sourceProvider, clazz, classInfos.get(clazz));
		}
	}
	
	/**
	 * Generates a java.dart file, defining the library we
	 * @param outputPath
	 * @param classes
	 */
	private void generateLibrary(String outputPath, Set<SootClass> classes) {
		FileDescriptor file = new FileDescriptor(outputPath).child("javalib.dart");
		SourceWriter w = new SourceWriter();
		w.wl("library javalib;\n");
		
		for(SootClass clazz: classes) {
			w.wl("part \"" + mangler.mangle(clazz)+ ".dart\";");
		}
		
		generateClassInitialization(w, classes);
		
		file.writeString(w.toString(), false);
	}

	private void generateClassInitialization(SourceWriter w,
			Set<SootClass> classes) {
	}

	private void generateClass(FileDescriptor outputFile, JavaSourceProvider sourceProvider, SootClass clazz, ClassInfo classInfo) {
		System.out.println("Generating file " + outputFile);
		SourceWriter w = new SourceWriter();
		w.wl("part of javalib;");
		
		if(clazz.isInterface() || clazz.isAbstract()) w.w("abstract ");
		w.w("class " + classInfo.mangledName);
		if(!classInfo.mangledName.equals("java_lang_Object")) w.w(" extends " + classInfo.superClass);
		if(classInfo.interfaces.size() > 0) {
			w.w(" implements ");
			int i = 0;
			for(String itf: classInfo.interfaces) {
				w.w(itf);
				if(i < classInfo.interfaces.size()-1) w.w(", ");
				i++;
			}
		}
		
		w.wl(" {");
		w.push();
		
		generateFields(w, sourceProvider, clazz, classInfo);
		
		w.pop();
		w.wl("}");
		
		outputFile.writeString(w.toString(), true);
	}

	private void generateFields(SourceWriter w,
			JavaSourceProvider sourceProvider, SootClass clazz,
			ClassInfo classInfo) {
		// TODO Auto-generated method stub
		for(SootField field: clazz.getFields()) {
			FieldInfo fieldInfo = classInfo.fieldInfos.get(field);
			w.wl((field.isStatic()?"static ": "") + fieldInfo.cType + " " + fieldInfo.mangledName + ";");
		}
	}

	@Override
	public Mangler getMangler() {
		return mangler;
	}

	@Override
	public TypeConverter getTypeConverter() {
		return converter;
	}
}
