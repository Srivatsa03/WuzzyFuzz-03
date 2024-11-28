package wuzzyfuzz

class NestedClass {

  // Accesses a variable in the outer (or parent) class, considering inheritance and nested classes.
  def accessOuterClassVariable(outer: ClassInstance, variableName: String): Option[Any] = {
    outer.variables.get(variableName)
      .orElse(outer.lookupInheritedVariable(variableName)) // Search in inherited variables
      .map {
        case Left(fuzzySet: FuzzySet) => fuzzySet
        case Right(info: String)      => info
      }
  }

  // Resolves a variable in the current instance, considering shadowing by nested classes and inheritance.
  def resolveVariable(instance: ClassInstance, variableName: String): Option[Any] = {
    // Check current instance variables
    instance.variables.get(variableName)
      .orElse {
        // Check inherited variables
        val inherited = instance.lookupInheritedVariable(variableName)
        if (inherited.isDefined) {
          println(s"Variable '$variableName' found in inherited variables: $inherited")
        }
        inherited
      }
      .orElse {
        // Check nested classes
        val nested = instance.classDef.nestedClasses.values
          .flatMap(nestedClass => nestedClass.variables.get(variableName))
          .headOption
        if (nested.isDefined) {
          println(s"Variable '$variableName' found in nested classes: $nested")
        }
        nested
      }
      .map {
        case Left(fuzzySet: FuzzySet) => fuzzySet
        case Right(info: String)      => info
        case other                    => s"Unhandled variable type: $other" // Catch-all
      }
  }
}