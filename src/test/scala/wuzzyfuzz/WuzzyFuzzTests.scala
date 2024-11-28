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

  it should "instantiate classes and inherit variables correctly" in {
    // Define a base class and a derived class that inherits variables from the base
    val baseClass = Class("Base", variables = Map("v1" -> VarType("int")))
    val derivedClass = Class(
      "Derived",
      superclass = Some(baseClass),
      variables = Map("v2" -> VarType("string")),
      methods = Map("m1" -> Method("m1", List(Parameter("p1", VarType("int"))), List(), VarType("int")))
    )

    // Create instances for both classes, wrapping variables in `Right`
    val baseInstance = ClassInstance(baseClass, Map("v1" -> Right("42"))) // Wrap in Right for `String` type
    val derivedInstance = ClassInstance(derivedClass, Map("v2" -> Right("Hello"))) // Wrap in Right

    // Confirm the derived instance has access to its own and inherited variables
    assert(derivedInstance.variables.contains("v2"))
    assert(derivedInstance.variables("v2") == Right("Hello")) // Verify the value
    assert(derivedInstance.classDef.superclass.contains(baseClass))

    assert(baseInstance.variables.contains("v1"))
    assert(baseInstance.variables("v1") == Right("42")) // Verify the value
  }

  it should "invoke methods and return the correct values from the derived class" in {
    // Define a base class and a derived class, adding a method in the derived class for a union operation
    val baseClass = Class("Base", variables = Map("v1" -> VarType("int")))
    val derivedClass = Class(
      "Derived",
      superclass = Some(baseClass),
      methods = Map(
        "m1" -> Method(
          "m1",
          List(Parameter("p1", VarType("int"))),
          List(Assign(new UnionGate(), Left(FuzzySet("C", Map("x1" -> 0.8))))), // Wrap FuzzySet in Left
          VarType("int")
        )
      )
    )

    // Instantiate derived class and invoke method "m1"
    val globalScope = new Scope()
    val derivedInstance = ClassInstance(derivedClass)
    val methodInvoker = new MethodInvocation(derivedInstance, globalScope)

    // Pass the parameter wrapped in Right
    val result = methodInvoker.invokeMethod("m1", Map("p1" -> Right("5")))

    // Expected result should also be wrapped in Left
    result shouldBe Left(FuzzySet("C Union C", Map("x1" -> 0.8)))
  }

  it should "correctly resolve variables in nested and outer classes" in {
    // Define an outer class with a variable and a nested class with a variable of the same name
    val outerClass = Class("Outer", variables = Map("v1" -> VarType("int")))
    val nestedClass = Class(
      "Nested",
      superclass = Some(outerClass),
      variables = Map("v1" -> VarType("string"))
    )

    // Instantiate outer and nested class instances
    val outerInstance = ClassInstance(outerClass, Map("v1" -> Right("42"))) // Wrap 42 in Right
    val nestedInstance = ClassInstance(nestedClass, Map("v1" -> Right("Hello"))) // Wrap "Hello" in Right

    val nested = new NestedClass()

    // Confirm that the nested class variable is resolved within its scope, while outer class variable is accessible via outer access
    val nestedVar = nested.resolveVariable(nestedInstance, "v1")
    assert(nestedVar.contains("Hello")) // Expecting "Hello" to be resolved from nested class

    val outerVar = nested.accessOuterClassVariable(outerInstance, "v1")
    assert(outerVar.contains("42")) // Expecting "42" to be resolved from outer class
  }

  it should "invoke methods in nested classes correctly" in {
    // Define an outer class and a nested class, with a method in the nested class for union operation
    val outerClass = Class("Outer", variables = Map("v1" -> VarType("int")))
    val nestedClass = Class(
      "Nested",
      superclass = Some(outerClass),
      methods = Map(
        "m1" -> Method(
          "m1",
          List(),
          List(Assign(new UnionGate(), Left(FuzzySet("NestedSet", Map("x1" -> 0.7))))), // Wrap the FuzzySet in Left
          VarType("int")
        )
      )
    )

    // Invoke method "m1" in the nested class instance
    val nestedInstance = ClassInstance(nestedClass)
    val methodInvoker = new MethodInvocation(nestedInstance, new Scope())
    val result = methodInvoker.invokeMethod("m1", Map())

    // Expected result after invoking "m1"
    result shouldBe Left(FuzzySet("NestedSet Union NestedSet", Map("x1" -> 0.7)))
  }

  it should "apply fuzzy gates with scoped variables" in {
    // Define a scope with fuzzy sets and test a complement gate operation
    val globalScope = new Scope()
      .assign("x1", Left(FuzzySet("A", Map("x1" -> 0.3)))) // Wrap FuzzySet in Left
      .assign("x2", Left(FuzzySet("A", Map("x2" -> 0.9)))) // Wrap FuzzySet in Left

    val gate = new ComplementGate() // Use ComplementGate instead of FuzzyGate
    val assignedGate = Assign(gate, Left(setA)) // Use Left(setA) instead of LeftA

    // Test the gate results in the scope for each variable
    val testResult1 = TestGate(assignedGate, "x1", 0.7, globalScope)
    val testResult2 = TestGate(assignedGate, "x2", 0.1, globalScope)

    assert(testResult1, "testResult1 was false")
    assert(testResult2, "testResult2 was false")
  }

  it should "dynamically dispatch methods from derived class and resolve superclass method if not found in derived class" in {
    val baseClass = Class(
      "Base",
      methods = Map(
        "m2" -> Method("m2", List(), List(Assign(new UnionGate(), Left(FuzzySet("BaseSet", Map("x1" -> 0.5))))), VarType("fuzzySet"))
      )
    )

    val derivedClass = Class(
      "Derived",
      superclass = Some(baseClass),
      methods = Map(
        "m1" -> Method("m1", List(Parameter("p1", VarType("fuzzySet"))), List(Assign(new UnionGate(), Left(FuzzySet("DerivedSet", Map("x1" -> 0.7))))), VarType("fuzzySet"))
      )
    )

    val derivedInstance = ClassInstance(derivedClass)
    val scope = new Scope()
    val methodInvoker = new MethodInvocation(derivedInstance, scope)

    // Fix: Pass a FuzzySet instead of Value
    val result1 = methodInvoker.invokeMethod("m1", Map("p1" -> Left(FuzzySet("ParamSet", Map("x1" -> 5.0)))))
    assert(result1 == Left(FuzzySet("DerivedSet Union DerivedSet", Map("x1" -> 0.7)))) // Check derived class result

    val result2 = methodInvoker.invokeMethod("m2", Map())
    assert(result2 == Left(FuzzySet("BaseSet Union BaseSet", Map("x1" -> 0.5)))) // Check superclass result
  }

  it should "apply alpha cut to method results" in {
    // Define a class method that returns a fuzzy set and apply alpha cut on its result
    val baseClass = Class("Base", variables = Map("v1" -> VarType("int")))
    val derivedClass = Class(
      "Derived",
      superclass = Some(baseClass),
      methods = Map(
        "m1" -> Method(
          "m1",
          List(Parameter("p1", VarType("int"))),
          List(Assign(new UnionGate(), Left(FuzzySet("C", Map("x1" -> 0.8))))), // Wrap FuzzySet in Left
          VarType("int")
        )
      )
    )

    val globalScope = new Scope()
    val derivedInstance = ClassInstance(derivedClass)
    val methodInvoker = new MethodInvocation(derivedInstance, globalScope)

    // Wrap method parameter in Right
    val result = methodInvoker.invokeMethod("m1", Map("p1" -> Right("5"))) // Use Right for the parameter

    // Handle the Either result to access alphaCut
    val alphaCutResult = result match {
      case Left(fuzzySet) => fuzzySet.alphaCut(0.7).elements
      case Right(errorMessage) => fail(s"Method evaluation failed with error: $errorMessage")
    }

    // Verify that alpha cut retains only values above 0.7
    alphaCutResult shouldBe Map("x1" -> 0.8)
  }

  // ==== Homework 3 Tests: Partial Evaluation for Fuzzy Gates ====

  it should "partially evaluate Union gate correctly" in {
    val gate = new UnionGate()
    val assign = Assign(gate, Left(setA))

    val result = assign.eval()
    result shouldBe Left(FuzzySet("A Union A", Map("x1" -> 0.3, "x2" -> 0.9)))
  }

  it should "partially evaluate Intersection gate correctly" in {
    val gate = new IntersectionGate()
    val assign = Assign(gate, Left(setA))

    val result = assign.eval()
    result shouldBe Left(FuzzySet("A Intersection A", Map("x1" -> 0.3, "x2" -> 0.9)))
  }

  it should "partially evaluate Complement gate correctly" in {
    val gate = new ComplementGate()
    val assign = Assign(gate, Left(setA))

    val result = assign.eval()
    result shouldBe Left(FuzzySet("Complement of A", Map("x1" -> 0.7, "x2" -> 0.1)))
  }

  it should "partially evaluate Addition gate correctly" in {
    val gate = new AdditionGate()
    val assign = Assign(gate, Left(setA))

    val result = assign.eval()
    result shouldBe Left(FuzzySet("A Addition A", Map("x1" -> 0.6, "x2" -> 1.0)))
  }

  it should "partially evaluate Multiplication gate correctly" in {
    val gate = new MultiplicationGate()
    val assign = Assign(gate, Left(setA))

    val result = assign.eval()
    result shouldBe Left(FuzzySet("A Multiplication A", Map("x1" -> 0.09, "x2" -> 0.81)))
  }

  it should "partially evaluate XOR gate correctly" in {
    val gate = new XORGate()
    val assign = Assign(gate, Left(setA))

    val result = assign.eval()
    result shouldBe Left(FuzzySet("A XOR A", Map("x1" -> 0.0, "x2" -> 0.0)))
  }
  
  // ==== Homework 3 Tests: Partial Evaluation ====
  it should "partially evaluate expressions" in {
    val expression = Multiply(Value(3), Multiply(Add(Value(5), Value(1)), Variable("var")))
    val simplified1 = PartialEvaluation.simplifyExpression(expression)
    val simplified2 = PartialEvaluation.simplifyExpression(simplified1)

    simplified1 shouldBe Multiply(Value(3), Multiply(Value(6), Variable("var")))
    simplified2 shouldBe Multiply(Value(18), Variable("var"))
  }

  it should "partially evaluate complex expressions" in {
    val expression = Multiply(Multiply(Value(5), Value(10)), Add(Value(10), Multiply(Variable("unknown"), Value(2))))
    val simplified1 = PartialEvaluation.simplifyExpression(expression)
    val simplified2 = PartialEvaluation.simplifyExpression(simplified1)

    simplified1 shouldBe Multiply(Value(50), Add(Value(10), Multiply(Variable("unknown"), Value(2))))
    simplified2 shouldBe Multiply(Value(50), Add(Value(10), Multiply(Variable("unknown"), Value(2))))
  }

  // ==== Homework 3 Tests: Partial Evaluation for Fuzzy Gates ====
  it should "fully evaluate expressions when all variables are defined in scope" in {
    // Create a scope (environment) and assign values
    val scope = new Scope()
      .assign("var", Left(FuzzySet("var", Map("x1" -> 0.5))))
      .assign("var1", Left(FuzzySet("var1", Map("x2" -> 0.7))))

    // Define a nested expression
    val expr = Multiply(Value(3), Multiply(Add(Value(5), Value(1)), Variable("var")))

    // Simplify or partially evaluate the expression
    val partiallyEvaluatedExpr = PartialEvaluation.simplifyExpression(expr)

    // Fully evaluate the expression using the scope
    val fullyEvaluatedResult = partiallyEvaluatedExpr match {
      case Multiply(Value(a), Multiply(Value(b), Variable(name))) =>
        scope.lookup(name) match {
          case Some(Left(fuzzySet: FuzzySet)) =>
            val variableValue = fuzzySet.elements.getOrElse("x1", 1.0) // Use "x1" for computation
            Value((a * b * variableValue).toInt)
          case _ => throw new Exception(s"Variable '$name' not found or partially defined")
        }
      case other => other
    }

    // Print and validate the result
    println(s"Fully evaluated result: $fullyEvaluatedResult")
    assert(fullyEvaluatedResult == Value(9)) 
  }

  it should "resolve and evaluate fuzzy gates when fully defined" in {
    val gate = new ComplementGate()
    val assign = Assign(gate, Left(setA))

    val result = assign.eval()
    result shouldBe Left(setA.eval(FuzzyOperation.Complement))
  }

  // ==== Homework 3 Tests: Conditional Constructs ====
  it should "evaluate conditional constructs with THEN and ELSE branches" in {
    val condition = Conditional(
      (a, b) => a.membership("x1") > b.membership("x1"), // Update the condition
      () => println("THEN block executed"),
      () => println("ELSE block executed")
    )

    condition.executeCondition(setA, setB)
    condition.partialEval(setA, setB) shouldBe Left("ELSE branch executed successfully")
  }

  it should "partially evaluate conditional constructs" in {
    val condition = Conditional(
      (a, b) => a.membership("x2") + b.membership("x2") > 1.5, // Adjust condition to trigger ELSE
      () => println("THEN block executed"),
      () => println("ELSE block executed")
    )

    condition.partialEval(setA, setB) shouldBe Left("THEN branch executed successfully")
  }

  // ==== Homework 3 Advanced Features ====
  it should "test partial evaluation for gates with unresolved inputs" in {
    val gate = new IntersectionGate()
    val assign = Assign(gate, Right("Unresolved fuzzy set"))

    val result = assign.eval()
    result shouldBe Right("Partial evaluation of gate 'Intersection' with expression: Unresolved fuzzy set")
  }

  it should "handle advanced partial evaluation with simplified nested expressions" in {
    // Simplified version of the original nested expression
    val expression = Multiply(Value(3), Multiply(Value(4), Variable("unknown")))
    val simplified1 = PartialEvaluation.simplifyExpression(expression)
    val simplified2 = PartialEvaluation.simplifyExpression(simplified1)

    simplified1 shouldBe Multiply(Value(12), Variable("unknown"))
    simplified2 shouldBe Multiply(Value(12), Variable("unknown"))
  }
  
  it should "test fuzzy gate evaluation within a scope" in {
    val scope = new Scope().assign("x1", Left(FuzzySet("A", Map("x1" -> 0.3))))
    val gate = new ComplementGate()
    val assign = Assign(gate, Left(setA))

    val testResult = TestGate(assign, "x1", 0.7, scope)
    testResult shouldBe true
  }

  it should "partially evaluate conditional expressions when variables are undefined" in {
    // Define a scope (environment) with one variable defined
    val scope = new Scope().assign("var1", Left(FuzzySet("var1", Map("x2" -> 2.0))))

    // Define the conditional construct
    val condition = (a: FuzzySet, b: FuzzySet) => a.membership("x1") >= b.membership("x2")
    val thenBlock = () => println("THEN block executed")
    val elseBlock = () => println("ELSE block executed")

    val conditional = Conditional(
      condition,
      thenBlock,
      elseBlock
    )

    // Partially evaluate the condition
    val partialResult = conditional.partialEval(
      FuzzySet("ConditionSetA", Map("x1" -> 15.0)),
      FuzzySet("ConditionSetB", Map("x2" -> 2.0))
    )

    // Expected partially evaluated result
    val expected = Left("THEN branch executed successfully")

    println(s"Conditional expression partial evaluation result: $partialResult")
    assert(partialResult == expected)
  }
}