# 001 - Flutter Frontend

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
Our current JavaFX frontend has a really strong coupling to Java itself as a language and does not allow for a native web app or a modern UI approach.
## Considered Options
<!-- What alternatives did we evaluate? -->
1. Option - Flutter
2. Option - React


## Decision
We decided to use **Flutter** because it allows the frontend to run natively on a variety of OSes and web browsers. It is all written in Dart and only the executable has to be switched out for each destination platform.

<!-- optional -->
## Consequences
<!-- Every decision has trade-offs. What does this mean for the future? -->
Using Flutter requires a REST API from our Java backend for communication. It also makes it more difficult to version frontend/backend because they live in different repositories.
