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

import java.io.File;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;

/**
 * Defines the properties to be used for the {@link RSTImporter}.
 * 
 * @author Florian Zipser
 * 
 */
public class TokenizerProperties extends PepperModuleProperties {
	public static final String PREFIX = "tokenizer.";

	/**
	 * Name of the property to specify a folder containing abbreviations for
	 * treetagger.
	 */
	public final static String PROP_ABBREVIATION_FOLDER = PREFIX + "abbreviationFolder";

	public boolean checkProperty(PepperModuleProperty<?> prop) {
		super.checkProperty(prop);

		if (PROP_ABBREVIATION_FOLDER.equals(prop.getName())) {
			File file = (File) prop.getValue();
			if (!file.exists())
				throw new PepperModuleException("The file set to property '" + PROP_ABBREVIATION_FOLDER + "' does not exist.");
			if (!file.isDirectory())
				throw new PepperModuleException("The file '" + file.getAbsolutePath() + "'set to property '" + PROP_ABBREVIATION_FOLDER + "' is not a directory.");
		}

		return (true);
	}

	public TokenizerProperties() {
		this.addProperty(new PepperModuleProperty<File>(PROP_ABBREVIATION_FOLDER, File.class, "Since the TreeTagger tokenizer produces better results, when it knows about abbreviations used in the text corresponding to the language of the text, it is possible to feed the Tokenizer module with lists of abbreviations.", null, false));
	}

	/**
	 * Since the TreeTagger tokenizer produces better results, when it knows
	 * about abbreviations used in the text corresponding to the language of the
	 * text, it is possible to feed the Tokenizer module with lists of
	 * abbreviations.
	 * 
	 * @return
	 */
	public File getAbbreviationFolder() {
		File abbFolder = ((File) this.getProperty(PROP_ABBREVIATION_FOLDER).getValue());
		return (abbFolder);
	}
}
