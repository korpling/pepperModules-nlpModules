![SaltNPepper project](./gh-site/img/SaltNPepper_logo2010.png)
# NLPModules
This project provides a tokenizer, a lemmatizer and a sentencer for the linguistic converter framework Pepper (see https://u.hu-berlin.de/saltnpepper). A detailed description of the tokenizer can be found in [Tokenizer](#details), for the sentencer see [Sentencer](#details2), and for the lemmatizer see [Lemmatizer](#details3).

Pepper is a pluggable framework to convert a variety of linguistic formats (like [TigerXML](http://www.ims.uni-stuttgart.de/forschung/ressourcen/werkzeuge/TIGERSearch/doc/html/TigerXML.html), the [EXMARaLDA format](http://www.exmaralda.org/), [PAULA](http://www.sfb632.uni-potsdam.de/paula.html) etc.) into each other. Furthermore Pepper uses Salt (see https://github.com/korpling/salt), the graph-based meta model for linguistic data, which acts as an intermediate model to reduce the number of mappings to be implemented. That means converting data from a format _A_ to format _B_ consists of two steps. First the data is mapped from format _A_ to Salt and second from Salt to format _B_. This detour reduces the number of Pepper modules from _n<sup>2</sup>-n_ (in the case of a direct mapping) to _2n_ to handle a number of n formats.

![n:n mappings via SaltNPepper](./gh-site/img/puzzle.png)

In Pepper there are three different types of modules:
* importers (to map a format _A_ to a Salt model)
* manipulators (to map a Salt model to a Salt model, e.g. to add additional annotations, to rename things to merge data etc.)
* exporters (to map a Salt model to a format _B_).

For a simple Pepper workflow you need at least one importer and one exporter.

## Requirements
Since the here provided module is a plugin for Pepper, you need an instance of the Pepper framework. If you do not already have a running Pepper instance, click on the link below and download the latest stable version (not a SNAPSHOT):

> Note:
> Pepper is a Java based program, therefore you need to have at least Java 7 (JRE or JDK) on your system. You can download Java from https://www.oracle.com/java/index.html or http://openjdk.java.net/ .


## Install module
If this Pepper module is not yet contained in your Pepper distribution, you can easily install it. Just open a command line and enter one of the following program calls:

**Windows**
```
pepperStart.bat 
```

**Linux/Unix**
```
bash pepperStart.sh 
```

Then type in command *is* and the path from where to install the module:
```
pepper> update de.hu_berlin.german.korpling.saltnpepper::NLPModules::https://korpling.german.hu-berlin.de/maven2/
```

## Usage
To use this module in your Pepper workflow, put the following lines into the workflow description file. Note the fixed order of xml elements in the workflow description file: &lt;importer/>, &lt;manipulator/>, &lt;exporter/>. The ${module.name} is an importer module, which can be addressed by one of the following alternatives.
A detailed description of the Pepper workflow can be found on the [Pepper project site](https://u.hu-berlin.de/saltnpepper). 

### a) Identify the module by name

```xml
<manipulator name="Tokenizer"/>
```

and

```xml
<manipulator name="Sentencer"/>
```

and

```xml
<manipulator name="Lemmatizer"/>
```

### b) Use properties

```xml
<manipulator name="Tokenizer">
  <property key="PROPERTY_NAME">PROPERTY_VALUE</property>
</manipulator>
```

## Contribute
Since this Pepper module is under a free license, please feel free to fork it from github and improve the module. If you even think that others can benefit from your improvements, don't hesitate to make a pull request, so that your changes can be merged.
If you have found any bugs, or have some feature request, please open an issue on github. If you need any help, please write an e-mail to saltnpepper@lists.hu-berlin.de .

## Funders
This project has been funded by the [department of corpus linguistics and morphology](https://www.linguistik.hu-berlin.de/institut/professuren/korpuslinguistik/) of the Humboldt-Universität zu Berlin, the Institut national de recherche en informatique et en automatique ([INRIA](www.inria.fr/en/)) and the [Sonderforschungsbereich 632](https://www.sfb632.uni-potsdam.de/en/) and the Department of Linguistics at Georgetown University.

## License
  Copyright 2009-2017 Humboldt-Universität zu Berlin, INRIA and Georgetown University

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.


# <a name="details1">Tokenizer</a>
The used tokenizer is the internal tokenizer of Salt, which is a java implementation of the [TreeTagger](http://www.ims.uni-stuttgart.de/projekte/corplex/TreeTagger/) tokenizer.

Tokenizes all STextualDS object being contained in all SDocumentGraph object. The Tokenization is similar to the tokenization made by the TreeTagger tokenizer. The language will be detected automatically for each STextualDS object by use of the TextCategorizer (see: http://textcat.sourceforge.net/doc/org/knallgrau/utils/textcat/TextCategorizer.html). If the language is one of the given ones: english, french, italian and german, abbreviations also taken from the Treetagger will be used. For each token detected in the text given by sTextualDS.getSText an SToken object is created and linked with the STextualDS object via a new STextualRelation object containing the textual offset.

## Properties

The table  contains an overview of all usable properties to customize the behaviour of this pepper module. The following section contains a close description to each single property and describes the resulting differences in the mapping to the salt model.

|Name of property             |	Type of property  | optional/ mandatory	| default value |
|-----------------------------|-------------------|---------------------|---------------|
|tokenizer.abbreviationFolder | file path	      | optional	default | --            |

### tokenizer.abbreviationFolder
Since the TreeTagger tokenizer produces better results, when it knows about abbreviations used in the text corresponding to the language of the text, it is possible to feed the Tokenizer module with lists of abbreviations. You can use this property to pass a folder location to the Tokenizer containing files, containing abbreviations. Such a file is just a text file containing abbreviations devided by linebreaks. 
```
a.e.
a.l.
a.m.
a.o.b.
a/c
a11y
AAR
AAT
AAUW
ab
```

Since abbreviations could be language dependent, the Tokenizer provides a mechanism to set abbreviation lists corresponding to a language. Therefore just name the file ending like the language for abbreviations. Use the the ISO 639-2 code for language description. For instance:
```
abbreviation.de
abbreviation.en
abbreviation.fr
abbreviation.it
```

# <a name="details2">Sentencer</a>
The sentencer is a Pepper module to bundle tokens to sentences. Therefore it creates a {@link SSpan} object for each sentence and connects that sentence with a set of tokens, belonging to the sentence.

A sentence is identified as being determined by punctuations ('.', '!' and '?'). The sentencer uses the abbreviation lists of Salt to identify abbreviations:

```
abbreviation.de
abbreviation.en
abbreviation.fr
abbreviation.it
```

# <a name="details3">Lemmatizer</a>
The lemmatizer adds lemmas to all STokens based on a lemma list, optionally also containing instructions per POS tag (for example the lemma can be different for
bit as a verb -> bite, vs. bit as a noun -> bit). No stochastic disambiguation is done - lemmatization is purely look-up based. A built in lemma list is provided for English, but
a file with lemmas per word and potentially POS tag can be provided in the following tab delimited format, one word per line:


```
bit	NN	bit
bit	VBD	bite
grew	VBD	grow
oxen	NNS	ox
```

or:

```
bit	bit
grew	grow
oxen	ox
```

Note that there is nothing preventing the use of this module for purposes other than lemmatization. It can add any annotation to tokens, based on word form look up, 
optionally cross-referenced with one other token annotation. Names for the checked and added annotations are configurable, and the module can be used multiple times
to add several annotations. 

If you have useful lemma lists for some language that can be made available, please consider contributing them to the project.

## Properties

The table  contains an overview of all usable properties to customize the behaviour of this pepper module. The following section contains a close description to each single property and describes the resulting differences in the mapping to the salt model.

|Name of property             |	Type of property  | optional/ mandatory	| default value |
|-----------------------------|-------------------|---------------------|---------------|
|lemmatizer.posAnno | string	      | optional  | pos            |
|lemmatizer.noLower | boolean	      | optional  | FALSE            |
|lemmatizer.allowUnknown | boolean	      | optional  | FALSE            |
|lemmatizer.makeUnknownLower | boolean	      | optional  | FALSE            |
|lemmatizer.unknownString | string	      | optional  | --            |
|lemmatizer.lemmaName | string	      | optional  | lemma            |
|lemmatizer.lemmaNamespace | string	      | optional  | default_ns            |
|lemmatizer.lexiconFile | file path	      | optional  | --            |

### lemmatizer.posAnno
Name of a part of speech annotation to check (default: pos).

### lemmatizer.noLower
Normally lower cased forms are searched if exact match is not found, set noLower to TRUE to override (default: FALSE).

### lemmatizer.allowUnknown
If no lemma is found, the token's string will be used instead, set noUnknown to FALSE to use the unknownString value instead (default: FALSE).

### lemmatizer.makeUnknownLower
Optionally, when using the token string instead of an unknown lemma, the value can be lower cased (default: FALSE).

### lemmatizer.unknownString
Specifies a string to use for all unknown lemmas.

### lemmatizer.lemmaName
Specifies the name of the lemma annotation (default: lemma).

### lemmatizer.lemmaNamespace
Specifies the namespace for the lemma annotation (default: default_ns).

### lemmatizer.lexiconFile
Optional path to a file containing the lemma lexicon, if not using a built-in lexicon. You may use absolute paths; use forward slashes for both Windows and *NIX, e.g.:

`<property key="lemmatizer.lexiconFile">C:/Users/J/Desktop/lemma_list.tab</property>`

