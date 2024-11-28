package wuzzyfuzz

// Defines an object-oriented class with a name, optional superclass, variables, methods, and inner classes.
case class Class(
                  name: String,                              // Class name
                  superclass: Option[Class] = None,         // Optional superclass for inheritance
                  variables: Map[String, VarType] = Map(),  // Variables in the class with their types
                  methods: Map[String, Method] = Map(),      // Methods defined in the class
                  nestedClasses: Map[String, Class] = Map()  // Nested classes defined within this class
                ){

  // Getter for superclass to avoid direct access to private fields
  def getSuperclass: Option[Class] = superclass
}

// Represents a variable within a class and its type.
case class ClassVar(
                     name: String,        // Variable name
                     varType: VarType     // Type of the variable (e.g., int, string, fuzzy set)
                   )

// Defines the type of the variable (e.g., int, string, fuzzy set).
case class VarType(
                    name: String         // Type name
                  )

// Represents a method in a class, including its name, parameters, body, and return type.
case class Method(
                   name: String,                    // Method name
                   parameters: List[Parameter],     // Method parameters
                   body: List[Assign],              // Method body as a list of assignments
                   returnType: VarType              // Return type of the method
                 )

// Defines a parameter for a method with a name and type.
case class Parameter(
                      name: String,        // Parameter name
                      paramType: VarType   // Parameter type
                    )