package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.ASTNode;
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
import edu.ufl.cise.plc.ast.Types;
import edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.WriteStatement;
import edu.ufl.cise.plc.CodeGenStringBuilder;
import edu.ufl.cise.plc.runtime.ConsoleIO;

import edu.ufl.cise.plc.runtime.ColorTuple;
import edu.ufl.cise.plc.runtime.ImageOps;
import edu.ufl.cise.plc.runtime.javaCompilerClassLoader.PLCLangExec;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.naming.Name;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class CodeGenVisitor implements ASTVisitor {
	private String packageName;
	private ArrayList<String> imports;

	public CodeGenVisitor(String packageName) {
		this.packageName = packageName;
		this.imports = new ArrayList<String>();
	}

	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		boolean bool = booleanLitExpr.getValue();

		sb.append(booleanLitExpr.getText());

		return sb;

	}

	@Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		Type type = stringLitExpr.getType();

		// TODO check if append is right
		sb.stringQuotes().newline();
		sb.append(stringLitExpr.getValue());
		sb.stringQuotes();

		return sb;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		Type type = intLitExpr.getType();

		int intValue = intLitExpr.getValue();

		// TODO check if right

		if (intLitExpr.getCoerceTo() != type && intLitExpr.getCoerceTo() != null) {

			genTypeConversion(type, intLitExpr.getCoerceTo(), sb);
			sb.append(String.valueOf(intValue));
			sb.append(")");
		}
		else {
			sb.append(String.valueOf(intValue));
		}
		// return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));

		return sb;

	}

	@Override
	public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		Type type = floatLitExpr.getType();
		float floatValue = floatLitExpr.getValue();

		if (floatLitExpr.getCoerceTo() != Type.FLOAT && floatLitExpr.getCoerceTo() != null) {
			genTypeConversion(type, floatLitExpr.getCoerceTo(), sb);
			sb.append(floatLitExpr.getText());
			sb.append("f");
			sb.append(")");
		}
		else {
			sb.append(floatLitExpr.getText());
			sb.append("f");
		}



		return sb;
		// return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));
	}

	@Override
	public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
		throw new UnsupportedOperationException("ColorConstExpr not yet implemented.");
		// return null;
	}

	@Override
	public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
		if (imports.indexOf("edu.ufl.cise.plc.runtime.ConsoleIO") == -1) {
			imports.add("edu.ufl.cise.plc.runtime.ConsoleIO");
		}
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		// sb.append("(Integer) ConsoleIO.readValueFromConsole(�gINT�h, �gEnter
		// integer:�h);");
		String box = "(" + primitiveToWrapper(Types.toString(consoleExpr.getCoerceTo())) + ")";
		String type = consoleExpr.getCoerceTo().toString();
		String prompt = "Enter " + Types.toString(consoleExpr.getCoerceTo()) + ":";
		String consoleStatement = box + " ConsoleIO.readValueFromConsole(\"" + type + "\", \"" + prompt + "\")";
		sb.append(consoleStatement);
		return sb;
	}

	@Override
	public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
		return null;
	}

	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		Type type = unaryExpression.getType();
		Expr expr = unaryExpression.getExpr();


		if (type == Type.IMAGE || type == Type.COLOR || type == Type.COLORFLOAT){
			//TODO idk what this if does
			if(unaryExpression.getOp().getKind() == Kind.COLOR_OP){

				if(expr.getType() == Type.INT){
					//   Image a <-
					ColorTuple.getBlue();


				}



			}
			//TODO idk if this is right
			else if(expr.getType() == Type.IMAGE ){
				//  BufferedImage image = (BufferedImage)(new PLCLangExec("mypackage",true)).exec(input, null);
				ImageOps.extractRed();
				ImageOps.extractGreen();
				ImageOps.extractBlue();


			}
		}

		else {
			sb.lparen();
			sb.append(unaryExpression.getOp().getText());
			unaryExpression.getExpr().visit(this, sb);
			sb.rparen();

		}
//		if (unaryExpression.getCoerceTo() != type) {
//			genTypeConversion(type, unaryExpression.getCoerceTo(), sb);
//		}
		return sb;
	}

	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
		// CodeGenStringBuilder sb = new CodeGenStringBuilder();
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		Type type = binaryExpr.getType();
		// Expr leftExpr = binaryExpr.getLeft();
		// Expr rightExpr = binaryExpr.getRight();
//        Type leftType = leftExpr.getCoerceTo() != null ? leftExpr.getCoerceTo() : leftExpr.getType();
		// Type rightType = rightExpr.getCoerceTo() != null ? rightExpr.getCoerceTo() :
		// rightExpr.getType();
		// Kind op = binaryExpr.getOp().getKind();

		// TODO check if binary expr is in assignment 5
		// if (not handled in assignment 5)
		// throw new UnsupportedOperationException("Not implemented");
		// else {
		sb.lparen();
		if(binaryExpr.getLeft().getType() == Type.STRING && binaryExpr.getOp().getKind() == Kind.EQUALS) {
			binaryExpr.getLeft().visit(this, sb);
			sb.append(".equals(");
			binaryExpr.getRight().visit(this, sb);
			sb.rparen();
		}
		else if(binaryExpr.getLeft().getType() == Type.STRING && binaryExpr.getOp().getKind() == Kind.NOT_EQUALS) {
			sb.append("!");
			binaryExpr.getLeft().visit(this, sb);
			sb.append(".equals(");
			binaryExpr.getRight().visit(this, sb);
			sb.rparen();
		}
		else {
			sb.lparen();
			binaryExpr.getLeft().visit(this, sb);
			sb.rparen();

			sb.append(binaryExpr.getOp().getText());
			sb.lparen();
			binaryExpr.getRight().visit(this, sb);
			sb.rparen();
		}
		sb.rparen();
		// }

		return sb;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		Type type = identExpr.getType();
		String text = identExpr.getText();

		// TODO might be wrong visit/also check if it needed
		// identExpr.visit(this, sb);


		if (identExpr.getCoerceTo() != type && identExpr.getCoerceTo() != null) {
			genTypeConversion(type, identExpr.getCoerceTo(), sb);
			sb.append(text);
			sb.append(")");
		}
		else {
			sb.append(text);
		}

		// return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));
		return sb;

	}

	@Override
	// ( <condition> ) ? <trueCase> : <falseCase>
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;

		Expr trueExpr = conditionalExpr.getTrueCase();
		Expr falseExpr = conditionalExpr.getFalseCase();
		sb.lparen();
		conditionalExpr.getCondition().visit(this, sb);
		sb.rparen();
		sb.question();
		trueExpr.visit(this, sb);
		sb.colon();
		falseExpr.visit(this, sb);

		return sb;
	}

	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;

		dimension.getWidth().visit(this, sb);
		sb.comma().space();
		dimension.getHeight().visit(this, sb);

		return sb;
		//throw new UnsupportedOperationException("Dimension not yet implemented.");
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		throw new UnsupportedOperationException("PixelSelector not yet implemented.");
		// return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;

		String name = assignmentStatement.getName();
		Expr expr = assignmentStatement.getExpr();

		sb.append(name);
		sb.equals();
		if (expr.getCoerceTo() != expr.getType() && expr.getCoerceTo() != null) {
//			if(declaration.getExpr().)
			genTypeConversionNoParen(expr.getType(), expr.getCoerceTo(), sb);
			sb.lparen();
			expr.visit(this, sb);
			sb.rparen();
		}
		else {
			expr.visit(this, sb);
//	        sb.append(expr.getText());
		}
		sb.semi();
		sb.newline();


		if(assignmentStatement.getTargetDec().getType() == Type.IMAGE && assignmentStatement.getExpr().getType() == Type.IMAGE){
			//TODO check if this works
			if(assignmentStatement.getTargetDec().getDim() != null){}

			ImageOps.resize();
		}



		return sb;
	}

	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		// TODO maybe need fix to have printstream
		// PrintStream stream = ConsoleIO.console.println();
		// ConsoleIO.console.println(<source>) ;
		sb.append("ConsoleIO.console.println(");
		writeStatement.getSource().visit(this, sb);
		sb.rparen().semi().newline();
		if(writeStatement.getDest().getType() == Type.CONSOLE && writeStatement.getSource().getType() == Type.IMAGE){
			ConsoleIO.displayImageOnScreen();

		}


		return null;
	}

	@Override
	public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		String name = readStatement.getName();
		Expr expr = readStatement.getSource();
		if (readStatement.getSource().getType() != Type.CONSOLE) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			sb.append(name);
			sb.equals();
			expr.visit(this, sb);
			sb.semi().newline();
		}

		return sb;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {

		CodeGenStringBuilder sb = new CodeGenStringBuilder();
		List<NameDef> namedef = program.getParams();
		List<ASTNode> decAndStatement = program.getDecsAndStatements();
		// sb.append("package " + packageName + ";").newline();
		sb.append("public class ");
		sb.append(program.getName());
		sb.LCurl().newline();
		sb.append("\tpublic static ");
		sb.append(Types.toString(program.getReturnType()));
		sb.append(" apply(");

		if (namedef.size() > 1) {
			namedef.get(0).visit(this, sb);
			for (int x = 1; x < namedef.size(); x++) {
				sb.comma().space();
				namedef.get(x).visit(this, sb);
			}
		} else {
			for (int x = 0; x < namedef.size(); x++) {
				namedef.get(x).visit(this, sb);
			}

		}
		sb.rparen().LCurl().newline();
		for (int x = 0; x < decAndStatement.size(); x++) {
			sb.tab().tab();
			decAndStatement.get(x).visit(this, sb);
			sb.newline();

		}
		sb.tab().RCurl().newline().RCurl();

		if (imports.size() > 0) {
			for (int x = 0; x < imports.size(); x++) {
				sb.insert(0, "import " + imports.get(x) + ";\n");
			}
		}
		if(packageName != null && packageName != "") {
			sb.insert(0, "package " + packageName + ";\n");
		}
		return sb.getString();
	}
	// public Object visitParams(List<NameDef> namedef, Object arg) throws Exception
	// {

	// }
	// public Object visitImport(Program program, Object arg) throws Exception {

	// }

	@Override
	public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {

		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		Type type = nameDef.getType();
		String name = nameDef.getName();

		if (type == Type.STRING) {
			sb.append("String").space();
		} else {
			sb.append(String.valueOf(type).toLowerCase()).space();
		}
		sb.append(name);

		return sb;
	}

	@Override
	public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		Type type = nameDefWithDim.getType();
		String name = nameDefWithDim.getName();

		if(type == Type.IMAGE) {
			sb.append("BufferedImage ");
		}
		else {
			if (type == Type.STRING) {
				sb.append("String").space();
			} else {
				sb.append(String.valueOf(type).toLowerCase()).space();
			}
		}
		sb.append(name).space();

		return sb;
	}

	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		Expr expr = returnStatement.getExpr();
		sb.append("return ");
		expr.visit(this, sb);
		sb.semi();
		return sb;
	}

	@Override
	public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		IToken opExpr = declaration.getOp();
		if (declaration.getOp() == null) {
			declaration.getNameDef().visit(this, sb);
			if(declaration.getNameDef().getType() == Type.IMAGE) {
				imports.add("java.awt.image.BufferedImage");
				sb.equals().space();
				sb.append("new BufferedImage(");
				declaration.getDim().visit(this, sb);
				sb.append(", BufferedImage.TYPE_INT_RGB)");
			}
			sb.semi();
		} else {
			Kind op = declaration.getOp().getKind();
			if (op == Kind.ASSIGN || op == Kind.LARROW) {
				declaration.getNameDef().visit(this, sb);
				sb.equals();
				//sb.append(opExpr.getText());
				if (declaration.getNameDef().getType() == Type.IMAGE) {
					imports.add("java.awt.image.BufferedImage");
					imports.add("edu.ufl.cise.plc.runtime.FileURLIO");
					sb.space();
					if(declaration.getDim() == null) {
						throw new UnsupportedOperationException("image without dimension not yet implemented.");
					}
					else {
						sb.append("FileURLIO.readImage(");
						declaration.getExpr().visit(this, sb);
						sb.comma().space();
						declaration.getDim().visit(this, sb);
						sb.rparen().semi().newline();
					}
					sb.tab().tab().append("FileURLIO.closeFiles()");
				}
				else {
					if (declaration.getExpr().getCoerceTo() != declaration.getExpr().getType() && declaration.getExpr().getCoerceTo() != null) {
						//					if(declaration.getExpr().)
						genTypeConversionNoParen(declaration.getType(), declaration.getExpr().getCoerceTo(), sb);
						sb.lparen();
						declaration.getExpr().visit(this, sb);
						sb.rparen();
					}
					else {
						declaration.getExpr().visit(this, sb);
					}
				}
				sb.semi();
			}
//            else if(op == Kind.LARROW) {
//            	if(declaration.getExpr().getType() != Type.CONSOLE) {
//            		throw new UnsupportedOperationException("rhs of read assignment is not console: " + declaration.getExpr().getType());
//            	}
//            	declaration.getNameDef().visit(this, sb);
//            	sb.append(opExpr.getText());
//            	declaration.getExpr().visit(this, sb);
//            	sb.semi();
//            }
		}

		return sb;

	}

	@Override
	public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
		throw new UnsupportedOperationException("UnaryExprPostfix not yet implemented.");
		// return null;
	}

	public void genTypeConversion(Type type, Type coerceTo, CodeGenStringBuilder sb) {
		sb.append("((" + Types.toString(coerceTo) + ")");
	}

	public void genTypeConversionNoParen(Type type, Type coerceTo, CodeGenStringBuilder sb) {
		sb.append("(" + Types.toString(coerceTo) + ")");
	}


	// Function to turn primitives to wrapper types (i.e. int -> Integer)
	private String primitiveToWrapper(String primitive) {
		return switch (primitive) {
			case "boolean" -> "Boolean";
			case "float" -> "Float";
			case "int" -> "Integer";
			default -> throw new IllegalArgumentException("No wrapper for type: " + primitive);
		};
	}

}
