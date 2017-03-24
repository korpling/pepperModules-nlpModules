/*
 * Copyright 2017 GU.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.corpus_tools.peppermodules.nlpModules;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;

/**
 *
 * @author Amir Zeldes
 */
public class LemmatizerProperties extends PepperModuleProperties  {


	public static final String PREFIX = "lemmatizer.";


        public final static String POS_ANNO = PREFIX + "posAnno";
	public final static String NO_LOWER = PREFIX + "noLower";
	public final static String ALLOW_UNKNOWN = PREFIX + "allowUnknown";
	public final static String MAKE_UNKNOWN_LOWER = PREFIX + "makeUnknownLower";
	public final static String UNKNOWN_STRING = PREFIX + "unknownString";
	public final static String LEMMA_NAME = PREFIX + "lemmaName";
	public final static String LEMMA_NAMESPACE = PREFIX + "lemmaNamespace";
	public final static String LEXICON_FILE = PREFIX + "lexiconFile";

        
	public LemmatizerProperties() {
		this.addProperty(new PepperModuleProperty<String>(POS_ANNO, String.class, "Name of a part of speech annotation to check (default: pos).", "pos", false));
		this.addProperty(new PepperModuleProperty<Boolean>(NO_LOWER, Boolean.class, "Normally lower cased forms are searched if exact match is not found, set noLower to TRUE to override (default: FALSE).", false, false));
		this.addProperty(new PepperModuleProperty<Boolean>(ALLOW_UNKNOWN, Boolean.class, "If no lemma is found, the token's string will be used instead, set noUnknown to FALSE to use the unknownString value instead (default: FALSE).", false, false));
		this.addProperty(new PepperModuleProperty<Boolean>(MAKE_UNKNOWN_LOWER, Boolean.class, "Optionally, when using the token string instead of an unknown lemma, the value can be lower cased (default: FALSE).", false, false));
		this.addProperty(new PepperModuleProperty<String>(UNKNOWN_STRING, String.class, "Specifies a string to use for all unknown lemmas.", null, false));
		this.addProperty(new PepperModuleProperty<String>(LEMMA_NAME, String.class, "Specifies the name of the lemma annotation (default: lemma).", "lemma", false));
		this.addProperty(new PepperModuleProperty<String>(LEMMA_NAMESPACE, String.class, "Specifies the namespace for the lemma annotation (default: default_ns).", "default_ns", false));
		this.addProperty(new PepperModuleProperty<String>(LEXICON_FILE, String.class, "Optional path to a file containing the lemma lexicon, if not using a built-in lexicon.", null, false));
        }
    
}
