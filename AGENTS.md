# AGENTS.md

## Purpose

This file defines how coding agents work in this repository.

Priorities:

1. Correctness
2. Clear communication
3. Minimal changes
4. Test-first development
5. Explicit approval before production code

---

## Language

Use English for all technical content:

* Source code and identifiers
* Comments and docstrings
* Tests and test names
* Error and log messages
* Commit messages
* Technical documentation

User-facing UI text may be localized when explicitly requested.

---

## Before Editing

Inspect relevant files, tests, and existing conventions before making changes.

Do not assume:

* Architecture or project structure
* API contracts
* Database behavior
* Framework behavior
* Existing test setup
* Intended edge cases

State important assumptions.

If multiple interpretations are reasonable, explain them briefly instead of silently choosing one.

Do not ask for clarification when the answer can be verified from the repository.

---

## Scope Rules

Keep changes limited to the approved task.

Do not:

* Refactor unrelated code
* Reformat unrelated files
* Rename unrelated identifiers
* Add speculative features
* Add unnecessary abstractions
* Add dependencies without a clear reason
* Change configuration without explaining why
* Remove existing code unless required

Mention unrelated issues if discovered, but do not fix them without approval.

Every changed line must be traceable to the requested task.

---

## Required Proposal

For every non-trivial task, explain the intended approach before creating tests or production code.

Use this structure:

```text
## Proposed approach

### Understanding
- [Requested outcome]

### Scope
- Change:
- Do not change:

### Plan
1. [Step] → verify: [check]
2. [Step] → verify: [check]

### Test plan
- [Test or scenario]
  - Verifies:
  - Expected result:

### Success criteria
- [Criterion]
- [Criterion]

Please approve this test plan before I create the tests.
```

Keep proposals short for simple tasks.

Do not write production code before explicit user approval.

---

## Test-First Workflow

After the user approves the test plan:

1. Write or update tests first.
2. Explain what the tests verify.
3. Run the relevant tests.
4. Confirm that tests fail for the expected reason when applicable.
5. Show the test result to the user.
6. Ask for approval before writing production code.
7. Tests should follow the AAA-principle (Act, Arrange, Assert)
Use this structure:

```text
## Tests prepared

### Test coverage
- [Test]&#58; verifies [behavior]

### Current result
- [Failing or passing result]
- [Reason]

### Intended production behavior
- [What the implementation will make true]

Please approve these tests before I implement production code.
```

Only write production code after the user explicitly approves the tests or explicitly tells you to proceed.

---

## Implementation Rules

After test approval:

* Implement the smallest solution that makes the approved tests pass.
* Follow existing project patterns.
* Avoid unrelated cleanup.
* Avoid premature abstractions.
* Avoid hidden side effects.
* Remove only imports or code made unused by your own changes.

Prefer direct and readable code over flexible but unnecessary designs.

---

## Security

Never expose, hardcode, or log:

* Passwords
* API keys
* Tokens
* Secrets
* Private certificates
* Personal data

Do not weaken authentication, authorization, validation, or security controls without explicit approval.

Warn before destructive actions, including:

* Database migrations
* Data deletion
* File deletion
* Configuration overwrites
* Force pushes
* Git resets
* Dependency removal

Do not perform destructive actions without explicit approval.

---

## Multi-Tenant Safety

Treat tenant isolation as a permanent invariant for `Recipe`, `PantryItem`, `DailyMeal`, and every future
tenant-owned resource. Authentication identifies the caller; every data-access path must additionally enforce
tenant authorization.

For every new or changed endpoint that handles tenant-owned data:

* Derive the tenant identity exclusively from the authenticated `Principal` or Spring Security context.
* Never accept an owner identity or `isGlobal` value from a regular client request.
* Controllers must pass the authenticated identity to the service and must not access repositories directly.
* Enforce tenant authorization at the service/repository seam with explicitly scoped methods such as
  `findVisibleById`, `findOwnedById`, `findByOwnerUsername`, or `deleteByIdAndOwnerUsername`.
* Do not use unscoped `findAll`, `findById`, or `deleteById` calls for tenant-owned data. An explicitly approved
  administrative or system path must be isolated, documented, role-protected, and integration-tested.
* Allow regular users to read only their own records and explicitly global records. Treat global records as
  read-only unless a separate requirement grants mutation rights.
* Set the owner and private/global state server-side when creating a record. A regular user-created record must
  belong to the authenticated user and must not be global.
* Validate every referenced tenant-owned entity through a visibility-scoped lookup before storing the reference.
* Scope updates, deletes, bulk operations, date ranges, availability queries, and natural-key lookups by tenant.
  Tenant-specific uniqueness constraints must include the owner identifier.
* Return `404 Not Found` for inaccessible foreign records unless an approved contract explicitly requires another
  status, so the endpoint does not disclose whether another tenant's record exists.

Add integration coverage with at least two authenticated users for every applicable access path:

* Missing or invalid JWT returns `401 Unauthorized`.
* The owner can perform the intended operation.
* Another user cannot list, read, reference, update, or delete the owner's private record.
* Explicitly global records are readable where supported but cannot be changed by a regular user.
* Creation assigns the authenticated owner regardless of client input.
* Different users can use the same tenant-scoped natural key where the domain permits it.

Do not consider a tenant-facing endpoint complete until these cases pass at the HTTP integration level.

---

## Verification

Before declaring a task complete:

* Run the smallest relevant test command first.
* Confirm approved tests pass.
* Confirm the implementation stays within scope.
* Confirm no unrelated behavior changed.
* Confirm no unnecessary dependencies were added.

Do not claim tests passed unless they were executed.

If tests cannot run, explain:

* Why they could not run
* What remains unverified
* Which command should be run later

---

## Completion Report

After implementation, provide:

```text
## Completed

### Changed
- [File]&#58; [change]

### Tests
- [Command or test]&#58; [result]

### Verification
- [Confirmed behavior]

### Notes
- [Limitations or follow-up items]
```

Do not claim completion or correctness without evidence.
