package com.yam.myaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.yam.myaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

/**
 * PDF 生成工具
 */
public class PDFGenerationTool {

    /**
     * 生成PDF文件工具方法
     * <p>
     * 该方法根据提供的文件名和内容生成PDF文件，使用iText7库进行PDF创建，
     * 支持中文字体显示，并将文件保存到指定目录。
     *
     * @param fileName 要保存的PDF文件名
     * @param content  要包含在PDF中的内容文本
     * @return 操作结果描述字符串，成功时返回文件路径，失败时返回错误信息
     */
    @Tool(description = "Generate a PDF file with given content", returnDirect = false)
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            System.out.println("开始生成PDF文件: " + filePath);

            // 创建 PdfWriter 和 PdfDocument 对象
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                // 使用内置中文字体
                System.out.println("尝试加载中文字体: STSongStd-Light");
                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                System.out.println("中文字体加载成功");

                document.setFont(font);

                // 创建段落
                Paragraph paragraph = new Paragraph(content);

                // 添加段落并关闭文档
                document.add(paragraph);
                System.out.println("PDF内容添加成功");
            }
            System.out.println("PDF生成完成: " + filePath);
            return "PDF generated successfully to: " + filePath;
        } catch (IOException e) {
            System.err.println("PDF生成失败: " + e.getMessage());
            e.printStackTrace();
            return "Error generating PDF: " + e.getMessage();
        }
    }
}
