package rs.ac.bg.etf.pp1;

import java.util.HashMap;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class SemanticAnalyzer extends VisitorAdaptor {

	private boolean main = false;
	private int designatorArray = 0; // 0-only 1-array
	private int formalParams = 0;
	private int numConst;

	public static HashMap<String, Obj> designators = new HashMap<String, Obj>();

	private enum realOperation {
		equals, not_equals, greater, greater_equals, less, less_equals;
	};

	private realOperation realOp;

	private Boolean wrongType = false;
	public static Struct bool = Tab.find("bool").getType();
	public static Struct array = new Struct(Struct.Array);
	public Struct typeNode = Tab.noType; // razmisliti da li treba negde da je azuriram na noType
	boolean errorDetected = false;
	int printCallCount = 0;
	Obj currentMethod = null;
	boolean returnFound = false;
	int nVars;

	Logger log = Logger.getLogger(getClass());

	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.info(msg.toString());
	}

	public void visit(ProgramName progName) {
		progName.obj = Tab.insert(Obj.Prog, progName.getPName(), Tab.noType);
		Tab.openScope();
	}

	public void visit(ProgramFile program) {
		nVars = Tab.currentScope.getnVars();
		Tab.chainLocalSymbols(program.getProgName().obj);
		Tab.closeScope();
	}

	public void visit(Type type) {
		Obj typeNodeDeclaration = Tab.find(type.getTypeName());
		if (typeNodeDeclaration == Tab.noObj) {
			report_error("Greska: Tip " + type.getTypeName() + " ne predstavlja tip ", type);
			type.struct = Tab.noType;
			typeNode = type.struct;
			wrongType = true;

		} else {
			// ako je pronadjen u tabeli simbola
			if (Obj.Type == typeNodeDeclaration.getKind()) {
				type.struct = typeNodeDeclaration.getType();
				// vraca tip pridruzen imenu (strukturni tip)
				typeNode = type.struct;
				wrongType = false;
				// report_info("Pronadjen je tip" + typeNode.getKind(), type);

			} else {
				report_error("Greska: Tip " + type.getTypeName() + " ne predstavlja tip ", type);
				type.struct = typeNode = Tab.noType;
				wrongType = true;
			}
		}

	}

	public void visit(NumConstantList numConstantList) {
		numConstantList.struct = Tab.intType;
		numConst = numConstantList.getNumCon();
	}

	public void visit(CharConstantList charConstantList) {
		charConstantList.struct = Tab.charType;
		numConst = Character.codePointAt(String.valueOf(charConstantList.getCharCon()), 0);

	}

	public void visit(ConstBoolList boolConstantList) {
		boolConstantList.struct = bool;
		numConst = boolConstantList.getBoolCon();

	}

	public void visit(ConstantListIdentEqual constantListIdentEqual) {
		{
			String constIdentifikator = constantListIdentEqual.getConstIdent();
			Obj constantList = Tab.find(constIdentifikator);

			if (constantList == Tab.noObj) {
				if (!constantListIdentEqual.getConstantList().struct.equals(typeNode)) {
					report_error("Greska: Tip identifikatora konstante " + constIdentifikator
							+ " i tip izraza koji se dodeljuje se ne poklapaju ", constantListIdentEqual);
					wrongType = true;
				}
				label: {
					if (wrongType)
						break label;
					Obj node = Tab.insert(Obj.Con, constIdentifikator, typeNode);
					node.setAdr(numConst);
					// node.setAdr(constantListIdentEqual.getConstantList().getNumConst());
					report_info("Deklarisana je konstanta " + printNode(node), constantListIdentEqual);
				}
			} else {
				report_error("Greska: Deklasirana konstanta " + constIdentifikator + " je vec u tabeli simbola ",
						constantListIdentEqual);
			}
		}
	}

	public void visit(VarIdGlobal varId) {
		String identifikator = varId.getId();
		Obj node = Tab.find(identifikator);
		if (node == Tab.noObj) {
			if (wrongType == false) {
				Obj a = Tab.insert(Obj.Var, identifikator, typeNode);

				report_info("Deklarisana je promenljiva " + printNode(a), varId);
				varId.obj = a;
				wrongType = false;
			}

		} else {
			report_error("Greska: Deklasirana promenljiva " + identifikator + " je vec u tabeli simbola ", varId);
		}

	}

	public void visit(VarArrayIdGlobal varArrayIdGlobal) {
		String identifikator = varArrayIdGlobal.getId();
		Struct array = new Struct(Struct.Array, typeNode);
		Obj node = Tab.find(identifikator);
		if (node == Tab.noObj) {
			if (wrongType == false) {
				Obj a = Tab.insert(Obj.Var, identifikator, array);
				report_info("Deklarisana je promenljiva " + printNode(a), varArrayIdGlobal);
				varArrayIdGlobal.obj = node;
				wrongType = false;
			}

		} else {
			report_error("Greska: Deklasirana promenljiva " + identifikator + " je vec u tabeli simbola ",
					varArrayIdGlobal);
		}

	}

	public void visit(VarIdMethod varId) {
		
		
		String identifikator = varId.getId();
		Obj node = Tab.find(identifikator);
		if (node == Tab.noObj) {
			if (wrongType == false) {
				Obj a = Tab.insert(Obj.Var, identifikator, typeNode);

				report_info("Deklarisana je promenljiva " + printNode(a), varId);
				//varId.obj = a;
				wrongType = false;
			}

		} else {
			report_error("Greska: Deklasirana promenljiva " + identifikator + " je vec u tabeli simbola ", varId);
		}

		
//		
//		String identifikator = varId.getId();
//		Obj node = Tab.insert(Obj.Var, identifikator, typeNode);
//		report_info("Deklarisana je promenljiva " + printNode(node), varId);

	}

	public void visit(VarArrayIdMethod varArrayIdMethod) {
		
		String identifikator = varArrayIdMethod.getId();
		Struct array = new Struct(Struct.Array, typeNode);
		Obj node = Tab.find(identifikator);
		if (node == Tab.noObj) {
			if (wrongType == false) {
				Obj a = Tab.insert(Obj.Var, identifikator, array);
				report_info("Deklarisana je promenljiva " + printNode(a), varArrayIdMethod);
				//varArrayIdMethod.obj = node;
				wrongType = false;
			}

		} else {
			report_error("Greska: Deklasirana promenljiva " + identifikator + " je vec u tabeli simbola ",
					varArrayIdMethod);
		}
		
		
//		String identifikator = varArrayIdMethod.getId();
//		Obj node = Tab.insert(Obj.Var, identifikator, array);
//		node.getType().setElementType(typeNode);
//		report_info("Deklarisana je promenljiva " + printNode(node), varArrayIdMethod);
	}

	public void visit(MethodTypeN methodTypeN) {

		currentMethod = Tab.insert(Obj.Meth, methodTypeN.getMethName(), methodTypeN.getType().struct);
		methodTypeN.obj = currentMethod;
		Tab.openScope();
		if (methodTypeN.getMethName().compareToIgnoreCase("main") == 0) {
			main = true;
			report_error("Greska: postoji metoda sa imenom main ali nije deklarisana kao void metoda ", methodTypeN);
		} else {
			report_info("Obradjuje se funkcija " + printNode(currentMethod), methodTypeN);
		}

	}

	@Override
	public void visit(MethodVoidN methodVoidN) {
		currentMethod = Tab.insert(Obj.Meth, methodVoidN.getMethName(), Tab.noType);
		methodVoidN.obj = currentMethod;
		Tab.openScope();
		if (methodVoidN.getMethName().compareToIgnoreCase("main") == 0) {
			main = true;
		}
		report_info("Obradjuje se funkcija " + printNode(currentMethod), methodVoidN);
	}

	public void visit(MethodDeclaration methodDecl) {
		if (!returnFound && currentMethod.getType() != Tab.noType) {
			report_error("Greska: funkcija " + currentMethod.getName() + " nema return iskaz ", methodDecl);
		}
		currentMethod.setLevel(formalParams);
		formalParams = 0;

		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();

		returnFound = false;
		currentMethod = null;
	}

	public void visit(FormalParameters formalParameter) {
		// report_info("main je " + main,null);
		if (main == true) {
			report_error("Greska: funkcija main ima formalne parametre ", formalParameter);
		} else {
			formalParams++;
		}
	}

	public void visit(ReturnNoExpr returnNoExpr) {
		returnFound = true;
		Struct currMethType = currentMethod.getType();
		if (currMethType != Tab.noType) {
			report_error("Greska: Funkciji " + currentMethod.getName() + " nedostaje povratna vrednost  ",
					returnNoExpr);
		}
	}

	public void visit(ReturnExpr returnExpr) {
		returnFound = true;
		Struct currMethType = currentMethod.getType();
		if (currentMethod.getName().compareToIgnoreCase("main") == 0) {
			report_error("Greska: Funkcija main ne moze imati povratnu vrednost posto moze biti samo void ",
					null);

		} 
		if (!currMethType.compatibleWith(returnExpr.getExpr().struct)) {
		 
				report_error(
						"Greska: " + "Tip izraza u return naredbi ne slaze se sa tipom povratne vrednosti funkcije "
								+ currentMethod.getName(),
						returnExpr);
		}
	}

	public void visit(Increment inc) {

		Obj designator = inc.getDesignator().obj;
		if (designator != Tab.noObj && designator.getKind() != Obj.Con && designator.getKind() != Obj.Type
				&& designator.getKind() != Obj.Meth && designator.getKind() != Obj.Prog)
			if (designator.getType().getKind() == Struct.Int) {
				report_info("Inkrementiranje promenljive " + printNode(designator), inc);
			} else {
				report_error("Greska: Inkrementiranje se  vrsi nad promenljivom " + printNode(designator)
						+ " koja nije tipa int ", inc);

			}
	}

	public void visit(Decrement dec) {
		Obj designator = dec.getDesignator().obj;
		if (designator != Tab.noObj && designator.getKind() != Obj.Con && designator.getKind() != Obj.Type
				&& designator.getKind() != Obj.Meth && designator.getKind() != Obj.Prog)
			if (designator.getType().getKind() == Struct.Int) {
				report_info("Dekrementiranje promenljive " + printNode(designator), dec);
			} else {
				report_error("Greska: Dekrementiranje se vrsi nad promenljivom " + printNode(designator)
						+ " koja nije tipa int ", dec);

			}
	}

	public void visit(PrintStmt1 print) {
		Struct expr = print.getExpr().struct;
		if (expr.getKind() == Struct.Bool || expr.getKind() == Struct.Int || expr.getKind() == Struct.Char
				|| (expr.getKind() == Struct.Array
						&& (expr.getElemType().getKind() == Struct.Int || expr.getElemType().getKind() == Struct.Char
								|| expr.getElemType().getKind() == Struct.Bool))) {
			report_info("Poziv print metode ", print);
		} else {
			report_error("Greska: Poziv print metode nad izrazom koji nije tipa bool, char ili int  ", print);

		}
	}

	public void visit(PrintStmt2 print) {
		Struct expr = print.getExpr().struct;
		if (expr.getKind() == Struct.Bool || expr.getKind() == Struct.Int || expr.getKind() == Struct.Char
				|| (expr.getKind() == Struct.Array
						&& (expr.getElemType().getKind() == Struct.Int || expr.getElemType().getKind() == Struct.Char
								|| expr.getElemType().getKind() == Struct.Bool))) {
			report_info("Poziv print metode ", print);
		} else

		{
			report_error("Greska: Poziv print metode nad izrazom koji nije tipa bool, char ili int  ", print);

		}
	}

	public void visit(ExpressionTerm expressionTerm) {
		expressionTerm.struct = expressionTerm.getExpressionTerms().struct;
	}

	public void visit(JustTerms justTerms) {
		justTerms.struct = justTerms.getTerms().struct;
	}

	public void visit(MinusTerms minusTerms) {
		if (minusTerms.getTerms().struct.getKind() != Struct.Int) {
			report_error("Greska: Unarni operator - se mora koristiti uz tip int ", minusTerms);
		}
		minusTerms.struct = minusTerms.getTerms().struct;
	}

	public void visit(TermsList terms) {
		Struct s1 = terms.getTerms().struct;
		Struct s2 = terms.getTerm().struct;

		if (!((s1.getKind() == Struct.Int && s2.getKind() == Struct.Int)
				|| (s1.getKind() == Struct.Array && s1.getElemType().getKind() == Struct.Int
						&& s2.getKind() == Struct.Int)
				|| (s2.getKind() == Struct.Array && s2.getElemType().getKind() == Struct.Int
						&& s1.getKind() == Struct.Int)
				|| ((s1.getKind() == Struct.Array && s2.getKind() == Struct.Array
						&& s1.getElemType().getKind() == Struct.Int && s2.getElemType().getKind() == Struct.Int))))
//			report_info("Pronadjeno sabiranje ", terms);
//		else {
			report_error("Greska: Nekompatabilni tipovi u operaciji  ", terms);
		// }
		terms.struct = terms.getTerm().struct;
//			terms.struct = Tab.intType;
	}

	public void visit(TermsExpressionTerm terms) {
		terms.struct = terms.getTerm().struct;
	}

	public void visit(TermFactor term) {
		term.struct = term.getFactor().struct;
	}

	public void visit(TermMullFactor term) {
		Struct s1 = term.getFactor().struct;
		Struct s2 = term.getTerm().struct;

		if (!((s1.getKind() == Struct.Int && s2.getKind() == Struct.Int)
				|| (s1.getKind() == Struct.Array && s1.getElemType().getKind() == Struct.Int
						&& s2.getKind() == Struct.Int)
				|| (s2.getKind() == Struct.Array && s2.getElemType().getKind() == Struct.Int
						&& s1.getKind() == Struct.Int)
				|| ((s1.getKind() == Struct.Array && s2.getKind() == Struct.Array
						&& s1.getElemType().getKind() == Struct.Int && s2.getElemType().getKind() == Struct.Int))))
//			report_info("Pronadjeno mnozenje ", term);
//		else {
			report_error("Greska: Nekompatabilni tipovi u operaciji ", term);
		// }
		term.struct = term.getFactor().struct;
	}

	public void visit(IntRef intRef) {
		intRef.struct = Tab.intType;

	}

	public void visit(CharRef charRef) {
		charRef.struct = Tab.charType;
	}

	public void visit(BoolRef boolRef) {
		boolRef.struct = bool;
	}

	public void visit(VarRef varRef) {
		varRef.struct = varRef.getDesignator().obj.getType();
	}

	public void visit(ReadExpr read) {
		Obj obj = read.getDesignator().obj;

		if (obj != Tab.noObj && obj.getKind() != Obj.Con && obj.getKind() != Obj.Type && obj.getKind() != Obj.Meth
				&& obj.getKind() != Obj.Prog)
			if (obj.getType().getKind() == Struct.Bool || obj.getType().getKind() == Struct.Char
					|| obj.getType().getKind() == Struct.Int
					|| (obj.getType().getKind() == Struct.Array && (obj.getType().getElemType().getKind() == Struct.Int
							|| obj.getType().getElemType().getKind() == Struct.Char
							|| obj.getType().getElemType().getKind() == Struct.Bool))) {
				report_info("Poziv read metode ", read);
			} else

			{
				report_error("Greska: Poziv read metode sa argumentom koji nije tipa bool, char ili int", read);

			}
		else {
			report_error("Greska: Designator " + printNode(obj) + " nije promenljiva ili element niza ", read);

		}
	}

	public void visit(FuncCall funcCall) {
		Obj func = funcCall.getDesignator().obj;
		if (Obj.Meth == func.getKind()) {
			report_info("Poziv funkcije " + func.getName(), funcCall);
			funcCall.struct = func.getType();
		} else {
			report_error("Greska: Ime " + func.getName() + " nije funkcija ", funcCall);
			funcCall.struct = Tab.noType;
		}
	}

	public void visit(ParenthesisExpr expr) {
		expr.struct = expr.getExpr().struct;
	}

	public void visit(ExpressionConditions expressionConditions) {
		expressionConditions.struct = expressionConditions.getExpressionCondition().struct;
		report_info("Koriscenje ternarnog operatora ", expressionConditions);
	}

	public void visit(WholeCondition wholeCondition) {
//		if (wholeCondition.getCondition().struct.getKind() != Struct.Bool) {
//			report_error("Greska: Uslov nije tipa bool ", wholeCondition);
//		}
		if (wholeCondition.getExpr().struct.getKind() != wholeCondition.getExpr1().struct.getKind()) {
			report_error("Greska: Izrazi nisu istog tipa ", wholeCondition);

		}
		wholeCondition.struct = wholeCondition.getExpr().struct;// ??????????????????????????????????
	}

	public void visit(ExprOne exprOne) {
		exprOne.struct = exprOne.getExpr().struct;
	}

	public void visit(ConditionTermOne conditionTermOne) {
		conditionTermOne.struct = conditionTermOne.getCondTerm().struct;
	}

	public void visit(ConditionFactOne conditionFactOne) {
		conditionFactOne.struct = conditionFactOne.getCondFact().struct;
	}

	public void visit(CondFactExpressionTerms condFactExpressionTerms) {
		condFactExpressionTerms.struct = condFactExpressionTerms.getExpressionTerms().struct;
	}

	public void visit(CondFactExpressionTermsList condFactExpressionTermsList) {
		Struct s1 = condFactExpressionTermsList.getExpressionTerms().struct;
		Struct s2 = condFactExpressionTermsList.getExpressionTerms1().struct;
		if (s1.getKind() == Struct.Array)
			s1 = s1.getElemType();
		if (s2.getKind() == Struct.Array)
			s2 = s2.getElemType();
		if (!s1.compatibleWith(s2)) {
			report_error("Greska: Izrazi u okviru ternarnog operatora nisu kompatabilni " + s1.getKind() + s2.getKind(),
					condFactExpressionTermsList);
			condFactExpressionTermsList.struct = condFactExpressionTermsList.getExpressionTerms().struct;
		} else {
			// if((s1.getKind()==Struct.Array || s2.getKind()==Struct.Array) )
			condFactExpressionTermsList.struct = bool;
		}

	}

	public void visit(ConditionList conditionList) {
		conditionList.struct = conditionList.getCondition().struct;
	}

	public void visit(ConditionTermList conditionList) {
		conditionList.struct = conditionList.getCondFact().struct;
	}

	public void visit(ExpressionTermsOne expressionTermsOne) {
		expressionTermsOne.struct = expressionTermsOne.getExpressionTerms().struct;
	}

	public void visit(DesignatorExpr designatorExpr) {
		designatorArray = 1;

	}

	public void visit(DesignatorIdent designator) { // ovo je za slucaj kada se u faktoru javlja samo designator, tu
													// imamo upotrebu promenljivih
		String designatorName = designator.getDesignatorName();
		Obj obj = Tab.find(designatorName);

		if (obj == Tab.noObj) {
			report_error("Greska: ime " + designatorName + " nije deklarisano  ", designator);
		} else {

			label: {
			if (designatorArray == 1) {
				if (obj.getType().getKind() != Struct.Array) {
					report_error("Greska: Pokusaj pristupa elementu promenljive koja nije niz", designator);
					designatorArray = 0;
					 break label;
				}
				report_info("Koriscenje elementa niza " + printNode(obj), designator);
				designators.put(designatorName, obj);
				obj = new Obj(Obj.Elem, designatorName, obj.getType().getElemType());

				designatorArray = 0;
			} else
				report_info("Koriscenje promenljive " + printNode(obj), designator);
		}
		}
		designator.obj = obj;
	}

	public void visit(OperatorNewArray array) {

		// if(obj.getType().getKind()==typeNode.getKind()) {

		Struct expr = array.getExpr().struct;
		if (expr.getKind() == Struct.Int) {
			report_info("Definicija niza tipa " + array.getType().getTypeName(), array);
		} else {
			report_error("Greska: Broj elemenata niza nije odgovarajuceg tipa " + array.getType().getTypeName(), array);

		}
		array.struct = new Struct(Struct.Array, array.getType().struct);

	}

	public void visit(AssignDes assignment) {
		Obj designator = assignment.getDesignator().obj;
		if (designator != Tab.noObj && designator.getKind() != Obj.Con && designator.getKind() != Obj.Type
				&& designator.getKind() != Obj.Meth && designator.getKind() != Obj.Prog) {
//			if ((assignment.getExpr().struct.getKind() != Struct.Array
//					&& !assignment.getExpr().struct.assignableTo(designator.getType()))
//					||assignment.getExpr().struct.getKind() == Struct.Array
//							&& !assignment.getExpr().struct.assignableTo(designator.getType().getElemType()))
			if (!assignment.getExpr().struct.assignableTo(designator.getType()))
				report_error("Greska: " + "Nekompatibilni tipovi u dodeli vrednosti ", assignment);
		} else {
			report_error("Greska: Ne moze se dodeliti vrednost necemu sto nije varijabla ili element niza ",
					assignment);
		}
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

	public boolean passed() {
		return !errorDetected;
	}

	public static String printNode(Obj obj) {
		StringBuilder sb = new StringBuilder();
		String kind = "";
		int getKind = obj.getKind();
		if (getKind == 0)
			kind = "Con";
		else if (getKind == 1)
			kind = "Var";
		else if (getKind == 2)
			kind = "Type";
		else if (getKind == 3)
			kind = "Meth";
		else if (getKind == 4)
			kind = "Fld";
		else if (getKind == 5)
			kind = "Elem";
		else if (getKind == 6)
			kind = "Prog";
		sb.append(kind);
		sb.append(" ");
		sb.append(obj.getName());
		sb.append(": ");
		int structKind = obj.getType().getKind();
		if (structKind == 0)
			kind = "none";
		else if (structKind == 1)
			kind = "int";
		else if (structKind == 2)
			kind = "char";
		else if (structKind == 3)
			kind = "Array";
		else if (structKind == 4)
			kind = "class";
		else if (structKind == 5)
			kind = "bool";
		sb.append(kind);
		if (kind == "Array") {
			structKind = obj.getType().getElemType().getKind();
			if (structKind == 0)
				kind = "none";
			else if (structKind == 1)
				kind = "int";
			else if (structKind == 2)
				kind = "char";
			else if (structKind == 3)
				kind = "Array";
			else if (structKind == 4)
				kind = "class";
			else if (structKind == 5)
				kind = "bool";

			sb.append(" of ");
			sb.append(kind);
		}
		sb.append(", ");
		sb.append(obj.getAdr());
		sb.append(", ");
		sb.append(obj.getLevel());

		return sb.toString();
	}
}
