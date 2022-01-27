package edu.ufl.cise.plc;

public class Token implements IToken{
    //returns the token kind
    public Kind getKind(){

    }

    //returns the characters in the source code that correspond to this token
    //if the token is a STRING_LIT, this returns the raw characters, including delimiting "s and unhandled escape sequences.
    public String getText(){}

    //returns the location in the source code of the first character of the token.
    public SourceLocation getSourceLocation(){}

    //returns the int value represented by the characters of this token if kind is INT_LIT
    public int getIntValue(){}

    //returns the float value represented by the characters of this token if kind is FLOAT_LIT
    public float getFloatValue()

    //returns the boolean value represented by the characters of this token if kind is BOOLEAN_LIT
    public boolean getBooleanValue()

    //returns the String represented by the characters of this token if kind is STRING_LIT
    //The delimiters should be removed and escape sequences replaced by the characters they represent.
    public String getStringValue()

}
