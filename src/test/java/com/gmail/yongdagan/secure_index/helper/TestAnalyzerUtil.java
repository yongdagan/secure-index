package com.gmail.yongdagan.secure_index.helper;

//import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.gmail.yongdagan.secure_index.dataobject.Index;
import com.gmail.yongdagan.secure_index.dataobject.Pair;
import com.gmail.yongdagan.secure_index.dataobject.TermNode;

public class TestAnalyzerUtil {
	
	@Test
	public void testStemming() throws Exception {
		Index index = AnalyzerUtil.stemming(0, Paths.get("/home", "yongdagan", "tmp"));
		Set<Map.Entry<String, TermNode>> terms = index.getTerms();
		List<Pair<Integer, String>> docIds = index.getDocIds();
		
		BufferedWriter writer = Files.newBufferedWriter(Paths.get("/home/yongdagan/test.txt"), Charset.defaultCharset());
		for(Pair<Integer, String> x : docIds) {
			writer.write(x.getFirst() + " : " + x.getSecond() + "\n");
		}
		for(Map.Entry<String, TermNode> term : terms) {
			writer.write(term.getKey() + " : ");
			TermNode termNode = term.getValue();
			
			List<Pair<Integer, Long>> docs = termNode.getDocs();
			for(Pair<Integer, Long> doc : docs) {
				writer.write(doc.getFirst() + " : " + doc.getSecond() + "\t");
//				assertEquals(0L, docId.longValue());
			}
			writer.write("\n");
		}
		writer.write(terms.size() + "\n");
		writer.close();
	}

}
