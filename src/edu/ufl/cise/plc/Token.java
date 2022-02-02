package edu.ufl.cise.plc;

public class Token implements IToken {

	//private SourceLocation sourceLocation;
	final int position;
	final int length;
	final String input;
	final Kind kind;


	public Token(Kind kind, int position, int length, String input) {
		this.kind = kind;
		this.position = position;
		this.length = length;
		this.input = input;
		//this.sourceLocation = new SourceLocation(0, 0); // TODO: Convert from position to source.
	}

	@Override
	// Returns the token kind.
	public Kind getKind() {
		return kind;
	}

	@Override
	//returns the characters in the source code that correspond to this token
	//if the token is a STRING_LIT, this returns the raw characters, including delimiting "s and unhandled escape sequences.
	//TODO: double check if I did this correctly.  I don't think so.
	public String getText() {
		return input;
	}

	@Override
	//returns the location in the source code of the first character of the token.
	public SourceLocation getSourceLocation() {
//		return sourceLocation;
		return null;
	}

	@Override
	public int getIntValue() {
		// TODO Auto-generated method stub
		return Integer.parseInt(input);
	}

	@Override
	public float getFloatValue() {
		return Float.parseFloat(input);
	}

	@Override
	public boolean getBooleanValue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getStringValue() {
		return input;
	}

}
