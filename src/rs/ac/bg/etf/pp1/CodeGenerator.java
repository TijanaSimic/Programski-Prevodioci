package rs.ac.bg.etf.pp1;

import java.util.LinkedList;
 

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter; 
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.*;

public class CodeGenerator extends VisitorAdaptor {

	private int mainPc;
	Logger log = Logger.getLogger(getClass());

	public int getMainPc() {
		return mainPc;
	}

	private realOperation realOp;
	private LinkedList<Integer> conditions  = new LinkedList<Integer>();

	private enum realOperation {
		equals, not_equals, greater, greater_equals, less, less_equals;
	};

	@Override
	public void visit(PrintStmt1 printStmt) {
		// mi podrazumevamo da kad se expr obradio tj posetio da je on stavio svoju
		// vrednost na exprstack
		// ta vrednost moze biti ili bajt ili int
			
		if (printStmt.getExpr().struct == Tab.charType) {
		 	Code.loadConst(1); // zasto 5?
			// zato sto nam treba da ucitamo velicinu a to cemo uraditi sa const
			// opsti oblik instrukcije const je CONST W ako zelimo konstantu vecu od 4 sto
			// je ovde slucaj
			// zbog toga nam treba 1B za rec const i 4B za rec
			Code.put(Code.bprint);
		} else { // ovde je sirina 1 pa nam je dovoljno da kazemo const_1 pa stoga saljemo samo
					// 1B
			Code.loadConst(5); // sirina
			Code.put(Code.print);
		}
	}

	@Override
	public void visit(PrintStmt2 printStmt) {

		if (printStmt.getExpr().struct == Tab.charType) {
		 	Code.loadConst(1);
			Code.put(Code.bprint);
			
			
			
		} else {
			Code.loadConst(5); 
			Code.put(Code.print);
		}
	}

	@Override
	public void visit(MethodTypeN methodTypeName) {
		if ("main".equalsIgnoreCase(methodTypeName.getMethName())) {
			mainPc = Code.pc;
		}
		methodTypeName.obj.setAdr(Code.pc);
		// pri ulasku u metodu postavljamo adr polje a to je polje trenutnog pc-a

		// Collect arguments and local variables.
		SyntaxNode methodNode = methodTypeName.getParent();
		VarCounter varCnt = new VarCounter();
		methodNode.traverseTopDown(varCnt);
		FormParamCounter fpCnt = new FormParamCounter();
		methodNode.traverseTopDown(fpCnt);

		// Generate the entry.
		Code.put(Code.enter);
		Code.put(fpCnt.getCount());
		Code.put(varCnt.getCount() + fpCnt.getCount());
	}

	@Override
	public void visit(MethodVoidN methodTypeName) {
		if ("main".equalsIgnoreCase(methodTypeName.getMethName())) {
			mainPc = Code.pc;
		}
		methodTypeName.obj.setAdr(Code.pc);
		// pri ulasku u metodu postavljamo adr polje a to je polje trenutnog pc-a

		// Collect arguments and local variables.
		SyntaxNode methodNode = methodTypeName.getParent();
		VarCounter varCnt = new VarCounter();
		methodNode.traverseTopDown(varCnt);
		FormParamCounter fpCnt = new FormParamCounter();
		methodNode.traverseTopDown(fpCnt);

		// Generate the entry.
		Code.put(Code.enter);
		Code.put(fpCnt.getCount());
		Code.put(varCnt.getCount() + fpCnt.getCount());
	}

	@Override
	public void visit(IntRef Const) {

		Code.load(new Obj(Obj.Con, "$", Const.struct, Const.getNumConst(), 0));
	}

	@Override
	public void visit(CharRef Const) {
		Code.load(new Obj(Obj.Con, "$", Const.struct, Const.getCharConst(), 0));
	}

	@Override
	public void visit(BoolRef Const) {
		Code.load(new Obj(Obj.Con, "$", Const.struct, Const.getBoolConst(), 0));
	}

	public void visit(OperatorNewArray operatorNew) {
		Code.put(Code.newarray);
		if (operatorNew.getType().struct == Tab.charType)
			Code.put(0);
		else
			Code.put(1);

	}

	@Override
	public void visit(MethodDeclaration MethodDecl) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	@Override
	public void visit(ReturnExpr ReturnExpr) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	@Override
	public void visit(ReturnNoExpr ReturnNoExpr) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	@Override
	public void visit(DesignatorIdent designator) {
		SyntaxNode parent = designator.getParent();
		// log.info("za designator "+ designator.getClass() + "parent je " +
		// parent.getClass()+ " DEBUUUUG");
		// za designator class rs.ac.bg.etf.pp1.ast.DesignatorIdentparent je class
		// rs.ac.bg.etf.pp1.ast.AssignDes
		// za designator class rs.ac.bg.etf.pp1.ast.DesignatorIdentparent je class
		// rs.ac.bg.etf.pp1.ast.VarRef
		// OVO ZNACI DA AKO PRISTUPAMO DESIGNATORU KOJI JE RVALUE TJ SA DESNE STRANE
		// JEDNAKOSTI ONDA CEMO UPISIVATI ZA TO KOD, A AKO JE LVALUE NECEMO UPISIVATI,
		// TO CEMO UPISIVATI KOD DODELE VEROVATNO

		if (designator.obj.getKind() == Obj.Elem) {
			Obj obj = SemanticAnalyzer.designators.get(designator.obj.getName());
			// log.info("Gore je " + SemanticAnalyzer.printNode(obj), null);
			Code.load(obj);
			Code.put(Code.dup_x1);
			Code.put(Code.pop);
			if (parent.getClass() == Increment.class || parent.getClass() == Decrement.class)
				Code.put(Code.dup2);

		}

		if (AssignDes.class != parent.getClass() && FuncCall.class != parent.getClass()
				&& parent.getClass() != ReadExpr.class) {
			// log.info("a dole je " + SemanticAnalyzer.printNode(designator.obj), null);
			Code.load(designator.obj);
		}

	}

	@Override
	public void visit(AssignDes assignment) {

		Code.store(assignment.getDesignator().obj);

	}

	@Override
	public void visit(TermsList addExpr) {
		if (addExpr.getAddop().getClass() == MinusOp.class)
			Code.put(Code.sub);
		if (addExpr.getAddop().getClass() == PlusOp.class)
			Code.put(Code.add);

	}

	@Override
	public void visit(TermMullFactor mulExpr) {
		if (mulExpr.getMulop().getClass() == DivOp.class)
			Code.put(Code.div);
		if (mulExpr.getMulop().getClass() == TimesOp.class)
			Code.put(Code.mul);
		if (mulExpr.getMulop().getClass() == ModOp.class)
			Code.put(Code.rem);
	}

	@Override
	public void visit(MinusTerms minTerm) {
		Code.put(Code.neg);
	}

	@Override
	public void visit(Increment inc) {
		Obj designator = inc.getDesignator().obj;

		Code.loadConst(1);

		Code.put(Code.add);
		Code.store(designator);

	}

	@Override
	public void visit(Decrement dec) {
		Obj designator = dec.getDesignator().obj;

		Code.loadConst(1);

		Code.put(Code.sub);
		Code.store(designator);

	}

//	@Override
//	public void visit(FuncCall FuncCall) {
//		Obj functionObj = FuncCall.getDesignator().obj;
//		int offset = functionObj.getAdr() - Code.pc;
//		Code.put(Code.call);
//		Code.put2(offset);
//	}

	@Override
	public void visit(SemiColonCondition cond) {
	  
	//   log.info("pc je +" + Code.pc);
	   conditions.add(Code.pc+1);
	    Code.putJump(0); 
	    Code.fixup((int)conditions.removeFirst());
	}

	@Override
	public void visit(WholeCondition cond) {
		    Code.fixup((int)conditions.removeFirst());
			
	}

	
	public void visit(ConditionFactOne confac) {
	 
		 
	//	log.info("SAcuvani pc je " +( Code.pc+1));
		if(realOp!=null) {
			conditions.add(Code.pc+1);
	 	switch(realOp) {
		case equals: Code.putFalseJump(0,0);   break;
		case not_equals:Code.putFalseJump(1,0); break;
		case greater: Code.putFalseJump(4,0);break;
		case greater_equals: Code.putFalseJump(5,0); break;
		case less: Code.putFalseJump(2,0); break;
		case less_equals:Code.putFalseJump(3,0);  break;
		default: 
			 
			break;
		}
		}
		else {
	 			Code.loadConst(0);
			conditions.add(Code.pc+1);
			Code.putFalseJump(4,0);
		}
		realOp=null;
		
		
		 
	   	
	
	  
	}
	public void visit(EqualsOp eq) {
		realOp = realOperation.equals;
	}

	public void visit(NoEqualsOp noeq) {
		realOp = realOperation.not_equals;
	}

	public void visit(GreaterOp greaterOp) {
		realOp = realOperation.greater;
	}

	public void visit(GreaterEqualsOperator greaterEqualsOperator) {
		realOp = realOperation.greater_equals;
	}

	public void visit(LessOperator lessOperator) {
		realOp = realOperation.less;
	}

	public void visit(LessEqualsOperator noLessEqualsOperator) {
		realOp = realOperation.less_equals;
	}

	@Override
	public void visit(ReadExpr readStmt) {
		if (readStmt.getDesignator().obj.getType() == Tab.intType
				|| readStmt.getDesignator().obj.getType() == SemanticAnalyzer.bool) {
			Code.put(Code.read);
		} else if (readStmt.getDesignator().obj.getType() == Tab.charType) {
			Code.put(Code.bread);
		}
		Code.store(readStmt.getDesignator().obj);
	}

}
