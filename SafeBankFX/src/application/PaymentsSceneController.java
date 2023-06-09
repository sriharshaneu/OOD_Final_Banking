package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;

import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import utils.CreditCardUtils;

public class PaymentsSceneController extends Controller implements Initializable {
	@FXML
	private Label lblHome;
	@FXML
	private Button btnLogout;
	@FXML
	private Label lblCurrentUserEmail;
	@FXML
	private Button btnGoBack;
	@FXML
	private AnchorPane anchorPane;
	@FXML
	private RadioButton radioBtnPayCCBill;
	@FXML
	private RadioButton radioBtnPayByCredit;
	@FXML
	private RadioButton radioBtnPayBySavings;
	@FXML
	private ToggleGroup PaymentOption;
	
//	layoutX="242.0" layoutY="242.0" prefHeight="456.0" prefWidth="1085.0"
	

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
	@FXML
	public void renderAnchorPane(ActionEvent event) throws IOException {
		
		if(radioBtnPayCCBill.isSelected()) {
			AnchorPane root = (AnchorPane)FXMLLoader.load(getClass().getResource(SceneFiles.CC_BILL_PAY));
			anchorPane.getChildren().setAll(root.getChildren());        
		}
		else if(radioBtnPayByCredit.isSelected()) {
			AnchorPane payByCredit = (AnchorPane)FXMLLoader.load(getClass().getResource(SceneFiles.PAY_BY_CREDIT));
			anchorPane.getChildren().setAll(payByCredit.getChildren());
		}
		else if(radioBtnPayBySavings.isSelected()) {
			AnchorPane payBySavings = (AnchorPane)FXMLLoader.load(getClass().getResource(SceneFiles.PAY_BY_SAVINGS));
			anchorPane.getChildren().setAll(payBySavings.getChildren());
		}
	}
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		refreshState();
		// TODO Auto-generated method stub
		try {
			AnchorPane ccBillPay = null;
			AnchorPane payBySavings = null;
			AnchorPane payByCredit = null;
			
			if(user.getCreditCard().getCreditCardId() != null) {
				radioBtnPayByCredit.setVisible(true);
				radioBtnPayBySavings.setVisible(true);
				double payableAmount = CreditCardUtils.getPayableAmount(user.getCreditCard());
				System.out.println("Payable Amount :: " + payableAmount);
				if(payableAmount > 0) {
					radioBtnPayCCBill.setVisible(true);
					radioBtnPayCCBill.setSelected(true);
					ccBillPay = (AnchorPane)FXMLLoader.load(getClass().getResource(SceneFiles.CC_BILL_PAY));
					anchorPane.getChildren().setAll(ccBillPay.getChildren());
				}
				else {
					radioBtnPayCCBill.setVisible(false);
					radioBtnPayByCredit.setSelected(true);
					payByCredit = (AnchorPane)FXMLLoader.load(getClass().getResource(SceneFiles.PAY_BY_CREDIT));
					anchorPane.getChildren().setAll(payByCredit.getChildren());
				}
			}
			else {
				radioBtnPayByCredit.setVisible(false);
				radioBtnPayCCBill.setVisible(false);
				radioBtnPayBySavings.setVisible(false);
				payBySavings = (AnchorPane)FXMLLoader.load(getClass().getResource(SceneFiles.PAY_BY_SAVINGS));
				anchorPane.getChildren().setAll(payBySavings.getChildren());
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
