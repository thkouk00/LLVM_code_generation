import java.util.*;

// Symbol Table Structure (LinkedHashMaps for objects like classes,methods,variables) 
// I chose LinkedHashMap instead of HashMap because insertion order matters
// For storing types in function calls in order to compare them with function's signature, i used a class with an arraylist that stores Strings. Arraylist is faster than a vector. 

// Classes (ProgramFile)
//    |
//    | -----------Contains all classes from input file

// Class 
//    |
//    | -----------Every Class has name and parentName(if there is inheritance), class variables, class methods and offsets that keep track of current count for vars and methods

// 		   	|
//		Class Methods
//		    |
//    		| -----------Every Method has name and return type, variables and parameters

// 		   	|
// 		Class Variables
//    		|
//    		| -----------Every Variable has name and type

public class SymbolTable{
	// linkedhashmap used because we care about insertion order
	LinkedHashMap <String, ClassType> classes;

	public SymbolTable(){
		this.classes = new LinkedHashMap<String, ClassType>();
	}

	public boolean addClass(String name, String parentName){
		if (ContainsClass(name))
			return false;

		// parentName is null
		ClassType newClass = new ClassType(name, parentName);
		// update Symbol Table
		classes.put(name, newClass);
		// System.out.println("ST::Inserting "+name+" class with parent "+parentName+" class");
		return true;
	}

	// differentiate from addClass because new class inherits parents offsets
	public boolean addClassExt(String name, String parentName){
		if (ContainsClass(name))
			return false;

		if (parentName != null && !ContainsClass(parentName))
			return false;

		ClassType parentclass = this.getClass(parentName);
		ClassType newClass = new ClassType(name, parentName, parentclass.getVarOffset(), parentclass.getMethodOffset());
		// update Symbol Table
		classes.put(name, newClass);
		// System.out.println("ST::Inserting "+name+" class with parent "+parentName+" class");
		return true;
	}
	
	// check if class is declared
	public boolean ContainsClass(String name){
		return classes.containsKey(name);
	}
	
	public ClassType getClass(String name){
		return classes.get(name);
	}

	// check if given types are equal, if there is inheritance check parents too
	public boolean checkForType(String type1, String type2){
		if (type1.equals(type2))
			return true;
		ClassType tmpClass;
		tmpClass = this.getClass(type2);
		if (tmpClass == null)
			return false;
		while (tmpClass.parentName != null){
			if (type1.equals(tmpClass.parentName))
				return true;
			tmpClass = this.getClass(tmpClass.parentName);
		}
		return false;
	}

	// print variable and method offsets for every class (except Main)
	public boolean printOffsets(){
		Iterator<Map.Entry<String, ClassType>> LinkedHashMapIterator = classes.entrySet().iterator();
		Iterator<Map.Entry<String, VariableType>> VarIterator;
		Iterator<Map.Entry<String, MethodType>> MethIterator;
		int count = 1;
		while (LinkedHashMapIterator.hasNext()) {
			Map.Entry<String, ClassType> entry = LinkedHashMapIterator.next();
			ClassType tmpclass = entry.getValue(); 
			if (count == 1){
				count = 0;
				continue;
			}
			System.out.println("----------Class "+tmpclass.getName()+"----------");

			LinkedHashMap<String, VariableType> tmpvars = tmpclass.getVars();
			VarIterator = tmpvars.entrySet().iterator();
			System.out.println("-----Variables-----");
			while (VarIterator.hasNext()){
				Map.Entry<String, VariableType> entry2 = VarIterator.next();
				VariableType tmpvar = entry2.getValue();
				System.out.println(entry.getKey()+"."+entry2.getKey()+" : "+tmpvar.getOffset());
			}

			LinkedHashMap<String, MethodType> tmpmethods = tmpclass.getMethods();
			MethIterator = tmpmethods.entrySet().iterator();
			System.out.println("-----Methods-----");
			while (MethIterator.hasNext()){
				Map.Entry<String, MethodType> entry3 = MethIterator.next();
				MethodType tmpmethod = entry3.getValue();
				// skip override methods
				if (tmpmethod.getOverrideFlag() == false)
					System.out.println(entry.getKey()+"."+entry3.getKey()+" : "+tmpmethod.getOffset());
			}

			System.out.println("\n");
		}

		return true;
	}

	// print initial code for every class, declare vtables and declare calloc, printf,exit funcs
	public String printLLCode(){
		String ll = "";
		Iterator<Map.Entry<String, ClassType>> LinkedHashMapIterator = classes.entrySet().iterator();
		Iterator<Map.Entry<String, VariableType>> VarIterator;
		Iterator<Map.Entry<String, MethodType>> MethIterator;

		while (LinkedHashMapIterator.hasNext()) {
			String tmpll = "";
			Set<String> linkedHashSet = new LinkedHashSet<>();
			Map.Entry<String, ClassType> entry = LinkedHashMapIterator.next();
			ClassType tmpclass = entry.getValue(); 
			int methods_num = tmpclass.methods.size();
			ll += "@." + tmpclass.getName() +"_vtable = global [";
			
			LinkedHashMap<String, MethodType> tmpmethods = tmpclass.getMethods();
			
			if (tmpmethods.containsKey("main")){
				
				ll += "0 x i8*] []\n";
				continue;
			}
			// else{
			// 	ll += tmpmethods.size() + " x i8*] [";
			// }
			
			int meth_counter = tmpmethods.size();
			MethIterator = tmpmethods.entrySet().iterator();
			while (MethIterator.hasNext()){
				Map.Entry<String, MethodType> entry3 = MethIterator.next();
				MethodType tmpmethod = entry3.getValue();
				
				// new start
				linkedHashSet.add(tmpmethod.getName());
				// new end
				System.out.println("MPIKA EDW kai len " + linkedHashSet.size());
				ArrayList<VariableType> params = tmpmethod.getParams();
				tmpll += "i8* bitcast (" + tmpmethod.typeToLLVM(tmpmethod.getType()) + " (i8*";
				for (VariableType temp : params) {
					tmpll += ", " + temp.typeToLLVM(temp.getType());
				}
				tmpll += ")* @" + entry.getKey() + "." + entry3.getKey() + " to i8*)";
				meth_counter -= 1;
				if (meth_counter != 0)
					tmpll += ", ";
			}
			
			// start
			Iterator<Map.Entry<String, MethodType>> MethIterator2;
			String parentName = tmpclass.getParentName();
			ClassType parentclass;
			while (parentName != null){
				parentclass = classes.get(parentName); 
				tmpmethods = parentclass.getMethods();
				meth_counter = tmpmethods.size();
				MethIterator2 = tmpmethods.entrySet().iterator();
				while (MethIterator2.hasNext()){
					Map.Entry<String, MethodType> entry4 = MethIterator2.next();
					MethodType tmpmethod = entry4.getValue();
					if (linkedHashSet.contains(tmpmethod.getName()))
						continue;
					linkedHashSet.add(tmpmethod.getName());
					if (linkedHashSet.size() > 0)
						tmpll += ", ";
					ArrayList<VariableType> params = tmpmethod.getParams();
					tmpll += "i8* bitcast (" + tmpmethod.typeToLLVM(tmpmethod.getType()) + " (i8*";
					for (VariableType temp : params) {
						tmpll += ", " + temp.typeToLLVM(temp.getType());
					}
					tmpll += ")* @" + parentName + "." + entry4.getKey() + " to i8*)";
					// meth_counter -= 1;
					// if (meth_counter != 0)
					// 	tmpll += ", ";

				}
				parentName = parentclass.getParentName();
			}
			// end
			ll += linkedHashSet.size() + " x i8*] [" + tmpll;
			ll += "]\n\n";

			tmpclass.addVtableSize(linkedHashSet.size());
			// System.out.println("\n");
		}

		ll += "declare i8* @calloc(i32, i32)\ndeclare i32 @printf(i8*, ...)\ndeclare void @exit(i32)\n\n@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n";
		ll += "define void @print_int(i32 %i) {\n\t%_str = bitcast [4 x i8]* @_cint to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n\tret void\n}\n\n";
		ll += "define void @throw_oob() {\n\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str)\n\tcall void @exit(i32 1)\n\tret void\n}\n\n";
		// System.out.println(ll);
		return ll;
	}
	
}