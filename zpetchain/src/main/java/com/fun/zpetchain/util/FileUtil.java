package com.fun.zpetchain.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class FileUtil {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(FileUtil.class);

	/**
	 * 将字符串追加到文本文件，如果文件已存在，追加到原来的内容后
	 * 
	 * @param str,
	 *            要写出的内容
	 * @param fileName,
	 *            文件名
	 * @throws Exception
	 */
	public static void appendTxt(String str, String fileName) throws Exception {
		write(str, fileName, true);
	}

	/**
	 * 文件复制
	 * 
	 * @param from
	 * @param to
	 * @throws Exception
	 */
	public static final void copy(String from, String to) throws Exception {
		// System.out.println("copy from("+ from + ") to(" + to + ")");
		makeParentDir(to);
		FileInputStream fis = new FileInputStream(from);
		try {
			FileOutputStream fos = new FileOutputStream(to, false);
			try {
				byte[] buf = new byte[1024 * 16];
				int size = 0;
				while ((size = fis.read(buf)) != -1) {
					fos.write(buf, 0, size);
				}
			}
			catch (Exception ex) {
				throw ex;
			}
			finally {
				fos.flush();
				fos.close();
			}
		}
		catch (Exception ex) {
			throw ex;
		}
		finally {
			fis.close();
		}
	}

	/**
	 * 要创建的目录
	 * 
	 * @param src
	 * @param dest
	 * @param recursion
	 * @return
	 */
	public static final void copyDir(String src, String dest, boolean override) throws Exception {

		// 源目录是否存在
		File srcDir = new File(src);
		if (!srcDir.exists()) {
			throw new IOException("源目录不存在!");
		}

		// 目标目录是否存在
		File destDir = new File(dest);
		if (destDir.exists()) {
			if (override) { // 如果选择覆盖，覆盖
				destDir.delete();
			}
		}

		// 创建目标目录
		destDir.mkdirs();
		File[] copyList = srcDir.listFiles();
		for (int i = 0; i < copyList.length; i++) {
			if (copyList[i].isFile()) {
				copy(src + copyList[i].getName(), dest + copyList[i].getName());
			}
		}
	}

	/**
	 * 删除文件
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean delFile(String filePath) {
		boolean flag = false;
		File f = new File(filePath);
		if (f.exists()) {
			flag = f.delete();
		}
		f = null;
		return flag;
	}

	/**
	 * 删除目录下面的所有文本文件
	 * 
	 * @param dir
	 */
	public static void delTxtFiles(String dir) {
		File dirFile = new File(dir);
		if (dirFile.isDirectory()) {
			File[] files = dirFile.listFiles();
			for (File file : files) {
				String strExt = getFileExt(file.getName());
				if ("txt".equals(strExt)) {
					file.delete(); // 删除
				}
			}
		}
	}

	// --得到文件扩展名
	public static String getFileExt(String fileName) {
		String ext = "";
		if (fileName != null && !"".equals(fileName)) {
			int index = fileName.lastIndexOf(".");
			if (index > -1 && index < fileName.length() - 1) {
				ext = fileName.substring(index + 1);
			}
		}
		return ext;
	}

	/**
	 * 获得文件的行数
	 * 
	 * @param fileName,
	 *            要读取的文件名
	 * @return long, 行数
	 */
	public static long getLineNum(String fileName) throws Exception {
		long lineNum = 0;
		if (fileName != null) {
			BufferedReader in = null;
			// String lineIn = null;
			// StringBuffer buffer = new StringBuffer();
			try {
				in = new BufferedReader(new FileReader(fileName));
				while ((in.readLine()) != null) {
					lineNum++;
				}
			}
			catch (Exception e) {
				logger.error(e);
			}
			finally {
				IOUtils.closeQuietly(in);
			}
		}
		return lineNum;
	}

	// --得到文件的名字 不包含扩展名
	public static String getName(String fileName) {
		String name = "";
		if (fileName != null && !"".equals(fileName)) {
			int index = fileName.lastIndexOf(".");
			if (index > -1 && index < fileName.length() - 1) {
				name = fileName.substring(0, index);
			}
		}
		return name;
	}



	public static void main(String[] args) throws Exception {
		for(int i=0;i<5; i++) {
			FileUtil.appendTxt("11\n", "E:\\购买记录.txt");
		}
	}

	/**
	 * 创建指定文件所在的目录
	 * 
	 * @param filename
	 * @throws Exception
	 */
	public static final void makeParentDir(String filename) throws Exception {
		File file = new File(filename);
		if (!file.exists()) {
			String parent = file.getParent();
			if (parent != null) {
				new File(parent).mkdirs();
			}
		}
	}

	/**
	 * 用来创建目录
	 * 
	 * @param dirName,
	 *            目录名（或绝对路径文件名）
	 * @return Boolean, 是否创建成功
	 */
	public static boolean mkDir(String dirName) {
		dirName = dirName.replaceAll("\\\\", "/");
		File file = new File(dirName);
		if (file.exists()) // 判断文件或目录是否存在
		{
			if (file.canWrite() == false) // 判断文件或目录是否可写
			{
				return false;
			} else {
				return true;
			}
		} else // 如果不存在就创建目录
		{
			String path = null;

			int firstSlash = dirName.indexOf("/");
			int finalSlash = dirName.lastIndexOf("/");

			if (finalSlash == 0) // 非法路径
			{
				return false;
			} else if (finalSlash == 1) // UNIX系统根目录
			{
				path = File.separator;
			} else if (firstSlash == finalSlash) // 确保文件路径分隔符是路径的一部分
			{
				path = dirName.substring(0, finalSlash + 1);
			} else {
				path = dirName.substring(0, finalSlash);
			}

			File dir = new File(path);
			return dir.mkdirs();
		}
	}

	/**
	 * 从文件中读取二进制字节流
	 * 
	 * @param fileName,
	 *            要读取的文件名
	 * @return Byte Array
	 */
	public static byte[] read(String fileName) throws Exception {
		try {
			return FileUtils.readFileToByteArray(new File(fileName));
		}
		catch (Exception ex) {
			logger.error(ex);
			return null;
		}
	}

	/**
	 * 读取一个文件所有内容
	 * 
	 * @param file
	 *            文件
	 * @return 内容
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readFile(File file) throws FileNotFoundException, IOException {
		StringBuffer list = new StringBuffer();
		String temp = "";

		if (file.exists()) {
			FileReader fr = new FileReader(file);
			java.io.BufferedReader br = new BufferedReader(fr);
			while ((temp = br.readLine()) != null) {
				list.append(temp + "\n");
			}
			br.close();
			fr.close();
		} else {
			return "";
		}

		return list.toString();

	}

	/**
	 * 读文件
	 * 
	 * @param filename
	 *            String 文件名
	 * @param len
	 *            int 返回的字数，如果是0的话，就返回全部内容
	 * @return String
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readFile(String filename, int len) throws FileNotFoundException, IOException {
		StringBuffer list = new StringBuffer();
		String temp = "";
		File file = new File(filename);
		if (file.exists()) {
			FileReader fr = new FileReader(file);
			java.io.BufferedReader br = new BufferedReader(fr);
			while ((temp = br.readLine()) != null) {
				list.append(temp + "\n");
			}
			br.close();
			fr.close();
		} else {
			return "";
		}
		if (len == 0 || list.toString().length() <= len) {// 返回文件的全部内容
			return list.toString();
		} else {// 返回文件的部分内容
			return list.toString().substring(0, len) + "...";
		}
	}

	/**
	 * 从文件中读取第一行文本
	 * 
	 * @param fileName,
	 *            要读取的文件名
	 * @return String, 文件第一行内容
	 */
	public static String readLine(String fileName) throws Exception {
		if (fileName == null) {
			return null;
		}
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fileName));
			String str = in.readLine();
			if (str != null) {
				str = str.trim();
			}
			return str;
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			IOUtils.closeQuietly(in);
		}
		return null;
	}

	public static String readMmsTxt(String fileName) throws Exception {
		if (fileName == null) {
			return null;
		}
		BufferedReader in = null;
		String lineIn = null;
		StringBuffer buffer = new StringBuffer();
		try {
			in = new BufferedReader(new FileReader(fileName));
			while ((lineIn = in.readLine()) != null) {
				buffer.append(lineIn.trim());
				buffer.append("n0n1n");// 该符号会在前台被替换成换行符号
			}
			return buffer.toString();
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			IOUtils.closeQuietly(in);
		}
		return null;
	}

	/**
	 * 从文件中读取文本到缓冲
	 * 
	 * @param fileName,
	 *            要读取的文件名
	 * @return String, 所读文件内容
	 * @throws Exception
	 */
	public static String readTxt(String fileName) throws Exception {
		if (fileName == null) {
			return null;
		}
		BufferedReader in = null;
		String lineIn = null;
		StringBuffer buffer = new StringBuffer();
		try {
			in = new BufferedReader(new FileReader(fileName));
			while ((lineIn = in.readLine()) != null) {
				buffer.append(lineIn.trim());
				buffer.append("\r\n"); // 行号不可少
			}
			return buffer.toString();
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			IOUtils.closeQuietly(in);
		}
		return null;
	}

	/**
	 * 修改文件名称
	 * 
	 * @param srcPath
	 * @param dstPath
	 * @return
	 */
	public static boolean rename(String srcPath, String dstPath) {
		File srcFile = new File(srcPath);
		File dstFile = new File(dstPath);
		boolean flag = srcFile.renameTo(dstFile);
		srcFile = null;
		dstFile = null;
		return flag;
	}

	/**
	 * 文件排序, 文件名为数字,按照文件名排序
	 * 
	 * @param array
	 *            文件数组
	 */
	public static void sortFiles(File[] array) {
		File temp = null;
		boolean condition = false;
		if (array == null || array.length == 0) {
			return;
		}
		for (int i = 0; i < array.length; i++) {
			for (int j = array.length - 1; j > i; j--) {
				condition = Integer.parseInt(array[j].getName().substring(0, array[j].getName().indexOf("."))) < Integer.parseInt(array[j - 1]
				        .getName().substring(0, array[j - 1].getName().indexOf(".")));
				if (condition) {
					temp = array[j];
					array[j] = array[j - 1];
					array[j - 1] = temp;
				}
			}
		}
	}

	/**
	 * 把二进制字节流写入文件
	 * 
	 * @param bytes,
	 *            要写出的字节
	 * @param fileName,
	 *            要写出的文件名
	 */
	public static void write(byte[] bytes, String fileName) throws Exception {
		if (bytes == null || fileName == null) {
			return;
		}
		mkDir(fileName); // 写文件需要检查相关目录是否存在，没有的话需要创建
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(fileName);
			out.write(bytes);
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * 写文件操作
	 * 
	 * @param str,
	 *            要写出的内容
	 * @param fileName,
	 *            文件名
	 * @throws Exception
	 */
	private static void write(String str, String fileName, boolean isAppend) throws Exception {
		if (fileName == null || str == null) {
			return;
		}
		mkDir(fileName); // 写文件需要检查相关目录是否存在，没有的话需要创建
		BufferedWriter out = null;

		try {
			out = new BufferedWriter(new FileWriter(fileName, isAppend));
			out.write(str);
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * 将字符串写出到文本文件，如果文件已存在，将覆盖原来的内容
	 * 
	 * @param str,
	 *            要写出的内容
	 * @param fileName,
	 *            文件名
	 * @throws Exception
	 */
	public static void writeTxt(String str, String fileName) throws Exception {
		write(str, fileName, false);
	}

}

