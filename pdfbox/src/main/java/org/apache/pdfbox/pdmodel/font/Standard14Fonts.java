/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.pdmodel.font;

import java.awt.geom.GeneralPath;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.fontbox.FontBoxFont;
import org.apache.fontbox.afm.AFMParser;
import org.apache.fontbox.afm.FontMetrics;
import org.apache.pdfbox.pdmodel.font.encoding.GlyphList;
import org.apache.pdfbox.pdmodel.font.encoding.SymbolEncoding;

import static org.apache.pdfbox.pdmodel.font.UniUtil.getUniNameOfCodePoint;

/**
 * The "Standard 14" PDF fonts, also known as the "base 14" fonts.
 * There are 14 font files, but Acrobat uses additional names for compatibility, e.g. Arial.
 *
 * @author John Hewson
 */
public final class Standard14Fonts
{
    /**
     * Contains all base names and alias names for the known fonts.
     * For base fonts both the key and the value will be the base name.
     * For aliases, the key is an alias, and the value is a base name.
     * We want a single lookup in the map to find the font both by a base name or an alias.
     */
    private static final Map<String, String> ALIASES = new HashMap<String, String>(38);

    /**
     * Contains the font metrics for the base fonts.
     * The key is a base font name, value is a FontMetrics instance.
     * Metrics are loaded into this map on demand, only if needed.
     * @see #getAFM
     */
    private static final Map<String, FontMetrics> FONTS =  new HashMap<String, FontMetrics>(14);
//    private static final Map<String, FontMetrics> FONTS = new EnumMap<FontName, FontMetrics>(14);

    /**
     * Contains the mapped fonts for the standard 14 fonts.
     * The key is the font name, value is a FontBoxFont instance.
     * FontBoxFont are loaded into this map on demand, only if needed.
     */
    private static final Map<String, FontBoxFont> GENERIC_FONTS = new HashMap<String, FontBoxFont>();


    static {
        // the 14 standard fonts
        mapName("Courier-Bold");
        mapName("Courier-BoldOblique");
        mapName("Courier");
        mapName("Courier-Oblique");
        mapName("Helvetica");
        mapName("Helvetica-Bold");
        mapName("Helvetica-BoldOblique");
        mapName("Helvetica-Oblique");
        mapName("Symbol");
        mapName("Times-Bold");
        mapName("Times-BoldItalic");
        mapName("Times-Italic");
        mapName("Times-Roman");
        mapName("ZapfDingbats");

        // alternative names from Adobe Supplement to the ISO 32000
        mapName("CourierCourierNew", "Courier");
        mapName("CourierNew", "Courier");
        mapName("CourierNew,Italic", "Courier-Oblique");
        mapName("CourierNew,Bold", "Courier-Bold");
        mapName("CourierNew,BoldItalic", "Courier-BoldOblique");
        mapName("Arial", "Helvetica");
        mapName("Arial,Italic", "Helvetica-Oblique");
        mapName("Arial,Bold", "Helvetica-Bold");
        mapName("Arial,BoldItalic", "Helvetica-BoldOblique");
        mapName("TimesNewRoman", "Times-Roman");
        mapName("TimesNewRoman,Italic", "Times-Italic");
        mapName("TimesNewRoman,Bold", "Times-Bold");
        mapName("TimesNewRoman,BoldItalic", "Times-BoldItalic");

        // Acrobat treats these fonts as "standard 14" too (at least Acrobat preflight says so)
        mapName("Symbol,Italic", "Symbol");
        mapName("Symbol,Bold", "Symbol");
        mapName("Symbol,BoldItalic", "Symbol");
        mapName("Times", "Times-Roman");
        mapName("Times,Italic", "Times-Italic");
        mapName("Times,Bold", "Times-Bold");
        mapName("Times,BoldItalic", "Times-BoldItalic");

        // PDFBOX-3457: PDF.js file bug864847.pdf
        mapName("ArialMT", "Helvetica");
        mapName("Arial-ItalicMT", "Helvetica-Oblique");
        mapName("Arial-BoldMT", "Helvetica-Bold");
    }


    private Standard14Fonts()
    {
    }

    /**
     * Loads the metrics for the base font specified by name. Metric file must exist in the pdfbox
     * jar under /org/apache/pdfbox/resources/afm/
     *
     * @param fontName one of the standard 14 font names for which to lod the metrics.
     * @throws IOException if no metrics exist for that font.
     */
    private static void loadMetrics(String fontName) throws IOException
    {
        String resourceName = "/org/apache/pdfbox/resources/afm/" + fontName + ".afm";
        InputStream resourceAsStream = PDType1Font.class.getResourceAsStream(resourceName);
        if (resourceAsStream == null)
        {
            throw new IOException("resource '" + resourceName + "' not found");
        }
        InputStream afmStream = new BufferedInputStream(resourceAsStream);
        try
        {
            AFMParser parser = new AFMParser(afmStream);
            FontMetrics metric = parser.parse(true);
            FONTS.put(fontName, metric);
        }
        finally
        {
            afmStream.close();
        }        
    }

    private static void loadMetrics(FontName fontName) throws IOException
    {
        String resourceName = "/org/apache/pdfbox/resources/afm/" + fontName.getName() + ".afm";
        InputStream resourceAsStream = PDType1Font.class.getResourceAsStream(resourceName);
        if (resourceAsStream == null)
        {
            throw new IOException("resource '" + resourceName + "' not found");
        }
        InputStream afmStream = new BufferedInputStream(resourceAsStream);
        try
        {
            AFMParser parser = new AFMParser(afmStream);
            FontMetrics metric = parser.parse(true);
            FONTS.put(fontName.getName(), metric);
        }
        finally
        {
            afmStream.close();
        }
    }

    /**
     * Adds a standard font name to the map of known aliases, to simplify the logic of finding
     * font metrics by name. We want a single lookup in the map to find the font both by a base name or
     * an alias.
     *
     * @see #getAFM
     * @param baseName the base name of the font; must be one of the 14 standard fonts
     */
    private static void mapName(String baseName)
    {
        ALIASES.put(baseName, baseName);
    }

    /**
     * Adds an alias name for a standard font to the map of known aliases to the map of aliases
     * (alias as key, standard name as value). We want a single lookup in the map to find the font
     * both by a base name or an alias.
     *
     * @param alias an alias for the font
     * @param baseName the base name of the font; must be one of the 14 standard fonts
     */
    private static void mapName(String alias, String baseName)
    {
        ALIASES.put(alias, baseName);
    }

    private static void mapName(String alias, FontName baseName)
    {
        ALIASES.put(alias, baseName.getName());
    }

    /**
     * Returns the metrics for font specified by fontName. Loads the font metrics if not already
     * loaded.
     *
     * @param fontName name of font; either a base name or alias
     * @return the font metrics or null if the name is not one of the known names
     * @throws IllegalArgumentException if no metrics exist for that font.
     */
    public static FontMetrics getAFM(String fontName)
    {
        String baseName = ALIASES.get(fontName);
        if (baseName == null)
        {
            return null;
        }

        if (FONTS.get(baseName) == null)
        {
            synchronized (FONTS)
            {
                if (FONTS.get(baseName) == null)
                {
                    try
                    {
                        loadMetrics(baseName);
                    }
                    catch (IOException ex)
                    {
                        throw new IllegalArgumentException(ex);
                    }
                }
            }
        }

        return FONTS.get(baseName);
    }

    /**
     * Returns true if the given font name is one of the known names, including alias.
     *
     * @param fontName the name of font, either a base name or alias
     * @return true if the name is one of the known names
     */
    public static boolean containsName(String fontName)
    {
        return ALIASES.containsKey(fontName);
    }

    /**
     * Returns the set of known font names, including aliases.
     */
    public static Set<String> getNames()
    {
        return Collections.unmodifiableSet(ALIASES.keySet());
    }

    /**
     * Returns the base name of the font which the given font name maps to.
     *
     * @param fontName name of font, either a base name or an alias
     * @return the base name or null if this is not one of the known names
     */
    public static String getMappedFontName(String fontName)
    {
        return ALIASES.get(fontName);
    }

    /**
     * Returns the mapped font for the specified Standard 14 font. The mapped font is cached.
     *
     * @param baseName name of the standard 14 font
     * @return the mapped font
     */
    private static FontBoxFont getMappedFont(String baseName) throws IOException {
        if (!GENERIC_FONTS.containsKey(baseName))
        {
            synchronized (GENERIC_FONTS)
            {
                if (!GENERIC_FONTS.containsKey(baseName))
                {
                    PDType1Font type1Font = new PDType1Font(baseName);
                    GENERIC_FONTS.put(baseName, type1Font.getFontBoxFont());
                }
            }
        }
        return GENERIC_FONTS.get(baseName);
    }




    /**
     * Returns the path for the character with the given name for the specified Standard 14 font. The mapped font is
     * cached. The path may differ in different environments as it depends on the mapped font.
     *
     * @param baseName name of the standard 14 font
     * @param glyphName name of glyph
     * @return the mapped font
     *
     * @throws IOException if the data could not be read
     */
    public static GeneralPath getGlyphPath(String baseName, String glyphName) throws IOException
    {
        // copied and adapted from PDType1Font.getNameInFont(String)
        if (!glyphName.equals(".notdef"))
        {
            FontBoxFont mappedFont = getMappedFont(baseName);
            if (mappedFont != null)
            {
                if (mappedFont.hasGlyph(glyphName))
                {
                    return mappedFont.getPath(glyphName);
                }
                String unicodes = getGlyphList(baseName).toUnicode(glyphName);
                if (unicodes != null && unicodes.length() == 1)
                {
                    String uniName = getUniNameOfCodePoint(unicodes.codePointAt(0));
                    if (mappedFont.hasGlyph(uniName))
                    {
                        return mappedFont.getPath(uniName);
                    }
                }
                if ("SymbolMT".equals(mappedFont.getName()))
                {
                    Integer code = SymbolEncoding.INSTANCE.getNameToCodeMap().get(glyphName);
                    if (code != null)
                    {
                        String uniName = getUniNameOfCodePoint(code + 0xF000);
                        if (mappedFont.hasGlyph(uniName))
                        {
                            return mappedFont.getPath(uniName);
                        }
                    }
                }
            }
        }
        return new GeneralPath();
    }

    private static GlyphList getGlyphList(String baseName)
    {
        return FontName.ZAPF_DINGBATS.getName().equals(baseName) ? GlyphList.getZapfDingbats()
                : GlyphList.getAdobeGlyphList();
    }
    /**
     * Enum for the names of the 14 standard fonts.
     */
    public enum FontName
    {
        TIMES_ROMAN("Times-Roman"), //
        TIMES_BOLD("Times-Bold"), //
        TIMES_ITALIC("Times-Italic"), //
        TIMES_BOLD_ITALIC("Times-BoldItalic"), //
        HELVETICA("Helvetica"), //
        HELVETICA_BOLD("Helvetica-Bold"), //
        HELVETICA_OBLIQUE("Helvetica-Oblique"), //
        HELVETICA_BOLD_OBLIQUE("Helvetica-BoldOblique"), //
        COURIER("Courier"), //
        COURIER_BOLD("Courier-Bold"), //
        COURIER_OBLIQUE("Courier-Oblique"), //
        COURIER_BOLD_OBLIQUE("Courier-BoldOblique"), //
        SYMBOL("Symbol"), //
        ZAPF_DINGBATS("ZapfDingbats");

        private final String name;

        private FontName(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
