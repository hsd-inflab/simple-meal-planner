# 002 - Spring Boot as backend framework

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
Using dependency Injection necessitates the initialization of the injected objects. this can be rather difficult to manage. REST API support also needs a good and easy to use framework. 

## Decision
We decided to use **Spring Boot** because it offers easy configuration for DI and general implementation of a web app using REST.

<!-- optional -->
## Consequences
<!-- Every decision has trade-offs. What does this mean for the future? -->
DI initialization now happens automatically. Architecture has to be more specific and needs to be thoroughly followed to maintain a readable codebase.
