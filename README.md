# MyGUI Registration Form

This Java application implements a simple registration form with Swing GUI components. Users can register by providing a username and password, and their information is stored in a CSV file. Registered users can then log in using their credentials.

## Overview

The MyGUI class serves as the main entry point for the application. It extends JFrame to create the GUI window. Key functionalities and methods include:

### MyGUI()

- *Constructor*: Sets up the main GUI window with registration form components.
- *Components*: Utilizes Swing components to create a user-friendly interface for registration.
- *Data Handling*: Loads existing user data from a CSV file, validates input, and manages user registration and login.

### loadUsersFromCSV()

- *Method*: Loads user data from the CSV file (users.csv) into a Map<String, User>.
- *File Handling*: Uses BufferedReader to read the CSV file line by line and stores user data in the map.
- *Error Handling*: Displays error messages for file not found or IO errors during data loading.

### saveUsersToCSV()

- *Method*: Saves user data from the usersMap to the CSV file.
- *File Handling*: Uses BufferedWriter to write user data to the CSV file.
- *Error Handling*: Displays error messages for IO errors during data saving.

### isPasswordStrong(String password)

- *Method*: Checks whether a password meets certain criteria for strength.
- *Criteria*: Ensures the password contains at least one uppercase letter, one lowercase letter, and is at least 8 characters long.
- *Regex*: Utilizes regular expressions for password validation.

## Setup

1. *Environment*: Requires a Java Runtime Environment (JRE).
2. *Execution*: Compile and execute the MyGUI.java file to run the application.
3. *Dependencies*: No external dependencies beyond standard Java libraries.

## Usage

1. *Registration*: Enter a unique username and password, then click "Register".
2. *Login*: Use registered credentials to log in.
3. *Admin Access*: Admin login available with username "admin" and password "admin".
4. *Error Handling*: Error messages displayed for invalid input or file handling errors.
5. *Persistence*: User data saved to CSV file upon registration and loaded during application startup.

# ShowBooks Application

This Java application presents a graphical interface for exploring a library of books. Users can search for books by title or author, view book details including ratings and reviews, and add selected books to their personal library.

## Overview

The ShowBooks class extends JFrame to create the main window of the application. It includes functionalities for displaying books, filtering them, and adding selected books to the user's library. Key methods and functionalities include:

### ShowBooks(String username)

- *Constructor*: Sets up the main GUI window with book display and interaction components.
- *GUI Components*: Utilizes Swing components to create an interface for browsing and selecting books.
- *Data Handling*: Loads book data from a CSV file, filters books based on user input, and manages user interactions such as adding books to the library.

### readDataFromCSV()

- *Method*: Reads book data from a CSV file and populates the table with book details.
- *File Handling*: Reads book information from the CSV file and parses it to display in the GUI.
- *Data Presentation*: Displays book titles, authors, ratings, and reviews in the table.

### filterBooks(String query)

- *Method*: Filters books based on the user's search query (title or author).
- *Search Functionality*: Compares search query with book titles and authors to filter the displayed books.
- *User Interaction*: Updates the table with filtered books in real-time.

### calculateAverageRatings()

- *Method*: Calculates the average rating for each book based on user ratings.
- *Data Processing*: Computes the average rating by aggregating user ratings for each book.
- *Dynamic Update*: Updates the table with recalculated average ratings for all books.

### addToLibrary(String username)

- *Method*: Adds selected books to the user's personal library.
- *User Interaction*: Enables users to select books from the displayed list and add them to their library.
- *Data Handling*: Manages user book history and updates personal CSV file with added books.

## Usage

1. *Browse Books*: View available books in the library.
2. *Search*: Enter keywords to search for specific books by title or author.
3. *View Details*: Click on book titles to view average ratings and reviews.
4. *Add to Library*: Select desired books and click "Add to Library" to add them to your personal collection.
5. *Personal Database*: Access your personal library and view added books.

## Setup

1. *Environment*: Requires a Java Runtime Environment (JRE).
2. *Execution*: Compile and execute the ShowBooks.java file to run the application.
3. *Dependencies*: No external dependencies beyond standard Java libraries.

# FilterAndSorting Utility

The FilterAndSorting utility class provides functionalities for sorting data in a JTable. It allows users to sort table columns in ascending or descending order by clicking on the column headers.

## Methods

1. **SortSelected(JTable table)**
   - *Functionality*: Initializes sorting functionality for the specified JTable.
   - *Initialization*: Sets up the initial state and data for sorting.
   - *Listener*: Adds a mouse listener to the table header for sorting when headers are clicked.

2. **SortMouseListener (Inner Class)**
   - *Purpose*: Implements MouseListener to detect clicks on table headers for sorting.
   - *Sorting Logic*: Determines the sorting order (ascending, descending, or original) for clicked columns.
   - *Data Manipulation*: Updates the table model with sorted data based on the column clicked and sorting order.

3. **MultiColumnComparator (Inner Class)**
   - *Objective*: Implements Comparator to compare multiple columns for sorting.
   - *Sorting Order*: Determines the sorting order for each column based on the sorting states.
   - *Comparison*: Compares values of rows based on the sorting order of each column.

## Usage

1. *Sorting*: Click on table headers to sort the corresponding column in ascending, descending, or original order.
2. *Data Display*: Display and manipulate data in the JTable using the provided sorting functionality.

## Implementation

- *Integration*: Integrate the FilterAndSorting class with a JTable component to enable sorting.
- *Listener*: Add a mouse listener to the table header to trigger sorting when headers are clicked.
- *Data Structure*: Maintain a map of column indices to their sorting states for efficient sorting.

# Personal Library Management System

This Java program serves as a Personal Library Management System. It allows users to manage their personal library, add new books, track reading progress, and write reviews.

## Features

1. *Graphical User Interface (GUI)*: The program utilizes Java's Swing library to provide an interactive GUI for users to manage their personal library.

2. *Add Books*: Users can add books to their library by selecting from a list of available books.

3. *Track Reading Progress*: Users can track their reading progress by updating book status, adding start and end dates, and specifying the time spent reading.

4. *Rate and Review*: Users can rate and review books once they have completed reading them.

5. *Delete Books*: Users can delete books from their library.

6. *Save Data*: The program saves user data to a CSV file for persistent storage.

## Usage

1. *Compile and Run*: Compile the Java code provided (AddToLibraryFrame.java) and run the compiled file to launch the Personal Library Management System.

2. *Login/Register*: Users need to login with their username. If a user does not have an account, they can register by providing a username.

3. *Add Books*: Users can add books to their library by selecting them from a list of available books. The added books will be displayed in the main interface.

4. *Track Reading Progress*: Users can update the status of each book (Not Started, Ongoing, Completed), add start and end dates, and specify the time spent reading.

5. *Rate and Review*: Once a book is marked as "Completed", users can rate it (1 to 5 stars) and write a review.

6. *Delete Books*: Users can delete books from their library by selecting the book and clicking the "Delete" button.

7. *Save Data*: Users should click the "Save" button to save their library data. The data will be stored in a CSV file (personal.csv) for future use.

## Dependencies

- Java Development Kit (JDK): Ensure you have Java installed on your system to compile and run the program.
- Swing Library: The program utilizes Java's Swing library for creating the graphical user interface.

## Additional Notes

- *Data Persistence*: User library data is saved to a CSV file (personal.csv). Make sure the file is accessible and writable by the program.

- *Error Handling*: The program provides error messages for invalid input and operations to guide users during interaction.

## Methods

1. AddToLibraryFrame(ArrayList<String[]> selectedBooks, ArrayList<String[]> userBookHistory, String username): Constructor method for the main frame of the Personal Library Management System. It initializes the GUI components and sets up the interface for managing the user's library.

2. isDataComplete(): Checks if all required data in the table is complete. Returns true if all required fields are filled, otherwise false.

3. isDataComplete(int row): Checks if data for a specific row in the table is complete. Returns true if all required fields are filled, otherwise false.

4. saveData(String username): Saves the user's library data to a CSV file. Data includes the user's username, book details, reading progress, ratings, and reviews.

5. deleteBookByUsernameAndTitle(String username, String title): Deletes a book entry from the user's library based on the username and title of the book.

6. showUserDetailsDialog(String title, String author, String averageRating, String userRating, String userReview, int rowIndex): Displays a dialog box for users to view and edit their rating and review for a particular book.

7. NonEditableCellRenderer: Custom cell renderer class to make specific columns non-editable in the table.

8. RatingCellEditor: Custom cell editor class for the "User Rating" column. Validates input and ensures only valid ratings (0 to 5) are entered.

9. TimeCellEditor: Custom cell editor class for the "Spend Time (minutes)" column. Validates input and ensures only non-negative integers are entered.

10. DateCellEditor: Custom cell editor class for the "Start Date" and "End Date" columns. Validates date format and ensures logical date constraints are met.

11. ButtonRenderer: Custom cell renderer class for rendering buttons in a table cell.

12. ButtonEditor: Custom cell editor class for handling button clicks in a table cell.

These methods and classes together facilitate the functionality of the Personal Library Management System, allowing users to effectively manage their personal libraries through a graphical interface.
   

 # Book Management System - Method Explanation

## deleteBook()
- Deletes a book from the library collection.
- Prompts the user to select a book to delete.
- Removes the selected book from the collection, updates the CSV file, and refreshes the table model.

## refreshTableModel()
- Clears the table model and refreshes it with updated data from the library collection.

## updateBook()
- Updates the information of an existing book.
- Prompts the user to select a book to update.
- Allows the user to modify the title and author fields, updates the CSV files, and refreshes the table model.

## updatePersonalCSV(String oldTitle, String oldAuthor, String newTitle, String newAuthor)
- Updates the personal CSV file with changes made to a book's title or author.
- Reads the original CSV file, modifies the relevant entries, and writes the updated data to a temporary file.
- Replaces the original CSV file with the temporary file.

## saveBooksToFile()
- Saves the current book collection to a CSV file.
- Writes the book titles and authors to the CSV file.

## showUserManagementDialog()
- Displays a dialog for managing user accounts.
- Allows administrators to delete user accounts.
- Updates the table model and reopens the application if a user account is deleted.

## readUserData(String filename)
- Reads user data from a CSV file.
- Parses the CSV file and returns the user data as an ArrayList of String arrays.

## deleteUserData(String usernameToDelete)
- Deletes user data from the user CSV file and the personal CSV file.
- Reads the original files, removes entries associated with the specified username, and replaces the original files with updated versions.

## addNewBook()
- Displays a dialog for adding a new book to the library collection.
- Allows users to input the title and author of the new book.
- Appends the new book to the CSV file and updates the table model.

## appendBookToCSV(String title, String author)
- Appends a new book to the CSV file.
- Writes the title and author of the new book to the CSV file.

## updateTableModel(String title, String author)
- Updates the table model with a new book.
- Adds a row to the table model with the title, author, and default values for rating and reviews.

## readDataFromCSV()
- Reads book data from the CSV file.
- Parses the CSV file and populates the library collection and table model with the retrieved data.

## loadReviewsForBook(String title)
- Loads reviews for a specific book from the personal CSV file.
- Searches for reviews associated with the specified book title and returns them as a string.

## calculateAverageRatingForBook(String title)
- Calculates the average rating for a book based on user ratings.
- Loads user ratings from the personal CSV file, calculates the average rating, and returns it as a formatted string.

## getUserRatingForBook(String username, String title)
- Retrieves a user's rating for a specific book from the personal CSV file.
- Searches for the user's rating associated with the specified username and book title and returns it as a string.

## getUserReviewForBook(String username, String title)
- Retrieves a user's review for a specific book from the personal CSV file.
- Searches for the user's review associated with the specified username and book title and returns it as a string.

## showUserDetails(String title, String author, String averageRating, String username, String userRating, String userReview)
- Displays detailed information about a book and user ratings/reviews in a new frame.
- Allows users to delete their reviews for the book.

## updateUserReview(String username, String title, String newReview)
- Updates a user's review for a book in the personal CSV file.
- Modifies the user's review to the specified value.

## calculateAverageRatings()
- Calculates the average ratings for all books in the table.
- Updates the table model with the calculated average ratings.

## loadUserRatings()
- Loads user ratings from the personal CSV file.
- Parses the CSV file and returns user ratings as an ArrayList of String arrays.

## saveDataToGeneralCSV()
- Saves book data to a general CSV file.
- Writes book data from the table model to the CSV file, updating average ratings in the personal CSV file.

## updateRatingInPersonalCSV(String title, String rating)
- Updates a book's rating in the personal CSV file.
- Reads the original file, modifies the relevant entry, and writes the updated data back to the file.

## filterBooks(String query)
- Filters books based on a search query.
- Searches for books whose title or author matches the query and updates the table model with the filtered results.

## showAllBooks()
- Displays all books in the table.
- Updates the table model with all books from the library collection.

## updateTable(ArrayList<String[]> books)
- Updates the table with the provided list of books.
- Clears the table model and populates it with the specified books.
  
 ### Challenges 
  *File I/O Handling:* 
   - Managing CSV files and reading/writing data posed challenges in handling file streams and parsing data accurately.

2. *UI Design:* 
   - Designing an intuitive user interface that effectively communicates features and functionalities required careful layout planning and usability considerations.

3. *Data Management:* 
   - Ensuring data integrity, synchronization, and updating across different parts of the application presented challenges in managing book data and user information.

   ### youtube link of our presentation 
   https://www.youtube.com/watch?v=T2Xv536vMPk
