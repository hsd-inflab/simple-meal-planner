# 008 - Introduce Mapping Layer

<!-- 
Keep it short! There is no need to fill every optional field for each ADR. 
Don't contrive, if a field does not feel useful to fill, leave it empty or delete it entirely.
-->

<!-- 
Title format: Number is sequential (0001, 0002, ...). 
Title should be clear, e.g., "0005 - Use Redis for Caching" 
-->

<!-- optional -->
* **Status:** Proposed | mildly connected to ADR-007
* **Date:** 12.05.2026

## Context and Problem Statement
<!-- 
What is the problem we are trying to solve? 
Why do we need to make a decision?
-->
DTOs and entities need to be converted into one another, this currently happens within the corresponding service. This is a deliberate design decision. But mapping is not a core responsibility of a service, which also violates SRP. A growing number of DTOs increased the amount of mapping code in services to the point where they are more mapping than performing business logic.
## Considered Options
<!-- What alternatives did we evaluate? -->
1. Option - Implement a Mapping Layer with MapStruct
2. Option - Implement Mapping without MapStruct
3. Option - keep as is

## Decision
I propose we use **Option 1** because it drastically reduces boilerplate. Apart from the mapper instance, there is only an annotation needed - given that both dto and entity match in field name, count and type. 

<!-- optional -->
## Consequences
<!-- Every decision has trade-offs. What does this mean for the future? -->

