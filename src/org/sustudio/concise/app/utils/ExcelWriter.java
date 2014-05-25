package org.sustudio.concise.app.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;

public class ExcelWriter {

	public static enum Style {
		/**
		 * 標題列
		 */
		HEADER,
		
		/**
		 * 靠左對齊
		 */
		TEXT_ALIGN_LEFT,
		
		/**
		 * 置中對齊
		 */
		TEXT_ALIGN_CENTER,
		
		/**
		 * 靠右對齊
		 */
		TEXT_ALIGN_RIGHT,
		
		/**
		 * 數字
		 */
		NUMBER,
		
		/**
		 * 含千分位的整數
		 */
		NUMBER_FREQUENCY,
		
		/**
		 * 百分比
		 */
		NUMBER_PERCENTAGE,
		
		/**
		 * 小數點八位的係數
		 */
		NUMBER_COEFFICIENT,
		
		/**
		 * 小數點十二位的精確數字
		 */
		NUMBER_PERCESION,
		
		/** PNG format */
		PICTURE_TYPE_PNG,
		/** JPEG format */
		PICTURE_TYPE_JPEG,
		/** Device independent bitmap */
		PICTURE_TYPE_DIB,
		/** Extended windows meta file */
		PICTURE_TYPE_EMF,
		/** Mac PICT format */
		PICTURE_TYPE_PICT,
		/** Windows Meta File */
		PICTURE_TYPE_WMF,
		
		;
		
	}
	
	private Workbook wb;
	private Sheet sh;
	private Map<Style, CellStyle> styles;
	private Row row;
	private int rownum;
	
	
	/**
	 * Default Constructor.
	 */
	public ExcelWriter() {
		this(null);
	}
	
	/**
	 * Constructor with specific sheet name.
	 * @param sheet
	 */
	public ExcelWriter(String sheet) {
		/**
		 * Refer to Apache POI 3.8
		 * http://poi.apache.org/spreadsheet/how-to.html#sxssf
		 */
		
		wb = new SXSSFWorkbook(100);
		if (sheet == null)
			sh = wb.createSheet();
		else
			sh = wb.createSheet(sheet);
		sh.createFreezePane(1, 1, 1, 1);
		
		styles = createStyles();
		rownum = 0;
	}
	
	/**
	 * Sets specific column width.
	 * @param columnIndex
	 * @param width
	 */
	public void setColumnWidth(int columnIndex, int width) {
		sh.setColumnWidth(columnIndex, width);
	}
	
	private Map<Style, CellStyle> createStyles() {
		Map<Style, CellStyle> styles = new HashMap<Style, CellStyle>();
		DataFormat fmt = wb.createDataFormat();
		Font textFont = wb.createFont();
		textFont.setFontName("Heiti TC Light");
		Font numericFont = wb.createFont();
		numericFont.setFontName("Courier");
		
		CellStyle style = wb.createCellStyle();
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		styles.put(Style.HEADER, style);
		
		style = wb.createCellStyle();
		style.setAlignment(XSSFCellStyle.ALIGN_LEFT);
		style.setFont(textFont);
		styles.put(Style.TEXT_ALIGN_LEFT, style);
		
		style = wb.createCellStyle();
		style.setAlignment(XSSFCellStyle.ALIGN_CENTER);
		style.setFont(textFont);
		styles.put(Style.TEXT_ALIGN_CENTER, style);
		
		style = wb.createCellStyle();
		style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
		style.setFont(textFont);
		styles.put(Style.TEXT_ALIGN_RIGHT, style);
		
		style = wb.createCellStyle();
		style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
		style.setFont(numericFont);
		styles.put(Style.NUMBER, style);
		
		style = wb.createCellStyle();
		style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
		style.setDataFormat(fmt.getFormat("#,##0"));
		style.setFont(numericFont);
		styles.put(Style.NUMBER_FREQUENCY, style);
		
		style = wb.createCellStyle();
		style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
		style.setDataFormat(fmt.getFormat("#,##0.00"));
		style.setFont(numericFont);
		styles.put(Style.NUMBER_PERCENTAGE, style);
		
		style = wb.createCellStyle();
		style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
		style.setDataFormat(fmt.getFormat("#,##0.00000000"));
		style.setFont(numericFont);
		styles.put(Style.NUMBER_COEFFICIENT, style);
		
		return styles;
	}
	
	/**
	 * Creates a header row.
	 * No need to call createRow() ahead.
	 * @param header
	 */
	public void createHeader(String[] header) {
		this.createRow();
		for (int i=0; i<header.length; i++) {
			this.createCell(i, header[i], Style.HEADER);
		}
	}
	
	/**
	 * Creates a data row.
	 */
	public void createRow() {
		this.row = this.sh.createRow(this.rownum);
		this.rownum++;
	}
	
	/**
	 * Creates a cell with String value and specific style.
	 * @param columIndex
	 * @param value
	 * @param style
	 */
	public void createCell(int columIndex, String value, Style style) {
		Cell cell = this.row.createCell(columIndex);
		cell.setCellValue(value);
		cell.setCellStyle(this.styles.get(style));
	}
	
	/**
	 * Creates a cell with String value.
	 * @param columIndex
	 * @param value
	 */
	public void createCell(int columIndex, String value) {
		Cell cell = this.row.createCell(columIndex);
		cell.setCellValue(value);
		cell.setCellStyle(this.styles.get(Style.TEXT_ALIGN_LEFT));
	}
	
	/**
	 * Creates a cell with double value and specific style.
	 * @param columIndex
	 * @param value
	 * @param style
	 */
	public void createCell(int columIndex, double value, Style style) {
		Cell cell = this.row.createCell(columIndex);
		cell.setCellValue(value);
		cell.setCellStyle(this.styles.get(style));
	}
	
	/**
	 * Creates a cell with double value.
	 * @param columIndex
	 * @param value
	 */
	public void createCell(int columIndex, double value) {
		Cell cell = this.row.createCell(columIndex);
		cell.setCellValue(value);
		cell.setCellStyle(this.styles.get(Style.NUMBER));
	}
	
	/**
	 * Creates a cell with a image.
	 * 
	 * To insert a SWT image:
	 * 		Image image = new Image(); // SWT image
	 * 		ImageLoader loader = new ImageLoader();
	 * 		loader.data = new ImageData[] { image.getImageData() };
	 * 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	 * 		loader.save(baos, SWT.IMAGE_PNG);
	 * 		. . . .
	 * 		createCell(3, baos, Workbook.PICTURE_TYPE_PNG);		
	 * 		. . . .
	 * 		baos.close();
	 * 		
	 * @param columnIndex
	 * @param baos
	 * @param pictureType - one of the following format
	 * 				Workbook.PICTURE_TYPE_DIB, Workbook.PICTURE_TYPE_EMF, Workbook.PICTURE_TYPE_JPEG,  				Workbook.PICTURE_TYPE_PICT
	 * 				Workbook.PICTURE_TYPE_PNG, Workbook.PICTURE_TYPE_WMF
	 * @throws IOException
	 */
	public void createCell(int columnIndex, ByteArrayOutputStream baos, Style pictureType) throws IOException {
		createCell(columnIndex, baos.toByteArray(), pictureType);
	}
	
	/**
	 * Creates a cell with image byte array.
	 * @param columnIndex
	 * @param image - image byte array.
	 * @param pictureType - one of the following format
	 * 				Workbook.PICTURE_TYPE_DIB, Workbook.PICTURE_TYPE_EMF, Workbook.PICTURE_TYPE_JPEG,  				Workbook.PICTURE_TYPE_PICT
	 * 				Workbook.PICTURE_TYPE_PNG, Workbook.PICTURE_TYPE_WMF
	 * @throws IOException
	 */
	public void createCell(int columnIndex, byte[] image, Style pictureType) throws IOException {
		int type = Workbook.PICTURE_TYPE_PNG;
		switch (pictureType) {
		case PICTURE_TYPE_DIB:
			type = Workbook.PICTURE_TYPE_DIB;
			break;
		case PICTURE_TYPE_EMF:
			type = Workbook.PICTURE_TYPE_EMF;
			break;
		case PICTURE_TYPE_JPEG:
			type = Workbook.PICTURE_TYPE_JPEG;
			break;
		case PICTURE_TYPE_PICT:
			type = Workbook.PICTURE_TYPE_PICT;
			break;
		case PICTURE_TYPE_PNG:
			type = Workbook.PICTURE_TYPE_PNG;
			break;
		case PICTURE_TYPE_WMF:
			type = Workbook.PICTURE_TYPE_WMF;
			break;
		default:
			break;
		}
		
		int pictureIndex = wb.addPicture(image, type);
		
		// Create the drawing patriarch.  This is the top level container for all shapes.
		Drawing drawing = sh.createDrawingPatriarch();
		
		// add a picture shape
		ClientAnchor anchor = new XSSFClientAnchor();
		anchor.setCol1(columnIndex);
		anchor.setCol2(columnIndex+1);
		anchor.setRow1(rownum-1);
		anchor.setRow2(rownum);
		drawing.createPicture(anchor, pictureIndex);
	}
	
	/**
	 * Writes to file and close it.
	 * @param fileOut
	 * @throws IOException
	 */
	public void write(File fileOut) throws IOException {
		FileOutputStream out = new FileOutputStream(fileOut);
		wb.write(out);
		out.close();
		
		((SXSSFSheet) sh).flushRows(); 
		sh = null;
		styles.clear();
		styles = null;
		wb = null;
	}
}
