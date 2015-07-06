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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.nlpModules;

import java.util.HashSet;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

import com.neovisionaries.i18n.LanguageCode;

import de.hu_berlin.german.korpling.saltnpepper.pepper.common.DOCUMENT_STATUS;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperManipulatorImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperMapperImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.tokenizer.AbbreviationDE;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.tokenizer.AbbreviationEN;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.tokenizer.AbbreviationFR;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.tokenizer.AbbreviationIT;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

/**
 * The sentencer is a Pepper module to bundle tokens to sentences. Therefore it
 * creates a {@link SSpan} object for each sentence and connects that sentence
 * with a set of tokens, belonging to the sentence. <br/>
 * A sentence is identified as being determined by punctuations ('.', '!' and
 * '?'). The sentencer uses the abbreviation lists of Salt to identify
 * abbreviations:
 * <ul>
 * <li>{@link AbbreviationDE}</li>
 * <li>{@link AbbreviationEN}</li>
 * <li>{@link AbbreviationFR}</li>
 * <li>{@link AbbreviationIT}</li>
 * </ul>
 * 
 * @author Florian Zipser
 * @version 1.0
 * 
 */
@Component(name = "SentencerComponent", factory = "PepperManipulatorComponentFactory")
public class Sentencer extends PepperManipulatorImpl {
	public Sentencer() {
		super();
		this.setName("Sentencer");
		setSupplierContact(URI.createURI("saltnpepper@lists.hu-berlin.de"));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-nlpModules"));
		setDesc("The sentencer is a Pepper module to bundle tokens to sentences. Therefore it creates a {@link SSpan} object for each sentence and connects that sentence with a set of tokens, belonging to the sentence. A sentence is identified as being determined by punctuations ('.', '!' and * '?'). The sentencer uses the abbreviation lists of Salt to identify abbreviations. ");
		this.setProperties(new TokenizerProperties());
	}

	/**
	 * Creates a mapper of type {@link EXMARaLDA2SaltMapper}. {@inheritDoc
	 * PepperModule#createPepperMapper(SElementId)}
	 */
	@Override
	public PepperMapper createPepperMapper(SElementId sElementId) {
		SentenceMapper mapper = new SentenceMapper();
		return (mapper);
	}

	public static class SentenceMapper extends PepperMapperImpl {
		/**
		 * {@inheritDoc PepperMapper#setSDocument(SDocument)}
		 * 
		 * OVERRIDE THIS METHOD FOR CUSTOMIZED MAPPING.
		 */
		@Override
		public DOCUMENT_STATUS mapSDocument() {
			if ((getSDocument().getSDocumentGraph() != null) && (getSDocument().getSDocumentGraph().getSTextualDSs().size() > 0)) {
				// if document contains a document graph
				for (STextualDS textualDS : getSDocument().getSDocumentGraph().getSTextualDSs()) {
					if ((textualDS.getSText() != null) && (!textualDS.getSText().isEmpty())) {
						char[] text = textualDS.getSText().toCharArray();
						LanguageCode language = de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.tokenizer.Tokenizer.checkLanguage(textualDS.getSText());
						HashSet<String> abbreviations = null;
						if (LanguageCode.de.equals(language)) {
							abbreviations = AbbreviationDE.createAbbriviations();
						} else if (LanguageCode.en.equals(language)) {
							abbreviations = AbbreviationEN.createAbbriviations();
						} else if (LanguageCode.fr.equals(language)) {
							abbreviations = AbbreviationFR.createAbbriviations();
						} else if (LanguageCode.it.equals(language)) {
							abbreviations = AbbreviationIT.createAbbriviations();
						}
						int startOfSentence = 0;
						StringBuilder word = new StringBuilder();
						for (int i = 0; i <= textualDS.getSText().length() - 1; i++) {
							word.append(text[i]);
							if (' ' == text[i]) {
								word = new StringBuilder();
							} else if (('.' == text[i]) || ('!' == text[i]) || ('?' == text[i])) {
								if (!abbreviations.contains(word.toString())) {
									SDataSourceSequence sequence = SaltFactory.eINSTANCE.createSDataSourceSequence();
									sequence.setSSequentialDS(textualDS);
									sequence.setSStart(startOfSentence);
									sequence.setSEnd(i + 1);
									EList<SToken> tokens = getSDocument().getSDocumentGraph().getSTokensBySequence(sequence);
									if (tokens != null) {
										getSDocument().getSDocumentGraph().createSSpan(tokens).createSAnnotation(null, "sentence", "sentence");
										startOfSentence = i + 1;
										word = new StringBuilder();
									}
								}
							}
						}
					}
				}
			}// if document contains a document graph
			return (DOCUMENT_STATUS.COMPLETED);
		}
	}
}