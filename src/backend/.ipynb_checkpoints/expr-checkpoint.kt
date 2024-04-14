package backend

abstract class Expr {
    abstract fun eval(runtime:Runtime):Data
}

class NoneExpr(): Expr() {
    override fun eval(runtime:Runtime) = None
}

class IntLiteral(val lexeme: String) : Expr() {
    override fun eval(runtime: Runtime): Data = IntData(Integer.parseInt(lexeme))
}

class StringLiteral(val lexeme: String) : Expr() {
    override fun eval(runtime: Runtime): Data = StringData(lexeme.substring(1, lexeme.length-1))
}

class BooleanLiteral(val lexeme: String) : Expr() {
    override fun eval(runtime: Runtime): Data = BooleanData(lexeme.equals("true"))
}

class Deref(val name:String): Expr() {
    override fun eval(runtime:Runtime):Data {
        val data = runtime.symbolTable[name]
        if(data == null) {
            throw Exception("$name is not assigned.")
        }
        return data
    }
}

class Assign(val name: String, val expression: Expr) :Expr() {
    override fun eval(runtime: Runtime):Data = expression.eval(runtime).apply {
        runtime.symbolTable.put(name, this)
        //println("test a")
    }

}

enum class Operator {
    Add,
    Sub,
    Mul,
    Div
}

class Arithmetics(
    val op:Operator,
    val left:Expr,
    val right:Expr
): Expr() {
    override fun eval(runtime:Runtime): Data {
        val x = left.eval(runtime)
        val y = right.eval(runtime)
        if(x is IntData && y is IntData) {
            return IntData(
                when(op) {
                    Operator.Add -> x.value + y.value
                    Operator.Sub -> x.value - y.value
                    Operator.Mul -> x.value * y.value
                    Operator.Div -> {
                        if(y.value != 0) {
                            x.value / y.value
                        } else {
                            throw Exception("cannot divide by zero")
                        }
                    }
                    
                }
            )
        }else if(x is StringData && y is IntData){
            
            var ret = "";
            for(i in 1..y.value){
                ret = ret + x.value
            }
            //println(ret)
            return StringData(ret)
        }else if(x is IntData && y is StringData){
            var ret = "";
            for(i in 1..x.value){
                ret = ret + y.value
            }
            //println(ret)
            return StringData(ret)
        }else{
            throw Exception("invalid arithmetics")
        }

    }
}

class Concat(
    val left:Expr,
    val right:Expr
):Expr(){
    override fun eval(runtime:Runtime): Data {
        //println("concat")
        val x = left.eval(runtime)
        val y = right.eval(runtime)
        if(x !is StringData || y !is StringData) {
            throw Exception("cannot handle non String")
        }
        //print("concat result " + x.value + y.value)
        return StringData(x.value + y.value)
    }
}



class Block(val statements: List<Expr>) : Expr() {
    override fun eval(runtime: Runtime): Data {
        var result: Data = None
        for (statement in statements) {
            result = statement.eval(runtime)
        }
        return result
    }
}

class Invoke(val name:String, val args:List<Expr>):Expr() {
    override fun eval(runtime:Runtime):Data {
        val func:Data? = runtime.symbolTable[name]
        if(func == null) {
            throw Exception("$name does not exist")
        }
        if(func !is FuncData) {
            throw Exception("$name is not a function.")
        }
        if(func.params.size != args.size) {
            throw Exception(
                "$name expects ${func.params.size} arguments "
                + "but received ${args.size}"
            )
        }
        
        val r = runtime.subscope(
            func.params.zip(args.map {it.eval(runtime)}).toMap()
        )
        return func.body.eval(r)
    }
}


class Declare(
    val name: String,
    val params: List<String>,
    val body: Expr
): Expr() {
    override fun eval(runtime:Runtime):Data
    = FuncData(name, params, body).also {
        runtime.symbolTable[name] = it
        //println("declare")
    }
}

class Ifelse(
    val cond:Expr,
    val trueExpr:Expr,
    val falseExpr:Expr
): Expr() {
    override fun eval(runtime:Runtime): Data {
        val cond_data = cond.eval(runtime)
        if(cond_data !is BooleanData) {
            throw Exception("need boolean data in if-else")
        }
        return if(cond_data.value) {
            return trueExpr.eval(runtime)
        } else {
            return falseExpr.eval(runtime)
        }
    }
}

class ioPrint(val message:Expr):Expr(){
     override fun eval(runtime:Runtime):Data {
       val msg:Data = message.eval(runtime)
       println(msg)
       return msg
    }
}

enum class ComparatorOP {
    LT,
    LE,
    GT,
    GE,
    EQ,
    NE,
}

class Compare(
    val comparator: ComparatorOP,
    val left: Expr,
    val right: Expr
): Expr() {
    override fun eval(runtime:Runtime): Data {
        val x = left.eval(runtime)
        val y = right.eval(runtime)
        if(x is IntData && y is IntData) {
            return BooleanData(
                when(comparator) {
                    ComparatorOP.LT -> x.value < y.value
                    ComparatorOP.LE -> x.value <= y.value
                    ComparatorOP.GT -> x.value > y.value
                    ComparatorOP.GE -> x.value >= y.value
                    ComparatorOP.EQ -> x.value == y.value
                    ComparatorOP.NE -> x.value != y.value
                }
            )
        } else {
            throw Exception("only int for comparison")
        }
    }
}

class intIteration(val assign:String,val start:Expr, val end:Expr, val body:Block):Expr(){
     override fun eval(runtime:Runtime):Data {
        
        val startint = start.eval(runtime).toString().toInt()
        val endint = end.eval(runtime).toString().toInt()
        // println(startint)
        // println(startint.toString().toInt().javaClass.name)    
        for(i in (startint)..(endint)){
            runtime.symbolTable.put(assign, IntData(i))
            body.eval(runtime)
        }
        return StringData("Done")
    }
}

class struct(val names:List<String>,val values:List<Expr>) :Expr() {

     override fun eval(runtime:Runtime):Data {
            
         val contents:MutableMap<String, Expr> = mutableMapOf()
         println("in struct")
         println(names.get(0))
         
         val names = names
         val values = values
         for(i in names.indices){
             contents.put(names.get(i),values.get(i))
         }
         return StructData(contents)
        
     }
}



class readStruct(
    val structobj:Expr,
    val target:String
):Expr(){
    override fun eval(runtime:Runtime): Data {
        //println("concat")
        val x:Data = structobj.eval(runtime)

        println(x)
        println(x.javaClass.name)
        if(x !is StructData) {
            throw Exception("cannot handle non String")
        }
        println(x.value)
        //print("concat result " + x.value + y.value)
        val ret = x.value[target]
        
        return ret!!.eval(runtime)
    }
}