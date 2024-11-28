package wuzzyfuzz

// Manages method invocation on a class instance, including dynamic dispatch and support for nested classes
class MethodInvocation(classInstance: ClassInstance, scope: Scope) {

  // Invokes the specified method on a class instance using provided arguments
  def invokeMethod(methodName: String, args: Map[String, Either[FuzzySet, String]]): Either[FuzzySet, String] = {
    classInstance.lookupMethod(methodName) match {
      case Some(method) =>
        try {
          // Partially evaluate the method body and parameters
          val evaluatedBody = method.body.map(assign => assignPartial(assign))

          // If all assignments are fully evaluated, return the last FuzzySet result
          if (evaluatedBody.forall(_.isLeft)) {
            val finalResult = evaluatedBody.collect { case Left(fuzzySet) => fuzzySet }.last
            scope.assign("result", Left(finalResult)) // Update scope with the final result
            Left(finalResult)
          } else {
            // Return a partially evaluated result if any assignments are unresolved
            val partialResults = evaluatedBody.collect { case Right(expr) => expr }.mkString(", ")
            Right(s"Partial evaluation for method $methodName: $partialResults")
          }
        } catch {
          case ex: Exception =>
            Right(s"Error during method invocation: ${ex.getMessage}")
        }
      case None =>
        Right(s"Method $methodName not found in class ${classInstance.classDef.name} or its hierarchy")
    }
  }

  // Partially evaluates an assignment within the method body
  private def assignPartial(assign: Assign): Either[FuzzySet, String] = {
    assign.eval() // Delegate to Assign's eval method
  }
}

// Represents an instance of a class, holding its variables and methods (via Class definition)
// Extended to support nested classes
case class ClassInstance(classDef: Class, variables: Map[String, Either[FuzzySet, String]] = Map()) {

  // Combines inherited variables from the superclass
  private val inheritedVariables: Map[String, Either[FuzzySet, String]] = classDef.superclass match {
    case Some(superClass) =>
      superClass.variables.map { case (k, v) =>
        k -> Right(s"Inherited variable of type ${v.name}")
      } ++ variables
    case None => variables
  }

  // Public method to access a specific inherited variable.
  // This is intentionally public to allow external components, such as NestedClass, to retrieve inherited variables.
  def lookupInheritedVariable(variableName: String): Option[Either[FuzzySet, String]] = {
    inheritedVariables.get(variableName)
  }

  // Look up a variable in the current class or its superclass
  def lookupVariable(variableName: String): Option[Either[FuzzySet, String]] = {
    variables.get(variableName).orElse(lookupInheritedVariable(variableName))
  }

  // Look up a method in the current class or its superclass
  def lookupMethod(methodName: String): Option[Method] = {
    classDef.methods.get(methodName)
      .orElse(classDef.superclass.flatMap(_.methods.get(methodName)))
  }
}