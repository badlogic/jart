package jart;

import java.util.Map;
import java.util.Set;

import soot.SootClass;
import jart.info.ClassInfo;
import jart.utils.JavaSourceProvider;

/**
 * Compiler interface, taking soot classes and {@link ClassInfo} instances.
 * @author badlogic
 *
 */
public interface Compiler {
	void compile(String outputPath, JavaSourceProvider sourceProvider, Set<SootClass> classes, Map<SootClass, ClassInfo> classInfos);
}
