import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class ShowBooksAdmin extends JFrame {

    private DefaultTableModel tableModel;
    private JTextField searchField;
    private ArrayList<String[]> allBooks;
    
    public ShowBooksAdmin(String username) {
        setTitle("Explore Books");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Header panel with BorderLayout
        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel headerLabel = new JLabel("Books Library");
        headerLabel.setFont(new Font("Monospaced", Font.PLAIN, 33));
        headerLabel.setForeground(Color.BLACK);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        // Back button panel with FlowLayout
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            MyGUI loginPage = new MyGUI();
            loginPage.setVisible(true);
            dispose();
        });
        backButton.setBackground(Color.WHITE);
        backButtonPanel.add(backButton);

        headerPanel.add(backButtonPanel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        searchField = new JTextField(20);
        searchField.setToolTipText("Enter title or author to search");
        searchPanel.add(searchField);

        // Search button
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim().toLowerCase();
            filterBooks(query);
        });

        searchPanel.add(searchButton);

        headerPanel.add(searchPanel, BorderLayout.EAST);

        // Initialize table model
        tableModel = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) {
                    return Boolean.class;
                } else {
                    return String.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };
        tableModel.addColumn("Title");
        tableModel.addColumn("Author");
        tableModel.addColumn("Rating");
        tableModel.addColumn("Reviews");
        tableModel.addColumn("Selected");

        readDataFromCSV();

        // Table setup
        JTable bookTable = new JTable(tableModel);
        bookTable.setRowHeight(30);

        JTableHeader tableHeader = bookTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 14));
        tableHeader.setForeground(Color.BLACK);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        bookTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        bookTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        // Customize row colors
        bookTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(new Color(204, 229, 255)); // Pastel blue background color
                return c;
            }
        });

        bookTable.getColumnModel().getColumn(3).setCellRenderer(new ReviewCellRenderer());
        bookTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = bookTable.getColumnModel().getColumnIndex("Reviews");
                int row = bookTable.rowAtPoint(e.getPoint());

                if (column == 3 && row != -1) { // Clicked on review column and valid row
                    Rectangle cellRect = bookTable.getCellRect(row, column, false);
                    if (cellRect.contains(e.getPoint())) { // Check if click is within the cell bounds
                        String reviews = (String) bookTable.getValueAt(row, column);
                        if (!reviews.equals("No reviews")) {
                            String[] users = reviews.split(", ");
                            String selectedUser = (String) JOptionPane.showInputDialog(
                                    ShowBooksAdmin.this,
                                    "Select a user to view details:",
                                    "User Details",
                                    JOptionPane.PLAIN_MESSAGE,
                                    null,
                                    users,
                                    null);

                            if (selectedUser != null && !selectedUser.isEmpty()) {
                                // Get book details
                                String title = (String) bookTable.getValueAt(row, 0);
                                String author = (String) bookTable.getValueAt(row, 1);
                                String averageRating = (String) bookTable.getValueAt(row, 2);

                                // Get user's details for the selected book
                                String userRating = getUserRatingForBook(selectedUser, title);
                                String userReview = getUserReviewForBook(selectedUser, title);

                                // Display user's details in a separate window
                                showUserDetails(title, author, averageRating, selectedUser, userRating, userReview);
                            }
                        }
                    }
                }
            }
        });

        // Add the table to a scroll pane and then to the frame's center
        JScrollPane scrollPane = new JScrollPane(bookTable);
        add(scrollPane, BorderLayout.CENTER);

        // Panel for admin actions
        JPanel adminPanel = new JPanel(new GridLayout(1, 4));

        JButton deleteUserButton = new JButton("Manage Users");
        deleteUserButton.addActionListener(e -> showUserManagementDialog());

        JButton addBookButton = new JButton("Add New Book");
        addBookButton.addActionListener(e -> addNewBook());

        JButton deleteBookButton = new JButton("Delete Book");
        deleteBookButton.addActionListener(e -> deleteBook());

        JButton updateBookButton = new JButton("Update Book");
        updateBookButton.addActionListener(e -> updateBook());

        adminPanel.add(deleteUserButton);
        adminPanel.add(addBookButton);
        adminPanel.add(deleteBookButton);
        adminPanel.add(updateBookButton);

        add(adminPanel, BorderLayout.SOUTH);

        FilterAndSorting.SortSelected(bookTable);
        setVisible(true);

        calculateAverageRatings();

        saveDataToGeneralCSV();
    }

    private void deleteBook() {
        String[] bookTitles = allBooks.stream().map(book -> book[0] + " by " + book[1]).toArray(String[]::new);
        String selectedTitle = (String) JOptionPane.showInputDialog(this, "Select a book to delete:", "Delete Book",
                JOptionPane.QUESTION_MESSAGE, null, bookTitles, bookTitles[0]);

        if (selectedTitle != null) {
            allBooks.removeIf(book -> (book[0] + " by " + book[1]).equals(selectedTitle));
            saveBooksToFile();
            refreshTableModel(); 
        }
    }

    private void refreshTableModel() {
        tableModel.setRowCount(0); // Clear the table first
        for (String[] book : allBooks) {
            String averageRating = calculateAverageRatingForBook(book[0]);
            String reviews = loadReviewsForBook(book[0]);
            tableModel.addRow(new Object[] { book[0], book[1], averageRating, reviews, false });
        }
    }

    private void updateBook() {
        String[] bookTitles = allBooks.stream().map(book -> book[0] + " by " + book[1]).toArray(String[]::new);
        String selectedTitle = (String) JOptionPane.showInputDialog(this, "Select a book to update:", "Update Book",
                JOptionPane.QUESTION_MESSAGE, null, bookTitles, bookTitles[0]);

        if (selectedTitle != null) {
            String[] selectedBook = allBooks.stream()
                    .filter(book -> (book[0] + " by " + book[1]).equals(selectedTitle))
                    .findFirst()
                    .orElse(null);

            if (selectedBook != null) {
                JTextField titleField = new JTextField(selectedBook[0]);
                JTextField authorField = new JTextField(selectedBook[1]);
                Object[] message = {
                        "Title:", titleField,
                        "Author:", authorField
                };
                int option = JOptionPane.showConfirmDialog(this, message, "Update Book", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    // Update in the ArrayList first
                    String oldTitle = selectedBook[0];
                    String oldAuthor = selectedBook[1];
                    selectedBook[0] = titleField.getText().trim();
                    selectedBook[1] = authorField.getText().trim();

                    // Update CSV files
                    updatePersonalCSV(oldTitle, oldAuthor, selectedBook[0], selectedBook[1]);
                    saveBooksToFile();
                    refreshTableModel(); 
                }
            }
        }
    }

    private void updatePersonalCSV(String oldTitle, String oldAuthor, String newTitle, String newAuthor) {
        File inputFile = new File("personal.csv");
        File tempFile = new File("temp_personal.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                String[] data = currentLine.split(",");
                if (data[1].equals(oldTitle) && data[2].equals(oldAuthor)) {
                    data[1] = newTitle; // Update title
                    data[2] = newAuthor; // Update author
                    currentLine = String.join(",", data);
                }
                writer.write(currentLine + "\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (!inputFile.delete()) {
            System.out.println("Could not delete the original file");
            return;
        }
        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Could not rename the updated file to the original file name");
        }
    }

    private void saveBooksToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("brodsky_copy.csv"))) {
            bw.write("Title,Author\n"); 
            for (String[] book : allBooks) {
                bw.write(book[0] + "," + book[1] + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showUserManagementDialog() {
        JFrame userManagementFrame = new JFrame("Manage Users");
        userManagementFrame.setSize(400, 300);
        userManagementFrame.setLocationRelativeTo(this);

        // Boolean flag to track if any user was deleted
        final boolean[] userDeleted = new boolean[1];

        ArrayList<String[]> userData = readUserData("users.csv");

        DefaultTableModel userModel = new DefaultTableModel();
        userModel.addColumn("Username");
        userModel.addColumn("Password");
        userModel.addColumn("Delete");

        for (String[] userDataEntry : userData) {
            Object[] row = { userDataEntry[0], userDataEntry[1], "Delete" };
            userModel.addRow(row);
        }

        JTable userTable = new JTable(userModel);
        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = userTable.getColumnModel().getColumnIndex("Delete");
                int row = userTable.rowAtPoint(e.getPoint());

                if (column == 2 && row != -1) { // Clicked on delete button
                    String usernameToDelete = (String) userModel.getValueAt(row, 0);
                    deleteUserData(usernameToDelete);
                    userModel.removeRow(row);
                    userDeleted[0] = true; // Set flag to true as a user has been deleted
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(userTable);
        userManagementFrame.add(scrollPane);
        userManagementFrame.setVisible(true);

        // Add Window Listener
        userManagementFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (userDeleted[0]) { 
                    // Close the current instance of ShowBooksAdmin and create a new instance
                    dispose(); 
                    new ShowBooksAdmin("username").setVisible(true);
                                                                     
                }
            }
        });

        userManagementFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    // Method to read user data from users.csv
    private ArrayList<String[]> readUserData(String filename) {
        ArrayList<String[]> userData = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                userData.add(parts);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userData;
    }

    // Method to delete user data from users.csv and personal.csv
    private void deleteUserData(String usernameToDelete) {
        String usersFile = "users.csv";
        String personalFile = "personal.csv";
        String tempUsersFile = "temp_users.csv";
        String tempPersonalFile = "temp_personal.csv";

        try (BufferedReader reader = new BufferedReader(new FileReader(usersFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempUsersFile));
                BufferedReader personalReader = new BufferedReader(new FileReader(personalFile));
                BufferedWriter personalWriter = new BufferedWriter(new FileWriter(tempPersonalFile))) {

            // Rewrite users.csv excluding the user to delete
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (!parts[0].equals(usernameToDelete)) {
                    writer.write(line);
                    writer.newLine(); 
                }
            }

            // Rewrite personal.csv excluding entries of the deleted user
            String personalLine;
            while ((personalLine = personalReader.readLine()) != null) {
                String[] parts = personalLine.split(",");
                if (!parts[0].equals(usernameToDelete)) {
                    personalWriter.write(personalLine);
                    personalWriter.newLine(); // Write new line character after each line
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Replace original files with temporary files
        File usersCsv = new File(usersFile);
        File personalCsv = new File(personalFile);
        File tempUsersCsv = new File(tempUsersFile);
        File tempPersonalCsv = new File(tempPersonalFile);

        if (!usersCsv.delete() || !tempUsersCsv.renameTo(usersCsv)) {
            System.out.println("Error deleting user from users.csv");
        }

        if (!personalCsv.delete() || !tempPersonalCsv.renameTo(personalCsv)) {
            System.out.println("Error deleting user from personal.csv");
        }

        System.out.println("User deleted successfully.");
    }

    private void addNewBook() {
        JFrame addBookFrame = new JFrame("Add New Book");
        addBookFrame.setSize(300, 200);
        addBookFrame.setLayout(new GridLayout(0, 2, 10, 10)); 
        addBookFrame.setLocationRelativeTo(this);

        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();

        JLabel titleLabel = new JLabel("Title:");
        JLabel authorLabel = new JLabel("Author:");

        addBookFrame.add(titleLabel);
        addBookFrame.add(titleField);
        addBookFrame.add(authorLabel);
        addBookFrame.add(authorField);

        // Button to submit new book
        JButton submitButton = new JButton("Add Book");
        submitButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            if (!title.isEmpty() && !author.isEmpty()) {
                appendBookToCSV(title, author);
                addBookFrame.dispose(); // Close the frame after adding the book
                updateTableModel(title, author); // Update the table model
            } else {
                JOptionPane.showMessageDialog(addBookFrame, "Both title and author must be filled out.");
            }
        });

        addBookFrame.add(new JLabel()); 
        addBookFrame.add(submitButton);

        addBookFrame.setVisible(true);
    }

    private void appendBookToCSV(String title, String author) {
        String csvFile = "brodsky_copy.csv";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile, true))) {
            // Append the new book to the CSV file without enclosing in quotes
            bw.write(title + "," + author + "\n");

            // Update the in-memory list of books
            String[] newBook = { title, author };
            allBooks.add(newBook);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateTableModel(String title, String author) {
        // Assume default values for rating and reviews
        tableModel.addRow(new Object[] { title, author, "No Rating", "No Reviews", false });
        JOptionPane.showMessageDialog(this, "New book added successfully.");
    }

    private void readDataFromCSV() {
        allBooks = new ArrayList<>();
        String csvFile = "brodsky_copy.csv";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "UTF-8"))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length >= 1) {
                    String title = data[0].trim();
                    String author = "";

                    if (title.startsWith("\"") && title.endsWith("\"")) {
                        title = title.substring(1, title.length() - 1);
                    }

                    // Remove extra characters like "п»i"
                    title = title.replaceAll("[^\\p{Print}]", "");

                    
                    if (title.isEmpty()) {
                        title = "Unknown";
                    }

                    // If there's an author provided
                    if (data.length > 1) {
                        author = data[1].trim();
                    } else {
                        author = "Unknown";
                    }

                    String[] titles = title.split(",\\s*");

                    String[] authors = author.split(",\\s*");

                    // Add each book to the list
                    for (String bookTitle : titles) {
                        for (String authorName : authors) {
                            allBooks.add(new String[] { bookTitle.trim(), authorName.trim() });
                            String averageRating = calculateAverageRatingForBook(bookTitle);
                            String rating = averageRating.equals("No Rating") ? "No Rating" : averageRating;
                            // Load reviews for the current book
                            String reviews = loadReviewsForBook(bookTitle);
                            tableModel.addRow(new Object[] { bookTitle, authorName, rating, reviews, false });
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data from CSV file.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Load reviews for a specific book from personal.csv
    private String loadReviewsForBook(String title) {
        StringBuilder reviews = new StringBuilder();
        String csvFile = "personal.csv"; 

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean hasReviews = false;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // Check if the line contains the title and has a review
                if (data.length >= 2 && data[1].trim().equals(title) && !data[10].trim().isEmpty()) {
                    if (reviews.length() > 0) {
                        reviews.append(", ");
                    }
                    // Append the username who reviewed the book
                    reviews.append(data[0].trim());
                    hasReviews = true;
                }
            }
            // If no reviews were found, return "No reviews"
            if (!hasReviews) {
                return "No reviews";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return reviews.toString();
    }

    // Calculate average rating for a book
    private String calculateAverageRatingForBook(String title) {
        // Load data from personal.csv
        ArrayList<String[]> userRatings = loadUserRatings();

        double totalRating = 0.0;
        int count = 0;
        boolean hasRating = false; // Flag to track if any rating exists for the book
        for (String[] rating : userRatings) {
            if (title.equals(rating[0]) && !rating[1].isEmpty()) { // Compare with title from userRatings
                try {
                    totalRating += Double.parseDouble(rating[1]);
                    count++;
                    hasRating = true;
                } catch (NumberFormatException e) {
                    // Ignore invalid ratings
                }
            }
        }
        if (hasRating) {
            double averageRating = totalRating / count;
            // Return the formatted average rating and count for the book
            return String.format("%.2f (%d)", averageRating, count);
        } else {
            // If no rating exists, return "No Rating" and count as 0
            return "No Rating";
        }
    }

    // Custom cell renderer for the review column to make usernames clickable
    private class ReviewCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            label.setText("<html><u>" + value.toString() + "</u></html>"); // Underline usernames
            return label;
        }
    }

    // Method to get user's rating for the selected book
    private String getUserRatingForBook(String username, String title) {
        String csvFile = "personal.csv"; // Path to the CSV file containing user ratings

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 11 && data[0].equals(username) && data[1].trim().equals(title)) {
                    return data[9].trim(); // User Rating
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "No rating";
    }

    // Method to get user's review for the selected book
    private String getUserReviewForBook(String username, String title) {
        String csvFile = "personal.csv"; // Path to the CSV file containing user ratings

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 11 && data[0].equals(username) && data[1].trim().equals(title)) {
                    return data[10].trim(); // User Review
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "No review";
    }

    // Modify the showUserDetails method
    private void showUserDetails(String title, String author, String averageRating,
            String username, String userRating, String userReview) {
        JFrame userDetailsFrame = new JFrame("User Details");
        userDetailsFrame.setSize(400, 210); // Increased height for the delete button
        userDetailsFrame.setLocationRelativeTo(null);
        userDetailsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel userDetailsPanel = new JPanel();
        userDetailsPanel.setLayout(new BoxLayout(userDetailsPanel, BoxLayout.Y_AXIS));
        userDetailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 4, 3, 4)); // Add padding to the panel

        JLabel titleLabel = new JLabel("Title: " + title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 16)); // Adjust font size and style
        JLabel authorLabel = new JLabel("Author: " + author);
        authorLabel.setFont(new Font("Arial", Font.PLAIN, 16)); // Adjust font size and style
        JLabel ratingLabel = new JLabel("Average Rating: " + averageRating);
        ratingLabel.setFont(new Font("Arial", Font.PLAIN, 16)); // Adjust font size and style

        // Separator line between book info and user info
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.BLACK); // Set separator color to black
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2)); // Set maximum width and height for the separator

        JLabel userLabel = new JLabel("User: " + username);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 16)); // Adjust font size and style
        JLabel userRatingLabel = new JLabel("User Rating: " + userRating);
        userRatingLabel.setFont(new Font("Arial", Font.PLAIN, 16)); // Adjust font size and style
        JLabel userReviewLabel = new JLabel("User Review: " + userReview);
        userReviewLabel.setFont(new Font("Arial", Font.PLAIN, 16)); // Adjust font size and style

        // Button to delete review
        JButton deleteReviewButton = new JButton("Delete Review");
        deleteReviewButton.addActionListener(e -> {
            // Change the user's review to an empty string
            updateUserReview(username, title, "");
            // Update the userReviewLabel text to reflect the change
            userReviewLabel.setText("User Review: ");
            JOptionPane.showMessageDialog(userDetailsFrame, "Review deleted successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Close the current userDetailsFrame
            userDetailsFrame.dispose();

            // Close the current ShowBooksAdmin frame
            dispose();

            // Reopen the ShowBooksAdmin frame
            new ShowBooksAdmin(username);
        });

        userDetailsPanel.add(titleLabel);
        userDetailsPanel.add(authorLabel);
        userDetailsPanel.add(ratingLabel);
        userDetailsPanel.add(Box.createVerticalStrut(10)); // Add spacing between book info and separator
        userDetailsPanel.add(separator); // Add separator line
        userDetailsPanel.add(Box.createVerticalStrut(10)); // Add spacing between separator and user info
        userDetailsPanel.add(userLabel);
        userDetailsPanel.add(userRatingLabel);
        userDetailsPanel.add(userReviewLabel);
        userDetailsPanel.add(deleteReviewButton); // Add delete review button

        userDetailsFrame.add(userDetailsPanel);
        userDetailsFrame.setVisible(true);
    }

    // Method to update user's review in personal.csv
    private void updateUserReview(String username, String title, String newReview) {
        String csvFile = "personal.csv";
        String tempFile = "tempd.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile));
                BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data[0].equals(username) && data[1].trim().equals(title)) {
                    // Update the review for the specified user and title
                    data[10] = newReview; // Assuming review column is at index 10
                    line = String.join(",", data);
                }
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Delete the original file
        File originalFile = new File(csvFile);
        if (!originalFile.delete()) {
            System.err.println("Error deleting original file.");
            return;
        }

        // Rename the temporary file to the original file name
        File tempFileObj = new File(tempFile);
        if (!tempFileObj.renameTo(originalFile)) {
            System.err.println("Error renaming temporary file.");
        } else {
            System.out.println("Review updated successfully.");
        }
    }

    // Calculate average ratings for all books in the table
    private void calculateAverageRatings() {
        // Load data from personal.csv
        ArrayList<String[]> userRatings = loadUserRatings();

        // Iterate through each row of the table
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            String title = (String) tableModel.getValueAt(row, 0); // Get the title from the table model

            double totalRating = 0.0;
            int count = 0;
            boolean hasRating = false; // Flag to track if any rating exists for the book
            for (String[] rating : userRatings) {
                if (title.equals(rating[0]) && !rating[1].isEmpty()) { // Compare with title from userRatings
                    try {
                        totalRating += Double.parseDouble(rating[1]);
                        count++;
                        hasRating = true;
                    } catch (NumberFormatException e) {
                        // Ignore invalid ratings
                    }
                }
            }
            if (hasRating) {
                double averageRating = totalRating / count;
                // Update the average rating for each book in the table model
                tableModel.setValueAt(String.format("%.2f (%d)", averageRating, count), row, 2);
            } else {
                // If no rating exists, update with "No Rating"
                tableModel.setValueAt("No Rating", row, 2);
            }
        }
    }

    // Load user's ratings from personal.csv
    private ArrayList<String[]> loadUserRatings() {
        ArrayList<String[]> userRatings = new ArrayList<>();
        String csvFile = "personal.csv"; // Path to the CSV file containing user ratings

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(","); // Assuming CSV uses comma as separator
                // Assuming the CSV format is: username,Title,Author,Rating,Reviews,Status,Spend
                // Time (minutes),Start Date,End Date,User Rating,User Review
                // Checking if there are at least 11 columns in the CSV data
                if (data.length >= 11) {
                    String title = data[1].trim(); // Title
                    String rating = data[9].trim(); // User Rating
                    userRatings.add(new String[] { title, rating });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return userRatings;
    }

    // Save data to general CSV
    private void saveDataToGeneralCSV() {
        // Create FileWriter and BufferedWriter to write to CSV file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("General.csv"))) {
            // Write column headers
            writer.write("Title,Author,Rating,Reviews\n");

            // Write data from table model
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                StringBuilder rowData = new StringBuilder();
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    if (col == 2) { // If it's the "Rating" column
                        String title = (String) tableModel.getValueAt(row, 0);
                        String rating = calculateAverageRatingForBook(title); // Get average rating
                        rowData.append(rating);
                        // Update the rating in personal.csv
                        updateRatingInPersonalCSV(title, rating);
                    } else {
                        rowData.append(tableModel.getValueAt(row, col));
                    }
                    if (col < tableModel.getColumnCount() - 1) {
                        rowData.append(",");
                    }
                }
                writer.write(rowData.toString() + "\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Update rating in personal.csv
    private void updateRatingInPersonalCSV(String title, String rating) {
        String csvFile = "personal.csv";

        // Read the contents of personal.csv into an ArrayList
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Update the rating for the specified title
        for (int i = 0; i < lines.size(); i++) {
            String[] data = lines.get(i).split(",");
            if (data.length >= 2 && data[1].equals(title)) { // Assuming the title is in the second column
                data[3] = rating; // Assuming the rating column is at index 3
                lines.set(i, String.join(",", data));
                break;
            }
        }

        // Write the updated contents back to personal.csv
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Filter books based on the search query
    private void filterBooks(String query) {
        if (query.isEmpty()) {
            // Show all books if search query is empty
            showAllBooks();
            return;
        }

        // Filter books based on the search query
        ArrayList<String[]> filteredBooks = new ArrayList<>();
        for (String[] book : allBooks) {
            String title = book[0].toLowerCase();
            String author = book[1].toLowerCase();
            if (title.contains(query) || author.contains(query)) {
                filteredBooks.add(book);
            }
        }

        // Update the table with filtered books
        updateTable(filteredBooks);
    }

    // Show all books in the table
    private void showAllBooks() {
        updateTable(allBooks);
    }

    // Update the table with the provided list of books
    private void updateTable(ArrayList<String[]> books) {
        tableModel.setRowCount(0); // Clear the table
        for (String[] book : books) {
            String title = book[0];
            String author = book[1];
            String rating = calculateAverageRatingForBook(title);
            String reviews = loadReviewsForBook(title);
            tableModel.addRow(new Object[] { title, author, rating, reviews, false });
        }
    }
}