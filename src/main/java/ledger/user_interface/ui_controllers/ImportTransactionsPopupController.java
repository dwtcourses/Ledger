package ledger.user_interface.ui_controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ledger.controller.ImportController;
import ledger.controller.register.TaskWithArgs;
import ledger.database.entity.Account;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controls the input gathered from the Import Transaction UI (gets the file to import and type)
 */
public class ImportTransactionsPopupController extends GridPane implements Initializable, IUIController {

    @FXML
    private AccountDropdown accountDropdown;

    @FXML
    private Button importButton;

    @FXML
    private FileSelectorButton fileSelector;

    @FXML
    private ConverterDropdown converterSelector;

    private final static String pageLoc = "/fxml_files/ImportTransactionsPopup.fxml";

    ImportTransactionsPopupController() {
        this.initController(pageLoc, this, "Error on transaction popup startup: ");
    }

    /**
     * Sets up action listener for the button on the page
     * <p>
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param fxmlFileLocation The location used to resolve relative paths for the root object, or
     *                         <tt>null</tt> if the location is not known.
     * @param resources        The resources used to localize the root object, or <tt>null</tt> if
     *                         the root object was not localized.
     */
    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        importButton.setOnAction(this::importFile);
        // TODO Hook up insert
    }

    private void importFile(ActionEvent actionEvent) {
        File file = fileSelector.getFile();
        if (file == null) {
            this.setupErrorPopup("Please make sure a file is selected.", new NullPointerException("Required field is empty."));
            return;
        }

        Account account = accountDropdown.getSelectedAccount();
        if (account == null) {
            this.setupErrorPopup("Please make sure an account is selected.", new NullPointerException("Required field is empty."));
            return;
        }

        ImportController.Converter converter = converterSelector.getFileConverter();
        if (converter == null) {
            this.setupErrorPopup("Please make sure a conversion type is selected.", new NullPointerException("Required field is empty."));
            return;
        }

        TaskWithArgs<Account> task = ImportController.INSTANCE.importTransactions(converter, file, account);
        task.RegisterFailureEvent((e) -> Startup.INSTANCE.runLater(() -> importButton.setDisable(false)));
        task.RegisterSuccessEvent(this::closeWindow);
        importButton.setDisable(true);


        task.startTask();
    }

    private void closeWindow() {
        Startup.INSTANCE.runLater(() -> ((Stage) this.getScene().getWindow()).close());
    }

}