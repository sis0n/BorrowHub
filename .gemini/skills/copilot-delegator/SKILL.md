---
name: copilot-delegator
description: Automates issue triaging and task delegation to the Copilot Coding Agent based on the parent/sub-issue workflow.
---

# Copilot Delegation Workflow

This skill automates the triaging and PR-creation process for issues in the BorrowHub repository.

## Workflow

### 1. Identify Issue Type
When asked to delegate an issue:
1. Load these skill instructions.
2. Use the `mcp_github_issue_read` tool with method `get_sub_issues` to check if the issue is a PARENT or a SUB-ISSUE.
3. If it has sub-issues, it is a **PARENT**.
4. If it has no sub-issues, check if it belongs to a parent issue (e.g., referenced in the body or via GitHub sub-issues API).
5. If it belongs to a parent, it is a **SUB-ISSUE**.
6. If it has neither sub-issues nor a parent, it is a **SINGLE ISSUE**.

### 2. Execution Logic

#### If PARENT Issue:
- **Do NOT write code.**
- Tell Copilot Coding Agent to follow `CONTRIBUTING.md` patterns: `feature/<issue-number>-parent-master`.
- Base this branch on `master`.
- PR Body MUST follow the `.github/PULL_REQUEST_TEMPLATE.md` and:
    - Describe the feature overview.
    - List the expected sub-PRs.
    - Reference: `Part of #<issue-number>`.
- Use `github_AssignCodingAgent` to assign the coding agent to the issue, including these instructions for the Master PR coordinator role.

#### If SUB-ISSUE:
- Implement the code.
- Tell Copilot Coding Agent to follow `CONTRIBUTING.md` patterns: `feature/<issue-number>-<short-desc>`.
- Target the PR base to the current Parent PR branch (NOT `master`).
- PR Body MUST follow the `.github/PULL_REQUEST_TEMPLATE.md` and:
    - Include standard PR template content.
    - Reference: `Closes #<sub-issue-number>`, `Part of #<parent-issue-number>`.
- Use `github_AssignCodingAgent` for implementation delegation.

#### If SINGLE Issue:
- Implement the code.
- Tell Copilot Coding Agent to follow `CONTRIBUTING.md` patterns: `feature/<issue-number>-<short-desc>`.
- Base this branch on `master`.
- PR Body MUST follow the `.github/PULL_REQUEST_TEMPLATE.md` and:
    - Reference: `Closes #<issue-number>`.
- Use `github_AssignCodingAgent` for implementation delegation.

## Reusable Resources
- [CONTRIBUTING.md](../../../docs/CONTRIBUTING.md) for branching and PR standards.
- [PULL_REQUEST_TEMPLATE.md](../../../.github/PULL_REQUEST_TEMPLATE.md) for PR body formatting.
