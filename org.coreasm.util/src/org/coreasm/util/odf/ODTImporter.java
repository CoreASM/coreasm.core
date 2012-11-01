/*	
 * ODTImporter.java 	$Revision: 173 $
 * Created on 29/gen/07
 * 
 * Copyright (C) 2007 Vincenzo Gervasi
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2010-05-12 01:47:11 +0200 (Mi, 12 Mai 2010) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.util.odf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ODTImporter {

	private static final String CONTENTSENTRYNAME = "content.xml";
	private static final String PAR_NODE_NAME = "text:p";
	private static final String STYLE_ATTR_NAME = "text:style-name";
	private static final String STYLE_ATTR_VALUE = "CoreASM_20_Code"; 		// Style name for code blocks
	private static final String TAB_NODE_NAME = "text:tab";					// node name for tabs
	private static final String LINEBREAK_NODE_NAME = "text:line-break";	// node name for "soft" line-breaks
	private static final String NOTE_NODE_NAME = "text:note";				// node name for notes
	private static final String STYLE_DEF_NODE_NAME = "style:style";
	private static final String STYLE_PARENT_ATTR_NAME = "style:parent-style-name";
	private static final String STYLE_DEF_NAME_ATTR = "style:name";

	// A simple command-line main; it will translate all the files whose names are provided as arguments,
	// saving the results in files with the same base name, but ".odt" substituted by ".coreasm" (if the
	// extension is not .odt, the .coreasm is simply added to the name).
	
	public static void main(String args[])
	{
		String buffer=null;
		for (String s:args) {
			try {
				buffer=null; // so we can tell whether importODT() worked or there was an exception
				buffer=importODT(s);
			} catch (FileNotFoundException e) {
				System.err.println("Could not find file '"+s+"' -- file ignored.");
			} catch (IOException e) {
				System.err.println("General I/O error in '"+s+"' -- file ignored. Details follow:");
				e.printStackTrace();
			}
			if (buffer!=null) {
				// Ok, everything went well. Save the file and go to the next one
				PrintWriter out;
				String outfile=s.replaceAll("\\.odt$", "")+".coreasm";
				try {
					out = new PrintWriter(outfile);
					out.println(buffer);
					out.close();
				} catch (FileNotFoundException e) {
					System.err.println("General I/O error in '"+s+"' -- could not write output file. Details follow:");
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Extracts CoreASM specification from an ODT file. The specification in the file
	 * should be in the "CoreASM Code" style. 
	 * 
	 * @param fileName name of an OpenOffice (ODF/.odt) file to read
	 * @return a string representation of the full CoreASM specification extracted from the ODF file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String importODT(String fileName) throws FileNotFoundException, IOException {
		StringBuffer buffer=new StringBuffer(16*1024);
		ZipInputStream zis=new ZipInputStream(new FileInputStream(fileName));
		ZipEntry ze;
		do {
			ze=zis.getNextEntry();
		} while (ze!=null && !ze.getName().equalsIgnoreCase(CONTENTSENTRYNAME));
		if (ze!=null) {
			process(zis,buffer);
		}
		return buffer.toString().replace('\u201c', '"').replace('\u201d','"');
	}

	private static void process(InputStream is, StringBuffer buffer) throws IOException {
		boolean inblock=true;
		Document doc=parseXml(is);
		
		// Process new style information
		Set<String> coreasmStyles = new HashSet<String>();
		NodeList styleDefs = doc.getElementsByTagName(STYLE_DEF_NODE_NAME);
		if (styleDefs != null) {
			for (int i=0; i < styleDefs.getLength(); i++){
				Element def = (Element)styleDefs.item(i);
				if (STYLE_ATTR_VALUE.equals(def.getAttribute(STYLE_PARENT_ATTR_NAME)))
					coreasmStyles.add(def.getAttribute(STYLE_DEF_NAME_ATTR));
			}
		}
		
		NodeList paragraphs=doc.getElementsByTagName(PAR_NODE_NAME);
		if (paragraphs!=null) {
			for (int i=0;i<paragraphs.getLength();i++) {
				Element par=(Element)paragraphs.item(i);
				final String styleName = par.getAttribute(STYLE_ATTR_NAME);
				if (styleName.equals(STYLE_ATTR_VALUE)
						|| coreasmStyles.contains(styleName)) {
					if (inblock==false) {
						buffer.append("\n");
						inblock=true;
					}
					// recurse into children
					handleChildren(par.getChildNodes(), buffer);
					buffer.append("\n");
				} else
					inblock=false;
			}
			
		}	
	}
	
	
	private static void handleChildren(NodeList childNodes, StringBuffer buffer) {
		// if there are no children, return...
		if (childNodes == null) {
			return;
		}
		
		for (int j = 0; j < childNodes.getLength(); j++) {
			Node n2 = childNodes.item(j);
			/*
			 * -- If we're dealing with elements (not text nodes) check for these
			 * -- special cases:
			 * --   a) if element is a line-break, output a line-break
			 * --   b) if element is a tab, output a tab
			 * --   c) if element is a (foot)note, do not recurse
			 */
			if (n2 instanceof Element) {
				String tagName = ((Element)n2).getTagName();
				if (LINEBREAK_NODE_NAME.equals(tagName)) {
					buffer.append("\n");
				} 
				else {
					if (TAB_NODE_NAME.equals(tagName)) {
						buffer.append("\t");
					}
				}
				
				if (!NOTE_NODE_NAME.equals(tagName)) {
					handleChildren(n2.getChildNodes(), buffer);
				}
			} 
			/*
			 * -- non-elements (text nodes) are appended
			 */
			else {				
				if (n2!=null) {
					buffer.append(n2.getTextContent());
				}
			}
		}
	}

	private static Document parseXml(InputStream is) {
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return parser.parse(new InputSource(is));
		} 
		catch (SAXException se) { se.printStackTrace(); }
		catch (IOException ioe) { ioe.printStackTrace(); }
		catch (ParserConfigurationException pce) { pce.printStackTrace(); }
		return null;
	}
}
