package edu.ufl.cise.plc;

import com.sun.source.tree.Tree;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.*;
import jdk.jshell.Snippet;

import java.text.ParseException;
import java.util.*;
//TODO make parse function, make the error functions, check for errors for the functions that don't check for them yet
public class Parser implements IParser {
    private ArrayList<IToken> tokens = new ArrayList<IToken>();
    private IToken t;
    int curr = 0;
    private ASTNode newNode;

    //change if needed
    public Parser(String input) throws PLCException {
        ILexer lexer = new Lexer(input);
        IToken tor = lexer.next();
        while(tor.getKind() != Kind.EOF) {
        	tokens.add(tor);
        	tor = lexer.next();
        }
        tokens.add(tor);
        t = tokens.get(curr);
    }


    //todo to fix this, needs to be public
    public ASTNode parse() throws PLCException{
        Expr ast = expr();
        System.out.println(ast);

        return ast;

    }

    //TODO, improve code namely maybe have predict sets? Either way, just incorporate errors
    //TODO also if we have problem with >= == and | &, we can split the expr function according to discord
    public Expr expr() throws PLCException{
        IToken firstToken = t;
        Expr e  = null;
        if(isKind(IToken.Kind.KW_IF)){
            e = ConditionalExpr();
        }
        else{
            e = LogicalAndExpr();

        }


        return e;
    }

    //TODO expr not done
    public Expr ConditionalExpr() throws PLCException{
        IToken firstToken = tokens.get(0);
        Expr e  = null;
        Expr e2 = null;
        Expr e3 = null;
        if(isKind(IToken.Kind.KW_IF)) {
            match(IToken.Kind.KW_IF);
            match(IToken.Kind.LPAREN);
            e = expr();
            match(IToken.Kind.RPAREN);
            e2 = expr();
            match(IToken.Kind.KW_ELSE);
            e3 = expr();
            match(IToken.Kind.KW_FI);
            e = new ConditionalExpr(firstToken, e, e2, e3);
        }else{
            throw new SyntaxException("Error: invalid conditional expression.");
        }
        return e;
    }
    //TODO
    Expr LogicalOrExpr() throws PLCException{
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;

        left =  LogicalAndExpr();
        while (isKind(IToken.Kind.OR)){
            IToken op = t;
            consume(IToken.Kind.OR,"");    //what to put in message?
            right = LogicalAndExpr();
            left = new BinaryExpr(firstToken,left,op,right);
        }
        return left;

    }


    Expr LogicalAndExpr() throws PLCException{
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = ComparisonExpr();
        while (isKind(IToken.Kind.AND)){

            IToken op = t;

            consume(IToken.Kind.AND,"");    //what to put in message?
            right = ComparisonExpr ();
            left = new BinaryExpr(firstToken,left,op,right);
        }
        return left;
    }
    Expr ComparisonExpr () throws PLCException{
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left= AdditiveExpr();
        while (isKind(IToken.Kind.GE, IToken.Kind.LE, IToken.Kind.EQUALS, IToken.Kind.NOT_EQUALS, IToken.Kind.GT, IToken.Kind.LT)){
            IToken op = t;
            if(isKind(IToken.Kind.GE)) {
                consume(IToken.Kind.GE,"");    //what to put in message?
            }
            if(isKind(IToken.Kind.EQUALS)) {
                consume(IToken.Kind.EQUALS,"");    //what to put in message?
            }
            if(isKind(IToken.Kind.NOT_EQUALS)) {
                consume(IToken.Kind.NOT_EQUALS,"");    //what to put in message?
            }
            if(isKind(IToken.Kind.LT)) {
                consume(IToken.Kind.LT,"");    //what to put in message?
            }
            if(isKind(IToken.Kind.GT)) {
                consume(IToken.Kind.GT,"");    //what to put in message?
            }

            else{//else if or else
                consume(IToken.Kind.LE,"");
            }
            right= AdditiveExpr ();
            left = new BinaryExpr(firstToken,left,op,right);
        }
        return left;



    }
    Expr AdditiveExpr () throws PLCException{

        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left= MultiplicativeExpr();
        while (isKind(IToken.Kind.PLUS, IToken.Kind.MINUS)){
            IToken op = t;
            if(isKind(IToken.Kind.PLUS)) {
                consume(IToken.Kind.PLUS,"");    //what to put in message?
            }
            else{//else if or else
                consume(IToken.Kind.MINUS,"");
            }
            right= MultiplicativeExpr();
            left = new BinaryExpr(firstToken,left,op,right);
        }
        return left;
    }

    //TODO
    Expr MultiplicativeExpr() throws PLCException{
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = UnaryExpr();
        while (isKind(IToken.Kind.TIMES, IToken.Kind.DIV)){
            IToken op = t;
            if(isKind(IToken.Kind.TIMES)) {
                consume(IToken.Kind.TIMES,"");    //what to put in message?
            }
            else{//else if or else
                consume(IToken.Kind.DIV,"");
            }
            right = UnaryExpr();
            left = new BinaryExpr(firstToken,left,op,right);

        }
        return left;
    }
    public Expr UnaryExpr() throws PLCException{
        IToken firstToken = t;
        Expr e = null;
        Expr e1= null;

        if(isKind(IToken.Kind.BANG, IToken.Kind.MINUS, IToken.Kind.COLOR_CONST, IToken.Kind.IMAGE_OP)){
            IToken op = t;
            if(isKind(IToken.Kind.COLOR_CONST)) {
                consume(IToken.Kind.COLOR_CONST,"");    //what to put in message?
            }
            if(isKind(IToken.Kind.IMAGE_OP)) {
                consume(IToken.Kind.IMAGE_OP,"");    //what to put in message?
            }
            if(isKind(IToken.Kind.BANG)) {
                consume(IToken.Kind.BANG,"");    //what to put in message?
            }
            else{//else if or else
                consume(IToken.Kind.MINUS,"");
            }
            e = UnaryExpr();
            e1 = new UnaryExpr(firstToken, op,  e);
            return e1;
        }
        else{
            e = UnaryExprPostfix();
            return e;
        }




    }
    public Expr UnaryExprPostfix() throws PLCException {
        IToken firstToken = t;
        Expr e = null;
        PixelSelector p = null;

        e = PrimaryExpr();


        //TODO   figure out ? 0 or 1 code
        if (isKind(IToken.Kind.LSQUARE)) {
            p = PixelSelector();
        }
        e = new UnaryExprPostfix(firstToken, e, p );

        return e;
    }
    Expr PrimaryExpr() throws PLCException{
        IToken firstToken = t;
        Expr e = null;

        if(isKind(IToken.Kind.BOOLEAN_LIT)){
            e = new BooleanLitExpr(firstToken);
            consume(IToken.Kind.BOOLEAN_LIT,"");

        }
        else if(isKind(IToken.Kind.STRING_LIT)){
            e = new StringLitExpr(firstToken);
            consume(IToken.Kind.STRING_LIT,"");
        }
        else if(isKind(IToken.Kind.INT_LIT)){
            e = new IntLitExpr(firstToken);
            consume(IToken.Kind.INT_LIT, "int lit error");
        }
        else if(isKind(IToken.Kind.FLOAT_LIT)){
            e = new FloatLitExpr(firstToken);
            consume(IToken.Kind.FLOAT_LIT, "");
        }
        else if(isKind(IToken.Kind.IDENT)){
            e = new IdentExpr(firstToken);
            consume(IToken.Kind.IDENT, "");
        }
        else if(isKind(IToken.Kind.LPAREN)){
            consume(IToken.Kind.LPAREN,"");
            e= expr();
            match(IToken.Kind.RPAREN);
        }
        else{
            throw new SyntaxException("Error in PrimaryExpr");
        }
        return e;

    }
    PixelSelector PixelSelector() throws PLCException {

        IToken firstToken = t;
        Expr x = null;
        Expr y = null;
        PixelSelector ast;
        if(isKind(IToken.Kind.LSQUARE)){
            consume(IToken.Kind.LSQUARE, "Expected left param");
            x = expr();
            match();
            y = expr();
            consume(IToken.Kind.RSQUARE, "Expected right param");
            //TODO, check this
            ast = new PixelSelector(firstToken, x, y);
        }
        else{
            throw new SyntaxException("Error in PixelSelector.");
        }
        return ast;
    }




    //basically copy pasted from the book, which i think is allowed, but check later if this type of stuff needs
//changed
    private boolean match(IToken.Kind... kind) {
        for (IToken.Kind k : kind) {
            if (check(k)) {
                advance();
                return true;
            }
        }

        return false;
    }
    private boolean check(IToken.Kind kind) {
        if (isAtEnd()) return false;
        return peek().getKind() == kind;
    }
    private IToken advance() {
        if (!isAtEnd()) curr++;
        return previous();
    }
    private boolean isAtEnd() {
        return peek().getKind() == IToken.Kind.EOF;
    }
    //TODO does this overlap with peek in the lexer class. Prob change this
    private IToken peek() {
        return tokens.get(curr);
    }
    private IToken previous() {
        return tokens.get(curr - 1);
    }

    // TODO check consume and match if which one we need to use
    private IToken consume(IToken.Kind kind, String message) throws SyntaxException {
        if (check(kind)) return advance();

//        throw error(peek(), message);
        throw new SyntaxException("Unknown error");
    }
//TODO change this

    static void error(Token token, String message) throws SyntaxException {

        //TODO is there suppose to be EOF error? and when
        if (token.getKind() == IToken.Kind.EOF) {
            throw new SyntaxException("end", token.getSourceLocation());
        } else {
            throw new SyntaxException(message, token.getSourceLocation());
        }
    }
    protected boolean isKind(IToken.Kind kind) {
        return t.getKind() == kind;
    }
    protected boolean isKind(IToken.Kind... kinds) {
        for (IToken.Kind k : kinds) {
            if (k == t.getKind())
                return true;
        }
        return false;
    }









}