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

	// returns produced ll code
	public String getLLCode(){
		return this.ll;
	}
	
	// functions to return relative register names in order to make it easier to read ll code
	// if label
	public String ifLabel(){
		int reg_count = curmethod.getRCount();
		return "if" + reg_count;
	}

	// loop label
	public String loopLabel(){
		int reg_count = curmethod.getRCount();
		return "loop" + reg_count;
	}

	// and label
	public String andLabel(){
		int reg_count = curmethod.getRCount();
		return "and" + reg_count;
	}

	// out of bounds label
	public String oobLabel(){
		int reg_count = curmethod.getRCount();
		return "oob" + reg_count;
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
		curclass = symtable.getClass(n.f1.f0.toString());
		curmethod = curclass.getMethod(n.f6.toString());

		this.ll += "define i32 @main() {\n";
		n.f11.accept(this, argu);
		n.f14.accept(this, argu);
		n.f15.accept(this, argu);

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
		curclass = symtable.getClass(n.f1.f0.toString());
		
		n.f3.accept(this, argu);
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
		// allocate space on the stack of the current function for local variables
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
		}
		
		this.ll += ") {\n" + paramcode;
		// n.f4.accept(this, argu);
		n.f7.accept(this, argu);
		n.f8.accept(this, argu);
		
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
		StoreTypes new_argu;
		if (argu != null)
			n.f0.accept(this, argu);
		else {
			new_argu = new StoreTypes();
			n.f0.accept(this, new_argu);
		}
		return null;
	}

	/**
	* f0 -> "{"
	* f1 -> ( Statement() )*
	* f2 -> "}"
	*/
	public String visit(Block n, StoreTypes argu) throws Exception {
		n.f1.accept(this, argu);
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
		
		// check to identify variable
		VariableType tmpvar;
		boolean variableFound = false;
		boolean classvariable = false;
		if ((tmpvar = curmethod.findVar(name)) != null){
			variableFound = true;
			String type = tmpvar.typeToLLVM(tmpvar.getType());
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

			// if variable is a class variable or from a parent class , use offset to take it
			if (classvariable){
				String type = tmpvar.typeToLLVM(tmpvar.getType());
				String r0 = curmethod.getRegCount();
				String r1 = curmethod.getRegCount();
				this.ll += "\t" + r0 + " = getelementptr i8, i8* %this, i32 " + (tmpvar.getOffset() + 8) + "\n";
				this.ll += "\t" + r1 + " = bitcast i8* " + r0 + " to " + type + "*\n";
				this.ll += "\tstore " + type + " " + reg + ", " + type + "* " + r1 + "\n";
			}
		}

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
		VariableType tmpvar;
		boolean variableFound = false;
		boolean classvariable = false;
		String arrayptr = "";
		String len = curmethod.getRegCount();
		if ((tmpvar = curmethod.findVar(name)) != null){
			variableFound = true;

			String r1 = curmethod.getRegCount();
			arrayptr = r1; 
			this.ll += "\t" + r1 + " = load i32*, i32** %" + tmpvar.getName() + "\n";
			// extra load in order to take first array index with array length as value
			this.ll += "\t" + len + " = load i32 , i32* " + r1 + "\n";
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
				String r1 = curmethod.getRegCount();
				String r2 = curmethod.getRegCount();
				String r3 = curmethod.getRegCount();
				arrayptr = r3; 
				this.ll += "\t" + r1 + " = getelementptr i8, i8* %this, i32 " + (tmpvar.getOffset() + 8) + "\n";
				this.ll += "\t" + r2 + " = bitcast i8* " + r1 + " to i32**\n";
				this.ll += "\t" + r3 + " = load i32*, i32** " + r2 + "\n";
				// extra load in order to take first array index with array length as value
				this.ll += "\t" + len + " = load i32, i32* " + r3 + "\n";  
			}
		} 

		if (!variableFound)
			throw new Exception("ArrayAssignmentStatement::Variable not found");
		String exp1 = n.f2.accept(this, argu);
		String r4 = curmethod.getRegCount();

		// check if index is out of bounds , i use ult in order to make only one check
		// ult is unsigned check , so i dont need to calculate if index is negative because if it is in signed mode, it transforms to a big int in unsigned mode
		this.ll += "\t" + r4 + " = icmp ult i32 " + exp1 + ", " + len + "\n";
		String oob1 = oobLabel();
		String oob2 = oobLabel();
		String oob3 = oobLabel();
		this.ll += "\tbr i1 " + r4 + ", label %" +  oob1 + ", label %" + oob2 + "\n";
		this.ll += "\t" + oob1 + ":\n";
		String r5 = curmethod.getRegCount();
		String r6 = curmethod.getRegCount();
		this.ll += "\t" + r5 + " = add i32 " + exp1 + ", 1\n";  
		this.ll += "\t" + r6 + " = getelementptr i32, i32* " + arrayptr + ", i32 "+ r5 + "\n";
		
		String exp2 = n.f5.accept(this, argu);
		
		this.ll += "\tstore i32 " + exp2 + ", i32* " + r6 + "\n";
		this.ll += "\t br label %" + oob3 + "\n";
		this.ll += "\t" + oob2 + ":\n";
		this.ll += "\tcall void @throw_oob()\n";
		this.ll += "\t br label %" + oob3 + "\n";
		this.ll += "\t" + oob3 + ":\n";

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
		String res1 = n.f2.accept(this, argu);
		
		this.ll += "\tbr i1 " + res1 + ", label %" + l0 + ", label %" + l1 + "\n\n";
		this.ll += l0 + ": \n";
		n.f4.accept(this, argu);
		this.ll += "\n\tbr label %" + l2 + "\n\n";
		this.ll += l1 + ": \n";
		n.f6.accept(this, argu);
		this.ll += "\n\tbr label %" + l2 + "\n\n";
		this.ll += l2 + ": \n";
		
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
		
		this.ll += "\n\tbr label %" + l0 + "\n\n";
		this.ll += "\t" + l2 + ":\n";
		
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
		String reg;
		if (argu == null)
			argu = new StoreTypes();

		reg = n.f0.accept(this, argu);

		return reg;
	}

	/**
	* f0 -> Expression()
	* f1 -> ExpressionTail()
	*/
	public String visit(ExpressionList n, StoreTypes argu) throws Exception {
		// add parameter type to list 
		String r = n.f0.accept(this, argu);
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
	* f0 -> Clause()
	* f1 -> "&&"
	* f2 -> Clause()
	*/
	public String visit(AndExpression n, StoreTypes argu) throws Exception {
		String clause1 = n.f0.accept(this, argu);
		String r0 = curmethod.getRegCount();
		String l0 = andLabel();
		String l1 = andLabel();
		String l2 = andLabel();
		String l3 = andLabel();

		this.ll += "\t\tbr i1 " + clause1 + ", label %" + l1 + ", label %" + l2 + "\n";
		this.ll += "\t" + l1 + ":\n";
		String clause2 = n.f2.accept(this, argu);
		this.ll += "\t\tbr label %" + l2 + "\n"; 
		this.ll += "\t" + l2 + ":\n";
		this.ll += "\t\tbr label %" + l3 + "\n";
		this.ll += "\t" + l3 + ":\n";
		// short-circuiting with phi func , means that if clause1 is 0 we dont need to check clause2
		this.ll += "\t\t" + r0 + " = " + "phi i1 [0, %" + l1 + "], [" + clause2 + ", %" + l2 + "]\n";
		
		// n.f0.accept(this, argu);
		// n.f1.accept(this, argu);
		// n.f2.accept(this, argu);
		return r0;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "+"
	* f2 -> PrimaryExpression()
	*/
	public String visit(PlusExpression n, StoreTypes argu) throws Exception {
		String term1 = n.f0.accept(this, argu);
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
			
		return r0;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "["
	* f2 -> PrimaryExpression()
	* f3 -> "]"
	*/
	public String visit(ArrayLookup n, StoreTypes argu) throws Exception {
		String array = n.f0.accept(this, argu);
		String index = n.f2.accept(this, argu);
		
		String r0 = curmethod.getRegCount();
		String r1 = curmethod.getRegCount();
		String r2 = curmethod.getRegCount();
		String r3 = curmethod.getRegCount();
		String r4 = curmethod.getRegCount();

		String oob1 = oobLabel();
		String oob2 = oobLabel();
		String oob3 = oobLabel();

		// check bounds
		this.ll += "\t" + r0 + " = load i32, i32* " + array + "\n";
		this.ll += "\t" + r1 + " = icmp ult i32 " + index + ", " + r0 + "\n";
		this.ll += "\tbr i1 " + r1 + ", label %" + oob1 + ", label %" + oob2 + "\n";
		this.ll += "\t" + oob1 + ":\n";
		this.ll += "\t" + r2 + " = add i32 " + index + ", 1\n"; 
		this.ll += "\t" + r3 + " = getelementptr i32, i32* " + array + ", i32 " + r2 + "\n";
		this.ll += "\t" + r4 + " = load i32, i32* " + r3 + "\n";
		this.ll += "\tbr label %" + oob3 + "\n";

		this.ll += "\t" + oob2 + ":\n";
		this.ll += "\tcall void @throw_oob()\n";
		this.ll += "\tbr label %" + oob3 + "\n";
		this.ll += "\t" + oob3 + ":\n";

		// n.f3.accept(this, argu);
		
		return r4;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "."
	* f2 -> "length"
	*/
	public String visit(ArrayLength n, StoreTypes argu) throws Exception {
		String r1 = curmethod.getRegCount();
		
		String array = n.f0.accept(this, argu);
		// loads in register the length of the array which is in first cell of array
		// when allocating array, total_length  = length + 1 in order to store length as variable
		this.ll += "\t" + r1 + " = load i32, i32* " + array + "\n";

		// isws xreiazetai argu.reg = "int"
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
		// use multiple instances of StoreTypes to avoid reinitialize it
		StoreTypes new_argu = new StoreTypes();
		String classname = n.f0.accept(this,new_argu);
		ClassType tmpclass = symtable.getClass(new_argu.reg);
		if (tmpclass == null) {
			throw new Exception("MessageSend::CLASS NULL");
		}
		String r0 = curmethod.getRegCount();
		String r1 = curmethod.getRegCount();
		String r2 = curmethod.getRegCount();
		String r3 = curmethod.getRegCount();
		String r4 = curmethod.getRegCount();

		String methodname = n.f2.f0.toString();
		MethodType tmpMethod;
		
		if ((tmpMethod = tmpclass.getMethod(methodname)) == null){
			// check if exist in parentclass
			if (tmpclass.getParentName() != null)
			{
				tmpclass = symtable.getClass(tmpclass.getParentName());
				boolean methodFound = false;
				if ((tmpMethod = tmpclass.getMethod(methodname)) != null)
					methodFound = true;
				else{
					// check every parent until there is noone left
					String parentName = tmpclass.getParentName();
					while (parentName != null){
						tmpclass = symtable.getClass(parentName);
						if ((tmpMethod = tmpclass.getMethod(methodname)) != null){
							methodFound = true;
							break;
						}
						parentName = tmpclass.getParentName();
					}
				}
				if (!methodFound)
					throw new Exception("MessageSend::Method has not been declared");
			}
			else
				throw new Exception("MessageSend::Method has not been declared");

		}


		argu.reg = tmpMethod.getType();
		this.ll += "\t; " + new_argu.reg + "." + methodname + " : " + (tmpMethod.getOffset()/8) + "\n"; 
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

		StoreTypes new_argu2 = new StoreTypes();
		n.f4.accept(this, new_argu2);
		this.ll += "\t" + r5 + " = call " + tmpMethod.typeToLLVM(tmpMethod.getType()) + " " + r4 + "(i8* " + classname;
		for (int i=0;i<new_argu2.types.size();i++){
			VariableType vari = params.get(i); 
			this.ll += ", " + vari.typeToLLVM(vari.getType()) + " " + new_argu2.types.get(i);
		}
		this.ll += ")\n";

		return r5;
	}

	/**
	* f0 -> NotExpression()
	*       | PrimaryExpression()
	*/
	public String visit(Clause n, StoreTypes argu) throws Exception {
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

			return r0;
		}

		return name;
	}

	/**
	* f0 -> "this"
	*/
	public String visit(ThisExpression n, StoreTypes argu) throws Exception {
		argu.reg = curclass.getName();
		return "%this";
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
		
		String r0 = curmethod.getRegCount();
		String r1 = curmethod.getRegCount();
		String r2 = curmethod.getRegCount();
		int methods_size = tmpclass.getVtableSize(); 
		this.ll += "\t" + r0 + " = call i8* @calloc(i32 " + 1 + ", i32 " + (tmpclass.getVarOffset()+8) + ")\n" ; 
		this.ll += "\t" + r1 + " = bitcast i8* " + r0 + " to i8***\n";
		this.ll += "\t" + r2 + " = getelementptr [" + methods_size +  " x i8*], [" + methods_size + " x i8*]* @." + tmpclass.getName() + "_vtable, i32 0, i32 0\n";
		this.ll += "\tstore i8** " + r2 + ", i8*** " + r1 + "\n";
		
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
		String ret = n.f3.accept(this, argu);
		String r0 = curmethod.getRegCount();
		String r1 = curmethod.getRegCount();
		String r2 = curmethod.getRegCount();
		String r3 = curmethod.getRegCount();
		String oob0 = oobLabel();
		String oob1 = oobLabel();
		// check if len is negative
		this.ll += "\t" + r2 + " = icmp slt i32 " + ret + ", 0\n";
		this.ll += "\tbr i1 " + r2 + ", label %" + oob0 + ", label %" + oob1 + "\n";
		this.ll += "\t" + oob0 + ":\n";
		this.ll += "\tcall void @throw_oob()\n"; 
		this.ll += "\tbr label %" + oob1 + "\n";
		this.ll += "\t" + oob1 + ":\n";
		// increase array length to store extra info (len)
		this.ll += "\t\t" + r3 + " = add i32 " + ret + ", 1\n";  
		this.ll += "\t" + r0 + " = call i8* @calloc(i32 4, i32 " + r3 + ")\n";
		this.ll += "\t" + r1 + " = bitcast i8* " + r0 + " to i32*\n";
		this.ll += "\tstore i32 " + ret + ", i32* " + r1 + "\n";
		
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