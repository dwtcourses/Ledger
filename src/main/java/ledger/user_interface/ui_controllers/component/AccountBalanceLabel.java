package ledger.user_interface.ui_controllers.component;

import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import ledger.controller.DbController;
import ledger.controller.register.TaskWithReturn;
import ledger.database.entity.Account;
import ledger.database.entity.AccountBalance;
import ledger.database.entity.Transaction;
import ledger.user_interface.ui_controllers.IUIController;
import ledger.user_interface.ui_controllers.Startup;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Label used to track the balance of the current account.
 */
public class AccountBalanceLabel extends Label implements IUIController, Initializable {

    private static final String pageLoc = "/fxml_files/Label.fxml";

    private Account currentAccount;

    public AccountBalanceLabel() {
        this.initController(pageLoc, this, "Unable to load Account Balance Label");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DbController.INSTANCE.registerTransactionSuccessEvent((ignored) -> this.recalculateBalance());
        calculateBalanceForAllAccounts();
    }

    /**
     * Recalculate the balance for the selected account after a change in the TableView
     */
    private void recalculateBalance() {
        if (this.currentAccount == null) {
            Startup.INSTANCE.runLater(this::calculateBalanceForAllAccounts);
            return;
        }
        Startup.INSTANCE.runLater(this::calculateBalanceForAccount);
    }

    /**
     * Calculate the account balance after a new account has been selected
     *
     * @param account The selected account
     */
    public void calculateBalance(Account account) {
        this.currentAccount = account;
        if (this.currentAccount == null) {
            calculateBalanceForAllAccounts();
            return;
        }
        calculateBalanceForAccount();
    }

    private void calculateBalanceForAccount() {
        TaskWithReturn<List<Transaction>> task = DbController.INSTANCE.getAllTransactionsForAccount(this.currentAccount);
        task.RegisterFailureEvent((e) -> this.setupErrorPopup("Unable to fetch transactions from the database.", e));
        task.startTask();
        List<Transaction> transactions = task.waitForResult();

        long amountSpent = 0;
        for (Transaction t : transactions) {
            amountSpent += t.getAmount();
        }

        TaskWithReturn<AccountBalance> balanceTask = DbController.INSTANCE.getBalanceForAccount(this.currentAccount);
        balanceTask.RegisterFailureEvent((e) -> this.setupErrorPopup("Unable to fetch current account balance from the database.", e));
        balanceTask.startTask();
        AccountBalance balance = balanceTask.waitForResult();

        long net = balance == null ? amountSpent : balance.getAmount() + amountSpent;

        this.setText(String.format("%.2f", (double) net / 100.0));
    }

    /**
     * Used to calculate the balance for all accounts in the 'All Accounts' aggregate.
     */
    public void calculateBalanceForAllAccounts() {
        TaskWithReturn<List<Account>> accountsTask = DbController.INSTANCE.getAllAccounts();
        accountsTask.RegisterFailureEvent((e) -> this.setupErrorPopup("Unable to fetch accounts from database.", e));
        accountsTask.startTask();
        List<Account> accounts = accountsTask.waitForResult();

        long net = 0;
        for (Account account : accounts) {
            TaskWithReturn<AccountBalance> balanceTask = DbController.INSTANCE.getBalanceForAccount(account);
            balanceTask.RegisterFailureEvent((e) -> this.setupErrorPopup("Unable to fetch current account balance from the database.", e));
            balanceTask.startTask();
            AccountBalance balance = balanceTask.waitForResult();
            net += balance == null ? 0 : balance.getAmount();
        }

        TaskWithReturn<List<Transaction>> transactionTask = DbController.INSTANCE.getAllTransactions();
        transactionTask.RegisterFailureEvent((e) -> this.setupErrorPopup("Unable to fetch transaction list form database.", e));
        transactionTask.startTask();
        List<Transaction> transactions = transactionTask.waitForResult();

        for (Transaction transaction : transactions) {
            net += transaction.getAmount();
        }

        this.setText(String.format("%.2f", (double) net / 100));
    }
}
