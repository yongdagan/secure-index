package com.gmail.yongdagan.secure_index.service;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.gmail.yongdagan.secure_index.helper.CryptoUtil;

public class TestSecureManagerHelper {
	
	@Test
	public void testEncryptAndCompress() throws Exception {
		Path inputFile = Paths.get("/home/yongdagan/rc.txt");
		Path outputFile = Paths.get("/home/yongdagan/");
		byte[] aesKey = CryptoUtil.decodeBASE64("8JMniEID46lN9XPWHuMTkQ==");
		SecureManagerHelper.encryptFiles(inputFile, outputFile, aesKey);
		outputFile = Paths.get("/home/yongdagan/");
		SecureManagerHelper.decryptFiles(outputFile.resolve("rc.dat"), outputFile.resolve("xx"), aesKey);
	}
	

}
