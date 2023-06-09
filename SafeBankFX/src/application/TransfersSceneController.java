package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import javafx.scene.control.ToggleGroup;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;

import javafx.scene.control.Label;

import javafx.scene.layout.AnchorPane;

import javafx.scene.control.RadioButton;

public class TransfersSceneController extends Controller implements Initializable {
	@FXML
	private Label lblHome;
	@FXML
	private Button btnLogout;
	@FXML
	private RadioButton transferOther;
	@FXML
	private ToggleGroup transferOptions;
	@FXML
	private RadioButton transferSelf;
	@FXML
	private RadioButton deposit;
	@FXML
	private AnchorPane anchorPane;
	@FXML
	private Button btnGoBack;

	@FXML
	public void handleLogoutAction(ActionEvent event) throws IOException {
		// TODO Autogenerated
		isSessionActive = false;
		user = null;
		SwitchSceneController.invokeLayout(event, SceneFiles.LOGIN_SCENE_LAYOUT);
	}
	@FXML
	public void invokeHomeSceneLayout(ActionEvent event) throws IOException {
		// TODO Autogenerated
		SwitchSceneController.invokeLayout(event, SceneFiles.HOME_SCENE_LAYOUT);
	}
	// Event Listener on RadioButton[#transferOther].onAction
	@FXML
	public void handleTransferOption(ActionEvent event) throws IOException {
		// TODO Autogenerated
		if(transferOther.isSelected()) {
			AnchorPane transferOther = (AnchorPane)FXMLLoader.load(getClass().getResource(SceneFiles.TRANSFER_OTHER));
			anchorPane.getChildren().setAll(transferOther.getChildren());
			
		}
		else if(transferSelf.isSelected()) {
			AnchorPane transferOther = (AnchorPane)FXMLLoader.load(getClass().getResource(SceneFiles.TRANSFER_TO_SELF));
			anchorPane.getChildren().setAll(transferOther.getChildren());
			
		}
		else if(deposit.isSelected()) {
			AnchorPane transferOther = (AnchorPane)FXMLLoader.load(getClass().getResource(SceneFiles.DEPOSIT));
			anchorPane.getChildren().setAll(transferOther.getChildren());
		}
	}
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		refreshState();
		try {
			AnchorPane transferOther = (AnchorPane)FXMLLoader.load(getClass().getResource(SceneFiles.TRANSFER_OTHER));
			anchorPane.getChildren().setAll(transferOther.getChildren());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
