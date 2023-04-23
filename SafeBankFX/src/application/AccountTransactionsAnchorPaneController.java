package application;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import dao.SavingsAccountsDAO;
import dao.TransactionsDAO;
import enums.TransactionType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import models.SavingsAccount;
import models.Transaction;
import utils.SavingsAccountUtils;

public class AccountTransactionsAnchorPaneController extends Controller implements Initializable {
	@FXML
	private Pane paneAccounts;
	@FXML
	private ComboBox<String> cbAccounts;
	@FXML
	private Button btnResetCBValue;
	@FXML
	private Label lblAccountBalance;
	@FXML
	private Label lblAccountBalanceValue;
	@FXML
	private TableView<Transaction> tblAccounts;
	@FXML
	private TableColumn<Transaction, String> tblClmTID;
	@FXML
	private TableColumn<Transaction, Long> tblClmAccNo;
	@FXML
	private TableColumn<Transaction, String> tblClmTName;
	@FXML
	private TableColumn<Transaction, Double> tblClmAmt;
	@FXML
	private TableColumn<Transaction, String> tblClmTType;
	@FXML
	private TableColumn<Transaction, Date> tblClmTDate;
	@FXML
	private TextField txtAccountBalance;
	@FXML
	private Label lblAcctBalance;
	
	private Map<String, Long> displayAccountNumbersMapping;
	private String selectedDisplayAccountNumber;
	private SavingsAccount currentSelectedAccount;
	
	// Event Listener on ComboBox[#cbAccounts].onAction
	@FXML
	public void displayAccounts(ActionEvent event) throws IOException {
		
		// TODO Autogenerated
		String selectedAccountNumber = cbAccounts.getSelectionModel().getSelectedItem();
		if(selectedAccountNumber != null) {
			btnResetCBValue.setVisible(true);
			lblAcctBalance.setVisible(true);
			txtAccountBalance.setVisible(true);
			tblAccounts.setVisible(true);
			
			Long accountNumber = displayAccountNumbersMapping.get(selectedAccountNumber);
			currentSelectedAccount = 
					SavingsAccountsDAO
					.getSavingsAccountByAccountNumber(accountNumber);
			txtAccountBalance.setText(currentSelectedAccount.getAccountBalance() + "");
			refreshState();
			List<Transaction> accountTransactions = 
					TransactionsDAO
					.getTransactionsByAccountNumber(accountNumber);
		        
			ObservableList<Transaction> transactions = 
		        		FXCollections.observableArrayList(accountTransactions);
					
			tblClmTID.setCellValueFactory(new PropertyValueFactory<>("TransactionId"));
			tblClmAccNo.setCellValueFactory(new PropertyValueFactory<>("AccountNumber"));
			tblClmTName.setCellValueFactory(new PropertyValueFactory<>("TransactionName"));
			tblClmAmt.setCellValueFactory(new PropertyValueFactory<>("Amount"));
			tblClmTType.setCellValueFactory(new PropertyValueFactory<>("TransactionType"));
			tblClmTDate.setCellValueFactory(new PropertyValueFactory<>("CreatedAt"));
			tblAccounts.setItems(transactions);
		}
	}
	// Event Listener on Button[#btnResetCBValue].onAction
	@FXML
	public void clearSelectedValue(ActionEvent event) throws IOException {
		// TODO Autogenerated
		cbAccounts.getSelectionModel().clearSelection();
		cbAccounts.setButtonCell(new PromptButtonCell<>(cbAccounts.getPromptText()));
		lblAcctBalance.setVisible(false);
		txtAccountBalance.setVisible(false);
		tblAccounts.setVisible(false);
		btnResetCBValue.setVisible(false);
	}
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
//		//List of account numbers of current user
		refreshState();
		displayAccountNumbersMapping = new HashMap<>();
		List<String> accountNumbers = user.getAccounts().stream().map(account -> {
			long accountNumber = account.getAccountNumber();
			String displayAccountNumber = SavingsAccountUtils.getLastFourDigitsOf(accountNumber);
			displayAccountNumbersMapping.put(displayAccountNumber, accountNumber);
			return displayAccountNumber;
		}).collect(Collectors.toList());
		
		ObservableList<String> accountNumbersList = 
				FXCollections.observableArrayList(accountNumbers);
		
		cbAccounts.setItems(accountNumbersList);
		cbAccounts.setStyle("-fx-font-size: 20px;");
		
		btnResetCBValue.setVisible(false);
		lblAcctBalance.setVisible(false);
		txtAccountBalance.setVisible(false);
		tblAccounts.setVisible(false);
		txtAccountBalance.setEditable(false);
	}
}
