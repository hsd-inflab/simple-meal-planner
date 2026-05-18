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
AI generated tests vary greatly in both quality and coverage. We have to lay some ground rules to ease automating their generation.

## Decision
We decided to use **jUnit 5 with Mockito** because it is the most supported testing infrastructure. 

### For Unit Tests
Before writing the tests, consult the method and identify equivalence classes and edge cases for each input parameter.

Implement the unit test with MethodSource as input parameter and stream the edge cases and equivalence classes (one test case for each) if there are multiple test cases. Don't use CSV source.

If the tested method returns a boolean (or the method structure implies a simple good/bad logic), split the test method into valid/invalid test methods.

Unit tests should focus on isolated business logic and avoid external systems such as databases, network calls, or filesystem access. 
Use Mockito to isolate the unit under test from external dependencies.

