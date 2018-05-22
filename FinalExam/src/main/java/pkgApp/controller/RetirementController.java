package pkgApp.controller;

import java.net.URL;
import java.util.*;

import javafx.scene.text.Text;
import org.apache.poi.ss.formula.functions.FinanceLib;

import com.sun.prism.paint.Color;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.text.FontWeight;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import javafx.beans.value.*;

import pkgApp.RetirementApp;
import pkgCore.Retirement;

public class RetirementController implements Initializable {

	private RetirementApp mainApp = null;
	@FXML
	private TextField txtSaveEachMonth;
	@FXML
	private TextField txtYearsToWork;
	@FXML
	private TextField txtAnnualReturnWorking;
	@FXML
	private TextField txtWhatYouNeedToSave;
	@FXML
	private TextField txtYearsRetired;
	@FXML
	private TextField txtAnnualReturnRetired;
	@FXML
	private TextField txtRequiredIncome;
	@FXML
	private TextField txtMonthlySSI;
	List<TextField> inFields, outFields;
	private static final String numRegex = "\\d*?";
	private static final String deciRegex = "\\d*(\\.\\d*)?";

	private static class IntLimit {
		public int l, h;
		public IntLimit(int l, int h) {
			this.l = l; this.h = h;
		}
	}

	private HashMap<TextField, String> hmTextFieldRegEx = new HashMap<TextField, String>();
	private HashMap<TextField, IntLimit> hmLimits = new HashMap<TextField, IntLimit>();

	public RetirementApp getMainApp() {
		return mainApp;
	}

	public void setMainApp(RetirementApp mainApp) {
		this.mainApp = mainApp;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// Adding an entry in the hashmap for each TextField control I want to validate
		// with a regular expression
		// "\\d*?" - means any decimal number
		// "\\d*(\\.\\d*)?" means any decimal, then optionally a period (.), then
		// decmial
		hmTextFieldRegEx.put(txtYearsToWork, numRegex);
		hmLimits.put(txtYearsToWork, new IntLimit(0, 40));
		hmTextFieldRegEx.put(txtAnnualReturnWorking, deciRegex);
		hmLimits.put(txtAnnualReturnWorking, new IntLimit(0, 10));
		hmTextFieldRegEx.put(txtYearsRetired, numRegex);
		hmLimits.put(txtYearsRetired, new IntLimit(0, 20));
		hmTextFieldRegEx.put(txtAnnualReturnRetired, deciRegex);
		hmLimits.put(txtAnnualReturnRetired, new IntLimit(0, 10));
		hmTextFieldRegEx.put(txtRequiredIncome, numRegex);
		hmLimits.put(txtRequiredIncome, new IntLimit(2642, 10000));
		hmTextFieldRegEx.put(txtMonthlySSI, numRegex);
		hmLimits.put(txtMonthlySSI, new IntLimit(0, 2642));


		// Check out these pages (how to validate controls):
		// https://stackoverflow.com/questions/30935279/javafx-input-validation-textfield
		// https://stackoverflow.com/questions/40485521/javafx-textfield-validation-decimal-value?rq=1
		// https://stackoverflow.com/questions/8381374/how-to-implement-a-numberfield-in-javafx-2-0
		// There are some  examples on how to validate / check format
		Iterator it = hmTextFieldRegEx.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			TextField txtField = (TextField) pair.getKey();
			String strRegEx = (String) pair.getValue();

			txtField.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0,
									Boolean oldPropertyValue,
									Boolean newPropertyValue) {
					// If newPropertyValue = true, then the field HAS FOCUS
					// If newPropertyValue = false, then field HAS LOST FOCUS
					if (!newPropertyValue) {
						IntLimit lim = hmLimits.get(txtField);
						double num;
						try {
							num = Double.parseDouble(txtField.getText());
						} catch (NumberFormatException ex) {
							num = lim.l;
						}
						if (!txtField.getText().matches(strRegEx) || (
								(int)num < lim.l || (int)Math.ceil(num) > lim.h)) {
							txtField.setText("");
							txtField.requestFocus();
						}
					}
				}
			});
		}

		inFields = new ArrayList<>(Arrays.asList(
		        txtYearsToWork, txtAnnualReturnRetired,
                txtAnnualReturnWorking, txtYearsRetired, txtRequiredIncome,
                txtMonthlySSI));
        outFields = new ArrayList<>(Arrays.asList(
                txtSaveEachMonth, txtWhatYouNeedToSave));

		

	@FXML
	public void btnClear(ActionEvent event) {
		System.out.println("Clear pressed");

		// disable read-only controls
        for (TextField field: outFields) {
            field.clear();
            field.setDisable(true);
        }

        for (TextField field: inFields) {
            field.clear();
            field.setDisable(false);
        }
	}


	@FXML
	public void btnCalculate() {
		System.out.println("calculating");

		txtSaveEachMonth.setDisable(false);
		txtWhatYouNeedToSave.setDisable(false);

		double nowRate = Double.parseDouble(
						txtAnnualReturnWorking.getText()) / 100,
				retRate = Double.parseDouble(
						txtAnnualReturnRetired.getText()) / 100,
				reqIncome = Double.parseDouble(txtRequiredIncome.getText()),
				ssi = Double.parseDouble(txtMonthlySSI.getText());
		int workYears = Integer.parseInt(txtYearsToWork.getText()),
				workRet = Integer.parseInt(txtYearsRetired.getText());

		Retirement calc = new Retirement(workYears, nowRate, workRet,
				retRate, reqIncome, ssi);
		System.out.println(calc.MonthlySavings());
		txtWhatYouNeedToSave.setText(String.valueOf(calc.TotalAmountToSave()));
		txtWhatYouNeedToSave.setDisable(true);

		txtSaveEachMonth.setText(String.valueOf(calc.MonthlySavings()));
		txtSaveEachMonth.setDisable(true);
	}
}
