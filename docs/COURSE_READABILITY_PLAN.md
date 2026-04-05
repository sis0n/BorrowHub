# Plan: Course Text Visibility and UI Consistency

## I. Overview
This plan addresses the issue of long course names (e.g., "Bachelor of Science in Information Technology") being truncated with ellipses in various parts of the system. This truncation makes it difficult for users to distinguish between courses with similar prefixes.

## II. Objectives
1.  **Readability**: Ensure full course names are visible in the Student List, Borrow Form, and Student Management Dialogs.
2.  **Consistency**: Apply a uniform multi-line or wrapping strategy across all course-related UI components.

## III. Technical Implementation

### 1. Student List (`item_student_row.xml`)
- **Modify `tvCourse`**:
    - Remove `android:maxLines="1"` (if present) or ensure it's not restricted.
    - Set `android:layout_width="0dp"` and `android:layout_weight="1"` if inside a horizontal container to allow wrapping.
    - Alternatively, let the badge wrap naturally by removing any fixed constraints that force single-line display.

### 2. Input Fields (`dialog_student.xml` & `fragment_borrow_item.xml`)
- **Modify `acCourse` (`AutoCompleteTextView`)**:
    - Add `android:inputType="textMultiLine"`.
    - Set `android:minLines="1"` and `android:maxLines="3"` to allow the input to grow vertically as needed when a long course is selected.
    - Ensure the parent `TextInputLayout` has `android:layout_height="wrap_content"`.

### 3. Dropdown Menu Readability
- **Custom Dropdown Layout**:
    - Create a new layout `item_dropdown_multiline.xml` containing a single `TextView` with `android:maxLines="3"` and `android:ellipsize="none"`.
    - Update the `ArrayAdapter` initialization in `StudentManagementFragment.java` and `BorrowItemFragment.java` to use this new layout instead of `android.R.layout.simple_dropdown_item_1line`.
    - This ensures that while selecting from the list, the user can read the full course title.

## IV. Verification Strategy
- **Visual Check**: Open the Student Management list and verify that students with long course names now show the course wrapped into 2 or 3 lines.
- **Selection Check**: Open the Borrow Form, click the Course dropdown, and verify that the items in the list wrap and are fully readable.
- **Form Check**: Select a long course and verify the input field expands to show the full name without "..." truncation.
