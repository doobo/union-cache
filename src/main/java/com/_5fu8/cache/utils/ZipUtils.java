package com._5fu8.cache.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;

/**
 * @author qpc
 */
@Slf4j
public class ZipUtils {

	private final static String NAME = "ZipUtilsError";

	private ZipUtils() {
	}

	/**
	 *
	 * 使用gzip进行压缩
	 */
	public static String gzip(String primStr) {
		if (primStr == null || primStr.length() == 0) {
			return primStr;
		}
		try(ByteArrayOutputStream out = new ByteArrayOutputStream();
			GZIPOutputStream gzip = new GZIPOutputStream(out);){
			gzip.write(primStr.getBytes(StandardCharsets.UTF_8.name()));
			String gzipStr = new sun.misc.BASE64Encoder().encode(out.toByteArray());
			return gzipStr;
		} catch (IOException e) {
			log.error(NAME,e);
		}
		return null;
	}

	/**
	 *
	 * <p>
	 * Description:使用gzip进行解压缩
	 * </p>
	 *
	 * @param compressedStr
	 * @return
	 */
	public static String gunZip(String compressedStr) {
		if (compressedStr == null) {
			return null;
		}

		ByteArrayInputStream in = null;
		GZIPInputStream ginZip = null;
		byte[] compressed = null;
		String decompressed = null;
		try(ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			compressed = new sun.misc.BASE64Decoder().decodeBuffer(compressedStr);
			in = new ByteArrayInputStream(compressed);
			ginZip = new GZIPInputStream(in);

			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = ginZip.read(buffer)) != -1) {
				out.write(buffer, 0, offset);
			}
			decompressed = new String(out.toByteArray(),StandardCharsets.UTF_8.name());
		} catch (IOException e) {
			log.error(NAME,e);
		} finally {
			if (ginZip != null) {
				try {
					ginZip.close();
				} catch (IOException e) {
					log.error(NAME,e);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.error(NAME,e);
				}
			}
		}
		return decompressed;
	}

	/**
	 * 使用zip进行压缩
	 *
	 * @param str
	 * 压缩前的文本
	 * @return 返回压缩后的文本
	 */
	public static final String zip(String str) {
		if(str == null) {
			return null;
		}
		byte[] compressed;
		ByteArrayOutputStream out = null;
		ZipOutputStream zout = null;
		String compressedStr = null;
		try{
			out = new ByteArrayOutputStream();
			zout = new ZipOutputStream(out);
			zout.putNextEntry(new ZipEntry("0"));
			zout.write(str.getBytes(StandardCharsets.UTF_8.name()));
			zout.closeEntry();
			compressed = out.toByteArray();
			compressedStr = new sun.misc.BASE64Encoder().encodeBuffer(compressed);
		} catch (IOException e) {
			log.error(NAME,e);
		} finally {
			if (zout != null) {
				try {
					zout.close();
				} catch (IOException e) {
					log.error(NAME,e);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					log.error(NAME,e);
				}
			}
		}
		return compressedStr;
	}

	/**
	 * 使用zip进行解压缩
	 * 压缩后的文本
	 * @return 解压后的字符串
	 */
	public static final String unzip(String compressedStr) {
		if (compressedStr == null) {
			return null;
		}
		ByteArrayOutputStream out = null;
		ByteArrayInputStream in = null;
		ZipInputStream zin = null;
		String decompressed = null;
		try {
			byte[] compressed = new sun.misc.BASE64Decoder().decodeBuffer(compressedStr);
			out = new ByteArrayOutputStream();
			in = new ByteArrayInputStream(compressed);
			zin = new ZipInputStream(in);
			zin.getNextEntry();
			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = zin.read(buffer)) != -1) {
				out.write(buffer, 0, offset);
			}
			decompressed = new String(out.toByteArray(),StandardCharsets.UTF_8.name());
		} catch (IOException e) {
			log.error(NAME,e);
		} finally {
			if (zin != null) {
				try {
					zin.close();
				} catch (IOException e) {
					log.error(NAME,e);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.error(NAME,e);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					log.error(NAME,e);
				}
			}
		}
		return decompressed;
	}
}
