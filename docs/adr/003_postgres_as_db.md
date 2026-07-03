# 003 - PostgreSQL as main database

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
Our current persistence is maintained by a couple of JSON files. This is not scalable and makes the project vulnerable to sudden interruption of code execution, as the apps context is held in RAM. 

## Considered Options
<!-- What alternatives did we evaluate? -->
Option - PostgreSQL with Spring JPA/Hibernate

## Decision
We decided to use **PostgreSQL** because it is free and integrates nicely with Spring JPA/Hibernate. It runs within a Docker container, which aids with portability and local testing.

<!-- optional -->
## Consequences
<!-- Every decision has trade-offs. What does this mean for the future? -->
Changes to Entities during development may break other developers DB or the prod DB. Therefore, Flyway migration has to be implemented at some point. 
