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

import java.io.File;
import java.io.PrintWriter;

import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import de.hu_berlin.german.korpling.saltnpepper.pepper.testFramework.PepperManipulatorTest;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.nlpModules.Tokenizer;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.nlpModules.TokenizerProperties;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;

public class TokenizerTest extends PepperManipulatorTest
{	
	URI resourceURI= URI.createFileURI(new File(".").getAbsolutePath());
	URI temproraryURI= URI.createFileURI(System.getProperty("java.io.tmpdir"));	
	
	@Before
	public void setUp() throws Exception 
	{
		super.setFixture(new Tokenizer());
		
		super.getFixture().setSaltProject(SaltFactory.eINSTANCE.createSaltProject());
		super.setResourcesURI(resourceURI);
		
		//setting temproraries and resources
		this.getFixture().setResources(resourceURI);
	}
	
	/**
	 * two {@link SDocument} objects containing one {@link STextualDS} each. 
	 */
	@Test
	public void testCase1()
	{
		SCorpusGraph importedSCorpusGraph= SaltFactory.eINSTANCE.createSCorpusGraph();
		this.getFixture().getSaltProject().getSCorpusGraphs().add(importedSCorpusGraph);
		
		String sText="Is this example more complicated, than it is supposed to be?";
		
		SDocument sDoc1= SaltFactory.eINSTANCE.createSDocument();
		sDoc1.setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
		sDoc1.getSDocumentGraph().createSTextualDS(sText);
		
		SDocument sDoc2= SaltFactory.eINSTANCE.createSDocument();
		sDoc2.setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
		sDoc2.getSDocumentGraph().createSTextualDS(sText);
		
		SCorpus corp1= SaltFactory.eINSTANCE.createSCorpus();
		importedSCorpusGraph.addSNode(corp1);
		importedSCorpusGraph.addSDocument(corp1, sDoc1);
		importedSCorpusGraph.addSDocument(corp1, sDoc2);
		
		//runs the PepperModule
		this.start();
		
		assertNotNull(sDoc1.getSDocumentGraph().getSTokens());
		assertEquals(13, sDoc1.getSDocumentGraph().getSTokens().size());
	}
	
	/**
	 * one {@link SDocument} objects containing two {@link STextualDS} objects. 
	 */
	@Test
	public void testCase2()
	{
		SCorpusGraph importedSCorpusGraph= SaltFactory.eINSTANCE.createSCorpusGraph();
		this.getFixture().getSaltProject().getSCorpusGraphs().add(importedSCorpusGraph);
		
		String sText="Is this example more complicated, than it is supposed to be?";
		
		SDocument sDoc1= SaltFactory.eINSTANCE.createSDocument();
		sDoc1.setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
		sDoc1.getSDocumentGraph().createSTextualDS(sText);
		sDoc1.getSDocumentGraph().createSTextualDS(sText);
		
		SCorpus corp1= SaltFactory.eINSTANCE.createSCorpus();
		importedSCorpusGraph.addSNode(corp1);
		importedSCorpusGraph.addSDocument(corp1, sDoc1);
		
		//runs the PepperModule
		this.start();
		
		assertNotNull(sDoc1.getSDocumentGraph().getSTokens());
		assertEquals(26, sDoc1.getSDocumentGraph().getSTokens().size());
	}
	
	/**
	 * one {@link SDocument} objects containing one {@link STextualDS} object with strange abbreviations. 
	 * @throws Exception 
	 */
	@Test
	public void testCase3() throws Exception
	{
		SCorpusGraph importedSCorpusGraph= SaltFactory.eINSTANCE.createSCorpusGraph();
		this.getFixture().getSaltProject().getSCorpusGraphs().add(importedSCorpusGraph);
		
		String sText="Is this. example more complicated, than. it is supposed to be?";
		
		SDocument sDoc1= SaltFactory.eINSTANCE.createSDocument();
		sDoc1.setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
		sDoc1.getSDocumentGraph().createSTextualDS(sText);
		
		SCorpus corp1= SaltFactory.eINSTANCE.createSCorpus();
		importedSCorpusGraph.addSNode(corp1);
		importedSCorpusGraph.addSDocument(corp1, sDoc1);
		
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
		
		this.getFixture().getProperties().setPropertyValue(TokenizerProperties.PROP_ABBREVIATION_FOLDER, abbFolder);
		
		//runs the PepperModule
		this.start();
		
		assertNotNull(sDoc1.getSDocumentGraph().getSTokens());
		assertEquals(13, sDoc1.getSDocumentGraph().getSTokens().size());
	}
}
