package jart.generators.cpp;

import jart.info.ClassInfo;
import jart.utils.JavaSourceProvider;
import jart.utils.Mangler;
import jart.utils.TypeConverter;

import java.util.Map;
import java.util.Set;

import soot.SootClass;

public class CppCompiler implements jart.Compiler {
	Mangler mangler = new CppMangler();
	TypeConverter typeConverter = new CppTypeConverter();
	
	/**
	 * Generates .dart files for each class found in the classpath
	 */
	public void compile(String outputPath, JavaSourceProvider sourceProvider, Set<SootClass> classes, Map<SootClass, ClassInfo> classInfos) {
		generateHeaders(outputPath, classes, classInfos);
		generateImplementations(outputPath, sourceProvider, classes, classInfos);
		generateAuxiliary(outputPath, classes, classInfos);
	}
	
	/**
	 * Generates the header file for each class
	 */
	private void generateHeaders(String outputPath, Set<SootClass> classes, Map<SootClass, ClassInfo> classInfos) {
		for(SootClass clazz: classes) {
			ClassInfo info = classInfos.get(clazz);
			HeaderGenerator headerGenerator = new HeaderGenerator(clazz, info, outputPath + info.mangledName + ".h");
			headerGenerator.generate();
		}
	}
	
	/**
	 * Generates the implementation file for each class
	 */
	private void generateImplementations(String outputPath, JavaSourceProvider sourceProvider, Set<SootClass> classes, Map<SootClass, ClassInfo> classInfos) {
		for(SootClass clazz: classes) {
			ClassInfo info = classInfos.get(clazz);
			info.gatherDependencies();
			ImplementationGenerator implGenerator = new ImplementationGenerator(clazz, sourceProvider, info, outputPath + info.mangledName + ".cpp");
			implGenerator.generate();
		}
	}
	
	/**
	 * Generates auxiliary file, such as the implementation
	 * of class initialization.
	 */
	private void generateAuxiliary(String outputPath, Set<SootClass> classes, Map<SootClass, ClassInfo> classInfos) {		
		new RuntimeGenerator(classes, classInfos, outputPath).generate();
	}

	@Override
	public Mangler getMangler() {
		return mangler;
	}

	@Override
	public TypeConverter getTypeConverter() {
		return typeConverter;
	}
}
