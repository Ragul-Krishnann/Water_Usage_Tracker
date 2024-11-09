    import javafx.application.Application;
    import javafx.beans.property.*;
    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;
    import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
    import javafx.scene.chart.LineChart;
    import javafx.scene.chart.NumberAxis;
    import javafx.scene.chart.XYChart;
    import javafx.scene.control.*;
    import javafx.scene.layout.GridPane;
    import javafx.scene.layout.HBox;
    import javafx.scene.layout.VBox;
    import javafx.stage.Stage;

    import java.sql.*;
    import java.time.LocalDate;

    public class app extends Application {
        private static final String DB_URL = "jdbc:mysql://localhost:3306/community_water_db";
        private static final String DB_USER = "root";
        private static final String DB_PASSWORD = "RAGUL12345";
        private static final double WATER_COST_PER_LITER = 4; // Cost of water per liter

        private Connection connection;
        private TableView<WaterUsageRecord> tableView;
        private int currentUserId = -1; // Store the logged-in user ID

        public static void main(String[] args) {
            launch(args);
        }

        @Override
        public void start(Stage primaryStage) {
            connectToDatabase();
            showLoginScreen(primaryStage);
        }

    @SuppressWarnings("unused")
    private void showLoginScreen(Stage primaryStage) {
        primaryStage.setTitle("Login");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20, 30, 20, 30));
        layout.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #b3b3b3; -fx-border-radius: 5; -fx-border-width: 1;");

        Label titleLabel = new Label("Community Water Tracker Login");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle("-fx-font-size: 14px;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setStyle("-fx-font-size: 14px;");

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        Button signUpButton = new Button("Sign Up");
        signUpButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (authenticateUser(username, password)) {
                showWaterUsageTracker(primaryStage);
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid login credentials");
            }
        });
        signUpButton.setOnAction(e -> showSignUpScreen(primaryStage));

        GridPane fields = new GridPane();
        fields.setHgap(10);
        fields.setVgap(10);
        fields.add(userLabel, 0, 0);
        fields.add(usernameField, 1, 0);
        fields.add(passLabel, 0, 1);
        fields.add(passwordField, 1, 1);

        HBox buttonBox = new HBox(10, loginButton, signUpButton);
        buttonBox.setStyle("-fx-padding: 15 0 0 0;");

        layout.getChildren().addAll(titleLabel, fields, buttonBox);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 350, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    
    @SuppressWarnings("unused")
    private void showSignUpScreen(Stage primaryStage) {
        primaryStage.setTitle("Sign Up");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20, 30, 20, 30));
        layout.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #b3b3b3; -fx-border-radius: 5; -fx-border-width: 1;");

        Label titleLabel = new Label("Create a New Account");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        Label confirmPassLabel = new Label("Confirm Password:");
        confirmPassLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle("-fx-font-size: 14px;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setStyle("-fx-font-size: 14px;");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        confirmPasswordField.setStyle("-fx-font-size: 14px;");

        Button signUpButton = new Button("Sign Up");
        signUpButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        Button backButton = new Button("Back to Login");
        backButton.setStyle("-fx-background-color: #b3b3b3; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        signUpButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Please fill in all fields.");
            } else if (!password.equals(confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Passwords do not match.");
            } else if (createUser(username, password)) {
                showAlert(Alert.AlertType.INFORMATION, "Account created successfully!");
                showLoginScreen(primaryStage);
            } else {
                showAlert(Alert.AlertType.ERROR, "Username already exists. Please choose a different one.");
            }
        });
        backButton.setOnAction(e -> showLoginScreen(primaryStage));

        GridPane fields = new GridPane();
        fields.setHgap(10);
        fields.setVgap(10);
        fields.add(userLabel, 0, 0);
        fields.add(usernameField, 1, 0);
        fields.add(passLabel, 0, 1);
        fields.add(passwordField, 1, 1);
        fields.add(confirmPassLabel, 0, 2);
        fields.add(confirmPasswordField, 1, 2);

        HBox buttonBox = new HBox(10, signUpButton, backButton);
        buttonBox.setStyle("-fx-padding: 15 0 0 0;");

        layout.getChildren().addAll(titleLabel, fields, buttonBox);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 350, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean createUser(String username, String password) {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // If there's an error, it could be due to a duplicate username
            e.printStackTrace();
            return false;
        }
    }

        @SuppressWarnings("unused")
        private boolean createNewUser(String username, String password) {
            String queryCheck = "SELECT id FROM users WHERE username = ?";
            String queryInsert = "INSERT INTO users (username, password) VALUES (?, ?)";
            try (PreparedStatement checkStmt = connection.prepareStatement(queryCheck)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    return false; // Username already exists
                }
            
                // Insert new user if username doesn't exist
                try (PreparedStatement insertStmt = connection.prepareStatement(queryInsert)) {
                    insertStmt.setString(1, username);
                    insertStmt.setString(2, password);
                    insertStmt.executeUpdate();
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }
    
        private boolean authenticateUser(String username, String password) {
            String query = "SELECT id FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    currentUserId = rs.getInt("id");
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        @SuppressWarnings("unused")
        private void showWaterUsageTracker(Stage primaryStage) {
            primaryStage.setTitle("Community Water Usage Tracker");
        
            // Labels and input fields
            Label householdLabel = new Label("Household Name:");
            householdLabel.setStyle("-fx-font-size: 14px;");
            TextField householdField = new TextField();
            householdField.setStyle("-fx-font-size: 14px;");
        
            Label dateLabel = new Label("Date:");
            dateLabel.setStyle("-fx-font-size: 14px;");
            DatePicker datePicker = new DatePicker();
            datePicker.setStyle("-fx-font-size: 14px;");
        
            Label consumptionLabel = new Label("Consumption (liters):");
            consumptionLabel.setStyle("-fx-font-size: 14px;");
            TextField consumptionField = new TextField();
            consumptionField.setStyle("-fx-font-size: 14px;");
        
            // Buttons
            Button addButton = new Button("Add Record");
            addButton.setStyle("-fx-font-size: 14px;");
            Button deleteButton = new Button("Delete Record");
            deleteButton.setStyle("-fx-font-size: 14px;");
            Button viewGraphButton = new Button("Show Graph");
            viewGraphButton.setStyle("-fx-font-size: 14px;");
            Button changeUserButton = new Button("Change User");
            changeUserButton.setStyle("-fx-font-size: 14px;");
        
            // Average and Total Labels
            Label avgConsumptionLabel = new Label("Average Consumption (liters): ");
            avgConsumptionLabel.setStyle("-fx-font-size: 14px;");
            Label avgCostLabel = new Label("Average Cost (Rupees): ");
            avgCostLabel.setStyle("-fx-font-size: 14px;");
            Label totalConsumptionLabel = new Label("Total Consumption (liters): ");
            totalConsumptionLabel.setStyle("-fx-font-size: 14px;");
            Label totalCostLabel = new Label("Total Cost (Rupees): ");
            totalCostLabel.setStyle("-fx-font-size: 14px;");
        
            // Create a GridPane for input fields and buttons
            GridPane grid = new GridPane();
            grid.setPadding(new Insets(15, 20, 15, 20));
            grid.setVgap(10);
            grid.setHgap(10);
        
            // Align the labels and input fields in the grid
            grid.add(householdLabel, 0, 0);
            grid.add(householdField, 1, 0);
            grid.add(dateLabel, 0, 1);
            grid.add(datePicker, 1, 1);
            grid.add(consumptionLabel, 0, 2);
            grid.add(consumptionField, 1, 2);
        
            // Align buttons in the grid
            grid.add(addButton, 0, 3);
            grid.add(deleteButton, 1, 3);
            grid.add(changeUserButton, 2, 3);
        
            // Set button actions
            addButton.setOnAction(e -> addRecord(householdField.getText(), datePicker.getValue(), consumptionField.getText()));
            deleteButton.setOnAction(e -> deleteSelectedRecord());
            viewGraphButton.setOnAction(e -> showUsageGraph());
            changeUserButton.setOnAction(e -> showLoginScreen(primaryStage));
        
            // Setup table view
            tableView = new TableView<>();
            setupTableView();
        
            // VBox for table and labels, with spacing for layout
            VBox tableWithGraph = new VBox(10, tableView, viewGraphButton, avgConsumptionLabel, avgCostLabel, totalConsumptionLabel, totalCostLabel);
            tableWithGraph.setPadding(new Insets(20, 0, 20, 0));
            tableWithGraph.setAlignment(Pos.TOP_LEFT);
        
            // Remove outline and make the table more spacious
            tableView.setStyle("-fx-padding: 10; -fx-border-width: 0; -fx-background-color: transparent;"); // Removes black outline
            tableView.setFixedCellSize(28);  // Adjust cell size for more spacious rows
            tableView.setPrefHeight(28 * 7 + 28);  // 7 rows + header height
        
            // Combine grid and tableWithGraph in a main layout
            VBox mainLayout = new VBox(15, grid, tableWithGraph);
            mainLayout.setPadding(new Insets(20, 20, 20, 20));
            mainLayout.setAlignment(Pos.TOP_LEFT);
        
            Scene scene = new Scene(mainLayout, 750, 650);
            primaryStage.setScene(scene);
            primaryStage.show();
        
            // Load records and update averages/totals
            viewRecords();
            updateAverageAndTotalConsumptionAndCost(avgConsumptionLabel, avgCostLabel, totalConsumptionLabel, totalCostLabel);
        }
        
        private void updateAverageAndTotalConsumptionAndCost(Label avgConsumptionLabel, Label avgCostLabel, Label totalConsumptionLabel, Label totalCostLabel) {
            // Ensure we have records to process
            if (tableView.getItems().isEmpty()) {
                avgConsumptionLabel.setText("Average Consumption (liters): 0");
                avgCostLabel.setText("Average Cost (Rupees): 0");
                totalConsumptionLabel.setText("Total Consumption (liters): 0");
                totalCostLabel.setText("Total Cost (Rupees): 0");
                return;
            }
        
            // Initialize variables to accumulate total consumption and cost
            double totalConsumption = 0;
            double totalCost = 0;
        
            // Iterate over the records in tableView to accumulate totals
            for (WaterUsageRecord record : tableView.getItems()) {
                totalConsumption += record.getConsumptionAmount();
                totalCost += record.getConsumptionAmount() * WATER_COST_PER_LITER;
            }
        
            // Calculate averages
            double avgConsumption = totalConsumption / tableView.getItems().size();
            double avgCost = totalCost / tableView.getItems().size();
        
            // Update labels with calculated values
            avgConsumptionLabel.setText(String.format("Average Consumption (liters): %.2f", avgConsumption));
            avgCostLabel.setText(String.format("Average Cost (Rupees): %.2f", avgCost));
            totalConsumptionLabel.setText(String.format("Total Consumption (liters): %.2f", totalConsumption));
            totalCostLabel.setText(String.format("Total Cost (Rupees): %.2f", totalCost));
        }
        
        
        private void viewRecords() {
            ObservableList<WaterUsageRecord> records = FXCollections.observableArrayList();
            try {
                String query = "SELECT h.household_name, w.date, w.consumption_amount " +
                        "FROM water_usage w " +
                        "JOIN households h ON w.household_id = h.household_id " +
                        "WHERE w.user_id = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, currentUserId);
                ResultSet resultSet = stmt.executeQuery();
        
                while (resultSet.next()) {
                    String householdName = resultSet.getString("household_name");
                    LocalDate date = resultSet.getDate("date").toLocalDate();
                    float consumptionAmount = resultSet.getFloat("consumption_amount");
                    records.add(new WaterUsageRecord(householdName, date, consumptionAmount));
                }
        
                tableView.setItems(records);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        @SuppressWarnings("unchecked")
        private void setupTableView() {
            // Create and configure table columns
            TableColumn<WaterUsageRecord, String> householdColumn = new TableColumn<>("Household Name");
            householdColumn.setCellValueFactory(data -> data.getValue().householdNameProperty());
            householdColumn.setPrefWidth(180); // Adjusted width for spacious layout
            householdColumn.setStyle("-fx-font-size: 14px;");
        
            TableColumn<WaterUsageRecord, LocalDate> dateColumn = new TableColumn<>("Date");
            dateColumn.setCellValueFactory(data -> data.getValue().dateProperty());
            dateColumn.setPrefWidth(120);
            dateColumn.setStyle("-fx-font-size: 14px;");
        
            TableColumn<WaterUsageRecord, Float> consumptionColumn = new TableColumn<>("Consumption (liters)");
            consumptionColumn.setCellValueFactory(data -> data.getValue().consumptionAmountProperty().asObject());
            consumptionColumn.setPrefWidth(170);
            consumptionColumn.setStyle("-fx-font-size: 14px;");
        
            TableColumn<WaterUsageRecord, Double> costColumn = new TableColumn<>("Cost (Rupees)");
            costColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getConsumptionAmount() * WATER_COST_PER_LITER).asObject());
            costColumn.setPrefWidth(140);
            costColumn.setStyle("-fx-font-size: 14px;");
        
            // Add columns to TableView
            tableView.getColumns().addAll(householdColumn, dateColumn, consumptionColumn, costColumn);
        
            // More spacious row and header adjustments
            tableView.setFixedCellSize(28);  // Spacious row height
            tableView.setPrefHeight(28 * 7 + 28);  // 7 rows + header height
        }
        
        private void addRecord(String householdName, LocalDate date, String consumptionAmountStr) {
            if (householdName.isEmpty() || date == null || consumptionAmountStr.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Please fill in all fields.");
                return;
            }

            try {
                float consumptionAmount = Float.parseFloat(consumptionAmountStr);

                int householdId = getOrCreateHousehold(householdName);
            
                String query = "INSERT INTO water_usage (household_id, date, consumption_amount, user_id) VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, householdId);
                preparedStatement.setDate(2, Date.valueOf(date));
                preparedStatement.setFloat(3, consumptionAmount);
                preparedStatement.setInt(4, currentUserId);
                preparedStatement.executeUpdate();
            
                showAlert(Alert.AlertType.INFORMATION, "Record added successfully.");
                viewRecords();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid number for consumption.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    
        private int getOrCreateHousehold(String householdName) {
            String selectQuery = "SELECT household_id FROM households WHERE household_name = ?";
            String insertQuery = "INSERT INTO households (household_name) VALUES (?)";
            try {
                // Try to get the household_id if it exists
                PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
                selectStmt.setString(1, householdName);
                ResultSet rs = selectStmt.executeQuery();
            
                if (rs.next()) {
                    // Household exists, return its ID
                    return rs.getInt("household_id");
                } else {
                    // Household does not exist, so insert it
                    PreparedStatement insertStmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                    insertStmt.setString(1, householdName);
                    insertStmt.executeUpdate();
                
                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Return the new household_id
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return -1; // Return an invalid ID if something goes wrong
        }
    

        private void deleteSelectedRecord() {
            // Get the selected record from the table
            WaterUsageRecord selectedRecord = tableView.getSelectionModel().getSelectedItem();
            if (selectedRecord == null) {
                showAlert(Alert.AlertType.ERROR, "No record selected for deletion.");
                return;
            }
    
            // Confirm deletion
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this record?", ButtonType.YES, ButtonType.NO);
            confirmationAlert.showAndWait();
    
            if (confirmationAlert.getResult() == ButtonType.YES) {
                try {
                    // Delete the record from the database
                    String deleteQuery = "DELETE FROM water_usage WHERE household_id = (SELECT household_id FROM households WHERE household_name = ?) AND date = ? AND user_id = ?";
                    PreparedStatement stmt = connection.prepareStatement(deleteQuery);
                    stmt.setString(1, selectedRecord.getHouseholdName());
                    stmt.setDate(2, Date.valueOf(selectedRecord.getDate()));
                    stmt.setInt(3, currentUserId);
                    int rowsAffected = stmt.executeUpdate();
    
                    if (rowsAffected > 0) {
                        // Remove the record from the table
                        tableView.getItems().remove(selectedRecord);
                        showAlert(Alert.AlertType.INFORMATION, "Record deleted successfully.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Failed to delete the record.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "An error occurred while deleting the record.");
                }
            }
        }
    
        private void showUsageGraph() {
            Stage graphStage = new Stage();
            graphStage.setTitle("Water Usage Cost Graph");
        
            // Change x-axis to a NumberAxis for showing entry count
            NumberAxis xAxis = new NumberAxis();
            xAxis.setLabel("Number of Entries");
        
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Cost (Rupees)");
        
            LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle("Water Usage Cost Over Time");
        
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Cost");
        
            try {
                String query = "SELECT date, consumption_amount FROM water_usage WHERE user_id = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, currentUserId);
                ResultSet resultSet = stmt.executeQuery();
        
                int entryCount = 0; // Counter for number of entries
                while (resultSet.next()) {
                    entryCount++;
                    float consumptionAmount = resultSet.getFloat("consumption_amount");
                    double cost = consumptionAmount * WATER_COST_PER_LITER;
        
                    // Add data point using entry count as the x-value
                    series.getData().add(new XYChart.Data<>(entryCount, cost));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        
            lineChart.getData().add(series);
        
            Scene scene = new Scene(lineChart, 800, 600);
            graphStage.setScene(scene);
            graphStage.show();
        }
        

        private void showAlert(Alert.AlertType alertType, String message) {
            Alert alert = new Alert(alertType, message, ButtonType.OK);
            alert.showAndWait();
        }

        private void connectToDatabase() {
            try {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Connected to the database.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void stop() {
            try {
                if (connection != null) {
                    connection.close();
                    System.out.println("Disconnected from the database.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public class WaterUsageRecord {
            private final SimpleStringProperty householdName;
            private final SimpleObjectProperty<LocalDate> date;
            private final SimpleFloatProperty consumptionAmount;

            public WaterUsageRecord(String householdName, LocalDate date, float consumptionAmount) {
                this.householdName = new SimpleStringProperty(householdName);
                this.date = new SimpleObjectProperty<>(date);
                this.consumptionAmount = new SimpleFloatProperty(consumptionAmount);
            }

            public String getHouseholdName() {
                return householdName.get();
            }

            public LocalDate getDate() {
                return date.get();
            }

            public float getConsumptionAmount() {
                return consumptionAmount.get();
            }

            public SimpleStringProperty householdNameProperty() {
                return householdName;
            }

            public ObjectProperty<LocalDate> dateProperty() {
                return date;
            }

            public FloatProperty consumptionAmountProperty() {
                return consumptionAmount;
            }
        }
    }