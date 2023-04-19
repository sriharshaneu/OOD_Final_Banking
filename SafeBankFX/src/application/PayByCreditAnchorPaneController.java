package application;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import dao.CreditCardsDAO;
import dao.SavingsAccountsDAO;
import enums.PaymentStatus;
import enums.TransactionCategory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import javafx.scene.control.ComboBox;

import javafx.scene.input.InputMethodEvent;
import models.CreditCard;
import models.SavingsAccount;
import transactions.PaymentFromAccountTransaction;
import transactions.PaymentFromCardTransaction;
import utils.SavingsAccountUtils;
import validations.TransactionValidations;

public class PayByCreditAnchorPaneController extends Controller implements Initializable {
	@FXML
	private ComboBox<String> cbCards;
	@FXML
	private TextField txtAmount;
	@FXML
	private Button btnPayByCredit;
	@FXML
	private Button btnReset;
	// Event Listener on ComboBox[#cbCards].onAction
	
	private Map<String, Long> displayCardNumbersMapping;
	private String selectedDisplayCardNumber;
	private CreditCard currentSelectedCard;
	
	private int incorrectOTPAttempts;
	private int invalidOTPAttemptsCount;
	private int insufficientFundsAttemptsCount;
	
	@FXML
	public void displayCards(ActionEvent event) {
		// TODO Autogenerated
		String selectedCardNumber = cbCards.getSelectionModel().getSelectedItem();
		if(selectedCardNumber != null) {
			btnReset.setVisible(true);
			txtAmount.setVisible(true);
			txtAmount.setText("");
			btnPayByCredit.setVisible(true);
			
			Long cardNumber = displayCardNumbersMapping.get(selectedCardNumber);
			currentSelectedCard = CreditCardsDAO.getCreditCardByCardNumber(cardNumber);
		}
	}
	
	// Event Listener on Button[#btnPayByCredit].onAction
	@FXML
	public void handlePayByCredit(ActionEvent event) throws Exception {
		// TODO Autogenerated
		String title = null;
		String headerText = null;
		String contentText = null;
		
		String amountTextFieldValue = txtAmount.getText();
		boolean isAmountValid = TransactionValidations
				.isAmountValidForOnlinePayment(amountTextFieldValue);
		if (isAmountValid) {

			if (currentSelectedCard == null) {
				headerText = "Account is Invalid. Select an Account";
				AlertController.showError(title, headerText, contentText);
				return;
			} else {
				String userId = user.getUserId().toString();
				String cardId = currentSelectedCard.getCreditCardId().toString();
				double remainingCreditLimit = currentSelectedCard.getRemainingCreditLimit();
				double amount = Double.parseDouble(amountTextFieldValue);


				PaymentFromCardTransaction paymentFromCardTransaction = 
						new PaymentFromCardTransaction();

				paymentFromCardTransaction.setUserId(userId);
				paymentFromCardTransaction.setCardId(cardId);
				paymentFromCardTransaction.setRemainingCreditLimit(remainingCreditLimit);
				paymentFromCardTransaction.setAmount(amount);
				paymentFromCardTransaction.setTransactionCategory(TransactionCategory.ONLINE_PAYMENT);

				PaymentStatus paymentStatus = 
						paymentFromCardTransaction.cardPayment.payment(amount);

				if (paymentStatus == PaymentStatus.SUCCESS) {
					title = "Transaction Successful";
					headerText = "*** Paid USD " + amount + " from card number ending "
							+ selectedDisplayCardNumber + " ***";
					AlertController.showSuccess(title, headerText, contentText);
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
							contentText = "Try again with different ammount or card." + " Attempts Remaining : "
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
	@FXML
	public void handleReset(ActionEvent event) {
		// TODO Autogenerated
		btnReset.setVisible(false);
		cbCards.getSelectionModel().clearSelection();
		cbCards.setButtonCell(new PromptButtonCell<>(cbCards.getPromptText()));;
		txtAmount.setText("");
		txtAmount.setVisible(false);
		btnPayByCredit.setVisible(false);
	}
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		refreshState();
		// TODO Auto-generated method stub
		displayCardNumbersMapping = new HashMap<>();
		List<String> cardNumbers = new ArrayList<>();
		CreditCard userCreditCard = CreditCardsDAO.getCreditCardByUserId(user.getUserId().toString());
		long cardNumber = userCreditCard.getCardNumber();
		String displayCardNumber = SavingsAccountUtils.getLastFourDigitsOf(cardNumber);
		displayCardNumbersMapping.put(displayCardNumber, cardNumber);
		cardNumbers.add(displayCardNumber);	
		
		ObservableList<String> cardNumbersList = 
				FXCollections
				.observableArrayList(cardNumbers);
		
		//initializing values of account transactions
		cbCards.setItems(cardNumbersList);
		
		txtAmount.setText("");
		txtAmount.setVisible(false);
		btnPayByCredit.setVisible(false);
		btnReset.setVisible(false);
	}
}
