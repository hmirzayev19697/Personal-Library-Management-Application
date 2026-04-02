
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class MyGUI extends JFrame {
    private static final String DATABASE_FILE = "users.csv";
    private Map<String, User> usersMap; // Map to store usernames and User objects

    public MyGUI() {
        setTitle("Registration Form");
        // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        ImageIcon icon = new ImageIcon("Images/left.jpeg");
        setIconImage(icon.getImage());

        // Set background image
        ImageIcon backgroundImage = new ImageIcon("Images/back.jpeg");
        JLabel backgroundLabel = new JLabel(backgroundImage);
        setContentPane(backgroundLabel);
        setLayout(new BorderLayout());

        // Load existing users from CSV database into memory
        usersMap = loadUsersFromCSV();

        // Create a panel for the left image and registration
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false); // Make panel transparent
        add(centerPanel, BorderLayout.CENTER);

        // Left side (Picture)
        ImageIcon leftImageIcon = new ImageIcon("Images/left.jpeg");
        Image scaledImage = leftImageIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        leftImageIcon = new ImageIcon(scaledImage);
        JLabel leftImageLabel = new JLabel(leftImageIcon);

        // Registration Panel
        JPanel registrationPanel = new JPanel(new BorderLayout());
        registrationPanel.setOpaque(false); // Make panel transparent

        // Title label for registration
        JLabel registerTitleLabel = new JLabel("Register", SwingConstants.CENTER);
        registerTitleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        registerTitleLabel.setForeground(Color.WHITE); // Set text color to light white
        registrationPanel.add(registerTitleLabel, BorderLayout.NORTH);
        registrationPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0)); // Add top padding

        // Form Panel for Username and Password
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false); // Make panel transparent

        // Username components
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        usernameLabel.setForeground(Color.WHITE); // Set text color to light white
        JTextField usernameField = new JTextField(20);

        // Password components
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        passwordLabel.setForeground(Color.WHITE); // Set text color to light white
        JPasswordField passwordField = new JPasswordField(20);

        // Set up grid constraints for form components
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10); // Padding

        // Add username components to formPanel
        formPanel.add(usernameLabel, gbc);
        gbc.gridy++;
        formPanel.add(usernameField, gbc);

        // Add password components to formPanel
        gbc.gridy++;
        formPanel.add(passwordLabel, gbc);
        gbc.gridy++;
        formPanel.add(passwordField, gbc);

        // Register and Login Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false); // Make panel transparent
        JButton registerButton = new JButton("Register");
        JButton loginButton = new JButton("Login");

        // Action listener for Register button
        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            // Validate input fields
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check password strength
            if (!isPasswordStrong(password)) {
                JOptionPane.showMessageDialog(this,
                        "Password must contain at least one uppercase letter, one lowercase letter, and be at least 8 characters long.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if username already exists
            if (usersMap.containsKey(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists. Please choose a different username.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                // Add new user to the map and update CSV file
                User newUser = new User(username, password);
                usersMap.put(username, newUser);
                saveUsersToCSV();
                JOptionPane.showMessageDialog(this, "Registration successful. You can now login.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Clear username and password fields after successful registration
                usernameField.setText("");
                passwordField.setText("");
            }
        });

        // Action listener for Login button
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            // Check if both username and password are "admin"
            if (username.equals("admin") && password.equals("admin")) {
                JOptionPane.showMessageDialog(this, "Admin login successful!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                new ShowBooksAdmin(username).setVisible(true); // Open the admin interface
                dispose(); // Close the current login window
                return; // Exit the method to prevent further execution
            }

            // Validate login credentials for regular users
            User user = usersMap.get(username);
            if (user != null && user.getPassword().equals(password)) {
                JOptionPane.showMessageDialog(this, "Login successful!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                new ShowBooks(username).setVisible(true); // Open the regular user interface
                dispose(); // Close the current login window
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password. Please try again or register.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        // Add buttons to buttonPanel
        buttonPanel.add(registerButton);
        buttonPanel.add(loginButton);

        // Add formPanel and buttonPanel to registrationPanel
        registrationPanel.add(formPanel, BorderLayout.CENTER);
        registrationPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add leftImageLabel and registrationPanel to centerPanel
        centerPanel.add(leftImageLabel);
        centerPanel.add(registrationPanel);

        pack();
        setLocationRelativeTo(null); // Center the frame on the screen
    }

    private Map<String, User> loadUsersFromCSV() {
        Map<String, User> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(DATABASE_FILE))) {
            String line;
            // Read CSV file line by line
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    // Store username and password as User object in map
                    User user = new User(parts[0], parts[1]);
                    map.put(parts[0], user);
                }
            }
        } catch (FileNotFoundException e) {
            // Handle file not found error
            JOptionPane.showMessageDialog(this, "User database file not found.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            // Handle other IO errors
            JOptionPane.showMessageDialog(this, "Error reading user database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return map;
    }

    private void saveUsersToCSV() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATABASE_FILE))) {
            // Write usersMap to CSV file
            for (Map.Entry<String, User> entry : usersMap.entrySet()) {
                bw.write(entry.getKey() + "," + entry.getValue().getPassword());
                bw.newLine();
            }
        } catch (IOException e) {
            // Handle IO errors during file writing
            JOptionPane.showMessageDialog(this, "Error saving user data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isPasswordStrong(String password) {
        // Password must contain at least one uppercase letter, one lowercase letter,
        // and be at least 8 characters long
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*");
    }
}
