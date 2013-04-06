package jart.generators.cpp;

import jart.info.ClassInfo;
import jart.utils.JavaSourceProvider;
import jart.utils.SourceWriter;

import java.util.TreeMap;

import soot.SootClass;
import soot.SootMethod;

public class ClinitGenerator {
	private static CppMangler mangler = new CppMangler();
	private final SourceWriter writer;
	private final JavaSourceProvider sourceProvider;
	private final ClassInfo info;
	private final SootMethod clinitMethod;
	
	public ClinitGenerator(SourceWriter writer, JavaSourceProvider sourceProvider, ClassInfo info, SootMethod clinitMethod) {
		this.writer = writer;
		this.sourceProvider = sourceProvider;
		this.info = info;
		this.clinitMethod = clinitMethod;
	}
	
	public void generate() {
		writer.wl("void " + mangler.mangle(info.clazz) + "::m_clinit() {");
		writer.push();
		writer.wl("// would enter class monitor for this class' clinit method");
		writer.wl("{");
		writer.push();
		
		// check if clinit was already called and bail out in that case
		writer.wl("if(" + mangler.mangle(info.clazz) + "::clinit) return;");
		
		// set the clinit flag of this class as a guard
		writer.wl(mangler.mangle(info.clazz) + "::clinit = true;");
		
		// generate the string literal definitions
		info.literals.generateDefinitions(writer);
		
		// emit calls to all classes and interfaces' clinit this class references
		// start with the super class
		if(!info.superClass.equals("gc")) {
			writer.wl(info.superClass + "::m_clinit();");
		}
		
		// generate clinit calls for dependencies, sorted
		TreeMap<String, SootClass> dependencies = new TreeMap<String, SootClass>();
		for(SootClass dependency: info.dependencies) {
			String name = mangler.mangle(dependency);
			if(!dependencies.containsKey(name)) {
				dependencies.put(name, dependency);
			}
		}
		for(SootClass dependency: dependencies.values()) {
			writer.wl(mangler.mangle(dependency) + "::m_clinit();");
		}		
		
		// generate the method body
		if(clinitMethod != null) {
			new MethodBodyGenerator(writer, sourceProvider, info, clinitMethod).generate();
		}
		writer.pop();
		writer.wl("}");
		writer.pop();
		writer.wl("}");
	}	
}
