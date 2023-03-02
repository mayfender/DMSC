package com.may.ple.phone;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Block;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.Page;
import com.google.cloud.vision.v1.Paragraph;
import com.google.cloud.vision.v1.Symbol;
import com.google.cloud.vision.v1.TextAnnotation;
import com.google.cloud.vision.v1.Word;
import com.google.protobuf.ByteString;

public class TestOCR {
	private static String dateFormat = "%1$tH:%1$tM:%1$tS";

	public static void main(String[] args) throws IOException, Exception {
//		InputStream is = new FileInputStream("C:\\Users\\LENOVO\\Desktop\\OCR_Documents\\sign_text.png");

		System.out.println("Start " + String.format(dateFormat, Calendar.getInstance()));

//		InputStream is = pdf2image(new File("C:\\Users\\LENOVO\\Desktop\\OCR_Documents\\IV2000032.pdf"));
//		InputStream is = new BufferedInputStream(new FileInputStream("C:\\Users\\LENOVO\\Desktop\\OCR_Documents\\ex3.jpg"));
//
//		detectText(is, System.out);
//
//		System.out.println("End " + String.format(dateFormat, Calendar.getInstance()));
//
//		is.close();

		String path = "C:\\Users\\LENOVO\\Desktop\\OCR_Documents\\ex3.jpg";
		detectDocumentText(path);
	}

	public static InputStream pdf2image(File file) throws Exception {
		System.out.println("Start pdf2image " + String.format(dateFormat, Calendar.getInstance()));

		try (
				PDDocument document = PDDocument.load(file);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
			) {

			PDFRenderer renderer = new PDFRenderer(document);
//			BufferedImage buffImage = renderer.renderImage(0);
			BufferedImage buffImage = renderer.renderImageWithDPI(0, 300, ImageType.RGB);

			ImageIO.write(buffImage, "jpeg", os);
			ByteArrayInputStream imageByte = new ByteArrayInputStream(os.toByteArray());

			//-------------------------------------
//			byte[] buffer = new byte[imageByte.available()];
//			imageByte.read(buffer);
//
//		    OutputStream outStream = new FileOutputStream(new File("C:\\\\Users\\\\LENOVO\\\\Desktop\\\\OCR_Documents\\\\IV2000032.jpg"));
//		    outStream.write(buffer);
//		    outStream.close();
		    //-------------------------------------

			return imageByte;
		} catch (Exception e) {
			throw e;
		} finally {
			System.out.println("End pdf2image " + String.format(dateFormat, Calendar.getInstance()));
		}
	}

	public static void detectText(InputStream is, PrintStream out) throws Exception, IOException {
		System.out.println("Start detectText " + String.format(dateFormat, Calendar.getInstance()));

		  List<AnnotateImageRequest> requests = new ArrayList<>();

		  ByteString imgBytes = ByteString.readFrom(is);

		  Image img = Image.newBuilder().setContent(imgBytes).build();
//		  Feature feat = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
		  Feature feat = Feature.newBuilder().setType(Type.DOCUMENT_TEXT_DETECTION).build();
		  AnnotateImageRequest request =
		      AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
		  requests.add(request);

		  try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
		    BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
		    List<AnnotateImageResponse> responses = response.getResponsesList();

		    for (AnnotateImageResponse res : responses) {
		      if (res.hasError()) {
		        out.printf("Error: %s\n", res.getError().getMessage());
		        return;
		      }

		      // For full list of available annotations, see http://g.co/cloud/vision/docs
		      for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
		        out.printf("Text: %s\n", annotation.getDescription());
		        out.printf("Position : %s\n", annotation.getBoundingPoly());
		      }
		    }
		  }

		  System.out.println("End detectText " + String.format(dateFormat, Calendar.getInstance()));
	}

	public static void detectDocumentText(String filePath) throws IOException {
		  List<AnnotateImageRequest> requests = new ArrayList<>();

		  ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

		  Image img = Image.newBuilder().setContent(imgBytes).build();
		  Feature feat = Feature.newBuilder().setType(Type.DOCUMENT_TEXT_DETECTION).build();
		  AnnotateImageRequest request =
		      AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
		  requests.add(request);


		  // Initialize client that will be used to send requests. This client only needs to be created
		  // once, and can be reused for multiple requests. After completing all of your requests, call
		  // the "close" method on the client to safely clean up any remaining background resources.
		  try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
		    BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
		    List<AnnotateImageResponse> responses = response.getResponsesList();
		    client.close();

		    for (AnnotateImageResponse res : responses) {
		      if (res.hasError()) {
		        System.out.format("Error: %s%n", res.getError().getMessage());
		        return;
		      }

		      // For full list of available annotations, see http://g.co/cloud/vision/docs
		      TextAnnotation annotation = res.getFullTextAnnotation();
		      for (Page page : annotation.getPagesList()) {
		        String pageText = "";
		        for (Block block : page.getBlocksList()) {
		          String blockText = "";
		          for (Paragraph para : block.getParagraphsList()) {
		            String paraText = "";
		            for (Word word : para.getWordsList()) {
		              String wordText = "";
		              for (Symbol symbol : word.getSymbolsList()) {
		                wordText = wordText + symbol.getText();
		                System.out.format(
		                    "Symbol text: %s (confidence: %f)%n",
		                    symbol.getText(), symbol.getConfidence());
		              }
		              System.out.format(
		                  "Word text: %s (confidence: %f)%n%n", wordText, word.getConfidence());
		              paraText = String.format("%s %s", paraText, wordText);
		            }
		            // Output Example using Paragraph:
		            System.out.println("%nParagraph: %n" + paraText);
		            System.out.format("Paragraph Confidence: %f%n", para.getConfidence());
		            blockText = blockText + paraText;
		          }
		          pageText = pageText + blockText;
		        }
		      }
		      System.out.println("%nComplete annotation:");
		      System.out.println(annotation.getText());
		    }
		  }
		}

}
