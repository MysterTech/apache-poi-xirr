package com.example.apachepoixirr;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.functions.MultiOperandNumericFunction;
import org.apache.poi.ss.formula.udf.AggregatingUDFFinder;
import org.apache.poi.ss.formula.udf.DefaultUDFFinder;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XIRREvaluator {

  private Workbook workbook;
  private Sheet sheet;
  private Row row;
  private Cell cell;
  private CellStyle percentStyle;
  private CellStyle dateStyle;
  private FormulaEvaluator evaluator;
  private String[] labels;
  private char c1;
  private char c2;
  private String[] formulas;
  private Double[] values;
  private SimpleDateFormat sdf;
  private Date[] dates;

  public XIRREvaluator() {
    this.workbook = new XSSFWorkbook();

    String[] functionNames = {"myXIRR"};
    FreeRefFunction[] functionImpls = {new CalculateXIRR()};

    UDFFinder udfs = new DefaultUDFFinder(functionNames, functionImpls);
    UDFFinder udfToolpack = new AggregatingUDFFinder(udfs);

    workbook.addToolPack(udfToolpack);

    this.percentStyle = workbook.createCellStyle();
    percentStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
    this.dateStyle = workbook.createCellStyle();
    dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));

    this.evaluator = workbook.getCreationHelper().createFormulaEvaluator();

    this.sheet = workbook.createSheet("Sheet1");

    this.labels = new String[] {"XIRR", "myXIRR", "diff"};

    this.sdf = new SimpleDateFormat("yyyy-MM-dd");
  }

  public static void main(String[] args) {

    XIRREvaluator xirrEvaluator = new XIRREvaluator();
    // test case as from the example in Excel's XIRR documentation
    // starting on column 0, row 0
    xirrEvaluator.testCaseFromExcelDocu(0, 0);

    // 9 random test cases
    // starting on column 0, row 10
    xirrEvaluator.randomTestCases(0, 10, 9);

    // 9 random test cases
    // starting on column 0, row 25
    xirrEvaluator.randomTestCases(0, 25, 9);

    xirrEvaluator.save();
  }

  public void save() {
    try {
      workbook.write(new FileOutputStream("ExcelWorkbookXIRR.xlsx"));
      workbook.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void testCaseFromExcelDocu(int startCol, int startRow) {

    /*
     This provides a test case as from the example in Excel's XIRR documentation:
     https://support.office.com/en-us/article/XIRR-function-de1242ec-6477-445b-b11b-a303ad9adc9d
    */

    if (startCol > 24) return;

    try {
      c1 = (char) (65 + startCol);
      c2 = (char) (65 + startCol + 1);
      formulas =
          new String[] {
            "XIRR("
                + c1
                + (startRow + 4)
                + ":"
                + c1
                + (startRow + 8)
                + ","
                + c2
                + (startRow + 4)
                + ":"
                + c2
                + (startRow + 8)
                + ")",
            "myXIRR("
                + c1
                + (startRow + 4)
                + ":"
                + c1
                + (startRow + 8)
                + ","
                + c2
                + (startRow + 4)
                + ":"
                + c2
                + (startRow + 8)
                + ")",
            "" + c2 + (startRow + 1) + "-" + c2 + (startRow + 2)
          };

      values = new Double[] {-10000d, 2750d, 4250d, 3250d, 2750d};

      dates =
          new Date[] {
            sdf.parse("2008-01-01"),
            sdf.parse("2008-03-01"),
            sdf.parse("2008-10-30"),
            sdf.parse("2009-02-15"),
            sdf.parse("2009-04-01")
          };

      for (int r = startRow; r < startRow + 3; r++) {
        row = (sheet.getRow(r) == null) ? sheet.createRow(r) : sheet.getRow(r);
        cell = row.createCell(startCol);
        cell.setCellValue(labels[r - startRow]);
      }

      for (int r = startRow + 3; r < startRow + 8; r++) {
        row = (sheet.getRow(r) == null) ? sheet.createRow(r) : sheet.getRow(r);
        cell = row.createCell(startCol);
        cell.setCellValue(values[r - startRow - 3]);
        cell = row.createCell(startCol + 1);
        cell.setCellValue(dates[r - startRow - 3]);
        cell.setCellStyle(dateStyle);
      }

      for (int r = startRow; r < startRow + 2; r++) {
        cell = sheet.getRow(r).createCell(startCol + 1);
        cell.setCellFormula(formulas[r - startRow]);
        cell.setCellStyle(percentStyle);
        if (r == startRow + 1) {
          cell = evaluator.evaluateInCell(cell);
          System.out.println(new DataFormatter().formatCellValue(cell));
        }
      }

      cell = sheet.getRow(startRow + 2).createCell(startCol + 1);
      cell.setCellFormula(formulas[2]);

      sheet.autoSizeColumn(startCol);
      sheet.autoSizeColumn(startCol + 1);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void randomTestCases(int startCol, int startRow, int count) {

    /*
     This provides randon test cases
    */

    try {
      long day = 24L * 60L * 60L * 1000L;
      long startDate = sdf.parse("2010-01-01").getTime();

      for (int test = startCol; test < startCol + 3 * count; test += 3) {

        if (test > 24) return;

        c1 = (char) (65 + test);
        c2 = (char) (65 + test + 1);
        Random rnd = new Random();
        int rows = 5 + rnd.nextInt(5);

        formulas =
            new String[] {
              "XIRR("
                  + c1
                  + (startRow + 4)
                  + ":"
                  + c1
                  + (startRow + 3 + rows)
                  + ","
                  + c2
                  + (startRow + 4)
                  + ":"
                  + c2
                  + (startRow + 3 + rows)
                  + ")",
              "myXIRR("
                  + c1
                  + (startRow + 4)
                  + ":"
                  + c1
                  + (startRow + 3 + rows)
                  + ", "
                  + c2
                  + (startRow + 4)
                  + ":"
                  + c2
                  + (startRow + 3 + rows)
                  + ")",
              "" + c2 + (startRow + 1) + "-" + c2 + (startRow + 2)
            };

        values = new Double[rows];
        values[0] = -1d * (rows - 1d) * (1000 + rnd.nextInt(5000));
        for (int i = 1; i < rows; i++) {
          values[i] = 1d * (1000 + rnd.nextInt(5000));
        }

        dates = new Date[rows];
        for (int i = 0; i < rows; i++) {
          dates[i] = sdf.parse(sdf.format(new Date(startDate += day * (1L + rnd.nextInt(150)))));
        }

        for (int r = startRow; r < startRow + 3; r++) {
          row = (sheet.getRow(r) == null) ? sheet.createRow(r) : sheet.getRow(r);
          cell = row.createCell(test);
          cell.setCellValue(labels[r - startRow]);
        }

        for (int r = startRow + 3; r < startRow + 3 + rows; r++) {
          row = (sheet.getRow(r) == null) ? sheet.createRow(r) : sheet.getRow(r);
          cell = row.createCell(test);
          cell.setCellValue(values[r - startRow - 3]);
          cell = row.createCell(test + 1);
          cell.setCellValue(dates[r - startRow - 3]);
          cell.setCellStyle(dateStyle);
        }

        for (int r = startRow; r < startRow + 2; r++) {
          cell = sheet.getRow(r).createCell(test + 1);
          cell.setCellFormula(formulas[r - startRow]);
          cell.setCellStyle(percentStyle);
          if (r == startRow + 1) {
            evaluator.clearAllCachedResultValues();
            cell = evaluator.evaluateInCell(cell);
            System.out.println(new DataFormatter().formatCellValue(cell));
          }
        }

        cell = sheet.getRow(startRow + 2).createCell(test + 1);
        cell.setCellFormula(formulas[2]);

        sheet.autoSizeColumn(test);
        sheet.autoSizeColumn(test + 1);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

/*
 Class for user defined function myXIRR
*/
class CalculateXIRR implements FreeRefFunction {

  static final void checkValue(double result) throws EvaluationException {
    if (Double.isNaN(result) || Double.isInfinite(result)) {
      throw new EvaluationException(ErrorEval.NUM_ERROR);
    }
  }

  @Override
  public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {

    if (args.length < 2 || args.length > 3) {
      return ErrorEval.VALUE_INVALID;
    }

    double result;

    try {

      double[] values = ValueCollector.collectValues(args[0]);
      double[] dates = ValueCollector.collectValues(args[1]);

      double guess;
      if (args.length == 3) {
        ValueEval v =
            OperandResolver.getSingleValue(args[2], ec.getRowIndex(), ec.getColumnIndex());
        guess = OperandResolver.coerceValueToDouble(v);
      } else {
        guess = 0.1d;
      }

      result = calculateXIRR(values, dates, guess);

      checkValue(result);

    } catch (EvaluationException e) {
      // e.printStackTrace();
      return e.getErrorEval();
    }

    return new NumberEval(result);
  }

  public double calculateXIRR(double[] values, double[] dates, double guess) {
    double result = Xirr.Newtons_method(guess, values, dates);

    return result;
  }

  static final class ValueCollector extends MultiOperandNumericFunction {
    private static final ValueCollector instance = new ValueCollector();

    public ValueCollector() {
      super(false, false);
    }

    public static double[] collectValues(ValueEval... operands) throws EvaluationException {
      return instance.getNumberArray(operands);
    }

    protected double evaluate(double[] values) {
      throw new IllegalStateException("should not be called");
    }
  }
}
