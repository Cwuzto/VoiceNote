# VoiceNote 🎙️ - Intelligent Voice-Command POS System

> **Offline-first Android Point-of-Sale application focused on speed and simplicity.**

## 📖 Introduction
**VoiceNote** is a mobile application designed to streamline the ordering process for small F&B businesses (Coffee shops, small restaurants). Unlike traditional POS systems that require multiple touches to navigate menus, VoiceNote utilizes **Speech-to-Text technology** combined with a smart **Syntax Parser** to input orders in seconds.

The application is built with an **Offline-first** architecture, ensuring stability and zero latency even without an internet connection.

## ✨ Key Features

* **🎙️ Voice Ordering:** Instantly convert speech commands (e.g., *"2 coffee 1 tea"*) into order items using Android SpeechRecognizer and custom Regex logic.
* **⚡ Offline-First:** 100% functional without internet. Data is persisted locally using **Room Database**.
* **🛒 Order Management:** efficient cart management, order status tracking (Unpaid/Paid), and history review.
* **📊 Analytics Dashboard:** Visual revenue reports and "Best Seller" tracking using **MPAndroidChart**.
* **👥 Role-Based Access:** Distinct interfaces and permissions for **Store Owners** (Admin) and **Employees**.
* **📝 Quick Input:** Smart parsing for manual text input (shortcuts supported).

## 🛠️ Tech Stack & Architecture

The project follows modern Android development standards:

* **Language:** Java
* **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
* **Local Database:** Room Persistence Library (SQLite)
* **UI Components:** Material Design 3, RecyclerView, CardView
* **Concurrency:** LiveData, Executors (Background Threading)
* **Third-party Libraries:**
    * `MPAndroidChart` (Data Visualization)
    * `Lottie` (Animations)

### Architecture Overview
`View (Activity/Fragment)` ↔ `ViewModel (LiveData)` ↔ `Repository` ↔ `Room DAO` ↔ `SQLite`

## 📸 Screenshots

| Overview Dashboard | 
<img width="398" height="666" alt="image" src="https://github.com/user-attachments/assets/3e0644b3-7842-4862-8c21-5e6eaeb08237" />

📊 Dashboard & Analytics
The command center for business owners, providing a comprehensive real-time overview of business performance.

🧩 Key Components
📈 Quick Stats Cards: Instant summary displaying Total Revenue and Total Orders for the selected period.

📉 Interactive Revenue Chart:

Built with MPAndroidChart library.

Visualizes trends by Day or Hour.

Features Cubic Bézier curves for smoothness, Gradient fills for aesthetics, and Touch-to-highlight values.

🏆 Best-Selling Products: A ranked list of the Top 3 most popular items, visualized with medal icons (🥇, 🥈, 🥉).

📅 Smart Time Filter: Flexible reporting ranges including Today, Yesterday, Last 7 Days, This Month, and Last Month.

⚙️ Technical Highlights
Real-time Aggregation: Data is fetched and calculated directly from the Room Database using optimized SQL queries (SUM, COUNT, GROUP BY).

Adaptive UI / Edge Case Handling:

Empty State: Displays a friendly placeholder when no data is available.

Smart Rendering: Automatically injects anchor points (0-value) for single-data-point scenarios to ensure the chart always renders a visually smooth curve.

| Product List | 
<img width="472" height="780" alt="image" src="https://github.com/user-attachments/assets/431b6f42-ff0f-4a78-a055-afc86bfda6f0" />

🍔 Menu & Product Management
Empowers store owners to easily manage their inventory with full CRUD capabilities.

🧩 UI Components
🗂️ Smart Listing: Products are automatically sorted alphabetically and visually grouped by their initial letter (Sectioned Headers A, B, C...) for fast navigation.

📄 Item Display: Each row clearly presents the Product Name and Unit Price.

➕ Quick Add: A prominent Floating Action Button (FAB) fixed at the bottom corner for rapid item creation.

🔍 Search Toolbar: Integrated Search and Clear buttons located on the top bar.

⚙️ Key Functionalities (CRUD)
Create: Opens an input Dialog for Name and Price. Includes built-in duplicate name validation to prevent data redundancy (junk data).

Update: Simply tap on any item to modify its Name or Price instantly.

Delete: Remove items from the menu via the trash icon button.

Real-time Search: Filters the product list instantly as you type, allowing for quick lookup even with large menus.

| Voice Order |
<img width="416" height="686" alt="image" src="https://github.com/user-attachments/assets/c76f3248-2279-4cff-ae95-0283ecd0c09f" />

🛒 Point of Sale (POS) & Voice Ordering
The primary workspace for staff, engineered for high-speed operation and efficiency.

🧩 UI Components
🔲 Quick Access Grid: Displays frequent menu items as touchable Cards, allowing for rapid one-tap selection.

🧾 Dynamic Cart: Lists currently ordered items with quantity and unit price. Supports modern gestures like Swipe-to-Delete for quick removal.

🎙️ Voice Toolbar: The central control hub featuring a Microphone trigger and a real-time WaveformView that visualizes audio input animation while speaking.

⌨️ Manual Input Bar: A fallback input method allowing staff to type item names and prices manually if preferred.

⚙️ Key Functionalities
🗣️ Voice Command (Core Feature):

Simply speak natural commands (e.g., "2 Beef Pho").

The system's Syntax Parser automatically separates the Quantity (2) from the Product Name (Beef Pho).

Performs an instant database lookup and adds the correct items to the cart.

✏️ Order Customization: detailed control to Adjust item quantities or attach Special Notes (e.g., "No onions", "Less ice") to specific line items.

💾 Complete Transaction: The "Done" button validates the cart, commits the order to the Local Database, and redirects the user to the Order History stream.

| Order List |
<img width="328" height="543" alt="image" src="https://github.com/user-attachments/assets/58aae368-5a90-4a3a-9ed7-e868ec03f6ef" />

📜 Order History & Management
A comprehensive log of all transactions, designed for quick retrieval and financial tracking.

🧩 UI Components
⏳ Chronological List: Displays orders in reverse-chronological order (newest first) for immediate access to recent activity.

📌 Sticky Date Headers: Orders are intelligently grouped by date (e.g., Today, Yesterday). The header sticks to the top while scrolling and summarizes the Total Daily Revenue for that specific day.

💳 Informative Order Cards: Each card provides a snapshot including:

Customer Name & Creation Time.

Payment Status (Paid ✅ / Unpaid ⚠️).

Total Amount.

Item Preview: A concise summary listing the first 5 items of the order.

Filters: Tools to filter the list by Payment Status (All vs. Unpaid) or specific Time Ranges.

⚙️ Key Functionalities
Quick Status Check: Instantly identify unpaid orders or review daily performance at a glance.

Detailed View: Tap on any card to navigate to the full Order Detail screen for further actions.

🔍 Smart Search: Locate specific transactions instantly by searching for either the Customer Name or a specific Dish Name contained within the order.

| Order List Detail |
<img width="459" height="761" alt="image" src="https://github.com/user-attachments/assets/327ea597-5b3c-4be9-85f2-23a6dfc18f2d" />

| More Screen |
<img width="509" height="844" alt="image" src="https://github.com/user-attachments/assets/63135bc4-a791-4a42-a485-05a72fedeff7" />

## 🧠 Core Logic: The Parser

The heart of VoiceNote is the algorithm that converts raw text into structured data.
* **Input:** *"hai bún bò "* (2 beef noodles)
* **Process:**
    1.  **Normalization:** Convert words to numbers (hai -> 2, một -> 1).
    2.  **Tokenization (Regex):** Split string by numbers to identify item groups.
    3.  **Matching:** Query Local Database to find product ID and Unit Price.
* **Output:** Adds items to cart automatically.

## ⚙️ Installation

1.  Clone the repository:
    ```bash
    git clone [https://github.com/Cwuzto/VoiceNote.git](https://github.com/Cwuzto/VoiceNote.git)
    ```
2.  Open the project in **Android Studio** (Koala Feature Drop or newer recommended).
3.  Sync Gradle files.
4.  Run on an Emulator or Physical Device (Requires Android 8.0+).
    * *Note: For Voice features to work on Emulator, ensure Microphone permissions are enabled.*
