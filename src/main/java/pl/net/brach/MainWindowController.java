package pl.net.brach;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.text.DecimalFormat;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

public class MainWindowController implements Initializable {

    private static final List<String> DATA_FORMATS = Arrays.asList("dd-MM-yyyy", "dd/MM/yyyy", "ddMMyyyy", "dd.MM.yyyy",
            "yyyy-MM-dd", "yyyy/MM/dd", "yyyyMMdd", "yyyy.MM.dd");

    private static final String NBP_API_LINK = "http://api.nbp.pl/api/exchangerates/rates/a/";
    private static final int NBP_API_RETRY_COUNT = 30;
    private static final DateTimeFormatter NBP_API_DATA_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

    @FXML
    private Button bClose;
    @FXML
    private RadioButton rbPurchase;
    @FXML
    private TextField tbTransactionAmount;
    @FXML
    private DatePicker dpTransactionDate;
    @FXML
    private DatePicker dpInvoiceDate;
    @FXML
    private ComboBox<String> cbCurrencies;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addCurrenciesToComboBox();

        dpInvoiceDate.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent keyEvent) -> {
            if (keyEvent.getCode() == KeyCode.DOWN) {
                dpInvoiceDate.setValue(dpInvoiceDate.getValue().minusDays(1));
                keyEvent.consume();
            }
            if (keyEvent.getCode() == KeyCode.UP) {
                dpInvoiceDate.setValue(dpInvoiceDate.getValue().plusDays(1));
                keyEvent.consume();
            }
        });

        dpTransactionDate.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent keyEvent) -> {
            if (keyEvent.getCode() == KeyCode.DOWN) {
                dpTransactionDate.setValue(dpTransactionDate.getValue().minusDays(1));
                keyEvent.consume();
            }
            if (keyEvent.getCode() == KeyCode.UP) {
                dpTransactionDate.setValue(dpTransactionDate.getValue().plusDays(1));
                keyEvent.consume();
            }
        });

        tbTransactionAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*,")) {
                tbTransactionAmount.setText(newValue.replaceAll("[^\\d,]", ""));
            }
        });

        dpTransactionDate.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(".{11}")) {
                dpTransactionDate.getEditor().setText(newValue.replaceAll("[^\\d,-\\/]{10}", ""));
            } else {
                dpTransactionDate.getEditor().setText("");
            }
        });

        dpInvoiceDate.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(".{11}")) {
                dpInvoiceDate.getEditor().setText(newValue.replaceAll("[^\\d,-\\/]{10}", ""));
            } else {
                dpInvoiceDate.getEditor().setText("");
            }
        });

        modifyDatePickers();
    }

    public void modifyDatePickers() {
        dpTransactionDate.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    for (String pattern : DATA_FORMATS) {
                        try {
                            if (date.isAfter(LocalDate.now())) {
                                return DateTimeFormatter.ofPattern(pattern).format(LocalDate.now());
                            } else {
                                return DateTimeFormatter.ofPattern(pattern).format(date);
                            }
                        } catch (DateTimeException dte) {
                            System.out.println("Format Error");
                        }
                    }
                }
                return "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    for (String pattern : DATA_FORMATS) {
                        try {
                            return LocalDate.parse(string, DateTimeFormatter.ofPattern(pattern));
                        } catch (DateTimeParseException ignored) { }
                    }
                    System.out.println("Parse Error");
                }
                return null;
            }
        });

        dpInvoiceDate.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    for (String pattern : DATA_FORMATS) {
                        try {
                            if (date.isAfter(LocalDate.now())) {
                                return DateTimeFormatter.ofPattern(pattern).format(LocalDate.now());
                            } else {
                                return DateTimeFormatter.ofPattern(pattern).format(date);
                            }
                        } catch (DateTimeException dte) {
                            System.out.println("Format Error");
                        }
                    }
                }
                return "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    for (String pattern : DATA_FORMATS) {
                        try {
                            return LocalDate.parse(string, DateTimeFormatter.ofPattern(pattern));
                        } catch (DateTimeParseException ignored) { }
                    }
                    System.out.println("Parse Error");
                }
                return null;
            }
        });
    }

    public void addCurrenciesToComboBox() {
        Currency currency = new Currency();

        cbCurrencies.getItems().clear();
        cbCurrencies.getItems().addAll(currency.getCurrencies());
        cbCurrencies.getSelectionModel().selectFirst();
    }

    @FXML
    private void currencyChosen() { cbCurrencies.getSelectionModel().select(cbCurrencies.getSelectionModel().getSelectedItem()); }

    private LocalDate extractDate(DatePicker datePicker) {
        LocalDate dInvoiceDate = null;
        for (String pattern : DATA_FORMATS) {
            try {
                dInvoiceDate = LocalDate.parse(datePicker.getEditor().getText(), DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException ignored) { }
        }
        return dInvoiceDate;
    }

    @FXML
    private void okClicked() throws IOException {
        //Get user input data
        if (dpTransactionDate.getEditor().getText().equals("")) {
            System.out.println("No data was provided. Aborting...");
        } else {
            LocalDate dInvoiceDate = extractDate(dpInvoiceDate);
            LocalDate dTransactionDate = extractDate(dpTransactionDate);

            String currency = cbCurrencies.getValue();

            String fetchedInvoiceData = getData(dInvoiceDate, currency);
            String fetchedTransactionData = getData(dTransactionDate, currency);

            String invoiceRate = getRate(fetchedInvoiceData);
            double calculatedInvoiceAmount = calculateAmount(invoiceRate);

            String transactionRate = getRate(fetchedTransactionData);
            double calculatedTransactionAmount = calculateAmount(transactionRate);

            double exchangeRatesDifference = calculatedInvoiceAmount - calculatedTransactionAmount;

            String[] args = new String[13];

            DecimalFormat format = new DecimalFormat("###,##0.00");

            args[0] = format.format(Double.parseDouble(tbTransactionAmount.getText().replace(",", "."))) + " " + cbCurrencies.getValue();
            args[1] = getTableNumber(fetchedInvoiceData);
            args[2] = getDate(fetchedInvoiceData);
            args[3] = invoiceRate.replace(".", ",");
            args[4] = format.format(calculatedInvoiceAmount) + " zł";
            args[5] = getTableNumber(fetchedTransactionData);
            args[6] = getDate(fetchedTransactionData);
            args[7] = transactionRate.replace(".", ",");
            args[8] = format.format(calculatedTransactionAmount) + " zł";
            args[9] = String.format("%.2f", exchangeRatesDifference);
            args[10] = getCommentary(exchangeRatesDifference);
            args[11] = format.format(Math.abs(calculatedInvoiceAmount - calculatedTransactionAmount)) + " zł";

            args[12] = currency;

            ExchangeDifferences.displaySummary(args);
        }
    }

    private String getData(LocalDate dData, String sCurrencyInput) throws IOException {
        String dataFetched = "";

        boolean retry = true;

        int loopCount = 0;

        while (retry) {
            //Get the data from NBP API
            try {
                dData = dData.minusDays(1);
                String formattedDate = dData.format(NBP_API_DATA_FORMAT);

                URL nbpApiUrl = new URL(NBP_API_LINK + sCurrencyInput + "/" + formattedDate + "/?format=json");

                try (BufferedReader in = new BufferedReader(new InputStreamReader(nbpApiUrl.openStream()))) {
                    dataFetched = in.readLine();
                }

                return dataFetched;
            } catch (FileNotFoundException ex) {
                if (loopCount > NBP_API_RETRY_COUNT) {
                    System.out.println("Retry count exceeded");
                    retry = false;
                }
                loopCount++;
            }
        }
        return dataFetched;
    }

    private static String getTableNumber(String sData) {
        return sData.substring(sData.indexOf("no") + 5, sData.indexOf("[") + 22);
    }

    private static String getDate(String sData) {
        return sData.substring(sData.indexOf("effectiveDate") + 16, sData.indexOf("[") + 51);
    }

    private String getRate(String sData) {
        //Check if the currency is not in 1/100 PLN and adjust if necessary
        String sRate = sData.substring(sData.indexOf("mid") + 5, sData.indexOf("[") + 67);
        if (sRate.charAt(sRate.length() - 1) == ']') {
            sRate = sRate.substring(0, sRate.length() - 1);
        }
        if (sRate.charAt(sRate.length() - 1) == '}') {
            sRate = sRate.substring(0, sRate.length() - 1);
        }
        return sRate;
    }

    private double calculateAmount(String sRate) {
        return Math.round((Double.parseDouble(tbTransactionAmount.getText().replace(",", "."))
                * Double.parseDouble(sRate)) * 100.00) / 100.00;
    }

    private String getCommentary(double exchangeRatesDifference) {
        if (rbPurchase.isSelected()) {
            if (exchangeRatesDifference <= 0) {
                return "Koszt z różnic kursowych w zakupie: ";
            } else {
                return "Przychód z różnic kursowych w zakupie: ";
            }
        } else {
            if (exchangeRatesDifference <= 0) {
                return "Przychód z różnic kursowych w sprzedaży: ";

            } else {
                return "Koszt z różnic kursowych w sprzedaży: ";
            }
        }
    }

    @FXML
    private void closeClicked() {
        Stage stage = (Stage) bClose.getScene().getWindow();
        stage.close();
    }
}
