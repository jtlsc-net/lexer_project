
package edu.ufl.cise.plc;
//import java.sql.SQLOutput;
import java.time.temporal.ValueRange;
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


	//DFA doesnt mention color_op on reserved? Add?

	private static enum States {
		START,
		HAVE_EQ,
		HAVE_BANG,
		IS_DOT,
		IN_NUM,
		HAVE_MINUS,
		IN_FLOAT,
		IN_INDENT,
		HAVE_GREAT,
		HAVE_LESS,
		HAVE_COMMENT,
		HAVE_STRING_LIT,

		END
	}

	public Lexer(String input){
		this.inputString = input;
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
		reservedWords.put("void", Kind.KW_VOID);
		reservedWords.put("getRed", Kind.COLOR_OP);
		reservedWords.put("getGreen", Kind.COLOR_OP);
		reservedWords.put("getBlue",Kind.COLOR_OP);
		char ch = 'a';
		int startPos=0;
		int lineNum = 0;
		int colNum = 0;
		boolean haveZero = false; //Used for IN_NUM
		state = States.START;
		while(true) {
			if(state != States.IN_NUM) {
				haveZero = false;
			}
			if(inputString.length() > position & state != States.END) {
				ch = inputString.charAt(position);  // get current character

			}
			else if((inputString.length() == position) & (position != 0) & (state == States.START)) {
				ch = inputString.charAt(position - 1);
				state = States.END;
			}
			else if(position == 0 & state == States.START) {
				ch = 'h';
				state = States.END;
			}
			else if(state != States.START) {}  // State-based eof handling.
			else {
//				throw new LexicalException("Position error. Position: " + String.valueOf(position));
				tokenArr.add(new Token(Kind.ERROR, startPos, 1, "Position error. Position: " + String.valueOf(position), lineNum, colNum));
				state = States.END;
			}
			//startPos = position; //maybe do this  when start happens, i feel like startPos resets after every while run
			// will be detrimental to values that need more than one char. Maybe, update startpos to position to the start case, tell if wrong;
			switch(state) {
				case START -> {
					startPos = position;  // change this if needed
					switch(ch) {
						case '+' -> {
							tokenArr.add(new Token(Kind.PLUS, startPos, 1, String.valueOf(ch), lineNum, colNum));
							position++;
							colNum++;
							break;
						}
						case 'a', 'A' ,'b', 'B', 'c', 'C', 'd', 'D', 'e','E' ,'f', 'F','g','G','h','H',
								'i','I','j','J','k','K','l','L','m','M', 'n','N','o','O','p','P','q', 'Q', 'r','R','s','S','t','T',
								'u','U','v','V', 'w','W', 'x', 'X','y','Y','z','Z','$','_'-> {
							state=States.IN_INDENT;
							position++;
							break;
						}
						case '0' -> {
							state = States.IN_NUM;
							position++;
							haveZero = true;
							break;
						}
						case '1','2','3','4','5','6','7','8','9'->{
							state=States.IN_NUM;
							position++;
							break;

						}


						case ';' -> {
							tokenArr.add(new Token(Kind.SEMI, startPos, 1, String.valueOf(ch), lineNum, colNum));
							position++;
							colNum++;
							break;
						}
						case '|' -> {
							tokenArr.add(new Token(Kind.OR, startPos, 1, String.valueOf(ch), lineNum, colNum));
							position++;
							colNum++;
							break;
						}
						case '%' -> {
							tokenArr.add(new Token(Kind.MOD, startPos, 1, String.valueOf(ch), lineNum, colNum));
							position++;
							colNum++;
							break;
						}
						case '^' -> {
							tokenArr.add(new Token(Kind.RETURN, startPos, 1, String.valueOf(ch), lineNum, colNum));
							position++;
							colNum++;
							break;
						}
						case '&' -> {
							tokenArr.add(new Token(Kind.AND, startPos, 1, String.valueOf(ch), lineNum, colNum));
							position++;
							colNum++;
							break;
						}
						case ',' -> {
							tokenArr.add(new Token(Kind.COMMA, startPos, 1, String.valueOf(ch), lineNum, colNum));
							position++;
							colNum++;
							break;
						}
						case '/' -> {
							tokenArr.add(new Token(Kind.DIV, startPos, 1, String.valueOf(ch), lineNum, colNum));
							position++;
							colNum++;
							break;
						}
						case '(' -> {
							tokenArr.add(new Token(Kind.LPAREN, startPos, 1, String.valueOf(ch), lineNum, colNum));
							position++;
							colNum++;
							break;
						}
						case ')' -> {
							tokenArr.add(new Token(Kind.RPAREN, startPos, 1, String.valueOf(ch), lineNum, colNum));
							position++;
							colNum++;
							break;
						}
						case '[' -> {
							tokenArr.add(new Token(Kind.LSQUARE, startPos, 1, String.valueOf(ch), lineNum, colNum));
							position++;
							colNum++;
							break;
						}
						case ']' -> {
							tokenArr.add(new Token(Kind.RSQUARE, startPos, 1, String.valueOf(ch), lineNum, colNum));
							position++;
							colNum++;
							break;
						}
						case '-' -> {
							//			tokenArr.add(new Token(Kind.MINUS, startPos, 1, String.valueOf(ch)));
							//			position++;

							state= States.HAVE_MINUS;
							position++;
							break;
						}
						case '*' -> {
							tokenArr.add(new Token(Kind.TIMES, startPos, 1, String.valueOf(ch), lineNum, colNum));
							position++;
							colNum++;
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
						case '#' -> {
							state = States.HAVE_COMMENT;
							position++;
							break;
						}
						case '"' -> {
							state = States.HAVE_STRING_LIT;
							position++;
							break;
						}
						case '\n' -> {
							position++;
							lineNum++;
							colNum = 0;
							break;
						}
						case '\r' -> {
							position = position + 2; // only \r\n occurs in input
							lineNum++;
							colNum = 0;
							break;
						}
						case '\t' -> {
							position++;
							colNum++;
							break;
						}
						case ' ' -> {
							position++;
							colNum++;
							break;
						}
//						case '@' -> {
//							tokenArr.add(new Token(Kind.ERROR, startPos, 1, "unused", lineNum, colNum));
//							break;
//						}
//						default -> throw new LexicalException("ERROR in START state.  Most likely invalid char.");
						default -> {
							tokenArr.add(new Token(Kind.ERROR, startPos, 1, "ERROR in START state. Most likely invalid char.", lineNum, colNum));
							state = States.END;
						}
					}
				}

				case IN_INDENT -> {
					//not sure if this will be STRING_LIT or Ident, check with teacher
					switch (ch) {
						case 'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D', 'e', 'E', 'f', 'F', 'g', 'G', 'h', 'H',
								'i', 'I', 'j', 'J', 'k', 'K', 'l', 'L', 'm', 'M', 'n', 'N', 'o', 'O', 'p', 'P', 'q', 'Q', 'r', 'R', 's', 'S', 't', 'T',
								'u', 'U', 'v', 'V', 'w', 'W', 'x', 'X', 'y', 'Y', 'z', 'Z', '$', '_', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'

								-> {
							//	String value = inputString.substring(startPos, position);
							//	System.out.println(value + " ");
							//	System.out.println(this.reservedWords.size());
							position++;
							//consider making a universal value variable
							String value = inputString.substring(startPos, position);
							//removed later if unnecessary
							if(inputString.length() <= position) {
								if (reservedWords.containsKey(value)) {
									tokenArr.add(new Token(reservedWords.get(value), startPos, position - startPos, value, lineNum, colNum));
									colNum = colNum + (position - startPos);
									state = States.START;
								} else {

									tokenArr.add(new Token(Kind.IDENT, startPos, position - startPos, value, lineNum, colNum));
									colNum = colNum + (position - startPos);
									state = States.START;
								}
							}
							break;

						}
						//need to set things like forbidden words  and true and false here

						default -> {
							//	System.out.println("test");
							String value = inputString.substring(startPos, position);
							if (reservedWords.containsKey(value)) {
								tokenArr.add(new Token(reservedWords.get(value), startPos, position - startPos, value, lineNum, colNum));
								colNum = colNum + (position - startPos);
								state = States.START;
							} else {

								tokenArr.add(new Token(Kind.IDENT, startPos, position - startPos, value, lineNum, colNum));
								colNum = colNum + (position - startPos);
								state = States.START;                                                //check if right
							}

						}


					}
				}
				//TODO: add extra coverage for escape chars in string lit.  in particular doesn't handle newline in string lit.
				case HAVE_STRING_LIT -> {
					if(inputString.length() <= position) {
						tokenArr.add(new Token(Kind.ERROR, startPos, 1, "EOF error in HAVE_STRING_LIT state.", lineNum, colNum));
						state= States.END;
						break;
					}
					switch(ch) {
						case '"' -> {
							if(position - startPos < 0) { // case for "", an empty string literal
								tokenArr.add(new Token(Kind.STRING_LIT, startPos, 0, "", lineNum, colNum));
								colNum = colNum + 2;
								position++;
								state = States.START;
							}
							else {
								String value = inputString.substring(startPos + 1, position);
								tokenArr.add(new Token(Kind.STRING_LIT, startPos + 1, position - (startPos + 1), value, lineNum, colNum));
								position++;
								colNum = colNum + (position - startPos);
								state = States.START;
							}
							break;
						}
						default -> {  //Unsure if we need this in the case of string literals
							position++;
							break;
						}
					}
				}
				case HAVE_COMMENT -> {
					switch(ch) {
						case '\r' -> {
							position++;
							lineNum++;
							colNum = 0;
							state = States.START;
							break;
						}
						case '\n' -> {
							position++;
							lineNum++;
							colNum = 0;
							state = States.START;
							break;
						}
						default -> {  //We don't care about error handling for comments.
							position++;
							colNum++;
							break;
						}
					}
				}
				case IN_NUM -> {
					boolean inNumError = false;
					switch(ch){
						case '0','1','2','3','4','5','6','7','8','9'->{
							// Case of 00, 01, etc., adding 0 found in START.
							// Should be read as INT_LIT INT_LIT
							if(haveZero) {
								tokenArr.add(new Token(Kind.INT_LIT, startPos, 1, String.valueOf(0), lineNum, colNum));
								startPos++;
								colNum++;
								haveZero = false;
							}
							//System.out.println(ch);
							if(inputString.length() <= position){

								String value = inputString.substring(startPos, position);

								try {
									Integer.valueOf(value);
								}
								catch(NumberFormatException e) {
									inNumError = true;
								}
								if(!inNumError) {
									tokenArr.add(new Token(Kind.INT_LIT, startPos, position - startPos, value, lineNum, colNum));
									colNum = colNum + (position - startPos);
									state = States.START;
								}
								else {
									tokenArr.add(new Token(Kind.ERROR, startPos, position - startPos, "Int too large", lineNum, colNum));
									state = States.END;
								}
							}else {

								position++;
							}
						}
						case '.'->{
							boolean isNum = true;
							position++;


							state = States.IS_DOT;
						}
						default ->{
							//System.out.println(inputString.substring(startPos, position));
							String value = inputString.substring(startPos, position);
							try {
								Integer.valueOf(value);
							}
							catch(NumberFormatException e) {
								inNumError = true;
							}
							if(!inNumError) {
								tokenArr.add(new Token(Kind.INT_LIT,startPos, position-startPos, value, lineNum, colNum));
								colNum = colNum + (position - startPos);
								state=States.START;
							}
							else {
								tokenArr.add(new Token(Kind.ERROR, startPos, position - startPos, "Int too large", lineNum, colNum));
								state = States.END;
							}
							//check if right
						}

					}

				}
				case IN_FLOAT -> {
					//TODO: Error checking doesn't work for float.  Makes infinity instead of throwing too large error, lol.
					boolean inFloatError = false;
					switch (ch) {
						case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
							if(inputString.length() <= position){
								//if over end
								String value = inputString.substring(startPos, position);

								try {
									Float.parseFloat(value);
								}
								catch(NumberFormatException e) {
									inFloatError = true;
								}
								if(!inFloatError) {
									tokenArr.add(new Token(Kind.FLOAT_LIT, startPos, position - startPos, value, lineNum, colNum));
									colNum = colNum + (position - startPos);
									state = States.START;
								}
								else {
									tokenArr.add(new Token(Kind.ERROR, startPos, position - startPos, "Float too large", lineNum, colNum));
									state = States.END;
								}
							}else {
								position++;
							}

						}
						default -> {
							String value = inputString.substring(startPos, position);
							try {
								Float.valueOf(value + "f");
							}
							catch(NumberFormatException e) {
								inFloatError = true;
							}
							if(!inFloatError) {
								tokenArr.add(new Token(Kind.FLOAT_LIT, startPos, position - startPos, value, lineNum, colNum));
								colNum = colNum + (position - startPos);
								state = States.START;
							}
							else {
								tokenArr.add(new Token(Kind.ERROR, startPos, position - startPos, "Float too large", lineNum, colNum));
								state = States.END;
							}
						}

					}
				}
				case IS_DOT -> {
					switch (ch){
						case '0','1','2','3','4','5','6','7','8','9'->{

							state=States.IN_FLOAT;

						}
						default->{
							tokenArr.add(new Token(Kind.ERROR, startPos, position - startPos, "No Value after decimal", lineNum, colNum));
							state = States.END;
						}






					}


				}

				case HAVE_MINUS -> {
					switch (ch){
						case '>'->{
							tokenArr.add(new Token(Kind.RARROW, startPos, 2, inputString.substring(startPos, position), lineNum, colNum));
							position++;
							colNum = colNum + 2;
							state=States.START;
							break;
						}
						default -> {
							tokenArr.add(new Token(Kind.MINUS, startPos, 1, String.valueOf(ch), lineNum, colNum));
							colNum++;
							state= States.START;
						}

					}
				}

				case HAVE_EQ -> {
//					switch(ch){
//						case'='->{
//							tokenArr.add(new Token(Kind.EQUALS, startPos, 2, inputString.substring(startPos, position), lineNum, colNum));
//							state= state.START;
//							position++;
//							colNum = colNum + 2;
//							break;
//						}
////						default -> {
////							throw new IllegalStateException("Lexer bug (HAVE_EQUAL)");
////						}
//						default -> {
//							tokenArr.add(new Token(Kind.ERROR, startPos, 1, "ERROR in HAVE_EQ state.", lineNum, colNum));
//							state = States.END;
//						}


					if(inputString.length() > position) {
						ch = inputString.charAt(position);  // get current character
					}
					// This would be a string like "HI!"
					else {
						tokenArr.add(new Token(Kind.ASSIGN, startPos, 1, String.valueOf(ch), lineNum, colNum));
						colNum++;
						state = States.START;
						break;
					}
					if(ch == '=') {
						tokenArr.add(new Token(Kind.EQUALS, startPos, 2, inputString.substring(startPos, position), lineNum, colNum));
						//check if input positioning is right
						position++;
						colNum = colNum + 2;
						state = States.START;
					}
					else if(ch != '=') {
						tokenArr.add(new Token(Kind.ASSIGN, startPos, 1, String.valueOf(ch), lineNum, colNum));
						colNum++;
						state = States.START;
						// DONT INCREMENT POSITION HERE!
					}
//					else {
//						throw new LexicalException("Lexer bug (HAVE_BANG)");
//					}
					else {
						tokenArr.add(new Token(Kind.ERROR, startPos, 1, "ERROR in HAVE_EQUALS state.", lineNum, colNum));
						state = States.END;
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
							tokenArr.add(new Token(Kind.LE, startPos, 2, inputString.substring(startPos, position), lineNum, colNum));
							position++;
							colNum = colNum + 2;
							state = States.START;
							break;
						}
						case '<'->{
							tokenArr.add(new Token(Kind.LANGLE, startPos, 2, inputString.substring(startPos, position), lineNum, colNum));
							position++;
							colNum = colNum + 2;
							state = States.START;
							break;
						}
						case '-'->{
							tokenArr.add(new Token(Kind.LARROW, startPos, 2, inputString.substring(startPos, position), lineNum, colNum));
							position++;
							colNum = colNum + 2;
							state = States.START;
							break;

						}
						default -> {
							tokenArr.add(new Token(Kind.LT, startPos, 1, String.valueOf(ch), lineNum, colNum));
							colNum++;
							state = States.START;

						}
					}
				}
				case HAVE_GREAT ->{
					switch(ch){
						case'='-> {
							tokenArr.add(new Token(Kind.GE, startPos, 2, inputString.substring(startPos, position), lineNum, colNum));
							state = States.START;
							position++;
							colNum = colNum + 2;
							break;
						}
						case '>'->{
							tokenArr.add(new Token(Kind.RANGLE, startPos, 2, inputString.substring(startPos, position), lineNum, colNum));
							state = States.START;
							position++;
							colNum = colNum + 2;
							break;
						}
						default -> {
							tokenArr.add(new Token(Kind.GT, startPos, 1, String.valueOf(ch), lineNum, colNum));
							colNum++;
							state = States.START;

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
						tokenArr.add(new Token(Kind.BANG, startPos, 1, String.valueOf(ch), lineNum, colNum));
						colNum++;
						state = States.START;
						break;
					}
					if(ch == '=') {
						tokenArr.add(new Token(Kind.NOT_EQUALS, startPos, 2, inputString.substring(startPos, position), lineNum, colNum));
						//check if input positioning is right
						position++;
						colNum = colNum + 2;
						state = States.START;
					}
					else if(ch != '=') {
						tokenArr.add(new Token(Kind.BANG, startPos, 1, String.valueOf(ch), lineNum, colNum));
						colNum++;
						state = States.START;
						// DONT INCREMENT POSITION HERE!
					}
//					else {
//						throw new LexicalException("Lexer bug (HAVE_BANG)");
//					}
					else {
						tokenArr.add(new Token(Kind.ERROR, startPos, 1, "ERROR in HAVE_BANG state.", lineNum, colNum));
						state = States.END;
					}
				}

				case END -> {
					tokenArr.add(new Token(Kind.EOF, startPos, 1, String.valueOf('h'), lineNum, colNum));
					break;
				}


//				default -> throw new LexicalException("Unknow lexer bug.  I am the default case of the main switch statement.");
				default -> {
					tokenArr.add(new Token(Kind.ERROR, startPos, 1, "ERROR in main switch statemnt.", lineNum, colNum));
					state = States.END;
				}
			}
			if(state == States.END) {
				break;
			}

		}
	}

	// Note that returning interface allows a function to return anything that implements
	// that interface.

	// Note I think that this
	@Override
	public IToken next() throws LexicalException {


		// Find return token
		if(arrListIndex < tokenArr.size()) {
			IToken returnToken = tokenArr.get(arrListIndex);
			if(returnToken.getKind() == Kind.ERROR) {
				throw new LexicalException(returnToken.getStringValue(), returnToken.getSourceLocation());
			}
			arrListIndex++;
			return returnToken;
		}
		else {
			throw new LexicalException("Attempted to access out of bounds.");
		}


	}


	@Override
	public IToken peek() throws LexicalException {
		if(arrListIndex < tokenArr.size()) {
			IToken returnToken = tokenArr.get(arrListIndex);
			if(returnToken.getKind() == Kind.ERROR) {
				throw new LexicalException(returnToken.getStringValue(), returnToken.getSourceLocation());
			}
			return returnToken;
		}
		else {
			throw new LexicalException("Attempted to access out of bounds.");
		}
//		throw new LexicalException("Peek not implemented yet.");
	}

}