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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.format.DateTimeFormatter;

/**
 * PDF 生成器 —— 每次调用生成最新数据的 PDF，返回字节数组。
 * 不再依赖磁盘旧文件，确保导出内容与数据库一致。
 */
@Component
public class PdfGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** 公共中文加载逻辑 */
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

    // ============================================================
    // 报名表
    // 数据来源: StudentInfo + User + VehicleType
    // ============================================================
    public byte[] generateRegistrationForm(StudentInfo info, User user,
                                            VehicleType vehicleType, Coach coach, User coachUser) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        PdfFont font = getChineseFont();
        PdfFont titleFont = getChineseFont();

        // 标题
        Paragraph title = new Paragraph("驾校学员报名表")
                .setFont(titleFont).setFontSize(20).setBold()
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        // 基本信息
        document.add(sectionTitle("基本信息", font));
        Table basic = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 3})).useAllAvailableWidth();
        addCell(basic, "姓名", user.getRealName(), font);
        addCell(basic, "性别", "", font); // StudentInfo 当前无性别字段
        addCell(basic, "身份证号", info.getIdCard(), font);
        addCell(basic, "联系电话", user.getPhone(), font);
        addCell(basic, "联系地址", info.getAddress(), font);
        addCell(basic, "报名日期", info.getRegistrationTime() != null
                ? info.getRegistrationTime().format(DATE_FMT) : "", font);
        document.add(basic);
        document.add(new Paragraph("\n"));

        // 报考信息
        document.add(sectionTitle("报考信息", font));
        Table exam = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 3})).useAllAvailableWidth();
        addCell(exam, "报考车型", vehicleType != null ? vehicleType.getName() : String.valueOf(info.getVehicleTypeId()), font);
        addCell(exam, "审核状态", auditStatusLabel(info.getAuditStatus()), font);
        addCell(exam, "分配教练", coachUser != null ? coachUser.getRealName() : "暂未分配", font);
        addCell(exam, "教练电话", coachUser != null ? coachUser.getPhone() : "", font);
        addCell(exam, "报名编号", "DS" + info.getId(), font);
        addCell(exam, "当前阶段", "", font);
        document.add(exam);
        document.add(new Paragraph("\n"));

        // 签名区
        document.add(new Paragraph("学员签名：______________    日期：______________").setFont(font));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("（本表由系统根据报名信息自动生成）")
                .setFont(font).setFontSize(8).setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY));

        document.close();
        return baos.toByteArray();
    }

    // ============================================================
    // 体检表
    // 数据来源: StudentInfo.medicalStatus
    // ============================================================
    public byte[] generateHealthReport(StudentInfo info, User user) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        PdfFont font = getChineseFont();

        Paragraph title = new Paragraph("驾驶员体检报告表")
                .setFont(font).setFontSize(20).setBold()
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        // 个人信息
        document.add(sectionTitle("个人信息", font));
        Table personal = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 3})).useAllAvailableWidth();
        addCell(personal, "姓名", user.getRealName(), font);
        addCell(personal, "身份证号", info.getIdCard(), font);
        addCell(personal, "性别", "", font);
        addCell(personal, "联系电话", user.getPhone(), font);
        document.add(personal);
        document.add(new Paragraph("\n"));

        // 体检项目
        document.add(sectionTitle("体检项目", font));
        String medicalLabel = medicalStatusLabel(info.getMedicalStatus());
        boolean passed = "PASSED".equals(info.getMedicalStatus());

        Table health = new Table(UnitValue.createPercentArray(new float[]{2, 2, 3, 3})).useAllAvailableWidth();
        // 表头
        addHeaderCell(health, "检查项目", font);
        addHeaderCell(health, "结果", font);
        addHeaderCell(health, "检查项目", font);
        addHeaderCell(health, "结果", font);
        // 体检项目行
        addCell(health, "身高", passed ? "合格" : "—", font);
        addCell(health, "视力（矫正）", passed ? "合格" : "—", font);
        addCell(health, "辨色力", passed ? "正常" : "—", font);
        addCell(health, "听力", passed ? "正常" : "—", font);
        addCell(health, "四肢躯干", passed ? "正常" : "—", font);
        addCell(health, "血压", passed ? "正常" : "—", font);
        addCell(health, "心电图", passed ? "正常" : "—", font);
        addCell(health, "胸部X光", passed ? "正常" : "—", font);
        document.add(health);
        document.add(new Paragraph("\n"));

        // 结论
        Table conclusion = new Table(UnitValue.createPercentArray(new float[]{2, 8})).useAllAvailableWidth();
        addCell(conclusion, "体检结论", (passed ? "合格" : medicalLabel), font);
        addCell(conclusion, "体检日期", passed ? info.getAuditedTime() != null
                ? info.getAuditedTime().format(DATE_FMT) : "" : "—", font);
        document.add(conclusion);
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("体检医生签名：______________    机构盖章：______________").setFont(font));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("（本表由系统根据体检审核数据自动生成，结果以医院原始报告为准）")
                .setFont(font).setFontSize(8).setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY));

        document.close();
        return baos.toByteArray();
    }

    // ============================================================
    // 准考证
    // 数据来源: ExamRegistration + Subject + ExamLocation
    // ============================================================
    public byte[] generateExamCard(StudentInfo info, User user,
                                    ExamRegistration examReg, Subject subject,
                                    ExamLocation location) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        PdfFont font = getChineseFont();

        Paragraph title = new Paragraph("机动车驾驶员考试准考证")
                .setFont(font).setFontSize(20).setBold()
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        // 考生信息
        document.add(sectionTitle("考生信息", font));
        Table personal = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 3})).useAllAvailableWidth();
        addCell(personal, "姓名", user.getRealName(), font);
        addCell(personal, "身份证号", info.getIdCard(), font);
        addCell(personal, "准考证号", "DS" + String.format("%08d", examReg != null ? examReg.getId() : info.getId()), font);
        addCell(personal, "学员编号", String.valueOf(info.getUserId()), font);
        document.add(personal);
        document.add(new Paragraph("\n"));

        // 考试信息
        document.add(sectionTitle("考试信息", font));
        Table examTable = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 3})).useAllAvailableWidth();
        addCell(examTable, "考试科目", subject != null ? subject.getName() : "—", font);
        addCell(examTable, "考试日期", examReg != null && examReg.getExamDate() != null
                ? examReg.getExamDate().format(DATE_FMT) : "—", font);
        addCell(examTable, "考试地点", location != null ? location.getName() : "—", font);
        addCell(examTable, "考场地址", location != null ? location.getAddress() : "—", font);
        addCell(examTable, "考试状态", examReg != null ? examStatusLabel(examReg.getStatus()) : "—", font);
        addCell(examTable, "缴费状态", "", font);
        document.add(examTable);
        document.add(new Paragraph("\n"));

        // 须知
        document.add(sectionTitle("考试须知", font));
        document.add(new Paragraph("1. 请携带本人身份证原件和本准考证参加考试。").setFont(font).setFontSize(11));
        document.add(new Paragraph("2. 请提前30分钟到达考场，迟到15分钟不得入场。").setFont(font).setFontSize(11));
        document.add(new Paragraph("3. 严禁携带手机、电子设备进入考场。").setFont(font).setFontSize(11));
        document.add(new Paragraph("4. 考试过程中不得交头接耳，违规者取消考试资格。").setFont(font).setFontSize(11));
        document.add(new Paragraph("5. 合格标准：科目一≥90分，科目二/三≥80分。").setFont(font).setFontSize(11));

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("（本准考证由系统自动生成，需加盖驾校公章方为有效）")
                .setFont(font).setFontSize(8).setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY));

        document.close();
        return baos.toByteArray();
    }

    // ============================================================
    // 工具方法
    // ============================================================
    private Paragraph sectionTitle(String text, PdfFont font) {
        return new Paragraph(text).setFont(font).setFontSize(14).setBold()
                .setMarginBottom(6);
    }

    private void addCell(Table table, String label, String value, PdfFont font) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(font).setFontSize(10)));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "").setFont(font).setFontSize(10)));
    }

    private void addHeaderCell(Table table, String text, PdfFont font) {
        table.addCell(new Cell().add(new Paragraph(text).setFont(font).setFontSize(10).setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.GRAY));
    }

    private String auditStatusLabel(String status) {
        switch (status) {
            case "PENDING": return "待审核";
            case "APPROVED": return "已通过";
            case "REJECTED": return "已驳回";
            default: return status;
        }
    }

    private String medicalStatusLabel(String status) {
        switch (status) {
            case "PENDING": return "待体检";
            case "PASSED": return "体检合格";
            case "FAILED": return "体检不合格";
            default: return status;
        }
    }

    private String examStatusLabel(String status) {
        switch (status) {
            case "PENDING": return "待审核";
            case "APPROVED": return "已排考";
            case "REJECTED": return "已驳回";
            case "COMPLETED": return "已完成";
            default: return status;
        }
    }
}
