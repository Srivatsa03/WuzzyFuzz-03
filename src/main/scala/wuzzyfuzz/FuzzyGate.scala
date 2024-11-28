package wuzzyfuzz

// Base class for fuzzy gates with support for partial evaluation
abstract class FuzzyGate(val name: String) {

  // Evaluates the gate with a given fuzzy set
  def eval(input: FuzzySet): FuzzySet

  // Partially evaluates the gate when inputs are incomplete or undefined
  def partialEval(input: Either[FuzzySet, String]): Either[FuzzySet, String] = {
    input match {
      case Left(fuzzySet) =>
        // Full evaluation when input is a FuzzySet
        try {
          Left(eval(fuzzySet))
        } catch {
          case _: Exception =>
            Right(s"Evaluation of gate '$name' failed with provided input set: ${fuzzySet.name}")
        }
      case Right(partialExpression) =>
        // Return a partial evaluation result if the input is incomplete
        Right(s"Partial evaluation of gate '$name' with expression: $partialExpression")
    }
  }
}

// A gate that performs union on a fuzzy set
class UnionGate extends FuzzyGate("Union") {
  override def eval(input: FuzzySet): FuzzySet = input.eval(FuzzyOperation.Union, input)
}

// A gate that performs intersection on a fuzzy set
class IntersectionGate extends FuzzyGate("Intersection") {
  override def eval(input: FuzzySet): FuzzySet = input.eval(FuzzyOperation.Intersection, input)
}

// A gate that performs the complement on a fuzzy set
class ComplementGate extends FuzzyGate("Complement") {
  override def eval(input: FuzzySet): FuzzySet = input.eval(FuzzyOperation.Complement, input)
}

// A gate that performs addition on a fuzzy set
class AdditionGate extends FuzzyGate("Addition") {
  override def eval(input: FuzzySet): FuzzySet = input.eval(FuzzyOperation.Addition, input)
}

// A gate that performs multiplication on a fuzzy set
class MultiplicationGate extends FuzzyGate("Multiplication") {
  override def eval(input: FuzzySet): FuzzySet = input.eval(FuzzyOperation.Multiplication, input)
}

// A gate that performs XOR operation on a fuzzy set
class XORGate extends FuzzyGate("XOR") {
  override def eval(input: FuzzySet): FuzzySet = input.eval(FuzzyOperation.XOR, input)
}