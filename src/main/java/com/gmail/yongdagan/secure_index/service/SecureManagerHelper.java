package com.gmail.yongdagan.secure_index.service;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import com.gmail.yongdagan.secure_index.dataobject.BitArray;
import com.gmail.yongdagan.secure_index.dataobject.Index;
import com.gmail.yongdagan.secure_index.dataobject.Pair;
import com.gmail.yongdagan.secure_index.dataobject.TermNode;
import com.gmail.yongdagan.secure_index.helper.CryptoUtil;
import com.gmail.yongdagan.secure_index.helper.Paillier;

class SecureManagerHelper {
	
	public static void generateIndexFile(Index index, Path outputPath, 
			byte[] hmacKey1, byte[] hamcKey2, String[] paiKey) throws Exception  {
		if (index.isEmpty()) return;

		Paillier paillier = new Paillier(Paillier.BIT_LENGTH,
				paiKey[0], paiKey[1], paiKey[2], paiKey[3]);
		
		try (BufferedWriter writer = Files.newBufferedWriter(
				outputPath.resolve("indexFile.txt"), Charset.defaultCharset())) {
			// docId : docName
			List<Pair<Integer, String>> docIds = index.getDocIds();
			for(Pair<Integer, String> x : docIds) {
				writer.write(x.getFirst() + " " + x.getSecond() + "\n");
			}
			writer.write("\n");

			Set<Map.Entry<String, TermNode>> terms = index.getTerms();
			double docNum = index.getDocNum();
			for (Map.Entry<String, TermNode> term : terms) {
				byte[] termName = term.getKey().getBytes();
				TermNode termNode = term.getValue();
				// encrypt
				byte[] termHmac = CryptoUtil.generateHMAC(hmacKey1, termName);
				try {
					// generate aesKey of this term
					byte[] aesKey = CryptoUtil.initKey(CryptoUtil.AES);
					// save aesKey with trapDoor
					byte[] trapDoorUsed = CryptoUtil.generateHMAC(hamcKey2,
							termName);
					byte[] trapdoor = new byte[aesKey.length];
					int len = 0;
					if (aesKey.length > trapDoorUsed.length) {
						len = trapDoorUsed.length;
					} else {
						len = aesKey.length;
					}
					for (int i = 0; i < len; i++) {
						trapdoor[i] = (byte) (aesKey[i] ^ trapDoorUsed[i]);
					}
					for(int i = len; i < trapdoor.length; i ++) {
						trapdoor[i] = aesKey[i];
					}
					
					// encrypt termNode
					List<Pair<Integer, Long>> docs = termNode.getDocs();
					double docNumOfTerm = termNode.getSize();
					
					ArrayList<Byte> scoreList = new ArrayList<Byte>();
					// gamma encode docId
					BitArray idBits = new BitArray();
					Pair<Integer, Long> doc1 = docs.get(0);
					Integer x = doc1.getFirst();
					gammaEncode(x, idBits);
					BigInteger c = computeScore(doc1.getSecond(), docNum, docNumOfTerm, paillier);
					encodeInteger(c, scoreList);
					for (int i = 1; i < docs.size(); i ++) {
						Pair<Integer, Long> doc = docs.get(i);
						gammaEncode(doc.getFirst() - x, idBits);
						x = doc.getFirst();
						// get score
						c = computeScore(doc.getSecond(), docNum, docNumOfTerm, paillier);
						encodeInteger(c, scoreList);
					}
					byte[] aesIds = CryptoUtil.encryptAES(aesKey, idBits.toByteArray());
					String ids = CryptoUtil.encodeBASE64(aesIds);
					
					byte[] data = new byte[scoreList.size()];
					for(int i = 0; i < data.length; i ++) {
						data[i] = scoreList.get(i);
					}
					String tmp = CryptoUtil.encodeBASE64(CryptoUtil.encryptAES(aesKey, data));

					writer.write(CryptoUtil.encodeBASE64(termHmac) + " "
							+ CryptoUtil.encodeBASE64(trapdoor) + " "
							+  ids + " " + tmp + "\n");
				} catch (NoSuchAlgorithmException e) {
					// ignore
				}
			}
		}
	}
	
	private static void gammaEncode(Integer x, BitArray bitArray) {
		String part2 = Integer.toBinaryString(x).substring(1);
		int len = part2.length();
		for(int j = 0; j < len; j ++) {
			bitArray.push('1');
		}
		bitArray.push('0');
		bitArray.push(part2);
	}
	
	private static BigInteger computeScore(Long x, double docNum,
			double docNumOfTerm, Paillier paillier) throws Exception {
		double tmpScore = x.doubleValue();
		tmpScore = (1 + Math.log(tmpScore))
				* Math.log(docNum / docNumOfTerm);
		long docScore = (long) (tmpScore * 100);
		// encrypt docScore
		BigInteger c = paillier.encrypt(new BigInteger(Long
				.toString(docScore)));
		return c;
	}

	public static void encryptFiles(Path inputPath, final Path outputPath,
			final byte[] aesKey) throws Exception {
		if (Files.isDirectory(inputPath)) {
			Files.walkFileTree(inputPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path path,
						BasicFileAttributes attrs) throws IOException {
					if (path.getFileName().toString().endsWith(".txt")) {
						try {
							encryptFile(path, outputPath, aesKey);
						} catch (Exception e) {
							throw new IOException(e);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			encryptFile(inputPath, outputPath, aesKey);
		}
	}
	
	public static void decryptFiles(Path inputPath, final Path outputPath,
			final byte[]aesKey) throws Exception {
		if (Files.isDirectory(inputPath)) {
			Files.walkFileTree(inputPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path path,
						BasicFileAttributes attrs) throws IOException {
					if (path.getFileName().toString().endsWith(".dat")) {
						try {
							decryptFile(path, outputPath, aesKey);
						} catch (Exception e) {
							throw new IOException(e);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			decryptFile(inputPath, outputPath, aesKey);
		}
	}
	
	private static void decryptFile(Path file, Path outputPath, byte[] aesKey) throws Exception {
		try (InputStream in = Files.newInputStream(file)) {
			String fileName = file.getFileName().toString();
			fileName = fileName.substring(0, fileName.lastIndexOf("."))
					+ ".txt";
			
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			CryptoUtil.decryptStreamAES(in, byteStream, aesKey);
			decompressFile(byteStream.toByteArray(), outputPath.resolve(fileName));
		}
	}
	
	private static void encryptFile(Path file, Path outputPath, byte[] aesKey) throws Exception {
		try (InputStream in = Files.newInputStream(file)) {
			String fileName = file.getFileName().toString();
			fileName = fileName.substring(0, fileName.lastIndexOf("."))
					+ ".dat";
			
			try (OutputStream out = Files.newOutputStream(outputPath.resolve(fileName))) {
				byte[] b = CryptoUtil.encryptAES(aesKey, compressFile(file));
				out.write(b);
			}
		}
	}
	
	private static byte[] compressFile(Path file) throws IOException {
		try(InputStream inputStream = Files.newInputStream(file)) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			try(OutputStream outputStream = new GzipCompressorOutputStream(byteArrayOutputStream)) {
				int tmp = 0;
				while((tmp = inputStream.read()) != -1) {
					outputStream.write(tmp);
				}
			}
			return byteArrayOutputStream.toByteArray();
		}
	}
	
	private static void decompressFile(byte[] bytes, Path file) throws Exception {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		try(InputStream inputStream = new GzipCompressorInputStream(byteArrayInputStream)) {
			try(OutputStream outputStream = Files.newOutputStream(file)) {
				int tmp = 0;
				while((tmp = inputStream.read()) != -1) {
					outputStream.write(tmp);
				}
			}
		}
	}
	
    private static void encodeInteger(final BigInteger value, ArrayList<Byte> list) {  
    	final String MASK = "127";
        BigInteger tmpValue = value;
        do {  
            byte b = tmpValue.and(new BigInteger(MASK)).byteValue();  
            tmpValue = tmpValue.shiftRight(7);  
            if (!tmpValue.equals(BigInteger.ZERO)) {  
                b |= (byte) 1<<7;  
            }  
            list.add(b);
        } while (!tmpValue.equals(BigInteger.ZERO));  
    }
    
}
