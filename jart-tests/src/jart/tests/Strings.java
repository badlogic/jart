package jart.tests;

public class Strings {
	static String staticString = "static string";
	
	public void test() {		
		String test = new String("this is a test");
		String test2 = "This is another test";
		
		test += test2;
		test2 = "a string" + "another string";
	}
}
