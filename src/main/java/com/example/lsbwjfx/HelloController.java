package com.example.lsbwjfx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class HelloController {

    @FXML
    private TextField outputImageDir;

    @FXML
    private ScrollPane attachImagePane;

    @FXML
    private Button browseButton1;

    @FXML
    private Button browseButton2;

    @FXML
    private Button clearButton;

    @FXML
    private TextField inputImageDir;

    @FXML
    private ScrollPane origImagePane;

    @FXML
    private Button showButton;

    @FXML
    private TextField textToInsert;

    private FileChooser fileChooser = new FileChooser();
    private File inputImageFile;
    private File outputImageFile;

    @FXML
    void initialize() {
        // Обработчик для кнопки выбора исходного изображения
        browseButton1.setOnAction(event -> {
            Stage stage = (Stage) browseButton1.getScene().getWindow();
            inputImageFile = fileChooser.showOpenDialog(stage);
            if (inputImageFile != null) {
                inputImageDir.setText(inputImageFile.getAbsolutePath());
            }
        });

        // Обработчик для кнопки выбора директории сохранения изображения
        browseButton2.setOnAction(event -> {
            Stage stage = (Stage) browseButton2.getScene().getWindow();
            outputImageFile = fileChooser.showSaveDialog(stage);
            if (outputImageFile != null) {
                outputImageDir.setText(outputImageFile.getAbsolutePath());
            }
        });

        // Обработчик для кнопки "Show"
        showButton.setOnAction(event -> {
            String text = textToInsert.getText();
            if (inputImageFile != null && outputImageFile != null && !text.isEmpty()) {
                try {
                    BufferedImage inputImage = ImageIO.read(inputImageFile);
                    BufferedImage outputImage = LSBEncoder(inputImage, text);

                    // Сохранение изображения с внедренным текстом
                    ImageIO.write(outputImage, "bmp", outputImageFile);

                    WritableImage extractedInImage = extractImageFromLSB(inputImage);
                    WritableImage extractedOutImage = extractImageFromLSB(outputImage);

                    // Отображение исходного изображения
                    ImageView originalImageView = new ImageView(extractedInImage);
                    origImagePane.setContent(originalImageView);

                    // Отображение изображения с внедренным текстом
                    ImageView attachedImageView = new ImageView(extractedOutImage);
                    attachImagePane.setContent(attachedImageView);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Обработчик для кнопки "Clear"
        clearButton.setOnAction(event -> {
            inputImageDir.clear();
            outputImageDir.clear();
            textToInsert.clear();
            origImagePane.setContent(null);
            attachImagePane.setContent(null);
        });
    }

    public static BufferedImage LSBEncoder(BufferedImage image, String text) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][][] rgb = new int[width][height][3];

        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                int pixel = image.getRGB(w, h);
                rgb[w][h][0] = (pixel & 0xff0000) >> 16;
                rgb[w][h][1] = (pixel & 0xff00) >> 8;
                rgb[w][h][2] = (pixel & 0xff);
            }
        }

        byte[] buf = text.getBytes();
        int byteLen = buf.length;

        int[] bufLen = new int[2];
        bufLen[0] = (byteLen & 0xff00) >> 8;
        bufLen[1] = (byteLen & 0xff);

        for (int i = 0; i < 2; i++) {
            for (int j = 7; j >= 0; j--) {
                int h = (i * 8 + (7 - j)) / width;
                int w = (i * 8 + (7 - j)) % width;
                if ((bufLen[i] >> j & 1) == 1) {
                    rgb[w][h][0] = rgb[w][h][0] | 1;
                } else {
                    rgb[w][h][0] = rgb[w][h][0] & 0xFE;
                }
            }
        }

        for (int i = 2; i < byteLen + 2; i++) {
            for (int j = 7; j >= 0; j--) {
                int h = (i * 8 + (7 - j)) / width;
                int w = (i * 8 + (7 - j)) % width;
                if ((buf[i - 2] >> j & 1) == 1) {
                    rgb[w][h][0] = rgb[w][h][0] | 1;
                } else {
                    rgb[w][h][0] = rgb[w][h][0] & 0xFE;
                }
            }
        }

        BufferedImage imageOutput = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                int[] color = new int[3];
                color[0] = rgb[w][h][0] << 16;
                color[1] = rgb[w][h][1] << 8;
                color[2] = rgb[w][h][2];
                int pixel = 0xff000000 | color[0] | color[1] | color[2];
                imageOutput.setRGB(w, h, pixel);
            }
        }

        return imageOutput;
    }

    private WritableImage extractImageFromLSB(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        WritableImage extractedImage = new WritableImage(width, height);
        PixelWriter pixelWriter = extractedImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int lsb = red & 1;

                Color color = (lsb == 1) ? Color.WHITE : Color.BLACK;
                pixelWriter.setColor(x, y, color);
            }
        }

        return extractedImage;
    }
}