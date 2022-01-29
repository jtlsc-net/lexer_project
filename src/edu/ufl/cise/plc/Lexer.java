package edu.ufl.cise.plc;

import java.util.ArrayList;
import edu.ufl.cise.plc.IToken.Kind;

/* Currently Implemented:
 * <token> : +, ;, &, !, ,, /, (, ), [, ], -, %, !=, |, ^, *
 */

public class Lexer implements ILexer {
	
	private String inputString;
	private int position = 0;
	private States state;
	private ArrayList<IToken> tokenArr = new ArrayList<IToken>();
	private int arrListIndex = 0;
	
	private static enum States {
		START,
		HAVE_EQ,
		HAVE_BANG,
		END
	}
	
	public Lexer(String input) {
		this.inputString = input;
	}
	
	// Note that returning interface allows a function to return anything that implements
	// that interface.
	@Override
	public IToken next() throws LexicalException {
		char ch;
		int startPos;
		state = States.START;
		while(true) {
			if(inputString.length() > position) {
				ch = inputString.charAt(position);  // get current character
			}
			else {
				ch = '0';
			}
			startPos = position;
			switch(state) {
			case START -> {
				switch(ch) {
				case '+' -> {
					tokenArr.add(new Token(Kind.PLUS, startPos, 1, String.valueOf(ch)));
					position++;
					break;
				}
				case ';' -> {
					tokenArr.add(new Token(Kind.SEMI, startPos, 1, String.valueOf(ch)));
					position++;
					break;
				}
				case '|' -> {
					tokenArr.add(new Token(Kind.OR, startPos, 1, String.valueOf(ch)));
					position++;
					break;
				}
				case '%' -> {
					tokenArr.add(new Token(Kind.MOD, startPos, 1, String.valueOf(ch)));
					position++;
					break;
				}
				case '^' -> {
					tokenArr.add(new Token(Kind.RETURN, startPos, 1, String.valueOf(ch)));
					position++;
					break;
				}
				case '&' -> {
					tokenArr.add(new Token(Kind.AND, startPos, 1, String.valueOf(ch)));
					position++;
					break;
				}
				case ',' -> {
					tokenArr.add(new Token(Kind.COMMA, startPos, 1, String.valueOf(ch)));
					position++;
					break;
				}
				case '/' -> {
					tokenArr.add(new Token(Kind.DIV, startPos, 1, String.valueOf(ch)));
					position++;
					break;
				}
				case '(' -> {
					tokenArr.add(new Token(Kind.LPAREN, startPos, 1, String.valueOf(ch)));
					position++;
					break;
				}
				case ')' -> {
					tokenArr.add(new Token(Kind.RPAREN, startPos, 1, String.valueOf(ch)));
					position++;
					break;
				}
				case '[' -> {
					tokenArr.add(new Token(Kind.LSQUARE, startPos, 1, String.valueOf(ch)));
					position++;
					break;
				}
				case ']' -> {
					tokenArr.add(new Token(Kind.RSQUARE, startPos, 1, String.valueOf(ch)));
					position++;
					break;
				}
				case '-' -> {
					tokenArr.add(new Token(Kind.MINUS, startPos, 1, String.valueOf(ch)));
					position++;
					break;
				}
				case '*' -> {
					tokenArr.add(new Token(Kind.TIMES, startPos, 1, String.valueOf(ch)));
					position++;
					break;
				}
				case '!' -> {
					state = States.HAVE_BANG;  // Need to determine if '!' or '!='
					position++;
					break;
				}
				case '0' -> {
					tokenArr.add(new Token(Kind.EOF, startPos, 1, String.valueOf('h'))); // For EOF I think string doesn't matter?
					state = States.END;
					break;
				}
				default -> throw new IllegalStateException("Lexer bug (START)");
				}
			}
			case HAVE_EQ -> {
				
			}
			// Need to determine if '!' or '!='
			case HAVE_BANG -> {
				// Check for end of file
				if(inputString.length() > position) {
					ch = inputString.charAt(position);  // get current character
				}
				// This would be a string like "HI!"
				else {
					tokenArr.add(new Token(Kind.BANG, startPos, 1, String.valueOf(ch)));
					state = States.START;
					break;
				}
				if(ch == '=') {
					tokenArr.add(new Token(Kind.NOT_EQUALS, startPos, 2, String.valueOf(ch)));
					position++;
					state = States.START;
				}
				else if(ch != '=') {
					tokenArr.add(new Token(Kind.BANG, startPos, 1, String.valueOf(ch)));
					state = States.START;
					// DONT INCREMENT POSITION HERE!
				}
				else {
					throw new IllegalStateException("Lexer bug (HAVE_BANG)");
				}
			}
			default -> throw new IllegalStateException("Lexer bug");
			}
			if(state == States.END) {
				break;
			}
			
		}
		
		// Find return token
		if(arrListIndex < tokenArr.size()) {
			IToken returnToken = tokenArr.get(arrListIndex);
			arrListIndex++;
			return returnToken;
		}
		else {
			throw new LexicalException("Attempted to access out of bounds.");
		}
	}

	@Override
	public IToken peek() throws LexicalException {
		throw new LexicalException("Peek not implemented yet.");
	}

}
