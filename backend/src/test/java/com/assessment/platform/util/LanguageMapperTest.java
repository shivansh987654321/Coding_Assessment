package com.assessment.platform.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LanguageMapperTest {

    @Test
    void returnsJavaByDefaultWhenLanguageIsNull() {
        assertEquals(62, LanguageMapper.toJudge0Id(null));
    }

    @Test
    void mapsKnownLanguagesCaseInsensitively() {
        assertEquals(71, LanguageMapper.toJudge0Id("Python"));
        assertEquals(54, LanguageMapper.toJudge0Id("CPP"));
    }

    @Test
    void fallsBackToJavaForUnknownLanguages() {
        assertEquals(62, LanguageMapper.toJudge0Id("kotlin"));
    }
}