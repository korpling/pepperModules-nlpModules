/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.nlpModules.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.junit.Before;
import org.junit.Test;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.nlpModules.Sentencer.SentenceMapper;

public class SentenceMapperTest {

	private SentenceMapper fixture= null;
	
	public SentenceMapper getFixture() {
		return fixture;
	}

	public void setFixture(SentenceMapper fixture) {
		this.fixture = fixture;
	}

	@Before
	public void setUp() throws Exception {
		setFixture(new SentenceMapper());
		getFixture().setDocument(SaltFactory.createSDocument());
		getFixture().getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());
	}

	@Test
	public void test() {
		String text= "Sentence number one. Sentence number two! Sentence etc. number three?";
		STextualDS textualDS= getFixture().getDocument().getDocumentGraph().createTextualDS(text);
		SToken t1= getFixture().getDocument().getDocumentGraph().createToken(textualDS, 0, 8);
		SToken t2= getFixture().getDocument().getDocumentGraph().createToken(textualDS, 9, 15);
		SToken t3= getFixture().getDocument().getDocumentGraph().createToken(textualDS, 16, 19);
		SToken t4= getFixture().getDocument().getDocumentGraph().createToken(textualDS, 19, 20);
		SToken t5= getFixture().getDocument().getDocumentGraph().createToken(textualDS, 21, 29);
		SToken t6= getFixture().getDocument().getDocumentGraph().createToken(textualDS, 30, 36);
		SToken t7= getFixture().getDocument().getDocumentGraph().createToken(textualDS, 37, 40);
		SToken t8= getFixture().getDocument().getDocumentGraph().createToken(textualDS, 40, 41);
		SToken t9= getFixture().getDocument().getDocumentGraph().createToken(textualDS, 42, 50);
		SToken t10= getFixture().getDocument().getDocumentGraph().createToken(textualDS, 51, 55);
		SToken t11= getFixture().getDocument().getDocumentGraph().createToken(textualDS, 56, 62);
		SToken t12= getFixture().getDocument().getDocumentGraph().createToken(textualDS, 63, 68);
		SToken t13= getFixture().getDocument().getDocumentGraph().createToken(textualDS, 68, 69);
		
		getFixture().mapSDocument();
		
		assertEquals(3, getFixture().getDocument().getDocumentGraph().getSpans().size());
		SSpan span1= getFixture().getDocument().getDocumentGraph().getSpans().get(0);
		SSpan span2= getFixture().getDocument().getDocumentGraph().getSpans().get(1);
		SSpan span3= getFixture().getDocument().getDocumentGraph().getSpans().get(2);
		List<SALT_TYPE> relType= new ArrayList<SALT_TYPE>();
		relType.add(SALT_TYPE.STEXT_OVERLAPPING_RELATION);
		List<SToken> tokens= null;
		
		tokens= getFixture().getDocument().getDocumentGraph().getOverlappedTokens(span1, relType);
		assertTrue(tokens.contains(t1));
		assertTrue(tokens.contains(t2));
		assertTrue(tokens.contains(t3));
		assertTrue(tokens.contains(t4));
		
		tokens= getFixture().getDocument().getDocumentGraph().getOverlappedTokens(span2, relType);
		assertTrue(tokens.contains(t5));
		assertTrue(tokens.contains(t6));
		assertTrue(tokens.contains(t7));
		assertTrue(tokens.contains(t8));
		
		tokens= getFixture().getDocument().getDocumentGraph().getOverlappedTokens(span3, relType);
		assertTrue(tokens.contains(t9));
		assertTrue(tokens.contains(t10));
		assertTrue(tokens.contains(t11));
		assertTrue(tokens.contains(t12));
		assertTrue(tokens.contains(t13));
	}

}
