package application;

import javafx.fxml.FXML;

import javafx.scene.control.Button;

import java.io.IOException;

import javafx.event.ActionEvent;

import javafx.scene.control.Label;

public class AnalyticsSceneController {
	@FXML
	private Label lblHome;
	@FXML
	private Button btnLogout;
	@FXML
	private Label lblCurrentUserEmail;
	@FXML
	private Button btnGoBack;

	// Event Listener on Button[#btnLogout].onAction
	@FXML
	public void handleLogoutAction(ActionEvent event) throws IOException {
		// TODO Autogenerated
		SwitchSceneController.invokeLayout(event, SceneFiles.LOGIN_SCENE_LAYOUT);
	}
	@FXML
	public void invokeHomeSceneLayout(ActionEvent event) throws IOException {
		// TODO Autogenerated
		SwitchSceneController.invokeLayout(event, SceneFiles.HOME_SCENE_LAYOUT);
	}
}