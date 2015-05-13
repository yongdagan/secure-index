package com.gmail.yongdagan.secure_index.helper;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.gmail.yongdagan.secure_index.dataobject.Index;
import com.gmail.yongdagan.secure_index.dataobject.Pair;
import com.gmail.yongdagan.secure_index.dataobject.TermNode;


public class TestSecureManager {
	
	
	@Test
	public void testTrapdoor() throws Exception {
//		byte[] aesKey = CryptoUtil.initKey(CryptoUtil.AES);
//		byte[] hmacKey = CryptoUtil.initKey(CryptoUtil.MAC);
//		byte[] hmac = CryptoUtil.generateHMAC(hmacKey, "term".getBytes());
		
		byte[] aesKey = new byte[1024];
		byte[] hmac = new byte[128];
		for(int i = 0; i < aesKey.length; i ++) {
			aesKey[i] = (byte) i;
		}
		for(int i = 0; i < hmac.length; i ++) {
			hmac[i] = (byte) i;
		}
		
		byte[] result = new byte[aesKey.length];
//		System.out.println("AES Key: " + CryptoUtil.encodeBASE64(aesKey));
		
		int len = 0;
		if(aesKey.length > hmac.length) {
			len = hmac.length;
		} else {
			len = aesKey.length;
		}
		for(int i = 0; i < len; i ++) {
			result[i] = (byte) (aesKey[i] ^ hmac[i]);
		}
		for(int i = len; i < aesKey.length; i ++) {
			result[i] = aesKey[i];
		}
//		System.out.println("Trapdoor: " + CryptoUtil.encodeBASE64(result));
		
		if(result.length > hmac.length) {
			len = hmac.length;
		} else {
			len = result.length;
		}
		byte[] key = new byte[result.length];
		for(int i = 0; i < len; i ++) {
			key[i] = (byte) (result[i] ^ hmac[i]);
		}
		for(int i = len; i < key.length; i ++) {
			key[i] = result[i];
		}
//		System.out.println("Key: " + CryptoUtil.encodeBASE64(key));
		
		assertArrayEquals(aesKey, key);
	}
	
	@Test
	public void testAesDocList() throws Exception {
		List<Long> scoreList = new ArrayList<Long>();
		for(int i = 0; i < 1; i ++) {
			scoreList.add(i * 1024L);
		}
		
		StringBuffer buffer = new StringBuffer();
		Paillier paillier = new Paillier(Paillier.BIT_LENGTH);
		paillier.generateKeys();
//		System.out.print("scoreList:");
		for(int i = 0; i < scoreList.size(); i ++) {
//			System.out.print(" " + i + ":" + scoreList.get(i).intValue());
			BigInteger score = paillier.encrypt(new BigInteger(Long.toString(scoreList.get(i))));
			buffer.append(CryptoUtil.encodeBASE64(Integer.toString(i).getBytes()) + " "
					+ CryptoUtil.encodeBASE64(score.toByteArray()) + " ");
		}
//		System.out.println();
		byte[] key = CryptoUtil.initKey(CryptoUtil.AES);
		String result = buffer.substring(0, buffer.length() - 1);
		result = CryptoUtil.encodeBASE64(CryptoUtil.encryptAES(key, result.getBytes()));
		try(BufferedWriter writer = Files.newBufferedWriter(Paths.get("/home/yongdagan/tttt.txt"), Charset.defaultCharset())) {
			writer.write(" " + result + " ");
			writer.newLine();
		}
//		System.out.println("encrypt: " + result);
		
		String value = null;
		try(BufferedReader reader = Files.newBufferedReader(Paths.get("/home/yongdagan/tttt.txt"), Charset.defaultCharset())) {
			value = reader.readLine();
		}
		result = new String(CryptoUtil.decryptAES(key, CryptoUtil.decodeBASE64(value.split(" ")[1])));
		
//		System.out.println(result);
		String[] buf = result.split(" ");
//		System.out.print("decrypt:");
		for(int i = 0; i < buf.length; i += 2) {
//			Integer docId = new Integer(new String(CryptoUtil.decodeBASE64(buf[i])));
//			BigInteger score = new BigInteger(CryptoUtil.decodeBASE64(buf[i + 1]));
//			System.out.print(" " + docId.intValue() + ":" + score.intValue());
//			assertEquals(i/2, docId.intValue());
//			assertEquals(scoreList.get(i/2).longValue(), paillier.decrypt(score).longValue());
		}
//		System.out.println();
	}
	
	@Test
	public void indexFile() throws Exception  {
		Path inputPath = Paths.get("/home/yongdagan/tmp");
		Path outputPath = Paths.get("/home/yongdagan/xx");
		Index index = AnalyzerUtil.stemming(0, inputPath);
		
		try (BufferedWriter writer = Files.newBufferedWriter(
				outputPath.resolve("decryptFile.txt"), Charset.defaultCharset())) {
			// write docId : docName
			List<Pair<Integer, String>> docIds = index.getDocIds();
			for(Pair<Integer, String> x : docIds) {
				writer.write(x.getFirst() + " " + x.getSecond());
				writer.newLine();
			}
			writer.newLine();
			// write index
			Set<Map.Entry<String, TermNode>> terms = index.getTerms();
			double docNum = index.getDocNum();
			for (Map.Entry<String, TermNode> term : terms) {
				String termName = term.getKey();
				TermNode termNode = term.getValue();
				try {
					List<Pair<Integer, Long>> docs = termNode.getDocs();
					double docNumOfTerm = termNode.getSize();
					StringBuffer buffer = new StringBuffer();
					for (Pair<Integer, Long> doc : docs) {
						int docId = doc.getFirst();
						double tmpScore = doc.getSecond().doubleValue();
						tmpScore = (1 + Math.log(tmpScore))
								* Math.log(docNum / docNumOfTerm);
						long docScore = (long) (tmpScore * 100);
						buffer.append(docId + " " + docScore
								);
					}
					String result = buffer.substring(0, buffer.length() - 1);
					// write
					writer.write(termName + "\t\t"
							+ result);
					writer.newLine();
				} catch (Exception e) {
					// ignore
				}
			}
		}
	}

	
}