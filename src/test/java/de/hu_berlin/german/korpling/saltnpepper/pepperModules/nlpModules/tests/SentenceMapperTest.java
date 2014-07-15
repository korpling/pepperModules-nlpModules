/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
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

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.junit.Before;
import org.junit.Test;

import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
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
		getFixture().setSDocument(SaltFactory.eINSTANCE.createSDocument());
		getFixture().getSDocument().setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
	}

	@Test
	public void test() {
		String text= "Sentence number one. Sentence number two! Sentence etc. number three?";
		STextualDS textualDS= getFixture().getSDocument().getSDocumentGraph().createSTextualDS(text);
		SToken t1= getFixture().getSDocument().getSDocumentGraph().createSToken(textualDS, 0, 8);
		SToken t2= getFixture().getSDocument().getSDocumentGraph().createSToken(textualDS, 9, 15);
		SToken t3= getFixture().getSDocument().getSDocumentGraph().createSToken(textualDS, 16, 19);
		SToken t4= getFixture().getSDocument().getSDocumentGraph().createSToken(textualDS, 19, 20);
		SToken t5= getFixture().getSDocument().getSDocumentGraph().createSToken(textualDS, 21, 29);
		SToken t6= getFixture().getSDocument().getSDocumentGraph().createSToken(textualDS, 30, 36);
		SToken t7= getFixture().getSDocument().getSDocumentGraph().createSToken(textualDS, 37, 40);
		SToken t8= getFixture().getSDocument().getSDocumentGraph().createSToken(textualDS, 40, 41);
		SToken t9= getFixture().getSDocument().getSDocumentGraph().createSToken(textualDS, 42, 50);
		SToken t10= getFixture().getSDocument().getSDocumentGraph().createSToken(textualDS, 51, 55);
		SToken t11= getFixture().getSDocument().getSDocumentGraph().createSToken(textualDS, 56, 62);
		SToken t12= getFixture().getSDocument().getSDocumentGraph().createSToken(textualDS, 63, 68);
		SToken t13= getFixture().getSDocument().getSDocumentGraph().createSToken(textualDS, 68, 69);
		
		getFixture().mapSDocument();
		
		assertEquals(3, getFixture().getSDocument().getSDocumentGraph().getSSpans().size());
		SSpan span1= getFixture().getSDocument().getSDocumentGraph().getSSpans().get(0);
		SSpan span2= getFixture().getSDocument().getSDocumentGraph().getSSpans().get(1);
		SSpan span3= getFixture().getSDocument().getSDocumentGraph().getSSpans().get(2);
		EList<STYPE_NAME> relType= new BasicEList<STYPE_NAME>();
		relType.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
		List<SToken> tokens= null;
		
		tokens= getFixture().getSDocument().getSDocumentGraph().getOverlappedSTokens(span1, relType);
		assertTrue(tokens.contains(t1));
		assertTrue(tokens.contains(t2));
		assertTrue(tokens.contains(t3));
		assertTrue(tokens.contains(t4));
		
		tokens= getFixture().getSDocument().getSDocumentGraph().getOverlappedSTokens(span2, relType);
		assertTrue(tokens.contains(t5));
		assertTrue(tokens.contains(t6));
		assertTrue(tokens.contains(t7));
		assertTrue(tokens.contains(t8));
		
		tokens= getFixture().getSDocument().getSDocumentGraph().getOverlappedSTokens(span3, relType);
		assertTrue(tokens.contains(t9));
		assertTrue(tokens.contains(t10));
		assertTrue(tokens.contains(t11));
		assertTrue(tokens.contains(t12));
		assertTrue(tokens.contains(t13));
	}

}
