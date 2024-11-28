package wuzzyfuzz

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class WuzzyFuzzTests extends AnyFlatSpec with Matchers {

  // Describing the behavior and objective of the WuzzyFuzz language tests
  behavior of "My WuzzyFuzz Language for Homework 1, Homework 2, and Homework 3"

  val epsilon = 1e-9  // A small threshold for checking floating-point equality in fuzzy operations

  // Define common fuzzy sets to be reused across multiple test cases
  val setA: FuzzySet = FuzzySet("A", Map("x1" -> 0.3, "x2" -> 0.9))
  val setB: FuzzySet = FuzzySet("B", Map("x1" -> 0.6, "x2" -> 0.7))

  // ==== Homework 1 & 2 Tests: Basic Fuzzy Operations and OOP Features ====
  it should "perform fuzzy union correctly" in {
    // The union operation takes the maximum membership value for each element
    val result = setA.eval(FuzzyOperation.Union, setB).elements
    // Expected result: maximum of {x1: 0.3 vs 0.6, x2: 0.9 vs 0.7}
    result shouldBe Map("x1" -> 0.6, "x2" -> 0.9)
  }

  // Test the intersection operation between two fuzzy sets
  it should "perform fuzzy intersection correctly" in {
    // The intersection operation takes the minimum membership value for each element
    val result = setA.eval(FuzzyOperation.Intersection, setB).elements
    // Expected result: minimum of {x1: 0.3 vs 0.6, x2: 0.9 vs 0.7}
    result shouldBe Map("x1" -> 0.3, "x2" -> 0.7)
  }

  // Test the complement operation on a fuzzy set
  it should "perform fuzzy complement correctly" in {
    // The complement operation subtracts each membership value from 1
    val result = setA.eval(FuzzyOperation.Complement).elements
    // Expected result: {x1 -> 1 - 0.3 = 0.7, x2 -> 1 - 0.9 = 0.1}
    result shouldBe Map("x1" -> 0.7, "x2" -> 0.1)
  }

  // Test the addition operation between two fuzzy sets
  it should "perform fuzzy addition correctly" in {
    // The addition operation sums the membership values, capping each result at 1
    val result = setA.eval(FuzzyOperation.Addition, setB).elements
    // Expected result: {x1 -> min(0.3 + 0.6, 1.0), x2 -> min(0.9 + 0.7, 1.0)}
    result shouldBe Map("x1" -> 0.9, "x2" -> 1.0)
  }

  // Test the multiplication operation between two fuzzy sets
  it should "perform fuzzy multiplication correctly" in {
    // The multiplication operation multiplies the membership values for each element
    val result = setA.eval(FuzzyOperation.Multiplication, setB).elements
    // Expected result: {x1 -> 0.3 * 0.6 = 0.18, x2 -> 0.9 * 0.7 = 0.63}
    result shouldBe Map("x1" -> 0.18, "x2" -> 0.63)
  }

  // Test the XOR operation between two fuzzy sets
  it should "perform fuzzy XOR correctly" in {
    // The XOR operation calculates the absolute difference of the membership values
    val result = setA.eval(FuzzyOperation.XOR, setB).elements
    // Expected result: {x1 -> |0.3 - 0.6| = 0.3, x2 -> |0.9 - 0.7| = 0.2}
    result shouldBe Map("x1" -> 0.3, "x2" -> 0.2)
  }

  // Test the alpha cut operation on a fuzzy set
  it should "perform alpha cut correctly" in {
    // The alpha cut operation filters out elements with membership values below the threshold
    val result = setA.alphaCut(0.7).elements
    // Expected result: Only elements with membership >= 0.7 are retained
    result shouldBe Map("x2" -> 0.9)
  }

  // ==== Homework 2 Tests: Object-Oriented Features ====

  // Test class instantiation and inheritance of variables
  it should "instantiate classes and inherit variables correctly" in {
    // Define a base class with one variable and a derived class that inherits from it
    val baseClass = Class("Base", variables = Map("v1" -> VarType("int")))
    val derivedClass = Class(
      "Derived",
      superclass = Some(baseClass), // The derived class extends the base class
      variables = Map("v2" -> VarType("string")), // The derived class adds its own variable
      methods = Map("m1" -> Method("m1", List(Parameter("p1", VarType("int"))), List(), VarType("int"))) // Dummy method
    )

    // Create instances for both classes
    val baseInstance = ClassInstance(baseClass, Map("v1" -> Right("42"))) // Assign "v1" to "42" in the base instance
    val derivedInstance = ClassInstance(derivedClass, Map("v2" -> Right("Hello"))) // Assign "v2" to "Hello" in the derived instance

    // Confirm the derived instance has access to its own and inherited variables
    assert(derivedInstance.variables.contains("v2")) // Check derived variable
    assert(derivedInstance.variables("v2") == Right("Hello")) // Verify the value of derived variable
    assert(derivedInstance.classDef.superclass.contains(baseClass)) // Ensure the superclass is linked

    assert(baseInstance.variables.contains("v1")) // Check base variable
    assert(baseInstance.variables("v1") == Right("42")) // Verify the value of base variable
  }

  // Test invoking methods from the derived class
  it should "invoke methods and return the correct values from the derived class" in {
    // Define a base class and a derived class with a method performing a union operation
    val baseClass = Class("Base", variables = Map("v1" -> VarType("int")))
    val derivedClass = Class(
      "Derived",
      superclass = Some(baseClass), // Derived class extends the base class
      methods = Map(
        "m1" -> Method(
          "m1",
          List(Parameter("p1", VarType("int"))), // Method with one parameter
          List(Assign(new UnionGate(), Left(FuzzySet("C", Map("x1" -> 0.8))))), // Performs a union operation
          VarType("int")
        )
      )
    )

    // Instantiate the derived class and invoke the method
    val globalScope = new Scope()
    val derivedInstance = ClassInstance(derivedClass)
    val methodInvoker = new MethodInvocation(derivedInstance, globalScope)

    // Pass the parameter wrapped in Right and invoke the method
    val result = methodInvoker.invokeMethod("m1", Map("p1" -> Right("5")))

    // Check the expected result wrapped in Left
    result shouldBe Left(FuzzySet("C Union C", Map("x1" -> 0.8)))
  }

  // Test resolving variables in nested and outer classes
  it should "correctly resolve variables in nested and outer classes" in {
    // Define an outer class with a variable and a nested class with a variable of the same name
    val outerClass = Class("Outer", variables = Map("v1" -> VarType("int")))
    val nestedClass = Class(
      "Nested",
      superclass = Some(outerClass), // Nested class extends the outer class
      variables = Map("v1" -> VarType("string")) // The nested class overrides the "v1" variable
    )

    // Instantiate both the outer and nested classes
    val outerInstance = ClassInstance(outerClass, Map("v1" -> Right("42"))) // Assign "v1" to "42" in the outer class
    val nestedInstance = ClassInstance(nestedClass, Map("v1" -> Right("Hello"))) // Assign "v1" to "Hello" in the nested class

    val nested = new NestedClass()

    // Confirm that the nested class variable is resolved within its own scope
    val nestedVar = nested.resolveVariable(nestedInstance, "v1")
    assert(nestedVar.contains("Hello")) // Expect "Hello" from the nested class

    // Confirm that the outer class variable is accessible via explicit access
    val outerVar = nested.accessOuterClassVariable(outerInstance, "v1")
    assert(outerVar.contains("42")) // Expect "42" from the outer class
  }

  // Test invoking methods in nested classes
  it should "invoke methods in nested classes correctly" in {
    // Define an outer class and a nested class with a method performing a union operation
    val outerClass = Class("Outer", variables = Map("v1" -> VarType("int")))
    val nestedClass = Class(
      "Nested",
      superclass = Some(outerClass), // Nested class extends the outer class
      methods = Map(
        "m1" -> Method(
          "m1",
          List(), // Method has no parameters
          List(Assign(new UnionGate(), Left(FuzzySet("NestedSet", Map("x1" -> 0.7))))), // Union operation on "NestedSet"
          VarType("int")
        )
      )
    )

    // Invoke the method "m1" from the nested class instance
    val nestedInstance = ClassInstance(nestedClass)
    val methodInvoker = new MethodInvocation(nestedInstance, new Scope())
    val result = methodInvoker.invokeMethod("m1", Map())

    // Check the expected result wrapped in Left
    result shouldBe Left(FuzzySet("NestedSet Union NestedSet", Map("x1" -> 0.7)))
  }

  // Test applying fuzzy gates with scoped variables
  it should "apply fuzzy gates with scoped variables" in {
    // Define a global scope and assign fuzzy sets to variables
    val globalScope = new Scope()
      .assign("x1", Left(FuzzySet("A", Map("x1" -> 0.3)))) // Assign "x1" with a fuzzy set
      .assign("x2", Left(FuzzySet("A", Map("x2" -> 0.9)))) // Assign "x2" with a fuzzy set

    // Create a complement gate and assign it to a fuzzy set
    val gate = new ComplementGate()
    val assignedGate = Assign(gate, Left(setA))

    // Test the gate results in the scope for each variable
    val testResult1 = TestGate(assignedGate, "x1", 0.7, globalScope)
    val testResult2 = TestGate(assignedGate, "x2", 0.1, globalScope)

    // Assert both results are correct
    assert(testResult1, "testResult1 was false")
    assert(testResult2, "testResult2 was false")
  }

  // Test dynamic method dispatch between derived and base classes
  it should "dynamically dispatch methods from derived class and resolve superclass method if not found in derived class" in {
    // Define a base class with a method and a derived class with its own method
    val baseClass = Class(
      "Base",
      methods = Map(
        "m2" -> Method("m2", List(), List(Assign(new UnionGate(), Left(FuzzySet("BaseSet", Map("x1" -> 0.5))))), VarType("fuzzySet"))
      )
    )

    val derivedClass = Class(
      "Derived",
      superclass = Some(baseClass), // Derived class extends the base class
      methods = Map(
        "m1" -> Method("m1", List(Parameter("p1", VarType("fuzzySet"))), List(Assign(new UnionGate(), Left(FuzzySet("DerivedSet", Map("x1" -> 0.7))))), VarType("fuzzySet"))
      )
    )

    // Instantiate the derived class and create a method invoker
    val derivedInstance = ClassInstance(derivedClass)
    val scope = new Scope()
    val methodInvoker = new MethodInvocation(derivedInstance, scope)

    // Pass a FuzzySet parameter to the derived class method and check the result
    val result1 = methodInvoker.invokeMethod("m1", Map("p1" -> Left(FuzzySet("ParamSet", Map("x1" -> 5.0)))))
    assert(result1 == Left(FuzzySet("DerivedSet Union DerivedSet", Map("x1" -> 0.7)))) // Verify derived class result

    // Invoke the base class method and check the result
    val result2 = methodInvoker.invokeMethod("m2", Map())
    assert(result2 == Left(FuzzySet("BaseSet Union BaseSet", Map("x1" -> 0.5)))) // Verify base class result
  }

  // Test applying alpha cut to method results
  it should "apply alpha cut to method results" in {
    // Define a derived class with a method returning a fuzzy set
    val baseClass = Class("Base", variables = Map("v1" -> VarType("int")))
    val derivedClass = Class(
      "Derived",
      superclass = Some(baseClass),
      methods = Map(
        "m1" -> Method(
          "m1",
          List(Parameter("p1", VarType("int"))),
          List(Assign(new UnionGate(), Left(FuzzySet("C", Map("x1" -> 0.8))))), // Union operation on "C"
          VarType("int")
        )
      )
    )

    val globalScope = new Scope()
    val derivedInstance = ClassInstance(derivedClass)
    val methodInvoker = new MethodInvocation(derivedInstance, globalScope)

    // Invoke the method and evaluate the result
    val result = methodInvoker.invokeMethod("m1", Map("p1" -> Right("5"))) // Use Right for the parameter

    // Apply alpha cut to filter results with membership values above 0.7
    val alphaCutResult = result match {
      case Left(fuzzySet) => fuzzySet.alphaCut(0.7).elements
      case Right(errorMessage) => fail(s"Method evaluation failed with error: $errorMessage")
    }

    // Verify that only values above the threshold are retained
    alphaCutResult shouldBe Map("x1" -> 0.8)
  }

  // ==== Homework 3 Tests: Partial Evaluation for Fuzzy Gates ====

  // Test partial evaluation of the Union gate
  it should "partially evaluate Union gate correctly" in {
    val gate = new UnionGate()
    val assign = Assign(gate, Left(setA)) // Assign Union gate to fuzzy set A

    val result = assign.eval()
    result shouldBe Left(FuzzySet("A Union A", Map("x1" -> 0.3, "x2" -> 0.9))) // Result should match union operation
  }

  // Test partial evaluation of the Intersection gate
  it should "partially evaluate Intersection gate correctly" in {
    val gate = new IntersectionGate()
    val assign = Assign(gate, Left(setA)) // Assign Intersection gate to fuzzy set A

    val result = assign.eval()
    result shouldBe Left(FuzzySet("A Intersection A", Map("x1" -> 0.3, "x2" -> 0.9))) // Result should match intersection operation
  }

  // Test partial evaluation of the Complement gate
  it should "partially evaluate Complement gate correctly" in {
    val gate = new ComplementGate()
    val assign = Assign(gate, Left(setA)) // Assign Complement gate to fuzzy set A

    val result = assign.eval()
    result shouldBe Left(FuzzySet("Complement of A", Map("x1" -> 0.7, "x2" -> 0.1))) // Result should match complement operation
  }

  // Test partial evaluation of the Addition gate
  it should "partially evaluate Addition gate correctly" in {
    val gate = new AdditionGate()
    val assign = Assign(gate, Left(setA)) // Assign Addition gate to fuzzy set A

    val result = assign.eval()
    result shouldBe Left(FuzzySet("A Addition A", Map("x1" -> 0.6, "x2" -> 1.0))) // Result should match addition operation
  }

  // Test partial evaluation of the Multiplication gate
  it should "partially evaluate Multiplication gate correctly" in {
    val gate = new MultiplicationGate()
    val assign = Assign(gate, Left(setA)) // Assign Multiplication gate to fuzzy set A

    val result = assign.eval()
    result shouldBe Left(FuzzySet("A Multiplication A", Map("x1" -> 0.09, "x2" -> 0.81))) // Result should match multiplication operation
  }

  // Test partial evaluation of the XOR gate
  it should "partially evaluate XOR gate correctly" in {
    val gate = new XORGate()
    val assign = Assign(gate, Left(setA)) // Assign XOR gate to fuzzy set A

    val result = assign.eval()
    result shouldBe Left(FuzzySet("A XOR A", Map("x1" -> 0.0, "x2" -> 0.0))) // Result should match XOR operation
  }

  // ==== Homework 3 Tests: Partial Evaluation ====

  // Test partial evaluation of expressions with nested arithmetic
  it should "partially evaluate expressions" in {
    val expression = Multiply(Value(3), Multiply(Add(Value(5), Value(1)), Variable("var")))
    val simplified1 = PartialEvaluation.simplifyExpression(expression) // First simplification
    val simplified2 = PartialEvaluation.simplifyExpression(simplified1) // Second simplification

    simplified1 shouldBe Multiply(Value(3), Multiply(Value(6), Variable("var"))) // Check intermediate result
    simplified2 shouldBe Multiply(Value(18), Variable("var")) // Check fully simplified result
  }

  // Test partial evaluation of more complex expressions with undefined variables
  it should "partially evaluate complex expressions" in {
    val expression = Multiply(Multiply(Value(5), Value(10)), Add(Value(10), Multiply(Variable("unknown"), Value(2))))
    val simplified1 = PartialEvaluation.simplifyExpression(expression) // First simplification
    val simplified2 = PartialEvaluation.simplifyExpression(simplified1) // Second simplification

    simplified1 shouldBe Multiply(Value(50), Add(Value(10), Multiply(Variable("unknown"), Value(2)))) // Intermediate result
    simplified2 shouldBe Multiply(Value(50), Add(Value(10), Multiply(Variable("unknown"), Value(2)))) // Fully simplified result
  }

  // Test full evaluation of expressions when all variables are defined
  it should "fully evaluate expressions when all variables are defined in scope" in {
    val scope = new Scope()
      .assign("var", Left(FuzzySet("var", Map("x1" -> 0.5)))) // Assign a fuzzy set to variable "var"

    val expr = Multiply(Value(3), Multiply(Add(Value(5), Value(1)), Variable("var"))) // Define an expression

    val partiallyEvaluatedExpr = PartialEvaluation.simplifyExpression(expr) // Simplify the expression
    val fullyEvaluatedResult = partiallyEvaluatedExpr match {
      case Multiply(Value(a), Multiply(Value(b), Variable(name))) =>
        scope.lookup(name) match {
          case Some(Left(fuzzySet: FuzzySet)) =>
            val variableValue = fuzzySet.elements.getOrElse("x1", 1.0) // Lookup "x1" in the fuzzy set
            Value((a * b * variableValue).toInt) // Compute the result
          case _ => throw new Exception(s"Variable '$name' not found or partially defined")
        }
      case other => other
    }

    assert(fullyEvaluatedResult == Value(9)) // Verify the fully evaluated result
  }

  // Test resolving and fully evaluating fuzzy gates
  it should "resolve and evaluate fuzzy gates when fully defined" in {
    val gate = new ComplementGate()
    val assign = Assign(gate, Left(setA)) // Assign Complement gate to fuzzy set A

    val result = assign.eval()
    result shouldBe Left(setA.eval(FuzzyOperation.Complement)) // Ensure the complement operation is correct
  }

  // ==== Homework 3 Tests: Conditional Constructs ====

  // Test conditional constructs with THEN and ELSE branches
  it should "evaluate conditional constructs with THEN and ELSE branches" in {
    val condition = Conditional(
      (a, b) => a.membership("x1") > b.membership("x1"), // Condition: Compare memberships of "x1"
      () => println("THEN block executed"), // Action for THEN branch
      () => println("ELSE block executed") // Action for ELSE branch
    )

    condition.executeCondition(setA, setB) // Execute the condition with fuzzy sets A and B
    condition.partialEval(setA, setB) shouldBe Left("ELSE branch executed successfully") // Check evaluation result
  }

  // Test partial evaluation of conditional constructs
  it should "partially evaluate conditional constructs" in {
    val condition = Conditional(
      (a, b) => a.membership("x2") + b.membership("x2") > 1.5, // Condition for partial evaluation
      () => println("THEN block executed"), // THEN branch
      () => println("ELSE block executed") // ELSE branch
    )

    condition.partialEval(setA, setB) shouldBe Left("THEN branch executed successfully") // Check partial evaluation result
  }

  // ==== Homework 3 other Features ====

  // Test partial evaluation for gates with unresolved inputs
  it should "test partial evaluation for gates with unresolved inputs" in {
    val gate = new IntersectionGate()
    val assign = Assign(gate, Right("Unresolved fuzzy set")) // Assign unresolved input to the gate

    val result = assign.eval()
    result shouldBe Right("Partial evaluation of gate 'Intersection' with expression: Unresolved fuzzy set") // Check partial evaluation
  }

  // Test advanced partial evaluation with simplified nested expressions
  it should "handle advanced partial evaluation with simplified nested expressions" in {
    val expression = Multiply(Value(3), Multiply(Value(4), Variable("unknown"))) // Nested expression
    val simplified1 = PartialEvaluation.simplifyExpression(expression) // Simplify expression
    val simplified2 = PartialEvaluation.simplifyExpression(simplified1) // Further simplify

    simplified1 shouldBe Multiply(Value(12), Variable("unknown")) // Intermediate result
    simplified2 shouldBe Multiply(Value(12), Variable("unknown")) // Fully simplified result
  }

  // Test fuzzy gate evaluation within a scope
  it should "test fuzzy gate evaluation within a scope" in {
    val scope = new Scope().assign("x1", Left(FuzzySet("A", Map("x1" -> 0.3)))) // Assign fuzzy set to "x1"
    val gate = new ComplementGate()
    val assign = Assign(gate, Left(setA)) // Assign Complement gate to fuzzy set A

    val testResult = TestGate(assign, "x1", 0.7, scope) // Evaluate gate within the scope
    testResult shouldBe true // Ensure evaluation matches expected result
  }

  // Test conditional expressions when variables are undefined
  it should "partially evaluate conditional expressions when variables are undefined" in {
    val scope = new Scope().assign("var1", Left(FuzzySet("var1", Map("x2" -> 2.0)))) // Assign fuzzy set to "var1"

    val conditional = Conditional(
      (a, b) => a.membership("x1") >= b.membership("x2"), // Condition for evaluation
      () => println("THEN block executed"), // THEN branch
      () => println("ELSE block executed") // ELSE branch
    )

    val partialResult = conditional.partialEval(
      FuzzySet("ConditionSetA", Map("x1" -> 15.0)),
      FuzzySet("ConditionSetB", Map("x2" -> 2.0))
    )

    partialResult shouldBe Left("THEN branch executed successfully") // Check partial evaluation result
  }
}