import java.util.*; 
import java.io.*;
abstract class ASTNode {
// abstract superclass; only subclasses are actually created

	int linenum;
	int colnum;
	static PrintStream afile;
	static int semanticsErrors = 0; // Total number of semantics errors found 
	static int cgErrors =  0;
	static int numberOfLocals =  0;
	static int labelCnt = 0;
	static String packageName;
	static int countingDecls;

	static void genIndent(int indent) {
		for (int i = 1; i <= indent; i += 1) {
			System.out.print("\t");
		}
	} // genIndent

	static void mustBe(boolean assertion) {
		if (! assertion) {
			throw new RuntimeException();
		}
	} // mustBe

	static void kindMustBe(Kinds testKind, Kinds requiredKind, String errorMsg){
		if(testKind != Kinds.OTHER && testKind != requiredKind){
			System.out.println(errorMsg);
			semanticsErrors++;
		}
	}

	static void kindMustBe(Kinds testKind,Kinds requiredKind,Kinds requiredKindTwo,String errorMsg){
                if(testKind != Kinds.OTHER && testKind != requiredKind && testKind != requiredKindTwo){
                        System.out.println(errorMsg);
                        semanticsErrors++;
                }
        }

	static void kindMustBeValue(Kinds test, String errorMsg){
		if(test != Kinds.OTHER && test != Kinds.VALUE && test != Kinds.VAR && 
				test != Kinds.CONST && test != Kinds.SCALARPARM && test != Kinds.FUNC){
			System.out.println(errorMsg);
			semanticsErrors++;
		}
	}

	static void typeMustBe(Types testType,Types requiredType,String errorMsg) {
		if ((testType != Types.ERROR) && (testType != requiredType)) {
			System.out.println(errorMsg);
			semanticsErrors++;
		}
	} // typeMustBe

	static void typeMustBe(Types testType,Types requiredOne,Types requiredTwo,String errorMsg) {
		if (testType != Types.ERROR && testType != requiredOne && testType != requiredTwo) {
                	System.out.println(errorMsg);
                        semanticsErrors++;
                }
	}

	static void typeMustBe(Types testType,Types requiredOne,Types requiredTwo,
                        Types requiredThree,String errorMsg) {
                if ((testType != Types.ERROR) && (testType != requiredOne) && 
				(testType != requiredTwo) && (testType != requiredThree)) {
                        System.out.println(errorMsg);
                        semanticsErrors++;
                }
        }
	
	static void typesMustBeEqual(Types type1,Types type2,String errorMsg) {
		if ((type1 != Types.ERROR) && (type2 != Types.ERROR) && (type1 != type2)) {
			System.out.println(errorMsg);
			semanticsErrors++;
		}
	} // typesMustBeEqual
	
	String error() { return "Error (line " + linenum + "): "; } 

	static void gen(String opcode){ afile.println("\t"+opcode);}

	static void gen(String opcode, String operand){ afile.println("\t"+opcode+"\t"+operand);}

	static void gen(String opcode, int operand){afile.println("\t"+opcode+"\t"+operand);}

	static void gen(String opcode, String operand1, String operand2){
        	afile.println("\t"+opcode+"\t"+ operand1+"  "+ operand2);
	}

	static void gen(String opcode, String operand1, int operand2){
        	afile.println("\t"+opcode+"\t"+ operand1+"  "+operand2);
	}

	String buildlabel(int suffix){ return "label"+suffix; }

	void genlab(String label){ afile.println(label+":"); }
	
	public static SymbolTable st = new SymbolTable();
	public static SymbolTable ft = new SymbolTable();

	public void closeScope(SymbolTable table){
		try {
                        table.closeScope();
                } catch (EmptySTException e) {
                        System.out.println(error() + " No open scope.");
                }
	}

	public SymbolInfo insertId(identNode name, Kinds kind, Types type){
		SymbolInfo id;
		id = (SymbolInfo) st.localLookup(name.idname);
		if(id == null){ id = (SymbolInfo) ft.globalLookup(name.idname);}
                if (id == null) {
                        id = new SymbolInfo(name.idname, kind, type);
			name.type = type;
			name.kind = kind;
                        try { 
				if(kind == Kinds.FUNC){ft.insert(id);}
				else { st.insert(id); }
			} catch (DuplicateException d) {
                        	// Can't happen
                	} catch (EmptySTException e) {
                        	// Can't happen
                	};
			name.idinfo = id;
			return id;
                } else {
                       	System.out.println(error() + id.getName() +
                        		" is already declared.");
                       	semanticsErrors++;
			name.type = Types.ERROR;
                }
		return null;
	}

	ASTNode() { linenum = -1; colnum = -1; } 
	ASTNode(int line, int col) { linenum = line; colnum = col; } 
	
	boolean isNull() { return false;} 
	void Unparse(int indent) {} 
	void checkSemantics() {} 
	boolean codegen(PrintStream asmfile){throw new Error();}
	void cg(){}
	
} // class ASTNode

class nullNode extends ASTNode {
	nullNode() { super(); }
	boolean isNull() { return true; }
	void Unparse(int indent) {}
	void cg(){}
} // class nullNode

class csxLiteNode extends ASTNode {

	csxLiteNode(identNode id, memberDeclsNode memb, int line, int col) {
		super(line, col);
		className = id; 
		members = memb;
	} // classNode

	void Unparse(int indent) {
		System.out.print(linenum + ":" + " package ");
		className.Unparse(0);
		System.out.println();
		members.Unparse(1);
	} //Unparse

	void checkSemantics() {
		ft.openScope();
		st.openScope();
		SymbolInfo id = new SymbolInfo(className.idname,Kinds.FUNC,Types.VOID);

                try { ft.insert(id); //insert into function symbolTable
                } catch (DuplicateException d) { // Can't happen
                } catch (EmptySTException e) {} // Can't happen
		
		className.type = Types.VOID;
                className.kind = Kinds.FUNC;
		className.idinfo = id;

		members.checkSemantics();
		closeScope(st);
		closeScope(ft);
	} //checkSemantics

	boolean isSemanticsCorrect(){
		checkSemantics();
		return (semanticsErrors == 0);
	} //isSemanticsCorrect

	boolean codegen(PrintStream asmfile) {
        	afile = asmfile;
        	cg();
        	return (cgErrors == 0);
 	} //codegen

	void cg() {
		packageName = className.idname;
        	gen(".class","public",packageName);
                gen(".super","java/lang/Object");
		members.cg();
	} //cg

	private final identNode className;
	private final memberDeclsNode members;
} // class csxLiteNode

class memberDeclsNode extends ASTNode {
	memberDeclsNode(fieldDeclsNode f, methodDeclsNode m, int line, int col) {
		super(line, col);
		fields = f;
		methods = m;
	} //memberDeclsNode

	void Unparse(int indent){
                fields.Unparse(indent);
                methods.Unparse(indent);
        } //Unparse

	void checkSemantics() { 
		fields.checkSemantics();
		methods.checkSemantics();
	} //checkSemantics

	void cg(){
		fields.field = true;
		fields.cg();
                gen(".method"," public static","main([Ljava/lang/String;)V");
		gen(".limit","locals",1);
                gen("invokestatic", packageName + "/main()V");
                gen("return");
                gen(".limit","stack",2); 
                gen(".end","method");
                methods.cg();
	}

	private fieldDeclsNode fields;
	private final methodDeclsNode methods;
} // class memberDeclsNode

class fieldDeclsNode extends ASTNode {
	fieldDeclsNode() {
		super();
	} //fieldDeclsNode()

	fieldDeclsNode(declNode d, fieldDeclsNode f, int line, int col) {
		super(line, col);
		thisField = d;
		moreFields = f;
		fieldArrays = new ArrayList<>();
	} //fieldDeclsNode(d, f, line, col)

	void Unparse(int indent){
		thisField.Unparse(indent);
                if(!moreFields.isNull()){ moreFields.Unparse(indent); }
        } //Unparse

	void checkSemantics() {
		thisField.checkSemantics();
		countingDecls++;
		if(!moreFields.isNull()){ moreFields.checkSemantics(); }
	} // checkSemantics

	void cg(){
		thisField.field = field;
                thisField.cg();
		if(thisField.name != null){
			fieldArrays.add(thisField.name);
		}
		moreFields.field = field;
                moreFields.fieldArrays = fieldArrays;
		if(!moreFields.isNull()){moreFields.cg();}
		else if(field){
			gen(".method static <clinit>()V");
			gen(".limit locals ", fieldArrays.size());
			for(int i = 0; i < fieldArrays.size(); i++){
				SymbolInfo id = fieldArrays.get(i);	
				if(id != null){
					gen("ldc", id.size);
                                	if(id.type == Types.INT){
                                 	        gen("newarray int");
						gen("putstatic", packageName + "/" + id.getName(), "[I");
	                                }else if(id.type == Types.CHAR){
        	                                gen("newarray char");
						gen("putstatic", packageName + "/" + id.getName(), "[C");
                	                }else if(id.type == Types.BOOL){
                        	                gen("newarray boolean");
						gen("putstatic", packageName + "/" + id.getName(), "[Z");
                                	}
        			}
			}
			gen("return");
			gen(".limit stack 20");
			gen(".end method");
		}
	}

	public ArrayList<SymbolInfo> fieldArrays;
	public boolean field = false;
	static nullFieldDeclsNode NULL = new nullFieldDeclsNode();
	private declNode thisField;
	private fieldDeclsNode moreFields;
} // class fieldDeclsNode

class nullFieldDeclsNode extends fieldDeclsNode {
	nullFieldDeclsNode() {}
	boolean isNull() { return true; } 
	void Unparse(int indent) {} 
	void checkSemantics() {} 
	void cg(){}
} // class nullFieldDeclsNode

// abstract superclass; only subclasses are actually created
abstract class declNode extends ASTNode {
	declNode() { super(); }
	declNode(int l, int c) { super(l, c); }
	public boolean field = false;
	SymbolInfo name;
} // class declNode

class varDeclNode extends declNode {
        varDeclNode(identNode id, typeNode t, int line, int col) {
                super(line, col);
                varName = id;
                varType = t;
        } //varDeclNode

        void Unparse(int indent){
                System.out.print(linenum + ":");
                genIndent(indent);
                System.out.print("var ");
                varName.Unparse(indent);
                varType.Unparse(indent);
                System.out.println(";");
        } //Unparse

	void checkSemantics() {
      		insertId(varName,Kinds.VAR,varType.type);
	} // checkSemantics

	void cg(){
		if(field){
			varName.idinfo.varIndex = -1;
			if(varType.type == Types.INT){
				gen(".field", "static", varName.idname + " I");
			}else if(varType.type == Types.CHAR){
				gen(".field", "static", varName.idname + " C");
			}else if(varType.type == Types.BOOL){
                                gen(".field", "static", varName.idname + " Z");
			}
		}else{
			varName.idinfo.varIndex = numberOfLocals;
			numberOfLocals++;
		}
	}

        private final identNode varName;
        private final typeNode varType;
} // class varDeclNode

class asgDeclNode extends declNode {
	asgDeclNode(identNode id, exprNode e, int line, int col) {
		super(line, col);
		varName = id;
		initValue = e;
	} //asgDeclNode

	void Unparse(int indent){
		System.out.print(linenum + ":");
		genIndent(indent);
		System.out.print("var ");
		varName.Unparse(indent);
                System.out.print("= ");
                initValue.Unparse(indent);
		System.out.println(";");
	} //Unparse

	void checkSemantics() {
		initValue.checkSemantics();
                insertId(varName,Kinds.VAR,initValue.type);
	}

	void cg(){
		if(field){
                        varName.idinfo.varIndex = -1;
                        if(varName.type == Types.INT){
                                gen(".field", "static", varName.idname + " I = " + initValue.ival);
		 	}else if(varName.type == Types.CHAR){
                                gen(".field", "static", varName.idname + " C = " + initValue.cval);
                        }else if(varName.type == Types.BOOL){
                                gen(".field", "static", varName.idname + " Z = " + initValue.bval);
                        }
                }else{
			varName.idinfo.varIndex = numberOfLocals;
        	        numberOfLocals++;
			initValue.cg();
			gen("istore", varName.idinfo.varIndex);
		}
	}

	private final identNode varName;
	private final exprNode initValue;
} // class asgDeclNode

class constDeclNode extends declNode {
	constDeclNode(identNode id,  exprNode e, int line, int col) {
		super(line, col);
		constName = id;
		constValue = e;
	} //constDeclNode

	void Unparse(int indent){
                System.out.print(linenum + ":");
                genIndent(indent);
                System.out.print("const ");
                constName.Unparse(indent);
		System.out.print("= ");
		constValue.Unparse(indent);
		System.out.println(";");
        } //Unparse

	void checkSemantics() {
		constValue.checkSemantics();
                insertId(constName,Kinds.CONST,constValue.type);
	}

	void cg(){
		if(field){
                        constName.idinfo.varIndex = -1;
                        if(constName.type == Types.INT){
                                gen(".field", "static", constName.idname + " I = " + constValue.ival);
                        }else if(constName.type == Types.CHAR){
                                gen(".field", "static", constName.idname + " C = " + constValue.cval);
                        }else if(constName.type == Types.BOOL){
                                gen(".field", "static", constName.idname + " Z = " + constValue.bval);
                        }
                }else{
			constName.idinfo.varIndex = numberOfLocals;
                	numberOfLocals++;
                	constValue.cg();
                	gen("istore", constName.idinfo.varIndex);
		}
	}

	private final identNode constName;
	private final exprNode constValue;
} // class constDeclNode

class arrayDeclNode extends declNode {
	arrayDeclNode(identNode id, typeNode t, intLitNode lit, int line, int col) {
		super(line, col);
		arrayName = id;
		elementType = t;
		arraySize = lit;
	} //arrayDeclNode

	void Unparse(int indent){
                System.out.print(linenum + ":");
                genIndent(indent);
                System.out.print("var ");
                arrayName.Unparse(indent);
                elementType.Unparse(indent);
                System.out.print("[ ");
		arraySize.Unparse(indent);
		System.out.println("] ;");
        } //Unparse

	void checkSemantics() {
                SymbolInfo id = insertId(arrayName,Kinds.ARRAY,elementType.type);
		if(id != null){ id.size = arraySize.intval;}
		if(arraySize.intval < 1){
			System.out.println(error() + "Array size must be greater then 0.");
			semanticsErrors++;
			arrayName.type = Types.ERROR;
		}
        }

	void cg(){
		if(field){
			name = arrayName.idinfo;
                        arrayName.idinfo.varIndex = -1;
                        if(elementType.type == Types.INT){
                                gen(".field", "static", arrayName.idname + " [I");
                        }else if(elementType.type == Types.CHAR){
                                gen(".field", "static", arrayName.idname + " [C");
                        }else if(elementType.type == Types.BOOL){
                                gen(".field", "static", arrayName.idname + " [Z");
                        }
                }else{
			arrayName.idinfo.varIndex = numberOfLocals;
	                numberOfLocals++;
			arraySize.cg();
			if(elementType.type == Types.INT){
				gen("newarray", "int");
			}else if(elementType.type == Types.CHAR){
        	                gen("newarray", "char");
			}else if(elementType.type == Types.BOOL){
        	                gen("newarray", "boolean");
			}
			gen("astore", arrayName.idinfo.varIndex);
		}
	}

	private final identNode arrayName;
	private final typeNode elementType;
	private final intLitNode arraySize;
} // class arrayDeclNode

abstract class typeNode extends ASTNode {
// abstract superclass; only subclasses are actually created
	typeNode() { super(); }
	typeNode(int l, int c, Types t) { super(l, c); type = t;}
	Types type;
	static nullTypeNode NULL = new nullTypeNode();
} // class typeNode

class nullTypeNode extends typeNode {
	nullTypeNode() {type = Types.VOID;}
	boolean isNull() { return true; }
	void Unparse(int indent) {}
	void checkSemantics() {}
	void cg(){}
} // class nullTypeNode

class intTypeNode extends typeNode {
	intTypeNode(int line, int col) { super(line,col,Types.INT);}
	void Unparse(int indent) { System.out.print("int "); }
	void checkSemantics() {} //No type checking needed
} // class intTypeNode

class boolTypeNode extends typeNode {
	boolTypeNode(int line, int col) { super(line,col,Types.BOOL); }
	void Unparse(int indent) { System.out.print("bool "); }
	void checkSemantics() {} //No type checking needed
} // class boolTypeNode

class charTypeNode extends typeNode {
	charTypeNode(int line,int col) { super(line,col,Types.CHAR); }
	void Unparse(int indent) { System.out.print("char "); }
	void checkSemantics() {} //No type checking needed
} // class charTypeNode

class methodDeclsNode extends ASTNode {
	methodDeclsNode() { super(); }
	methodDeclsNode(methodDeclNode m, methodDeclsNode ms, int line, int col) {
		super(line, col);
		thisDecl = m;
		moreDecls = ms;
		containsMain = false;
	} //methodDeclsNode

	void Unparse(int indent){
                thisDecl.Unparse(indent);
                if(!moreDecls.isNull()){
                        moreDecls.Unparse(indent);
                }
        } // Unparse

	void checkSemantics() {
		thisDecl.checkSemantics();
                if(containsMain){
                        System.out.println(error() + "Functions cannot be declared after main().");
                        semanticsErrors++;
                }

		if(!moreDecls.isNull()){
			if(containsMain == true){ moreDecls.containsMain = true; }
                	else { moreDecls.containsMain = thisDecl.calledMain; }
			moreDecls.checkSemantics();
		}
	}

	void cg(){
		thisDecl.cg();
		if(!moreDecls.isNull()){ moreDecls.cg(); }
	}

	public boolean containsMain;
	static nullMethodDeclsNode NULL = new nullMethodDeclsNode();
	private methodDeclNode thisDecl;
	private methodDeclsNode moreDecls;
} // class methodDeclsNode 

class nullMethodDeclsNode extends methodDeclsNode {
	nullMethodDeclsNode() {}
	boolean   isNull() {return true;}
	void Unparse(int indent) {}
	void checkSemantics() {}
	void cg(){}
} // class nullMethodDeclsNode 

class methodDeclNode extends ASTNode {
	methodDeclNode(identNode id, argDeclsNode a, typeNode t, blockNode b, 
			int line, int col) {
		super(line, col);
		name = id;
		args = a;
		returnType = t;
		block = b;
		calledMain = false;
	} //methodDeclNode

	void Unparse(int indent){
                System.out.print(linenum + ":");
                genIndent(indent);
                System.out.print("func ");
                name.Unparse(indent);
		System.out.print("( ");
                args.Unparse(indent);
                System.out.print(") ");
                returnType.Unparse(indent);
		System.out.println();
                block.Unparse(indent);
        } //Unparse

	void checkSemantics() {
         	st.openScope();

		SymbolInfo id = insertId(name, Kinds.FUNC, returnType.type);

		if(name.idname.equals("main")){
		       	if (args.isNull() && returnType.isNull()) {
				calledMain = true;
			}else{
				System.out.println(error()+"Rename method. The method main is reserved "
					       	+ "and cannot have args or a return type.");
				semanticsErrors++;
			}
		}
		args.checkSemantics();
                if(id != null && !args.isNull()){ id.list = args.list;}
		block.checkSemantics();
		SymbolInfo returnid = (SymbolInfo) st.localLookup("return000");
                if (returnid == null) {
        //                typeMustBe(returnType.type, Types.VOID, error() + name.idname 
        //                              + " does not have a return type.");
                } else {
                        typesMustBeEqual(returnType.type, returnid.type, error() + name.idname
                                      +" must return type " + returnType.type + ".");
                }
		closeScope(st);
        	locals = countingDecls;
                countingDecls = 0;
	}

	void cg(){
		ArrayList<SymbolInfo> argsList = args.list;
		String methodSig = "(";
		if(argsList == null){
			methodSig = "()";
		}else{
			for(int i = 0; i < argsList.size(); i++){
				SymbolInfo id = argsList.get(i);
				if(id.kind == Kinds.ARRAY || id.kind == Kinds.ARRAYPARM){
					methodSig += "[";
                                }
				if(id.type == Types.INT){
                        		methodSig += "I";
                		}else if(id.type == Types.CHAR){
                        		methodSig += "C";
	                	}else if(id.type == Types.BOOL){
        	                	methodSig += "Z";
                		}
			}
			methodSig += ")";
		}
		
		if(returnType.type == Types.INT){
			gen(".method","public static " + name.idname + methodSig +"I");
		}else if(returnType.type == Types.CHAR){
                        gen(".method","public static " + name.idname + methodSig +"C");
                }else if(returnType.type == Types.BOOL){
                        gen(".method","public static " + name.idname + methodSig +"Z"); 
                }else if(returnType.type == Types.VOID){
                        gen(".method","public static " + name.idname + methodSig +"V"); 
                }

                gen(".limit","locals",locals);
		numberOfLocals = 0;
		args.cg();
		block.cg();
		SymbolInfo returnid = (SymbolInfo) st.localLookup("return000");
                if (returnid == null) {
                	gen("return");
		} 

                gen(".limit", " stack", 40); //figure out real number
                gen(".end", " method");
	}

	private int locals;
	public boolean calledMain;
	private final identNode name;
	private final argDeclsNode args;
	private final typeNode returnType;
	private final blockNode block;
} // class methodDeclNode 

// abstract superclass; only subclasses are actually created
abstract class argDeclNode extends ASTNode {
	argDeclNode() { super(); }
	argDeclNode(int l, int c) { super(l, c); }
	public SymbolInfo parmInfo;
} // class argDeclNode

class argDeclsNode extends ASTNode {
	argDeclsNode() {}
	argDeclsNode(argDeclNode arg, argDeclsNode args, int line, int col) {
		super(line, col);
		thisDecl = arg;
		moreDecls = args;
		list = new ArrayList<>();
	} // argDeclsNode

	void Unparse(int indent){
                thisDecl.Unparse(indent);
                if(!moreDecls.isNull()){
			System.out.print(", ");
			moreDecls.Unparse(indent);
                }
        } // Unparse

	void checkSemantics() {
		thisDecl.checkSemantics();
		countingDecls++;
		if(!moreDecls.isNull()){
			moreDecls.checkSemantics();
			list = moreDecls.list;
		}
		list.add(0, thisDecl.parmInfo);
	}
	
	void cg(){
		thisDecl.cg();
		moreDecls.cg();
	}

	public ArrayList<SymbolInfo> list;
	static nullArgDeclsNode NULL = new nullArgDeclsNode();
	private argDeclNode thisDecl;
	private argDeclsNode moreDecls;
} // class argDeclsNode 

class nullArgDeclsNode extends argDeclsNode {
	nullArgDeclsNode() {}
	boolean   isNull() {return true;}
	void Unparse(int indent) {}
	void checkSemantics() {}
	void cg(){}
} // class nullArgDeclsNode 

class arrayArgDeclNode extends argDeclNode {
	arrayArgDeclNode(identNode id, typeNode t, int line, int col) {
		super(line, col);
		argName = id;
		elementType = t;
	} //arrayArgDeclNode

	void Unparse(int indent){
                argName.Unparse(indent);
                System.out.print("[]");
		elementType.Unparse(indent);
        } //Unparse

	void checkSemantics() {
		insertId(argName,Kinds.ARRAYPARM,elementType.type); 
		parmInfo = new SymbolInfo(argName.idname, Kinds.ARRAYPARM, elementType.type);
	}

	void cg(){
		argName.idinfo.varIndex = numberOfLocals;
                numberOfLocals++;
	}

	private final identNode argName;
	private final typeNode elementType;
} // class arrayArgDeclNode 

class valArgDeclNode extends argDeclNode {
	valArgDeclNode(identNode id, typeNode t, int line, int col) {
		super(line, col);
		argName = id;
		argType = t;
	} //valArgDeclNode

	void Unparse(int indent){
                argName.Unparse(indent);
                argType.Unparse(indent);
        } //Unparse

	void checkSemantics() {
		insertId(argName,Kinds.SCALARPARM,argType.type); 
		parmInfo = new SymbolInfo(argName.idname,Kinds.SCALARPARM,argType.type);
	}

	void cg(){
		argName.idinfo.varIndex = numberOfLocals;
                numberOfLocals++;
	}

	private final identNode argName;
	private final typeNode argType;
} // class valArgDeclNode 

// abstract superclass; only subclasses are actually created
abstract class stmtNode extends ASTNode {
	stmtNode() { super(); }
	stmtNode(int l, int c) { super(l, c); }
	static nullStmtNode NULL = new nullStmtNode();
} // stmtNode

class nullStmtNode extends stmtNode {
	nullStmtNode() {}
	boolean   isNull() {return true;}
	void Unparse(int indent) {}
	void cg(){}
} // class nullStmtNode 

class stmtsNode extends ASTNode {
	stmtsNode(stmtNode stmt, stmtsNode stmts, int line, int col) {
		super(line, col);
		thisStmt = stmt;
		moreStmts = stmts;
	} //stmtsNode
	stmtsNode() {}

	void Unparse(int indent) {
		thisStmt.Unparse(indent);
		if(!moreStmts.isNull()){ moreStmts.Unparse(indent); }
	} //Unparse

	void checkSemantics() {
		thisStmt.checkSemantics();
		moreStmts.checkSemantics();
	}

	void cg(){
		thisStmt.cg();
		moreStmts.cg();
	}

	public Types returnType;
	static nullStmtsNode NULL = new nullStmtsNode();
	private stmtNode thisStmt;
	private stmtsNode moreStmts;
} // class stmtsNode 

class nullStmtsNode extends stmtsNode {
	nullStmtsNode() {}
	boolean   isNull() {return true;}
	void Unparse(int indent) {}
	void checkSemantics() {}
	void cg(){}
} // class nullStmtsNode 

class asgNode extends stmtNode {
	asgNode(nameNode n, exprNode e, int line, int col) {
		super(line, col);
		target = n;
		source = e;
	} //asgNode

	void Unparse(int indent) {
		System.out.print(linenum + ":");
		genIndent(indent);
		target.Unparse(0);
		System.out.print(" = ");
		source.Unparse(0);
		System.out.println(";");
	} //Unparse

	void checkSemantics() {
		target.checkSemantics();
		source.checkSemantics();

		if(target.type == Types.CHAR && (target.kind == Kinds.ARRAYPARM ||
				target.kind == Kinds.ARRAY) && source.type == Types.STR){
			if(target.varName.size != source.size){
				kindMustBeValue(source.kind, error() + source.kind + " does not"
                                		+ "have a value.");
				System.out.println(error() + "Char array and String"
						+ " must be the same size.");
				semanticsErrors++;
			}
		}else {
			typesMustBeEqual(source.type, target.type, error() + "Both the left and right "+
					"hand sides of an assignment must have the same type.");

			if((target.kind == Kinds.ARRAY || target.kind == Kinds.ARRAYPARM) &&
					(source.kind == Kinds.ARRAY||source.kind == Kinds. ARRAYPARM)){

				if(target.kind != Kinds.ARRAYPARM && source.kind != Kinds.ARRAYPARM
						&& target.varName.size != source.size){
					System.out.println(error() + "Arrays must be the same size.");
					semanticsErrors++;
				}
			}else {
				kindMustBe(target.kind, Kinds.VAR, Kinds.SCALARPARM, error()
						+ "Cannot assign a " + target.kind + ".");
				kindMustBeValue(source.kind, error() + source.kind 
						+ " does not have a value.");
			}
		}
	} // checkSemantics

	void cg(){
		 if(!target.subscriptVal.isNull()){
			target.indexedArrayStore = true;
			target.cg();
			source.cg();
			if(target.type == Types.INT){
				gen("iastore");
			}else if(target.type == Types.CHAR){
				gen("castore");
			}else if(target.type == Types.BOOL){
                                gen("bastore");
			}
		}else if(source.kind == Kinds.ARRAY || source.kind == Kinds.ARRAY){
			String type = "";
			source.cg();
			if(source.type == Types.INT){
				type = "[I";
                               	gen("invokestatic", "CSXLib/cloneIntArray([I)[I");
                       	}else if(source.type == Types.CHAR){
                               	type = "[C";
				gen("invokestatic", "CSXLib/cloneCharArray([C)[C");
                    	}else if(source.type == Types.BOOL){
                               	type = "[Z";
				gen("invokestatic", "CSXLib/cloneBoolArray([Z)[Z");
                       	}
			if(target.varName.idinfo.varIndex < 0){
                                gen("putstatic", packageName + "/" + target.varName.idname, type);
                        }else{
                                gen("astore", target.varName.idinfo.varIndex);
                        }
		}else if(source.type == Types.STR){
			source.cg();
			gen("invokestatic", "CSXLib/convertString(Ljava/lang/String;)[C");
			if(target.varName.idinfo.varIndex < 0){
				gen("putstatic", packageName + "/" + target.varName.idname, "[C");
			}else{
				gen("astore", target.varName.idinfo.varIndex);
			}
		}else if(target.varName.idinfo.varIndex < 0){
                        source.cg();
                        if(target.varName.type == Types.INT){
                                gen("putstatic", packageName + "/" + target.varName.idname, "I");
                        }else if(target.varName.type == Types.CHAR){
                                gen("putstatic", packageName + "/" + target.varName.idname, "C");
                        }else if(target.varName.type == Types.BOOL){
                                gen("putstatic", packageName + "/" + target.varName.idname, "Z");
                        }
                 }else{
			source.cg();	
			gen("istore", target.varName.idinfo.varIndex);
		}
	}

	private final nameNode target;
	private final exprNode source;
} // class asgNode 

class ifThenNode extends stmtNode {
	ifThenNode(exprNode e, blockNode b1, blockNode b2, int line, int col) {
		super(line, col);
		condition = e;
		thenPart = b1;
		elsePart = b2;
	} //ifThenNode

	void Unparse(int indent) {
		System.out.print(linenum + ":");
		genIndent(indent);
		System.out.print("if ");
		condition.Unparse(0);
		System.out.println();
		thenPart.Unparse(indent);
		if(!elsePart.isNull()){
                	System.out.print(linenum + ":");
                	genIndent(indent);
                	System.out.println("else ");
		elsePart.Unparse(indent);
    		}
	} //Unparse

	void checkSemantics() {
		st.openScope();
		condition.checkSemantics();
		kindMustBeValue(condition.kind, error() + condition.kind + " does not have a value.");
		typeMustBe(condition.type, Types.BOOL, error() + "The control"
			       + " expression of an if statement must be a bool.");
		thenPart.checkSemantics();
		closeScope(st);
		if(!elsePart.isNull()){
			st.openScope();
			elsePart.checkSemantics();
			closeScope(st);
		}
	} // checkSemantics

	void cg(){
		String iflabel;
		String elselabel;

		condition.cg();
		iflabel = buildlabel(labelCnt++);
		gen("ifeq", iflabel);
		thenPart.cg();

		elselabel = buildlabel(labelCnt++);
                gen("goto", elselabel);
		genlab(iflabel);

		elsePart.cg();
		genlab(elselabel);
	}

	private final exprNode condition;
	private final blockNode thenPart;
	private final blockNode elsePart;
} // class ifThenNode 

class whileNode extends stmtNode {
	whileNode(identNode i, exprNode e, blockNode b, int line, int col) {
		super(line, col);
		label = i;
		condition = e;
	 	loopBody = b;
	} //whileNode

	void Unparse(int indent) {
		System.out.print(linenum + ":");
                genIndent(indent);
		if(!label.isNull()){
			label.Unparse(0);
			System.out.print(" : ");
		}
                System.out.print("for ");
                condition.Unparse(0);
		System.out.println();
                loopBody.Unparse(indent);
        } //Unparse

	void checkSemantics() {
		st.openScope();
		if(!label.isNull()){ insertId(label,Kinds.LABEL,Types.VOID); }
                condition.checkSemantics();
                typeMustBe(condition.type, Types.BOOL, error() + "The control"
			       + " expression of a for loop must be a bool.");
                kindMustBeValue(condition.kind, error() + condition.kind + " does not have a value.");
		loopBody.checkSemantics();
		closeScope(st);
        } // checkSemantics

	void cg(){
		String headOfLoop;
                String loopExit;

		headOfLoop = buildlabel(labelCnt++);
                loopExit = buildlabel(labelCnt++);
		if(!label.isNull()){
                        label.idinfo.headOfLoop = headOfLoop;
			label.idinfo.loopExit = loopExit;
		}

		genlab(headOfLoop);
		condition.cg();
		gen("ifeq", loopExit);


                loopBody.cg();
		
		gen("goto", headOfLoop);
                genlab(loopExit);


	}

	private final identNode label;
	private final exprNode condition;
	private final blockNode loopBody;
} // class whileNode 

class readNode extends stmtNode {
	readNode() {}
	readNode(nameNode n, readNode rn, int line, int col) {
		super(line, col);
		targetVar = n;
		moreReads = rn;
	} //readNode

	private void UnparseList(){
		targetVar.Unparse(0);
                if(!moreReads.isNull()){
                        System.out.print(", ");
                        moreReads.UnparseList();
                }else{
                        System.out.println(";");
               	}
		
	} //UnparseList
	
	void Unparse(int indent) {
               	System.out.print(linenum + ":");
               	genIndent(indent);
               	System.out.print("read ");
		UnparseList();
	} //Unparse

	void checkSemantics() {
		targetVar.checkSemantics();
		typeMustBe(targetVar.type,Types.CHAR, Types.INT, error() + 
				"Only char and int values may be read.");
		kindMustBe(targetVar.kind,Kinds.VAR,Kinds.SCALARPARM, error()
				+ "Can only read value into variable");
                if(!moreReads.isNull()){
			moreReads.checkSemantics();
		}
	}

	void cg(){
		targetVar.indexedArrayStore = true;
		if(!targetVar.subscriptVal.isNull()){
			targetVar.cg();
			if(targetVar.type == Types.INT){
				gen("invokestatic", "CSXLib/readInt()I");
                		gen("iastore");
			}else if(targetVar.type == Types.CHAR){
                        	gen("invokestatic", "CSXLib/readChar()C");
				gen("castore");
			}
		}else if(targetVar.varName.idinfo.varIndex < 0){
                         if(targetVar.varName.type == Types.INT){
                        	gen("invokestatic", "CSXLib/readInt()I");
			 	gen("putstatic", packageName + "/" + targetVar.varName.idname, "I");
                         }else if(targetVar.varName.type == Types.CHAR){
				gen("invokestatic", "CSXLib/readChar()C");
                                gen("putstatic", packageName + "/" + targetVar.varName.idname, "C");
			 }
                 }else{
			if(targetVar.type == Types.INT){
				gen("invokestatic", "CSXLib/readInt()I");
			}else if(targetVar.type == Types.CHAR){
                        	gen("invokestatic", "CSXLib/readChar()C");
			}
			gen("istore", targetVar.varName.idinfo.varIndex);
		}
		moreReads.cg();
	}

	static nullReadNode NULL = new nullReadNode();
	private nameNode targetVar;
	private readNode moreReads;
} // class readNode 

class nullReadNode extends readNode {
	nullReadNode() {}
	boolean   isNull() {return true;}
	void Unparse(int indent) {}
	void checkSemantics() {}
	void cg(){}
} // class nullReadNode 

class displayNode extends stmtNode {
	displayNode() {}
	displayNode(exprNode val, displayNode pn, int line, int col) {
		super(line, col);
		outputValue = val;
		moreDisplays = pn;
	} //displayNode

	private void UnparseList(){
                outputValue.Unparse(0);
                if(moreDisplays.isNull()){
                        System.out.print(";");
                }else{
                        System.out.print(", ");
                        moreDisplays.UnparseList();
                }

        } //UnparseList

        void Unparse(int indent) {
                System.out.print(linenum + ":");
                genIndent(indent);
                System.out.print("print ");
                UnparseList();
        } //Unparse

	void checkSemantics() {
		outputValue.checkSemantics();
		String errorMsg = error() + "Only int, char and bool " +
                		"values, char arrays or strings may be printed.";

		if(outputValue.kind == Kinds.ARRAY){
			typeMustBe(outputValue.type, Types.CHAR, errorMsg);
		}else{
			kindMustBeValue(outputValue.kind, errorMsg);
			if(outputValue.type == Types.VOID ||
					outputValue.type == Types.ERROR ||
					outputValue.type == Types.UNKNOWN){
				System.out.println(errorMsg);
				semanticsErrors++;
			}
		}
                moreDisplays.checkSemantics();

	} // checkSemantics

	void cg(){
		outputValue.cg();

		if(outputValue.type == Types.INT){
			gen("invokestatic"," CSXLib/printInt(I)V");
		}else if(outputValue.type == Types.CHAR){
			if(outputValue.kind == Kinds.ARRAY || outputValue.kind == Kinds.ARRAYPARM){
                        	gen("invokestatic"," CSXLib/printCharArray([C)V");
                	}else{
				gen("invokestatic"," CSXLib/printChar(C)V");
			}
		}else if(outputValue.type == Types.STR){
			gen("invokestatic"," CSXLib/printString(Ljava/lang/String;)V");
		}else if(outputValue.type == Types.BOOL){
                        gen("invokestatic"," CSXLib/printBool(Z)V");
		}
		moreDisplays.cg(); 
	}

	static nullDisplayNode NULL = new nullDisplayNode();
	private exprNode outputValue;
	private displayNode moreDisplays;
} // class displayNode 

class nullDisplayNode extends displayNode {
	nullDisplayNode() {}
	boolean   isNull() {return true;}
	void Unparse(int indent) {}
	void checkSemantics() {}
	void cg(){}
} // class nullDisplayNode 

class callNode extends stmtNode {
	callNode(identNode id, argsNode a, int line, int col) {
		super(line, col);
		methodName = id;
		args = a;
	} //callNode

	void Unparse(int indent) {
		System.out.print(linenum + ":");
                genIndent(indent);
                methodName.Unparse(0);
                System.out.print("( ");
                args.Unparse(0);
                System.out.println(") ;");
        } //Unparse

	void checkSemantics() {
		methodName.checkSemantics();
		kindMustBe(methodName.kind, Kinds.FUNC, error()+methodName.idname+" is not a function.");
		typeMustBe(methodName.type, Types.VOID, error()	+ methodName.idname + " returns type " 
				+ methodName.type + ".");

		args.checkSemantics();
                ArrayList<SymbolInfo> list = methodName.list;
                ArrayList<SymbolInfo> argsList = args.list;
		int argsSize, listSize;
		if(args.isNull()){argsSize = 0;}
		else {argsSize = argsList.size();}

		if(list == null){listSize = 0;}
                else {listSize = list.size();}
		
		if(listSize != argsSize){
                        System.out.println(error() + "Number of args given does not "
                                        + "match number of args in " + methodName.idname + ".");
                }else{
                        for(int i = 0; i < listSize; i++){
                                typesMustBeEqual(list.get(i).type, argsList.get(i).type, error() 
						+ "Argument " + (i+1) + " in " + methodName.idname 
						+ " must be type " + list.get(i).type + ".");
                                if(list.get(i).kind == Kinds.ARRAYPARM){
                                        kindMustBe(argsList.get(i).kind, Kinds.ARRAY, Kinds.ARRAYPARM, 
							error() + "Argument " + (i+1) + " in " 
							+ methodName.idname + " must be an array.");
                                }else{
                                        kindMustBeValue(argsList.get(i).kind, error() + "Argument " 
							+ (i+1) + " in " + methodName.idname 
							+ " must have a value.");
                                }
                        }
                }

	}

	void cg(){
		args.cg();
		String methodSig = "(";
                ArrayList<SymbolInfo> argsList = args.list;
                if(argsList == null){
                        methodSig = "()";
                }else{
                        for(int i = 0; i < argsList.size(); i++){
                                SymbolInfo id = argsList.get(i);
                                if(id.kind == Kinds.ARRAY || id.kind == Kinds.ARRAYPARM){
                                        methodSig += "[";
                                }
                                if(id.type == Types.INT){
                                        methodSig += "I";
                                }else if(id.type == Types.CHAR){
                                        methodSig += "C";
                                }else if(id.type == Types.BOOL){
                                        methodSig += "Z";
                                }
                        }
                        methodSig += ")";
                }	
		gen("invokestatic", packageName + "/" + methodName.idname + methodSig +"V");
	}

	private final identNode methodName;
	private final argsNode args;
} // class callNode 

class returnNode extends stmtNode {
	returnNode(exprNode e, int line, int col) {
		super(line, col);
		returnVal = e;
	} //returnNode

	void Unparse(int indent) {
                System.out.print(linenum + ":");
                genIndent(indent);
		System.out.print("return ");
                if(!returnVal.isNull()){ returnVal.Unparse(0); }
                System.out.println(";");
        } //Unparse

	void checkSemantics() {
		returnVal.checkSemantics();
		SymbolInfo id;
		if(!returnVal.isNull()){
			id = new SymbolInfo("return000", Kinds.OTHER, returnVal.type);
		}else{
			id = new SymbolInfo("return000", Kinds.OTHER, Types.VOID);
		}
                try {
                        st.insert(id); 
                } catch (DuplicateException d) {
                        // Can't happen
                } catch (EmptySTException e) {
                        // Can't happen
                };
	}

	void cg(){
		if(!returnVal.isNull()){
		       returnVal.cg();
                       gen("ireturn");	
		}else{ gen("return");}
	}

	private final exprNode returnVal;
} // class returnNode 

class blockNode extends stmtNode {
	blockNode() {}
	blockNode(fieldDeclsNode f, stmtsNode s, semiNode se, int line, int col) {
		super(line, col);
		decls = f;
		stmts = s;
		optSemi = se;
		method = false;
	} //blockNode

	void Unparse(int indent) {
		System.out.print(linenum + ":");
                genIndent(indent);
		System.out.println("{ ");
		decls.Unparse(indent+1);
		stmts.Unparse(indent+1);
		System.out.print(linenum + ":");
		genIndent(indent);
		System.out.print("} ");
		if(!optSemi.isNull()){ optSemi.Unparse(indent);}
		System.out.println();
	} //Unparse

	void checkSemantics() {
		decls.checkSemantics();
		stmts.checkSemantics();
	}

	void cg(){
		decls.cg();
		stmts.cg();
		optSemi.cg();
	}

	public boolean method;
	static nullBlockNode NULL = new nullBlockNode();
	private fieldDeclsNode decls;
	private stmtsNode stmts;
	private semiNode optSemi;
} // class blockNode 

class nullBlockNode extends blockNode {
        nullBlockNode() {}
        boolean   isNull() {return true;}
        void Unparse(int indent) {}
	void checkSemantics() {}
	void cg(){}
} // class nullBlockNode

class breakNode extends stmtNode {
	breakNode(identNode i, int line, int col) {
		super(line, col);
		label = i;
	} //breakNode

	void Unparse(int indent) {
                System.out.print(linenum + ":");
                genIndent(indent);
                System.out.print("break ");
                label.Unparse(0);
                System.out.println(";");
        } //Unparse

	void checkSemantics() {
		label.checkSemantics();
		kindMustBe(label.kind,Kinds.LABEL, error() + "Break statement must reference a label.");
	}

	void cg(){
		gen("goto", label.idinfo.loopExit);
	}

	private final identNode label;
} // class breakNode 

class continueNode extends stmtNode {
	continueNode(identNode i, int line, int col) {
		super(line, col);
		label = i;
	} //continueNode

	void Unparse(int indent) {
                System.out.print(linenum + ":");
                genIndent(indent);
                System.out.print("continue ");
                label.Unparse(0);
                System.out.println(";");
        } //Unparse

	void checkSemantics() {
		label.checkSemantics();
		kindMustBe(label.kind,Kinds.LABEL, error()+"Continue statment must reference a label.");
	}

	void cg(){
		gen("goto", label.idinfo.headOfLoop);
	}

	private final identNode label;
} // class continueNode 

class semiNode extends ASTNode {
	semiNode() {}
	semiNode(int line, int col) { super(line,col); }
	void Unparse(int indent){ System.out.print(";");}
	void checkSemantics() {} //Always type correct

	static nullSemiNode NULL = new nullSemiNode();
} // class semiNode

class nullSemiNode extends semiNode {
        nullSemiNode() {}
        boolean isNull() {return true;}
        void Unparse(int indent) {}
	void checkSemantics() {}
	void cg(){}
} // class nullSemiNode

class argsNode extends ASTNode {
	argsNode() {}
	argsNode(exprNode e, argsNode a, int line, int col) {
		super(line, col);
		argVal = e;
		moreArgs = a;
		list = new ArrayList<>();
	} //argsNode

	void Unparse(int indent) {
		argVal.Unparse(indent);
		if(!moreArgs.isNull()){
			System.out.print(", ");
			moreArgs.Unparse(0);
		}
        } //Unparse

	void checkSemantics() {
		argVal.checkSemantics();
		if(!moreArgs.isNull()){
			moreArgs.checkSemantics();
			list = moreArgs.list;
		}
		list.add(0,new SymbolInfo("", argVal.kind, argVal.type));
	}

	void cg(){
		argVal.cg();
		moreArgs.cg();
	}

	public ArrayList<SymbolInfo> list;
	static nullArgsNode NULL = new nullArgsNode();
	private exprNode argVal;
	private argsNode moreArgs;
} // class argsNode 

class nullArgsNode extends argsNode {
	nullArgsNode() {}
	boolean isNull() {return true;}
	void Unparse(int indent) {}
	void checkSemantics() {}
	void cg(){}
} // class nullArgsNode 

class strLitNode extends exprNode {
	strLitNode(String fullstring, String stringval, int line, int col) {
		super(line, col, Types.STR, Kinds.VALUE);
		fullstr = fullstring;
		strval = stringval;
	} //strLitNode
	
	void Unparse(int indent) {System.out.print(strval + " ");} //Unparse
	void checkSemantics() { size = strval.length();}

	void cg(){
		gen("ldc", fullstr);
	}

	private final String fullstr;
	private final String strval;
} // class strLitNode 

// abstract superclass; only subclasses are actually created
abstract class exprNode extends ASTNode {
	exprNode() { super();}
	exprNode(int l, int c) {
		super(l, c);
		type = Types.UNKNOWN;
		kind = Kinds.OTHER;
		size = -1;
	} //exprNode

	exprNode(int l,int c,Types t,Kinds k) {
		super(l,c);
		type = t;
		kind = k;
		size = -1;
	} // exprNode
	
	public int size;
	public int ival;
	public boolean bval;
	public char cval;
	static final nullExprNode NULL = new nullExprNode();
	protected Types type;
	protected Kinds kind;
} // class exprNode

class nullExprNode extends exprNode {
	nullExprNode() { super(); }
	boolean isNull() {return true;}
	void Unparse(int indent) {}
	void checkSemantics() {}
       void cg(){}
} // class nullExprNode 

class binaryOpNode extends exprNode {
	binaryOpNode(exprNode e1, int op, exprNode e2, int line, int col, Types resultType) {
		super(line, col, resultType, Kinds.VALUE);
		operator = op;
		leftOperand = e1;
		rightOperand = e2;
	} //binaryOpNode

	void Unparse(int indent) {
		System.out.print("( ");
		leftOperand.Unparse(0);
		System.out.print(toString(operator));
		rightOperand.Unparse(0);
		System.out.print(") ");
	} //Unparse

	static String toString(int op) {
                switch (op) {
                        case sym.PLUS:
                                return ("+ ");
                        case sym.MINUS:
                                return ("- ");
                        case sym.MULTIPLY:
                                return ("* ");
                        case sym.DIVIDE:
                                return ("/ ");
                        case sym.AND:
                                return ("&& ");
                        case sym.OR:
                                return ("|| ");
			case sym.GREATER:
                                return ("> "); 
                        case sym.LESS:
                                return ("< ");
                        case sym.LESS_EQUAL:
                                return ("<= ");
                        case sym.GREAT_EQUAL:
                                return (">= ");
			case sym.BOOL_EQUAL:
                                return ("== ");
                        case sym.NOT_EQUAL:
                               	return ("!= ");
                        default:
                                mustBe(false);
				return "";
                }
        } //toString

	void checkSemantics() {
		leftOperand.checkSemantics();
		rightOperand.checkSemantics();
		kind = Kinds.VALUE;
		kindMustBeValue(leftOperand.kind, error()+"Left of operand is kind "+leftOperand.kind
				+ " and does not contain a value.");
		kindMustBeValue(rightOperand.kind, error()+"Right of operand is kind "+rightOperand.kind
                                + " and does not contain a value.");

		if(operator==sym.PLUS||operator==sym.MINUS||operator==sym.MULTIPLY
				||operator==sym.DIVIDE){
			type = Types.INT;
			typeMustBe(leftOperand.type, Types.INT, Types.CHAR, error() + "Left operand of "
					+ toString(operator) + "must be an int or char.");
			typeMustBe(rightOperand.type, Types.INT, Types.CHAR, error()+"Right operand of "
					+ toString(operator) + "must be an int or char.");
		}else if(operator==sym.AND||operator==sym.OR){
			type = Types.BOOL;
			typeMustBe(leftOperand.type, Types.BOOL, error() + "Left operand of "
					+ toString(operator) + "must be a boolean.");
                        typeMustBe(rightOperand.type, Types.BOOL, error() + "Right operand of " 
					+ toString(operator) + "must be a boolean.");
		}else if(operator==sym.NOT_EQUAL||operator==sym.BOOL_EQUAL||
                                operator==sym.GREAT_EQUAL||operator==sym.GREATER||
				operator==sym.LESS_EQUAL||operator==sym.LESS){
			
			type = Types.BOOL;
			typeMustBe(leftOperand.type, Types.INT, Types.CHAR, Types.BOOL,error()
					+"Left operand of " + toString(operator)
                                        + "must be an int,char or bool.");
			if(leftOperand.type==Types.BOOL){
				typeMustBe(rightOperand.type, Types.BOOL, error() + "Right operand of " 
						+ toString(operator) + "must be a boolean.");
			}else {
                        	typeMustBe(rightOperand.type, Types.INT, Types.CHAR, error() 
						+ "Right operand of " +  toString(operator)
                                        	+ "must be an int or char.");
			}
                }else{
			mustBe(false);
		}
	} // checkSemantics

	void cg(){
		leftOperand.cg();
		rightOperand.cg();
		if(operator == sym.PLUS){ gen("iadd"); }
		else if(operator == sym.MINUS){ gen("isub"); }
		else if(operator == sym.MULTIPLY){ gen("imul"); }
		else if(operator == sym.DIVIDE){ gen("idiv"); }
		else if(operator == sym.AND){ gen("iand"); }
		else if(operator == sym.OR){ gen("ior"); }
		else{
			String truelabel;
			String falselabel;

			truelabel = buildlabel(labelCnt++);
			falselabel = buildlabel(labelCnt++);

			if(operator == sym.BOOL_EQUAL){
				gen("if_icmpeq", truelabel);
			}else if(operator == sym.NOT_EQUAL){
                                gen("if_icmpne", truelabel);
                        }else if(operator == sym.LESS){
                                gen("if_icmplt", truelabel);
                        }else if(operator == sym.GREATER){
                                gen("if_icmpgt", truelabel);
                        }else if(operator == sym.LESS_EQUAL){
                                gen("if_icmple", truelabel);
                        }else if(operator == sym.GREAT_EQUAL){
                                gen("if_icmpge", truelabel);
                        }
			gen("iconst_0");
			gen("goto", falselabel); 
			genlab(truelabel);
			gen("iconst_1");
			genlab(falselabel);
		}

	}

	private final exprNode leftOperand;
	private final exprNode rightOperand;
	private final int operator; // Token code of the operator
} // class binaryOpNode 

class unaryOpNode extends exprNode {
	unaryOpNode(exprNode e, int line, int col) {
		super(line, col, Types.BOOL, Kinds.VALUE);
		operand = e;
	} //unaryOpNode

	void Unparse(int indent) {
                System.out.print("( ! ");
                operand.Unparse(0);
		System.out.print(" ) ");
        } //Unparse

	void checkSemantics() {
		type = Types.BOOL;
		kind = Kinds.VALUE;
		operand.checkSemantics();
		kindMustBeValue(operand.kind, error() + "Rignt of ! operand must be a value");
		typeMustBe(operand.type, Types.BOOL, error()+"Right of ! operand must be a boolean.");
	}

	void cg(){
		operand.cg();
		String truelabel;
                String falselabel;

                truelabel = buildlabel(labelCnt++);
                falselabel = buildlabel(labelCnt++);
                gen("ifeq", truelabel);
                gen("iconst_0");
                gen("goto", falselabel);
                genlab(truelabel);
                gen("iconst_1");
                genlab(falselabel);
	}

	private final exprNode operand;
} // class unaryOpNode 

class castNode extends exprNode {
	castNode(typeNode t, exprNode e, int line, int col) {
		super(line, col, t.type, Kinds.VALUE);
		operand = e;
		resultType = t;
	} //castNode

	void Unparse(int indent) {
		resultType.Unparse(0);
                System.out.print("( ");
                operand.Unparse(0);
                System.out.print(") ");
        } //Unparse

	void checkSemantics() {
		type = resultType.type;
		kind = Kinds.VALUE;
		operand.checkSemantics();
		kindMustBeValue(operand.kind, error() + "can only type cast a value");
		typeMustBe(operand.type, Types.INT, Types.CHAR, Types.BOOL, 
				error() + "type cast must be of int bool or char.");
	}

	void cg(){
		operand.cg();
		if(resultType.type == Types.BOOL){
			String labelone;
	                String labeltwo;
                	labelone = buildlabel(labelCnt++);
                	labeltwo = buildlabel(labelCnt++);

			gen("ifeq", labelone);
                	gen("iconst_1");

                	gen("goto", labeltwo);
               		genlab(labelone);

                	gen("iconst_0");
                	genlab(labeltwo);
		}else if(resultType.type == Types.CHAR){
			if(operand.type == Types.INT){
				gen("i2b");	
			}
		}
	}

	private final exprNode operand;
	private final typeNode resultType;
} // class castNode 

class fctCallNode extends exprNode {
	fctCallNode(identNode id, argsNode a, int line, int col) {
		super(line, col, id.type, Kinds.FUNC); 
		methodName = id;
		methodArgs = a;
	} //fctCallNode

	void Unparse(int indent) {
                methodName.Unparse(0);
                System.out.print("( ");
                methodArgs.Unparse(0);
                System.out.print(") ");
        } //Unparse

	void checkSemantics() {
		methodName.checkSemantics();
		typeMustBe(methodName.type, Types.INT, Types.BOOL, Types.CHAR, error() 
				+ methodName.idname + "does not have a return type");
		kindMustBe(methodName.kind, Kinds.FUNC, error() +
				methodName.idname + " is not a function");
		methodArgs.checkSemantics();
		ArrayList<SymbolInfo> list = methodName.list;
		ArrayList<SymbolInfo> argsList = methodArgs.list;
		int argsSize, listSize;
		if(methodArgs.isNull()){argsSize = 0;}
                else {argsSize = argsList.size();}

                if(list == null){listSize = 0;}
                else {listSize = list.size();}

		if(listSize != argsSize){
			System.out.println(error() + "Number of args given does not match number of "
					+ "args in " + methodName.idname + argsList.size() + " " 
					+ list.size() + ".");
		}else{
			for(int i = 0; i < listSize; i++){
				typesMustBeEqual(list.get(i).type, argsList.get(i).type, error() 
						+ "Argument " + (i+1) + " in " + methodName.idname 
						+ " must be type " + list.get(i).type + ".");
				if(list.get(i).kind == Kinds.ARRAYPARM){
					kindMustBe(argsList.get(i).kind, Kinds.ARRAY, Kinds.ARRAYPARM, 
							error() + "Argument " + (i+1) + " in " 
							+ methodName.idname + " must be an array"+".");
				}else{
					kindMustBeValue(argsList.get(i).kind, error() + "Argument " 
							+ (i+1) + " in " + methodName.idname + 
							" does not have a value.");
				}
			}
		}
		type = methodName.type;
		kind = Kinds.VALUE;
	}

	void cg(){
		methodArgs.cg();
		String methodSig = "(";
		ArrayList<SymbolInfo> argsList = methodArgs.list;
  		if(argsList == null){
       	 		methodSig = "()";
        	}else{
        		for(int i = 0; i < argsList.size(); i++){
        			SymbolInfo id = argsList.get(i);
        			if(id.kind == Kinds.ARRAY || id.kind == Kinds.ARRAYPARM){
        				methodSig += "[";
        			}
        			if(id.type == Types.INT){
        				methodSig += "I";
        			}else if(id.type == Types.CHAR){
        				methodSig += "C";
        			}else if(id.type == Types.BOOL){
        				methodSig += "Z";
        			}
        		}
        		methodSig += ")";
        	}

		if(methodName.type == Types.INT){
			gen("invokestatic", packageName + "/" + methodName.idname + methodSig + "I");
		}else if(methodName.type == Types.CHAR){
                        gen("invokestatic", packageName + "/" + methodName.idname + methodSig + "C");
                }else if(methodName.type == Types.BOOL){
                        gen("invokestatic", packageName + "/" + methodName.idname + methodSig + "Z");
                }else if(methodName.type == Types.VOID){
                        gen("invokestatic", packageName + "/" + methodName.idname + methodSig + "V");
                }

	}

	public boolean returned;
	private final identNode methodName;
	private final argsNode methodArgs;
} // class fctCallNode 

class identNode extends exprNode {
	identNode(String identname, int line, int col) {
		super(line, col,Types.UNKNOWN, Kinds.VAR);
		idname   = identname;
		nullFlag = false;
		list = new ArrayList<>();
	} //identNode

	identNode(boolean flag){
                super(0,0,Types.UNKNOWN, Kinds.VAR);
                idname   = "";
                nullFlag = flag;
        };

	boolean isNull() {return nullFlag;}
	void Unparse(int indent) { System.out.print(idname + " "); }

	void checkSemantics() {
		SymbolInfo id = (SymbolInfo) st.localLookup(idname);
		if(id == null) {id = (SymbolInfo) st.globalLookup(idname);}
		if(id == null) {id = (SymbolInfo) ft.localLookup(idname);}

		if (id == null) {
			System.out.println(error() + idname + " is not declared.");
			semanticsErrors++;
			type = Types.ERROR;
			kind = Kinds.OTHER;
		} else {
			kind = id.kind;
			type = id.type;
			size = id.size;
			idinfo = id;
			if(id.list != null){ list = id.list;}
		} 
	} // checkSemantics

	public ArrayList<SymbolInfo> list;
	static identNode NULL = new identNode(true);
	public SymbolInfo idinfo;
	public String idname;
	private boolean	nullFlag;
} // class identNode 

class nameNode extends exprNode {
	nameNode(identNode id, exprNode expr, int line, int col) {
		super(line, col,Types.UNKNOWN,Kinds.VAR);
		varName = id;
		subscriptVal = expr;
		indexedArrayStore = false;
	} //nameNode

	void Unparse(int indent) {
		varName.Unparse(0); 
		if(!subscriptVal.isNull()){
			System.out.print("[ ");
			subscriptVal.Unparse(0);
			System.out.print("] ");
		}
	} //Unparse

	void checkSemantics() {
		varName.checkSemantics();
		subscriptVal.checkSemantics();
		type = varName.type;
		kind = varName.kind;
		size = varName.size;
		if(!subscriptVal.isNull()){
			kind = Kinds.VAR;
			kindMustBe(varName.kind, Kinds.ARRAY, Kinds.ARRAYPARM, error() +
					"Cannot apply subscript to non-array "
					+ varName.idname + ".");
			typeMustBe(subscriptVal.type,Types.CHAR,Types.INT, 
					error() + "Only types int and char"
					+ " can index arrays.");
                	kindMustBeValue(subscriptVal.kind, error() + "Subscript of kind " 
					+ subscriptVal.kind + " does not have a value." );
		}
	} // checkSemantics

	void cg(){
		if(!subscriptVal.isNull()){
			if(varName.idinfo.varIndex < 0){
				if(varName.type == Types.INT){
                                        gen("getstatic", packageName + "/" + varName.idname, "[I");
                                }else if(varName.type == Types.CHAR){
                                        gen("getstatic", packageName + "/" + varName.idname, "[C");
                                }else if(varName.type == Types.BOOL){
                                        gen("getstatic", packageName + "/" + varName.idname, "[Z");
                                }
			}else{
				gen("aload", varName.idinfo.varIndex);
			}
			subscriptVal.cg();
			if(!indexedArrayStore && varName.type == Types.INT){
				gen("iaload");
			}else if(!indexedArrayStore && varName.type == Types.CHAR){
                                gen("caload");
			}else if(!indexedArrayStore && varName.type == Types.BOOL){
                                gen("baload");
			}
		}else{
			if(varName.idinfo.varIndex < 0){
				String array = "";
				if(varName.kind == Kinds.ARRAY || varName.kind == Kinds.ARRAYPARM){
					array = "[";
				}
				if(varName.type == Types.INT){
                                	gen("getstatic", packageName + "/" + varName.idname, array + "I");
                        	}else if(varName.type == Types.CHAR){
                                	gen("getstatic", packageName + "/" + varName.idname, array + "C");
                        	}else if(varName.type == Types.BOOL){
                                	gen("getstatic", packageName + "/" + varName.idname, array + "Z");
                        	}
			}else if(varName.kind == Kinds.ARRAY || varName.kind == Kinds.ARRAYPARM){
				gen("aload", varName.idinfo.varIndex);
			}else{
				gen("iload", varName.idinfo.varIndex);
			}
		}
	}

	public boolean indexedArrayStore;
	public final identNode varName;
	public final exprNode subscriptVal;
} // class nameNode 

class intLitNode extends exprNode {
	intLitNode(int val, int line, int col) {
		super(line, col, Types.INT, Kinds.VALUE);
		intval = val;
		ival = val;
	} //intLitNode

	void Unparse(int indent) { 
		if(intval < 0){
			System.out.print("~" + Math.abs(intval) + " ");
		} else {
			System.out.print(intval + " "); 
		}
	}

	void checkSemantics() {} // All intLits are automatically type-correct

	void cg(){
		gen("ldc",intval);	
	}

	public final int intval;
} // class intLitNode 

class charLitNode extends exprNode {
	charLitNode(String charInput, char charValue, int line, int col) {
		super(line, col, Types.CHAR, Kinds.VALUE);
		input = charInput;
		value = charValue;
		cval = charValue;
	} //charLitNode
	
	void Unparse(int indent) { System.out.print(input + " "); }
	void checkSemantics() {} // All charLits are automatically type-correct

	void cg(){
		gen("ldc", value);
	}

	private final String input;
	private final char value;
} // class charLitNode 

class trueNode extends exprNode {
	trueNode(int line, int col) { super(line,col,Types.BOOL,Kinds.VALUE); bval = true;}
	void Unparse(int indent) { System.out.print("true ");}
	void checkSemantics() {} // All boolLits are automatically type-correct
	void cg(){
		gen("iconst_1");
	}
} // class trueNode 

class falseNode extends exprNode {
	falseNode(int line, int col) { super(line,col,Types.BOOL,Kinds.VALUE); bval = false;}
	void Unparse(int indent) { System.out.print("false "); }
	void checkSemantics() {} // All boolLits are automatically type-correct
	void cg(){
		gen("iconst_0");
	}
} // class falseNode 

