package wuzzyfuzz

// The Assign class binds a fuzzy gate to a fuzzy set, enabling evaluation and partial evaluation.
case class Assign(gate: FuzzyGate, fuzzySet: Either[FuzzySet, String]) {

  // Applies the gate's operation to the fuzzy set and returns the resulting fuzzy set or partial result.
  def eval(): Either[FuzzySet, String] = {
    fuzzySet match {
      case Left(fullSet) => Left(gate.eval(fullSet)) // Fully evaluate if the input is a FuzzySet
      case Right(partialExpression) => Right(s"Partial evaluation of gate '${gate.name}' with expression: $partialExpression")
    }
  }
}