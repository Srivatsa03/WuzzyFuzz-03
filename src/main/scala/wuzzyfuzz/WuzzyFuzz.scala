package wuzzyfuzz

object WuzzyFuzz {
  def main(args: Array[String]): Unit = {

    // === Define Fuzzy Sets ===
    // Defining two fuzzy sets A and B with membership values for variables x1 and x2
    val setA = FuzzySet("A", Map("x1" -> 0.3, "x2" -> 0.9))
    val setB = FuzzySet("B", Map("x1" -> 0.6, "x2" -> 0.7))

    // Creating a global scope and assigning fuzzy sets A and B to variables "A" and "B"
    val globalScope = new Scope().assign("A", Left(setA)).assign("B", Left(setB))

    // === Fuzzy Set Operations ===
    // Demonstrates core fuzzy operations like Union, Intersection, Complement, Addition, etc.
    println("=== Fuzzy Set Operations ===")
    val unionAB = setA.eval(FuzzyOperation.Union, setB) // Union operation
    val intersectionAB = setA.eval(FuzzyOperation.Intersection, setB) // Intersection operation
    val complementA = setA.eval(FuzzyOperation.Complement) // Complement of set A
    val addAB = setA.eval(FuzzyOperation.Addition, setB) // Addition operation
    val multAB = setA.eval(FuzzyOperation.Multiplication, setB) // Multiplication operation
    val xorAB = setA.eval(FuzzyOperation.XOR, setB) // XOR operation

    // Printing results of each fuzzy set operation
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

    // Perform and display the result of an alpha cut on set A with a threshold of 0.7
    val alphaCutA = setA.alphaCut(0.7)
    println("Alpha cut of A with threshold 0.7:")
    println(alphaCutA)

    // === Partial Evaluation for Fuzzy Gates ===
    // Demonstrating partial evaluation for different fuzzy gates like Union, Intersection, Complement, etc.
    println("\n=== Partial Evaluation for Fuzzy Gates ===")

    // Define a list of fuzzy gates
    val gates = Seq(
      new UnionGate(),
      new IntersectionGate(),
      new ComplementGate(),
      new AdditionGate(),
      new MultiplicationGate(),
      new XORGate()
    )

    // Partial evaluation using unresolved (string-based) input and resolved (fuzzy set) input
    val unresolvedInput = Right("Partially defined fuzzy set")
    val resolvedInput = Left(setA)

    gates.foreach { gate =>
      println(s"Testing gate: ${gate.name}")
      val partialEvalResult = gate.partialEval(unresolvedInput) // Partial evaluation with unresolved input
      println(s"Partial evaluation result: $partialEvalResult")
      val resolvedEvalResult = gate.partialEval(resolvedInput) // Full evaluation with resolved input
      println(s"Resolved evaluation result: $resolvedEvalResult\n")
    }

    // === Conditional Constructs ===
    // Demonstrating conditional constructs with THEN and ELSE branches
    println("\n=== Conditional Constructs ===")
    val condition = Conditional(
      (a, b) => a.membership("x1") > b.membership("x1"), // Check if x1 in set A is greater than x1 in set B
      () => println("Condition is TRUE: Executing THEN block."), // Action if condition is TRUE
      () => println("Condition is FALSE: Executing ELSE block.") // Action if condition is FALSE
    )
    condition.executeCondition(setA, setB) // Execute the condition with fuzzy sets A and B

    val partialConditionResult = condition.partialEval(setA, setB) // Partial evaluation of the condition
    println(s"Partial evaluation result for condition: $partialConditionResult")

    // Testing another conditional with reversed logic
    val condition1 = Conditional(
      (a, b) => a.membership("x1") < b.membership("x1"), // Check if x1 in set A is less than x1 in set B
      () => println("Condition is TRUE: Executing THEN block."),
      () => println("Condition is FALSE: Executing ELSE block.")
    )
    condition1.executeCondition(setA, setB)

    val partialConditionResult1 = condition1.partialEval(setA, setB)
    println(s"Partial evaluation result for condition: $partialConditionResult1")

    // === Partial Evaluation Examples ===
    // Demonstrating partial evaluation for arithmetic-like expressions
    println("\n=== Partial Evaluation Examples ===")

    // Define an arithmetic expression and simplify it step-by-step
    val example1 = Multiply(Value(3), Multiply(Add(Value(5), Value(1)), Variable("var")))
    val simplifiedExample1Step1 = PartialEvaluation.simplifyExpression(example1) // Simplify one level
    val simplifiedExample1Step2 = PartialEvaluation.simplifyExpression(simplifiedExample1Step1) // Fully simplify
    println(s"Initial Expression: $example1")
    println(s"Simplified Expression Step 1: $simplifiedExample1Step1")
    println(s"Simplified Expression Step 2: $simplifiedExample1Step2")

    // Define another expression with less complexity and simplify
    val example2 = Multiply(Value(3), Multiply(Value(5), Variable("var")))
    val simplifiedExample2 = PartialEvaluation.simplifyExpression(example2) // Simplify directly
    println(s"Initial Expression: $example2")
    println(s"Simplified Expression: $simplifiedExample2")

    // === AD Partial Evaluation ===
    // Demonstrating advanced nested expressions with partial evaluation
    println("\n=== other Partial Evaluation examples===")
    val complexExpression = Multiply(
      Multiply(Value(5), Value(10)), // Multiply two constants
      Add(Value(10), Multiply(Variable("unknown"), Value(2))) // Addition with a nested multiplication
    )
    val simplifiedComplexStep1 = PartialEvaluation.simplifyExpression(complexExpression) // Simplify first level
    val simplifiedComplexStep2 = PartialEvaluation.simplifyExpression(simplifiedComplexStep1) // Fully simplify
    println(s"Initial Complex Expression: $complexExpression")
    println(s"Simplified Expression Step 1: $simplifiedComplexStep1")
    println(s"Simplified Expression Step 2: $simplifiedComplexStep2")

    // === Testing a Fuzzy Gate ===
    // Demonstrating partial evaluation and testing of fuzzy gates in a specific scope
    println("\n=== Testing a Fuzzy Gate with Partial Evaluation ===")
    val gateScope = globalScope.createChildScope()
      .assign("x1", Left(FuzzySet("C", Map("x1" -> 0.6)))) // Assign a new fuzzy set to x1
      .assign("x2", Left(FuzzySet("C", Map("x2" -> 0.9)))) // Assign a new fuzzy set to x2

    val gate1 = new UnionGate()
    val partialGateTest = Assign(gate1, Right("Unresolved fuzzy set")) // Partially define the gate
    val testResultPartial = TestGate(partialGateTest, "x1", 0.3, gateScope) // Test partial evaluation
    println(s"Partial test result for 'x1': $testResultPartial")
  }
}