# Plan: Inventory Layout Refinement

## I. Overview
This plan details the UI refactoring of the Inventory screen to improve the placement of the "Add Item" button. The goal is to move the button from a cramped horizontal header to a more prominent vertical layout, consistent with the User Management screen.

## II. UI/UX Requirements
1.  **Consistency:** Align the header layout of `fragment_inventory.xml` with `fragment_user_management.xml`.
2.  **Accessibility:** Make the "Add Item" button more accessible by increasing its hit area (full width) and placing it in a dedicated row.
3.  **Clarity:** Ensure the title and subtitle have sufficient space without being squeezed by the action button.

## III. Technical Implementation

### 1. XML Layout Updates (`fragment_inventory.xml`)
- **Header Structure**:
    - Change `layoutInventoryHeader` (LinearLayout) `android:orientation` to `"vertical"`.
    - Remove the inner `LinearLayout` that currently wraps the `TextView`s.
- **Button Modification**:
    - Set `btnAddItem` `android:layout_width` to `"match_parent"`.
    - Set `btnAddItem` `android:layout_marginTop` to `"10dp"`.
    - (Optional) Ensure `app:iconGravity="textStart"` is maintained for visual balance.

## IV. Verification Strategy
- **Visual Check**: Verify that the Inventory header looks identical in structure to the User Management header.
- **Responsiveness**: Check the layout on different screen sizes to ensure the full-width button doesn't look awkward on very wide screens (tablet vs. phone).
