package de.opti4apps.timelytest.data;

import android.app.Activity;
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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.opti4apps.timelytest.App;
import io.objectbox.Box;
import io.objectbox.query.Query;

public class PDFGenerator {

    Activity c;

    public PDFGenerator(Activity _c) {
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
            loadData();
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

            PdfPCell spacer = getCell(null,1,1);
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

    private User currentUser;
    private Box<User> usersBox;

    private List<Day> mDayList = new ArrayList<>();
    private Box<Day> mDayBox;
    private Query<Day> mDayQuery;

    DateTime selectedMonth;
    DateTime startDate, endDate;

    private void loadData() {
        usersBox = ((App) c.getApplication()).getBoxStore().boxFor(User.class);
        currentUser = UserManager.getSignedInUser(usersBox);
        mDayBox = ((App) c.getApplication()).getBoxStore().boxFor(Day.class);
        DateTime dt = DateTime.now();
        selectedMonth = new DateTime(dt.getYear(),dt.getMonthOfYear(),1,0,0);
        startDate = selectedMonth.withDayOfMonth(1);
        endDate = selectedMonth.withDayOfMonth(1).plusMonths(1).minusDays(1);
        mDayQuery = mDayBox.query().between(Day_.day, startDate.toDate(), endDate.toDate()).orderDesc(Day_.day).build();
        mDayList.addAll(mDayQuery.find());
        mDayList.size();
    }

    private Day findDay(Integer dom) {
        for (Day d : mDayList) {
            if (d.getDay().dayOfMonth().get()==dom){
                return d;
            }
        }
        return null;
    }

    private void createHeadline() throws DocumentException {
        //Create headline
        Paragraph p1 =new Paragraph("Working Time Page",new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
        p1.setAlignment(Element.ALIGN_LEFT);
        p1.setSpacingAfter(10);
        doc.add(p1);
    }

    //MAIN TABLE

    private String[] germanDays = new String[] {"Mo","Di","Mi","Do","Fr","Sa","So"};
    private String[] germanMonths = new String[] {"Jän","Feb","März","Apr","Mai","Juni","Juli","Aug","Sept","Okt","Nov","Dez"};

    private PdfPTable getMainTable() throws DocumentException {
        PdfPTable firstTable = new PdfPTable(14);
        firstTable.setWidthPercentage(100.0F);
        firstTable.setHorizontalAlignment(Element.ALIGN_LEFT);

        firstTable.setWidths(new float[] {1,2.5f,2,2, 2,2,2,2, 2,2,2,2, 2.5f,6});

        firstTable = populateMainTableHeader(firstTable);

        for (int i = 0;i<14;i++) {
            PdfPCell cell = getCell(" ",1,1);
            cell.setBackgroundColor(WebColors.getRGBColor("#808080"));
            firstTable.addCell(cell);
        }

        for (int i = 1;i<=31;i++) {
            if (endDate.dayOfMonth().get()>=i) {
                Day thisDay = findDay(i);
                DateTime date;
                if (thisDay == null) {
                    date = startDate.withDayOfMonth(i);
                } else {
                    date = thisDay.getDay();
                }
                boolean blue = (date.dayOfWeek().get() == 6 || date.dayOfWeek().get() == 7);
                firstTable.addCell(getTupleCell(germanDays[date.dayOfWeek().get() - 1], 1, 1, blue));
                DateTimeFormatter fmt = DateTimeFormat.forPattern("dd.MM.yy");
                firstTable.addCell(getTupleCell(date.toString(fmt), 1, 1, blue));

                String beginn = "", mVon = "", mBis = "", zVon = "", zBis = "", ende = "";
                if (thisDay != null) {
                    beginn = thisDay.getStart().toString(Day.TIME_FORMATTER);
                    ende = thisDay.getEnd().toString(Day.TIME_FORMATTER);

                    DateTime breakstart = thisDay.getStart().plusHours(1);
                    mVon = breakstart.toString(Day.TIME_FORMATTER);
                    mBis = breakstart.plus(thisDay.getPause()).toString(Day.TIME_FORMATTER);
                }

                firstTable.addCell(getTupleCell(beginn, 1, 1, blue));
                firstTable.addCell(getTupleCell(mVon, 1, 1, blue));
                firstTable.addCell(getTupleCell(mBis, 1, 1, blue));
                firstTable.addCell(getTupleCell(zVon, 1, 1, blue));
                firstTable.addCell(getTupleCell(zBis, 1, 1, blue));
                firstTable.addCell(getTupleCell(ende, 1, 1, blue));
                firstTable.addCell(getTupleCell(" ", 1, 1, blue));
                firstTable.addCell(getTupleCell(" ", 1, 1, blue));
                firstTable.addCell(getTupleCell(" ", 1, 1, blue));
                firstTable.addCell(getTupleCell(" ", 1, 1, blue));
                firstTable.addCell(getTupleCell(" ", 1, 1, blue));
                firstTable.addCell(getTupleCell(" ", 1, 1, blue));
            }
            else {
                firstTable = addEmptyTupleCells(firstTable,14,false);
            }
        }

        //Footer
        for (int i = 0;i<8;i++) {
            PdfPCell cell = getCell(null,1,1);
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
        PdfPCell ecell = getCell(null,1,1);
        ecell.setBorder(PdfPCell.NO_BORDER);
        firstTable.addCell(ecell);

        return firstTable;
    }

    private PdfPTable populateMainTableHeader(PdfPTable firstTable) {
        Font monthFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD);
        PdfPCell hCell = getCell(null,2,2,monthFont);
        hCell.setLeading(0,0);
        Paragraph p = new Paragraph();
        p.setAlignment(Element.ALIGN_CENTER);
        p.setLeading(0,1f);
        p.setSpacingBefore(0);
        p.setSpacingAfter(0);
        String month = germanMonths[selectedMonth.monthOfYear().get()-1];
        p.add(new Chunk(month,monthFont));
        p.add(Chunk.NEWLINE);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy");
        p.add(new Chunk(selectedMonth.toString(fmt),monthFont));
        hCell.setPadding(4.0f);

        hCell.setLeading(0,0);
        hCell.addElement(p);
        hCell.setBorderWidthTop(1.5f);
        hCell.setBorderWidthBottom(1.5f);
        firstTable.addCell(hCell);

        firstTable.addCell(getHeaderCell("Beginn",1,1, EXCEPT_BOTTOM, true));
        firstTable.addCell(getHeaderCell("Mittagspause",1,2, EXCEPT_BOTTOM, true));

        firstTable.addCell(getHeaderCell("zusatzl.Pause",1,2, EXCEPT_BOTTOM, true));
        firstTable.addCell(getHeaderCell("Ende",1,1, EXCEPT_BOTTOM, true));
        firstTable.addCell(getHeaderCell("Ist-Std",1,1, EXCEPT_BOTTOM, true));
        firstTable.addCell(getHeaderCell("Soll-Std",1,1, EXCEPT_BOTTOM, true));
        firstTable.addCell(getHeaderCell("+/- Std",1,1, EXCEPT_BOTTOM, true));
        firstTable.addCell(getHeaderCell("AZA",1,1, EXCEPT_BOTTOM, true));
        firstTable.addCell(getHeaderCell("Übertrag",1,1, EXCEPT_BOTTOM, true));
        firstTable.addCell(getHeaderCell("Bemerkungen",1,1, EXCEPT_BOTTOM, true));

        firstTable.addCell(getHeaderCell(" ",1,1, EXCEPT_TOP, false));
        firstTable.addCell(getHeaderCell("von",1,1, Rectangle.BOTTOM| Rectangle.LEFT, false));
        firstTable.addCell(getHeaderCell("bis",1,1, Rectangle.BOTTOM| Rectangle.RIGHT, false));
        firstTable.addCell(getHeaderCell("von",1,1, Rectangle.BOTTOM| Rectangle.LEFT, false));
        firstTable.addCell(getHeaderCell("bis",1,1, Rectangle.BOTTOM| Rectangle.RIGHT, false));
        firstTable.addCell(getHeaderCell(" ",1,1, EXCEPT_TOP, false));
        firstTable.addCell(getHeaderCell(" ",1,1, EXCEPT_TOP, false));
        firstTable.addCell(getHeaderCell(" ",1,1, EXCEPT_TOP, false));
        firstTable.addCell(getHeaderCell(" ",1,1, EXCEPT_TOP, false));
        firstTable.addCell(getHeaderCell(" ",1,1, EXCEPT_TOP, false));
        firstTable.addCell(getHeaderCell("7:25",1,1, EXCEPT_TOP, false));
        firstTable.addCell(getHeaderCell(" ",1,1, EXCEPT_TOP, false));

        return firstTable;
    }

    private PdfPTable addEmptyTupleCells(PdfPTable table, int count, boolean blue) {
        for (int i = 0; i < count; i++) {
            table.addCell(getTupleCell(" ", 1, 1, blue));
        }
        return table;
    }

    BaseColor nameBGColor = WebColors.getRGBColor("#FDD4B4");
    BaseColor titleBGColor = WebColors.getRGBColor("#FFFF01");
    BaseColor titleTextColor = WebColors.getRGBColor("#BA445B");

    Font sidebarRegular = new Font(Font.FontFamily.HELVETICA, 9);
    Font sidebarBold = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
    Font sidebarFineprint = new Font(Font.FontFamily.HELVETICA, 8);

    private PdfPTable getSidebarTable() {
        PdfPTable secondTable = new PdfPTable(1);
        secondTable.setWidthPercentage(100.0F);
        secondTable.setHorizontalAlignment(Element.ALIGN_LEFT);

        String name = currentUser.getFirstName() + " " + currentUser.getLastName();
        Font nameFont = new Font(sidebarBold);
        nameFont.setSize(13);
        secondTable.addCell(getSidebarCell(name,nameFont,nameBGColor, BaseColor.BLACK,35f));
        PdfPCell tCell = getSidebarCell("Schritt 1",sidebarBold,titleBGColor,titleTextColor,null);
        tCell.setPaddingBottom(4.0f);
        secondTable.addCell(tCell);

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

        p1.setSpacingAfter(10f);
        secondTable.addCell(getSidebarCell(p1, BaseColor.WHITE,null));

        tCell = getSidebarCell("Schritt 2",sidebarBold,titleBGColor,titleTextColor,null);
        tCell.setPaddingBottom(5.0f);
        secondTable.addCell(tCell);

        Paragraph p2 = new Paragraph();
        p2.setAlignment(Element.ALIGN_CENTER);
        p2.add(getChunk("Die/der ",titleTextColor,sidebarBold));
        p2.add(getChunk("Beschäftigte",titleTextColor,new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD| Font.UNDERLINE)));
        p2.add(Chunk.NEWLINE);
        p2.add(getChunk("bestätigt die Richtigkeit", BaseColor.BLACK,sidebarBold));
        p2.add(Chunk.NEWLINE);
        p2.add(getChunk("der gemachten Angaben.", BaseColor.BLACK,bold));
        p2.add(Chunk.NEWLINE);

        PdfPCell p2Cell = getSidebarCell(p2, BaseColor.WHITE,60f);
        p2Cell.setVerticalAlignment(Element.ALIGN_TOP);
        p2Cell.setBorder(EXCEPT_BOTTOM);
        secondTable.addCell(p2Cell);

        PdfPCell p2Footer = getSidebarCell("Datum/Unterschrift)",sidebarBold, BaseColor.WHITE, BaseColor.BLACK,null);
        p2Footer.setPaddingBottom(5.0f);
        p2Footer.setBorder(EXCEPT_TOP);
        p2Footer.setCellEvent(new DottedCell(PdfPCell.TOP));
        secondTable.addCell(p2Footer);

        tCell = getSidebarCell("Schritt 3",sidebarBold,titleBGColor,titleTextColor,null);
        tCell.setPaddingBottom(5.0f);
        secondTable.addCell(tCell);

        p2 = new Paragraph();
        p2.setAlignment(Element.ALIGN_CENTER);
        p2.add(getChunk("Die/der ",titleTextColor,sidebarBold));
        p2.add(getChunk("Vorgesetzte",titleTextColor,new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD| Font.UNDERLINE)));
        p2.add(Chunk.NEWLINE);
        p2.add(getChunk("bestätigt die Einhaltung", BaseColor.BLACK,sidebarBold));
        p2.add(Chunk.NEWLINE);
        p2.add(getChunk("der arbeitsrechtlichen", BaseColor.BLACK,bold));
        p2.add(Chunk.NEWLINE);
        p2.add(getChunk("Regelungen.", BaseColor.BLACK,bold));
        p2.add(Chunk.NEWLINE);

        p2Cell = getSidebarCell(p2, BaseColor.WHITE,75f);
        p2Cell.setVerticalAlignment(Element.ALIGN_TOP);
        p2Cell.setBorder(EXCEPT_BOTTOM);
        secondTable.addCell(p2Cell);

        p2Footer = getSidebarCell("Datum/Unterschrift)",sidebarBold, BaseColor.WHITE, BaseColor.BLACK,null);
        p2Footer.setPaddingBottom(5.0f);
        p2Footer.setBorder(EXCEPT_TOP);
        p2Footer.setCellEvent(new DottedCell(PdfPCell.TOP));
        secondTable.addCell(p2Footer);

        PdfPCell footer = getSidebarCell(" ",sidebarBold,nameBGColor, BaseColor.BLACK,null);
        footer.setBorder(Rectangle.TOP| Rectangle.BOTTOM);
        secondTable.addCell(footer);

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
            hCell.setPaddingBottom(6.0f);
            hCell.setPaddingTop(6.0f);
        }
        return hCell;
    }
    private PdfPCell getTupleCell(String text, int rowspan, int colspan, boolean blue) {
        //#C5D9F1
        PdfPCell hCell = getCell(text,rowspan,colspan);
        hCell.setPaddingBottom(3.0f);
        hCell.setLeading(0,0.9f);
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
