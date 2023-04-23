package application;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import dao.SavingsAccountsDAO;
import dao.UsersDAO;
import enums.TransactionCategory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import javafx.scene.control.ComboBox;

import javafx.scene.input.InputMethodEvent;

import javafx.scene.layout.VBox;
import models.SavingsAccount;
import models.User;
import notifications.EmailService;
import transactions.BeneficiaryTransferTransaction;
import transactions.SelfTransferTransaction;
import utils.SavingsAccountUtils;
import utils.TransactionUtils;
import validations.TransactionValidations;

public class TransferToSelfAnchorPaneController extends Controller implements Initializable {
	@FXML
	private ComboBox<String> cbFromAccount;
	@FXML
	private ComboBox<String> cbToAccount;
	@FXML
	private TextField txtAmount;
	@FXML
	private Button btnTransfer;
	@FXML
	private Button btnResetToAccount;
	@FXML
	private Button btnResetFromAccount;

	private Map<String, Long> displayFromAccountNumberMapping;
	private String selectedDisplayFromAccountNumber;
	private SavingsAccount currentSelectedFromAccount;

	private Map<String, Long> displayToAccountNumberMapping;
	private String selectedDisplayToAccountNumber;
	private SavingsAccount currentSelectedToAccount;

	private int incorrectOTPAttempts;
	private int invalidOTPAttemptsCount;

	// Event Listener on ComboBox[#cbFromAccount].onAction
	@FXML
	public void displayFromAccounts(ActionEvent event) {
		// TODO Autogenerated
		selectedDisplayFromAccountNumber = cbFromAccount.getSelectionModel().getSelectedItem();
		if (selectedDisplayFromAccountNumber != null) {
			cbFromAccount.setVisible(true);
			btnResetFromAccount.setVisible(true);
			Long accountNumber = displayFromAccountNumberMapping.get(selectedDisplayFromAccountNumber);
			currentSelectedFromAccount = SavingsAccountsDAO.getSavingsAccountByAccountNumber(accountNumber);
			cbToAccount.setVisible(true);
		}
	}

	// Event Listener on ComboBox[#cbToAccount].onAction
	@FXML
	public void displayToAccounts(ActionEvent event) {
		// TODO Autogenerated
		selectedDisplayToAccountNumber = cbToAccount.getSelectionModel().getSelectedItem();
		if (selectedDisplayToAccountNumber != null) {
			cbToAccount.setVisible(true);
			btnResetToAccount.setVisible(true);
			txtAmount.setText("");
			txtAmount.setVisible(true);
			btnTransfer.setVisible(true);
			Long accountNumber = displayToAccountNumberMapping.get(selectedDisplayToAccountNumber);
			currentSelectedToAccount = SavingsAccountsDAO.getSavingsAccountByAccountNumber(accountNumber);
			cbToAccount.setVisible(true);
		}
	}

	@FXML
	public void clearFromAcountValue(ActionEvent event) {
		cbToAccount.setVisible(false);
		cbFromAccount.getSelectionModel().clearSelection();
		cbFromAccount.setButtonCell(new PromptButtonCell<>(cbFromAccount.getPromptText()));
		btnResetFromAccount.setVisible(false);
		txtAmount.setText("");
		txtAmount.setVisible(false);
		btnTransfer.setVisible(false);
		clearToAcountValue(event);
	}

	@FXML
	public void clearToAcountValue(ActionEvent event) {
		cbToAccount.getSelectionModel().clearSelection();
		cbToAccount.setButtonCell(new PromptButtonCell<>(cbToAccount.getPromptText()));
		btnResetToAccount.setVisible(false);
		txtAmount.setText("");
		txtAmount.setVisible(false);
		btnTransfer.setVisible(false);
	}

	// Event Listener on Button[#btnTransfer].onAction
	@FXML
	public void handleTransfer(ActionEvent event) throws Exception {
		// TODO Autogenerated
		String title = null;
		String headerText = null;
		String contentText = null;

		String amountTextField = txtAmount.getText();
		String amountValidationStatus = TransactionValidations.isAmountValidForTransfer(amountTextField,
				currentSelectedFromAccount);

		if (amountValidationStatus.equals("invalid")) {
			headerText = "Amount is Invalid. Enter a valid numerical value";
			AlertController.showError(title, headerText, contentText);
			return;
		} else if (amountValidationStatus.equals("insufficient_funds")) {
			headerText = "Insufficient funds in your account";
			if (user.getAccounts().size() > 0)
				contentText = "Choose a different account of yours for transfering the funds to beneficiary";
			AlertController.showError(title, headerText, contentText);
			return;
		} else if (amountValidationStatus.equals("exceeded_limit")) {
			headerText = "Amount exceeded the limit. You cannot transfer more than USD 100000";
			AlertController.showError(title, headerText, contentText);
			return;
		} else if (amountValidationStatus.equals("valid")) {

			String toEmail = null;
			String emailSubject = null;
			String emailMessage = null;

			toEmail = user.getEmail();
			emailSubject = "OTP for Online Account Payment Transaction";
			emailMessage = "Your OTP for this transaction : ";

			int generatedOTP = TransactionUtils.generateOTP();
			Date generatedOTPTimestamp = new Date();
			EmailService.sendEmail(toEmail, emailSubject, emailMessage + generatedOTP);
			String OTP = DialogController.getEnteredOTPInputForTransaction(TransactionCategory.ONLINE_PAYMENT);
			boolean isValidOTP = TransactionValidations.isOTPValid(OTP, generatedOTPTimestamp);
			System.out.println("Is OTP Valid : " + isValidOTP);
			if (isValidOTP) {
				int enteredOTP = Integer.parseInt(OTP);
				if (enteredOTP == generatedOTP) {

					double amount = Double.parseDouble(amountTextField);
					Long fromAccountNumber = displayFromAccountNumberMapping.get(selectedDisplayFromAccountNumber);
					currentSelectedFromAccount = SavingsAccountsDAO.getSavingsAccountByAccountNumber(fromAccountNumber);

					Long toAccountNumber = displayToAccountNumberMapping.get(selectedDisplayToAccountNumber);
					currentSelectedToAccount = SavingsAccountsDAO.getSavingsAccountByAccountNumber(toAccountNumber);

					String userId = user.getUserId().toString();
					String senderAccountId = currentSelectedFromAccount.getAccountId().toString();
					String receiverAccountId = currentSelectedToAccount.getAccountId().toString();

					SelfTransferTransaction selfTransferTransaction = new SelfTransferTransaction();
					selfTransferTransaction.setUserId(userId);
					selfTransferTransaction.setSenderAccountId(senderAccountId);
					selfTransferTransaction.setReceiverAccountId(receiverAccountId);
					selfTransferTransaction.setAmount(amount);

					selfTransferTransaction.transferToSelf();

					headerText = "Successfully transferred " + amount + " from your account " + ""
							+ selectedDisplayFromAccountNumber + " to your account " + selectedDisplayToAccountNumber;
					AlertController.showConfirmation(title, headerText, contentText);

					toEmail = user.getEmail();
					emailSubject = "Transfer From your account " + selectedDisplayFromAccountNumber;
					emailMessage = "You have transferred USD " + amount + "\n" + "From your account : "
							+ selectedDisplayFromAccountNumber + "\n" + "To your account : "
							+ selectedDisplayToAccountNumber;

					EmailService.sendEmail(toEmail, emailSubject, emailMessage);

				} else {
					title = "Incorrect OTP";
					headerText = "*** Incorrect OTP. Transaction Declined. " + "Attempts Remaining : "
							+ (2 - incorrectOTPAttempts) + "***";
					incorrectOTPAttempts += 1;
					if (incorrectOTPAttempts == 2) {
						title = "Transaction Blocked";
						headerText = "*** You have reached maximum allowed attempts ***";
						contentText = "For security reasons, you will be signed out";
						if (isSessionActive) {
							isSessionActive = false;
							user = null;
						}
						AlertController.showError(title, headerText, contentText);
						SwitchSceneController.invokeLayout(event, SceneFiles.LOGIN_SCENE_LAYOUT);
					}
				}
			} else {
				title = "Invalid OTP";
				headerText = "*** Invalid OTP. " + "Transaction Declined. Attempts Remaining : "
						+ (2 - invalidOTPAttemptsCount) + " ***";
				invalidOTPAttemptsCount += 1;
				if (invalidOTPAttemptsCount == 2) {
					title = "Transaction Blocked";
					headerText = "*** You have reached maximum allowed attempts ***";
					contentText = "For security reasons, you will be signed out";
					if (isSessionActive) {
						isSessionActive = false;
						user = null;
					}
					AlertController.showError(title, headerText, contentText);
					SwitchSceneController.invokeLayout(event, SceneFiles.LOGIN_SCENE_LAYOUT);
				}
			}
		}
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		refreshState();
		displayFromAccountNumberMapping = new HashMap<>();
		List<String> accountNumbers = user.getAccounts().stream().map(account -> {
			long accountNumber = account.getAccountNumber();
			String displayAccountNumber = SavingsAccountUtils.getLastFourDigitsOf(accountNumber);
			displayFromAccountNumberMapping.put(displayAccountNumber, accountNumber);
			return displayAccountNumber;
		}).collect(Collectors.toList());

		ObservableList<String> fromAccountNumbersList = FXCollections.observableArrayList(accountNumbers);
		cbFromAccount.setItems(fromAccountNumbersList);

		cbFromAccount.setItems(fromAccountNumbersList);

		displayToAccountNumberMapping = new HashMap<>();
		List<String> toAccountNumbers = user.getAccounts().stream().map(account -> {
			long accountNumber = account.getAccountNumber();
			String displayAccountNumber = SavingsAccountUtils.getLastFourDigitsOf(accountNumber);
			displayToAccountNumberMapping.put(displayAccountNumber, accountNumber);
			return displayAccountNumber;
		}).collect(Collectors.toList());

		ObservableList<String> toAccountNumbersList = FXCollections.observableArrayList(toAccountNumbers);
		cbFromAccount.setItems(fromAccountNumbersList);

		cbToAccount.setItems(toAccountNumbersList);

		txtAmount.setText("");
		txtAmount.setVisible(false);
		btnTransfer.setVisible(false);
		btnResetFromAccount.setVisible(false);
		btnResetToAccount.setVisible(false);
		cbToAccount.setVisible(false);
	}
}
