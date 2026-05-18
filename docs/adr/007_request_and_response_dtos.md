# 007 - Implement Request & Response DTOs

<!-- 
Keep it short! There is no need to fill every optional field for each ADR. 
Don't contrive, if a field does not feel useful to fill, leave it empty or delete it entirely.
-->

<!-- 
Title format: Number is sequential (0001, 0002, ...). 
Title should be clear, e.g., "0005 - Use Redis for Caching" 
-->

<!-- optional -->
* **Status:** Proposed
* **Date:** 12.05.2026

## Context and Problem Statement
<!-- 
What is the problem we are trying to solve? 
Why do we need to make a decision?
-->
POST and GET commands are currently utilizing the same DTOs in the controller. this is an issue, because although the backend filters out a possible id set by the frontend, this happens implicitly and might result in inconsistencies. 
## Considered Options
<!-- What alternatives did we evaluate? -->
1. Option - implement RequestDTOs and ResponseDTOs
2. Option - throw an exception when id is not null
3. Option - do nothing and hope for the best

## Decision
I suggest we implement **Option 1** because this offers the clearest solution to our problem. there are no more misplaced or orphaned fields in the corresponding DTOs.

<!-- optional -->
## Consequences
<!-- Every decision has trade-offs. What does this mean for the future? -->
more abstraction, more boilerplate. decreases understandability of the codebase for new maintainers.

increases codebase stability and logic coherence.