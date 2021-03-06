package ledger.user_interface.ui_controllers.component;

import javafx.collections.FXCollections;
import ledger.controller.DbController;
import ledger.controller.register.TaskWithReturn;
import ledger.database.entity.Account;
import ledger.user_interface.ui_controllers.Startup;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Account dropdown with an added "All Accounts" option. Used to filter transactions by account.
 */
public class FilteringAccountDropdown extends AccountDropdown {

    private final static Account allAccounts = new Account("All Accounts", "View all transactions");
    private static final String pageLoc = "/fxml_files/ChoiceBox.fxml";

    public FilteringAccountDropdown() {
        this.initController(pageLoc, this, "Unable to load Filtering Account Dropdown");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateAccounts();
        DbController.INSTANCE.registerAccountSuccessEvent((ignored) -> Startup.INSTANCE.runLater(this::updateAccounts));
        this.setValue(allAccounts);
    }

    private void updateAccounts() {
        Account currentSelection = this.getValue();
        TaskWithReturn<List<Account>> task = DbController.INSTANCE.getAllAccounts();
        task.startTask();
        List<Account> accounts = task.waitForResult();

        accounts.add(allAccounts);

        this.setItems(FXCollections.observableArrayList(accounts));
        if (this.getItems().contains(currentSelection))
            this.setValue(currentSelection);
        else {
            this.setValue(allAccounts);
        }
    }

    /**
     * Gets the selected Account.
     *
     * @return Selected account, or null if "All Accounts" selected
     */
    @Override
    public Account getSelectedAccount() {
        if (this.getValue() == allAccounts) {
            return null;
        }
        return this.getValue();
    }

    public void selectDefault() {
        this.setValue(allAccounts);
    }
}
