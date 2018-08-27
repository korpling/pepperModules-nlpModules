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
package org.corpus_tools.peppermodules.nlpModules;

import java.util.HashSet;
import java.util.List;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.impl.PepperManipulatorImpl;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.graph.Identifier;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

import com.neovisionaries.i18n.LanguageCode;

import org.corpus_tools.salt.common.tokenizer.AbbreviationDE;
import org.corpus_tools.salt.common.tokenizer.AbbreviationEN;
import org.corpus_tools.salt.common.tokenizer.AbbreviationFR;
import org.corpus_tools.salt.common.tokenizer.AbbreviationIT;

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
		setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-nlpModules"));
		setDesc("The sentencer is a Pepper module to bundle tokens to sentences. Therefore it creates a {@link SSpan} object for each sentence and connects that sentence with a set of tokens, belonging to the sentence. A sentence is identified as being determined by punctuations ('.', '!' and * '?'). The sentencer uses the abbreviation lists of Salt to identify abbreviations. ");
	}

	/**
	 * Creates a mapper of type {@link EXMARaLDA2SaltMapper}. {@inheritDoc
	 * PepperModule#createPepperMapper(Identifier)}
	 */
	@Override
	public PepperMapper createPepperMapper(Identifier sElementId) {
		SentenceMapper mapper = new SentenceMapper();
		return (mapper);
	}

	public static class SentenceMapper extends PepperMapperImpl {
		/**
		 * {@inheritDoc PepperMapper#setDocument(SDocument)}
		 * 
		 * OVERRIDE THIS METHOD FOR CUSTOMIZED MAPPING.
		 */
		@Override
		public DOCUMENT_STATUS mapSDocument() {
			if ((getDocument().getDocumentGraph() != null) && (getDocument().getDocumentGraph().getTextualDSs().size() > 0)) {
				// if document contains a document graph
				for (STextualDS textualDS : getDocument().getDocumentGraph().getTextualDSs()) {
					if ((textualDS.getText() != null) && (!textualDS.getText().isEmpty())) {
						char[] text = textualDS.getText().toCharArray();
						LanguageCode language = org.corpus_tools.salt.common.tokenizer.Tokenizer.checkLanguage(textualDS.getText());
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
						for (int i = 0; i <= textualDS.getText().length() - 1; i++) {
							word.append(text[i]);
							if (' ' == text[i]) {
								word = new StringBuilder();
							} else if (('.' == text[i]) || ('!' == text[i]) || ('?' == text[i])) {
								if (!abbreviations.contains(word.toString())) {
									DataSourceSequence sequence = new DataSourceSequence();
									sequence.setDataSource(textualDS);
									sequence.setStart(startOfSentence);
									sequence.setEnd(i + 1);
									List<SToken> tokens = getDocument().getDocumentGraph().getTokensBySequence(sequence);
									if (tokens != null) {
										getDocument().getDocumentGraph().createSpan(tokens).createAnnotation(null, "sentence", "sentence");
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