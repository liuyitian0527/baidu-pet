package com.fun.zpetchain.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

/**
 * tool class that handles the captcha and identifes the captcha <br>
 * <b>Copyright 2018 the original author or authors.</b>
 * 
 * @author 2bears
 * @since
 * @version 1.0
 */
public class OcrUtil {

	private static final Logger logger = LoggerFactory.getLogger(OcrUtil.class);

	private static Runtime runtime = null;

	/**
	 * identify captcha by Tesseract via cmd you should set Tesseract environment first
	 * 
	 * @author 2bears
	 * @since
	 * @param img
	 * @return
	 */
	public static String ocrByTesseract(BufferedImage img) {
		String result = "";
		try {
			String fileName = "F:\\testPic\\vCode.jpg";
			String tessConfig = "--tessdata-dir \"F:\\Tesseract-OCR\\tessdata\" --psm 3 -l my+my2";

			setBorder(img);
			imgFilter(img);
			binarization(img, 175);

			File ff = new File(fileName);
			ImageIO.write(img, "jpg", ff);

			if (runtime == null) {
				runtime = Runtime.getRuntime();
			}
			String cmd = "F:\\Tesseract-OCR\\tesseract " + fileName + " stdout " + tessConfig;
			Process ps = runtime.exec(cmd);

			BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			String line = "";
			while ((line = br.readLine()) != null) {
				result += line;
			}
			br.close();

			result = filterResult(result.trim());

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {

		}
		return result;
	}

	/**
	 * identify captcha by tess4j
	 * 
	 * @author 2bears
	 * @since
	 * @param img
	 * @return
	 */
	public static String ocrByTess4j(BufferedImage img) {
		String result = "";
		try {
			ITesseract tess = new Tesseract();
			String dataPath = System.getProperty("user.dir") + "\\";
			tess.setDatapath(dataPath);
			tess.setLanguage("my2");

			setBorder(img);
			imgFilter(img);
			binarization(img, 175);
			result = tess.doOCR(img).trim();
			result = filterResult(result.trim());

			// if (result.length() == 4) {
			// File ff = new File("E:\\testPic\\" + result + ".jpg");
			// ImageIO.write(img, "jpg", ff);
			// }

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return result;
	}

	private static String filterResult(String result) {

		if (StringUtils.isEmpty(result)) {
			return result;
		}

		result = result.replaceAll("[^a-zA-Z0-9]", "");
		result = result.replaceAll("J", "T");
		result = result.replaceAll("[iI1l]", "J");
		result = result.replaceAll("[oO0]", "Q");

		return result.toUpperCase();
	}

	/**
	 * gray img
	 * 
	 * @author 2bears
	 * @since
	 * @param img
	 */
	private void imgGray(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		int gray = 0;
		Color color = null;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				color = new Color(img.getRGB(x, y));
				if (color.getRed() != color.getGreen() || color.getRed() != color.getBlue()) {
					gray = 30 * color.getRed() + 59 * color.getGreen() + 11 * color.getBlue();
					gray = gray / 100;
					img.setRGB(x, y, new Color(gray, gray, gray).getRGB());
				}
			}
		}
	}

	/**
	 * remove interferences lines of the picture
	 * 
	 * @author 2bears
	 * @since
	 * @param img
	 * @return
	 * @throws Exception
	 */
	private static BufferedImage imgFilter(BufferedImage img) throws Exception {
		int width = img.getWidth();
		int height = img.getHeight();
		int filterPix = 4;
		int borderPix = 230;
		int count = 0, yH = 0, yL = 0, xH = 0, xL = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (new Color(img.getRGB(x, y)).getRed() > borderPix) {
					continue;
				}
				count = 0;
				yH = y + filterPix;
				yL = y - filterPix;
				if (yH >= height) {
					yH = height - 1;
				}
				if (yL < 0) {
					yL = 0;
				}
				for (int index = yL; index <= yH; index++) {
					if (new Color(img.getRGB(x, index)).getRed() < borderPix) {
						count++;
					}
				}
				if (count < filterPix) {
					img.setRGB(x, y, Color.WHITE.getRGB());
				}
			}
		}

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (new Color(img.getRGB(x, y)).getRed() > borderPix) {
					continue;
				}
				count = 0;
				xH = x + filterPix;
				xL = x - filterPix;
				if (xH >= width) {
					xH = width - 1;
				}
				if (xL < 0) {
					xL = 0;
				}
				for (int index = xL; index <= xH; index++) {
					if (new Color(img.getRGB(index, y)).getRed() < borderPix) {
						count++;
					}
				}
				if (count < filterPix) {
					img.setRGB(x, y, Color.WHITE.getRGB());
				}
			}
		}
		for (int y = 0; y <= 3; y++) {
			for (int x = 0; x < width; x++) {
				img.setRGB(x, y, Color.WHITE.getRGB());
			}
		}
		return img;
	}

	/**
	 * binarization
	 * 
	 * @author 2bears
	 * @since
	 * @param img
	 * @param pixValue
	 */
	private static void binarization(BufferedImage img, int pixValue) {
		int width = img.getWidth();
		int height = img.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (new Color(img.getRGB(x, y)).getRed() < pixValue) {
					img.setRGB(x, y, Color.BLACK.getRGB());
				} else {
					img.setRGB(x, y, Color.WHITE.getRGB());
				}
			}
		}
	}

	/**
	 * crop the text area
	 * 
	 * @author 2bears
	 * @since
	 * @param img
	 */
	private static void setBorder(BufferedImage img) {
		int borderXLeftUp = 38;
		int borderYLeftUp = 13;
		int borderXRightBottom = 20;
		int borderYRightBottom = 20;
		int width = img.getWidth();
		int height = img.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (x > borderXLeftUp && y > borderYLeftUp && x < width - borderXRightBottom && y < height - borderYRightBottom) {
					continue;
				} else {
					img.setRGB(x, y, Color.WHITE.getRGB());
				}
			}
		}
	}

	/**
	 * test by tess4j
	 * 
	 * @author 2bears
	 * @since
	 * @return
	 */
	public String ocrByTess4jTest() {
		// File imageFile = new File("test.jpg");
		Tesseract tess = new Tesseract();
		tess.setDatapath("C:\\Users\\yitianliu\\Desktop\\tessdata");
		tess.setLanguage("my2");
		try {

			File folder = new File("C:\\Users\\yitianliu\\Desktop\\testPic");
			int succ = 0, fail = 0;
			long start = System.currentTimeMillis();
			for (File f : folder.listFiles()) {
				if (f.isFile()) {
					BufferedImage img = ImageIO.read(f);
					setBorder(img);
					imgFilter(img);
					binarization(img, 175);

					String result = tess.doOCR(img).trim();

					File ff = new File("E:\\testPic\\" + result + ".jpg");
					ImageIO.write(img, "jpg", ff);

					result = filterResult(result.trim());
					String fileName = f.getName().substring(0, 4).toUpperCase();
					if (!fileName.equals(result)) {
						System.out.println(String.format("result=%s, fileName=%s", result, fileName));
						fail++;
					} else {
						succ++;
					}
				}
			}

			System.out.println(String.format("succ=%s,fail=%s,cost:%s", succ, fail, System.currentTimeMillis() - start));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public static void main(String[] args) {
		OcrUtil util = new OcrUtil();
		util.ocrByTess4jTest();
		System.out.println(System.getProperty("user.dir"));
	}

	/**
	 * test by Tesseract via cmd
	 * 
	 * @author 2bears
	 * @since
	 */
	public void ocrBytessTest() {
		try {

			File folder = new File("F:\\chrome插件\\pet-chain-buyer-master\\pic\\save");
			int succ = 0, fail = 0;
			long start = System.currentTimeMillis();
			for (File f : folder.listFiles()) {
				if (f.isFile()) {
					BufferedImage img = ImageIO.read(f);
					setBorder(img);
					imgFilter(img);
					binarization(img, 175);

					String result = ocrByTesseract(img);
					if (result == null) {
						continue;
					}
					result = filterResult(result.trim());
					String fileName = f.getName().substring(0, 4).toUpperCase();
					if (!fileName.equals(result)) {
						System.out.println(String.format("result=%s, fileName=%s", result, fileName));
						fail++;
					} else {
						succ++;
					}
				}
			}

			System.out.println(String.format("succ=%s,fail=%s,cost:%s", succ, fail, System.currentTimeMillis() - start));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
