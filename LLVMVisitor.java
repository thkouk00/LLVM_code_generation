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
		System.out.println("CLASS DECL "+curclass.getName());
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
		String varname = n.f1.f0.toString();
		
		VariableType tmpvar;
		if ((tmpvar = curmethod.getVar(varname)) == null){
			tmpvar = curclass.getVar(varname);
		}
		this.ll += "\t%" + varname + " = alloca " + tmpvar.typeToLLVM(tmpvar.getType()) + "\n";

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
			paramcode += "\tstore " + lltype + " %." + varname + ", " + lltype + "* %" + varname + "\n\n";
			// load var in register
			// loadparams += "\t%_" + curmethod.getRegCount() + " = load " + lltype + ", " + lltype + "* %" + varname + "\n";
		}
		
		this.ll += ") {\n";
		// n.f4.accept(this, argu);
		n.f7.accept(this, argu);
		n.f8.accept(this, argu);
		// check return type to match with methods type
		String retType = n.f10.accept(this, argu);
		boolean typefound = false;
		String methodtype = curmethod.getType();
		if (methodtype.equals(retType))
			typefound = true;
		else{
			ClassType tmpClass;
			tmpClass = symtable.getClass(retType);
			if (tmpClass != null){
				String parentName = tmpClass.getParentName();
				while (parentName != null){
					if (methodtype.equals(parentName)){
						typefound = true;
						break;
					}
					tmpClass = symtable.getClass(tmpClass.parentName);
					parentName = tmpClass.getParentName();
				}
			}
		}
		
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
			n.f0.accept(this, argument);
		}
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
		this.ll += "\tcall void (i32) @print_int(i32 " + expr + ")";
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
		String reg = n.f1.accept(this, argu);
		argu.addType(reg);
		return null;
	}

	// /**
	// * f0 -> PrimaryExpression()
	// * f1 -> "<"
	// * f2 -> PrimaryExpression()
	// */
	// public String visit(CompareExpression n, StoreTypes argu) throws Exception {
	// 	String type1 = n.f0.accept(this, argu);
	// 	// if (!type1.equals("int") || !type1.equals(n.f2.accept(this, argu)))
	// 	// 	throw new Exception("CompareExpression::Error, type must be int");
			
	// 	// return "%_8";
	// }

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
		String classname = n.f0.accept(this,argument);
		System.out.println("***MSGSENT "+argument.reg);
		ClassType tmpclass = symtable.getClass(classname);
		if (tmpclass == null) {
			throw new Exception("MessageSend::CLASS NULL");
		}
		String r0 = curmethod.getRegCount();
		String r1 = curmethod.getRegCount();
		String r2 = curmethod.getRegCount();
		String r3 = curmethod.getRegCount();
		String r4 = curmethod.getRegCount();

		// argument.reg = null;
		String methodname = n.f2.accept(this, argu);
		MethodType tmpMethod;
		if ((tmpMethod = tmpclass.getMethod(methodname)) == null)
			throw new Exception("MessageSend::Method NULL");
		
		this.ll += "\t; " + classname + "." + methodname + " : " + tmpMethod.getOffset() + "\n"; 
		this.ll += "\t" + r0 + " = bitcast i8* " + argument.reg + " to i8***\n";
		this.ll += "\t" + r1 + " = load i8**, i8*** " + r0 + "\n";
		this.ll += "\t" + r2 + " = getelementptr i8*, i8** " + r1 + ", i32 0\n";
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
		this.ll += "\t" + r5 + " = call " + tmpMethod.typeToLLVM(tmpMethod.getType()) + " " + r4 + "(i8* " + argument.reg;
		// String fname = n.f2.f0.toString();

		argu = new StoreTypes();
		n.f4.accept(this, argu);
		for (int i=0;i<argu.types.size();i++){
			VariableType vari = params.get(i); 
			this.ll += ", " + vari.typeToLLVM(vari.getType()) + " " + argu.types.get(i);
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
		n.f0.accept(this, argu);
		System.out.println("Clause "+argu.reg);
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
		String register = "";
		// which -> find which rule was chosen
		// 3 is Identifier(), only here must check for type
		// every other rule returns type
		if (n.f0.which == 3){
			VariableType tmpvar;
			boolean variableFound = false;
			if ((tmpvar = curmethod.findVar(name)) != null){
				variableFound = true;

				register = curmethod.getRegCount();
				String type = curmethod.typeToLLVM(curmethod.getType());
				this.ll += "\t" + register + " = load " + type + ", " + type + "* %" + name + "\n"; 
				System.out.println("&&&&&&& " + register);
				if (argu != null)
					argu.reg = tmpvar.getType();
			}
			else{
				if ((tmpvar = curclass.getVar(name)) != null)
					variableFound = true;
				else{
					String parentName = curclass.getParentName();
					ClassType tmpClass;
					while (parentName != null){
						tmpClass = symtable.getClass(parentName);
						if ((tmpvar = tmpClass.getVar(name)) != null){
							variableFound = true;
							break;
						}
						parentName = tmpClass.getParentName();
					}
				}
			}

			if (!variableFound)
				throw new Exception("PrimExpr::Variable doesn't have a declared type");

			
			System.out.println("RET PR " + name);
			return register;
		}

		// if (argu != null)
		// 	argu.reg = name;
		System.out.println("RET PR2 " + name);
		return name;
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
		argu.reg = r0;
		return name;
	}

	/**
	* f0 -> ArrayType()
	*       | BooleanType()
	*       | IntegerType()
	*       | Identifier()
	*/
	public String visit(Type n, StoreTypes argu) throws Exception {
		String type =  n.f0.accept(this, argu);
		if (type.equals("int") == false && type.equals("boolean") == false && type.equals("int[]") == false && symtable.getClass(type) == null)
			throw new Exception("Not accepted type");
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


}