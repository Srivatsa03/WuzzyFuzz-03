package wuzzyfuzz

class Scope(val parent: Option[Scope] = None, val variables: Map[String, Either[FuzzySet, String]] = Map()) {

  // Adds or updates a variable in the current scope and returns a new Scope with the updated variables
  def assign(name: String, value: Either[FuzzySet, String]): Scope = {
    new Scope(parent, variables + (name -> value))
  }

  // Searches for a variable by name, looking in the current scope and then recursively in parent scopes if not found
  def lookup(name: String): Option[Either[FuzzySet, String]] = {
    variables.get(name).orElse(parent.flatMap(_.lookup(name)))
  }

  // Retrieves a fully evaluated FuzzySet, if available, or throws an error
  def resolveFully(name: String): Either[FuzzySet, String] = {
    lookup(name).getOrElse(throw new Exception(s"Variable '$name' not found in scope"))
  }

  // Creates a new child scope that inherits all variables from this scope
  def createChildScope(): Scope = new Scope(Some(this))

  // Debugging utility to display the scope hierarchy
  override def toString: String = {
    val currentScope = variables.map {
      case (key, Left(fuzzySet))    => s"$key -> $fuzzySet"
      case (key, Right(expression)) => s"$key -> (Partial: $expression)"
    }.mkString(", ")
    parent match {
      case Some(parentScope) => s"{ $currentScope } -> Parent: ${parentScope.toString}"
      case None              => s"{ $currentScope }"
    }
  }
}