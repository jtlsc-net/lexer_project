package edu.ufl.cise.plc;

import edu.ufl.cise.plc.IToken.SourceLocation;

public class Token implements IToken {

	private SourceLocation sourceLocation;
	final int position;
	final int length;
	private String input;
	private Kind kind;


	public Token(Kind kind, int position, int length, String input, int lineNum, int colNum) {
		this.kind = kind;
		this.position = position;
		this.length = length;
		this.input = input;
		this.sourceLocation = new SourceLocation(lineNum, colNum); // TODO: Convert from position to source.
//		this.sourceLocation = findSource(position, length, input);
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
		return sourceLocation;
	}

	@Override
	public int getIntValue() {
		return Integer.parseInt(input);
	}

	@Override
	public float getFloatValue() {
		return Float.parseFloat(input);
	}

	@Override
	public boolean getBooleanValue() {
		return Boolean.parseBoolean(input);
	}

	@Override
	public String getStringValue() {
		int remove = 0;
		if(kind == Kind.STRING_LIT) {
			String newString = input.substring(1, input.length() - 1);
			char[] ch = newString.toCharArray();
			for(int i = 0; i < ch.length; i++) {
				if(ch[i] == '\\') {
					switch(ch[i + 1]) {
						case '\\' -> {
							ch[i] = '\\';
						}
						case 'b' -> {
							ch[i] = '\b';
						}
						case 't' -> {
							ch[i] = '\t';
						}
						case 'n' -> {
							ch[i] = '\n';
						}
						case 'f' -> {
							ch[i] = '\f';
						}
						case 'r' -> {
							ch[i] = '\r';
						}
						case '\"' -> {
							ch[i] = '\"';
						}
						case '\'' -> {
							ch[i] = '\'';
						}
						default -> {
							this.kind = Kind.ERROR;
							this.input = "Illegal string literal expression";
							return input;
						}
					}
					remove++;
					for(int z = i + 1; z < ch.length - 1; z++) {
						ch[z] = ch[z + 1];
					}
				}
			}
			char[] q = new char[ch.length - remove];
			for(int n = 0; n < q.length; n++){
				q[n] = ch[n];
			}
			return String.valueOf(q);
		} 
	   
		return input;
	}

}
