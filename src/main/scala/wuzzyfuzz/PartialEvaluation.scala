package wuzzyfuzz

// Simplifies expressions through partial evaluation
object PartialEvaluation {

  // Simplifies expressions based on operator associativity and constant folding
  def simplifyExpression(expression: Any): Any = {
    expression match {
      // Simplify addition of two constants
      case Add(Value(a), Value(b)) =>
        Value(a + b)

      // Simplify multiplication of two constants
      case Multiply(Value(a), Value(b)) =>
        Value(a * b)

      // Simplify nested Multiply(Value, Multiply(Value, ...))
      case Multiply(Value(a), Multiply(Value(b), rest)) =>
        simplifyExpression(Multiply(Value(a * b), rest))

      // Simplify nested Multiply(Multiply(Value, ...), Value)
      case Multiply(Multiply(Value(a), rest), Value(b)) =>
        simplifyExpression(Multiply(Value(a * b), rest))

      // Simplify general nested Multiply expressions
      case Multiply(left, right) =>
        Multiply(simplifyExpression(left), simplifyExpression(right))

      // Simplify general nested Add expressions
      case Add(left, right) =>
        Add(simplifyExpression(left), simplifyExpression(right))

      // Return Variables and Values as-is
      case Variable(name) => Variable(name)
      case Value(v)       => Value(v)

      // Leave other expressions as-is
      case other => other
    }
  }
}

// Supporting classes for the partial evaluation examples
case class Value(v: Int)
case class Variable(name: String)
case class Multiply(left: Any, right: Any)
case class Add(left: Any, right: Any)