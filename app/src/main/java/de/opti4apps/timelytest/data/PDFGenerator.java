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
import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.opti4apps.timelytest.App;
import de.opti4apps.timelytest.shared.TimelyHelper;
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

    private Calendar reportDate;
    private Calendar reportEndDate;

    Document doc;

    private User currentUser;
    private Box<User> usersBox;

    private List<Day> mDayList = new ArrayList<>();
    private Box<Day> mDayBox;
    private Query<Day> mDayQuery;

    private WorkProfile mCurrentWorkProfile;

    Date startDate, endDate;

    Map<Integer, String> germanDays = new HashMap<>();

    private String[] germanMonths = new String[] {"Jän","Feb","März","Apr","Mai","Juni","Juli","Aug","Sept","Okt","Nov","Dez"};

    PeriodFormatter hoursMinutesFormatter;

    public void createPDF(String _path, Calendar _reportSelectedDate) throws FileNotFoundException, DocumentException {

        path = _path;
        dir = new File(path);
        reportDate = _reportSelectedDate;
        reportEndDate = Calendar.getInstance();
        reportEndDate.setTime(reportDate.getTime());
        reportEndDate.add(Calendar.MONTH, 1);
        reportEndDate.add(Calendar.DAY_OF_MONTH, -1);

        hoursMinutesFormatter = new PeriodFormatterBuilder()
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendHours()
                .appendSeparator(":")
                .appendMinutes()
                .toFormatter();

        setupWeekdaysArr();
        //create document file
        try {

            loadData();

            if(mCurrentWorkProfile != null) {
                configureDocument();
                doc.open();

                createHeadline();

                PdfPTable holderTable = new PdfPTable(3);
                holderTable.setWidthPercentage(100.0F);
                holderTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                holderTable.setWidths(new int[]{80, 1, 19});

                PdfPTable mainTable = getMainTable();

                PdfPCell firstTableCell = new PdfPCell();
                firstTableCell.setPadding(0f);
                firstTableCell.setBorder(PdfPCell.NO_BORDER);
                firstTableCell.addElement(mainTable);
                holderTable.addCell(firstTableCell);

                PdfPCell spacer = getCell(null, 1, 1);
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

                Toast.makeText(c, "Report saved in Documents", Toast.LENGTH_LONG).show();

                doc.close();
            }
            else{
                Toast.makeText(c, "Report can't be generated. No working profile", Toast.LENGTH_LONG).show();
            }

        } catch (DocumentException de) {
            Log.e("PDFCreator", "DocumentException:" + de);
        }
    }

    private void configureDocument() throws FileNotFoundException, DocumentException {
        doc = new Document(PageSize.A4.rotate());
        doc.setMargins(25,25,25,25);

        Log.e("PDFCreator", "PDF Path: " + path);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMyyyy");
        file = new File(dir, "UniTyLabEmployeesTimesheet_" + sdf.format(reportDate.getTime()) + ".pdf");
        FileOutputStream fOut = new FileOutputStream(file);
        PdfWriter writer = PdfWriter.getInstance(doc, fOut);
    }

    private void setupWeekdaysArr(){
        germanDays.put(Calendar.MONDAY, "Mo");
        germanDays.put(Calendar.TUESDAY, "Di");
        germanDays.put(Calendar.WEDNESDAY, "Mi");
        germanDays.put(Calendar.THURSDAY, "Do");
        germanDays.put(Calendar.FRIDAY, "Fr");
        germanDays.put(Calendar.SATURDAY, "Sa");
        germanDays.put(Calendar.SUNDAY, "So");
    }

    private void loadData() {
        usersBox = ((App) c.getApplication()).getBoxStore().boxFor(User.class);
        currentUser = UserManager.getSignedInUser(usersBox);

        mDayBox = ((App) c.getApplication()).getBoxStore().boxFor(Day.class);
        startDate = reportDate.getTime();
        endDate = reportEndDate.getTime();
        mDayQuery = mDayBox.query().between(Day_.day, startDate, endDate).equal(Day_.userID, currentUser.getId()).orderDesc(Day_.day).build();
        mDayList.addAll(mDayQuery.find());

        Box<WorkProfile> mWorkProfileBox = ((App) c.getApplication()).getBoxStore().boxFor(WorkProfile.class);
        Query<WorkProfile> mWorkProfileQuery =  mWorkProfileBox.query().between(WorkProfile_.startDate, startDate, endDate).equal(WorkProfile_.userID, currentUser.getId()).orderDesc(WorkProfile_.startDate).build();
        mCurrentWorkProfile = mWorkProfileQuery.findFirst();
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
        Paragraph p1 = new Paragraph("Working Time Page",new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
        p1.setAlignment(Element.ALIGN_LEFT);
        p1.setSpacingAfter(10);
        doc.add(p1);
    }

    //MAIN TABLE

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
            if (reportEndDate.get(Calendar.DAY_OF_MONTH)>=i) {
                Day thisDay = findDay(i);
                Calendar date = Calendar.getInstance();
                date.setTime(reportDate.getTime());
                date.set(Calendar.DAY_OF_MONTH, i);
                date.setFirstDayOfWeek(Calendar.MONDAY);
                boolean blue = (date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
                firstTable.addCell(getTupleCell(germanDays.get(date.get(Calendar.DAY_OF_WEEK)), 1, 1, blue));
                firstTable.addCell(getTupleCell(new SimpleDateFormat("dd.MM.yy").format(date.getTime()), 1, 1, blue));

                String beginn = "", mVon = "", mBis = "", zVon = "", zBis = "", ende = "", istStd = "00:00", sollStd = "00:00", plusMinusStd = "00:00", aza = "00:00", uebertrag = "00:00", bemerkungen = "";

                Period workingHoursWP = TimelyHelper.getDayOfTheWeekWorkingHours(mCurrentWorkProfile, date.getTime()).toPeriod();
                sollStd = TimelyHelper.negativeTimePeriodFormatter(workingHoursWP, hoursMinutesFormatter);

                if (thisDay != null) {

                    //BEMERKUNGEN
                    bemerkungen = getDayTypeRepostString(thisDay);
                    //UEBERTRAG
                    Period totalDayOvertime = Duration.millis(TimelyHelper.getTotalOvertimeForDay(thisDay, mCurrentWorkProfile, mDayBox,currentUser.getId())).toPeriod();
                    uebertrag = TimelyHelper.negativeTimePeriodFormatter(totalDayOvertime, hoursMinutesFormatter);

                    if(thisDay.getType().compareTo(Day.DAY_TYPE.HOLIDAY) != 0 && thisDay.getType().compareTo(Day.DAY_TYPE.DAY_OFF_IN_LIEU) != 0 &&
                            thisDay.getType().compareTo(Day.DAY_TYPE.OTHER) != 0 && thisDay.getType().compareTo(Day.DAY_TYPE.ILLNESS) != 0) {
                        beginn = thisDay.getStart().toString(Day.TIME_FORMATTER);
                        ende = thisDay.getEnd().toString(Day.TIME_FORMATTER);

                        //BREAK IS SAVED AS DURATION, SWITCH TO START & END DATE
                        DateTime breakStart = thisDay.getStart().plusHours(2);
                        mVon = breakStart.toString(Day.TIME_FORMATTER);
                        mBis = breakStart.plus(thisDay.getPause()).toString(Day.TIME_FORMATTER);

                        Period workingHoursTotal = thisDay.getTotalWorkingTime().toPeriod();
                        istStd = hoursMinutesFormatter.print(workingHoursTotal);

                        Period workingHoursDiffSigned = workingHoursTotal.toStandardDuration().minus(workingHoursWP.toStandardDuration()).toPeriod();
                        plusMinusStd = TimelyHelper.negativeTimePeriodFormatter(workingHoursDiffSigned, hoursMinutesFormatter);


                    }
                    else if(thisDay.getType().compareTo(Day.DAY_TYPE.DAY_OFF_IN_LIEU) == 0){
                        aza = sollStd;
                    }

                }

                firstTable.addCell(getTupleCell(beginn, 1, 1, blue));
                firstTable.addCell(getTupleCell(mVon, 1, 1, blue));
                firstTable.addCell(getTupleCell(mBis, 1, 1, blue));
                firstTable.addCell(getTupleCell(zVon, 1, 1, blue));
                firstTable.addCell(getTupleCell(zBis, 1, 1, blue));
                firstTable.addCell(getTupleCell(ende, 1, 1, blue));
                firstTable.addCell(getTupleCell(istStd, 1, 1, blue));
                firstTable.addCell(getTupleCell(sollStd, 1, 1, blue));
                firstTable.addCell(getTupleCell(plusMinusStd, 1, 1, blue));
                firstTable.addCell(getTupleCell(aza, 1, 1, blue));
                firstTable.addCell(getTupleCell(uebertrag, 1, 1, blue));
                firstTable.addCell(getTupleCell(bemerkungen, 1, 1, blue));
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

        Period monthOvertime =  TimelyHelper.getMonthTotalOvertime(mCurrentWorkProfile, mDayBox,currentUser.getId()).toPeriod();
        String monthOvertimeStr = TimelyHelper.negativeTimePeriodFormatter(monthOvertime, hoursMinutesFormatter);
        PdfPCell footerCell2 = getCell(monthOvertimeStr, 1, 1, bold);
        footerCell2.setBorder(Rectangle.BOTTOM| Rectangle.RIGHT);
        firstTable.addCell(footerCell2);
        PdfPCell ecell = getCell(null,1,1);
        ecell.setBorder(PdfPCell.NO_BORDER);
        firstTable.addCell(ecell);

        return firstTable;
    }

    private String getDayTypeRepostString(Day thisDay){

        switch(thisDay.getType()){
            case OTHER:
                return "S";
            case BUSINESS_TRIP:
                return "D";
            case HOLIDAY:
                return "U";
            case DOCTOR_APPOINTMENT:
                return "A";
            case FURTHER_EDUCATION:
                return "F";
            case DAY_OFF_IN_LIEU:
                return "AZA";
            case ILLNESS:
                return "K";
            default:
                return "";
        }
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
        String month = germanMonths[reportDate.get(Calendar.MONTH)];
        p.add(new Chunk(month,monthFont));
        p.add(Chunk.NEWLINE);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy");
        p.add(new Chunk(new SimpleDateFormat("yyyy").format(reportDate.getTime()),monthFont));
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

        String lastMonthOvertime = TimelyHelper.negativeTimePeriodFormatter(mCurrentWorkProfile.getPreviousOvertime().toPeriod(), hoursMinutesFormatter);
        firstTable.addCell(getHeaderCell(lastMonthOvertime,1,1, EXCEPT_TOP, false));


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
        tCell.setPaddingBottom(5.0f);
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
        p1.setSpacingAfter(10f);

        p1.setLeading(0, 1.5f);

        secondTable.addCell(getSidebarCell(p1, BaseColor.WHITE,null));

        tCell = getSidebarCell("Schritt 2",sidebarBold,titleBGColor,titleTextColor,null);
        tCell.setPaddingBottom(5.0f);
        secondTable.addCell(tCell);

        Paragraph p2 = new Paragraph();
        p2.setAlignment(Element.ALIGN_CENTER);
        p2.setPaddingTop(2.0f);
        p2.add(getChunk("Die/der ",titleTextColor,sidebarBold));
        p2.add(getChunk("Beschäftigte",titleTextColor,new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD| Font.UNDERLINE)));
        p2.add(Chunk.NEWLINE);
        p2.add(getChunk("bestätigt die Richtigkeit", BaseColor.BLACK,sidebarBold));
        p2.add(Chunk.NEWLINE);
        p2.add(getChunk("der gemachten Angaben.", BaseColor.BLACK,bold));
        //p2.setSpacingAfter(10f);
        p2.setLeading(0, 1.5f);

        PdfPCell p2Cell = getSidebarCell(p2, BaseColor.WHITE,85f);
        p2Cell.setVerticalAlignment(Element.ALIGN_TOP);
        p2Cell.setBorder(EXCEPT_BOTTOM);
        p2Cell.setPaddingBottom(2f);
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
        p2.setLeading(0, 1.5f);
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

        p2Cell = getSidebarCell(p2, BaseColor.WHITE,95f);
        p2Cell.setVerticalAlignment(Element.ALIGN_TOP);
        p2Cell.setBorder(EXCEPT_BOTTOM);
        secondTable.addCell(p2Cell);

        p2Footer = getSidebarCell("Datum/Unterschrift)",sidebarBold, BaseColor.WHITE, BaseColor.BLACK,null);
        p2Footer.setPaddingBottom(5.0f);
        p2Footer.setBorder(EXCEPT_TOP);
        p2Footer.setCellEvent(new DottedCell(PdfPCell.TOP));
        secondTable.addCell(p2Footer);

        PdfPCell footer = getSidebarCell("Professor Meixner",sidebarBold,nameBGColor, BaseColor.BLACK,null);
        footer.setPaddingBottom(5.0f);
        footer.setBorder(Rectangle.TOP| Rectangle.BOTTOM | Rectangle.LEFT | Rectangle.RIGHT);
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
