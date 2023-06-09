package application;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import dao.SavingsAccountsDAO;
import enums.PaymentStatus;
import enums.TransactionCategory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import javafx.scene.control.ComboBox;

import javafx.scene.layout.VBox;
import models.SavingsAccount;
import transactions.DepositToAccountTransaction;
import transactions.PaymentFromAccountTransaction;
import utils.SavingsAccountUtils;
import validations.TransactionValidations;

public class DepositAnchorPaneController extends Controller implements Initializable {
	@FXML
	private VBox vBoxDeposit;
	@FXML
	private ComboBox<String> cbAccounts;
	@FXML
	private TextField txtAmount;
	@FXML
	private Button btnDeposit;
	
	private Map<String, Long> displayAccountNumbersMapping;
	private String selectedDisplayAccountNumber;
	private SavingsAccount currentSelectedAccount;
	
	private int incorrectOTPAttempts;
	private int invalidOTPAttemptsCount;
	private int insufficientFundsAttemptsCount;
 
	// Event Listener on ComboBox[#cbAccounts].onAction
	@FXML
	public void displayAccounts(ActionEvent event) {
	// TODO Autogenerated
		String selectedAccountNumber = cbAccounts.getSelectionModel().getSelectedItem();
		if (selectedAccountNumber != null) {
			selectedDisplayAccountNumber = selectedAccountNumber;
			txtAmount.setVisible(true);
			txtAmount.setText("");
			btnDeposit.setVisible(true);
			
			Long accountNumber = displayAccountNumbersMapping.get(selectedAccountNumber);
			currentSelectedAccount = SavingsAccountsDAO.getSavingsAccountByAccountNumber(accountNumber);
		}
		
	}
	// Event Listener on TextField[#txtAmount].onInputMethodTextChanged
	
	// Event Listener on Button[#btnDeposit].onAction
	@FXML
	public void handleDeposit(ActionEvent event) throws Exception {
		// TODO Autogenerated
		String title = null;
		String headerText = null;
		String contentText = null;
		
		String amountTextFieldValue = txtAmount.getText();
		boolean isAmountValid = TransactionValidations.isAmountValidForOnlinePayment(amountTextFieldValue);
		if (isAmountValid) {

			if (currentSelectedAccount == null) {
				headerText = "Account is Invalid. Select an Account";
				AlertController.showError(title, headerText, contentText);
				return;
			} else {
				String userId = user.getUserId().toString();
				String accountId = currentSelectedAccount.getAccountId().toString();
				double accountBalance = currentSelectedAccount.getAccountBalance();
				double amount = Double.parseDouble(amountTextFieldValue);

				DepositToAccountTransaction depositToAccountTransaction = 
						new DepositToAccountTransaction();

				depositToAccountTransaction.setUserId(userId);
				depositToAccountTransaction.setAccountId(accountId);
				depositToAccountTransaction.setAccountBalance(accountBalance);
				depositToAccountTransaction.setAmount(amount);
				depositToAccountTransaction.setTransactionCategory(TransactionCategory.CASH_DEPOSIT);

				PaymentStatus paymentStatus = 
						depositToAccountTransaction.deposit.deposit(amount);

				if (paymentStatus == PaymentStatus.SUCCESS) {
					title = "Transaction Successful";
					headerText = "*** Deposited USD " + amount + " to account number ending "
							+ selectedDisplayAccountNumber + " ***";
					AlertController.showSuccess(title, headerText, contentText);
					SwitchSceneController.invokeLayout(event, SceneFiles.TRANSACTIONS_SCENE_LAYOUT);
					return;
				} else {
					if (insufficientFundsAttemptsCount == 2 || invalidOTPAttemptsCount == 2
							|| incorrectOTPAttempts == 2) {

						title = "Transaction Blocked";
						headerText = "*** You have reached maximum allowed attempts ***";
						contentText = "For security reasons, you will be signed out";
						if(isSessionActive) {
							isSessionActive = false;
							user = null;
						}
						AlertController.showError(title, headerText, contentText);
						SwitchSceneController.invokeLayout(event, SceneFiles.LOGIN_SCENE_LAYOUT);
					} else {
						if (paymentStatus == PaymentStatus.INSUFFICIENT_FUNDS) {
							title = "Insufficient Funds";
							headerText = "*** Insufficient Funds ***";
							contentText = "Try again with different ammount or account." + " Attempts Remaining : "
									+ (2 - insufficientFundsAttemptsCount) + " ***";
							insufficientFundsAttemptsCount += 1;
						} else if (paymentStatus == PaymentStatus.INCORRECT_OTP) {
							title = "Incorrect OTP";
							headerText = "*** Incorrect OTP. Transaction Declined. " + "Attempts Remaining : "
									+ (2 - incorrectOTPAttempts) + "***";
							incorrectOTPAttempts += 1;
						} else if (paymentStatus == PaymentStatus.INVALID_OTP) {
							title = "Invalid OTP";
							headerText = "*** Invalid OTP. " + "Transaction Declined. Attempts Remaining : "
									+ (2 - invalidOTPAttemptsCount) + " ***";
							invalidOTPAttemptsCount += 1;
						} else if (paymentStatus == PaymentStatus.PAYMENT_EXCEPTION) {
							title = "Transaction Exception";
							headerText = "*** Transaction Failed .Try Again Later ***";
						}
						AlertController.showSuccess(title, headerText, contentText);
						return;
					}
				}
			}
		} else {
			title = "Invalid Amount";
			headerText = "Amount is invalid for transaction";
			contentText = "Enter a valid numerical value";
			AlertController.showError(title, headerText, contentText);
			return;
		}
	}
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		refreshState();
		displayAccountNumbersMapping = new HashMap<>();
		List<String> accountNumbers = user.getAccounts().stream().map(account -> {
			long accountNumber = account.getAccountNumber();
			String displayAccountNumber = SavingsAccountUtils.getLastFourDigitsOf(accountNumber);
			displayAccountNumbersMapping.put(displayAccountNumber, accountNumber);
			return displayAccountNumber;
		}).collect(Collectors.toList());

		ObservableList<String> accountNumbersList = FXCollections.observableArrayList(accountNumbers);
		cbAccounts.setItems(accountNumbersList);
		
		txtAmount.setText("");
		txtAmount.setVisible(false);
		btnDeposit.setVisible(false);
		
	}
}
