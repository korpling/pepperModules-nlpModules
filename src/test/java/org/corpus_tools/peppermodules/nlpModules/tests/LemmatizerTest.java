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
package org.corpus_tools.peppermodules.nlpModules.tests;

/**
 *
 * @author Amir Zeldes
 */


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.corpus_tools.pepper.testFramework.PepperManipulatorTest;
import org.corpus_tools.peppermodules.nlpModules.Lemmatizer;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

public class LemmatizerTest extends PepperManipulatorTest
{	
	URI resourceURI= URI.createFileURI(new File(".").getAbsolutePath());
	URI temproraryURI= URI.createFileURI(System.getProperty("java.io.tmpdir"));	
	
	@Before
	public void setUp() throws Exception 
	{
		super.setFixture(new Lemmatizer());
		
		super.getFixture().setSaltProject(SaltFactory.createSaltProject());
		super.setResourcesURI(resourceURI);
		
		//setting temproraries and resources
		getFixture().setResources(resourceURI);
	}
	
	/**
	 * two {@link SDocument} objects containing one {@link STextualDS} each. 
	 */
	@Test
	public void testLemma()
	{
		SCorpusGraph importedSCorpusGraph= SaltFactory.createSCorpusGraph();
		getFixture().getSaltProject().addCorpusGraph(importedSCorpusGraph);
		
		String sText="examples";
		
		SDocument sDoc1= SaltFactory.createSDocument();
		sDoc1.setDocumentGraph(SaltFactory.createSDocumentGraph());
		STextualDS sTextualDS = sDoc1.getDocumentGraph().createTextualDS(sText);
		
		
		SCorpus corp1= SaltFactory.createSCorpus();
		importedSCorpusGraph.addNode(corp1);
		importedSCorpusGraph.addDocument(corp1, sDoc1);

                SToken tok = SaltFactory.createSToken();
                tok.setGraph(sDoc1.getDocumentGraph());
                STextualRelation sTextualRelation = SaltFactory.createSTextualRelation();
                sTextualRelation.setSource(tok);
                sTextualRelation.setTarget(sTextualDS);
                sTextualRelation.setStart(0);
                sTextualRelation.setEnd("examples".length()-1);
                sTextualRelation.setGraph(sDoc1.getDocumentGraph());

		//runs the PepperModule
		this.start();
		
                // check that the document and token were created as expected
                assertNotNull(sDoc1.getDocumentGraph().getTokens());
		assertEquals(1, sDoc1.getDocumentGraph().getTokens().size());
                // check that the lemma of 'examples' is 'example'
		assertEquals("example", sDoc1.getDocumentGraph().getTokens().get(0).getAnnotation("default_ns", "lemma").getValue_STEXT());
	}
	
}
