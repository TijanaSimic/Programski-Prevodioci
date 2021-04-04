package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.*; 

import rs.etf.pp1.symboltable.concepts.*;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;

 
public class Compiler {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	 
	public static void main(String[] args) throws Exception {
		
		//java -jar JFlex.jar ../spec/mjlexer.flex -d ../src/rs/ac/bg/etf/pp1
		//java -jar cup_v10k.jar -parser MJParser -destdir ../src/rs/ac/bg/etf/pp1  ../spec/mjparser.cup
		Logger log = Logger.getLogger(Compiler.class);
		
		Reader br = null;
		try {
			File sourceCode = new File("test/test301.mj");
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);
			
			MJParser p = new MJParser(lexer);
	        Symbol s = p.parse();  //pocetak parsiranja
	        
	        Program prog = (Program)(s.value); 
			// ispis sintaksnog stabla
	     
			log.info(prog.toString("")); 
 
			Tab.init();
			Struct bool = new Struct(Struct.Bool);
			CounterVisitor g = new CounterVisitor();
			
			Tab.currentScope().addToLocals(new Obj (Obj.Type,"bool",bool));
			// ispis prepoznatih programskih konstrukcija
			
			log.info("================SINTAKSNA ANALIZA===================");
			prog.traverseBottomUp(g); 
			if(g.correct) {
			log.info("\n" + g.printCounter());

			
			SemanticAnalyzer v= new SemanticAnalyzer();
			prog.traverseBottomUp(v); 
			
			SymbolTableDump std = new SymbolTableDump();
			tsdump(std);

			
		
			   if (!p.errorDetected) {
 			   if(v.passed()) {
 			   log.info("Parsiranje uspesno zavrseno!");
				   
				  File output;
				  if(args.length==1) {
					  log.info("Za generisanje koda je potreban jos jedan argument komandne linije!");
				  }
				  else {
					  output = new File(args[1]);
				 if(output.exists()) output.delete();;
				  
				 
				  CodeGenerator codeGenerator = new CodeGenerator();
				  prog.traverseBottomUp(codeGenerator);
				  Code.dataSize=v.nVars;
				  Code.mainPc= codeGenerator.getMainPc();
				 
				  if(Code.pc<8000)   {
				  Code.write(new FileOutputStream(output));
				   }
				  else {
					  log.error("Preveliki fajl!");
				  }
				  }
				   }
				   else 
					   log.error("Semanticka greska!");
		        }
		        else {
		        	log.error("Parsiranje NIJE uspesno zavrseno!");
		        }
		} 
			else {

				log.error("Greska: Preveliki broj lokalnih promenljivih!");
	        	
			}
		}
		finally {
			if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}

	}
	public static void tsdump(SymbolTableVisitor stv) {
		Tab.dump(stv);
	}
	
	
}
