import syntaxtree.*;
import visitor.*;
import java.util.*;

public class LLVMVisitor extends GJDepthFirst<String, StoreTypes>{
	SymbolTable symtable;
	ClassType curclass;
	MethodType curmethod;
	public String ll;

	public LLVMVisitor(SymbolTable table, String init_code){
		this.symtable = table;
		this.curclass = null;
		this.curmethod = null;
		this.ll = init_code;
	}

	public String getLLCode(){
		return this.ll;
	}
	
	public String ifLabel(){
		int reg_count = curmethod.getRCount();
		return "if" + reg_count;
	}

	public String loopLabel(){
		int reg_count = curmethod.getRCount();
		return "loop" + reg_count;
	}

	/**
	* f0 -> "class"
	* f1 -> Identifier()
	* f2 -> "{"
	* f3 -> "public"
	* f4 -> "static"
	* f5 -> "void"
	* f6 -> "main"
	* f7 -> "("
	* f8 -> "String"
	* f9 -> "["
	* f10 -> "]"
	* f11 -> Identifier()
	* f12 -> ")"
	* f13 -> "{"
	* f14 -> ( VarDeclaration() )*
	* f15 -> ( Statement() )*
	* f16 -> "}"
	* f17 -> "}"
	*/
	public String visit(MainClass n, StoreTypes argu) throws Exception {
		// curclass = symtable.getClass(n.f1.accept(this,argu));
		curclass = symtable.getClass(n.f1.f0.toString());
		curmethod = curclass.getMethod(n.f6.toString());

		this.ll += "define i32 @main() {\n";
		n.f11.accept(this, argu);
		n.f14.accept(this, argu);
		n.f15.accept(this, argu);
		// System.out.println(ll);

		this.ll += "\n\n\tret i32 0\n}\n";
		// end of declaration, initialize variables
		curclass = null;
		curmethod = null;
		return null;
	}

	/**
	* f0 -> "class"
	* f1 -> Identifier()
	* f2 -> "{"
	* f3 -> ( VarDeclaration() )*
	* f4 -> ( MethodDeclaration() )*
	* f5 -> "}"
	*/
	public String visit(ClassDeclaration n, StoreTypes argu) throws Exception {
		// curclass = symtable.getClass(n.f1.accept(this, argu));
		curclass = symtable.getClass(n.f1.f0.toString());
		if (curclass == null)
			throw new Exception("ClassDeclaration::CLASS NULL");
		// n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		// end of declaration, initialize variables
		curclass = null;
		curmethod = null;
		return null;
	}

	/**
	* f0 -> "class"
	* f1 -> Identifier()
	* f2 -> "extends"
	* f3 -> Identifier()
	* f4 -> "{"
	* f5 -> ( VarDeclaration() )*
	* f6 -> ( MethodDeclaration() )*
	* f7 -> "}"
	*/
	public String visit(ClassExtendsDeclaration n, StoreTypes argu) throws Exception {
		// curclass = symtable.getClass(n.f1.accept(this, argu));
		curclass = symtable.getClass(n.f1.f0.toString());
		
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
		
		// end of declaration, initialize variables
		curclass = null;
		curmethod = null;
		return null;
	}

	/**
	* f0 -> Type()
	* f1 -> Identifier()
	* f2 -> ";"
	*/
	public String visit(VarDeclaration n, StoreTypes argu) throws Exception {
		// check for var decls slipped from first visitor, eg forward declaration
		// String type = n.f0.accept(this, argu);
		// String varname = n.f1.f0.toString();
		
		// VariableType tmpvar;
		// if ((tmpvar = curmethod.getVar(varname)) == null){
		// 	tmpvar = curclass.getVar(varname);
		// }
		this.ll += "\t%" + n.f1.f0.toString() + " = alloca " + curmethod.typeToLLVM(n.f0.accept(this, argu)) + "\n";

		return null;
	}

	/**
	* f0 -> "public"
	* f1 -> Type()
	* f2 -> Identifier()
	* f3 -> "("
	* f4 -> ( FormalParameterList() )?
	* f5 -> ")"
	* f6 -> "{"
	* f7 -> ( VarDeclaration() )*
	* f8 -> ( Statement() )*
	* f9 -> "return"
	* f10 -> Expression()
	* f11 -> ";"
	* f12 -> "}"
	*/
	public String visit(MethodDeclaration n, StoreTypes argu) throws Exception {
		// update curmethod
		// curmethod = curclass.getMethod(n.f2.accept(this, argu));
		String paramcode = "";
		String methodname = n.f2.f0.toString();
		curmethod = curclass.getMethod(methodname);
		this.ll += "\ndefine " + curmethod.typeToLLVM(curmethod.getType()) + " @" + curclass.getName() + "." + methodname + "(i8* %this";
		ArrayList<VariableType> params = curmethod.getParams();
		for (VariableType vari : params){
			String lltype = vari.typeToLLVM(vari.getType());
			String varname = vari.getName();
			this.ll += ", " + lltype + " %." + varname;

			// generate code to allocate parameters, with this way we loop only once 
			paramcode += "\t%" + varname + " = alloca " + lltype + "\n";
			paramcode += "\tstore " + lltype + " %." + varname + ", " + lltype + "* %" + varname + "\n";
			// load var in register
			// loadparams += "\t%_" + curmethod.getRegCount() + " = load " + lltype + ", " + lltype + "* %" + varname + "\n";
		}
		
		this.ll += ") {\n" + paramcode;
		// this.ll += paramcode;
		// n.f4.accept(this, argu);
		n.f7.accept(this, argu);
		n.f8.accept(this, argu);
		
		// // check return type to match with methods type
		// String retType = n.f10.accept(this, argu);
		// boolean typefound = false;
		// String methodtype = curmethod.getType();
		// if (methodtype.equals(retType))
		// 	typefound = true;
		// else{
		// 	ClassType tmpClass;
		// 	tmpClass = symtable.getClass(retType);
		// 	if (tmpClass != null){
		// 		String parentName = tmpClass.getParentName();
		// 		while (parentName != null){
		// 			if (methodtype.equals(parentName)){
		// 				typefound = true;
		// 				break;
		// 			}
		// 			tmpClass = symtable.getClass(tmpClass.parentName);
		// 			parentName = tmpClass.getParentName();
		// 		}
		// 	}
		// }
		
		System.out.println("---------STARTING RET----------");
		// String ret ="";
		String ret = n.f10.accept(this, argu);
		this.ll += "\tret " + curmethod.typeToLLVM(curmethod.getType()) + " " + ret + "\n";
		this.ll += "}\n";
		// end of method decl, initialize variable
		curmethod = null;

		return null;
	}

	/**
	* f0 -> Block()
	*   | AssignmentStatement()
	*   | ArrayAssignmentStatement()
	*   | IfStatement()
	*   | WhileStatement()
	*   | PrintStatement()
	*/
	public String visit(Statement n, StoreTypes argu) throws Exception {
		StoreTypes argument;
		if (argu != null)
			n.f0.accept(this, argu);
		else {
			argument = new StoreTypes();
			// argu = new StoreTypes();
			n.f0.accept(this, argument);
		}
		return null;
	}

	/**
	* f0 -> "{"
	* f1 -> ( Statement() )*
	* f2 -> "}"
	*/
	public String visit(Block n, StoreTypes argu) throws Exception {
		// n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		// n.f2.accept(this, argu);
		return null;
	}

	/**
	* f0 -> Identifier()
	* f1 -> "="
	* f2 -> Expression()
	* f3 -> ";"
	*/
	public String visit(AssignmentStatement n, StoreTypes argu) throws Exception {
		String name = n.f0.accept(this, argu);
		String reg = n.f2.accept(this, argu);
		
		System.out.println("Assignment:: " + name);
		// check to identify variable
		VariableType tmpvar;
		boolean variableFound = false;
		boolean classvariable = false;
		if ((tmpvar = curmethod.findVar(name)) != null){
			variableFound = true;
			String type = tmpvar.typeToLLVM(tmpvar.getType());
			// System.out.println("Assignment2:: " + n.f2.accept(this, argu));
			this.ll += "\tstore " + type + " " + reg + ", " + type + "* %" + name + "\n"; 

		}
		else{
			if ((tmpvar = curclass.getVar(name)) != null){
				variableFound = true;
				classvariable = true;
			}
			else{
				String parentName = curclass.getParentName();
				ClassType tmpClass;
				while (parentName != null){
					tmpClass = symtable.getClass(parentName);
					if ((tmpvar = tmpClass.getVar(name)) != null){
						variableFound = true;
						classvariable = true;
						break;
					}
					parentName = tmpClass.getParentName();
				}
			}

			if (classvariable){
				String type = tmpvar.typeToLLVM(tmpvar.getType());
				String r0 = curmethod.getRegCount();
				String r1 = curmethod.getRegCount();
				this.ll += "\t" + r0 + " = getelementptr i8, i8* %this, i32 " + (tmpvar.getOffset() + 8) + "\n";
				this.ll += "\t" + r1 + " = bitcast i8* " + r0 + " to " + type + "*\n";
				this.ll += "\tstore " + type + reg + ", " + type + "* " + r1 + "\n";
			}
		}

		// if (!variableFound)
		// 	throw new Exception("Assignment::Variable doesn't have a declared type");
		
		// // check if types are same
		// if (symtable.checkForType(tmpvar.getType(), n.f2.accept(this, argu)) == false)
		// 	throw new Exception("Types dont match");


		// String type1 = tmpvar.getType();
		// String type2 = n.f2.accept(this, argu);
		// boolean typeFound = false;
		// if (type1.equals(type2))
		// 	typeFound = true;
		// else{
		// 	ClassType tmpclass = symtable.getClass(type2);
		// 	if (tmpclass != null){
		// 		String parentName = tmpclass.getParentName();
		// 		while (parentName != null){
		// 			if (type1.equals(parentName)){
		// 				typeFound = true;
		// 				break;
		// 			}
		// 			tmpclass = symtable.getClass(parentName);
		// 			parentName = tmpclass.getParentName();
		// 		}
		// 	}
		// }

		// if (!typeFound)
		// 	throw new Exception("Types dont match");

		// return null;
		return name;
	}

	/**
	* f0 -> Identifier()
	* f1 -> "["
	* f2 -> Expression()
	* f3 -> "]"
	* f4 -> "="
	* f5 -> Expression()
	* f6 -> ";"
	*/
	public String visit(ArrayAssignmentStatement n, StoreTypes argu) throws Exception {
		String name = n.f0.accept(this, argu);
		// String r0 = curmethod.getRegCount();
		// this.ll += "\t" + r0 + " = load i32*, i32** %" + array + "\n";
		// // n.f1.accept(this, argu);
		// String index = n.f2.accept(this, argu);
		// String r1 = curmethod.getRegCount();
		// this.ll += "\t" + r1 + " = getelementptr i32, i32* " + r0 + ", i32 " + index + "\n";
		// // System.out.println("ArrayAssignmentStatement::" + n.f5.accept(this, argu));
		// // System.out.println("ArrayAssignmentStatementarg::" + argu.reg);
		// // n.f3.accept(this, argu);
		// // n.f4.accept(this, argu);
		// String value = n.f5.accept(this, argu);
		// // String r2 = curmethod.getRegCount();
		// this.ll += "\tstore i32 " + value + ", i32* " + r1 + "\n"; 
		// // n.f6.accept(this, argu);
		// return null;

		VariableType tmpvar;
		boolean variableFound = false;
		String tempreg = "";
		if ((tmpvar = curmethod.findVar(name)) != null){
			variableFound = true;

			String r1 = curmethod.getRegCount();
			tempreg = r1; 
			this.ll += "\t" + r1 + " = load i32*, i32** %" + tmpvar.getName() + "\n";
			System.out.println("OXI EDW MESA GAMW " + name);
		}
		else{
			if ((tmpvar = curclass.getVar(name)) != null){
				variableFound = true;
				System.out.println("EDW MESA GAMW " + name);
				String r1 = curmethod.getRegCount();
				String r2 = curmethod.getRegCount();
				String r3 = curmethod.getRegCount();
				tempreg = r3; 
				this.ll += "\t" + r1 + " = getelementptr i8, i8* %this, i32 " + (tmpvar.getOffset() + 8) + "\n";
				this.ll += "\t" + r2 + " = bitcast i8* " + r1 + " to i32**\n";
				this.ll += "\t" + r3 + " = load i32*, i32** " + r2 + "\n";
			}
			else{
				String parentName = curclass.getParentName();
				ClassType tmpClass;
				while (parentName != null){
					tmpClass = symtable.getClass(parentName);
					if ((tmpvar = tmpClass.getVar(name)) != null){
						variableFound = true;

						String r1 = curmethod.getRegCount();
						String r2 = curmethod.getRegCount();
						String r3 = curmethod.getRegCount();
						tempreg = r3; 
						this.ll += "\t" + r1 + " = getelementptr i8, i8* %this, i32 " + (tmpvar.getOffset() + 8) + "\n";
						this.ll += "\t" + r2 + " = bitcast i8* " + r1 + " to i32**\n"; 
						this.ll += "\t" + r3 + " = load i32*, i32** " + r2 + "\n";
						break;
					}
					parentName = tmpClass.getParentName();
				}
			}
		} 

		if (!variableFound)
			throw new Exception("ArrayAssignmentStatement::Variable not found");
		String exp1 = n.f2.accept(this, argu);
		String exp2 = n.f5.accept(this, argu);
		System.out.println(curmethod.getName() + "LALA 1" +exp1 + "  2 -> " + exp2);
		String r4 = curmethod.getRegCount();
		// String r5 = curmethod.getRegCount();
		String type = tmpvar.typeToLLVM(tmpvar.getType());
		// this.ll += "\t" + r4 + " = getelementptr " + type + ", " + type + "* " + tempreg + ", " + type + " " + exp1 + "\n";
		this.ll += "\t" + r4 + " = getelementptr i32, i32* " + tempreg + ", i32 "+ exp1 + "\n";
		this.ll += "\tstore i32 " + exp2 + ", i32* " + r4 + "\n";
		// String r4 = curmethod.getRegCount();
		// String r5 = curmethod.getRegCount();
		// this.ll += "\t" + 
		return null;
	}

	/**
	* f0 -> "if"
	* f1 -> "("
	* f2 -> Expression()
	* f3 -> ")"
	* f4 -> Statement()
	* f5 -> "else"
	* f6 -> Statement()
	*/
	public String visit(IfStatement n, StoreTypes argu) throws Exception {
		// allocate registers now in order to match up register names with given example code
		String l0 = ifLabel();
		String l1 = ifLabel();
		String l2 = ifLabel();
		System.out.println("iflabel "+l1);
		String res1 = n.f2.accept(this, argu);
		System.out.println("IFSTMT::"+res1); 
		this.ll += "\tbr i1 " + res1 + ", label %" + l0 + ", label %" + l1 + "\n\n";
		this.ll += l0 + ": \n";
		n.f4.accept(this, argu);
		this.ll += "\n\tbr label %" + l2 + "\n\n";
		this.ll += l1 + ": \n";
		n.f6.accept(this, argu);
		this.ll += "\n\tbr label %" + l2 + "\n\n";
		this.ll += l2 + ": \n";
		
		// if (!n.f2.accept(this, argu).equals("boolean"))
		// 	throw new Exception("IfStatement::Expression type must be Boolean");
		// System.out.println("^^ "+n.f4.accept(this, argu));
		// System.out.println("^^^ "+n.f6.accept(this, argu));
		return null;
	}

	/**
	* f0 -> "while"
	* f1 -> "("
	* f2 -> Expression()
	* f3 -> ")"
	* f4 -> Statement()
	*/
	public String visit(WhileStatement n, StoreTypes argu) throws Exception {
		
		String l0 = loopLabel();
		String l1 = loopLabel();
		String l2 = loopLabel();
		this.ll += "\tbr label %" + l0 + "\n";
		this.ll += "\t" + l0 + ":\n";
		String cond = n.f2.accept(this, argu);
		this.ll += "\tbr i1 " + cond + ", label %" + l1 + ", label %" + l2 + "\n\n";
		this.ll += "\t" + l1 + ":\n";
		String stmt = n.f4.accept(this, argu);
		System.out.println("WhileStatement::" + argu.reg);
		this.ll += "\n\tbr label %" + l0 + "\n\n";
		this.ll += "\t" + l2 + ":\n";
		// String r0 = curmethod.getRegCount();
		// this.ll += "\t" + r0 + " = "
		// n.f0.accept(this, argu);
		// n.f1.accept(this, argu);
		// n.f2.accept(this, argu);
		// n.f3.accept(this, argu);
		// n.f4.accept(this, argu);
		
		return null;
	}

	/**
	* f0 -> "System.out.println"
	* f1 -> "("
	* f2 -> Expression()
	* f3 -> ")"
	* f4 -> ";"
	*/
	public String visit(PrintStatement n, StoreTypes argu) throws Exception {
		String expr = n.f2.accept(this, argu);
		this.ll += "\tcall void (i32) @print_int(i32 " + expr + ")\n";
		return null;
	}

	/**
	* f0 -> AndExpression()
	*   | CompareExpression()
	*   | PlusExpression()
	*   | MinusExpression()
	*   | TimesExpression()
	*   | ArrayLookup()
	*   | ArrayLength()
	*   | MessageSend()
	*   | Clause()
	*/
	public String visit(Expression n, StoreTypes argu) throws Exception {
		// System.out.println("WHICH "+n.f0.which);
		System.out.println("EXPRE which "+ n.f0.which);
		String reg;
		if (argu != null)
			reg = n.f0.accept(this, argu);
		else {
			StoreTypes new_argu = new StoreTypes();
			// argu = new StoreTypes();
			reg = n.f0.accept(this, new_argu);
		}

		System.out.println(reg + "WHICH "+n.f0.which);
		return reg;
	}

	/**
	* f0 -> Expression()
	* f1 -> ExpressionTail()
	*/
	public String visit(ExpressionList n, StoreTypes argu) throws Exception {
		// add parameter type to list 
		String r = n.f0.accept(this, argu);
		System.out.println("EXPRLIST " + r);
		argu.addType(r);
		n.f1.accept(this, argu);
		return null;
	}

	/**
	* f0 -> ( ExpressionTerm() )*
	*/
	public String visit(ExpressionTail n, StoreTypes argu) throws Exception {
		return n.f0.accept(this, argu);
	}

	/**
	* f0 -> ","
	* f1 -> Expression()
	*/
	public String visit(ExpressionTerm n, StoreTypes argu) throws Exception {
		// add parameter type to list 
		String r = n.f1.accept(this, argu);
		argu.addType(r);
		return null;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "+"
	* f2 -> PrimaryExpression()
	*/
	public String visit(PlusExpression n, StoreTypes argu) throws Exception {
		String term1 = n.f0.accept(this, argu);
		// n.f1.accept(this, argu);
		String term2 = n.f2.accept(this, argu);
		String r0 = curmethod.getRegCount();
		this.ll += "\t" + r0 + " = add i32 " + term1 + ", " + term2 + "\n";
		return r0;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "-"
	* f2 -> PrimaryExpression()
	*/
	public String visit(MinusExpression n, StoreTypes argu) throws Exception {
		String type1 = n.f0.accept(this, argu);
		String type2 = n.f2.accept(this, argu);
		String sub = "sub i32 " + type1 + ", " + type2 + "\n";
		String r = curmethod.getRegCount();
		this.ll += "\t" + r + " = " + sub;
		System.out.println("MinusExpression::" + type1 + " , " + type2);
		// if (!type1.equals("int") || !type1.equals(n.f2.accept(this, argu)))
		// 	throw new Exception("MinusExpression::Error, type must be int");

		return r;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "*"
	* f2 -> PrimaryExpression()
	*/
	public String visit(TimesExpression n, StoreTypes argu) throws Exception {
		String type1 = n.f0.accept(this, argu);
		String type2 = n.f2.accept(this, argu);
		String mul = "mul i32 " + type1 + ", " + type2 + "\n"; 
		String r = curmethod.getRegCount();
		this.ll += "\t" + r + " = " + mul;
		// if (!type1.equals("int") || !type1.equals(n.f2.accept(this, argu)))
		// 	throw new Exception("TimesExpression::Error, type must be int");

		return r;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "<"
	* f2 -> PrimaryExpression()
	*/
	public String visit(CompareExpression n, StoreTypes argu) throws Exception {
		String type1 = n.f0.accept(this, argu);
		String type2 = n.f2.accept(this, argu);
		String cmp = "icmp slt i32 " + type1 + ", " + type2 + "\n";
		String r0 = curmethod.getRegCount();
		this.ll += "\t" + r0 + " = " + cmp; 
		// if (!type1.equals("int") || !type1.equals(n.f2.accept(this, argu)))
		// 	throw new Exception("CompareExpression::Error, type must be int");
			
		return r0;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "["
	* f2 -> PrimaryExpression()
	* f3 -> "]"
	*/
	public String visit(ArrayLookup n, StoreTypes argu) throws Exception {
		// System.out.println("ArrayLookup1::" + n.f0.accept(this, argu));
		// System.out.println("ArrayLookup2::" + n.f2.accept(this, argu));
		String array = n.f0.accept(this, argu);
		// n.f1.accept(this, argu);
		String index = n.f2.accept(this, argu);
		String r0 = curmethod.getRegCount();
		String r1 = curmethod.getRegCount();
		this.ll += "\t" + r0 + " = getelementptr i32, i32* " + array + ", i32 " + index + "\n";
		this.ll += "\t" + r1 + " = load i32, i32* " + r0 + "\n";
		// n.f3.accept(this, argu);
		
		return r1;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "."
	* f2 -> Identifier()
	* f3 -> "("
	* f4 -> ( ExpressionList() )?
	* f5 -> ")"
	*/
	public String visit(MessageSend n, StoreTypes argu) throws Exception {
		StoreTypes argument = new StoreTypes();
		// argu = new StoreTypes();
		String classname = n.f0.accept(this,argument);
		System.out.println("***MSGSENT "+argument.reg);
		ClassType tmpclass = symtable.getClass(argument.reg);
		if (tmpclass == null) {
			throw new Exception("MessageSend::CLASS NULL");
		}
		String r0 = curmethod.getRegCount();
		String r1 = curmethod.getRegCount();
		String r2 = curmethod.getRegCount();
		String r3 = curmethod.getRegCount();
		String r4 = curmethod.getRegCount();

		// argument.reg = null;
		String methodname = n.f2.f0.toString();
		MethodType tmpMethod;
		if ((tmpMethod = tmpclass.getMethod(methodname)) == null)
			throw new Exception("MessageSend::Method NULL");
		
		argu.reg = tmpMethod.getType();
		this.ll += "\t; " + argument.reg + "." + methodname + " : " + (tmpMethod.getOffset()/8) + "\n"; 
		this.ll += "\t" + r0 + " = bitcast i8* " + classname + " to i8***\n";
		this.ll += "\t" + r1 + " = load i8**, i8*** " + r0 + "\n";
		this.ll += "\t" + r2 + " = getelementptr i8*, i8** " + r1 + ", i32 " + (tmpMethod.getOffset()/8) + "\n";
		this.ll += "\t" + r3 + " = load i8*, i8** " + r2 + "\n";
		this.ll += "\t" + r4 + " = bitcast i8* " + r3 + " to " + tmpMethod.typeToLLVM(tmpMethod.getType()) + " (i8*";
		ArrayList<VariableType> params = tmpMethod.getParams();
		int sz = params.size();
		if (sz == 0)
			 this.ll += ")*\n";
		else{
			for (VariableType vari : params){
					this.ll += ", " + vari.typeToLLVM(vari.getType());
			}
			this.ll += ")*\n";
		}
		String r5 = curmethod.getRegCount();
		// String fname = n.f2.f0.toString();

		StoreTypes args2 = new StoreTypes();
		n.f4.accept(this, args2);
		this.ll += "\t" + r5 + " = call " + tmpMethod.typeToLLVM(tmpMethod.getType()) + " " + r4 + "(i8* " + classname;
		for (int i=0;i<args2.types.size();i++){
			VariableType vari = params.get(i); 
			this.ll += ", " + vari.typeToLLVM(vari.getType()) + " " + args2.types.get(i);
		}
		this.ll += ")\n";
		// argument.type = null;
		// FunctionInfo tempfunction;
		// if ((tempfunction = tempclass.getFunction(fname)) == null) {
		// if (tempclass.getParentName() != null) {
		// ClassInfo parentClass = table.getClass(tempclass.getParentName());
		// FunctionInfo parentFunction;
		// if ((tempfunction = table.getRecursiveFunctionCheck(parentClass,fname)) == null) {
		// throw new Exception("problem");
		// }
		// }
		// else
		// throw new Exception("problem");
		// }
		// int offset = tempfunction.getOffset();
		// ll += "\t; " + tempclass.getName() + "." + fname + " : " + offset + "\n";
		// ll += "\t" + reg1 + " = bitcast i8* " + regres + " to i8***" + "\n";
		// ll += "\t" + reg2 + " = load i8**, i8*** " + reg1 + "\n";
		// ll += "\t" + reg3 + " = getelementptr i8*, i8** " + reg2 + ", i32 " + offset + "\n";
		// ll += "\t" + reg4 + " = load i8*, i8** " + reg3 + "\n";
		// String ftype = tempfunction.getType();
		// argu.type = ftype;
		// String type = table.typeTransform(ftype);
		// ftype = "";
		// ftype += type;
		// ll += "\t" + reg5 + " = bitcast i8* " + reg4 + " to " + type + " (i8*";
		// String reg6 = curmethod.getRegCount();
		// ArrayList<VariableInfo> parameters = tempfunction.getParameters();
		// type = "";
		// String vtype = "";
		// ArrayList<String> llparameterTypeList = new ArrayList<String>();
		// for (VariableInfo var : parameters) {
		// vtype = var.getType();
		// type = curmethod.typeTransform(vtype);
		// llparameterTypeList.add(type);

		// ll += ", " + type;  
		// }
		// ll += ")*";
		// StoreTypes checklist = new StoreTypes();
		// n.f4.accept(this, checklist);

		// ll += "\n\t" + reg6 + " = call " + ftype + " " + reg5 + "(i8* " + regres;
		// if (checklist.types.size() != tempfunction.getParameters().size())
		// throw new Exception("Wrong parameters number");

		// for (int i = 0;i < checklist.types.size();++i) {
		// String currentreg = checklist.types.get(i);
		// String currenttype = llparameterTypeList.get(i);
		// ll += ", " + currenttype + " " + currentreg;
		// }
		// ll += ")";  

		return r5;
		// return reg6;

	}

	/**
	* f0 -> NotExpression()
	*       | PrimaryExpression()
	*/
	public String visit(Clause n, StoreTypes argu) throws Exception {
		// n.f0.accept(this, argu);
		// System.out.println("Clause "+argu.reg);
		return n.f0.accept(this, argu);
	}

	/**
	* f0 -> IntegerLiteral()
	*       | TrueLiteral()
	*       | FalseLiteral()
	*       | Identifier()
	*       | ThisExpression()
	*       | ArrayAllocationExpression()
	*       | AllocationExpression()
	*       | BracketExpression()
	*/
	public String visit(PrimaryExpression n, StoreTypes argu) throws Exception {
		String name = n.f0.accept(this, argu);		
		System.out.println("NAME "+name + " which " + n.f0.which);
		String r0 = "";
		// which -> find which rule was chosen
		// 3 is Identifier(), only here must check for type
		// every other rule returns type
		if (n.f0.which == 3){
			VariableType tmpvar;
			boolean variableFound = false;
			boolean classvariable = false;
			if ((tmpvar = curmethod.findVar(name)) != null){
				variableFound = true;

				r0 = curmethod.getRegCount();
				String type = curmethod.typeToLLVM(tmpvar.getType());
				this.ll += "\t" + r0 + " = load " + type + ", " + type + "* %" + name + "\n"; 
				System.out.println("(1) " + r0 + " name is " + tmpvar.getName());
				// if (argu != null)
				// 	argu.reg = tmpvar.getType();
			}
			else{
				if ((tmpvar = curclass.getVar(name)) != null){
					variableFound = true;
					classvariable = true;
					// String r1 = curmethod.getRegCount();
					// String r2 = curmethod.getRegCount();
					// r0 = curmethod.getRegCount();
					// this.ll += "\t" + r1 + " = getelementptr i8, i8* %this, i32 " + (tmpvar.getOffset() + 8) + "\n";
					// String type = tmpvar.typeToLLVM(tmpvar.getType());
					// this.ll += "\t" + r2 + " = bitcast i8* " + r1 + " to " + type + "*\n";
					// this.ll += "\t" + r0 + " = load " + type + ", " + type + "* " + r2 + "\n";
					// System.out.println("(2) getelem " + r1 + " bitcast " + r2 + " load " + r0 + " name is " + tmpvar.getName());
				}
				else{
					String parentName = curclass.getParentName();
					ClassType tmpClass;
					while (parentName != null){
						tmpClass = symtable.getClass(parentName);
						if ((tmpvar = tmpClass.getVar(name)) != null){
							variableFound = true;
							classvariable = true;
							// String r1 = curmethod.getRegCount();
							// String r2 = curmethod.getRegCount();
							// r0 = curmethod.getRegCount();
							// String type = tmpvar.typeToLLVM(tmpvar.getType());
							// this.ll += "\t" + r1 + " = getelementptr i8, i8* %this, i32 " + (tmpvar.getOffset() + 8) + "\n";
							// this.ll += "\t" + r2 + " = bitcast i8* " + r1 + " to " + type + "*\n";
							// this.ll += "\t" + r0 + " = load " + type + ", " + type + "* " + r2 + "\n";
							// System.out.println("(3) getelem " + r1 + " bitcast " + r2 + " load " + r0 + " name is " + tmpvar.getName());
							break;
						}
						parentName = tmpClass.getParentName();
					}
				}

				if (classvariable){
					String r1 = curmethod.getRegCount();
					String r2 = curmethod.getRegCount();
					r0 = curmethod.getRegCount();
					String type = tmpvar.typeToLLVM(tmpvar.getType());
					this.ll += "\t" + r1 + " = getelementptr i8, i8* %this, i32 " + (tmpvar.getOffset() + 8) + "\n";
					this.ll += "\t" + r2 + " = bitcast i8* " + r1 + " to " + type + "*\n";
					this.ll += "\t" + r0 + " = load " + type + ", " + type + "* " + r2 + "\n";
				}
			}

			if (!variableFound)
				throw new Exception("PrimExpr::Variable doesn't have a declared type");

			if (argu != null)
				argu.reg = tmpvar.getType();

			if (argu!=null)
				System.out.println("RET PR " + name + " and  "+ argu.reg);
			else
				System.out.println("RET PR " + name);
			return r0;
		}

		// if (argu != null)
		// 	argu.reg = name;
		System.out.println("RET PR2 " + name);
		return name;
	}

	/**
	* f0 -> "this"
	*/
	public String visit(ThisExpression n, StoreTypes argu) throws Exception {
		argu.reg = curclass.getName();
		System.out.println("ThisExpr::" + curclass.getName() + " and argu " + argu.reg);
		return "%this";
		// return curclass.getName();
	}

	/**
	* f0 -> "new"
	* f1 -> Identifier()
	* f2 -> "("
	* f3 -> ")"
	*/
	public String visit(AllocationExpression n, StoreTypes argu) throws Exception {
		String name = n.f1.f0.toString();
		ClassType tmpclass = this.symtable.getClass(name);
		// System.out.println("ONOMA "+tmpclass.getName());
		// System.out.println("CURMETHOD "+curmethod.getName());
		String r0 = curmethod.getRegCount();
		String r1 = curmethod.getRegCount();
		String r2 = curmethod.getRegCount();
		int methods_size = tmpclass.getMethods().size(); 
		this.ll += "\t" + r0 + " = call i8* @calloc(i32 " + 1 + ", i32 " + (tmpclass.getVarOffset()+8) + ")\n" ; 
		this.ll += "\t" + r1 + " = bitcast i8* " + r0 + " to i8***\n";
		this.ll += "\t" + r2 + " = getelementptr [" + methods_size +  " x i8*], [" + methods_size + " x i8*]* @." + tmpclass.getName() + "_vtable, i32 0, i32 0\n";
		this.ll += "\tstore i8** " + r2 + ", i8*** " + r1 + "\n";
		// System.out.println(ll);
		// will need it in future
		argu.reg = name;
		return r0;
	}

	/**
	* f0 -> "new"
	* f1 -> "int"
	* f2 -> "["
	* f3 -> Expression()
	* f4 -> "]"
	*/
	public String visit(ArrayAllocationExpression n, StoreTypes argu) throws Exception {
		// R _ret=null;
		// System.out.println("ArrayAllocationExpr::" + n.f3.accept(this, argu));
		// System.out.println("ArrayAllocationExprargu::" + argu.reg);
		
		String ret = n.f3.accept(this, argu);
		String r0 = curmethod.getRegCount();
		String r1 = curmethod.getRegCount();
		int sz;
		if (argu.reg.equals("int"))
			sz = 4;
		else if (argu.reg.equals("boolean"))
			sz = 4;
		else
			sz = 8;
		this.ll += "\t" + r0 + " = call i8* @calloc(i32 " + sz + ", i32 " + ret + ")\n";
		this.ll += "\t" + r1 + " = bitcast i8* " + r0 + " to i32*\n";
		
		// return type
		argu.reg = "int[]";
		// return register
		return r1;
	}

	/**
	* f0 -> "!"
	* f1 -> Clause()
	*/
	public String visit(NotExpression n, StoreTypes argu) throws Exception {
		String clause = n.f1.accept(this, argu);
		String r0 = curmethod.getRegCount();
		this.ll += "\t" + r0 + " = xor i1 1, " + clause + "\n";
		return r0;
	}

	/**
	* f0 -> "("
	* f1 -> Expression()
	* f2 -> ")"
	*/
	public String visit(BracketExpression n, StoreTypes argu) throws Exception {
		// String type = n.f1.accept(this, argu);
		return n.f1.accept(this, argu);
	}

	/**
	* f0 -> ArrayType()
	*       | BooleanType()
	*       | IntegerType()
	*       | Identifier()
	*/
	public String visit(Type n, StoreTypes argu) throws Exception {
		String type =  n.f0.accept(this, argu);
		// if (type.equals("int") == false && type.equals("boolean") == false && type.equals("int[]") == false && symtable.getClass(type) == null)
		// 	throw new Exception("Not accepted type");
		return type;
	}

	/**
	* f0 -> "int"
	* f1 -> "["
	* f2 -> "]"
	*/
	public String visit(ArrayType n,StoreTypes argu) throws Exception {
		return "int[]";
	}

	/**
	* f0 -> "boolean"
	*/
	public String visit(BooleanType n,StoreTypes argu) throws Exception {
		return "boolean";
	}

	/**
	* f0 -> "int"
	*/
	public String visit(IntegerType n,StoreTypes argu) throws Exception {
		return "int";
	}

	/**
	* f0 -> <IDENTIFIER>
	*/
	public String visit(Identifier n, StoreTypes argu) throws Exception {
		return n.f0.toString();
	}

	/**
	* f0 -> <INTEGER_LITERAL>
	*/
	public String visit(IntegerLiteral n, StoreTypes argu) throws Exception {
		return n.f0.toString();
	}

	/**
	* f0 -> "true"
	*/
	public String visit(TrueLiteral n, StoreTypes argu) throws Exception {
		return "1";
	}

	/**
	* f0 -> "false"
	*/
	public String visit(FalseLiteral n, StoreTypes argu) throws Exception {
		return "0";
	}

}