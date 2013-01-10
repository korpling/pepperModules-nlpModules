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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.nlpModules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.log.LogService;

import de.hu_berlin.german.korpling.saltnpepper.misc.treetagger.tokenizer.TTTokenizer;
import de.hu_berlin.german.korpling.saltnpepper.misc.treetagger.tokenizer.Token;
import de.hu_berlin.german.korpling.saltnpepper.misc.treetagger.tokenizer.TTTokenizer.TT_LANGUAGES;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperManipulator;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperManipulatorImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

/**
 * This class allows it to use the tokenizer of Salt via Pepper. It uses the java implementation of the 
 * TreeTagger tokenizer of Salt.
 * @author Florian Zipser
 * @version 1.0
 *
 */
@Component(name="TokenizerComponent", factory="PepperManipulatorComponentFactory")
@Service(value=PepperManipulator.class)
public class Tokenizer extends PepperManipulatorImpl 
{
	public Tokenizer()
	{
		super();
		this.name= "Tokenizer";
	}
	
	private File abbreviationFolder= null;
	/**
	 * Returns the Abbriviation folder, where to find abbreviation files for the Treetagger tokenizer.
	 * @return
	 */
	private synchronized File getAbbriviationFolder()
	{
		if (abbreviationFolder== null)
			abbreviationFolder= new File(this.getResources().toFileString()+"/tokenizer/abbreviations");
		
		return(abbreviationFolder);
	}
	
	private TT_LANGUAGES language= null;
	/**
	 * Returns the language of the STextualDS given by a property file.
	 * @return
	 */
	private synchronized TTTokenizer.TT_LANGUAGES getLanguage()
	{
		if (language== null)
		{
			if (this.getSpecialParams()!= null)
			{
				//default case
				language= TT_LANGUAGES.EN;
				Properties props= new Properties();
				{//load properties
					InputStream in= null;
					try {
						in = new FileInputStream(this.getSpecialParams().toFileString());
						props.load(in);
					} catch (FileNotFoundException e) {
						if (this.getLogService()!= null)
							this.getLogService().log(LogService.LOG_WARNING, "Cannot load property file '"+this.getSpecialParams()+"' for module '"+this.getName()+"', because of nested exception. ",e);
					} catch (IOException e) {
						if (this.getLogService()!= null)
							this.getLogService().log(LogService.LOG_WARNING, "Cannot load property file '"+this.getSpecialParams()+"' for module '"+this.getName()+"', because of nested exception. ",e);
					}
					finally
					{
						if (in!= null)
						try {
							in.close();
						} catch (IOException e) {
							if (this.getLogService()!= null)
								this.getLogService().log(LogService.LOG_WARNING, "Cannot close property file '"+this.getSpecialParams()+"' for module '"+this.getName()+"', because of nested exception. ",e);
						}
					}
				}//load properties
				String prop= props.getProperty(PROP_TOKENIZER_LANGUAGE).trim();
				if (prop!= null)
				{
					this.language= TT_LANGUAGES.valueOf(prop);
				}
			}
		}
		return(language);
	}
	
	/**
	 * This method is called by method start() of superclass PepperManipulator, if the method was not overriden
	 * by the current class. If this is not the case, this method will be called for every document which has
	 * to be processed.
	 * @param sElementId the id value for the current document or corpus to process  
	 */
	@Override
	public void start(SElementId sElementId) throws PepperModuleException 
	{
		if (	(sElementId!= null) &&
				(sElementId.getSIdentifiableElement()!= null) &&
				((sElementId.getSIdentifiableElement() instanceof SDocument)))
		{//only if given sElementId belongs to an object of type SDocument or SCorpus	
			SDocumentGraph sDocGraph= ((SDocument)sElementId.getSIdentifiableElement()).getSDocumentGraph();
			if(sDocGraph!= null)
			{//if document contains a document graph
				if (sDocGraph.getSTextualDSs()!= null)
				{
					for (STextualDS sText: sDocGraph.getSTextualDSs())
					{
						if (sText!= null)
						{
							TTTokenizer tokenizer= new TTTokenizer();
							tokenizer.setAbbreviationFolder(this.getAbbriviationFolder());
							tokenizer.setLngLang(this.getLanguage());
							for (Token token: tokenizer.tokenizeToToken(sText.getSText()))
							{
								SToken sTok= SaltFactory.eINSTANCE.createSToken();
								sDocGraph.addSNode(sTok);
								STextualRelation sTextRelation= SaltFactory.eINSTANCE.createSTextualRelation();
								sTextRelation.setSStart(token.start);
								sTextRelation.setSEnd(token.end);
								sTextRelation.setSToken(sTok);
								sTextRelation.setSTextualDS(sText);
								sDocGraph.addSRelation(sTextRelation);
							}
						}
					}
				}
			}//if document contains a document graph
		}//only if given sElementId belongs to an object of type SDocument or SCorpus
	}
}