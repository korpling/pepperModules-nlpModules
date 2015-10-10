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
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.PrintWriter;

import org.corpus_tools.pepper.testFramework.PepperManipulatorTest;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.STextualDS;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.nlpModules.Tokenizer;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.nlpModules.TokenizerProperties;

public class TokenizerTest extends PepperManipulatorTest
{	
	URI resourceURI= URI.createFileURI(new File(".").getAbsolutePath());
	URI temproraryURI= URI.createFileURI(System.getProperty("java.io.tmpdir"));	
	
	@Before
	public void setUp() throws Exception 
	{
		super.setFixture(new Tokenizer());
		
		super.getFixture().setSaltProject(SaltFactory.createSaltProject());
		super.setResourcesURI(resourceURI);
		
		//setting temproraries and resources
		getFixture().setResources(resourceURI);
	}
	
	/**
	 * two {@link SDocument} objects containing one {@link STextualDS} each. 
	 */
	@Test
	public void testCase1()
	{
		SCorpusGraph importedSCorpusGraph= SaltFactory.createSCorpusGraph();
		getFixture().getSaltProject().addCorpusGraph(importedSCorpusGraph);
		
		String sText="Is this example more complicated, than it is supposed to be?";
		
		SDocument sDoc1= SaltFactory.createSDocument();
		sDoc1.setDocumentGraph(SaltFactory.createSDocumentGraph());
		sDoc1.getDocumentGraph().createTextualDS(sText);
		
		SDocument sDoc2= SaltFactory.createSDocument();
		sDoc2.setDocumentGraph(SaltFactory.createSDocumentGraph());
		sDoc2.getDocumentGraph().createTextualDS(sText);
		
		SCorpus corp1= SaltFactory.createSCorpus();
		importedSCorpusGraph.addNode(corp1);
		importedSCorpusGraph.addDocument(corp1, sDoc1);
		importedSCorpusGraph.addDocument(corp1, sDoc2);
		
		//runs the PepperModule
		this.start();
		
		assertNotNull(sDoc1.getDocumentGraph().getTokens());
		assertEquals(13, sDoc1.getDocumentGraph().getTokens().size());
	}
	
	/**
	 * one {@link SDocument} objects containing two {@link STextualDS} objects. 
	 */
	@Test
	public void testCase2()
	{
		SCorpusGraph importedSCorpusGraph= SaltFactory.createSCorpusGraph();
		getFixture().getSaltProject().addCorpusGraph(importedSCorpusGraph);
		
		String sText="Is this example more complicated, than it is supposed to be?";
		
		SDocument sDoc1= SaltFactory.createSDocument();
		sDoc1.setDocumentGraph(SaltFactory.createSDocumentGraph());
		sDoc1.getDocumentGraph().createTextualDS(sText);
		sDoc1.getDocumentGraph().createTextualDS(sText);
		
		SCorpus corp1= SaltFactory.createSCorpus();
		importedSCorpusGraph.addNode(corp1);
		importedSCorpusGraph.addDocument(corp1, sDoc1);
		
		//runs the PepperModule
		this.start();
		
		assertNotNull(sDoc1.getDocumentGraph().getTokens());
		assertEquals(26, sDoc1.getDocumentGraph().getTokens().size());
	}
	
	/**
	 * one {@link SDocument} objects containing one {@link STextualDS} object with strange abbreviations. 
	 * @throws Exception 
	 */
	@Test
	public void testCase3() throws Exception
	{
		SCorpusGraph importedSCorpusGraph= SaltFactory.createSCorpusGraph();
		getFixture().getSaltProject().addCorpusGraph(importedSCorpusGraph);
		
		String sText="Is this. example more complicated, than. it is supposed to be?";
		
		SDocument sDoc1= SaltFactory.createSDocument();
		sDoc1.setDocumentGraph(SaltFactory.createSDocumentGraph());
		sDoc1.getDocumentGraph().createTextualDS(sText);
		
		SCorpus corp1= SaltFactory.createSCorpus();
		importedSCorpusGraph.addNode(corp1);
		importedSCorpusGraph.addDocument(corp1, sDoc1);
		
		String abbFolderName= System.getProperty("java.io.tmpdir")+"/abbreviations";
		File abbFolder= new File(abbFolderName);
		abbFolder.mkdirs();
		String abbFileName= abbFolderName+"/tmpAbbreviation.en";
		File abbFile= new File(abbFileName);
		abbFile.createNewFile();
		PrintWriter writer= new PrintWriter(abbFile);
		try
		{
			writer.println("this.");
			writer.println("than.");
			writer.flush();
		}catch (Exception e) {
			throw e;
		}
		finally
		{
			writer.close();
		}
		
		getFixture().getProperties().setPropertyValue(TokenizerProperties.PROP_ABBREVIATION_FOLDER, abbFolder);
		
		//runs the PepperModule
		this.start();
		
		assertNotNull(sDoc1.getDocumentGraph().getTokens());
		assertEquals(13, sDoc1.getDocumentGraph().getTokens().size());
	}
}
