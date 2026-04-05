# Plan: User Dialog UI Consistency

## I. Overview
This plan addresses a UI inconsistency in the User Management dialog (`dialog_user.xml`). Specifically, the "Role" dropdown field lacks the necessary start padding/space compared to the name and username fields, making the text look misaligned.

## II. Objectives
1.  **Alignment**: Fix the inline-start padding for the `AutoCompleteTextView` in the user role field.
2.  **Standards**: Use the Material 3 standard style for exposed dropdown menus to ensure consistent spacing and behavior.

## III. Technical Implementation

### 1. Layout Modification (`dialog_user.xml`)
- **Update `TextInputLayout`**:
    - Apply `style="@style/Widget.Material3.TextInputLayout.FilledBox.ExposedDropdownMenu"` to the `TextInputLayout` containing the `acRole` field.
    - This style naturally handles the internal padding for the dropdown icon and the text content.
- **Update `AutoCompleteTextView`**:
    - Ensure it is correctly positioned inside the styled parent.
    - (Optional) If the style doesn't fully resolve the padding in the current environment, manually add `android:paddingStart="16dp"` to match the `TextInputEditText` defaults.

## IV. Verification Strategy
- **Visual Check**: Open the "Add User" or "Edit User" dialog and verify that the "Role" text is horizontally aligned with the "Full Name" and "Username" text.
- **Interaction Check**: Ensure the dropdown still functions correctly and shows the options when clicked.
