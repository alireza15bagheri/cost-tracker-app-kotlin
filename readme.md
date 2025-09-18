Simple Cost Tracker

Simple Cost Tracker is a native Android application designed for personal budget management and expense tracking. It allows users to create distinct financial periods (e.g., monthly budgets), track incomes, set budgets for different categories, and monitor daily household spending with a unique carryover system. The app is built with modern Android development technologies and uses Firebase for backend services.

✨ Core Features

    User Authentication: Secure sign-up and login using Firebase Authentication with email and password.

Multi-Language Support: The interface supports both English and Persian (Farsi).

Period-Based Tracking:

    Create distinct financial periods with a name, start date, and end date.

Select an active period from a dropdown menu on the dashboard to view its specific data.

Delete a period and all its associated data (incomes, budgets, costs) in a single operation.

Financial Management:

    Incomes: Add multiple income sources with descriptions and amounts for each period.

Budgets: Set budgets for various categories (e.g., Groceries, Rent) and track their total. Toggle the payment status between "Paid" and "Unpaid" directly from the dashboard.

Miscellaneous Costs: Record miscellaneous one-off expenses.

Daily Spending System:

    Set a daily spending limit for the active period.

The app automatically generates an entry for each day within the period.

Features a 

carryover system: any unspent amount from one day automatically rolls over to the next, increasing its budget.

Comprehensive Dashboard:

    At-a-glance view of the active period's financial health.

Summaries for total income, total budget, planned daily spending, and total miscellaneous costs.

Calculates and displays the final 

Total Remaining balance for the period.

User Account Management:

    Users can change their password securely.

A dedicated sign-out option is available in the navigation drawer.

Data Persistence: All financial data is stored and synced in real-time using Google Firestore.

🛠️ Tech Stack & Architecture

    Language: Kotlin

    UI: Jetpack Compose for building the entire UI declaratively.

Architecture: Follows the MVVM (Model-View-ViewModel) architecture pattern.

Backend: Firebase

    Authentication: For user management.

Firestore: As a real-time NoSQL database for all app data.

Asynchronous Programming: Kotlin Coroutines and Flow are used extensively to handle asynchronous operations and data streams from Firestore.

Navigation: Jetpack Navigation for Compose to manage screen transitions.

Dependency Injection: Manual dependency injection via ViewModel Factories.

Code Obfuscation: ProGuard rules are configured to shrink and obfuscate the code for release builds.

⚙️ Firebase Setup

To build and run this project, you need to set up your own Firebase project.

    Go to the Firebase Console.

    Create a new project.

    Add an Android app to your Firebase project with the package name fr.alirezabagheri.simplecosttracker.

    Enable Authentication: Go to the "Authentication" section, select the "Sign-in method" tab, and enable the Email/Password provider.

    Enable Firestore: Go to the "Firestore Database" section and create a new database. Start in test mode for initial development (you can secure it with security rules later).

    Download Config File: Download the google-services.json file provided by Firebase.

    Place the File: Place the downloaded google-services.json file in the app/ directory of this project.

Firestore Data Structure

The app uses the following collections in Firestore:

    periods: Stores period documents containing a userId, name, startDate, endDate, etc. 

incomes: Stores income documents, each linked to a period via periodId.

budgets: Stores budget documents, each linked to a period via periodId.

dailySpendings: Stores daily spending documents, each linked to a period via periodId.

miscCosts: Stores miscellaneous cost documents, each linked to a period via periodId.

🚀 Building and Running

    Clone the repository:
    Bash

    git clone https://github.com/alireza15bagheri/simplecosttracker.git

    Follow the Firebase Setup steps above to add your own google-services.json file.

    Open the project in Android Studio.

    Let Gradle sync the dependencies.

    Run the app on an emulator or a physical device.

Note: The project contains release keystore credentials in the gradle.properties file. For your own release builds, it is highly recommended to replace these with your own secure keystore file and credentials.