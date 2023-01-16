package pl.net.brach;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import java.text.DecimalFormat;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

public class MainWindowController implements Initializable {

    RozniceKursowe RozniceKursowe = new RozniceKursowe();
    PodsumowanieController PodsumowanieController = new PodsumowanieController();

    private static final String NBP_API_LINK = "http://api.nbp.pl/api/exchangerates/rates/a/";
    private static final int RETRY_COUNT = 30;
    private static final DateTimeFormatter API_DATA_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
    private static final List<String> DATA_FORMATS = Arrays.asList("dd-MM-yyyy", "dd/MM/yyyy", "ddMMyyyy", "dd.MM.yyyy",
            "yyyy-MM-dd", "yyyy/MM/dd", "yyyyMMdd", "yyyy.MM.dd");

    @FXML
    public MainWindowController MainWindow;
    public Button bOK;
    public Button bZamknij;
    public RadioButton rbZakup;
    public TextField tbWartoscTransakcji;
    public DatePicker dpDataTransakcji;
    public DatePicker dpDataFaktury;
    public ComboBox<String> cbWaluty;
    public Label lFooter;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addCurrenciesToComboBox();

        dpDataFaktury.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent keyEvent) -> {
            if (keyEvent.getCode() == KeyCode.DOWN) {
                dpDataFaktury.setValue(dpDataFaktury.getValue().minusDays(1));
                keyEvent.consume();
            }
            if (keyEvent.getCode() == KeyCode.UP) {
                dpDataFaktury.setValue(dpDataFaktury.getValue().plusDays(1));
                keyEvent.consume();
            }
        });

        dpDataTransakcji.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent keyEvent) -> {
            if (keyEvent.getCode() == KeyCode.DOWN) {
                dpDataTransakcji.setValue(dpDataTransakcji.getValue().minusDays(1));
                keyEvent.consume();
            }
            if (keyEvent.getCode() == KeyCode.UP) {
                dpDataTransakcji.setValue(dpDataTransakcji.getValue().plusDays(1));
                keyEvent.consume();
            }
        });

        tbWartoscTransakcji.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*,")) {
                    tbWartoscTransakcji.setText(newValue.replaceAll("[^\\d,]", ""));
                }
            }
        });

        dpDataTransakcji.getEditor().textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches(".{11}")) {
                    dpDataTransakcji.getEditor().setText(newValue.replaceAll("[^0-9,-\\/]{10}", ""));
                } else {
                    dpDataTransakcji.getEditor().setText("");
                }
            }
        });

        dpDataFaktury.getEditor().textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches(".{11}")) {
                    dpDataFaktury.getEditor().setText(newValue.replaceAll("[^0-9,-\\/]{10}", ""));
                } else {
                    dpDataFaktury.getEditor().setText("");
                }
            }
        });

        modifyDatePickers();
    }

    public void modifyDatePickers() {
        dpDataTransakcji.setConverter(new StringConverter<LocalDate>() {
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
                        } catch (DateTimeParseException dtpe) {
                        }
                    }
                    System.out.println("Parse Error");
                }
                return null;
            }
        });

        dpDataFaktury.setConverter(new StringConverter<LocalDate>() {
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
                        } catch (DateTimeParseException dtpe) {
                        }
                    }
                    System.out.println("Parse Error");
                }
                return null;
            }
        });
    }

    public void addCurrenciesToComboBox() {
        cbWaluty.getItems().clear();
        cbWaluty.getItems().addAll("EUR", "USD", "CHF", "GBP", "CZK", "RON", "HUF");
        cbWaluty.getSelectionModel().selectFirst();
    }

    @FXML
    private void walutaWybrana() {
        cbWaluty.getSelectionModel().select(cbWaluty.getSelectionModel().getSelectedItem());
    }

    private LocalDate extractDataFaktury() {
        LocalDate dDataFaktury = null;
        for (String pattern : DATA_FORMATS) {
            try {
                dDataFaktury = LocalDate.parse(dpDataFaktury.getEditor().getText(), DateTimeFormatter.ofPattern(pattern));//getValue().toString());
            } catch (DateTimeParseException dtpe) {
            }
        }
        return dDataFaktury;
    }

    private LocalDate extractDataTransakcji() {
        LocalDate dDataTransakcji = null;
        for (String pattern : DATA_FORMATS) {
            try {
                dDataTransakcji = LocalDate.parse(dpDataTransakcji.getEditor().getText(), DateTimeFormatter.ofPattern(pattern));//getValue().toString());
            } catch (DateTimeParseException dtpe) {
            }
        }
        return dDataTransakcji;
    }

    @FXML
    private void okClick() throws ParseException, MalformedURLException, IOException {
        //Pobierz wprowadzone dane

        LocalDate dDataFaktury = extractDataFaktury();
        LocalDate dDataTransakcji = extractDataTransakcji();

        String pobraneDaneDlaFaktury = pobierzDane(dDataFaktury, cbWaluty.getValue());
        String pobraneDaneDlaTransakcji = pobierzDane(dDataTransakcji, cbWaluty.getValue());

        //Pobierz i przelicz dane dla faktury
        String numerTabeliDlaFaktury = wyciagnijNumerTabeli(pobraneDaneDlaFaktury);
        String dzienTabeliDlaFaktury = wyciagnijDzien(pobraneDaneDlaFaktury);
        String kursDlaFaktury = wyciagnijKurs(pobraneDaneDlaFaktury);
        double przeliczonaKwotaDlaFaktury = przeliczKwote(kursDlaFaktury);

        //Pobierz i przelicz dane dla transakcji
        String numerTabeliDlaTransakcji = wyciagnijNumerTabeli(pobraneDaneDlaTransakcji);
        String dzienTabeliDlaTransakcji = wyciagnijDzien(pobraneDaneDlaTransakcji);
        String kursDlaTransakcji = wyciagnijKurs(pobraneDaneDlaTransakcji);
        double przeliczonaKwotaDlaTransakcji = przeliczKwote(kursDlaTransakcji);

        //Oblicz różnice kursowe i stwórz popup
        String rozniceKursowe = String.format("%.2f", obliczRozniceKursowe(przeliczKwote(kursDlaFaktury), przeliczKwote(kursDlaTransakcji)));

        NumberFormat formatRoznic = NumberFormat.getInstance(Locale.getDefault());
        Number roznice = formatRoznic.parse(rozniceKursowe);
        String komentarz = generujKomentarz(roznice.doubleValue());

        //Twórz Array z parametrami
        String[] parameters = new String[12];

        DecimalFormat format = new DecimalFormat("###,##0.00");

        parameters[0] = format.format(Double.parseDouble(tbWartoscTransakcji.getText().replace(",", "."))) + " " + cbWaluty.getValue();
        parameters[1] = numerTabeliDlaFaktury;
        parameters[2] = dzienTabeliDlaFaktury;
        parameters[3] = kursDlaFaktury.replace(".", ",");
        parameters[4] = format.format(przeliczonaKwotaDlaFaktury) + " zł";
        parameters[5] = numerTabeliDlaTransakcji;
        parameters[6] = dzienTabeliDlaTransakcji;
        parameters[7] = kursDlaTransakcji.replace(".", ",");
        parameters[8] = format.format(przeliczonaKwotaDlaTransakcji) + " zł";
        parameters[9] = rozniceKursowe;
        parameters[10] = komentarz;
        parameters[11] = format.format(Math.abs(roznice.doubleValue())) + " zł";

        pokazPodsumowanie(parameters);
    }
    //Metody pobierające

    private String pobierzDane(LocalDate dData, String sWpisanaWaluta) throws MalformedURLException, IOException {

        String pobraneDane = "";

        boolean retry = true;

        int loopCount = 0;

        while (retry) {

            //Połącz z API NBP i odczytaj dane (to też do odrębnej metody?)
            try {
                //Stwórz link do połączenia się z API NBP
                dData = dData.minusDays(1);
                String formatowanaData = dData.format(API_DATA_FORMAT);

                StringBuilder stringDoAPINBP = new StringBuilder(NBP_API_LINK);
                stringDoAPINBP.append(sWpisanaWaluta).append("/").append(formatowanaData).append("/?format=json");
                String linkDoAPINBP = stringDoAPINBP.toString();

                URL urlAPINBP = new URL(linkDoAPINBP);

                try (BufferedReader in = new BufferedReader(new InputStreamReader(urlAPINBP.openStream()))) {
                    pobraneDane = in.readLine();
                }

                return pobraneDane;

            } catch (FileNotFoundException ex) {
                if (loopCount > RETRY_COUNT) {
                    System.out.println("Przekroczono limit powtórzeń");
                }
                loopCount++;
            }
        }
        return pobraneDane;
    }

    private static String wyciagnijNumerTabeli(String sDane) {
        String numerTabeli = sDane.substring(sDane.indexOf("no") + 5, sDane.indexOf("[") + 22);
        return numerTabeli;
    }

    private static String wyciagnijDzien(String sDane) {
        String sDzienTabeli = sDane.substring(sDane.indexOf("effectiveDate") + 16, sDane.indexOf("[") + 51);
        return sDzienTabeli;
    }

    private String wyciagnijKurs(String sDane) {
        //Sprawdzenie, czy przypadkiem waluta nie jest groszowa i jeśli tak, to ew. dostosowanie kursu
        String sKurs = sDane.substring(sDane.indexOf("mid") + 5, sDane.indexOf("[") + 67);
        if (sKurs.charAt(sKurs.length() - 1) == ']') {
            sKurs = sKurs.substring(0, sKurs.length() - 1);
        }
        if (sKurs.charAt(sKurs.length() - 1) == '}') {
            sKurs = sKurs.substring(0, sKurs.length() - 1);
        }
        return sKurs;
    }

    private double przeliczKwote(String sKurs) {
        double przeliczonaKwota = Math.round((Double.parseDouble(tbWartoscTransakcji.getText().replace(",", ".")) * Double.parseDouble(sKurs)) * 100.00) / 100.00;
        return przeliczonaKwota;
    }

    private double obliczRozniceKursowe(double przeliczonaKwota1, double przeliczonaKwota2) {
        double rozniceKursowe = przeliczonaKwota1 - przeliczonaKwota2;
        return rozniceKursowe;
    }

    private String generujKomentarz(double rozniceKursowe) {
        if (rbZakup.isSelected()) {
            if (rozniceKursowe <= 0) {
                return "Koszt z różnic kursowych w zakupie: ";
            } else {
                return "Przychód z różnic kursowych w zakupie: ";
            }
        } else {
            if (rozniceKursowe <= 0) {
                return "Przychód z różnic kursowych w sprzedaży: ";

            } else {
                return "Koszt z różnic kursowych w sprzedaży: ";
            }
        }
    }

    @FXML
    private void pokazPodsumowanie(String[] arguments) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Podsumowanie.fxml"));

        Pane root = (Pane) fxmlLoader.load();
        Scene scene = new Scene(root);

        //scene.getStylesheets().add("style.css");

        Stage stage = new Stage();
        stage.setScene(scene);

        stage.setTitle("BRACHSoft - Różnice kursowe v.1.07 - Podsumowanie");
        stage.getIcons().add(new Image("pl/net/brach/brachs.png"));

        PodsumowanieController controller = fxmlLoader.<PodsumowanieController>getController();
        controller.generujPodsumowanie(arguments);

        stage.setResizable(false);
        stage.show();
    }

    @FXML
    private void kopiowanieAdresu() {
        Tooltip tooltip = new Tooltip();
        tooltip.setText("Skopiowano adres e-mail");
        StringSelection selection = new StringSelection("jurek.rafal@outlook.com");
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        lFooter.setTooltip(tooltip);
    }

    @FXML
    private void zamknijClick() {
        Stage stage = (Stage) bZamknij.getScene().getWindow();
        stage.close();
    }
}
