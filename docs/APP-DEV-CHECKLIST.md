# ANDROID APPLICATION DEVELOPMENT (UNIT II – FINAL TERM)

## I. Overview
This part demonstrates competency in application development using Android Studio, XML, and Java. It covers the ability and knowledge to design, develop, and assess a complete Android application using both technical skills and conceptual understanding aligned with UNIT II learning outcomes.

## II. Intended Learning Outcomes
The students should demonstrate comprehensive ability to:
1. Design and implement an interface that adapts correctly to different screen sizes, densities, and orientations.
2. Create a consistent and usable interface by defining styles, applying themes, implementing scrollable content, and using fragments.
3. Implement user and system event handling using Java, including click events, Broadcast Receivers, and configuration change management.
4. Design and manage menus and the action bar, including dynamic updates at runtime and display data using intents and lists, including custom list items and click handling (pass, receive, return).
5. Implement SQLite and demonstrate through working CRUD operations and manipulate dynamic data using appropriate SQL queries.

## III. Content

### 1. Supporting Multiple Screens
- [x] **1.1. Dealing with android market fragmentation** (Screen resolutions & densities: mdpi, hdpi, xhdpi, etc., Android OS versions)
- [x] **1.2. Creating drawable resources for multiple screens** (PNG/JPG, Icons, Vector graphics, Shapes, Backgrounds)
- [ ] **1.3. Creating stretchable 9-path graphics** (9-Patch Image; file name .9.png)
- [x] **1.4. Creating a custom launcher icon** (app icon display on home screen)

### 2. Managing the User Interface
- [x] **2.1. Defining and using styles** (Implemented in `themes.xml` and custom layout styles)
- [x] **2.2. Applying application themes** (Implemented Day/Night themes in `res/values` and `res/values-night`)
- [x] **2.3. Creating a scrollable text display** (Implemented using `NestedScrollView` and `RecyclerView` in Dashboard, Inventory, and Borrow forms)
- [x] **2.4. Laying out a screen with fragments** (Used `NavHostFragment` and `nav_graph.xml` for core navigation)

### 3. Working with Events
- [x] **3.1. Handling user events with Java code** (Implemented `setOnClickListener`, `TextWatcher` for student lookup, and form validation logic)
- [ ] **3.2. Creating a Broadcast Receiver to handle system events** (Pending: Can be implemented for network connectivity or battery alerts)
- [x] **3.3. Handling orientation and other configuration changes** (Handled via dynamic layout logic in `InventoryFragment` and `EdgeToEdge` support in `MainActivity`)

### 4. Working with Menus and the Action Bar
- [x] **4.1. Adding items to the options menu** (Implemented `menu_profile.xml` for Top App Bar)
- [x] **4.2. Displaying menu items in the action bar** (Used `MaterialToolbar` in `MainActivity` with `app:menu`)
- [x] **4.3. Managing the action bar and menus at runtime** (Handled dynamic navigation and menu item clicks in `MainActivity.java`)

### 5. Working with Data
- [x] **5.1. Passing data to an activity with intent extras** (Handled in `LoginActivity` to `MainActivity` flow)
- [x] **5.2. Receiving data in a new activity** (Implemented session token retrieval and user data display)
- [ ] **5.3. Returning data to a calling activity** (Pending: Can be implemented for selecting students/items in a sub-activity)
- [x] **5.4. Displaying data in a list** (Implemented multiple `RecyclerViews`: Items, Students, Transactions, and Active Borrows)
- [x] **5.5. Handling list items click events** (Implemented click listeners for editing/deleting items and processing returns)
- [x] **5.6. Customizing the list item** (Created custom XML layouts for inventory rows, student cards, and transaction logs)
- [x] **5.7. Exploring other uses of data** (Implemented data binding concepts and ViewModel-to-View communication)

### 6. Working with dynamic data using SQLite
- [x] **6.1. Fetching data from database** (Implemented via Room `DAO` and `LiveData`)
- [x] **6.2. Understanding different SQL queries.** (Used `@Query` for searching, filtering, and bulk operations)
- [x] **6.3. Creating CRUD application using SQLite Database** (Full CRUD implemented for **Inventory Items** and **Student Records**)

---

### 📌 Checkpoint Info
- **Last Updated:** Saturday, March 21, 2026 at 5:24 AM
- **Latest Change:** [[FEAT] Student Management Mobile Integration (Admin Profile Flow) (#68)](https://github.com/99lash/BorrowHub/commit/defe72b90388b5e0fb618cf8aa779e28054a5e45)
