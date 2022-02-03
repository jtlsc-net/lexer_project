package edu.ufl.cise.plc.test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import edu.ufl.cise.plc.CompilerComponentFactory;
import edu.ufl.cise.plc.ILexer;
import edu.ufl.cise.plc.IToken;
import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.LexicalException;


public class LexerTests {

	ILexer getLexer(String input){
		return CompilerComponentFactory.getLexer(input);
	}

	//makes it easy to turn output on and off (and less typing than System.out.println)
	static final boolean VERBOSE = true;
	void show(Object obj) {
		if(VERBOSE) {
			System.out.println(obj);
		}
	}

	//check that this token has the expected kind
	void checkToken(IToken t, Kind expectedKind) {
		assertEquals(expectedKind, t.getKind());
	}

	//check that the token has the expected kind and position
	void checkToken(IToken t, Kind expectedKind, int expectedLine, int expectedColumn){
		assertEquals(expectedKind, t.getKind());
		assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());
	}

	//check that this token is an IDENT and has the expected name
	void checkIdent(IToken t, String expectedName){
		assertEquals(Kind.IDENT, t.getKind());
		assertEquals(expectedName, t.getText());
	}

	//check that this token is an IDENT, has the expected name, and has the expected position
	void checkIdent(IToken t, String expectedName, int expectedLine, int expectedColumn){
		checkIdent(t,expectedName);
		assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());
	}

	//check that this token is an INT_LIT with expected int value
	void checkInt(IToken t, int expectedValue) {
		assertEquals(Kind.INT_LIT, t.getKind());
		assertEquals(expectedValue, t.getIntValue());
	}

	//check that this token  is an INT_LIT with expected int value and position
	void checkInt(IToken t, int expectedValue, int expectedLine, int expectedColumn) {
		checkInt(t,expectedValue);
		assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());
	}

	//check that this token is the EOF token
	void checkEOF(IToken t) {
		checkToken(t, Kind.EOF);
	}


	//The lexer should add an EOF token to the end.
	@Test
	void testEmpty() throws LexicalException {
		String input = "";
		show(input);
		ILexer lexer = getLexer(input);
		checkEOF(lexer.next());
	}
	
	@Test
	// Checking for correct 0 return.  Should not return EOF.
	//00 should be read as INT_LIT INT_LIT (it is technically not illegal with our parser.
	void testZero() throws LexicalException {
		String input = """
				0
				00
				010
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.INT_LIT,0,0);
		checkToken(lexer.next(), Kind.INT_LIT,1,0);
		checkToken(lexer.next(), Kind.INT_LIT,1,1);
		checkToken(lexer.next(), Kind.INT_LIT,2,0);
		checkToken(lexer.next(), Kind.INT_LIT,2,1);
	}

	//Just a plus.
	@Test
	void testSinglePlus() throws LexicalException {
		String input = "+";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.PLUS);
		checkEOF(lexer.next());
	}

	//Every symbol currently implemented that is defined under <token> (see lexical structure)
	@Test
	void testAllSymbols() throws LexicalException {
		String input = "+;|%^&,/()[]-*!=!";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.PLUS);
		checkToken(lexer.next(), Kind.SEMI);
		checkToken(lexer.next(), Kind.OR);
		checkToken(lexer.next(), Kind.MOD);
		checkToken(lexer.next(), Kind.RETURN);
		checkToken(lexer.next(), Kind.AND);
		checkToken(lexer.next(), Kind.COMMA);
		checkToken(lexer.next(), Kind.DIV);
		checkToken(lexer.next(), Kind.LPAREN);
		checkToken(lexer.next(), Kind.RPAREN);
		checkToken(lexer.next(), Kind.LSQUARE);
		checkToken(lexer.next(), Kind.RSQUARE);
		checkToken(lexer.next(), Kind.MINUS);
		checkToken(lexer.next(), Kind.TIMES);
		checkToken(lexer.next(), Kind.NOT_EQUALS);
		checkToken(lexer.next(), Kind.BANG);
		checkEOF(lexer.next());
	}

	@Test
		// Bang/not equals
	void testBang() throws LexicalException {
		String input = "!=!+!";  //TODO: add !Sammy and Sam!Sam when ident is added.
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.NOT_EQUALS);
		checkToken(lexer.next(), Kind.BANG);
		checkToken(lexer.next(), Kind.PLUS);
		checkToken(lexer.next(), Kind.BANG);
		checkEOF(lexer.next());
	}

	//A couple of single character tokens
	@Test
	void testSingleChar0() throws LexicalException {
		String input = """
				+ 
				- 	 
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.PLUS, 0,0);
		checkToken(lexer.next(), Kind.MINUS, 1,0);
		checkEOF(lexer.next());
	}

	//comments should be skipped
	@Test
	void testComment0() throws LexicalException {
		//Note that the quotes around "This is a string" are passed to the lexer.
		String input = """
				"This is a string"
				#this is a comment
				*
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.STRING_LIT, 0,0);
		checkToken(lexer.next(), Kind.TIMES, 2,0);
		checkEOF(lexer.next());
	}

	//Example for testing input with an illegal character
	@Test
	void testError0() throws LexicalException {
		String input = """
				abc
				@
				""";
		show(input);
		ILexer lexer = getLexer(input);
		//this check should succeed
		checkIdent(lexer.next(), "abc");
		//this is expected to throw an exception since @ is not a legal
		//character unless it is part of a string or comment
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
	}
	

	//Several identifiers to test positions
	@Test
	public void testIdent0() throws LexicalException {
		String input = """
				abc
				  def
				     ghi

				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "abc", 0,0);
		checkIdent(lexer.next(), "def", 1,2);
		checkIdent(lexer.next(), "ghi", 2,5);
		checkEOF(lexer.next());
	}


	@Test
	public void testEquals0() throws LexicalException {
		String input = """
				= == ===
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(),Kind.ASSIGN,0,0);
		checkToken(lexer.next(),Kind.EQUALS,0,2);
		checkToken(lexer.next(),Kind.EQUALS,0,5);
		checkToken(lexer.next(),Kind.ASSIGN,0,7);
		checkEOF(lexer.next());
	}

	@Test
	public void testIdenInt() throws LexicalException {
		String input = """
				a123 456b
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "a123", 0,0);
		checkInt(lexer.next(), 456, 0,5);
		checkIdent(lexer.next(), "b",0,8);
		checkEOF(lexer.next());
	}


	//example showing how to handle number that are too big.
	@Test
	public void testIntTooBig() throws LexicalException {
		String input = """
				42
				99999999999999999999999999999999999999999999999999999999999999999999999
				""";
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(),42);
		Exception e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}
	@Test
	public void testNumber() throws LexicalException {
		String input = "123456";
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(),123456);

	}
	@Test
	public void testVoidName() throws LexicalException {
		String input = "void+";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.KW_VOID);
		checkToken(lexer.next(), Kind.PLUS);
	}
	
	@Test
	//TODO update with better example.
	public void testSlashN() throws LexicalException {
		String input = "\n+";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.PLUS, 1,0);
	}
	
	@Test
	//Test for \n in middle of string.  Should make 2 tokens.
	public void testSlashNString() throws LexicalException {
		String input = "hi\nbye";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT,0,0);
		checkToken(lexer.next(), Kind.IDENT,1,0);
	}
	
	@Test
	//Test for reserved word in larger string.  Should return normal indent, not reserved word.
	public void testNotReserved() throws LexicalException {
		String input = "stringVar";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT,0,0);
	}
	
	@Test
	//Test for rarrow and minus.
	public void testHaveMinus() throws LexicalException {
		String input = "-->-";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.MINUS,0,0);
		checkToken(lexer.next(), Kind.RARROW,0,1);
		checkToken(lexer.next(), Kind.MINUS,0,3);
	}


	// Test for \\t problem mentioned in class.
	// Expected output: hi\twhat
	// Wrong output: hi	what  (actual tab)
	// TODO implement checkString method.
	/*
	public void testEscapeChar() throws LexicalException {
		String input = "hi\\twhat";
		ILexer lexer = getLexer(input);

	}
	*/



}
