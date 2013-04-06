package jart;

import jart.generators.cpp.CppCompiler;
import jart.generators.cpp.CppMangler;
import jart.generators.cpp.HeaderGenerator;
import jart.generators.cpp.ImplementationGenerator;
import jart.generators.cpp.RuntimeGenerator;
import jart.generators.dart.DartCompiler;
import jart.info.ClassInfo;
import jart.utils.FileDescriptor;
import jart.utils.JavaSourceProvider;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.Scene;
import soot.SootClass;
import soot.options.Options;

/**
 * Takes a directory containing .class files and generates Dart Code from the JVM bytecode.
 * 
 * @author mzechner
 *
 */
public class Jart {
	private Compiler compiler;
	private final String[] classPath;
	private final String[] sourcePath;
	private final String outputPath;
	private Set<SootClass> classes;
	private final Map<SootClass, ClassInfo> classInfos = new HashMap<SootClass, ClassInfo>();
	private JavaSourceProvider sourceProvider;
	
	/**
	 * Creates a new compiler, setting the classpath directory,
	 * the source path and the output path. Loads all classes
	 * based on the input parameters. The incremental flag
	 * defines whether classes should be translated incrementally, that
	 * is only if they class file is newer than the last .h/.cpp file
	 * generated for them. This improves compilation times for the C++
	 * code.
	 * 
	 * @param compiler the {@link Compiler} to use
	 * @param classPath the directory containing the .class files
	 * @param sourcePath the directory containing the Java source files
	 * @param outputPath the output directory
	 * @param incremental whether to incrementally translate files
	 */
	public Jart(Compiler compiler, String classPath, String sourcePath, String outputPath, boolean incremental) {
		this.compiler = compiler;
		this.classPath = parsePath(classPath);
		this.sourcePath = parsePath(sourcePath);
		this.outputPath = outputPath.endsWith("/")? outputPath: outputPath + "/";
	}
	
	private String[] parsePath(String path) {
		String[] paths = path.split(";");
		for(int i = 0; i < paths.length; i++) {
			paths[i] = paths[i].endsWith("/")? paths[i]: paths[i] + "/";
		}
		return paths;
	}
	
	private String concatenatePath(String[] paths) {
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < paths.length; i++) {
			buffer.append(paths[i]);
			if(i != paths.length -1) buffer.append(":");
		}
		return buffer.toString();
	}
	
	/**
	 * Sets up Soot and loads the classes from the classpath directory. If
	 * incremental builds are enabled, only classes who's classfile is newer
	 * than the last generated .h/.cpp file will be translated.
	 *  
	 * @return the loaded {@link SootClass} instances
	 */
	private Set<SootClass> loadClasses() {		
		Options.v().set_keep_line_number(true);
		Options.v().set_process_dir(Arrays.asList(classPath));
		Scene.v().setSootClassPath(concatenatePath(classPath));
		Options.v().set_full_resolver(true);
		Scene.v().loadNecessaryClasses();
		Scene.v().loadDynamicClasses();
		
		Set<SootClass> classes = new HashSet<SootClass>();
		for(SootClass clazz: Scene.v().getClasses()) {		
			classes.add(clazz);
			ClassInfo info = new ClassInfo(compiler.getMangler(), compiler.getTypeConverter(), clazz);
			classInfos.put(clazz, info);
		}
		return classes;
	}
	
	/**
	 * Generates .dart files for each class found in the classpath
	 */
	public void compile() {
		// load the classes and source files
		classes = loadClasses();
		sourceProvider = new JavaSourceProvider();
		for(String path: sourcePath) {
			sourceProvider.load(new FileDescriptor(path));
		}
		
		compiler.compile(outputPath, sourceProvider, classes, classInfos);
	}
	
	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Usage: Jack <classpath> <sources> <outputdir>");
			System.exit(0);
		}			
		
		String classpath = args[0].endsWith("/")? args[0]: args[0] + "/";
		String sources = args[1].endsWith("/")? args[1]: args[1] + "/";
		String outputDir = args[2].endsWith("/")? args[2]: args[2] + "/";
		
		Compiler compiler = new DartCompiler();
//		Compiler compiler = new CppCompiler();
		
		Jart jart = new Jart(compiler, classpath, sources, outputDir, false);
		jart.compile();
	}
}
