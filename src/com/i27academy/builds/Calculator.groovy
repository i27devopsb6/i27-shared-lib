// methods
// Define the package 
package com.i27academy.builds;

// Define a Calculator class 

class Calculator {
    // Define a variable to hold the jenkins context 
    def jenkins

    // Constructor tom intiliase the Calculator class with the jenkins instance 
    Calculator(jenkins) {
        this.jenkins = jenkins
    }


    // method to perform addition of 2 numbers
    def add(firstNumber, secondNumber) {
        // logical code for addition
        return firstNumber + secondNumber
    }
    // Example Usage: add(2,3)

    // Method to perform multiplication of 2 numbers 
    def multiply(firstNumber, secondNumber) {
        return firstNumber*secondNumber
    }
}

