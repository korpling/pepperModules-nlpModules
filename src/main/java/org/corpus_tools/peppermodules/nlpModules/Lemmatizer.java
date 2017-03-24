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

import java.util.List;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.impl.PepperManipulatorImpl;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleDataException;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;

import org.corpus_tools.salt.core.SAnnotation;

/**
 * The Lemmatizer is a Pepper module to add lemmas to tokens. Therefore it
 * creates an {@link SAnnotation} object for each token and adds
 * it to that token. <br/>
 * 
 * @author Amir Zeldes
 * @version 1.0
 * 
 */
@Component(name = "LemmatizerComponent", factory = "PepperManipulatorComponentFactory")
public class Lemmatizer extends PepperManipulatorImpl {
	public Lemmatizer() {
		super();
		this.setName("Lemmatizer");
		setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-nlpModules"));
		setDesc("The lemmatizer is a Pepper module to lemmatize tokens. By default English lemmatization is supported.");
		this.setProperties(new TokenizerProperties());
	}

	/**
	 * Creates a mapper. {@inheritDoc
	 * PepperModule#createPepperMapper(Identifier)}
	 */
	@Override
	public PepperMapper createPepperMapper(Identifier sElementId) {
		LemmaMapper mapper = new LemmaMapper();
                
                
		return (mapper);
	}

	public static class LemmaMapper extends PepperMapperImpl {
            
            private HashMap<String, String> wordPos2Lemma;
            private boolean userLexicon = false;
            
            @Override
            protected void initialize(){

                InputStream is = null;
                
                // Load lexicon
                is = getClass().getResourceAsStream("/en-lemmatizer.dict.txt");                
                
                InputStreamReader isr = new InputStreamReader(is);

                this.wordPos2Lemma = getLexicon(isr);

                    
            } 
                        
                        
            /**
             * {@inheritDoc PepperMapper#setDocument(SDocument)}
             * 
             */
            @Override
            public DOCUMENT_STATUS mapSDocument() {

                    String lang = "en";  // TODO: setup multiple languages with built-in lemmatizer lexicon files
                    String posAnno = (String) getProperties().getProperties().getOrDefault(LemmatizerProperties.POS_ANNO, "pos");
                    boolean noLower = Boolean.valueOf(getProperties().getProperties().getProperty(LemmatizerProperties.NO_LOWER)); 
                    boolean allowUnknown = Boolean.valueOf(getProperties().getProperties().getProperty(LemmatizerProperties.ALLOW_UNKNOWN));
                    boolean makeUnknownLower = Boolean.valueOf(getProperties().getProperties().getProperty(LemmatizerProperties.MAKE_UNKNOWN_LOWER)); 
                    String unknownString = (String) getProperties().getProperties().getOrDefault(LemmatizerProperties.UNKNOWN_STRING, null);
                    String lemmaName = (String) getProperties().getProperties().getOrDefault(LemmatizerProperties.LEMMA_NAME, "lemma");
                    String lemmaNamespace = (String) getProperties().getProperties().getOrDefault(LemmatizerProperties.LEMMA_NAMESPACE, "default_ns");
                    
                    SDocumentGraph graph = getDocument().getDocumentGraph();

                    // check for user defined lexicon path
                    String lexiconFile = (String) getProperties().getProperties().getOrDefault(LemmatizerProperties.LEXICON_FILE, null);
                    
                    if (lexiconFile != null){
                        if (!this.userLexicon) { // user-defined lexicon has not been loaded yet
                            InputStream is;
                            try {
                                 is = new FileInputStream(lexiconFile);

                                } catch (FileNotFoundException e) {
                                    throw new PepperModuleDataException(this,"Lexicon file not found: " + lexiconFile);
                                }
                            InputStreamReader isr = new InputStreamReader(is);                     
                            this.wordPos2Lemma = getLexicon(isr);
                            this.userLexicon = true;
                        }

                    }
                    
                    if ((getDocument().getDocumentGraph() != null) && (graph.getTextualDSs().size() > 0)) {
                        // if document contains a document graph
                        List<SToken> tokens = getDocument().getDocumentGraph().getTokens();
                        for (SToken tok : tokens){
                            // Check if the token has a pos annotation
                            String pos = null;
                            Set<SAnnotation> tok_annos = tok.getAnnotations();
                            for (SAnnotation anno : tok_annos){
                                if (posAnno.equals(anno.getName())){
                                    pos = anno.getValue_STEXT();
                                }
                            }
                            String word = "";
                            word = graph.getText(tok);
                            String lemma = null;
                            if (pos != null){
                                if (this.wordPos2Lemma.containsKey(word + "\t" + pos)){
                                     lemma = wordPos2Lemma.get(word + "\t" + pos);
                                } else if ((!noLower) && this.wordPos2Lemma.containsKey(word.toLowerCase() + "\t" + pos)){
                                     lemma = wordPos2Lemma.get(word.toLowerCase() + "\t" + pos);
                                }
                            }
                            if (lemma == null){
                                if (this.wordPos2Lemma.containsKey(word)){
                                     lemma = wordPos2Lemma.get(word);
                                } else if ((!noLower) && this.wordPos2Lemma.containsKey(word.toLowerCase())){
                                     lemma = wordPos2Lemma.get(word.toLowerCase());
                                }
                            }
                            if (lemma == null && !allowUnknown && !makeUnknownLower){
                                lemma = word;
                            } else if (lemma == null && !allowUnknown){
                                lemma = word.toLowerCase();
                            }
                            if (unknownString != null) {
                                lemma = unknownString;
                            }
                            if (lemma != null){
                                SAnnotation lemmaAnno = SaltFactory.createSAnnotation();
                                lemmaAnno.setName(lemmaName);
                                lemmaAnno.setNamespace(lemmaNamespace);
                                lemmaAnno.setValue(lemma);
                                tok.addAnnotation(lemmaAnno);
                            }

                        }


                    }// if document contains a document graph
                    return (DOCUMENT_STATUS.COMPLETED);
            }
            
            
            private HashMap<String, String> getLexicon(InputStreamReader strm){
                
                wordPos2Lemma = new HashMap<>();

                String line;
                BufferedReader reader;
                reader = new BufferedReader(strm);

                try {
                    while ((line = reader.readLine()) != null)
                    {
                        String[] parts = line.split("\t");
                        if (parts.length > 2)
                        {
                            String word = parts[0];
                            String pos = parts[1];
                            String lemma = parts[2];
                            wordPos2Lemma.put(word + "\t" + pos, lemma);
                            wordPos2Lemma.put(word, lemma);                                
                        } else if (parts.length > 1){
                            String word = parts[0];
                            String lemma = parts[1];
                            wordPos2Lemma.put(word, lemma);                                
                        }
                        else {
                            System.out.println("Lexicon warning - ignoring line: " + line);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Lemmatizer.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {    
                    reader.close();
                } catch (IOException ex) {
                    Logger.getLogger(Lemmatizer.class.getName()).log(Level.SEVERE, null, ex);
                }                
                
                return wordPos2Lemma;
                
            }
            
	}
}