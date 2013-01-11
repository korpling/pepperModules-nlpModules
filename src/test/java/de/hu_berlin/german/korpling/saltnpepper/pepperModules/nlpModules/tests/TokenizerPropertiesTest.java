package de.hu_berlin.german.korpling.saltnpepper.pepperModules.nlpModules.tests;

import java.io.File;

import junit.framework.TestCase;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.nlpModules.TokenizerProperties;

public class TokenizerPropertiesTest extends TestCase {
	
	protected TokenizerProperties fixture= null;

	public TokenizerProperties getFixture() {
		return fixture;
	}

	public void setFixture(TokenizerProperties fixture) {
		this.fixture = fixture;
	}
	
	public void setUp()
	{
		this.setFixture(new TokenizerProperties());
	}
	
	public void test_PROP_ABBREVIATION_FOLDER()
	{
		assertNull(this.getFixture().getAbbreviationFolder());
		
		String testFileName= "notExisting";
		File testFile=new File(testFileName); 
		
		try {
			this.getFixture().setPropertyValue(TokenizerProperties.PROP_ABBREVIATION_FOLDER, testFile);
			fail("Should fail, because abbreviation folder does not exist");
		} catch (Exception e) {
		}
		
		
		assertEquals(testFile, this.getFixture().getAbbreviationFolder());
		
		testFileName= System.getProperty("java.io.tmpdir")+"/tokPropTestAbbFolder";
		testFile=new File(testFileName); 
		testFile.mkdirs();
		
		this.getFixture().setPropertyValue(TokenizerProperties.PROP_ABBREVIATION_FOLDER, testFile);
		assertEquals(testFile, this.getFixture().getAbbreviationFolder());
	}
}