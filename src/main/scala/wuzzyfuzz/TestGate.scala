package wuzzyfuzz

// TestGate evaluates the output of a fuzzy gate on a specific variable within a given scope.
// It checks if the result matches an expected value and returns true or false.
object TestGate {

  // Tests the result of applying a gate to a fuzzy set by comparing the result with an expected value.
  // Parameters:
  // - assign: An Assign object that binds a gate to a fuzzy set
  // - variable: The name of the variable to test
  // - expectedValue: The expected membership value for the variable
  // - scope: The scope to look up the variable in
  def apply(assign: Assign, variable: String, expectedValue: Double, scope: Scope): Boolean = {

    // Looks for the variable in the scope and evaluates the gate if found
    scope.lookup(variable) match {
      case Some(Left(fuzzySet)) =>
        assign.eval() match {
          case Left(resultSet) =>
            val membershipValue = resultSet.membership(variable)
            println(s"Testing variable '$variable': expected $expectedValue, got $membershipValue from gate '${assign.gate.name}'")
            // Checks if the actual value is close to the expected value (within a tolerance)
            math.abs(membershipValue - expectedValue) < 1e-9

          case Right(partialMessage) =>
            println(s"Partial evaluation result: $partialMessage")
            false
        }

      // If the variable isn't found in scope, logs the issue and returns false
      case None =>
        println(s"Variable '$variable' not found in scope")
        false

      case Some(Right(partialMessage)) =>
        println(s"Partial evaluation result for variable lookup: $partialMessage")
        false
    }
  }
}