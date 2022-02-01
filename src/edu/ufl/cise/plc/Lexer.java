package edu.ufl.cise.plc;
import java.util.*;
import java.util.ArrayList;

import com.sun.source.tree.Tree;
import edu.ufl.cise.plc.IToken.Kind;

/* Currently Implemented:
 * <token> : +, ;, &, !, ,, /, (, ), [, ], -, %, !=, |, ^, *,=,intnum,
 */

public class Lexer implements ILexer {
	HashMap<String, Kind> reservedWords;
	private String inputString;
	private int position = 0;
	private States state;
	private ArrayList<IToken> tokenArr = new ArrayList<IToken>();
	private int arrListIndex = 0;

	//make prettier map if needed
	public Lexer(){
		reservedWords= new HashMap<String, Kind>();;
		reservedWords.put("BLACK", Kind.COLOR_CONST);
		reservedWords.put("BLUE",  Kind.COLOR_CONST);
		reservedWords.put("CYAN",  Kind.COLOR_CONST);
		reservedWords.put("DARK_GRAY", Kind.COLOR_CONST);
		reservedWords.put("GRAY", Kind.COLOR_CONST);
		reservedWords.put("GREEN", Kind.COLOR_CONST);
		reservedWords.put("LIGHT_GRAY", Kind.COLOR_CONST);
		reservedWords.put("MAGENTA", Kind.COLOR_CONST);
		reservedWords.put("ORANGE", Kind.COLOR_CONST);
		reservedWords.put("PINK", Kind.COLOR_CONST);
		reservedWords.put("RED", Kind.COLOR_CONST);
		reservedWords.put("WHITE", Kind.COLOR_CONST);
		reservedWords.put("YELLOW", Kind.COLOR_CONST);
		reservedWords.put("true", Kind.BOOLEAN_LIT);
		reservedWords.put("false", Kind.BOOLEAN_LIT);

		reservedWords.put("if", Kind.KW_IF);
		reservedWords.put("fi", Kind.KW_FI);
		reservedWords.put("else", Kind.KW_ELSE);
		reservedWords.put("write", Kind.KW_WRITE);
		reservedWords.put("console", Kind.KW_CONSOLE);
		reservedWords.put("getWidth", Kind.IMAGE_OP);
		reservedWords.put("getHeight", Kind.IMAGE_OP);
		reservedWords.put("int", Kind.TYPE);
		reservedWords.put("float", Kind.TYPE);
		reservedWords.put("string", Kind.TYPE);
		reservedWords.put("boolean", Kind.TYPE);
		reservedWords.put("color", Kind.TYPE);
		reservedWords.put("image", Kind.TYPE);
		reservedWords.put("void", Kind.TYPE);
	}


	//DFA doesnt mention color_op on reserved? Add?

	private static enum States {
		START,
		HAVE_EQ,
		HAVE_BANG,
		HAVE_AND,
		IN_NUM,
		IN_FLOAT,
		IN_INDENT,
		HAVE_GREAT,
		HAVE_LESS,

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
		int startPos=0;
		state = States.START;
		while(true) {
			if(inputString.length() > position) {
				ch = inputString.charAt(position);  // get current character
			}
			else {
				ch = '0';
				//change or keep
			}
			//startPos = position; //maybe do this  when start happens, i feel like startPos resets after every while run
			// will be detrimental to values that need more than one char. Maybe, update startpos to position to the start case, tell if wrong;
			switch(state) {
				case START -> {
					startPos = position;  // change this if needed
					switch(ch) {
						case '+' -> {
							tokenArr.add(new Token(Kind.PLUS, startPos, 1, String.valueOf(ch)));
							position++;
							break;
						}
						case 'a', 'A' ,'b', 'B', 'c', 'C', 'd', 'D', 'e','E' ,'f', 'F','g','G','h','H',
								'i','I','j','J','k','K','l','L','m','M', 'n','N','o','O','p','P','q', 'Q', 'r','R','s','S','t','T',
								'u','U','v','V', 'w','W', 'x', 'X','y','Y','z','Z','$','_'-> {
							state=States.IN_INDENT;
							position++;
							break;
						}
						case '1','2','3','4','5','6','7','8','9'->{
							state=States.IN_NUM;
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
						case '=' -> {
							state = States.HAVE_EQ;  // Need to determine if '!' or '!='
							position++;
							break;
						}
						case '>' -> {
							state = States.HAVE_GREAT;
							position++;
							break;
						}
						case '<' -> {
							state = States.HAVE_LESS;
							position++;
							break;

						}


						case '0' -> {
							//this needs to be fixed
							tokenArr.add(new Token(Kind.EOF, startPos, 1, String.valueOf('h'))); // For EOF I think string doesn't matter?
							state = States.END;
							break;
						}
						default -> throw new IllegalStateException("Lexer bug (START)");
					}
				}

				case IN_INDENT -> {
					//not sure if this will be STRING_LIT or Ident, check with teacher
					switch (ch) {
						case 'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D', 'e', 'E', 'f', 'F', 'g', 'G', 'h', 'H',
								'i', 'I', 'j', 'J', 'k', 'K', 'l', 'L', 'm', 'M', 'n', 'N', 'o', 'O', 'p', 'P', 'q', 'Q', 'r', 'R', 's', 'S', 't', 'T',
								'u', 'U', 'v', 'V', 'w', 'W', 'x', 'X', 'y', 'Y', 'z', 'Z', '$', '_', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
								//,'+', '*','=' 			DFA says these values not needed?
								-> {
							position++;
							break;

						}
						//need to set things like forbidden words  and true and false here

						default -> {

							String value = inputString.substring(startPos, position);
							if (reservedWords.containsKey(value)) {
								tokenArr.add(new Token(reservedWords.get(value), startPos, position - startPos, String.valueOf(value)));
							} else {

								tokenArr.add(new Token(Kind.IDENT, startPos, position - startPos, String.valueOf(value)));
								state = state.START;                                                //check if right
							}

						}


					}
				}
				case IN_NUM -> {
					switch(ch){
						case '0','1','2','3','4','5','6','7','8','9'->{

							position++;
						}
						case '.'->{
							position++;
							state = States.IN_FLOAT;

						}
						default ->{
							tokenArr.add(new Token(Kind.IDENT,startPos, position-startPos, String.valueOf(inputString.substring(startPos, position))));
							state=state.START;												//check if right
						}

					}

				}
				case IN_FLOAT -> {
					switch (ch) {
						case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
							//still need to consider cases with leading 0s at end of decimal
							position++;
						}
						default -> {
							tokenArr.add(new Token(Kind.FLOAT_LIT, startPos, position - startPos, String.valueOf(inputString.substring(startPos, position))));
							state = state.START;
						}

					}
				}


				case HAVE_EQ -> {
					switch(ch){
						case'='->{
							tokenArr.add(new Token(Kind.EQUALS, startPos, 2, inputString.substring(startPos, position)));
							state= state.START;
							position++;
							break;
						}
						default -> {
							throw new IllegalStateException("Lexer bug (HAVE_EQUAL)");
						}
					}




					//Determine if ==
					//		if(inputString.length() > position) {
					//			ch = inputString.charAt(position);
					//		}
					//		else{
					//			tokenArr.add(new Token(Kind.ASSIGN, startPos, 1, String.valueOf(ch)));
					//			state = States.START;
					//			break;
					//		}
					//		if(ch == '=') {
					//			tokenArr.add(new Token(Kind.EQUALS, startPos, 2, inputString.substring(startPos, position)));
					//			position++;
					//			state = States.START;
					//		}
					//		else if( ch!= '='){
					//			tokenArr.add(new Token(Kind.ASSIGN,startPos, 1,String.valueOf(ch)));
					//			//Dont Increment Position
					//		}
					//		else{
					//			throw new IllegalStateException("Lexer bug (HAVE_EQ)");
					//		}

				}
//check if my logic makes sense
				case HAVE_LESS ->{
					switch(ch){
						case'='-> {
							tokenArr.add(new Token(Kind.LE, startPos, 2, inputString.substring(startPos, position)));
							position++;
							state = States.START;
							break;
						}
						case '<'->{
							tokenArr.add(new Token(Kind.LANGLE, startPos, 2, inputString.substring(startPos, position)));
							position++;
							state = States.START;
							break;
						}
						default -> {
							tokenArr.add(new Token(Kind.LT, startPos, 1, String.valueOf(ch)));

							state= state.START;

						}
					}
				}
				case HAVE_GREAT ->{
					switch(ch){
						case'='-> {
							tokenArr.add(new Token(Kind.GE, startPos, 2, inputString.substring(startPos, position)));
							state = States.START;
							position++;
							break;
						}
						case '>'->{
							tokenArr.add(new Token(Kind.RANGLE, startPos, 2, inputString.substring(startPos, position)));
							state = States.START;
							position++;
							break;
						}
						default -> {
							tokenArr.add(new Token(Kind.GT, startPos, 1, String.valueOf(ch)));
							state= state.START;

						}
					}
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
						tokenArr.add(new Token(Kind.NOT_EQUALS, startPos, 2, inputString.substring(startPos, position)));
						//check if input positioning is right
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