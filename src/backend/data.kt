package backend

abstract class Data

object None:Data() {
    override fun toString() = "None"
}

object NullData: Data() {
    override fun toString(): String = "NULL"
}

class IntData(val value:Int): Data() {
    override fun toString(): String = "$value"

}

class StringData(val value:String): Data() {
    override fun toString(): String = value

}

class BooleanData(val value:Boolean): Data() {
    override fun toString() = 
    "Boolean:${value}"
}

class FuncData(
    val name: String,
    val params: List<String>,
    val body: Expr
): Data() {
    override fun toString()
    = params.joinToString(", ").let {
        "$name($it) { ... }"
    }
}