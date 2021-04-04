package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;

public class CounterVisitor extends VisitorAdaptor {
	Logger log = Logger.getLogger(getClass());
	private boolean main = false;
	private int countClassDeclatation;
	private int countMethodDeclaration;
	private int countConstDelcatation;
	private int countGlobalVar;
	private int countMainStatement;
	private int countGlobalArrays;
	private int countLocalMainVars; // in main
	private int countMainFunctionCalls;
	
	public static final int MAX_LOCALVAR=256;
	public static final int MAX_GLOBALVAR=65536;
	
	
	public int globals=0;
	public boolean correct=true;
	protected int count;
	
	public int getCount(){
		return count;
	}
	

	public String printCounter() {

		StringBuilder sb = new StringBuilder();
		sb.append(countClassDeclatation).append("    ").append("classes").append("\n");
		sb.append(countMethodDeclaration).append("    ").append("methods in the program").append("\n");
		sb.append(countGlobalVar).append("    ").append("global variables").append("\n");
		sb.append(countConstDelcatation).append("    ").append("global constants").append("\n");
		sb.append(countGlobalArrays).append("    ").append("global arrays").append("\n");
		sb.append(countLocalMainVars).append("    ").append("local variables in main").append("\n");
		sb.append(countMainStatement).append("    ").append("statements in main").append("\n");
		sb.append(countMainFunctionCalls).append("    ").append("function calls in main").append("\n");
		return sb.toString();
	}

	@Override
	public void visit(ClassDeclaration classDeclaration) {
		countClassDeclatation++;
	}

	@Override
	public void visit(MethodDeclaration MethodDeclaration) {
		countMethodDeclaration++;
	}

	@Override
	public void visit(ConstantListIdentEqual constDeclaration) {
		countConstDelcatation++;

	}

	@Override
	public void visit(VarIdGlobal varIdGlobal) {
		countGlobalVar++;
		globals++;
		if(globals>=MAX_GLOBALVAR) {
			correct=false;
		}
	}

	@Override
	public void visit(VarArrayIdGlobal varArrayIdGlobal) {
		countGlobalArrays++;
		globals++;
		if(globals>=MAX_GLOBALVAR) {
			correct=false;
		}
	}

	@Override
	public void visit(MethodTypeN methodTypeN) {
		if (methodTypeN.getMethName().compareToIgnoreCase("main") == 0) {
			main = true;
		}

	}

	@Override
	public void visit(MethodVoidN methodVoidN) {
		if (methodVoidN.getMethName().compareToIgnoreCase("main") == 0) {
			main = true;
		}
	}

	@Override
	public void visit(VarIdMethod varIdMethod) {
		if (main)
			countLocalMainVars++;
		if(countLocalMainVars>=MAX_LOCALVAR) {
			correct=false;
		}
	}

	@Override
	public void visit(VarArrayIdMethod varArrayIdMethod) {
		if (main)
			countLocalMainVars++;
		if(countLocalMainVars>=MAX_LOCALVAR) {
			correct=false;
		}
	}

	@Override
	public void visit(Statements statements) {
		if (main)
			countMainStatement++;
	}

	@Override
	public void visit(ActParamsDes actParamsDes) {
		if (main)
			countMainFunctionCalls++;
	}

	@Override
	public void visit(FuncCall actParamsDes) {
		if (main)
			countMainFunctionCalls++;
	}
	
	public static class FormParamCounter extends CounterVisitor{
		
		public void visit(FormalParameters formParamDecl){
			count++;
		}
	}
	
	public static class VarCounter extends CounterVisitor{
		
		public void visit(VarIdMethod varDecl){
			count++;
		}
		public void visit(VarArrayIdMethod varDecl){
			count++;
		}
		
	}
}
