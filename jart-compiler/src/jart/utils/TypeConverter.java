package jart.utils;

import java.util.List;

import soot.Type;

public interface TypeConverter {
	public String toType(Type type);
	public String toUnsignedType(Type type);
	
	public String generateArraySignature(String elementType, int numDimensions);
	
	public String generateArray(int size, String elementType, boolean isPrimitive);
	
	public String generateArray(String size, String elementType, boolean isPrimitive);
	
	public String generateMultiArray(String target, String elementType, boolean isPrimitive, List<String> sizes);
}
