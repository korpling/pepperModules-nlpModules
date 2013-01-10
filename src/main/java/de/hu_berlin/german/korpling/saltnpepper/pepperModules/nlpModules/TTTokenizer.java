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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hu_berlin.german.korpling.saltnpepper.misc.treetagger.exceptions.TTTokenizerException;
import de.hu_berlin.german.korpling.saltnpepper.misc.treetagger.language.ISO639_LANGUAGE_CODE;
import de.hu_berlin.german.korpling.saltnpepper.misc.treetagger.language.LanguageDetector;


/**
 * The general task of this class is to tokenize a given text in the same order as the tool TreeTagger will do. 
 * A list of tokenized text is returned with the text anchor (start and end position) in original text.
 * Reimplemented in Java with permission from the original TreeTagger tokenizer in Perl by Helmut Schmid 
 * (see http://www.ims.uni-stuttgart.de/projekte/corplex/TreeTagger/). 
 * This implementation uses sets of abbreviations to detect tokens, which are abbreviations in a specific language.
 * Therefore you can set a file containing abbreviations, to take others than the default ones. Because of 
 * abbreviations are language dependend, you can set a language, to use only a specific set of abbreviations.
 * The current version of the {@link TTTokenizer} supports abbreviations for english, french, italian and german language.
 * If no language is set, all available abbreviations will be used.    
 *  
 * @author Amir Zeldes
 * @author Florian Zipser
 *
 */
public class TTTokenizer 
{
    public enum TT_LANGUAGES {DE,EN,FR,IT};
   
	/**
     * Initializes a new TTokenizer object.
     */
    public TTTokenizer()  
    {
    }
    
    /**
     * Initializes a new TTokenizer object.
     * @param flAbbreviations
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public TTTokenizer(File flAbbreviations) 
    {
        readAbbreviations(flAbbreviations);
    }

    /**
     * Initializes a new TTokenizer object.
     * @param lngLang
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public TTTokenizer(TT_LANGUAGES lngLang) 
    {
        this.setLngLang(lngLang);
    	setClitics(lngLang);
    }
    
    /**
     * Initializes a new TTokenizer object.
     * @param flAbbreviations
     * @param lngLang
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public TTTokenizer(File flAbbreviations, TT_LANGUAGES lngLang) 
    {
        this.setLngLang(lngLang);
    	setClitics(lngLang);
        readAbbreviations(flAbbreviations);
    }
    
    /**
     * Resets this object, to have a fresh and empty object like creating a new one.
     */
    public void reset()
    {
    	this.abbreviationFile= null;
    	this.abbreviationFolder= null;
    	this.lngLang= null;
    	this.currentAbbreviations= null;
    }
    
//=============================== start: language    
    /**
     * the language of the text to be tokenized
     */
    private TT_LANGUAGES lngLang = null;
    /**
     * Sets the language of the text which shall be tokenized.
	 * @param lngLang the lngLang to set
	 */
	public void setLngLang(TT_LANGUAGES lngLang) {
		this.lngLang = lngLang;
	}

	/**
	 * returns the set language of the text which shall be tokenized.
	 * @return the lngLang
	 */
	public TT_LANGUAGES getLngLang() {
		return lngLang;
	}
//=============================== end: language
//=============================== start: abbreviation folder
	public static final String FILE_ABBR_IT= "italian-abbreviations";
	public static final String FILE_ABBR_DE= "german-abbreviations";
	public static final String FILE_ABBR_FR= "french-abbreviations";
	public static final String FILE_ABBR_EN= "english-abbreviations";
	
	/**
	 * Folder where the abbreviation files can be found.
	 */
	private File abbreviationFolder= null;
	
	/**
	 * Sets the folder where the abbreviation files can be found.
	 * @param abbriviationFolder the abbriviationFolder to set
	 */
	public void setAbbreviationFolder(File abbreviationFolder) {
		this.abbreviationFolder = abbreviationFolder;
	}

	/**
	 * Returns the set folder where the abbreviation files can be found.
	 * @return the abbriviationFolder
	 */
	public File getAbbreviationFolder() {
		return abbreviationFolder;
	}
//=============================== end: abbreviation folder	
//=============================== start: abbreviation file
	/**
	 * File where the abbreviation files can be found.
	 */
	private File abbreviationFile= null;
	
	/**
	 * Sets the file where the abbreviation files can be found.
	 * @param abbriviationFile the abbriviationFile to set
	 */
	public void setAbbreviationFile(File abbreviationFile) {
		this.abbreviationFile = abbreviationFile;
	}

	/**
	 * Returns the set file where the abbreviation files can be found.
	 * @return the abbriviationFile
	 */
	public File getAbbreviationFile() {
		return abbreviationFile;
	}
//=============================== end: abbreviation folder		
//=============================== start: abbreviation
	/**
	 * abbreviations currently being used
	 */
	private HashSet<String> currentAbbreviations = null;
	
	/**
	 * Calls {@link #getAbbreviations(TT_LANGUAGES)} with currently set language.
	 * @return
	 */
	public HashSet<String> getAbbreviations()
	{
		return(this.getAbbreviations(this.getLngLang()));
	}
	
	/**
	 * Resets the table of abbreviations for instance, when the language has changed.
	 * @param language
	 * @return
	 */
	private HashSet<String> resetAbbreviations(TT_LANGUAGES language)
	{
		HashSet<String> retVal= null;
		if (this.getAbbreviationFile()!= null)
		{
			retVal= this.readAbbreviations(this.getAbbreviationFile());
		}
		else if (this.getAbbreviationFolder()!= null)
		{
			File abbrFile= null;
			if (language!= null)
			{
				switch (language) {
				case DE:
					abbrFile= new File(this.getAbbreviationFolder().getAbsolutePath()+"/"+FILE_ABBR_DE);
					retVal= this.readAbbreviations(abbrFile);
					break;
				case EN:
					abbrFile= new File(this.getAbbreviationFolder().getAbsolutePath()+"/"+FILE_ABBR_EN);
					retVal= this.readAbbreviations(abbrFile);
					break;
				case FR:
					abbrFile= new File(this.getAbbreviationFolder().getAbsolutePath()+"/"+FILE_ABBR_FR);
					retVal= this.readAbbreviations(abbrFile);
					break;
				case IT:
					abbrFile= new File(this.getAbbreviationFolder().getAbsolutePath()+"/"+FILE_ABBR_IT);
					retVal= this.readAbbreviations(abbrFile);
					break;
				}
			}
			else
			{
				abbrFile= new File(this.getAbbreviationFolder().getAbsolutePath()+"/"+FILE_ABBR_DE);
				retVal= this.readAbbreviations(abbrFile);
				abbrFile= new File(this.getAbbreviationFolder().getAbsolutePath()+"/"+FILE_ABBR_EN);
				retVal.addAll(this.readAbbreviations(abbrFile));
				abbrFile= new File(this.getAbbreviationFolder().getAbsolutePath()+"/"+FILE_ABBR_FR);
				retVal.addAll(this.readAbbreviations(abbrFile));
				abbrFile= new File(this.getAbbreviationFolder().getAbsolutePath()+"/"+FILE_ABBR_IT);
				retVal.addAll(this.readAbbreviations(abbrFile));
			}
		}
		else
		{
			if (language!= null)
			{
				switch (language) {
				case DE:
					retVal= AbbreviationDE.createAbbriviations();
					break;
				case EN:
					retVal= AbbreviationEN.createAbbriviations();
					break;
				case FR:
					retVal= AbbreviationFR.createAbbriviations();
					break;
				case IT:
					retVal= AbbreviationIT.createAbbriviations();
					break;
				}
			}
			else 
			{
				retVal= AbbreviationDE.createAbbriviations();
				retVal.addAll(AbbreviationEN.createAbbriviations());
				retVal.addAll(AbbreviationFR.createAbbriviations());
				retVal.addAll(AbbreviationIT.createAbbriviations());
			}
		}
		return(retVal);
	}
	
	/**
	 * Returns a set of available abbreviations concerning the given language. If no language is given, all available
	 * abbreviations will be returned. The order of searching for abbreviations is the following:
	 * <ol>
	 * 	<li>Check if an abbreviation file is set, if true, use this one.</li>
	 * 	<li>Check if an abbreviation folder is set, if true, detect available languages in folder and take them.</li>
	 * 	<li>If no files or folders set, take the default abbreviation set given by {@link AbbreviationDE}, {@link AbbreviationEN}, {@link AbbreviationFR} and {@link AbbreviationIT}.</li>
	 * </ol>
	 */
	public HashSet<String> getAbbreviations(TT_LANGUAGES language)
	{
		HashSet<String> retVal= null;
		if (currentAbbreviations!= null)
			retVal= currentAbbreviations;
		else
		{//abbreviations aren't set yet
			this.currentAbbreviations= resetAbbreviations(language);
			retVal= this.currentAbbreviations; 
		}//abbreviations aren't set yet
		return(retVal);
	}
	
    /**
     * Reads the abbreviation from given file. 
     * @param flAbbreviations
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    private HashSet<String> readAbbreviations(File flAbbreviations)
    {
    	HashSet<String> abbreviations= null;
    	try 
    	{
    		abbreviations= new HashSet<String>();
    		
    		BufferedReader inReader;
		
			inReader = new BufferedReader(new InputStreamReader(new FileInputStream(flAbbreviations), "UTF8"));
		
			String input = "";
			
			while((input = inReader.readLine()) != null)
			{
	           //putting
	           abbreviations.add(input);
			}
			inReader.close();
			
        } catch (FileNotFoundException e) 
        {
			throw new TTTokenizerException("Cannot tokenize the given text, because the file for abbreviation '"+flAbbreviations.getAbsolutePath()+"' was not found.");
		} catch (IOException e) 
		{
			throw new TTTokenizerException("Cannot tokenize the given text, because can not read file '"+flAbbreviations.getAbsolutePath()+"'.");
		}
		return(abbreviations);
    }

//======================= start: important issues
 // characters which have to be cut off at the beginning of a word
    protected final static String P_CHAR = "\\[\\{\\(´`\"»«‚„†‡‹‘’“”•–—›";
    // characters which have to be cut off at the end of a word
    protected final static String F_CHAR = "\\]\\}'`\"\\),;:!\\?%»«‚„…†‡‰‹‘’“”•–—›";

    // character sequences which have to be cut off at the beginning of a word
    private String PClitic="";
    // character sequences which have to be cut off at the end of a word
    private String FClitic="";
    
    /**
     * Sets clitics corresponding to the given language.
     * @param lngLang language
     */
    private void setClitics(TT_LANGUAGES lngLang)
    {
        if (this.getLngLang()!=null)
        {
            this.setLngLang(lngLang);
            switch (lngLang){
                case EN:
                    this.FClitic = "('(s|re|ve|d|m|em|ll)|n't)";
                    break;
                case FR:
                    this.PClitic = "([dcjlmnstDCJLNMST]'|[Qq]u'|[Jj]usqu'|[Ll]orsqu')";
                    this.FClitic = "(-t-elles?|-t-ils?|-t-on|-ce|-elles?|-ils?|-je|-la|-les?|-leur|-lui|-mêmes?|-m'|-moi|-nous|-on|-toi|-tu|-t'|-vous|-en|-y|-ci|-là)";
                    break;
                case IT:
                    this.PClitic = "([dD][ae]ll'|[nN]ell'|[Aa]ll'|[lLDd]'|[Ss]ull'|[Qq]uest'|[Uu]n'|[Ss]enz'|[Tt]utt')";
                    break;
                case DE: //do nothing

            }
        }
    }
    
    /**
     * The general task of this class is to tokenize a given text in the same order as the tool TreeTagger will do. 
     * A list of tokenized text is returned with the text anchor (start and end position) in original text.
     * @param strInput original text
     * @return tokenized text fragments and their position in the original text
     */
    public List<Token> tokenizeToToken(String strInput) 
    {
    	List<Token> retVal= null;
   	
    	List<String> strTokens = null;
    	strTokens= this.tokenizeToString(strInput);
    	if (	(strTokens!= null)&&
    			(strTokens.size()> 0))
    	{
    		retVal= new Vector<Token>();
    		char[] chrText= strInput.toCharArray();
    		int tokenCntr= 0;
    		
    		for (int i=0; i< chrText.length; i++ )
     		{
    			if (	(strTokens.get(tokenCntr).length()< 1) ||
    					(strTokens.get(tokenCntr).substring(0, 1).equals(String.valueOf(chrText[i]))))
     			{//first letter matches
     	 			StringBuffer pattern= new StringBuffer();
     				for (int y=0; y < strTokens.get(tokenCntr).length(); y++)
     				{//compute pattern in text
     					pattern.append(chrText[i+y]);
     				}//compute pattern in text
     				if (strTokens.get(tokenCntr).hashCode()== pattern.toString().hashCode())
     				{//pattern found
     					Token token= new Token();
     	    			retVal.add(token);
     	    			token.text= strTokens.get(tokenCntr);
     	    			token.start= i;
     	    			token.end= i+strTokens.get(tokenCntr).length();
     					i= i+strTokens.get(tokenCntr).length()-1;
     					tokenCntr++;
     					if (tokenCntr >= strTokens.size())
     						break;
     				}//pattern found
     			}//first letter matches
     		}
    	}
    	return(retVal);
    }
    
    /**
     * The general task of this class is to tokenize a given text in the same order as the tool TreeTagger will do. 
     * Returns a list of tokenized text. 
     * @param strInput original text
     * @return tokeized text fragments
     */
    public List<String> tokenizeToString(String strInput) 
    {
    	if (	(strInput== null)||
    			(strInput.isEmpty()))
    		return(null);
    	if (this.getLngLang()== null)
    	{
    		ISO639_LANGUAGE_CODE lang= LanguageDetector.detectLanguage(strInput);
    		if (lang!= null)
    		{
	    		switch (lang) {
				case ENG:
					this.setLngLang(TT_LANGUAGES.EN);
					break;
				case FRE_FRA:
					this.setLngLang(TT_LANGUAGES.FR);
					break;
				case GER_DEU:
					this.setLngLang(TT_LANGUAGES.DE);
					break;
				case ITA:
					this.setLngLang(TT_LANGUAGES.IT);
					break;
				default:
					break;
				}
	    		this.resetAbbreviations(this.getLngLang());
    		}
    	}
    	this.setClitics(this.getLngLang());

        //insert missing blanks after punctuation
        strInput = strInput.replaceAll("\\.\\.\\."," ... ");
        strInput = strInput.replaceAll("([;\\!\\?])([^ ])","$1 $2");
            
        //replace any amount of white spaces with one space
        strInput = strInput.replaceAll("\\s+", " ");
        
        //make sure there are no leading or trailing spaces
        strInput = strInput.trim();
        
        String[] strOutput;
        //split based on whitespace
        strOutput = strInput.split(" ");

        ArrayList<String> lstTokens = new ArrayList<String>(Arrays.asList(strOutput));
        Pattern p = null;
        Matcher m = null;
        Pattern p2 = null;
        Matcher m2 = null;
        
           for (int i=0; i < lstTokens.size();i++)
           {
               
               //cut off preceding punctuation 
               p = Pattern.compile("^([" + P_CHAR + "])(.+)");
               m = p.matcher(lstTokens.get(i));
               if (m.find())
               {
                  lstTokens.remove(i);
                  lstTokens.add(i, m.group(2));
                  lstTokens.add(i, m.group(1));
                  continue; //advance further in the loop, checking the token without preceding punctuation
               }               
               
               //cut off trailing punctuation 
               p = Pattern.compile("^(.+)([" + F_CHAR + "])$");
               m = p.matcher(lstTokens.get(i));
               if (m.find())
               {
                  lstTokens.remove(i);
                  lstTokens.add(i, m.group(2));
                  lstTokens.add(i, m.group(1));
                  i--; //do not advance, the token needs to be checked without trailing punctuation
                  continue; //repeat the loop, checking the same token without trailing punctuation
               }

               //cut off trailing periods if punctuation precedes
               p = Pattern.compile("^(.+[" + F_CHAR + "])(\\.)$");
               m = p.matcher(lstTokens.get(i));
               if (m.find())
               {
                  lstTokens.remove(i);
                  lstTokens.add(i, m.group(2));
                  lstTokens.add(i, m.group(1));
                  i--; //do not advance, the token needs to be checked without trailing period
                  continue; //repeat the loop, checking the same token without trailing period
               }

               //check abbreviation list
               if (this.getAbbreviations().contains(lstTokens.get(i)))
               {
                   //known abbreviation found
                   continue;
               }

               //abbreviations of the form A. or U.S.A.
               p = Pattern.compile("^([A-Za-zÁÂÃÈý®Ð×ÝÞÍðÎÓÔÕØÙãõš›€ß‚ƒ„‡ˆ‰Š‹ŒŽøŸ÷·”“’]\\.)+$");
               m = p.matcher(lstTokens.get(i));
               if (m.find())
               {
                  continue; //leave this acronym token alone and advance the loop
               }
               
               //disambiguate periods
               p = Pattern.compile("^(.+)(\\.)$");
               m = p.matcher(lstTokens.get(i));
               p2 = Pattern.compile("^(\\.\\.\\.|[0-9]+\\.)$");
               m2 = p2.matcher(lstTokens.get(i));
               if (m.find() && !m2.find())
               {
                  lstTokens.remove(i);
                  lstTokens.add(i, m.group(2));
                  lstTokens.add(i, m.group(1));
                  i++; //no need to check next token, as it is a separate period
                  continue; //advance the loop
               }

                //attempt to separate proclitics
                p = Pattern.compile("^(" + PClitic + ")(.+)$");
                m = p.matcher(lstTokens.get(i));
               if (m.find() && !PClitic.isEmpty())
               {
                  lstTokens.remove(i);
                  lstTokens.add(i, m.group(2));
                  lstTokens.add(i, m.group(1));
                  continue; //proclitic has been removed, but next token must still be checked
               }

               //attempt to separate enclitics
               p = Pattern.compile("(.+)(" + FClitic + ")$");
               m = p.matcher(lstTokens.get(i));
               if (m.find() && !FClitic.isEmpty())
               {
                  lstTokens.remove(i);
                  lstTokens.add(i, m.group(2));
                  lstTokens.add(i, m.group(1));
                  i++; //next token is a known enclitic, skip it
                  continue; //advance to get past the enclitic
               }

                   
               
           }
        
        return lstTokens;
    }
// ======================= end: important issues
}
