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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.nlpModules.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.nlpModules.TokenizerProperties;

public class TokenizerPropertiesTest{
	
	protected TokenizerProperties fixture= null;

	public TokenizerProperties getFixture() {
		return fixture;
	}

	public void setFixture(TokenizerProperties fixture) {
		this.fixture = fixture;
	}
	@Before
	public void setUp()
	{
		this.setFixture(new TokenizerProperties());
	}
	@Test
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