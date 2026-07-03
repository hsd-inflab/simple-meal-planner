# 005 - Lombok and Records

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
Boilerplate code drastically expands with rising abstraction. This can lead to coding fatigue and reduce codebase readability as well as slow development effort.

## Decision
We decided to use **Lombok** and **Records for DTOs** because they offer great value for little code written. Records also enable immutability for DTOs which is appreciated. 

Use Lombok where the number of annotations needed would be smaller than the lines of code for getter/setter and constructors or where builder pattern / logging is needed. 

<!-- optional -->
## Consequences
<!-- Every decision has trade-offs. What does this mean for the future? -->
boilerplate code will reduce, but the additional tool will make the codebase less accessible for beginners. 

auto generated getter/setter will no longer reduce test coverage.

