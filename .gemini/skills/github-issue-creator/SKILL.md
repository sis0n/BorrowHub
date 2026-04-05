---
name: github-issue-creator
description:
  Use this skill when asked to create a GitHub issue for the BorrowHub project. It handles different issue types (bug, feature, etc.) using the repository's Markdown templates and ensures proper labeling and component tracking.
---

# GitHub Issue Creator

This skill guides the creation of high-quality GitHub issues that adhere to the BorrowHub repository's standards and use the appropriate templates.

## Workflow

Follow these steps to create a GitHub issue:

1.  **Identify Issue Type**: Determine if the request is a bug report, feature request, chore, documentation update, or improvement based on the user's request.

2.  **Locate Template**: Search for the corresponding Markdown issue template in `.github/ISSUE_TEMPLATE/`.
    - `bug_report.md`
    - `feature_request.md`
    - `chore.md`
    - `documentation.md`
    - `improvement.md`

3.  **Read Template**: Read the content of the identified `.md` template file to understand the required fields and structure.

4.  **Draft Content**: Draft the issue title and body.
    - Follow the template's structure exactly.
    - Fill in the component checkboxes (e.g., `[x] mobile-app (Android)`).
    - **Labels**: Identify appropriate labels based on the template's default labels and the specific context. Ensure you include at least:
      - A component label (`component: mobile`, `component: backend`, `component: shared`, or `component: documentation`).
      - A type label (`feature`, `bug`, `type: chore`, `type: refactor`, `type: security`).
      - A priority label (`p0`, `p1`, `p2`, `p3`).
      - Any extra relevant tags (`ui`, `ux`, etc.).

5.  **Create Issue**: Use the `gh` CLI to create the issue, as it reliably handles label assignments and bypasses token permission restrictions on the native API tool.
    - **CRITICAL:** To avoid shell escaping and formatting issues with multi-line Markdown, ALWAYS write the drafted body to a temporary file first.

    ```bash
    # 1. Write the drafted content to a temporary file (using the write_file tool to /tmp/issue_body.md)
    # 2. Create the issue using the --body-file flag and apply all relevant labels
    gh issue create --title "[PREFIX] Succinct title" --body-file /tmp/issue_body.md --label "component: backend,feature,p1"
    # 3. Remove the temporary file
    rm /tmp/issue_body.md
    ```

6.  **Verify**: Confirm the issue was created successfully and provide the link to the user.

7.  **Relate Issues (if applicable)**: If you are generating two or more issues that are logically related (e.g., part of the same feature or plan). Be mindful because usually with the two or more issues is more likely a task that involves backend and mobile (frontend):
    - Create a "Master" tracking issue (Parent Issue) using the `gh` CLI.
    - **CRITICAL**: To add sub-issues via the `mcp_github_sub_issue_write` tool, you MUST use the **internal database integer ID** of the sub-issue, not its display number (e.g., #102).
        1. Fetch the integer ID for each sub-issue using the `gh` CLI:
           ```bash
           gh api repos/99lash/BorrowHub/issues/<DISPLAY_NUMBER> --jq .id
           ```
        2. Use the resulting long integer (e.g., `4207017606`) as the `sub_issue_id` in the `mcp_github_sub_issue_write` tool.
        3. Use the Master issue's regular display number (e.g., `104`) as the `issue_number`.
    - Explicitly link the Master issue in the comments or body of the sub-tasks.

## Principles

- **Clarity**: Titles should be descriptive and follow project conventions (e.g., prefixing with `[FEAT]`, `[BUG]`, `[CHORE]`).
- **Defensive Formatting**: Always use temporary files with `--body-file` to prevent newline and special character issues in bash.
- **Architectural Alignment**: When writing the body, explicitly reference the project's architectural standards (MVVM + Repository for Mobile, Service-Repository for Laravel) where the template asks for it.
- **Completeness**: Provide all requested information defined in the templates.
