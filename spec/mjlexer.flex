package rs.ac.bg.etf.pp1;

import java_cup.runtime.Symbol;

%%

%{

	// ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type) {
		return new Symbol(type, yyline+1, yycolumn);
	}
	
	// ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type, Object value) {
		return new Symbol(type, yyline+1, yycolumn, value);
	}

%}

%cup
%line
%column

%xstate COMMENT

%eofval{
	return new_symbol(sym.EOF);
%eofval}

%%

" " 	{ }
"\b" 	{ }
"\t" 	{ }
"\r\n" 	{ }
"\f" 	{ }

"program"   { return new_symbol(sym.PROGRAM, yytext()); }
"break"		{ return new_symbol(sym.BREAK, yytext()); }
"class"		{ return new_symbol(sym.CLASS, yytext()); }

"else"		{ return new_symbol(sym.ELSE, yytext()); }
"const"		{ return new_symbol(sym.CONST, yytext()); }	
"if"		{ return new_symbol(sym.IF, yytext()); }
"switch"	{ return new_symbol(sym.SWITCH, yytext()); }
"do"	 	{ return new_symbol(sym.DO, yytext()); }
"while"		{ return new_symbol(sym.WHILE, yytext()); }
"new"		{ return new_symbol(sym.NEW, yytext()); }
"print" 	{ return new_symbol(sym.PRINT, yytext()); }
"read"		{ return new_symbol(sym.READ, yytext()); }
"return" 	{ return new_symbol(sym.RETURN, yytext()); }
"void" 		{ return new_symbol(sym.VOID, yytext()); }
"extends"	{ return new_symbol(sym.EXTENDS, yytext()); }
"continue"	{ return new_symbol(sym.CONTINUE, yytext()); }
"case"		{ return new_symbol(sym.CASE, yytext()); }
"+" 		{ return new_symbol(sym.PLUS, yytext()); }
"-"			{ return new_symbol(sym.MINUS, yytext()); }
"*"		    { return new_symbol(sym.MUL, yytext()); }
"/"			{ return new_symbol(sym.DIV, yytext()); }
"%"		    { return new_symbol(sym.MOD, yytext()); }
"==" 		{ return new_symbol(sym.EQUALS, yytext()); }
"!="		{ return new_symbol(sym.NOT_EQUALS, yytext()); }
">"	    	{ return new_symbol(sym.GREATER, yytext()); }
">="		{ return new_symbol(sym.GREATER_EQUALS, yytext()); }
"<"	    	{ return new_symbol(sym.LESS, yytext()); }
"<="		{ return new_symbol(sym.LESS_EQUALS, yytext()); }
"&&"		{ return new_symbol(sym.AND, yytext()); }
"||"		{ return new_symbol(sym.OR, yytext()); }
"="		    { return new_symbol(sym.EQUAL, yytext()); }
"++"		{ return new_symbol(sym.INC, yytext()); }
"--"		{ return new_symbol(sym.DEC, yytext()); }
";" 		{ return new_symbol(sym.SEMI, yytext()); }
"," 		{ return new_symbol(sym.COMMA, yytext()); }
"(" 		{ return new_symbol(sym.L_ROUND_BR, yytext()); }
")" 		{ return new_symbol(sym.R_ROUND_BR, yytext()); }
"{" 		{ return new_symbol(sym.L_CURRLY_BR, yytext()); }
"}"			{ return new_symbol(sym.R_CURRLY_BR, yytext()); }
"["			{ return new_symbol(sym.L_SQUARE_BR, yytext()); }
"]"			{ return new_symbol(sym.R_SQUARE_BR, yytext()); }
"?"			{ return new_symbol(sym.QUESTION_MARK, yytext()); }
":"			{ return new_symbol(sym.COLON, yytext()); }
"."			{ return new_symbol(sym.DOT, yytext()); }

<YYINITIAL> "//"   { yybegin(COMMENT); }
<COMMENT> .      { yybegin(COMMENT); }
<COMMENT> "\r\n" { yybegin(YYINITIAL); }


"true"|"false"       { return new_symbol(sym.BOOLCONST, new Integer (yytext().equals("true") ? 1 : 0)); }
[0-9]+  { return new_symbol(sym.NUMCONST, new Integer (yytext())); }
"'"[\040-\176]"'" 				{return new_symbol (sym.CHARCONST, new Character (yytext().charAt(1)));}
([a-z]|[A-Z])[a-z|A-Z|0-9|_]* 	{return new_symbol (sym.IDENT, yytext()); }

. { System.err.println("Leksicka greska ("+yytext()+") u liniji "+(yyline+1) + " i u koloni " + yycolumn); }






