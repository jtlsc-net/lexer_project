package edu.ufl.cise.plc;

import com.sun.source.tree.Tree;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.*;
import jdk.jshell.Snippet;

import javax.naming.Name;
import javax.xml.stream.events.Namespace;
import java.text.ParseException;
import java.util.*;
public class Parser implements IParser {
    private ArrayList<IToken> tokens = new ArrayList<IToken>();
    private IToken t;
    int curr = 0;

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

    public ASTNode parse() throws PLCException{
        Expr ast = expr();

        return ast;

    }


    public Expr Program() throws PLCException{
        Expr e = null;
        Expr e2 = null;
        Statement state= null;
        ASTNode node = null;

        //TOTO change type to something usable
        Types.Type type = null;
        IToken firstToken = t;

        if(isKind(IToken.Kind.TYPE,Kind.KW_VOID)) {
            if (isKind(IToken.Kind.TYPE)) {
                match(Kind.TYPE);

                //TODO I dont know what variable i should do this
                type = t.getKind();
            } else if (isKind(Kind.KW_VOID)) {
                match(Kind.KW_VOID);
                //TODO Change?
                type = void;
            } else {
                throw new SyntaxException("Error: invalid conditional expression.");
            }

            match(Kind.IDENT);

            match(Kind.LPAREN);
            e= NameDef();


            if(isKind(Kind.COMMA)) {
                match(Kind.COMMA);
                e2 = NameDef();
            }

            if(isKind(Kind.TYPE)){
                node = Declaration();
                match(Kind.SEMI);
            }
            else if(isKind(Kind.IDENT, Kind.RETURN, Kind.KW_WRITE)){
                state = Statement();
                match(Kind.SEMI);
            }
            else{
                throw new SyntaxException("Error: invalid Program expression.");

            }


            //Declaration();


        }

        return e;
    }
    public Declaration NameDef() throws PLCException{
        IToken firstToken =t;


        Dimension d= null;
        Declaration dec = null;

        if(isKind(Kind.TYPE)){
            consume(Kind.TYPE, "Expected Type");
            if(isKind(Kind.IDENT)){
                consume(Kind.IDENT, "Expected IDENT");

                //TODO FINISH THIS SENTENCE
                dec = new NameDef(firstToken, firstToken.getText(), firstToken. );
            }
            else if(isKind(Kind.LSQUARE)){
                d = Dimension();
                match(Kind.IDENT);

                //TODO FINISH THIS
                dec = new NameDefWithDim(firstToken, );

            }
        }

        return dec;

    }

    public ASTNode Declaration() throws PLCException{
        IToken firstToken = t;
        Declaration n = null;
        Expr e1= null;
        ASTNode node = null;
        n = NameDef();
        if(isKind(Kind.EQUALS, Kind.LARROW)){
            if(isKind(Kind.EQUALS)){
                consume(Kind.EQUALS, "Expected Equals");
            }
            else{
                consume(Kind.LARROW, "Expected LARROW");
            }
            e1 = expr();
        }
        //TODO fix
        node = new VarDeclaration(firstToken, n,e1);

        return node;
    }
    public Expr expr() throws PLCException{
        Expr e  = null;
        if(isKind(IToken.Kind.KW_IF)){
            e = ConditionalExpr();
        }
        else{

            //TODO wierd cause this was logicalandexp before, so i changed it
            e = LogicalOrExpr();

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

        if(isKind(IToken.Kind.BANG, IToken.Kind.MINUS, IToken.Kind.COLOR_OP, IToken.Kind.IMAGE_OP)){
            IToken op = t;
            if(isKind(IToken.Kind.COLOR_OP)) {
//            	System.out.println("COLOR");
                consume(IToken.Kind.COLOR_OP,"");    //what to put in message?
            }
            else if(isKind(IToken.Kind.IMAGE_OP)) {
                consume(IToken.Kind.IMAGE_OP,"");    //what to put in message?
            }
            else if(isKind(IToken.Kind.BANG)) {
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


        if(curr + 1 < tokens.size()) {
            if(tokens.get(curr + 1).getKind() == IToken.Kind.LSQUARE) {
                e = PrimaryExpr();
                p = PixelSelector();
                e = new UnaryExprPostfix(firstToken, e, p);
            }
            else {
                e = PrimaryExpr();
            }
        }

//        if (isKind(IToken.Kind.LSQUARE)) {
//            p = PixelSelector();
//            e = new UnaryExprPostfix(firstToken, e, p);
//        }
        else {
            e = PrimaryExpr();
        }

        //e = new UnaryExprPostfix(firstToken, e, p );

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
            match(IToken.Kind.LPAREN);
            e= expr();
            match(IToken.Kind.RPAREN);
        }
        else if(isKind(Kind.COLOR_CONST)){
            e = new ColorConstExpr(firstToken);
            consume(Kind.COLOR_CONST, "");
        }
        else if(isKind(Kind.LANGLE)){
            Expr e2 = null;
            Expr e3=  null;

            consume(Kind.LANGLE, "");
            e = expr();
            match(Kind.COMMA);
            e2 = expr();
            match(Kind.COMMA);
            e3 = expr();
            match(Kind.RANGLE);
            e = new ColorExpr(firstToken, e, e2, e3);


        }
        else if(isKind(Kind.KW_CONSOLE)){
            e = new ConsoleExpr(firstToken);
            consume(Kind.KW_CONSOLE, "");

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
            match(IToken.Kind.COMMA);
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
    Dimension Dimension() throws PLCException {

        IToken firstToken = t;
        Expr x = null;
        Expr y = null;
        Dimension ast;
        if(isKind(IToken.Kind.LSQUARE)){
            consume(IToken.Kind.LSQUARE, "Expected left param");
            x = expr();
            match(IToken.Kind.COMMA);
            y = expr();
            consume(IToken.Kind.RSQUARE, "Expected right param");

            ast = new Dimension(firstToken, x, y);
        }
        else{
            throw new SyntaxException("Error in Dimension.");
        }
        return ast;
    }

    Statement Statement() throws PLCException{
        IToken firstToken = t;
        PixelSelector p = null;
        Expr e = null;
        Expr e2 = null;
        Statement state = null;

        if(isKind(Kind.IDENT)){
            consume(Kind.IDENT, "Expected Ident");
            if(isKind(Kind.LSQUARE)){
                p= PixelSelector();


            }
            if(isKind(Kind.ASSIGN)){
                consume(Kind.ASSIGN, "Expected Assign");
                e = expr();
                //TODO CHECK IF ITS T.GETTEXT() or FIRSTTOKEN.GETTEXT
                state = new AssignmentStatement(firstToken, t.getText(), p ,e);
            }
            else if(isKind(Kind.LARROW)){
                consume(Kind.LARROW, "Expected LARROW");
                e = expr();
                //TODO CHECK IF ITS T.GETTEXT() or FIRSTTOKEN.GETTEXT
                state = new ReadStatement(firstToken, t.getText(), p ,e);

            }




        }
        else if(isKind(Kind.KW_WRITE)){
            consume(Kind.KW_WRITE, "Expected Write");
            e = expr();
            match(Kind.RARROW);
            e2 = expr();
            state = new WriteStatement(firstToken, expr(),expr());


        }
        else if(isKind(Kind.RETURN)) {
            e = expr();
            state = new ReturnStatement(firstToken, e);

        }
        else{
            throw new SyntaxException("Error in Statement.");
        }

        return state;
    }



    //basically copy pasted from the book, which i think is allowed, but check later if this type of stuff needs
//changed
    private boolean match(IToken.Kind... kind) throws SyntaxException{
        for (IToken.Kind k : kind) {
            if (check(k)) {
                advance();
                return true;
            }
        }
        throw new SyntaxException("Match not found for " + peek().getKind());
//        return false;
    }
    private boolean check(IToken.Kind kind) {
        if (isAtEnd()) return false;
        return peek().getKind() == kind;
    }
    private IToken advance() throws SyntaxException {
        if (!isAtEnd()) {
            curr++;
            t = tokens.get(curr);
        }
        else {
            throw new SyntaxException("EOF");
        }
        return previous();
    }
    private boolean isAtEnd() {
        return peek().getKind() == IToken.Kind.EOF;
    }
    //TODO does this overlap with peek in the lexer class. Prob change this
    // I think no need because this is private and the lexer one will have to be called with lexer.peek()
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