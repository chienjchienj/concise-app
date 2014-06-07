package org.sustudio.concise.app.helper;

import it.uniroma1.dis.wsngroup.gexf4j.core.Edge;
import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.List;

import javafx.embed.swt.FXCanvas;
import javafx.embed.swt.SWTFXUtils;
import javafx.scene.image.WritableImage;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.eclipse.gef4.zest.core.widgets.GraphConnection;
import org.eclipse.gef4.zest.core.widgets.GraphNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.dialog.CASaveFileDialog;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.gear.IGearTableBased;
import org.sustudio.concise.app.gear.collocationalNetworker.NetworkGraph;
import org.sustudio.concise.app.gear.wordClouder.WordCloud;
import org.sustudio.concise.app.utils.ExcelWriter;
import org.sustudio.concise.app.utils.ExcelWriter.Style;
import org.sustudio.concise.app.widgets.CASpinner;

public class SaveOutputHelper {

	private static Gear gear;
	private static MenuItem saveOutputItem;
		
	public static void setPopupMenuItem(MenuItem item) {
		item.setText(saveOutputItem.getText());
		item.setEnabled(saveOutputItem.getEnabled());
	}
	
	public static void setMenuItem(MenuItem item) {
		SaveOutputHelper.saveOutputItem = item;
	}
	
	public static Gear getGear() {
		return gear;
	}
	
	public static void listenTo(Gear gear) {
		SaveOutputHelper.gear = gear;
		boolean enabled = false;
		saveOutputItem.setEnabled(enabled);
		if (gear == null) return;
				
		Control ctrl = gear.getController(Concise.getCurrentWorkspace()).getControl();
		if (ctrl instanceof Table) {
			enabled = ((Table) ctrl).getItemCount() > 0;
		}
		
		else if (ctrl instanceof WordCloud) {
			enabled = ((WordCloud) ctrl).getWords() != null &&
					  !((WordCloud) ctrl).getWords().isEmpty();
		}
		
		else if (ctrl instanceof NetworkGraph) {
			enabled = !((NetworkGraph) ctrl).getNodes().isEmpty();
		}
		
		else if (ctrl instanceof FXCanvas) {
			enabled = true;
		}
		
		
		if (saveOutputItem != null)
			saveOutputItem.setEnabled(enabled);
	}
	
	
	private static SelectionAdapter saveOutputSelectionAdapter = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			if (gear == null) return;
			
			CASaveFileDialog dlg = new CASaveFileDialog();
			switch (gear) {
			case ScatterPlotter:
			case WordClouder:
			case WordTrender:
				dlg.setSaveImageConfigure();
				break;
				
			case CollocationalNetworker:
				dlg.setNetworkConfigure();
				break;
				
			default:
				dlg.setSaveOutputAsConfigure();
				break;
			}
			dlg.setFileName(gear.name());
			String filename = dlg.open();
			if (filename != null) {
				File file = new File(filename);
				try {
					SaveOutputHelper.write(file);
				} catch (Exception e) {
					CAErrorMessageDialog.open(null, e);
				}
			}
		} }; 
	
	public static SelectionAdapter getSaveOutputSelectionAdapter() {
		return saveOutputSelectionAdapter;
	}
	
	
	public static void write(File file) throws Exception {
		Concise.getCurrentWorkspace().logInfo("Output " + file.getCanonicalPath());
		CASpinner spinner = new CASpinner(gear.getController(Concise.getCurrentWorkspace()));
		spinner.open();
		
		// handling text file
		String f = file.getName().toLowerCase();
		if (f.endsWith(".txt") || f.endsWith(".csv"))
			writeTextFile(file, f.endsWith(".csv"));
		
		// handling Excel file
		else if (f.endsWith(".xlsx"))
			writeExcel(file);
		
		// handling Image file
		else if (f.endsWith(".png") ||
				 f.endsWith(".jpg") ||
				 f.endsWith(".tif") ||
				 f.endsWith(".gif") ||
				 f.endsWith(".bmp"))
		{
			writeImage(file);
		}
				
		// handling Network file GEXF
		else if (f.endsWith(".gexf")) {
			writeGexf(file);
		}
		
		// handling Network file XGMML (Cytoscape)
		else if (f.endsWith(".xgmml")) {
			writeXgmml(file);
		}
		
		// handling Network file NET
		else if (f.endsWith(".net")) {
			writeNet(file);
		}
		
		// handling Network file DL
		else if (f.endsWith(".dl")) { 
			writeDL(file);
		}
		
		spinner.close();
	}
	
	
	protected static void writeImage(File file) throws Exception {
		ImageData imageData = null;
		Control control = gear.getController(Concise.getCurrentWorkspace()).getControl();
		if (control instanceof WordCloud)
			imageData = ((WordCloud) control).getImageData();
		else if (control instanceof NetworkGraph)
			imageData = ((NetworkGraph) control).getImageData();
		else if (control instanceof FXCanvas) {
			WritableImage writableImage = ((FXCanvas) control).getScene().snapshot(null);
			imageData = SWTFXUtils.fromFXImage(writableImage, null);
		}
		else
			throw new UnsupportedOperationException(gear.label() + " cannot output image data.");
		
		int format = -1;
		String f = file.getName().toLowerCase();
		if (f.endsWith(".png"))
			format = SWT.IMAGE_PNG;
		else if (f.endsWith(".jpg"))
			format = SWT.IMAGE_JPEG;
		else if (f.endsWith(".tif"))
			format = SWT.IMAGE_TIFF;
		else if (f.endsWith(".gif"))
			format = SWT.IMAGE_GIF;
		else if (f.endsWith(".bmp"))
			format = SWT.IMAGE_BMP;
		else
			throw new UnsupportedOperationException("File format is not supported!");
		
		FileOutputStream stream = new FileOutputStream(file);
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { imageData };
		loader.save(stream, format);
		stream.close();
		loader = null;
	}
	
	protected static void writeTextFile(File file, boolean csv) throws Exception {
		final String delim = csv ? "," : "\t";
		
		GearController gearControl = gear.getController(Concise.getCurrentWorkspace());
		if (gearControl instanceof IGearTableBased &&
			gearControl.getControl() instanceof Table) {
			
			Table table = (Table) gearControl.getControl();
			
			PrintWriter pw = new PrintWriter(
					new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(file),
												   "UTF-8")));
			
			// header
			StringBuilder buffer = new StringBuilder();
			for (TableColumn col : table.getColumns()) {
				buffer.append(buffer.length() > 0 ? delim : "");
				buffer.append(escapeCsv(col.getText(), csv));
			}
			pw.println(buffer.toString());
			buffer.setLength(0);
			
			// contents
			for (int i=0; i<table.getItemCount(); i++) {
				String[] texts = ((IGearTableBased) gearControl).getItemTexts(i);
				texts = escapeCsv(texts, csv);
				String line = StringUtils.join(texts, delim);
				pw.println(line);
				
				if (i % 10000 == 0) {
					pw.flush();
				}
			}
			pw.close();
		}
	}
	
	private static String escapeCsv(String input, boolean isCsv) {
		return isCsv ? StringEscapeUtils.escapeCsv(input) : input;
	}
	
	private static String[] escapeCsv(String[] input, boolean isCsv) {
		for (int i=0; i<input.length; i++) {
			input[i] = escapeCsv(input[i], isCsv);
		}
		return input;
	}
	
	protected static void writeExcel(File file) throws Exception {
		
		GearController gearControl = gear.getController(Concise.getCurrentWorkspace());
		if (gearControl instanceof IGearTableBased &&
			gearControl.getControl() instanceof Table) {
			
			ExcelWriter excel = new ExcelWriter(gear.label());
			
			Table table = (Table) gearControl.getControl();
			TableColumn[] cols = table.getColumns();
			String header[] = new String[cols.length];
			for (int i=0; i<cols.length; i++) {
				header[i] = cols[i].getText();
			}
			excel.createHeader(header);
			
			for (int i=0; i<table.getItemCount() && i<1048575; i++) {
				excel.createRow();
				String[] texts = ((IGearTableBased) gearControl).getItemTexts(i);
				for (int c=0; c<texts.length; c++) {
					
					DBColumn dbColumn = ((DBColumn) table.getColumns()[c].getData(GearController._DB_COLUMN));
					if (dbColumn == null) {
						excel.createCell(c, Double.valueOf(texts[c].replace(",", "")));
					}
					else {
						switch (dbColumn.getDataType()) {
						case INTEGER:
						case BIGINT:
							excel.createCell(c, Double.valueOf(texts[c].replace(",", "")));
							break;
						case DOUBLE:
							excel.createCell(c, Double.valueOf(texts[c].replace(",", "")), Style.NUMBER_PERCESION);
							break;
						
						case VARCHAR:
						default:
							excel.createCell(c, texts[c]);
							break;
						}
					}
				}
				
				// Concordance Plotter has to handle image
				if (gear == Gear.ConcordancePlotter) {
					ImageLoader loader = new ImageLoader();
					loader.data = new ImageData[] { Concise.getData().plotDataList.get(i).getPlotImage(SWT.DEFAULT, SWT.DEFAULT).getImageData() };
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					loader.save(baos, SWT.IMAGE_PNG);
					excel.createCell(1, baos, Style.PICTURE_TYPE_PNG);
					baos.close();
				}
			}
			
			excel.write(file);
			excel = null;
			
			if (table.getItemCount() > 1048575) {
				// Max records of Excel
				Dialog.inform("Table not written completely!", "Max 1,048,575 Rows Reached!");
			}
		}
	}
	
	
	protected static void writeGexf(File file) throws Exception
	{

		Gexf gexf = new GexfImpl();
		Calendar date= Calendar.getInstance();
		
		gexf.getMetadata()
				.setLastModified(date.getTime())
				.setCreator("Concise")
				.setDescription("Collocational Network");
		
		Graph graph = gexf.getGraph();
		graph.setDefaultEdgeType(EdgeType.UNDIRECTED).setMode(Mode.STATIC);
		
		// node attributes
		AttributeList nodeAttrList = new AttributeListImpl(AttributeClass.NODE);
		graph.getAttributeLists().add(nodeAttrList);
		
		// edge attributes
		AttributeList edgeAttrList = new AttributeListImpl(AttributeClass.EDGE);
		graph.getAttributeLists().add(edgeAttrList);
				
		NetworkGraph network = (NetworkGraph) Gear.CollocationalNetworker.getController(Concise.getCurrentWorkspace()).getControl();
		
		// nodes
		List<GraphNode> nodeList = network.getNodes();
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = graph.createNode(String.valueOf(i));
			node
				.setLabel(nodeList.get(i).getText());
		}
		
		// edges
		List<GraphConnection> connectionList = network.getConnections();
		for (GraphConnection connection : connectionList) 
		{
			
			Node sourceNode = graph.getNodes().get(nodeList.indexOf(connection.getSource()));
			Node targetNode = graph.getNodes().get(nodeList.indexOf(connection.getDestination()));
			Edge edge = sourceNode.connectTo(targetNode);
			edge
				.setLabel(connection.getSource().getText() + " - " + connection.getDestination().getText());
		}
		
		StaxGraphWriter graphWriter = new StaxGraphWriter();
		Writer out = new FileWriter(file, false);
		graphWriter.writeToStream(gexf, out, "UTF-8");
		
	}
	
	
	
	protected static void writeXgmml(File file) throws Exception
	{
		Document d = DocumentHelper.createDocument();
		
		Element graph = 
			d.addElement("graph", "http://www.cs.rpi.edu/XGMML")
				.addAttribute("label", "Concise Collocational Network")
				.addAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1")
				.addAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink")
				.addAttribute("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
				.addAttribute("xmlns:cy", "http://www.cytoscape.org")
				.addAttribute("undirected", "1");
		graph
			.addElement("att")
			.addAttribute("type", "string")
			.addAttribute("name", "backgroundColor")
			.addAttribute("value", "#ffffff");
		graph
			.addElement("att")
			.addAttribute("type", "real")
			.addAttribute("name", "GRAPH_VIEW_ZOOM")
			.addAttribute("value", "0.2854838709677419");
		graph
			.addElement("att")
			.addAttribute("type", "real")
			.addAttribute("name", "GRAPH_VIEW_CENTER_X")
			.addAttribute("value", "560.0");
		graph
			.addElement("att")
			.addAttribute("type", "real")
			.addAttribute("name", "GRAPH_VIEW_CENTER_Y")
			.addAttribute("value", "600.0");
		graph
			.addElement("att")
			.addAttribute("type", "string")
			.addAttribute("name", "__layoutAlgorithm")
			.addAttribute("value", "grid")
			.addAttribute("cy:hidden", "true");
		
		
		NetworkGraph network = (NetworkGraph) Gear.CollocationalNetworker.getController(Concise.getCurrentWorkspace()).getControl();
		
		// nodes
		List<GraphNode> nodeList = network.getNodes();
		int cols = (int) Math.round( Math.sqrt( (double)nodeList.size() ) );
		int row = -1;
		for (int i = 0; i < nodeList.size(); i++) 
		{
			if (i % cols == 0) row++;
			int x = (i % cols) * 80;
			int y = row * 80;
			
			String label = nodeList.get(i).getText();
			String id = String.valueOf(i);
			
			Element node = graph.addElement("node")
					.addAttribute("id", id)
					.addAttribute("label", label);
			
			// node attr
			node.addElement("att")
				.addAttribute("type", "string")
				.addAttribute("name", "canonicalName")
				.addAttribute("value", id);
			node.addElement("graphics")
				.addAttribute("type", "ELLIPSE")
				.addAttribute("h","40.0")
				.addAttribute("w", "40.0")
				.addAttribute("x", String.valueOf(x)+".0")
				.addAttribute("y", String.valueOf(y)+".0")
				.addAttribute("fill", "#ff9999")
				.addAttribute("width", "1")
				.addAttribute("outline", "#666666")
				.addAttribute("cy:nodeTransparency", "1.0")
				.addAttribute("cy:nodeLabelFont", "SansSerif.bold-0-12")
				.addAttribute("cy:borderLineType", "solid");				
		}
		
		
		// edges
		List<GraphConnection> connectionList = network.getConnections();
		for (GraphConnection connection : connectionList) 
		{
			String sourceID = String.valueOf(nodeList.indexOf(connection.getSource()));
			String targetID = String.valueOf(nodeList.indexOf(connection.getDestination())); 
			String label = connection.getSource().getText() + " - " + connection.getDestination();
				
			Element edge = graph.addElement("edge")
				.addAttribute("source", sourceID)
				.addAttribute("target", targetID)
				.addAttribute("label", label);
					
			// edge attr
			edge.addElement("att")
				.addAttribute("type", "string")
				.addAttribute("name", "canonicalName")
				.addAttribute("value", label);
			edge.addElement("att")
				.addAttribute("type", "string")
				.addAttribute("name", "interaction")
				.addAttribute("value", "with");
			edge.addElement("att")
				.addAttribute("type", "real")
				.addAttribute("name", "weight")
				.addAttribute("value", String.valueOf(connection.getWeightInLayout()));
			edge.addElement("graphics")
				.addAttribute("width", "1")
				.addAttribute("fill", "#0000ff")
				.addAttribute("cy:sourceArrow", "0")
				.addAttribute("cy:targetArrow", "0")
				.addAttribute("cy:sourceArrowColor", "#000000")
				.addAttribute("cy:targetArrowColor", "#000000")
				.addAttribute("cy:edgeLabelFont", "Default-0-10")
				.addAttribute("cy:edgeLineType", "SOLID")
				.addAttribute("cy:curved", "STRAIGHT_LINES");
		}
		
		XMLWriter writer = new XMLWriter(new FileOutputStream(file), OutputFormat.createPrettyPrint());
		writer.write(d);
		writer.close();
		d.clearContent();
	}
	
	
	protected static void writeNet(File file) throws Exception 
	{
		NetworkGraph network = (NetworkGraph) Gear.CollocationalNetworker.getController(Concise.getCurrentWorkspace()).getControl();
		
		List<GraphNode> nodeList = network.getNodes();
		List<GraphConnection> connectionList = network.getConnections();
		
		PrintWriter pw = new PrintWriter(
				new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(file),
											   "UTF-8")));
		// *Vertices
		pw.write("*Vertices " + nodeList.size() + "\r\n");
		for (int i = 0; i < nodeList.size(); i++) {
			pw.write(String.valueOf(i) + " \"" + nodeList.get(i).getText() + "\" \r\n");
		}
		
		// *Arcs
		pw.write("\r\n");
		pw.write("*Arcs \r\n");
		for (GraphConnection connection : connectionList) {
			pw.write(nodeList.indexOf(connection.getSource()) + " " + nodeList.indexOf(connection.getDestination()) + "\r\n");
		}
		pw.close();
		pw = null;
	}
	
	
	protected static void writeDL(File file) throws Exception 
	{
		NetworkGraph network = (NetworkGraph) Gear.CollocationalNetworker.getController(Concise.getCurrentWorkspace()).getControl();
		
		List<GraphNode> nodeList = network.getNodes();
		List<GraphConnection> connectionList = network.getConnections();
		
		PrintWriter pw = new PrintWriter(
				new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(file),
											   "UTF-8")));
		
		pw.write("dl n=" + nodeList.size() + "\r\n");
		pw.write("format: edgelist1\r\n");
		pw.write("\r\n");
		
		// labels
		pw.write("labels:\r\n");
		
		for (GraphNode node : nodeList) {
			pw.write(node.getText()+"\r\n");
		}
		
		// edges
		pw.write("\r\n");
		pw.write("data: \r\n");
		for (GraphConnection connection : connectionList) {			
			pw.write(nodeList.indexOf(connection.getSource()) + " " + nodeList.indexOf(connection.getDestination()) + " \r\n");
		}
		
		pw.close();
		pw = null;
	}
}
