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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FilenameUtils;
import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.impl.PepperManipulatorImpl;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleNotReadyException;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

import com.neovisionaries.i18n.LanguageCode;

/**
 * This class allows it to use the tokenizer of Salt via Pepper. It uses the java implementation of the TreeTagger
 * tokenizer of Salt.
 *
 * @author Florian Zipser
 * @version 1.0
 *
 */
@Component(name = "TokenizerComponent", factory = "PepperManipulatorComponentFactory")
public class Tokenizer extends PepperManipulatorImpl {

  public Tokenizer() {
    super();
    setName("Tokenizer");
    setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
    setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-nlpModules"));
    setDesc("The tokenizer tokenzizes a document using the tokenizer provided by Salt. The tokenizer uses abbreviation lists and is implemented along the Treetaggers tokenizer by Helmut Schmid (see: http://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/). ");
    setProperties(new TokenizerProperties());
  }

  /**
   * stores all abbreviations corresponding to their language
   */
  private Map<LanguageCode, HashSet<String>> abbreviationMap = null;

  /**
   * Checks abbreviation folder in case of t is set.
   */
  @Override
  public boolean isReadyToStart() throws PepperModuleNotReadyException {
    if (((TokenizerProperties) this.getProperties()).getAbbreviationFolder() != null) {
      loadAbbFolder();
    }
    return (true);
  }

  /**
   * Checks abbreviation folder, if it contains abbreviation files (files with decoded language endings like *.de, *.en,
   * etyc.)
   */
  private void loadAbbFolder() {
    File abbFolder = ((TokenizerProperties) this.getProperties()).getAbbreviationFolder();
    File[] abbFiles = abbFolder.listFiles();
    if (abbFiles != null) {
      for (File abbFile : abbFiles) {// check if file ending is a ISO
        // 639-2 code
        String ending = FilenameUtils.getExtension(abbFile.getName());
        LanguageCode langCode = LanguageCode.valueOf(ending);
        if (langCode != null) {// file is abbreviation file, load it
          if (abbreviationMap == null) {
            abbreviationMap = new ConcurrentHashMap<LanguageCode, HashSet<String>>();
          }
          HashSet<String> abbreviations = null;
          try {
            abbreviations = new HashSet<String>();
            BufferedReader inReader;
            inReader = new BufferedReader(new InputStreamReader(new FileInputStream(abbFile.getAbsolutePath()), "UTF8"));
            String input = "";
            while ((input = inReader.readLine()) != null) {
              // putting
              abbreviations.add(input);
            }
            inReader.close();

          } catch (FileNotFoundException e) {
            throw new PepperModuleException(this, "Cannot tokenize the given text, because the file for abbreviation '" + abbFile.getAbsolutePath() + "' was not found.");
          } catch (IOException e) {
            throw new PepperModuleException(this, "Cannot tokenize the given text, because can not read file '" + abbFile.getAbsolutePath() + "'.");
          }
          abbreviationMap.put(langCode, abbreviations);
        }// file is abbreviation file, load it
      }
    }
  }

  /**
   * Creates a mapper of type {@link EXMARaLDA2SaltMapper}. {@inheritDoc
   * PepperModule#createPepperMapper(Identifier)}
   */
  @Override
  public PepperMapper createPepperMapper(Identifier sElementId) {
    TokenizerMapper mapper = new TokenizerMapper();
    return (mapper);
  }

  private class TokenizerMapper extends PepperMapperImpl {

    /**
     * {@inheritDoc PepperMapper#setDocument(SDocument)}
     *
     * OVERRIDE THIS METHOD FOR CUSTOMIZED MAPPING.
     */
    @Override
    public DOCUMENT_STATUS mapSDocument() {
      SDocumentGraph sDocGraph = getDocument().getDocumentGraph();
      if (sDocGraph != null) {// if document contains a document graph
        org.corpus_tools.peppermodules.nlpModules.tokenizer.Tokenizer tokenizer
          = new org.corpus_tools.peppermodules.nlpModules.tokenizer.Tokenizer();

        tokenizer.setsDocumentGraph(sDocGraph);

        if (abbreviationMap != null) {
          Set<LanguageCode> keys = abbreviationMap.keySet();
          for (LanguageCode lang : keys) {
            tokenizer.addAbbreviation(lang, abbreviationMap.get(lang));
          }
        }
        if ((sDocGraph.getTextualDSs() != null) && (sDocGraph.getTextualDSs().size() > 0)) {
          List<STextualDS> texts = new ArrayList<STextualDS>();
          for (STextualDS sTextualDs : sDocGraph.getTextualDSs()) {
            texts.add(sTextualDs);
          }
          for (STextualDS sTextualDs : texts) {
            tokenizer.tokenize(sTextualDs);
          }
        }
      }// if document contains a document graph
      return (DOCUMENT_STATUS.COMPLETED);
    }
  }
}
