package wuzzyfuzz

// Defines the possible operations that can be applied in fuzzy logic.
enum FuzzyOperation {
  case Union           // It combines two fuzzy sets, taking the maximum value for each element.
  case Intersection    // It finds commonality between sets, taking the minimum value for each element.
  case Complement      // This inverts a fuzzy set by subtracting each element's value from 1.
  case Addition        // Addition of membership values from the two sets, capping at 1.
  case Multiplication  // Multiplies the membership values of elements in two sets.
  case XOR             // Calculates the absolute difference between membership values.
}