package jart.generators.dart;

import java.util.List;

import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.LongType;
import soot.NullType;
import soot.RefType;
import soot.ShortType;
import soot.Type;
import soot.VoidType;
import jart.utils.TypeConverter;

public class DartTypeConverter implements TypeConverter {
	DartMangler mangler = new DartMangler();
	
	@Override
	public String toType(Type type) {
		if(type instanceof RefType) {
			return mangler.mangle(type);
		} else if(type instanceof ArrayType) {
			ArrayType t = (ArrayType)type;			
			String elementType = toType(t.baseType);
			String array = generateArraySignature(elementType, t.numDimensions);
			return array;			
		} else {		
			if(type instanceof BooleanType) return "bool";
			else if(type instanceof ByteType) return "int";
			else if(type instanceof CharType) return "int";
			else if(type instanceof ShortType) return "int";
			else if(type instanceof IntType) return "int";
			else if(type instanceof LongType) return "int";
			else if(type instanceof FloatType) return "double";
			else if(type instanceof DoubleType) return "double";
			else if(type instanceof VoidType) return "void";
			else if(type instanceof NullType) return "null";
			else throw new RuntimeException("Unknown primitive type " + type);
		}
	}

	@Override
	public String toUnsignedType(Type type) {
		if(type instanceof ByteType) return "int";
		else if(type instanceof CharType) return "int";
		else if(type instanceof ShortType) return "int";
		else if(type instanceof IntType) return "int";
		else if(type instanceof LongType) return "int";
		else throw new RuntimeException("Can't create unsigned primitive type for " + type);
	}

	@Override
	public String generateArraySignature(String elementType, int numDimensions) {
		return "List<" + elementType + ">";
	}

	@Override
	public String generateArray(int size, String elementType,
			boolean isPrimitive) {
		return generateArray(Integer.toString(size), elementType, isPrimitive);
	}

	@Override
	public String generateArray(String size, String elementType,
			boolean isPrimitive) {
		return "new List<" + elementType + ">(" + size + ")";
	}

	@Override
	public String generateMultiArray(String target, String elementType,
			boolean isPrimitive, List<String> sizes) {
		// FIXME
		return "MultiArraySig";
	}

}
