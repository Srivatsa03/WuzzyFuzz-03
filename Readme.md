# CS476 - Srivatsa_Kamballa
### <u>_NET ID: 664840432_</u>
### <u>_Email: skamb10@uic.edu_</u>
## Programming Language Design - Homework - 03

# WuzzyFuzz Homework 3

## Overview

**WuzzyFuzz** is a domain-specific language (DSL) designed for fuzzy logic operations and their extensions. This implementation builds on previous homework assignments and introduces the following features:

- **Conditional Constructs**: Enabling decision-making capabilities within fuzzy logic.
- **Partial Evaluation**: Allowing for optimization and pre-computation of expressions.
- **Dynamic Evaluation**: Facilitating runtime evaluation of fuzzy logic expressions.

The project provides a clear pipeline for:
1. **Creating** fuzzy logic expressions.
2. **Evaluating** fuzzy logic expressions with well-defined semantics.
3. **Partially Evaluating** expressions for improved performance.

This implementation extends the versatility of WuzzyFuzz by ensuring efficient and intuitive handling of complex fuzzy logic scenarios.
# Project Structure

## File Descriptions

### 1. `FuzzySet.scala`
- **Purpose**: Defines the core data structure for fuzzy sets. A `FuzzySet` maps elements (e.g., `x1`, `x2`) to their membership values.
- **Features**:
    - **Common Fuzzy Operations**:
        - **Union**: Combines two fuzzy sets, selecting the maximum membership for each element.
        - **Intersection**: Combines two fuzzy sets, selecting the minimum membership for each element.
        - **Complement**: Inverts membership values (`1 - membership`).
        - **Arithmetic Operations**: Supports addition, multiplication, and XOR operations on fuzzy sets.
    - **Alpha Cut**: Filters elements with membership values below a given threshold.

---

### 2. `FuzzyGate.scala`
- **Purpose**: Represents logical gates (e.g., `UnionGate`, `ComplementGate`) for fuzzy set operations.
- **Features**:
    - Supports **partial evaluation**, enabling gates to process partially defined fuzzy sets or expressions.

---

### 3. `Assign.scala`
- **Purpose**: Links a fuzzy gate to a fuzzy set or partial expression for evaluation.
- **Features**:
    - Supports **dynamic evaluation** for both full and partial evaluations.
    - **Sample Usage**:
      ```scala
      val assign = Assign(new UnionGate(), Left(FuzzySet("A", Map("x1" -> 0.3))))
      val result = assign.eval() // Fully evaluates the UnionGate on FuzzySet A
      ```

---

### 4. `PartialEvaluation.scala`
- **Purpose**: Simplifies mathematical expressions through optimization techniques.
- **Features**:
    - **Simplifications**:
        - Constant folding: Simplifies operations like `3 * (5 + 1)` to `18`.
        - Operator associativity.
    - Retains unresolved variables for later evaluation.

---

### 5. `Conditional.scala`
- **Purpose**: Implements conditional constructs similar to `if-else` in traditional programming languages.
- **Features**:
    - **Partial Evaluation**: Evaluates both branches (THEN and ELSE) partially if conditions are unresolved.
    - **Full Resolution**: Fully evaluates branches when all inputs are defined.

---

### 6. `Scope.scala`
- **Purpose**: Manages variable assignments and creates a dynamic evaluation environment.
- **Features**:
    - Supports **nested scopes** to simulate block-level variable visibility.

---

### 7. `WuzzyFuzzTests.scala`
- **Purpose**: Contains unit tests for validating:
    - Fuzzy set operations.
    - Conditional constructs.
    - Partial and dynamic evaluation.

---

### 8. `WuzzyFuzz.scala`
- **Purpose**: The main entry point for the program.
- **Features**:
    - Demonstrates:
        - Basic fuzzy set operations.
        - Partial evaluation of fuzzy gates.
        - Conditional constructs and their evaluation.
# Conditional Constructs

## Design Explanation

The conditional construct in **WuzzyFuzz** is inspired by traditional `if-else` logic but is adapted to fuzzy logic semantics. It evaluates conditions based on **membership values** and supports **partial evaluation** for unresolved inputs, making it versatile for dynamic and fuzzy scenarios.

### Key Features
- **Fuzzy Semantics**:
    - Evaluates conditions based on the membership values of fuzzy sets.
- **Partial Evaluation**:
    - Allows evaluation of both branches if conditions have unresolved inputs.
- **Dynamic Execution**:
    - Fully resolves the appropriate branch when conditions are entirely known.

### Implementation
- **Condition Definition**:
    - A condition is expressed as a function: `(FuzzySet, FuzzySet) => Boolean`.
- **Branch Representation**:
    - Both THEN and ELSE branches are implemented as lambda functions.
- **Partial Evaluation**:
    - When inputs are unresolved, both branches are evaluated partially to prepare them for future resolution.

---

### Example: Conditional Evaluation

```scala
val condition = Conditional(
  (a, b) => a.membership("x1") > b.membership("x1"),
  () => println("THEN block executed"),
  () => println("ELSE block executed")
)
```
#### 1. Full Evaluation
- **Behavior**:
  - When `a.membership("x1")` and `b.membership("x1")` are both known, the conditional construct evaluates the given condition and executes the appropriate branch.
- **Example**:
  ```scala
  // Define fuzzy sets with known membership values
  val a = FuzzySet("A", Map("x1" -> 0.6))
  val b = FuzzySet("B", Map("x1" -> 0.3))

  val condition = Conditional(
    (a, b) => a.membership("x1") > b.membership("x1"),
    () => println("THEN block executed"),
    () => println("ELSE block executed")
  )

  condition.eval(a, b)
  
  // Output:
  // THEN block executed
  ```

#### 2. Partial Evaluation
- **Behavior**:
    - When either `a` or `b` has unresolved membership values, the conditional construct partially evaluates both THEN and ELSE branches to prepare them for full execution when the inputs are fully defined.

- **Example**:
  ```scala
  // Define fuzzy sets with partial membership values
  val a = FuzzySet("A", Map("x1" -> 0.6)) // "x1" is resolved, but "x2" is not defined
  val b = FuzzySet("B", Map())            // Membership values unresolved for all elements

  val condition = Conditional(
    (a, b) => a.membership.get("x1").getOrElse(0.0) > b.membership.get("x1").getOrElse(0.0),
    () => println("THEN block executed"),
    () => println("ELSE block executed")
  )

  val partialResult = condition.partialEval(a, b)
  println(partialResult)
  // Output:
  // THEN branch executed successfully
  ```
  ### Partial Evaluation

#### Design Explanation
- **Purpose**:
    - Partial evaluation simplifies expressions to their simplest form when some variables remain undefined.
- **Example**:
  ```scala
  val expr = Multiply(
    Value(3),
    Multiply(
      Add(Value(5), Value(1)),
      Variable("var")
    )
  )
  val simplified = PartialEvaluation.simplifyExpression(expr)
  println(simplified)
  // Output:
  // Multiply(Value(18), Variable("var"))```


### Equivalence with Full Evaluation

- **Behavior**:
  - When all variables are defined in the environment, partially evaluated expressions fully resolve.

- **Example**:
  ```scala
  val scope = new Scope().assign(
    "var", 
    Left(FuzzySet("var", Map("x1" -> 0.5)))
  )
  val result = expr.evaluate(scope)
  println(result)
  // Fully evaluates to Value(9.0)

# Types, Semantics, and Evaluation

## Types
- **FuzzySet**: Represents a collection of elements and their membership values.
- **FuzzyGate**: Models logical gates (e.g., Union, Complement, etc.) for operations on fuzzy sets.
- **Assign**: Binds gates to sets or partial expressions for dynamic evaluation.
- **Conditional**: Represents conditional expressions based on fuzzy logic.

---

## Static Semantics
- Variables **must be defined** before they are referenced.
- Gates **must operate** on valid `FuzzySet` inputs or compatible expressions.
- **Type Mismatch**:
    - Applying a fuzzy gate to a non-fuzzy input is statically invalid.

---

## Dynamic Semantics
- **Full Evaluation**:
    - Computes exact results for expressions or gates when all inputs are fully defined.
- **Partial Evaluation**:
    - Simplifies expressions by:
        - Folding constants.
        - Retaining unresolved variables.
    - Outputs descriptive strings for unresolved gates or expressions.
## Build Instructions

### 1. Clone the repository:
```
git clone https://github.com/Srivatsa03/WuzzyFuzz-03.git
cd WuzzyFuzz-02
```
### 2. Compile the Project:
```
sbt compile
```
### 3. Run the testss:
```
sbt test
```
### 4. Run the WuzzyFuzz Program Code:
```
sbt run
```
### Or just use the below comands directly to run from the command line
```
//Select or Create a Repository Where do you want to deploy this WuzzyFuzz Repository 
// Next, Go to Command Line, type out 
git clone  https://github.com/Srivatsa03/WuzzyFuzz-02.git
cd WuzzyFuzz-02
sbt clean compile run
sbt clean compile test 
```

# Code Samples and Usage

## Creating a Fuzzy Set
You can create fuzzy sets with elements mapped to their membership values:

```scala
val setA = FuzzySet("A", Map("x1" -> 0.3, "x2" -> 0.9))
val setB = FuzzySet("B", Map("x1" -> 0.6, "x2" -> 0.7))

## Applying Gates
Use gates like `UnionGate`, `IntersectionGate`, and others to operate on fuzzy sets:
```
```scala
val unionGate = new UnionGate()
val assign = Assign(unionGate, Left(setA))
val result = assign.eval()
println(result)
// Outputs: FuzzySet(A Union A, {x1 -> 0.3, x2 -> 0.9})
```

## Conditional Logic
Implement conditional constructs for fuzzy operations:

```scala
val condition = Conditional(
  (a, b) => a.membership("x1") > b.membership("x1"),
  () => println("THEN block executed"),
  () => println("ELSE block executed")
)
condition.executeCondition(setA, setB)
```

## Partial Evaluation
Simplify expressions to evaluate only parts that are resolvable, while retaining unresolved variables for future evaluation:

### Step 1: Partial Evaluation
Simplify constants but preserve unresolved variables:
```scala
val expr = Multiply(Value(3), Multiply(Add(Value(5), Value(1)), Variable("var")))
val partiallyEvaluatedExpr = PartialEvaluation.simplifyExpression(expr)
println(partiallyEvaluatedExpr)
// Outputs: Multiply(Value(3), Multiply(Value(6), Variable("var")))
```
### Step 2: Full Evaluation
Fully evaluate expressions if all variables are defined in the scope:
```scala
val scope = new Scope().assign("var", Left(FuzzySet("var", Map("x1" -> 0.5))))
val fullyEvaluatedResult = partiallyEvaluatedExpr match {
  case Multiply(Value(a), Multiply(Value(b), Variable(name))) =>
    scope.lookup(name) match {
      case Some(Left(fuzzySet)) =>
        val variableValue = fuzzySet.elements.getOrElse("x1", 1.0) // Assume "x1" is the key
        Value(a * b * variableValue)
      case _ => throw new Exception(s"Variable '$name' not found")
    }
  case other => other
}
println(fullyEvaluatedResult)
// Outputs: Value(9.0)
```
### How the DSL Handles Partial Evaluation:

1. **Simplifies Expressions**:
    - Simplifies as much as possible while preserving unresolved variables.

2. **Full Evaluation**:
    - When a complete environment is provided, fully evaluates the expression.

## Future Extensions

- **Support for More Complex Fuzzy Gates**:
    - Expand the DSL to include additional fuzzy logic gates for advanced operations.

- **Advanced Optimizations for Partial Evaluation**:
    - Enhance the partial evaluation process with more sophisticated optimization techniques.

- **Integration with External Libraries for Fuzzy Logic Visualization**:
    - Provide tools for visualizing fuzzy sets, gates, and operations using external visualization libraries.
## Conclusion

This homework demonstrates the creation and implementation of a custom Domain-Specific Language (DSL) for fuzzy logic, supporting fuzzy set operations, conditional constructs, and partial evaluation. The project incorporates advanced features such as:

- **Scoped Variable Resolution**: Ensuring variables are properly managed and accessible within their defined scopes.
- **Dynamic Method Invocation**: Facilitating runtime evaluation of fuzzy logic constructs.
- **Systematic Partial Evaluation**: Handling incomplete inputs with a structured approach.

The DSL’s design prioritizes flexibility, modularity, and clarity. Comprehensive testing and detailed documentation have been provided to showcase its functionality and extendability. This implementation meets all specified requirements and serves as a robust framework for fuzzy logic computation.