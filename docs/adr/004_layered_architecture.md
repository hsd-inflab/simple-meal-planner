# 004 - Layered Architecture

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
We need a specified and enforced architecture to maintain a solid codebase and avoid spaghetti code.

## Considered Options
<!-- What alternatives did we evaluate? -->
1. Option - Layered
2. Option - Onion
3. Option - Hexagonal

## Decision
We decided to use **Layered Architecture** because it offers the correct amount of abstraction layers and decoupling for our use case. Hexagonal/Onion would have been overkill. 

As an automatic architecture testing tool, we will implement archUnit.   

<!-- optional -->
## Consequences
<!-- Every decision has trade-offs. What does this mean for the future? -->
Exchangeability of layers will probably decrease with this decision, but since we dont intend to change much of the codebase after the fact, this is ok. Readability and general understandability of the codebase will drastically improve, as will code quality due to guardrails such as archUnit.
