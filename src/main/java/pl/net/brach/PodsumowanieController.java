package pl.net.brach;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.OrientationRequested;

public class PodsumowanieController implements Initializable {

    private static final String LABEL_FILE_PATH = "C:/Temp/Kursy.txt";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH);
    private static final float PAGE_WIDTH = 160;
    private static final float PAGE_HEIGHT = 100;

    @FXML
    private Button bZamknij;
    public Button bDrukuj;
    public Label fakturaNumerTabeli;
    public Label fakturaDataKursu;
    public Label fakturaKwota;
    public Label fakturaKurs;
    public Label fakturaPrzeliczonaKwota;
    public Label transakcjaNumerTabeli;
    public Label transakcjaDataKursu;
    public Label transakcjaKwota;
    public Label transakcjaKurs;
    public Label transakcjaPrzeliczonaKwota;
    public Label rozniceKursoweKomentarz;
    public Label rozniceKursoweKwota;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    public void generujPodsumowanie(String[] arguments) {

        fakturaNumerTabeli.setText(arguments[1]);
        fakturaDataKursu.setText(LocalDate.parse(arguments[2]).format(DATE_FORMAT));
        fakturaKwota.setText(arguments[0]);
        fakturaKurs.setText(arguments[3]);
        fakturaPrzeliczonaKwota.setText(arguments[4]);

        transakcjaNumerTabeli.setText(arguments[5]);
        transakcjaDataKursu.setText(LocalDate.parse(arguments[6]).format(DATE_FORMAT));
        transakcjaKwota.setText(arguments[0]);
        transakcjaKurs.setText(arguments[7]);
        transakcjaPrzeliczonaKwota.setText(arguments[8]);

        rozniceKursoweKomentarz.setText(arguments[10]);
        rozniceKursoweKwota.setText(arguments[11]);

    }

    @FXML
    private void drukujClick() throws IOException {
        zapisywanieEtykiety();

        try {
            drukowanieEtykiety();
        } catch (PrinterException ex) {
        }
    }

    @FXML
    private void zapisywanieEtykiety() throws IOException {

        String[] stringArray = new String[19];

        stringArray[0] = "--------------------------------------";
        stringArray[1] = "            Różnice kursowe";
        stringArray[2] = "--------------------------------------";
        stringArray[3] = "                Faktura";
        stringArray[4] = "    Numer tabeli: " + fakturaNumerTabeli.getText();
        stringArray[5] = "    Data kursu: " + fakturaDataKursu.getText();
        stringArray[6] = "    Kwota w walucie: " + fakturaKwota.getText();
        stringArray[7] = "    Kurs: " + fakturaKurs.getText();
        stringArray[8] = "    Przeliczona kwota: " + fakturaPrzeliczonaKwota.getText();
        stringArray[9] = "-------------------------------------";
        stringArray[10] = "               Zapłata";
        stringArray[11] = "    Numer tabeli: " + transakcjaNumerTabeli.getText();
        stringArray[12] = "    Data kursu: " + transakcjaDataKursu.getText();
        stringArray[13] = "    Kwota w walucie: " + transakcjaKwota.getText();
        stringArray[14] = "    Kurs: " + transakcjaKurs.getText();
        stringArray[15] = "    Przeliczona kwota: " + transakcjaPrzeliczonaKwota.getText();
        stringArray[16] = "-------------------------------------";
        
        if (rozniceKursoweKomentarz.getText().substring(0, 1).equals("K")) { //Koszt
                stringArray[17] = "    " + rozniceKursoweKomentarz.getText().substring(0, 24);
                stringArray[18] = "    " + rozniceKursoweKomentarz.getText().substring(25) + rozniceKursoweKwota.getText();
        } else { //Przychod
                stringArray[17] = "    " + rozniceKursoweKomentarz.getText().substring(0, 27);
                stringArray[18] = "    " + rozniceKursoweKomentarz.getText().substring(28) + rozniceKursoweKwota.getText();
        }

            File tempFile = new File(LABEL_FILE_PATH);

            try ( //Plik jest nadpisywany
                    BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                for (String stringToBeSaved : stringArray) {
                    writer.write(stringToBeSaved + System.lineSeparator());
                }
            }
        }

    private PrintService findPrintService(String printerName) {
        PrintService service = null;

        PrintService[] services = PrinterJob.lookupPrintServices();

        for (int i = 0; service == null && i < services.length; i++) {
            if (services[i].getName().equals(printerName)) {
                service = services[i];
            }
        }
        return service;
    }

    @FXML
    private void drukowanieEtykiety() throws PrinterException, IOException {

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new LabelPrint(odczytywaniePliku()));

        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add(OrientationRequested.PORTRAIT);
        aset.add(OrientationRequested.PORTRAIT);
        aset.add(new MediaPrintableArea(0, 0, PAGE_HEIGHT, PAGE_WIDTH, MediaPrintableArea.MM));

        boolean doPrint = job.printDialog();
        if (doPrint) {
            job.print(aset);
        }
    }

    @FXML
    private String odczytywaniePliku() throws FileNotFoundException, IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(LABEL_FILE_PATH)));
        StringBuilder stringBuilder = new StringBuilder();

        String textLine = bufferedReader.readLine();

        while (textLine != null) {
            stringBuilder.append(textLine);
            stringBuilder.append(System.lineSeparator());
            textLine = bufferedReader.readLine();
        }

        return stringBuilder.toString();
    }

    @FXML
    private void zamknijClick() {
        Stage stage = (Stage) bZamknij.getScene().getWindow();
        stage.close();
    }

}
