package com.gmail.yongdagan.secure_index.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import com.gmail.yongdagan.secure_index.dataobject.Index;
import com.gmail.yongdagan.secure_index.helper.AnalyzerUtil;
import com.gmail.yongdagan.secure_index.helper.CryptoUtil;
import com.gmail.yongdagan.secure_index.helper.Paillier;

public class SecureManager {

	private static void genKey(Path keyFile, Path serverKey) {
		// generate key: aeskey, hmacKey1, hmacKey2, paiKey
		try {
			Paillier paillier = new Paillier(Paillier.BIT_LENGTH);
			paillier.generateKeys();
			
			byte[][] keys = new byte[3][];
			keys[0] = CryptoUtil.initKey(CryptoUtil.AES);		// aesKey
			keys[1] = CryptoUtil.initKey(CryptoUtil.MAC);		// hmacKey1
			keys[2] = CryptoUtil.initKey(CryptoUtil.MAC);		// hmacKey2
			String[] paiKey = new String[4];
			paiKey[0] = paillier.getN();			// paiKeyN							
			paiKey[1] = paillier.getG();			// paiKeyG
			paiKey[2] = paillier.getLambda();		// paiKeyLambda
			paiKey[3] = paillier.getMu();			// paiKeyMu
			String addKey = paillier.getNsquare();
			// generate keyFile
			try (BufferedWriter writer = Files.newBufferedWriter(keyFile, Charset.defaultCharset())) {
				for(int i = 0; i < keys.length; i ++) {
					writer.write(CryptoUtil.encodeBASE64(keys[i]));
					writer.newLine();
				}
				for(int i = 0; i < paiKey.length; i ++) {
					writer.write(paiKey[i]);
					writer.newLine();
				}
			} catch (IOException e) {
				System.out.println("I/O ERROR");
			}
			// generate serverKey
			try (BufferedWriter writer = Files.newBufferedWriter(serverKey, Charset.defaultCharset())) {
				writer.write(addKey);
			} catch (IOException e) {
				System.out.println("I/O ERROR");
			}
		} catch (NoSuchAlgorithmException e) {
			// ignore
		}
	}
	
	private static void secureIndex(Path inputPath, Path outputPath, Path keyFile, int startId) {
		try (BufferedReader reader = Files.newBufferedReader(keyFile, Charset.defaultCharset())) {
			// read keys
			byte[][] keys = new byte[3][];
			String tmp = null;
			for(int i = 0; i < keys.length; i ++) {
				tmp = reader.readLine();
				keys[i] = CryptoUtil.decodeBASE64(tmp);
			}
			String[] paiKey = new String[4];
			for(int i = 0; i < paiKey.length; i ++) {
				tmp = reader.readLine();
				paiKey[i] = tmp;
			}
			final byte[] aesKey = keys[0];
			byte[] hmacKey1 = keys[1];
			byte[] hmacKey2 = keys[2];
			final Path in = inputPath;
			final Path out = outputPath;
			// encrypt files
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						SecureManagerHelper.encryptFiles(in, out, aesKey);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("SYSTEM ERROR");
					}
				}
			});
			thread.start();
			// generate index
			Index index = AnalyzerUtil.stemming(startId, inputPath);
			SecureManagerHelper.generateIndexFile(index, outputPath, hmacKey1, hmacKey2, paiKey);
			while(!thread.getState().equals(Thread.State.TERMINATED)) {
				Thread.sleep(100);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("I/O ERROR");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SYSTEM ERROR");
		}
	}
	
	private static void decrypt(Path inputPath, Path outputPath, Path keyFile) {
		try (BufferedReader reader = Files.newBufferedReader(keyFile, Charset.defaultCharset())) {
			// read aesKey
			String tmp = reader.readLine();
			byte[] aesKey = CryptoUtil.decodeBASE64(tmp);
			SecureManagerHelper.decryptFiles(inputPath, outputPath, aesKey);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println("SYSTEM ERROR");
		}
	}

	public static void main(String[] args) {
		final String ma = "missing args";
		final String sd = " should be a directory";
		final String sf = " should be a file";
		if (args.length < 1 || args[0].equals("-help")) {
			System.out.println("-genkey outputPath");
			System.out.println("-secureindex inputPath outputPath keyFile startId");
			System.out.println("-decrypt inputPath outputPath keyFile");

		} else if (args[0].equals("-genkey")) {
			if (args.length < 2) {
				System.out.println(ma);
			} else {
				Path keyFile = Paths.get(args[1]);
				Path serverKey = Paths.get(args[1]);
				if (Files.isDirectory(keyFile)) {
					keyFile = keyFile.resolve("keyFile.txt");
					serverKey = serverKey.resolve("serverKey.txt");
				} else {
					System.out.println("\"" + keyFile + "\"" + sd);
					return;
				}
				genKey(keyFile, serverKey);
				System.out.println("Completed!");
			}

		} else {
			if (args.length < 4) {
				System.out.println(ma);
				return;
			}
			Path inputPath = Paths.get(args[1]);
			Path outputPath = Paths.get(args[2]);
			Path keyFile = Paths.get(args[3]);
			if(!Files.isDirectory(outputPath)) {
				System.out.println("\"" + outputPath + "\"" + sd);
				return;
			}
			if(Files.isDirectory(keyFile)) {
				System.out.println("\"" + keyFile + "\"" + sf);
				return;
			}
			if(args[0].equals("-secureindex")) {
				// generate indexFile and encrypt files
				int startId = 1;
				if(args.length > 4) {
					startId = Integer.valueOf(args[4]);
				}
				secureIndex(inputPath, outputPath, keyFile, startId);
			} else if(args[0].equals("-decrypt")) {
				// decrypt files
				decrypt(inputPath, outputPath, keyFile);
			}
			System.out.println("Completed!");
		}
		
	}

}
