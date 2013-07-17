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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FilenameUtils;
import org.osgi.service.component.annotations.Component;

import com.neovisionaries.i18n.LanguageCode;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleNotReadyException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.MAPPING_RESULT;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperManipulatorImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperMapperImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.nlpModules.exceptions.TokenizerException;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

/**
 * This class allows it to use the tokenizer of Salt via Pepper. It uses the java implementation of the 
 * TreeTagger tokenizer of Salt.
 * @author Florian Zipser
 * @version 1.0
 *
 */
@Component(name="TokenizerComponent", factory="PepperManipulatorComponentFactory")
public class Tokenizer extends PepperManipulatorImpl 
{
	public Tokenizer()
	{
		super();
		this.name= "Tokenizer";
		this.setProperties(new TokenizerProperties());
	}
	
	/**
	 * stores all abbreviations corresponding to their language
	 */
	private Map<LanguageCode, HashSet<String>> abbreviationMap= null; 
	
	/**
	 * Checks abbreviation folder in case of t is set.
	 */
	@Override
	public boolean isReadyToStart() throws PepperModuleNotReadyException
	{
		if (((TokenizerProperties)this.getProperties()).getAbbreviationFolder()!= null)
		{
			loadAbbFolder();
		}
		return(true);
	}
	
	/**
	 * Checks abbreviation folder, if it contains abbreviation files (files with decoded language endings like *.de, *.en, etyc.)
	 */
	private void loadAbbFolder()
	{
		File abbFolder= ((TokenizerProperties)this.getProperties()).getAbbreviationFolder();
		File[] abbFiles= abbFolder.listFiles();
		if (abbFiles!= null)
		{
			for (File abbFile: abbFiles)
			{//check if file ending is a ISO 639-2 code
				String ending= FilenameUtils.getExtension(abbFile.getName());
				LanguageCode langCode= LanguageCode.valueOf(ending);
				if (langCode!= null)
				{//file is abbreviation file, load it
					if (abbreviationMap== null)
						abbreviationMap= new ConcurrentHashMap<LanguageCode, HashSet<String>>();
					HashSet<String> abbreviations= null;
			    	try 
			    	{
			    		abbreviations= new HashSet<String>();
			    		BufferedReader inReader;
						inReader = new BufferedReader(new InputStreamReader(new FileInputStream(abbFile.getAbsolutePath()), "UTF8"));
						String input = "";
						while((input = inReader.readLine()) != null)
						{
				           //putting
				           abbreviations.add(input);
						}
						inReader.close();
						
			        } catch (FileNotFoundException e) 
			        {
						throw new TokenizerException("Cannot tokenize the given text, because the file for abbreviation '"+abbFile.getAbsolutePath()+"' was not found.");
					} catch (IOException e) 
					{
						throw new TokenizerException("Cannot tokenize the given text, because can not read file '"+abbFile.getAbsolutePath()+"'.");
					}
					abbreviationMap.put(langCode, abbreviations);
				}//file is abbreviation file, load it
			}
		}
	}
	
	/**
	 * Creates a mapper of type {@link EXMARaLDA2SaltMapper}.
	 * {@inheritDoc PepperModule#createPepperMapper(SElementId)}
	 */
	@Override
	public PepperMapper createPepperMapper(SElementId sElementId){
		TokenizerMapper mapper= new TokenizerMapper();
		return(mapper);
	}
	
	private class TokenizerMapper extends PepperMapperImpl{
		/**
		 * {@inheritDoc PepperMapper#setSDocument(SDocument)}
		 * 
		 * OVERRIDE THIS METHOD FOR CUSTOMIZED MAPPING.
		 */
		@Override
		public MAPPING_RESULT mapSDocument() {
			SDocumentGraph sDocGraph= getSDocument().getSDocumentGraph();
			if(sDocGraph!= null)
			{//if document contains a document graph
				de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.tokenizer.Tokenizer tokenizer= sDocGraph.createTokenizer();
				if (abbreviationMap!= null)
				{
					Set<LanguageCode> keys= abbreviationMap.keySet();
					for (LanguageCode lang: keys)
					{
						tokenizer.addAbbreviation(lang, abbreviationMap.get(lang));
					}
				}
				if (	(sDocGraph.getSTextualDSs()!= null)&&
						(sDocGraph.getSTextualDSs().size()>0))
				{
					for (STextualDS sTextualDs: sDocGraph.getSTextualDSs())
						tokenizer.tokenize(sTextualDs);
				}
			}//if document contains a document graph
			return(MAPPING_RESULT.FINISHED);
		}
	}
	
//	/**
//	 * This method is called by method start() of superclass PepperManipulator, if the method was not overriden
//	 * by the current class. If this is not the case, this method will be called for every document which has
//	 * to be processed.
//	 * @param sElementId the id value for the current document or corpus to process  
//	 */
//	@Override
//	public void start(SElementId sElementId) throws PepperModuleException 
//	{
//		if (	(sElementId!= null) &&
//				(sElementId.getSIdentifiableElement()!= null) &&
//				((sElementId.getSIdentifiableElement() instanceof SDocument)))
//		{//only if given sElementId belongs to an object of type SDocument or SCorpus	
//			SDocumentGraph sDocGraph= ((SDocument)sElementId.getSIdentifiableElement()).getSDocumentGraph();
//			if(sDocGraph!= null)
//			{//if document contains a document graph
//				de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.tokenizer.Tokenizer tokenizer= sDocGraph.createTokenizer();
//				if (this.abbreviationMap!= null)
//				{
//					Set<LanguageCode> keys= abbreviationMap.keySet();
//					for (LanguageCode lang: keys)
//					{
//						tokenizer.addAbbreviation(lang, abbreviationMap.get(lang));
//					}
//				}
//				if (	(sDocGraph.getSTextualDSs()!= null)&&
//						(sDocGraph.getSTextualDSs().size()>0))
//				{
//					for (STextualDS sTextualDs: sDocGraph.getSTextualDSs())
//						tokenizer.tokenize(sTextualDs);
//				}
//			}//if document contains a document graph
//		}//only if given sElementId belongs to an object of type SDocument or SCorpus
//	}
}