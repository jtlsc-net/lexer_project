package edu.ufl.cise.plc.ast;

public class Types {
	
	public static enum Type {
		BOOLEAN, COLOR, CONSOLE, FLOAT, IMAGE, INT, STRING, VOID, COLORFLOAT;
		
		public static Type toType(String s) {
			return switch(s) {
			case "boolean" -> Type.BOOLEAN;		
			case "color" -> Type.COLOR;
			case "console" -> Type.CONSOLE;
			case "float" -> Type.FLOAT;
			case "image" -> Type.IMAGE;
			case "int" -> Type.INT;
			case "string" -> Type.STRING;
			case "void" -> Type.VOID;	
			default -> throw new IllegalArgumentException("Unexpected type value: " + s);
			};
		}
	}
	
	
	public static Type toType(String s) {
		return switch(s) {
		case "boolean" -> Type.BOOLEAN;		
		case "color" -> Type.COLOR;
		case "console" -> Type.CONSOLE;
		case "float" -> Type.FLOAT;
		case "image" -> Type.IMAGE;
		case "int" -> Type.INT;
		case "string" -> Type.STRING;
		case "void" -> Type.VOID;	
		default -> throw new IllegalArgumentException("Unexpected type value: " + s);
		};
	}
	
	public static String toString(Type s) {
		return switch(s) {
		case BOOLEAN -> "boolean";
		case COLOR -> "color";
		case CONSOLE -> "console";
		case FLOAT -> "float";
		case IMAGE -> "image";
		case INT -> "int";
		case STRING -> "String";
		case VOID -> "void";
		default -> throw new IllegalArgumentException("Unexpected type: " + s);
		};
	}
	


}
