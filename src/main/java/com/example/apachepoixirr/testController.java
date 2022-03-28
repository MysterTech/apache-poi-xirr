package com.example.apachepoixirr;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.formula.eval.FunctionEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class testController {

  @GetMapping("/available")
  public List<String> available() {
    List<String> availableFunctions =
        new ArrayList<String>(FunctionEval.getSupportedFunctionNames());
    return availableFunctions;
  }
  @GetMapping("/notavailable")
  public List<String> notAvailable() {
    List<String> availableFunctions =
        new ArrayList<String>(FunctionEval.getNotSupportedFunctionNames());
    return availableFunctions;
  }

  @GetMapping("/xirr")
  public double calcXirr() {
    String xlFileAddress="tempwb.xlsx";
    try {
      InputStream fileIn = new FileInputStream(xlFileAddress);
      XSSFWorkbook workbook = new XSSFWorkbook(fileIn);

      Cell cell = workbook.getSheet("Valuations").getRow(11).getCell(2);

      FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
      formulaEvaluator.evaluateFormulaCell(cell);

      return cell.getNumericCellValue();

    } catch(Exception e) {
      System.out.println(e);
    }

    Workbook workbook1 = new SXSSFWorkbook(-1);
    Sheet newSheet = workbook1.createSheet("Valuations");
    Row newRow = newSheet.createRow(1);
    newRow.createCell(4, CellType.NUMERIC).setCellValue(20);
    newRow.createCell(5, CellType.NUMERIC).setCellValue(40);
    newRow.createCell(6, CellType.NUMERIC).setCellValue(30);
    Cell cell = newRow.createCell(1);
        cell.setCellFormula("XIRR(" + "E" + 2 + ":" + "G" + 2 + ")");
    FormulaEvaluator formulaEvaluator = workbook1.getCreationHelper().createFormulaEvaluator();
    formulaEvaluator.evaluateFormulaCell(cell);

    /*try {
      String xlFileAddress="tempwb.xls";
      OutputStream fileOut = new FileOutputStream(xlFileAddress);
      workbook1.write(fileOut);
    }

    catch(Exception e) {
      System.out.println(e);
    }*/
    return cell.getNumericCellValue();
  }

  @GetMapping("/xirrEval")
  public void xirrEval() {


  }

}
