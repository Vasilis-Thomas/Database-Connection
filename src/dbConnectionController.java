import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class dbConnectionController {

    @FXML
    private TextField dbHostField, dbPortField, dbNameField;

    @FXML
    private TextField dbUsernameField;

    @FXML
    private PasswordField dbPasswordField;

    @FXML
    private TextField sshHostField;

    @FXML
    private TextField sshPortField;

    @FXML
    private TextField sshUsernameField;

    @FXML
    private PasswordField sshPasswordField;

    @FXML
    private CheckBox useSshCheckBox;

    @FXML
    private Button connectButton;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab sshTab, dbConnectTab;

    @FXML
    private void initialize() {
        // You can initialize components or perform setup here
        // For example, disable SSH components initially
        disableSshComponents(true);

//        // Add an event listener to the text property of the TextField
//        sshPortField.textProperty().addListener((observable, oldValue, newValue) -> {
//            // Validate if the entered text is a valid integer
//            if (!isValidPortNumber(newValue)) {
//                // If not valid, revert to the old value
//                sshPortField.setText(oldValue);
//            }
//        });
//
//        dbPortField.textProperty().addListener((observable, oldValue, newValue) -> {
//            // Validate if the entered text is a valid integer
//            if (!isValidPortNumber(newValue)) {
//                // If not valid, revert to the old value
//                dbPortField.setText(oldValue);
//            }
//        });

    }


    // Show an alert for invalid input
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleUseSshCheckBox() {
        // Enable or disable SSH components based on checkbox state
        boolean isSshSelected = useSshCheckBox.isSelected();
        disableSshComponents(!isSshSelected);
    }

    @FXML
    private void handleConnectButton() {

        int localPort = 5432; // Local port to use for the tunnel

        if (checkTextFields(dbHostField, dbPortField, dbUsernameField, dbPasswordField, dbNameField)) return;
        if(!isValidPortNumber(dbPortField))return;

        // Implement database connection logic here
        String dbHost = dbHostField.getText();
        int dbPort = Integer.parseInt(dbPortField.getText());
        String dbUsername = dbUsernameField.getText();
        String dbPassword = dbPasswordField.getText();
        String dbName = dbNameField.getText();

        // Perform the actual connection and other actions
        // For example, you can print the values to the console
        System.out.println("DB URL: " + dbHost);
        System.out.println("DB Port: " + dbPort);
        System.out.println("DB Username: " + dbUsername);
        System.out.println("DB Password: " + dbPassword);
        System.out.println("DB Name: " + dbName);

        String     driverClassName = "org.postgresql.Driver" ;
        Connection dbConnection = null;

        // Implement SSH connection logic if needed
        if (useSshCheckBox.isSelected()) {

            if (checkTextFields(sshHostField, sshUsernameField, sshPasswordField, sshPortField, dbHostField, dbPortField, dbUsernameField, dbPasswordField, dbNameField)) return;
            if(!isValidPortNumber(sshPortField))return;

            String sshHost = sshHostField.getText();
            String sshUsername = sshUsernameField.getText();
            String sshPassword = sshPasswordField.getText();
            int sshPort = Integer.parseInt(sshPortField.getText());

            // Implement SSH connection logic here
            System.out.println("SSH Host: " + sshHost);
            System.out.println("SSH Port: " + sshPort);
            System.out.println("SSH Username: " + sshUsername);
            System.out.println("SSH Password: " + sshPassword);

            Session session = null;

            try {
                // Create SSH session
                JSch jsch = new JSch();
                session = jsch.getSession(sshUsername, sshHost, sshPort);
                session.setPassword(sshPassword);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();

                // Set up port forwarding
                session.setPortForwardingL(localPort, dbHost, dbPort);

                System.out.println("SSH tunnel established on port " + localPort);

                //Database Connection via ssh...

                // Connect to the PostgreSQL database
                String jdbcUrl = "jdbc:postgresql://localhost:"+localPort+"/"+dbName;
                System.out.println("Url: " + jdbcUrl);

                try{
                    Class.forName (driverClassName);
                    dbConnection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);

                    // Use the database connection...
                    System.out.println("Connected to PostgreSQL database");
                    dbConnection.close();
                    System.out.println("Database Disconnected");
                } catch (SQLException ex) {
                    //ex.printStackTrace();
                    System.out.println("\n -- SQL Exception --- \n");
                    while(ex != null) {
                        System.out.println("Message: " + ex.getMessage());
                        System.out.println("SQLState: " + ex.getSQLState());
                        System.out.println("ErrorCode: " + ex.getErrorCode());
                        ex = ex.getNextException();
                        System.out.println("");
                    }
                }

            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("\n -- Exception --- \n");
                    System.out.println("Message: " + e.getMessage());
                    System.out.println("");
            } finally {
                // Close the SSH session when done
                if (session != null && session.isConnected()) {
                    System.out.println("Closing SSH session.");
                    session.disconnect();
                }
            }
        } else{
            // Connect to the PostgreSQL database
            String jdbcUrl = "jdbc:postgresql://"+dbHost+":"+dbPort+"/"+dbName;
            System.out.println("Url: " + jdbcUrl);

            try{
                Class.forName (driverClassName);
                dbConnection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);

                // Use the database connection...
                System.out.println("Connected to PostgreSQL database");
                dbConnection.close();
                System.out.println("Database Disconnected");
            } catch (SQLException ex) {
                //e.printStackTrace();
                System.out.println("\n -- SQL Exception --- \n");
                while(ex != null) {
                    System.out.println("Message: " + ex.getMessage());
                    System.out.println("SQLState: " + ex.getSQLState());
                    System.out.println("ErrorCode: " + ex.getErrorCode());
                    ex = ex.getNextException();
                    System.out.println("");
                }
            } catch (Exception e){
                //e.printStackTrace();
                System.out.println("\n -- Exception --- \n");
                while(e != null) {
                    System.out.println("Message: " + e.getMessage());
                    System.out.println("");
                }
            }
        }
    }

    private boolean checkTextFields(TextField... textFields) {
        boolean isEmpty = false;
        for (TextField textField : textFields) {
            if (textField.getText().isEmpty()) {
                isEmpty = true;
                //System.out.println("TextField is empty: " + textField.getId());
                // You can add your logic here for handling empty text fields
            }
        }
        if (isEmpty){
            showAlert("Empty Fields", "Please fill in all the fields");
            for (TextField textField : textFields) {
                textField.setText("");
            }
        }
        return isEmpty;
    }
    private boolean isValidPortNumber(TextField textField) {
        try {
            int port = Integer.parseInt(textField.getText());
            return port >= 0 && port <= 65535;
        } catch (NumberFormatException e) {
            // If not a valid integer, show an alert or handle it accordingly
            showAlert("Invalid Port Number", "Please enter a valid integer between 0 and 65535.");
            return false;
        }

    }

    private void disableSshComponents(boolean disable) {
        // Disable or enable SSH components
        sshHostField.setDisable(disable);
        sshPortField.setDisable(disable);
        sshUsernameField.setDisable(disable);
        sshPasswordField.setDisable(disable);
    }


}
