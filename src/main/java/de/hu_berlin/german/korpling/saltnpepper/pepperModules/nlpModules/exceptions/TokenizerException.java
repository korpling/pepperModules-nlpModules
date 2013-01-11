package de.hu_berlin.german.korpling.saltnpepper.pepperModules.nlpModules.exceptions;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;

public class TokenizerException extends PepperModuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8025820959230689282L;
	
	public TokenizerException()
	{ super(); }
	
    public TokenizerException(String s)
    { super(s); }
    
	public TokenizerException(String s, Throwable ex)
	{super(s, ex); }

}
