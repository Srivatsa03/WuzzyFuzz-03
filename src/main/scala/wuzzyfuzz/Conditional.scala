package wuzzyfuzz

// Represents a conditional construct with THEN and ELSE branches
case class Conditional(
                        condition: (FuzzySet, FuzzySet) => Boolean, // A condition function taking two fuzzy sets
                        thenBlock: () => Unit,                      // Code to execute if condition is true
                        elseBlock: () => Unit                       // Code to execute if condition is false
                      ) {

  // Executes the appropriate branch based on the condition evaluation
  def executeCondition(set1: FuzzySet, set2: FuzzySet): Unit = {
    if (condition(set1, set2)) thenBlock()
    else elseBlock()
  }

  // Partially evaluate the branches of the conditional construct
  def partialEval(set1: FuzzySet, set2: FuzzySet): Either[String, String] = {
    try {
      if (condition(set1, set2)) {
        thenBlock()
        Left("THEN branch executed successfully")
      } else {
        elseBlock()
        Left("ELSE branch executed successfully")
      }
    } catch {
      case ex: Exception => Right(s"Partial evaluation resulted in an error: ${ex.getMessage}")
    }
  }
}