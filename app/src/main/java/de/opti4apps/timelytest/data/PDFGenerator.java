package de.opti4apps.timelytest.data;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PDFGenerator {

    Context c;

    public PDFGenerator(Context _c) {
        c = _c;
    }

    Font regular = new Font(Font.FontFamily.HELVETICA, 10);
    Font bold = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);

    int EXCEPT_TOP = Rectangle.BOTTOM| Rectangle.RIGHT| Rectangle.LEFT;
    int EXCEPT_BOTTOM = Rectangle.TOP| Rectangle.RIGHT| Rectangle.LEFT;

    private String path;
    private File dir;
    private File file;

    Document doc;

    public void createPDF(String _path) throws FileNotFoundException, DocumentException {

        path = _path;
        dir = new File(path);

        //create document file
        try {

            configureDocument();

            //open the document
            doc.open();

            createHeadline();

            PdfPTable holderTable = new PdfPTable(3);
            holderTable.setWidthPercentage(100.0F);
            holderTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            holderTable.setWidths(new int[] {80,1,19});

            PdfPTable mainTable = getMainTable();

            PdfPCell firstTableCell = new PdfPCell();
            firstTableCell.setPadding(0f);
            firstTableCell.setBorder(PdfPCell.NO_BORDER);
            firstTableCell.addElement(mainTable);
            holderTable.addCell(firstTableCell);

            PdfPCell spacer = getCell("",1,1);
            spacer.setPadding(0f);
            spacer.setBorder(PdfPCell.NO_BORDER);
            holderTable.addCell(spacer);

            PdfPTable sidebarTable = getSidebarTable();

            PdfPCell secondTableCell = new PdfPCell();
            secondTableCell.setPadding(0f);
            secondTableCell.setBorder(PdfPCell.NO_BORDER);
            secondTableCell.addElement(sidebarTable);
            holderTable.addCell(secondTableCell);

            doc.add(holderTable);

            Toast.makeText(c, "Time sheet Created and stored in device", Toast.LENGTH_LONG).show();

            doc.close();

        } catch (DocumentException de) {
            Log.e("PDFCreator", "DocumentException:" + de);
        }
    }

    private void configureDocument() throws FileNotFoundException, DocumentException {
        doc = new Document(PageSize.A4.rotate());
        doc.setMargins(25,25,25,25);

        Log.e("PDFCreator", "PDF Path: " + path);
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        file = new File(dir, "UniTyLab_Employees_Timesheet" + sdf.format(Calendar.getInstance().getTime()) + ".pdf");
        FileOutputStream fOut = new FileOutputStream(file);
        PdfWriter writer = PdfWriter.getInstance(doc, fOut);
    }

    private void createHeadline() throws DocumentException {
        //Create headline
        Paragraph p1 =new Paragraph("Working Time Page",new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
        p1.setAlignment(Element.ALIGN_LEFT);
        p1.setSpacingAfter(10);
        doc.add(p1);
    }

    //MAIN TABLE

    private PdfPTable getMainTable() throws DocumentException {
        PdfPTable firstTable = new PdfPTable(14);
        firstTable.setWidthPercentage(100.0F);
        firstTable.setHorizontalAlignment(Element.ALIGN_LEFT);

        firstTable.setWidths(new float[] {1,3,2.5f,2, 2,2,2,2, 2,2,2,2, 2.5f,5});

        //Header
        PdfPCell hCell = getCell("Juni 2016",2,2,new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
        hCell.setBorderWidthTop(1.5f);
        hCell.setBorderWidthBottom(1.5f);
        firstTable.addCell(hCell);

        firstTable.addCell(getHeaderCell("Beginn",1,1, EXCEPT_BOTTOM, true));
        firstTable.addCell(getHeaderCell("Mittagspause",1,2, EXCEPT_BOTTOM, true));

        firstTable.addCell(getHeaderCell("zusätzl.Pause",1,2, EXCEPT_BOTTOM, true));
        firstTable.addCell(getHeaderCell("Ende",1,1, EXCEPT_BOTTOM, true));
        firstTable.addCell(getHeaderCell("Ist-Std",1,1, EXCEPT_BOTTOM, true));
        firstTable.addCell(getHeaderCell("Soll-Std.",1,1, EXCEPT_BOTTOM, true));
        firstTable.addCell(getHeaderCell("+/- Std.",1,1, EXCEPT_BOTTOM, true));
        firstTable.addCell(getHeaderCell("AZA",1,1, EXCEPT_BOTTOM, true));
        firstTable.addCell(getHeaderCell("Übertrag",1,1, EXCEPT_BOTTOM, true));
        firstTable.addCell(getHeaderCell("Bemerkungen",1,1, EXCEPT_BOTTOM, true));

        firstTable.addCell(getHeaderCell("",1,1, EXCEPT_TOP, false));
        firstTable.addCell(getHeaderCell("von",1,1, Rectangle.BOTTOM| Rectangle.LEFT, false));
        firstTable.addCell(getHeaderCell("bis",1,1, Rectangle.BOTTOM| Rectangle.RIGHT, false));
        firstTable.addCell(getHeaderCell("von",1,1, Rectangle.BOTTOM| Rectangle.LEFT, false));
        firstTable.addCell(getHeaderCell("bis",1,1, Rectangle.BOTTOM| Rectangle.RIGHT, false));
        firstTable.addCell(getHeaderCell("",1,1, EXCEPT_TOP, false));
        firstTable.addCell(getHeaderCell("",1,1, EXCEPT_TOP, false));
        firstTable.addCell(getHeaderCell("",1,1, EXCEPT_TOP, false));
        firstTable.addCell(getHeaderCell("",1,1, EXCEPT_TOP, false));
        firstTable.addCell(getHeaderCell("",1,1, EXCEPT_TOP, false));
        firstTable.addCell(getHeaderCell("7:25",1,1, EXCEPT_TOP, false));
        firstTable.addCell(getHeaderCell("",1,1, EXCEPT_TOP, false));

        for (int i = 0;i<14;i++) {
            PdfPCell cell = getCell(" ",1,1);
            cell.setBackgroundColor(WebColors.getRGBColor("#808080"));
            firstTable.addCell(cell);
        }

        String[] days = new String[] {"Mo","Di","Mi","Do","Fr","Sa","So"};

        for (int i = 1;i<=31;i++) {
            boolean blue = (days[i%7].equals("Sa") || days[i%7].equals("So"));
            firstTable.addCell(getTupleCell(days[i%7],1,1,blue));
            firstTable.addCell(getTupleCell(String.format("%02d.06.16", i),1,1,blue));
            firstTable.addCell(getTupleCell("08:30",1,1,blue));
            firstTable.addCell(getTupleCell("12:00",1,1,blue));
            firstTable.addCell(getTupleCell("13:00",1,1,blue));
            firstTable.addCell(getTupleCell("",1,1,blue));
            firstTable.addCell(getTupleCell("",1,1,blue));
            firstTable.addCell(getTupleCell("18:00",1,1,blue));
            firstTable.addCell(getTupleCell("08:30",1,1,blue));
            firstTable.addCell(getTupleCell("06:00",1,1,blue));
            firstTable.addCell(getTupleCell("02:30",1,1,blue));
            firstTable.addCell(getTupleCell("00:00",1,1,blue));
            firstTable.addCell(getTupleCell("9:55",1,1,blue));
            firstTable.addCell(getTupleCell("",1,1,blue));

        }

        //Footer
        for (int i = 0;i<8;i++) {
            PdfPCell cell = getCell("",1,1);
            cell.setBorder(PdfPCell.NO_BORDER);
            firstTable.addCell(cell);
        }
        PdfPCell footerCell1 = getCell("Übertrag:",1,4,bold);
        footerCell1.setBorder(Rectangle.BOTTOM| Rectangle.LEFT);
        footerCell1.setHorizontalAlignment(Element.ALIGN_LEFT);
        firstTable.addCell(footerCell1);
        PdfPCell footerCell2 = getCell("8:46",1,1,bold);
        footerCell2.setBorder(Rectangle.BOTTOM| Rectangle.RIGHT);
        firstTable.addCell(footerCell2);
        PdfPCell ecell = getCell("",1,1);
        ecell.setBorder(PdfPCell.NO_BORDER);
        firstTable.addCell(ecell);

        return firstTable;
    }

    BaseColor nameBGColor = WebColors.getRGBColor("#FDD4B4");
    BaseColor titleBGColor = WebColors.getRGBColor("#FFFF01");
    BaseColor titleTextColor = WebColors.getRGBColor("#BA445B");

    Font sidebarRegular = new Font(Font.FontFamily.HELVETICA, 10);
    Font sidebarBold = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    Font sidebarFineprint = new Font(Font.FontFamily.HELVETICA, 8);

    private PdfPTable getSidebarTable() {
        PdfPTable secondTable = new PdfPTable(1);
        secondTable.setWidthPercentage(100.0F);
        secondTable.setHorizontalAlignment(Element.ALIGN_LEFT);

        secondTable.addCell(getSidebarCell("John Doe",sidebarBold,nameBGColor, BaseColor.BLACK,30f));
        secondTable.addCell(getSidebarCell("Schritt 1",sidebarBold,titleBGColor,titleTextColor,null));

        Paragraph p1 = new Paragraph();
        p1.setAlignment(Element.ALIGN_CENTER);
        p1.add(getChunk("Wenn Zellen ", BaseColor.BLACK,sidebarBold));
        p1.add(getChunk("rot ",titleTextColor,sidebarBold));
        p1.add(getChunk("erscheinen,", BaseColor.BLACK,sidebarBold));
        p1.add(Chunk.NEWLINE);
        p1.add(getChunk("dann prüfen Sie bitte lhre", BaseColor.BLACK,sidebarBold));
        p1.add(Chunk.NEWLINE);
        p1.add(getChunk("Einträge nach den folgenden", BaseColor.BLACK,sidebarBold));
        p1.add(Chunk.NEWLINE);
        p1.add(getChunk("Kriterien:", BaseColor.BLACK,sidebarBold));
        p1.add(Chunk.NEWLINE);

        p1.add(getChunk("Rahmenarbeitszeit",titleTextColor,sidebarBold));
        p1.add(Chunk.NEWLINE);
        p1.add(getChunk("Beginn: 7:00 Uhr", BaseColor.BLACK,sidebarRegular));
        p1.add(Chunk.NEWLINE);
        p1.add(getChunk("Ende: 19:30 Uhr", BaseColor.BLACK,sidebarRegular));
        p1.add(Chunk.NEWLINE);

        p1.add(getChunk("tägliche Höchstarbeitszeit",titleTextColor,sidebarBold));
        p1.add(Chunk.NEWLINE);
        p1.add(getChunk("10 Stunden", BaseColor.BLACK,sidebarRegular));
        p1.add(Chunk.NEWLINE);

        p1.add(getChunk("Pausenzeiten",titleTextColor,sidebarBold));
        p1.add(Chunk.NEWLINE);
        p1.add(getChunk("Bei mehr als 6h = mind. 30 Min.", BaseColor.BLACK,sidebarRegular));
        p1.add(Chunk.NEWLINE);
        p1.add(getChunk("*Bei mehr als 9h = mind. 45 Min.", BaseColor.BLACK,sidebarRegular));
        p1.add(Chunk.NEWLINE);

        p1.add(getChunk("(*ausgenommen Beamtinnen/Beamte)", BaseColor.BLACK,sidebarFineprint));
        p1.add(Chunk.NEWLINE);

        p1.setSpacingAfter(12f);
        secondTable.addCell(getSidebarCell(p1, BaseColor.WHITE,null));

        secondTable.addCell(getSidebarCell("Schritt 2",sidebarBold,titleBGColor,titleTextColor,null));

        Paragraph p2 = new Paragraph();
        p2.setAlignment(Element.ALIGN_CENTER);
        p2.add(getChunk("Die/der ",titleTextColor,sidebarBold));
        p2.add(getChunk("Beschäftigte",titleTextColor,new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD| Font.UNDERLINE)));
        p2.add(Chunk.NEWLINE);
        p2.add(getChunk("bestätigt die Richtigkeit", BaseColor.BLACK,sidebarBold));
        p2.add(Chunk.NEWLINE);
        p2.add(getChunk("der gemachten Angaben.", BaseColor.BLACK,bold));
        p2.add(Chunk.NEWLINE);

        PdfPCell p2Cell = getSidebarCell(p2, BaseColor.WHITE,100f);
        p2Cell.setVerticalAlignment(Element.ALIGN_TOP);
        p2Cell.setBorder(EXCEPT_BOTTOM);
        secondTable.addCell(p2Cell);

        PdfPCell p2Footer = getSidebarCell("Datum/Unterschrift)",sidebarBold, BaseColor.WHITE, BaseColor.BLACK,null);
        p2Footer.setBorderWidthTop(0.5f);

        return secondTable;
    }

    //CONVENIENCE METHODS

    private Chunk getChunk(String text, BaseColor textColor, Font _font) {
        Font font = new Font(_font);
        font.setColor(textColor);
        return new Chunk(text, font);
    }

    private PdfPCell getSidebarCell(Paragraph text, BaseColor background, Float height) {
        PdfPCell hCell = getSidebarCell(null,sidebarRegular,background, BaseColor.BLACK,height);
        hCell.addElement(text);
        return hCell;
    }

    private PdfPCell getSidebarCell(String text, Font _font, BaseColor background, BaseColor textColor, Float height) {
        Font font = new Font(_font);
        font.setColor(textColor);
        PdfPCell hCell = getCell(text,1,1,font);
        if (height!=null) {
            hCell.setFixedHeight(height);
        }
        hCell.setBorderWidth(1.5f);
        hCell.setBackgroundColor(background);
        return hCell;
    }

    private PdfPCell getHeaderCell(String text, int rowspan, int colspan, int border, boolean top) {
        PdfPCell hCell = getCell(text,rowspan,colspan,border);
        if (top) {
            hCell.setBorderWidthTop(1.5f);
        }
        else {
            hCell.setBorderWidthBottom(1.5f);
            hCell.setPaddingBottom(3.0f);
        }
        return hCell;
    }

    private PdfPCell getTupleCell(String text, int rowspan, int colspan, boolean blue) {
        //#C5D9F1
        PdfPCell hCell = getCell(text,rowspan,colspan);
        if (blue) {
            hCell.setBackgroundColor(WebColors.getRGBColor("#C5D9F1"));
        }
        return hCell;
    }

    private PdfPCell getCell(String text, int rowspan, int colspan) {
        return getCell(text,rowspan,colspan,regular);
    }

    private PdfPCell getCell(String text, int rowspan, int colspan, Font font) {
        return getCell(text,rowspan,colspan,font, PdfPCell.BOX);
    }

    private PdfPCell getCell(String text, int rowspan, int colspan, int border) {
        return getCell(text,rowspan,colspan,regular,border);
    }

    private PdfPCell getCell(String text, int rowspan, int colspan, Font font, int border) {
        PdfPCell cell;
        if (text==null) {
            cell = new PdfPCell();
        }
        else {
            cell = new PdfPCell(new Phrase(text, font));
        }
        cell.setRowspan(rowspan);
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(border);
        return cell;
    }
}
