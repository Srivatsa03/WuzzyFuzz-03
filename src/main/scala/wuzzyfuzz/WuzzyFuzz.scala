package wuzzyfuzz

object WuzzyFuzz {
  def main(args: Array[String]): Unit = {

    // === Define Fuzzy Sets ===
    val setA = FuzzySet("A", Map("x1" -> 0.3, "x2" -> 0.9))
    val setB = FuzzySet("B", Map("x1" -> 0.6, "x2" -> 0.7))

    // Create a global scope and assign fuzzy sets to it
    val globalScope = new Scope().assign("A", Left(setA)).assign("B", Left(setB))

    // === Fuzzy Set Operations ===
    println("=== Fuzzy Set Operations ===")
    val unionAB = setA.eval(FuzzyOperation.Union, setB)
    val intersectionAB = setA.eval(FuzzyOperation.Intersection, setB)
    val complementA = setA.eval(FuzzyOperation.Complement)
    val addAB = setA.eval(FuzzyOperation.Addition, setB)
    val multAB = setA.eval(FuzzyOperation.Multiplication, setB)
    val xorAB = setA.eval(FuzzyOperation.XOR, setB)

    println("Union of A and B:")
    println(unionAB)
    println("Intersection of A and B:")
    println(intersectionAB)
    println("Complement of A:")
    println(complementA)
    println("Addition of A and B:")
    println(addAB)
    println("Multiplication of A and B:")
    println(multAB)
    println("XOR of A and B:")
    println(xorAB)

    val alphaCutA = setA.alphaCut(0.7)
    println("Alpha cut of A with threshold 0.7:")
    println(alphaCutA)

    // === Partial Evaluation for Fuzzy Gates ===
    println("\n=== Partial Evaluation for Fuzzy Gates ===")

    val gates = Seq(
      new UnionGate(),
      new IntersectionGate(),
      new ComplementGate(),
      new AdditionGate(),
      new MultiplicationGate(),
      new XORGate()
    )

    val unresolvedInput = Right("Partially defined fuzzy set")
    val resolvedInput = Left(setA)

    gates.foreach { gate =>
      println(s"Testing gate: ${gate.name}")
      val partialEvalResult = gate.partialEval(unresolvedInput)
      println(s"Partial evaluation result: $partialEvalResult")
      val resolvedEvalResult = gate.partialEval(resolvedInput)
      println(s"Resolved evaluation result: $resolvedEvalResult\n")
    }

    // === Conditional Constructs ===
    println("\n=== Conditional Constructs ===")
    val condition = Conditional(
      (a, b) => a.membership("x1") > b.membership("x1"),
      () => println("Condition is TRUE: Executing THEN block."),
      () => println("Condition is FALSE: Executing ELSE block.")
    )
    condition.executeCondition(setA, setB)

    val partialConditionResult = condition.partialEval(setA, setB)
    println(s"Partial evaluation result for condition: $partialConditionResult")

    val condition1 = Conditional(
      (a, b) => a.membership("x1") < b.membership("x1"),
      () => println("Condition is TRUE: Executing THEN block."),
      () => println("Condition is FALSE: Executing ELSE block.")
    )
    condition1.executeCondition(setA, setB)

    val partialConditionResult1 = condition1.partialEval(setA, setB)
    println(s"Partial evaluation result for condition: $partialConditionResult1")

    // === Partial Evaluation Examples ===
    println("\n=== Partial Evaluation Examples ===")

    val example1 = Multiply(Value(3), Multiply(Add(Value(5), Value(1)), Variable("var")))
    val simplifiedExample1Step1 = PartialEvaluation.simplifyExpression(example1)
    val simplifiedExample1Step2 = PartialEvaluation.simplifyExpression(simplifiedExample1Step1)
    println(s"Initial Expression: $example1")
    println(s"Simplified Expression Step 1: $simplifiedExample1Step1")
    println(s"Simplified Expression Step 2: $simplifiedExample1Step2")

    val example2 = Multiply(Value(3), Multiply(Value(5), Variable("var")))
    val simplifiedExample2 = PartialEvaluation.simplifyExpression(example2)
    println(s"Initial Expression: $example2")
    println(s"Simplified Expression: $simplifiedExample2")

    // === Advanced Partial Evaluation ===
    println("\n=== Advanced Partial Evaluation ===")
    val complexExpression = Multiply(
      Multiply(Value(5), Value(10)), // Nested multiplication
      Add(Value(10), Multiply(Variable("unknown"), Value(2))) // Addition with a nested multiply
    )
    val simplifiedComplexStep1 = PartialEvaluation.simplifyExpression(complexExpression)
    val simplifiedComplexStep2 = PartialEvaluation.simplifyExpression(simplifiedComplexStep1)
    println(s"Initial Complex Expression: $complexExpression")
    println(s"Simplified Expression Step 1: $simplifiedComplexStep1")
    println(s"Simplified Expression Step 2: $simplifiedComplexStep2")

    // === Testing a Fuzzy Gate ===
    println("\n=== Testing a Fuzzy Gate with Partial Evaluation ===")
    val gateScope = globalScope.createChildScope()
      .assign("x1", Left(FuzzySet("C", Map("x1" -> 0.6))))
      .assign("x2", Left(FuzzySet("C", Map("x2" -> 0.9))))

    val gate1 = new UnionGate()
    val partialGateTest = Assign(gate1, Right("Unresolved fuzzy set"))
    val testResultPartial = TestGate(partialGateTest, "x1", 0.3, gateScope)
    println(s"Partial test result for 'x1': $testResultPartial")



  }
}