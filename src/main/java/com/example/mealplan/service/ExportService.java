package com.example.mealplan.service;

import com.example.mealplan.entity.MealPlan;
import com.example.mealplan.entity.MealPlanDay;
import com.example.mealplan.entity.Meal;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExportService {

    public Map<String, Double> getShoppingList(MealPlan plan) {
        Map<String, Double> shoppingList = new HashMap<>();
        if (plan.getDays() != null) {
            plan.getDays().forEach(day -> {
                if (day.getMeals() != null) {
                    day.getMeals().forEach(meal -> {
                        if (meal.getRecipe() != null && meal.getRecipe().getRecipeIngredients() != null) {
                            meal.getRecipe().getRecipeIngredients().forEach(ri -> {
                                String ingredientName = ri.getIngredient().getName();
                                shoppingList.merge(ingredientName, ri.getAmount(), Double::sum);
                            });
                        }
                    });
                }
            });
        }
        return shoppingList;
    }

    public byte[] exportToPdf(MealPlan plan) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(baos);
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);

            com.itextpdf.kernel.font.PdfFont font = com.itextpdf.kernel.font.PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            document.add(new com.itextpdf.layout.element.Paragraph("Meal Plan: " + plan.getName())
                    .setFont(font).setFontSize(18));
            document.add(new com.itextpdf.layout.element.Paragraph(
                    "Period: " + plan.getStartDate() + " - " + plan.getEndDate())
                    .setFont(font).setFontSize(12));
            document.add(new com.itextpdf.layout.element.Paragraph(" "));

            if (plan.getDays() != null) {
                for (MealPlanDay day : plan.getDays()) {
                    document.add(new com.itextpdf.layout.element.Paragraph("Date: " + day.getDate())
                            .setFont(font).setFontSize(14));

                    com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(5);
                    table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(
                            new com.itextpdf.layout.element.Paragraph("Meal").setFont(font)));
                    table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(
                            new com.itextpdf.layout.element.Paragraph("Recipe").setFont(font)));
                    table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(
                            new com.itextpdf.layout.element.Paragraph("Kcal").setFont(font)));
                    table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(
                            new com.itextpdf.layout.element.Paragraph("Protein").setFont(font)));
                    table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(
                            new com.itextpdf.layout.element.Paragraph("Fat").setFont(font)));

                    if (day.getMeals() != null) {
                        for (Meal meal : day.getMeals()) {
                            table.addCell(new com.itextpdf.layout.element.Cell().add(
                                    new com.itextpdf.layout.element.Paragraph(meal.getMealType().name()).setFont(font)));
                            table.addCell(new com.itextpdf.layout.element.Cell().add(
                                    new com.itextpdf.layout.element.Paragraph(
                                            meal.getRecipe() != null ? meal.getRecipe().getTitle() : "N/A").setFont(font)));
                            table.addCell(new com.itextpdf.layout.element.Cell().add(
                                    new com.itextpdf.layout.element.Paragraph(
                                            String.valueOf(meal.getRecipe() != null && meal.getRecipe().getTotalCalories() != null ?
                                                    meal.getRecipe().getTotalCalories() : 0)).setFont(font)));
                            table.addCell(new com.itextpdf.layout.element.Cell().add(
                                    new com.itextpdf.layout.element.Paragraph(
                                            String.valueOf(meal.getRecipe() != null && meal.getRecipe().getTotalProteins() != null ?
                                                    meal.getRecipe().getTotalProteins() : 0)).setFont(font)));
                            table.addCell(new com.itextpdf.layout.element.Cell().add(
                                    new com.itextpdf.layout.element.Paragraph(
                                            String.valueOf(meal.getRecipe() != null && meal.getRecipe().getTotalFats() != null ?
                                                    meal.getRecipe().getTotalFats() : 0)).setFont(font)));
                        }
                    }
                    document.add(table);
                    document.add(new com.itextpdf.layout.element.Paragraph(" "));
                }
            }

            Map<String, Double> shoppingList = getShoppingList(plan);
            document.add(new com.itextpdf.layout.element.Paragraph("Shopping List:")
                    .setFont(font).setFontSize(14));
            for (Map.Entry<String, Double> entry : shoppingList.entrySet()) {
                document.add(new com.itextpdf.layout.element.Paragraph(
                        "  - " + entry.getKey() + ": " + entry.getValue())
                        .setFont(font).setFontSize(10));
            }

            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при генерации PDF", e);
        }
    }

    public byte[] exportToExcel(MealPlan plan) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Meal Plan");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("Meal Plan: " + plan.getName());

            Row periodRow = sheet.createRow(1);
            periodRow.createCell(0).setCellValue("Period: " + plan.getStartDate() + " - " + plan.getEndDate());

            Row headerRow = sheet.createRow(3);
            String[] headers = {"Date", "Meal Type", "Recipe", "Calories", "Proteins", "Fats", "Carbs"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 4;
            if (plan.getDays() != null) {
                for (MealPlanDay day : plan.getDays()) {
                    if (day.getMeals() != null) {
                        for (Meal meal : day.getMeals()) {
                            Row row = sheet.createRow(rowNum++);
                            row.createCell(0).setCellValue(day.getDate().toString());
                            row.createCell(1).setCellValue(meal.getMealType().name());
                            row.createCell(2).setCellValue(
                                    meal.getRecipe() != null ? meal.getRecipe().getTitle() : "N/A");
                            row.createCell(3).setCellValue(
                                    meal.getRecipe() != null && meal.getRecipe().getTotalCalories() != null ?
                                            meal.getRecipe().getTotalCalories() : 0);
                            row.createCell(4).setCellValue(
                                    meal.getRecipe() != null && meal.getRecipe().getTotalProteins() != null ?
                                            meal.getRecipe().getTotalProteins() : 0);
                            row.createCell(5).setCellValue(
                                    meal.getRecipe() != null && meal.getRecipe().getTotalFats() != null ?
                                            meal.getRecipe().getTotalFats() : 0);
                            row.createCell(6).setCellValue(
                                    meal.getRecipe() != null && meal.getRecipe().getTotalCarbohydrates() != null ?
                                            meal.getRecipe().getTotalCarbohydrates() : 0);
                        }
                    }
                }
            }

            rowNum += 2;
            Row shoppingHeader = sheet.createRow(rowNum++);
            shoppingHeader.createCell(0).setCellValue("Shopping List");

            Map<String, Double> shoppingList = getShoppingList(plan);
            for (Map.Entry<String, Double> entry : shoppingList.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при генерации Excel", e);
        }
    }
}
