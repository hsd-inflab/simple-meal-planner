#!/usr/bin/env bash
# Detects ADR numbering collisions across branches.
#
# Fails if this branch ADDS an ADR whose number is already used, under a
# DIFFERENT filename, on another branch (including the base branch). Same
# number + same filename = the same ADR living on multiple refs (normal after a
# merge) and is intentionally NOT flagged.
#
# Usage: check-adr-duplicates.sh [BASE_REF]   (default: origin/master)
set -euo pipefail

ADR_DIR="docs/adr"
BASE_REF="${1:-origin/master}"

number_of() { basename "$1" | grep -oE '^[0-9]+'; }

# 1. In-branch sanity check: two files sharing a number on THIS branch.
in_branch_dupes=$(ls "$ADR_DIR" 2>/dev/null | grep -oE '^[0-9]+' | sort | uniq -d || true)
if [[ -n "$in_branch_dupes" ]]; then
  echo "::error::Duplicate ADR numbers within this branch: $in_branch_dupes"
  exit 1
fi

# 2. Which ADRs did THIS branch add relative to the base (since divergence)?
added=$(git diff --diff-filter=A --name-only "${BASE_REF}...HEAD" -- "$ADR_DIR" || true)
if [[ -z "$added" ]]; then
  echo "No newly added ADRs on this branch. Nothing to check."
  exit 0
fi

# 3. For each added ADR, look for the same number under a different name elsewhere.
status=0
while IFS= read -r file; do
  [[ -z "$file" ]] && continue
  num=$(number_of "$file")
  ours=$(basename "$file")
  echo "Checking new ADR $num ($ours)..."
  for ref in $(git for-each-ref --format='%(refname:short)' refs/remotes/origin/ | grep -v '/HEAD$'); do
    while IFS= read -r match; do
      [[ -z "$match" ]] && continue
      theirs=$(basename "$match")
      if [[ "$theirs" != "$ours" ]]; then
        echo "::error::ADR number $num already used on '$ref' as '$theirs' (this branch adds '$ours')."
        status=1
      fi
    done < <(git ls-tree -r --name-only "$ref" -- "$ADR_DIR" | grep -E "/${num}_" || true)
  done
done <<< "$added"

[[ $status -eq 0 ]] && echo "No ADR number collisions found."
exit $status
