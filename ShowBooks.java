import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class ShowBooks extends JFrame {

    private DefaultTableModel tableModel;
    private JTextField searchField;
    private ArrayList<String[]> allBooks;
    private ArrayList<String[]> selectedBooks = new ArrayList<>();

    // Constructor
    public ShowBooks(String username) {
        createCsvCopy();  // Create a copy of the CSV file at initialization
        setTitle("Explore Books");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Header panel with BorderLayout
        JPanel headerPanel = new JPanel(new BorderLayout());

        // Header label
        JLabel headerLabel = new JLabel("Books Library");
        headerLabel.setFont(new Font("Monospaced", Font.PLAIN, 33));
        headerLabel.setForeground(Color.BLACK);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        // Back button panel with FlowLayout
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Back button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            MyGUI loginPage = new MyGUI();
            loginPage.setVisible(true);
          dispose();
          dispose();
        });
        backButton.setBackground(Color.WHITE);
        backButtonPanel.add(backButton);

        // Add header panel and back button panel to NORTH of the frame
        headerPanel.add(backButtonPanel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Search panel with FlowLayout
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Search field
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

        // Add search panel to NORTH of the frame
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

        // Read data from CSV and populate the table
        readDataFromCSV();

        // Table setup
        JTable bookTable = new JTable(tableModel);
        bookTable.setRowHeight(30);

        // Customize column header appearance
        JTableHeader tableHeader = bookTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 14));
        tableHeader.setForeground(Color.BLACK);

        // Center-align "Rating" and "Reviews" columns
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
                                    ShowBooks.this,
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
       
        // Create a panel for the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Add "Add to Library" button
        JButton addToLibraryButton = new JButton("Add to Library");
        addToLibraryButton.addActionListener(e -> {
            addToLibrary(username);
            dispose();
        });
        buttonPanel.add(addToLibraryButton);

 
        // Add button to open personal database
        JButton personalDatabaseButton = new JButton("Personal Database");
        personalDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the current frame
                // Open AddToLibraryFrame without adding a new book
                AddToLibraryFrame addToLibraryFrame = new AddToLibraryFrame(new ArrayList<>(), loadUserBookHistory(username), username);
                addToLibraryFrame.setVisible(true);
            }
        });
        buttonPanel.add(personalDatabaseButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        FilterAndSorting.SortSelected(bookTable);
        setVisible(true);
    //    dispose();

        // Calculate average ratings after the frame is visible
        calculateAverageRatings();

        // Save data to general CSV
        saveDataToGeneralCSV();
    }

    private void createCsvCopy() {
        Path sourcePath = Paths.get("brodsky (1).csv");
        Path targetPath = Paths.get("brodsky_copy.csv");

        try {
            if (!Files.exists(targetPath)) {  // Check if the file doesn't exist
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to create a copy of the CSV file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void readDataFromCSV() {
        allBooks = new ArrayList<>();
        String csvFile = "brodsky_copy.csv";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "UTF-8"))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                // Split by comma outside of quotes, including brackets as part of titles
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length >= 1) {
                    // Trim whitespace from titles and author
                    String title = data[0].trim();
                    String author = "";
    
                    // If title is enclosed in double quotes, remove the quotes
                    if (title.startsWith("\"") && title.endsWith("\"")) {
                        title = title.substring(1, title.length() - 1);
                    }
    
                    // Remove extra characters like "п»i"
                    title = title.replaceAll("[^\\p{Print}]", "");
    
                    // If title is empty, set it to "Unknown"
                    if (title.isEmpty()) {
                        title = "Unknown";
                    }
    
                    // If there's an author provided
                    if (data.length > 1) {
                        author = data[1].trim();
                    } else {
                        // If no author provided, set it to "Unknown"
                        author = "Unknown";
                    }
    
                    // Split titles if there are multiple
                    String[] titles = title.split(",\\s*");
    
                    // Split authors if there are multiple
                    String[] authors = author.split(",\\s*");
    
                    // Add each book to the list
                    for (String bookTitle : titles) {
                        for (String authorName : authors) {
                            allBooks.add(new String[]{bookTitle.trim(), authorName.trim()});
                            String averageRating = calculateAverageRatingForBook(bookTitle);
                            String rating = averageRating.equals("No Rating") ? "No Rating" : averageRating;
                            // Load reviews for the current book
                            String reviews = loadReviewsForBook(bookTitle);
                            tableModel.addRow(new Object[]{bookTitle, authorName, rating, reviews, false});
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
        String csvFile = "personal.csv"; // Path to the CSV file containing user ratings

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
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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

    // Method to display user's details in a separate window
    private void showUserDetails(String title, String author, String averageRating,
        String username, String userRating, String userReview) {
        JFrame userDetailsFrame = new JFrame("User Details");
        userDetailsFrame.setSize(400, 200);
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

        userDetailsPanel.add(titleLabel);
        userDetailsPanel.add(authorLabel);
        userDetailsPanel.add(ratingLabel);
        userDetailsPanel.add(Box.createVerticalStrut(10)); // Add spacing between book info and separator
        userDetailsPanel.add(separator); // Add separator line
        userDetailsPanel.add(Box.createVerticalStrut(10)); // Add spacing between separator and user info
        userDetailsPanel.add(userLabel);
        userDetailsPanel.add(userRatingLabel);
        userDetailsPanel.add(userReviewLabel);

        userDetailsFrame.add(userDetailsPanel);
        userDetailsFrame.setVisible(true);
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
                tableModel.setValueAt(String.format("%.2f (%d)", averageRating,count), row, 2);
                tableModel.setValueAt(String.format("%.2f (%d)", averageRating,count), row, 2);
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
                // Assuming the CSV format is: username,Title,Author,Rating,Reviews,Status,Spend Time (minutes),Start Date,End Date,User Rating,User Review
                // Checking if there are at least 11 columns in the CSV data
                if (data.length >= 11) {
                    String title = data[1].trim(); // Title
                    String rating = data[9].trim(); // User Rating
                    userRatings.add(new String[]{title, rating});
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

    // Add selected books to the library
    private void addToLibrary(String username) {
        selectedBooks.clear(); // Clear the list before adding new selections

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            boolean isSelected = (boolean) tableModel.getValueAt(row, 4);
            if (isSelected) {
                String[] selectedBook = new String[5];
                selectedBook[0] = (String) tableModel.getValueAt(row, 0); // Title
                selectedBook[1] = (String) tableModel.getValueAt(row, 1); // Author
                selectedBook[2] = (String) tableModel.getValueAt(row, 2); // Rating
                selectedBook[3] = (String) tableModel.getValueAt(row, 3); // Reviews
                selectedBook[4] = ""; // Placeholder for other fields

                selectedBooks.add(selectedBook);
            }
        }

        if (!selectedBooks.isEmpty()) {
            // Load user's book history here
            ArrayList<String[]> userBookHistory = loadUserBookHistory(username);

            // Check if each selected book already exists in the user's book history
            ArrayList<String[]> booksToAdd = new ArrayList<>();
            boolean anyAlreadyExist = false;
            for (String[] selectedBook : selectedBooks) {
                boolean alreadyExists = false;
                for (String[] history : userBookHistory) {
                    if (selectedBook[0].equals(history[0]) && selectedBook[1].equals(history[1])) {
                        alreadyExists = true;
                        anyAlreadyExist = true;
                        break;
                    }
                }
                if (!alreadyExists) {
                    booksToAdd.add(selectedBook);
                }
            }

            if (!booksToAdd.isEmpty()) {
                new AddToLibraryFrame(booksToAdd, userBookHistory, username);
            } else {
                JOptionPane.showMessageDialog(this, "Selected books already exist in your library.", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
                if (!anyAlreadyExist) {
                    // If none of the selected books exist in the library, clear the selection for the user to choose again
                    for (int row = 0; row < tableModel.getRowCount(); row++) {
                        tableModel.setValueAt(false, row, 4);
                    }
                }
            }
        }
    }

    // Load user's book history
    private ArrayList<String[]> loadUserBookHistory(String username) {
        ArrayList<String[]> userBookHistory = new ArrayList<>();
        String csvFile = "personal.csv"; // Path to the CSV file containing user book history

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(","); // Assuming CSV uses comma as separator
                // Check if the line has at least 2 columns and the first column matches the username
                if (data.length >= 2 && data[0].equals(username)) {
                    // Exclude the first column (username) and add the remaining data to the userBookHistory list
                    String[] userData = new String[data.length - 1];
                    System.arraycopy(data, 1, userData, 0, data.length - 1);
                    userBookHistory.add(userData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading user's book history from CSV file.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return userBookHistory;
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
            tableModel.addRow(new Object[]{title, author, rating, reviews, false});
        }
    }
}