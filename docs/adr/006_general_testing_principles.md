# 006 -  General Testing Principles

<!-- 
Keep it short! There is no need to fill every optional field for each ADR. 
Don't contrive, if a field does not feel useful to fill, leave it empty or delete it entirely.
-->

<!-- 
Title format: Number is sequential (0001, 0002, ...). 
Title should be clear, e.g., "0005 - Use Redis for Caching" 
-->

<!-- optional -->
* **Status:** Accepted
* **Date:** retrospective

## Context and Problem Statement
<!-- 
What is the problem we are trying to solve? 
Why do we need to make a decision?
-->
AI generated tests vary greatly in both quality and coverage. Nobody likes to write tests, therefore we have to lay some groundrules to ease automating their generation.

## Decision
We decided to use **jUnit 5 with Mockito** because it is the most supported testing infrastructure. 

### For Unit Tests
Before writing the tests, consult the method and identify equivalence classes and edge cases for each input parameter (White Box Testing).

Implement the unit test with MethodSource as input parameter and stream the edge cases and equivalence classes (one test case for each) if there are more than 3 test cases. Don't use CSV source.

If the tested method returns a boolean (or the method structure implies a simple good/bad logic), split the test method into valid/invalid test methods.

Use Mockito.

