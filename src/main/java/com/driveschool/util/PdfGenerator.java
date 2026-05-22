package com.driveschool.util;

import com.driveschool.config.CorsConfig;
import com.driveschool.entity.*;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

@Component
public class PdfGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private PdfFont getChineseFont() throws Exception {
        try {
            File fontFile = ResourceUtils.getFile("classpath:fonts/simsun.ttc,1");
            FontProgram fontProgram = FontProgramFactory.createFont(fontFile.getAbsolutePath());
            return PdfFontFactory.createFont(fontProgram);
        } catch (Exception e) {
            try {
                return PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
            } catch (Exception ex) {
                return PdfFontFactory.createFont();
            }
        }
    }

    public String generateRegistrationForm(StudentInfo studentInfo, User user) throws Exception {
        String fileName = "registration_" + studentInfo.getId() + ".pdf";
        String filePath = CorsConfig.PDF_OUTPUT_PATH + fileName;

        PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        PdfFont font = getChineseFont();

        document.add(new Paragraph("驾校报名表").setFont(font).setFontSize(18).setBold());
        document.add(new Paragraph("\n"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 7})).useAllAvailableWidth();
        addRow(table, "姓名", user.getRealName(), font);
        addRow(table, "身份证号", studentInfo.getIdCard(), font);
        addRow(table, "联系电话", user.getPhone(), font);
        addRow(table, "联系地址", studentInfo.getAddress(), font);
        addRow(table, "报考车型", String.valueOf(studentInfo.getVehicleTypeId()), font);
        addRow(table, "报名时间", studentInfo.getRegistrationTime() != null ?
                studentInfo.getRegistrationTime().format(DATE_FMT) : "", font);
        document.add(table);

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("学员签名：______________    日期：______________").setFont(font));

        document.close();
        return fileName;
    }

    public String generateHealthReport(StudentInfo studentInfo, User user) throws Exception {
        String fileName = "health_" + studentInfo.getId() + ".pdf";
        String filePath = CorsConfig.PDF_OUTPUT_PATH + fileName;

        PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        PdfFont font = getChineseFont();

        document.add(new Paragraph("体检表").setFont(font).setFontSize(18).setBold());
        document.add(new Paragraph("\n"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 7})).useAllAvailableWidth();
        addRow(table, "姓名", user.getRealName(), font);
        addRow(table, "身份证号", studentInfo.getIdCard(), font);
        addRow(table, "体检状态", "合格", font);
        document.add(table);

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("体检医生签名：______________    日期：______________").setFont(font));

        document.close();
        return fileName;
    }

    public String generateExamCard(StudentInfo studentInfo, User user) throws Exception {
        String fileName = "examcard_" + studentInfo.getId() + ".pdf";
        String filePath = CorsConfig.PDF_OUTPUT_PATH + fileName;

        PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        PdfFont font = getChineseFont();

        document.add(new Paragraph("准考证").setFont(font).setFontSize(18).setBold());
        document.add(new Paragraph("\n"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 7})).useAllAvailableWidth();
        addRow(table, "学员编号", String.valueOf(studentInfo.getUserId()), font);
        addRow(table, "姓名", user.getRealName(), font);
        addRow(table, "身份证号", studentInfo.getIdCard(), font);
        addRow(table, "准考证号", "DS" + System.currentTimeMillis() % 100000000, font);
        document.add(table);

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("考试须知：请携带本人身份证和本准考证参加考试。").setFont(font));

        document.close();
        return fileName;
    }

    private void addRow(Table table, String label, String value, PdfFont font) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(font)));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "").setFont(font)));
    }
}
