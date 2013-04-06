package jart.generators.jart;

import jart.info.ClassInfo;
import jart.utils.JavaSourceProvider;

import java.util.Map;
import java.util.Set;

import soot.SootClass;

public class JartCompiler implements jart.Compiler {
	@Override
	public void compile(String outputPath, JavaSourceProvider sourceProvider,
			Set<SootClass> classes, Map<SootClass, ClassInfo> classInfos) {
	}
}
