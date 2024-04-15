grammar PL;

@header {
import backend.*;
import java.util.*;
}

@members {
}

program returns [Expr expr]
    : {List<Expr> statements = new ArrayList<Expr>();} (statement {statements.add($statement.expr);})+ EOF {$expr = new Block(statements);}
    ;



    
statement returns [Expr expr]// high level expressions.
    : {List<Expr> loopstates = new ArrayList<Expr>();}'for(' a=NAME 'in' sta=expr1 '..' end=expr1 ')' '{' (s=statement { loopstates.add($s.expr); })* '}' { $expr = new 
    intIteration($a.text, $sta.expr, $end.expr, new Block(loopstates)); }
    | { List<String> params = new ArrayList<String>();}
    'function' fName=NAME '(' stra=NAME { params.add($stra.text); } (',' strb=NAME  { params.add($strb.text); })* ')' '{' { List<Expr> lines = new ArrayList<Expr>(); } 
    (st=statement {lines.add($st.expr); })*'}' { $expr = new Declare($fName.text, params , new Block(lines));}
    | 'if' '(' cond=expr1 ')'  '{' { List<Expr> statementscnd = new ArrayList<Expr>(); } (st=statement { statementscnd.add($st.expr); } )* '}' 'else' '{' { List<Expr> 
    statementscnd2 = new ArrayList<Expr>(); } (st2=statement { statementscnd2.add($st2.expr); } )* '}' { $expr = new Ifelse($cond.expr, new Block(statementscnd), new 
    Block(statementscnd2));}
    | ex1=expr1 ';'? { $expr = $ex1.expr; }


    ;




expr1 returns [Expr expr]//lower level expressions
    : 'print(' e=expr1 ')' ';'? { $expr = new ioPrint($e.expr); }
    | id=NAME '(' arguments=argsList ')' { $expr = new Invoke($id.text, $arguments.args); }
    | ex1=expr1 '+' ex2=expr1 {$expr = new Arithmetics(Operator.Add,$ex1.expr,$ex2.expr);}
    | ex1=expr1 '-' ex2=expr1 {$expr = new Arithmetics(Operator.Sub,$ex1.expr,$ex2.expr);}
    | ex1=expr1 '*' ex2=expr1 {$expr = new Arithmetics(Operator.Mul,$ex1.expr,$ex2.expr);}
    | ex1=expr1 '/' ex2=expr1 {$expr = new Arithmetics(Operator.Div,$ex1.expr,$ex2.expr);}
    | ex1=expr1 '++' ex2=expr1 {$expr = new Concat($ex1.expr,$ex2.expr);}
    | ex1=expr1 '<' ex2=expr1 { $expr = new Compare(ComparatorOP.LT, $ex1.expr, $ex2.expr); }
    | ex1=expr1 '<=' ex2=expr1 { $expr = new Compare(ComparatorOP.LE, $ex1.expr, $ex2.expr); }
    | ex1=expr1 '>' ex2=expr1 { $expr = new Compare(ComparatorOP.GT, $ex1.expr, $ex2.expr); }
    | ex1=expr1 '>=' ex2=expr1 { $expr = new Compare(ComparatorOP.GE, $ex1.expr, $ex2.expr); }
    | ex1=expr1 '==' ex2=expr1 { $expr = new Compare(ComparatorOP.EQ, $ex1.expr, $ex2.expr); }
    | ex1=expr1 '!=' ex2=expr1 { $expr = new Compare(ComparatorOP.NE, $ex1.expr, $ex2.expr); }
    | '(' e=expr1 ')' { $expr=$e.expr; }
    | id=NAME { $expr = new Deref($id.text); }
    | 'let'? NAME '=' expr1 {$expr = new Assign($NAME.text,$expr1.expr);}
    | 'IntArray[' argsList ']' { $expr = new IntArray($argsList.args); }
    | 'print(' ex1=expr1 ')' ';'? { $expr = new ioPrint($ex1.expr); }
    | fName=NAME '(' arguments=argsList ')' { $expr = new Invoke($fName.text, $arguments.args);}
    |{List<String> names = new ArrayList<String>(); List<Expr> vals = new ArrayList<Expr>();}'struct{' (NAME{names.add($NAME.text);} '=' 
    expr1{vals.add($expr1.expr);})*'}'{$expr = new struct(names,vals); System.out.println("struct");}
    | sname=expr1'.'vname=NAME {$expr= new readStruct($sname.expr,$vname.text);}
    | STRING {$expr = new StringLiteral($STRING.text);} //note i think this might cause issues ill check later
    | NUMBER {$expr = new IntLiteral($NUMBER.text);}
    ;

argsList returns [List<Expr> args]
    : { List<Expr> arguments = new ArrayList<Expr>(); }
    e1=expr1 { arguments.add($e1.expr); } (',' e2=expr1 { arguments.add($e2.expr); })* { $args = arguments; }
    ;
    


Whitespace: [ \t\r\n]+ -> skip;
STRING : '"'(~[\\"\r]|'\\'[\\"])*'"' ;
NUMBER : [0-9]+ ;
NAME : [a-zA-Z][a-zA-Z0-9]*;



