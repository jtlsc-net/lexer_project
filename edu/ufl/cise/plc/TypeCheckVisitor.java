package edu.ufl.cise.plc;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.ASTVisitor;
import edu.ufl.cise.plc.ast.AssignmentStatement;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.ColorConstExpr;
import edu.ufl.cise.plc.ast.ColorExpr;
import edu.ufl.cise.plc.ast.ConditionalExpr;
import edu.ufl.cise.plc.ast.ConsoleExpr;
import edu.ufl.cise.plc.ast.Declaration;
import edu.ufl.cise.plc.ast.Dimension;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.FloatLitExpr;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.IntLitExpr;
import edu.ufl.cise.plc.ast.NameDef;
import edu.ufl.cise.plc.ast.NameDefWithDim;
import edu.ufl.cise.plc.ast.PixelSelector;
import edu.ufl.cise.plc.ast.Program;
import edu.ufl.cise.plc.ast.ReadStatement;
import edu.ufl.cise.plc.ast.ReturnStatement;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.WriteStatement;

import javax.naming.Name;

import static edu.ufl.cise.plc.ast.Types.Type.*;

public class TypeCheckVisitor implements ASTVisitor {

	SymbolTable symbolTable = new SymbolTable();
	Program root;

	record Pair<T0,T1>(T0 t0, T1 t1){};  //may be useful for constructing lookup tables.
	private boolean assignmentCompatible(Type targetType, Type rhsType) {

		//TODO CHANGE FOR NEEDED
		boolean flag = false ;

		if(targetType == rhsType){
			flag=true;
		}

		return flag;
	}
	private void check(boolean condition, ASTNode node, String message) throws TypeCheckException {
		if (!condition) {
			throw new TypeCheckException(message, node.getSourceLoc());
		}
	}

	//The type of a BooleanLitExpr is always BOOLEAN.
	//Set the type in AST Node for later passes (code generation)
	//Return the type for convenience in this visitor.
	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
		booleanLitExpr.setType(Type.BOOLEAN);
		return Type.BOOLEAN;
	}

	@Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
		//TODO:  implement this method

		stringLitExpr.setType(Type.STRING);
		return Type.STRING;

	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
		//TODO:  implement this method
		//does this have to be more complex

		intLitExpr.setType(Type.INT);
		return Type.INT;
	}

	@Override
	public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
		floatLitExpr.setType(Type.FLOAT);
		return Type.FLOAT;
	}

	@Override
	public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
		//TODO:  implement this method


		//TODO check if needs to be more complex

		colorConstExpr.setType(COLOR);
		return Type.COLOR;
	}

	@Override
	public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
		consoleExpr.setType(Type.CONSOLE);
		return Type.CONSOLE;
	}

	//Visits the child expressions to get their type (and ensure they are correctly typed)
	//then checks the given conditions.
	@Override
	public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
		Type redType = (Type) colorExpr.getRed().visit(this, arg);
		Type greenType = (Type) colorExpr.getGreen().visit(this, arg);
		Type blueType = (Type) colorExpr.getBlue().visit(this, arg);
		check(redType == greenType && redType == blueType, colorExpr, "color components must have same type");
		check(redType == Type.INT || redType == Type.FLOAT, colorExpr, "color component type must be int or float");
		Type exprType = (redType == Type.INT) ? Type.COLOR : Type.COLORFLOAT;
		colorExpr.setType(exprType);
		return exprType;
	}



	//Maps forms a lookup table that maps an operator expression pair into result type.
	//This more convenient than a long chain of if-else statements.
	//Given combinations are legal; if the operator expression pair is not in the map, it is an error.
	Map<Pair<Kind,Type>, Type> unaryExprs = Map.of(
			new Pair<Kind,Type>(Kind.BANG,BOOLEAN), BOOLEAN,
			new Pair<Kind,Type>(Kind.MINUS, FLOAT), FLOAT,
			new Pair<Kind,Type>(Kind.MINUS, INT),INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,INT), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,COLOR), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,IMAGE), IMAGE,
			new Pair<Kind,Type>(Kind.IMAGE_OP,IMAGE), INT
	);

	//Visits the child expression to get the type, then uses the above table to determine the result type
	//and check that this node represents a legal combination of operator and expression type.
	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws Exception {
		// !, -, getRed, getGreen, getBlue
		Kind op = unaryExpr.getOp().getKind();
		Type exprType = (Type) unaryExpr.getExpr().visit(this, arg);
		//Use the lookup table above to both check for a legal combination of operator and expression, and to get result type.
		Type resultType = unaryExprs.get(new Pair<Kind,Type>(op,exprType));
		check(resultType != null, unaryExpr, "incompatible types for unaryExpr");
		//Save the type of the unary expression in the AST node for use in code generation later.
		unaryExpr.setType(resultType);
		//return the type for convenience in this visitor.
		return resultType;
	}


	//This method has several cases. Work incrementally and test as you go.
	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
		//TODO:  implement this method

		Kind op = binaryExpr.getOp().getKind();
		Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
		Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
		Type resultType = null;
		switch(op) {
			case AND,OR -> {
				//TODO check if right
				if (leftType == BOOLEAN && rightType == BOOLEAN) resultType = BOOLEAN;
				else check(false, binaryExpr, "incompatible types for operator");
			}
			case EQUALS,NOT_EQUALS -> {
				check(leftType == rightType, binaryExpr, "incompatible types for comparison");
				resultType = Type.BOOLEAN;

			}

			case PLUS, MINUS -> {
				if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
				else if (leftType == Type.STRING && rightType == Type.STRING) resultType = Type.STRING;
				else if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) resultType = Type.BOOLEAN;
				else if (leftType == FLOAT && rightType == FLOAT) resultType = Type.FLOAT;
				else if (leftType == FLOAT && rightType == INT){
					binaryExpr.getRight().setCoerceTo(FLOAT);
					resultType = Type.FLOAT;

				}
				else if (leftType == INT && rightType == FLOAT){
					binaryExpr.getLeft().setCoerceTo(FLOAT);
					resultType = Type.FLOAT;}
				else if (leftType == COLOR && rightType == COLOR) resultType = COLOR;
				else if (leftType == COLORFLOAT && rightType == COLORFLOAT) resultType = COLORFLOAT;
				else if (leftType == COLORFLOAT && rightType == COLOR){
					binaryExpr.getRight().setCoerceTo(COLORFLOAT);
					resultType = COLORFLOAT;}
				else if (leftType == COLOR && rightType == COLORFLOAT) {
					binaryExpr.getLeft().setCoerceTo(COLORFLOAT);
					resultType = COLORFLOAT;
				}
				else if (leftType == IMAGE && rightType == IMAGE) resultType = IMAGE;
				else check(false, binaryExpr, "incompatible types for operator");

			}
			case TIMES,MOD,DIV -> {
				if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
				else if (leftType == Type.STRING && rightType == Type.STRING) resultType = Type.STRING;
				else if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) resultType = Type.BOOLEAN;
				else if (leftType == FLOAT && rightType == FLOAT) resultType = Type.FLOAT;
				else if (leftType == FLOAT && rightType == INT){
					binaryExpr.getRight().setCoerceTo(FLOAT);
					resultType = Type.FLOAT;}
				else if (leftType == INT && rightType == FLOAT){
					binaryExpr.getLeft().setCoerceTo(FLOAT);
					resultType = Type.FLOAT;}
				else if (leftType == COLOR && rightType == COLOR) resultType = COLOR;
				else if (leftType == COLORFLOAT && rightType == COLORFLOAT) resultType = COLORFLOAT;
				else if (leftType == COLORFLOAT && rightType == COLOR){
					binaryExpr.getRight().setCoerceTo(COLORFLOAT);
					resultType = COLORFLOAT;}
				else if (leftType == COLOR && rightType == COLORFLOAT) {
					binaryExpr.getLeft().setCoerceTo(COLORFLOAT);
					resultType = COLORFLOAT;
				}
				else if (leftType == IMAGE && rightType == IMAGE) resultType = IMAGE;
				else if (leftType == IMAGE && rightType == INT){
					binaryExpr.getRight().setCoerceTo(IMAGE);
					resultType = IMAGE;
				}
				else if (leftType == IMAGE && rightType == FLOAT){
					binaryExpr.getRight().setCoerceTo(IMAGE);
					resultType = IMAGE;
				}
				else if (leftType == INT && rightType == COLOR) {
					binaryExpr.getLeft().setCoerceTo(COLOR);
					resultType = COLOR;
				}
				else if (leftType == COLOR && rightType == INT) {
					binaryExpr.getRight().setCoerceTo(COLOR);
					resultType = COLOR;}
				else if (leftType == FLOAT && rightType == COLOR) {
					binaryExpr.getLeft().setCoerceTo(COLORFLOAT);
					binaryExpr.getRight().setCoerceTo(COLORFLOAT);
					resultType = COLORFLOAT;}
				else if (leftType == COLOR && rightType == FLOAT){
					binaryExpr.getLeft().setCoerceTo(COLORFLOAT);
					binaryExpr.getRight().setCoerceTo(COLORFLOAT);
					resultType = COLORFLOAT;}
				else check(false, binaryExpr, "incompatible types for operator");
			}

			case LT, LE, GT, GE -> {

				if (leftType == Type.INT && rightType == Type.INT) resultType = BOOLEAN;
				else if (leftType == FLOAT && rightType == FLOAT) resultType = BOOLEAN;
				else if (leftType == FLOAT && rightType == INT){

					binaryExpr.getRight().setCoerceTo(FLOAT);
					resultType = BOOLEAN;}
				else if (leftType == INT && rightType == FLOAT) {

					binaryExpr.getLeft().setCoerceTo(FLOAT);
					resultType = BOOLEAN;}

				else check(false, binaryExpr, "incompatible types for operator");
			}
			default -> {
				throw new Exception("compiler error");
			}
		}
		binaryExpr.setType(resultType);
		return resultType;
	}




	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
		//TODO:  implement this method



		String name = identExpr.getText();
		Declaration dec = symbolTable.lookup(name);
		check(dec != null, identExpr, "undefined identifier " + name);

		check(dec.isInitialized(), identExpr, "using uninitialized variable");
		identExpr.setDec(dec);
		Type type = dec.getType();
		identExpr.setType(type);

		return type;
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
		//TODO  implement this method
		Type condition = (Type) conditionalExpr.getCondition().visit(this, arg);
		check(condition == BOOLEAN, conditionalExpr, "Type of condition must be boolean");

		check(conditionalExpr.getTrueCase().getType() == conditionalExpr.getTrueCase().getType(), conditionalExpr,
				"Type of trueCase must be the same as the type of falseCase");
		check(condition == conditionalExpr.getTrueCase().getType(), conditionalExpr,
				"Type of trueCase must be the same as the type of falseCase");

		return condition;

	}

	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws Exception {
		//TODO  implement this method
		throw new UnsupportedOperationException();
	}

	@Override
	//This method can only be used to check PixelSelector objects on the right hand side of an assignment.
	//Either modify to pass in context info and add code to handle both cases, or when on left side
	//of assignment, check fields from parent assignment statement.
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		Type xType = (Type) pixelSelector.getX().visit(this, arg);
		check(xType == Type.INT, pixelSelector.getX(), "only ints as pixel selector components");
		Type yType = (Type) pixelSelector.getY().visit(this, arg);
		check(yType == Type.INT, pixelSelector.getY(), "only ints as pixel selector components");
		return null;
	}

	@Override
	//This method several cases--you don't have to implement them all at once.
	//Work incrementally and systematically, testing as you go.
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
		//TODO:  implement this method

//TODO CHECK IF THE THING DOWN BELOW IS RIGHT

		String name = assignmentStatement.getName();
		Declaration declaration = symbolTable.lookup(name);
		check(declaration != null, assignmentStatement, "undeclared variable " + name);
		Type expressionType= (Type) assignmentStatement.getExpr().visit(this, arg);
		declaration.setInitialized(true);

		if(declaration.getType() != Type.IMAGE) {
			boolean flag = false;
			if(declaration.getType() == INT && expressionType ==FLOAT){
				assignmentStatement.getExpr().setCoerceTo(INT);
				flag = true;
			}
			if(declaration.getType() == FLOAT && expressionType ==INT){
				assignmentStatement.getExpr().setCoerceTo(FLOAT);
				flag = true;
			}
			if(declaration.getType() == INT && expressionType ==COLOR){
				assignmentStatement.getExpr().setCoerceTo(INT);
				flag = true;
			}
			if(declaration.getType() == COLOR && expressionType ==INT){
				assignmentStatement.getExpr().setCoerceTo(COLOR);
				flag = true;
			}

			check(assignmentCompatible(declaration.getType(), expressionType)|| flag==true, assignmentStatement, "incompatible types in assignment");
			return expressionType;
		}


		else if( declaration.getType() == IMAGE && assignmentStatement.getSelector() == null){
			boolean flag = false;
			if(declaration.getType() == IMAGE && expressionType ==IMAGE){
				flag = true;
			}
			if(declaration.getType() == IMAGE && expressionType ==INT){
				assignmentStatement.getExpr().setCoerceTo(COLOR);
				flag = true;
			}
			if(declaration.getType() == IMAGE && expressionType ==FLOAT){
				assignmentStatement.getExpr().setCoerceTo(COLORFLOAT);
				flag = true;
			}
			if(declaration.getType() == IMAGE && expressionType ==COLORFLOAT){
				flag = true;
			}
			if(declaration.getType() == IMAGE && expressionType ==COLOR){
				flag = true;
			}
			check(flag==true, assignmentStatement, "incompatible types in assignment");
			return expressionType;
		}
		else if(declaration.getType() == IMAGE && assignmentStatement.getSelector() != null){

			String pixel1 =  assignmentStatement.getSelector().getX().getText();
			String pixel2 =  assignmentStatement.getSelector().getY().getText();

			Declaration dec1 = symbolTable.lookup(pixel1);
			Declaration dec2 = symbolTable.lookup(pixel2);

			check(dec1 == null, assignmentStatement, "Pixel 1 can't declared variable " + pixel1);
			check(dec2 == null, assignmentStatement, "Pixel 2 can't declared variable  " + pixel2);
			boolean flag = false;
			if( expressionType ==INT || expressionType == FLOAT|| expressionType == COLOR || expressionType == COLORFLOAT){
				assignmentStatement.getExpr().setCoerceTo(COLOR);
				flag = true;
			}

			check(flag ==  true , assignmentStatement, "incompatible types in assignment");
			symbolTable.remove(pixel1);
			symbolTable.remove(pixel2);
			return expressionType;
		}


		return null;

	}


	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
		Type sourceType = (Type) writeStatement.getSource().visit(this, arg);
		Type destType = (Type) writeStatement.getDest().visit(this, arg);
		check(destType == Type.STRING || destType == Type.CONSOLE, writeStatement,
				"illegal destination type for write");
		check(sourceType != Type.CONSOLE, writeStatement, "illegal source type for write");
		return null;
	}

	@Override
	public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
		//TODO:  implement this method
		//TODO: A read statement cannot have a PixelSelector
		//The right hand side type must be CONSOLE or STRING
		//Mark target variable as initialized.

		String name = readStatement.getName();
		Declaration declaration = symbolTable.lookup(name);
		check(declaration != null, readStatement, "undeclared variable " + name);
		Type lhs = declaration.getType();
		check(readStatement.getSelector() == null, readStatement, "Read Statement cannot have Pixel Statement" );
		Type rhs = (Type) readStatement.getSource().getType();
		check(rhs == CONSOLE || rhs == STRING, readStatement, "Right hand side must be Console or String" );

		//TODO check if it is the lhs declaration, may be wrong
		readStatement.getTargetDec().setInitialized(true);

		return null;

	}

	@Override
	public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
		//TODO:  implement this method

		throw new UnsupportedOperationException("Unimplemented visit method.");
	}


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		//TODO:  this method is incomplete, finish it.

		List<NameDef> nameDefParam = program.getParams();
		//TODO this needs to be checked
		for (NameDef node : nameDefParam) {
			node.visit(this, arg);
			node.setInitialized(true);

		}


		//Save root of AST so return type can be accessed in return statements
		root = program;

		//Check declarations and statements
		List<ASTNode> decsAndStatements = program.getDecsAndStatements();
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}
		return program;
	}

	@Override
	public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
		//TODO:  implement this method
		String name = nameDef.getName();
		boolean inserted = symbolTable.insert(name,nameDef);
		check(inserted, nameDef, "variable " + name + "already declared");



		return null;
	}

	@Override
	public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
		//TODO:  implement this method
		String name = nameDefWithDim.getName();
		boolean inserted = symbolTable.insert(name,nameDefWithDim);
		check(inserted, nameDefWithDim, "variable " + name + "already declared");

		check(nameDefWithDim.getDim().getHeight().getType()==INT &&nameDefWithDim.getDim().getWidth().getType()==INT,
				nameDefWithDim, "both expressions must be int" );


		return null;
	}

	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
		Type returnType = root.getReturnType();  //This is why we save program in visitProgram.
		Type expressionType = (Type) returnStatement.getExpr().visit(this, arg);
		check(returnType == expressionType, returnStatement, "return statement with invalid type");
		return null;
	}

	@Override
	public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
		Type expType = (Type) unaryExprPostfix.getExpr().visit(this, arg);
		check(expType == Type.IMAGE, unaryExprPostfix, "pixel selector can only be applied to image");
		unaryExprPostfix.getSelector().visit(this, arg);
		unaryExprPostfix.setType(Type.INT);
		unaryExprPostfix.setCoerceTo(COLOR);
		return Type.COLOR;
	}

}
