package application;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import dao.DatabaseConnectionFactory;
import dao.UsersDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import models.User;
import utils.UserUtils;
import validations.UserValidations;

public class LoginSceneController extends Controller implements Initializable {
	@FXML
	private Label lblBankName;
	@FXML
	private TextField txtEmailLogin;
	@FXML
	private PasswordField txtPasswordLogin;
	@FXML
	private Button btnLogin;
	@FXML
	private Label lblSwitchToRegister;
	@FXML
	private Hyperlink hyperlinkToRegister;
	@FXML
	private Label lblForgotPassword;
	@FXML
	private Label lblForgotEmail;
	@FXML
	private Hyperlink hyperlinkForgotPassword;
	@FXML
	private Hyperlink hyperlinkForgotEmail;
	
	private static final Properties properties;
	private static final String APPLICATION_EMAIL;
	private static final String APPLICATION_PASSWORD;
	
	static {
		properties = new Properties();

		try {
			properties.load(new FileInputStream("env.properties"));
		} catch (FileNotFoundException fileNotFoundException) {
			// TODO Auto-generated catch block
			Logger.getLogger(DatabaseConnectionFactory.class.getName()).log(Level.SEVERE, null, fileNotFoundException);

		} catch (IOException ioException) {
			// TODO Auto-generated catch block
			Logger.getLogger(DatabaseConnectionFactory.class.getName()).log(Level.SEVERE, null, ioException);
		}

		APPLICATION_EMAIL = properties.getProperty("APPLICATION_EMAIL");
		APPLICATION_PASSWORD = properties.getProperty("APPLICATION_PASSWORD");
	}

	// Event Listener on Button[#btnLogin].onAction
	@FXML
	public void handleLoginAction(ActionEvent event) throws IOException {

		String title = null;
		String headerText = null;
		String contentText = null;

		// TODO Autogenerated
		String email = APPLICATION_EMAIL;
		String password = APPLICATION_PASSWORD;
		
//		String email = txtEmailLogin.getText();
//		String password = txtPasswordLogin.getText();

		boolean isEmailValid = UserValidations.isEmailValid(email);
		boolean isPasswordValid = UserValidations.isPasswordValid(password);

		if (!isEmailValid || !isPasswordValid) {
			if (!isEmailValid) {
				headerText = "Invalid Email";
				AlertController.showError(title, headerText, contentText);
				return;
			}

			if (!isPasswordValid) {
				headerText = "Invalid Password";
				AlertController.showError(title, headerText, contentText);
				return;
			}
		} else {
			// check whether user exists
			boolean userExists = UsersDAO.userExistsByEmail(email);
			if (!userExists) {
				title = "Login";
				headerText = "User does not exist with given email";
				AlertController.showError(title, headerText, contentText);
				return;
			} else {
				boolean isValidEmailPassword = UserUtils.isEmailPasswordValid(email, password);
				if (isValidEmailPassword) {
					User currentUser = UsersDAO.getUserByEmail(email);
					Controller.isSessionActive = true;
					Controller.user = currentUser;
					SwitchSceneController.invokeLayout(event, SceneFiles.HOME_SCENE_LAYOUT);
				} else {
					title = "Login";
					headerText = "Email and Password Does Not Match";
					AlertController.showError(title, headerText, contentText);
					return;
				}
			}
		}
	}

	// Event Listener on Hyperlink[#hyperlinkToRegister].onAction
	@FXML
	public void handleSwitchToRegisterScene(ActionEvent event) throws IOException {
		// TODO Autogenerated
		SwitchSceneController.invokeLayout(event, SceneFiles.REGISTER_SCENE_LAYOUT);
	}

	@FXML
	public void handleForgotPasswordAction(ActionEvent event) throws IOException {
		// TODO Autogenerated
		SwitchSceneController.invokeLayout(event, SceneFiles.FORGOT_PASSWORD_SCENE);
	}

	@FXML
	public void handleForgotEmailAction(ActionEvent event) throws IOException {
		// TODO Autogenerated
		DialogController.displayForgottenEmail();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		
	}
}
