package dev.che1s.syalsdelight.util;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageResolverTest {
    @Test
    void acceptsFileNamesAndLegacyBareCodes() {
        assertEquals("zh_cn", LanguageResolver.select(
                "zh_cn.yml", Locale.US).language());
        assertEquals("ja_jp", LanguageResolver.select(
                "JA-JP.yml", Locale.US).language());
        assertEquals("de_de", LanguageResolver.select(
                "de", Locale.US).language());
    }

    @Test
    void autoUsesServerLocaleAndRegionalAliases() {
        assertEquals("ja_jp", LanguageResolver.select(
                "auto", Locale.JAPAN).language());
        assertEquals("zh_tw", LanguageResolver.select(
                "auto", Locale.forLanguageTag("zh-Hant-HK")).language());
        assertEquals("es_es", LanguageResolver.select(
                "auto", Locale.forLanguageTag("es-MX")).language());
        assertEquals("de_de", LanguageResolver.select(
                "auto", Locale.GERMANY).language());
    }

    @Test
    void unsupportedAutoAndManualLanguagesFallBackToEnglish() {
        LanguageResolver.Selection automatic = LanguageResolver.select(
                "auto", Locale.forLanguageTag("nl-NL"));
        assertEquals("en_us", automatic.language());
        assertTrue(automatic.automatic());
        assertTrue(automatic.fallback());

        LanguageResolver.Selection manual = LanguageResolver.select(
                "nl_nl.yml", Locale.JAPAN);
        assertEquals("en_us", manual.language());
        assertFalse(manual.automatic());
        assertTrue(manual.fallback());
    }

    @Test
    void builtInLanguageListContainsEveryRequestedLocale() {
        assertEquals(9, LanguageResolver.SUPPORTED.size());
        assertEquals(
                java.util.Set.of(
                        "en_us", "zh_cn", "ja_jp", "ko_kr", "zh_tw",
                        "de_de", "fr_fr", "es_es", "ru_ru"
                ),
                java.util.Set.copyOf(LanguageResolver.SUPPORTED)
        );
    }
}
