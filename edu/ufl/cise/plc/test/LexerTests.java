package edu.ufl.cise.plc.test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import edu.ufl.cise.plc.CompilerComponentFactory;
import edu.ufl.cise.plc.ILexer;
import edu.ufl.cise.plc.IToken;
import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.LexicalException;

import java.util.Arrays;

// Credit to Brando Mora for sharing several tests on Slack.

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

	String getASCII(String s) {
		int[] ascii = new int[s.length()];
		for (int i = 0; i != s.length(); i++) {
			ascii[i] = s.charAt(i);
		}
		return Arrays.toString(ascii);
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

	//check that the token has the expected kind and position and text
	void checkToken(IToken t, Kind expectedKind, int expectedLine, int expectedColumn, String expectedText){
		assertEquals(expectedKind, t.getKind());
		assertEquals(expectedText, t.getText());
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

	void checkFloat(IToken t, float expectedValue) {
		assertEquals(Kind.FLOAT_LIT, t.getKind());
		assertEquals(expectedValue, t.getFloatValue());
	}

	void checkFloat(IToken t, float expectedValue, int expectedLine, int expectedColumn) {
		checkFloat(t, expectedValue);
		assertEquals(new IToken.SourceLocation(expectedLine, expectedColumn), t.getSourceLocation());
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
	void testError1() throws LexicalException {
		String input = """
	            abc
	            00.4
	            123
	            _Name1
	            _1@
	            """;
		show(input);
		ILexer lexer = getLexer(input);
		//these checks should succeed
		checkIdent(lexer.next(), "abc");
		checkInt(lexer.next(), 0, 1,0);
//	    checkToken(lexer.peek(), Kind.FLOAT_LIT, 1, 1);
		checkToken(lexer.next(), Kind.FLOAT_LIT, 1, 1);
		checkToken(lexer.next(), Kind.INT_LIT, 2,0);
		checkIdent(lexer.next(), "_Name1", 3, 0);
		checkIdent(lexer.next(), "_1", 4, 0);
		//this is expected to throw an exception since @ is not a legal
		//character unless it is part of a string or comment
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});

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
		checkEOF(lexer.next());
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

	// Simple test for boolean
	@Test
	public void hasBool() throws LexicalException {
		String input = "true";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.BOOLEAN_LIT, 0,0);
		checkEOF(lexer.next());
	}

	//Every symbol currently implemented that is defined under <token> (see lexical structure)
	@Test
	void testAllSymbols() throws LexicalException {
		String input = """
	            &
	            |
	            /
	            *
	            +
	            (
	            )
	            [
	            ]
	            !=
	            ==
	            >=
	            <=
	            >>
	            <<
	            <-
	            ->
	            %
	            ^
	            ,
	            ;
	            !
	            =
	            -
	            <
	            >     
	            """;
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.AND,        0, 0);
		checkToken(lexer.next(), Kind.OR,        1, 0);
		checkToken(lexer.next(), Kind.DIV,        2, 0);
		checkToken(lexer.next(), Kind.TIMES,    3, 0);
		checkToken(lexer.next(), Kind.PLUS,        4, 0);
		checkToken(lexer.next(), Kind.LPAREN,    5, 0);
		checkToken(lexer.next(), Kind.RPAREN,    6, 0);
		checkToken(lexer.next(), Kind.LSQUARE,    7, 0);
		checkToken(lexer.next(), Kind.RSQUARE,    8, 0);
		checkToken(lexer.next(), Kind.NOT_EQUALS,    9, 0);
		checkToken(lexer.next(), Kind.EQUALS,        10, 0);
		checkToken(lexer.next(), Kind.GE,         11, 0);
		checkToken(lexer.next(), Kind.LE,         12, 0);
		checkToken(lexer.next(), Kind.RANGLE,     13, 0);
		checkToken(lexer.next(), Kind.LANGLE,     14, 0);
		checkToken(lexer.next(), Kind.LARROW,     15, 0);
		checkToken(lexer.next(), Kind.RARROW,     16, 0);
		checkToken(lexer.next(), Kind.MOD,        17, 0);
		checkToken(lexer.next(), Kind.RETURN,     18, 0);
		checkToken(lexer.next(), Kind.COMMA,      19, 0);
		checkToken(lexer.next(), Kind.SEMI,       20, 0);
		checkToken(lexer.next(), Kind.BANG,       21, 0);
		checkToken(lexer.next(), Kind.ASSIGN,     22, 0);
		checkToken(lexer.next(), Kind.MINUS,      23, 0);
		checkToken(lexer.next(), Kind.LT,        24, 0);
		checkToken(lexer.next(), Kind.GT,        25, 0);
		checkEOF(lexer.next());

	}

	@Test
	void testMany() throws LexicalException {
		String input = """
	            [
	            int a = 28.3 * 55.597;
	            string _b1 = "testing \\nstring";
	            boolean c$5 = true;
	            ]
	            """;
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.LSQUARE, 0,0);
		checkToken(lexer.next(), Kind.TYPE, 1,0, "int");
		checkToken(lexer.next(), Kind.IDENT, 1,4, "a");
		checkToken(lexer.next(), Kind.ASSIGN, 1,6);
		checkToken(lexer.next(), Kind.FLOAT_LIT, 1, 8, "28.3");
		checkToken(lexer.next(), Kind.TIMES, 1,13);
		checkToken(lexer.next(), Kind.FLOAT_LIT, 1, 15, "55.597");
		checkToken(lexer.next(), Kind.SEMI, 1,21);
		checkToken(lexer.next(), Kind.TYPE, 2,0, "string");
		checkToken(lexer.next(), Kind.IDENT, 2,7, "_b1");
		checkToken(lexer.next(), Kind.ASSIGN, 2,11);
		IToken t = lexer.next();
		String val = t.getStringValue();
		show("getStringValueChars=     " + getASCII(val));
		String expectedStringValue = "testing \nstring";
		show("expectedStringValueChars=" + getASCII(expectedStringValue));
		assertEquals(expectedStringValue, val);
		String text = t.getText();
		show("getTextChars=     " +getASCII(text));
		String expectedText = "\"testing \\nstring\"";
		show("expectedTextChars="+getASCII(expectedText));
		assertEquals(expectedText,text);
		checkToken(lexer.next(), Kind.SEMI, 2,31);
		checkToken(lexer.next(), Kind.TYPE, 3,0, "boolean");
		checkToken(lexer.next(), Kind.IDENT, 3,8, "c$5");
		checkToken(lexer.next(), Kind.ASSIGN, 3,12);
		checkToken(lexer.next(), Kind.BOOLEAN_LIT, 3,14, "true");
		checkToken(lexer.next(), Kind.SEMI, 3,18);
		checkToken(lexer.next(), Kind.RSQUARE, 4,0);
		checkEOF(lexer.next());
	}

	@Test
	public void BasicStringLit() throws LexicalException {
		String input = """
				"hi"
				""";
		show(input);
		ILexer lexer = getLexer(input);
		IToken t = lexer.next();
		String text = t.getText();
		show("getText = " + getASCII(text));
		String expectedText = "\"hi\"";
		show("expectedText = " + getASCII(expectedText));
		assertEquals(expectedText, text);
		String val = t.getStringValue();
		String expectedString = "hi";
		show("getStringValue = " + getASCII(val));
		show("expectedStringValue = " + getASCII(expectedString));
		assertEquals(expectedString, val);
		checkEOF(lexer.next());
	}

	// trying all the single character tokens which aren't the start of multicharacter tokens
	@Test
	void testReservedWords() throws LexicalException {
		String input = """
	            string CYAN
	            int
	            float
	            boolean
	            color
	            image
	            void
	            getWidth
	            getHeight
	            getRed
	            getGreen
	            getBlue
	            BLACK
	            BLUE
	            CYAN
	            DARK_GRAY
	            GRAY
	            GREEN
	            LIGHT_GRAY
	            MAGENTA
	            ORANGE
	            PINK
	            RED
	            WHITE
	            YELLOW
	            true
	            false
	            if
	            else
	            fi
	            write
	            console     
	            """;
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.TYPE,            0, 0, "string");
		checkToken(lexer.next(), Kind.COLOR_CONST,    0, 7, "CYAN");
		checkToken(lexer.next(), Kind.TYPE,            1, 0, "int");
		checkToken(lexer.next(), Kind.TYPE,            2, 0, "float");
		checkToken(lexer.next(), Kind.TYPE,            3, 0, "boolean");
		checkToken(lexer.next(), Kind.TYPE,            4, 0, "color");
		checkToken(lexer.next(), Kind.TYPE,            5, 0, "image");
		checkToken(lexer.next(), Kind.KW_VOID,        6, 0, "void");
		checkToken(lexer.next(), Kind.IMAGE_OP,        7, 0, "getWidth");
		checkToken(lexer.next(), Kind.IMAGE_OP,        8, 0, "getHeight");
		checkToken(lexer.next(), Kind.COLOR_OP,        9, 0, "getRed");
		checkToken(lexer.next(), Kind.COLOR_OP,        10, 0, "getGreen");
		checkToken(lexer.next(), Kind.COLOR_OP,        11, 0, "getBlue");
		checkToken(lexer.next(), Kind.COLOR_CONST,    12, 0, "BLACK");
		checkToken(lexer.next(), Kind.COLOR_CONST,    13, 0, "BLUE");
		checkToken(lexer.next(), Kind.COLOR_CONST,    14, 0, "CYAN");
		checkToken(lexer.next(), Kind.COLOR_CONST,    15, 0, "DARK_GRAY");
		checkToken(lexer.next(), Kind.COLOR_CONST,    16, 0, "GRAY");
		checkToken(lexer.next(), Kind.COLOR_CONST,    17, 0, "GREEN");
		checkToken(lexer.next(), Kind.COLOR_CONST,    18, 0, "LIGHT_GRAY");
		checkToken(lexer.next(), Kind.COLOR_CONST,    19, 0, "MAGENTA");
		checkToken(lexer.next(), Kind.COLOR_CONST,    20, 0, "ORANGE");
		checkToken(lexer.next(), Kind.COLOR_CONST,    21, 0, "PINK");
		checkToken(lexer.next(), Kind.COLOR_CONST,    22, 0, "RED");
		checkToken(lexer.next(), Kind.COLOR_CONST,    23, 0, "WHITE");
		checkToken(lexer.next(), Kind.COLOR_CONST,    24, 0, "YELLOW");
		checkToken(lexer.next(), Kind.BOOLEAN_LIT,    25, 0, "true");
		checkToken(lexer.next(), Kind.BOOLEAN_LIT,    26, 0, "false");
		checkToken(lexer.next(), Kind.KW_IF,        27, 0, "if");
		checkToken(lexer.next(), Kind.KW_ELSE,        28, 0, "else");
		checkToken(lexer.next(), Kind.KW_FI,        29, 0, "fi");
		checkToken(lexer.next(), Kind.KW_WRITE,        30, 0, "write");
		checkToken(lexer.next(), Kind.KW_CONSOLE,    31, 0, "console");
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

	@Test
	public void testBangSlack() throws LexicalException{
		String input = """
	        !=
	        !!
	        !=!
	        !!=>>>=<-<<<
	        """;
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.NOT_EQUALS, 0, 0);
		checkToken(lexer.next(), Kind.BANG, 1, 0);
		checkToken(lexer.next(), Kind.BANG, 1, 1);
		checkToken(lexer.next(), Kind.NOT_EQUALS, 2, 0);
		checkToken(lexer.next(), Kind.BANG, 2, 2);
		checkToken(lexer.next(), Kind.BANG, 3, 0 );
		checkToken(lexer.next(), Kind.NOT_EQUALS, 3, 1);
		checkToken(lexer.next(), Kind.RANGLE, 3, 3);
		checkToken(lexer.next(), Kind.GE, 3, 5);
		checkToken(lexer.next(), Kind.LARROW, 3,7);
		checkToken(lexer.next(), Kind.LANGLE, 3, 9);
		checkToken(lexer.next(), Kind.LT, 3, 11);
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
		checkToken(lexer.next(), Kind.STRING_LIT, 0,0, "\"This is a string\"");
		checkToken(lexer.next(), Kind.TIMES, 2,0);
		checkEOF(lexer.next());
	}

	@Test
	public void testComment1() throws LexicalException {
		String input = "#this is a comment \\b\n*";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.TIMES, 1,0);
		checkEOF(lexer.next());
	}

	@Test
	public void testEmptyStringLit() throws LexicalException {
		String input = """
				""*
				""";
		show(input);
		ILexer lexer = getLexer(input);
		IToken t = lexer.next();
		String val = t.getStringValue();
		show("getStringValueChars=     " + getASCII(val));
		String expectedStringValue = "";
		show("expectedStringValueChars=" + getASCII(expectedStringValue));
		assertEquals(expectedStringValue, val);
		String text = t.getText();
		show("getTextChars=     " +getASCII(text));
		String expectedText = "\"\""; //almost the same as input, but white space is omitted
		show("expectedTextChars="+getASCII(expectedText));
		assertEquals(expectedText,text);
		checkToken(lexer.next(), Kind.TIMES, 0,2);
		checkEOF(lexer.next());
	}

	@Test
	//Checking for proper backspace + general parsing.
	//TODO: add check for proper text (see escape sequence tests)
	public void testStringLit1() throws LexicalException {
		String input = "\"hen\\blo\"";
		show(input);
		ILexer lexer = getLexer(input);
		IToken t = lexer.next();
		String value = t.getStringValue();
		show("getStringValueChars=    " + getASCII(value));
		String expectedStringValue = "hen\blo";
		show("expectedStringValueChars=    " + getASCII(expectedStringValue));
		assertEquals(expectedStringValue, value);
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

	@Test
	public void testIdentFloat() throws LexicalException {
		String input = """
				a123 4.56b
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "a123", 0,0);
		checkFloat(lexer.next(), (float)4.56, 0,5);
		checkIdent(lexer.next(), "b", 0,9);
		checkEOF(lexer.next());
	}

	// Lexer cannot handle period if part of ident.
	@Test
	public void testPeriod() throws LexicalException {
		String input = "b4.23";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "b4", 0,0);
		assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}

	//Example for testing input with an illegal character
	@Test
	void testIntFloatError() throws LexicalException {
		String input = """
	            0.32
	            00.15
	            10.030.32
	            """;
		show(input);
		ILexer lexer = getLexer(input);
		checkFloat(lexer.next(), (float) 0.32,    0, 0);
		checkInt(lexer.next(), 0,             1, 0);
		checkFloat(lexer.next(), (float) 0.15,    1, 1);
		checkFloat(lexer.next(), (float) 10.030,    2, 0);
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
	}

	@Test
	//Test for no digits after . in float.
	public void testNoDecimals() throws LexicalException {
		String input = "1.";
		show(input);
		ILexer lexer = getLexer(input);
		assertThrows(LexicalException.class, () ->{
			lexer.next();
		});
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
		assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}

	@Test
	// Float too big
	public void testFloatTooBig() throws LexicalException {
		String input = """
				4.2
				99999999999999999999999999999999999999999999999999999999999999.9999999999999999999
				""";
		ILexer lexer = getLexer(input);
		checkFloat(lexer.next(),(float)4.2,0,0);
		assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}

	@Test
	public void testNumber() throws LexicalException {
		String input = "123456";
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(),123456);
		checkEOF(lexer.next());
	}
	@Test
	public void testVoidName() throws LexicalException {
		String input = "void+";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.KW_VOID);
		checkToken(lexer.next(), Kind.PLUS);
		checkEOF(lexer.next());
	}

	@Test
	//TODO update with better example.
	public void testSlashN() throws LexicalException {
		String input = "\n+";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.PLUS, 1,0);
		checkEOF(lexer.next());
	}

	@Test
	//Test for \n in middle of string.  Should make 2 tokens.
	public void testSlashNString() throws LexicalException {
		String input = "hi\nbye";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT,0,0);
		checkToken(lexer.next(), Kind.IDENT,1,0);
		checkEOF(lexer.next());
	}

	@Test
	//Test for reserved word in larger string.  Should return normal indent, not reserved word.
	public void testNotReserved() throws LexicalException {
		String input = "stringVar";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT,0,0);
		checkEOF(lexer.next());
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
		checkEOF(lexer.next());
	}

	// Test made by professor for escape sequences.
	@Test
	public void testEscapeSequences0() throws LexicalException {
		String input = "\"\\b \\t \\n \\f \\r \"";
		show(input);
		show("input chars= " + getASCII(input));
		ILexer lexer = getLexer(input);
		IToken t = lexer.next();
		String val = t.getStringValue();
		show("getStringValueChars=     " + getASCII(val));
		String expectedStringValue = "\b \t \n \f \r ";
		show("expectedStringValueChars=" + getASCII(expectedStringValue));
		assertEquals(expectedStringValue, val);
		String text = t.getText();
		show("getTextChars=     " +getASCII(text));
		String expectedText = "\"\\b \\t \\n \\f \\r \"";
		show("expectedTextChars="+getASCII(expectedText));
		assertEquals(expectedText,text);
	}

	// Another test made by professor for escape sequences.
	@Test
	public void testEscapeSequences1() throws LexicalException {
		String input = "   \" ...  \\\"  \\\'  \\\\  \"";
		show(input);
		show("input chars= " + getASCII(input));
		ILexer lexer = getLexer(input);
		IToken t = lexer.next();
		String val = t.getStringValue();
		show("getStringValueChars=     " + getASCII(val));
		String expectedStringValue = " ...  \"  \'  \\  ";
		show("expectedStringValueChars=" + getASCII(expectedStringValue));
		assertEquals(expectedStringValue, val);
		String text = t.getText();
		show("getTextChars=     " +getASCII(text));
		String expectedText = "\" ...  \\\"  \\\'  \\\\  \""; //almost the same as input, but white space is omitted
		show("expectedTextChars="+getASCII(expectedText));
		assertEquals(expectedText,text);
	}

	@Test
	public void testStringErrorEOF() throws LexicalException {
		String input = """
	           "good"
	           "test
	   
	            """;
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.STRING_LIT, 0, 0);
		assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
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
