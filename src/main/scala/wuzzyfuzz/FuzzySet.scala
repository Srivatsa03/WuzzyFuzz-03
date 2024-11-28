package wuzzyfuzz

// Represents a fuzzy set class with a name and a mapping of elements to their membership values (0.0 to 1.0).
// Additionally, it can contain nested fuzzy sets, allowing for hierarchical fuzzy structures.
case class FuzzySet(name: String, elements: Map[String, Double], nestedSets: Option[Map[String, FuzzySet]] = None) {

  // Method to get the membership value for a given element in the set
  def membership(element: String): Double = elements.getOrElse(element, 0.0)

  // Helper function to round a value to 2 decimal places
  private def round(value: Double): Double =
    BigDecimal(value).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

  // Evaluate binary operations between two fuzzy sets, performing operations on the elements map
  // and recursively on nested sets if they exist
  def eval(op: FuzzyOperation, that: FuzzySet = this): FuzzySet = {
    val primaryEval = op match {
      case FuzzyOperation.Union =>
        FuzzySet(s"$name Union ${that.name}", elements ++ that.elements.map {
          case (k, v) => k -> round(math.max(v, membership(k)))
        })

      case FuzzyOperation.Intersection =>
        FuzzySet(s"$name Intersection ${that.name}", elements ++ that.elements.map {
          case (k, v) => k -> round(math.min(v, membership(k)))
        })

      case FuzzyOperation.Complement =>
        FuzzySet(s"Complement of $name", elements.map {
          case (k, v) => k -> round(1.0 - v)
        })

      case FuzzyOperation.Addition =>
        FuzzySet(s"$name Addition ${that.name}", elements ++ that.elements.map {
          case (k, v) => k -> round(math.min(1.0, v + membership(k)))
        })

      case FuzzyOperation.Multiplication =>
        FuzzySet(s"$name Multiplication ${that.name}", elements ++ that.elements.map {
          case (k, v) => k -> round(v * membership(k))
        })

      case FuzzyOperation.XOR =>
        FuzzySet(s"$name XOR ${that.name}", elements ++ that.elements.map {
          case (k, v) => k -> round(math.abs(v - membership(k)))
        })
    }

    // Recursively evaluate nested sets if they exist
    val nestedEval = nestedSets.map(_.view.mapValues(_.eval(op, that)).toMap)

    primaryEval.copy(nestedSets = nestedEval)
  }

  // Partial evaluation of fuzzy set operations
  def partialEval(op: FuzzyOperation, that: Option[FuzzySet] = None): Either[FuzzySet, String] = {
    try {
      // Attempt full evaluation
      val evaluatedSet = eval(op, that.getOrElse(this))
      Left(evaluatedSet)
    } catch {
      case _: Exception =>
        // Return a partially evaluated expression if full evaluation fails
        Right(s"Cannot fully evaluate $name with operation $op")
    }
  }

  // Alpha cut method trims fuzzy set elements below a threshold value
  def alphaCut(threshold: Double): FuzzySet = {
    val filteredElements = elements.filter { case (_, value) => value >= threshold }
    val filteredNested = nestedSets.map(_.view.mapValues(_.alphaCut(threshold)).filter(_._2.elements.nonEmpty).toMap)
    FuzzySet(name, filteredElements, filteredNested)
  }

  // String representation of the fuzzy set, including nested sets if present
  override def toString: String = {
    val elementStr = elements.map { case (k, v) => s"$k -> ${round(v)}" }.mkString(", ")
    val nestedStr = nestedSets.map(_.map { case (k, v) => s"$k -> $v" }.mkString(", ")).getOrElse("")
    s"FuzzySet($name): {$elementStr ${if (nestedStr.nonEmpty) s", NestedSets: {$nestedStr}" else ""}}"
  }
}