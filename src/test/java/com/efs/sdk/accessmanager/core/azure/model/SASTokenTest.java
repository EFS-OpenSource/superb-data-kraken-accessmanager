/*
Copyright (C) 2023 e:fs TechHub GmbH (sdk@efs-techhub.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.efs.sdk.accessmanager.core.azure.model;

import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class SASTokenTest {

    private final int cacheBuffer = 2;

    @Test
    void givenValidToken_whenIsValid_thenTrue() {
        // start of token 20 minutes ago
        String start = URLEncoder.encode(ZonedDateTime.now().minusMinutes(20L).truncatedTo(ChronoUnit.SECONDS).toInstant().atZone(ZoneOffset.UTC).toString(),
                Charset.defaultCharset());
        // end of token in 20 minutes
        String end = URLEncoder.encode(ZonedDateTime.now().plusMinutes(20L).truncatedTo(ChronoUnit.SECONDS).toInstant().atZone(ZoneOffset.UTC).toString(),
                Charset.defaultCharset());

        String token = "sv=2021-04-10&st=" + start + "&se=" + end + "&sr=c&sp=racwl&sig=ebgCqSgWRsNo" + "%2Fjs6MTNIXYQ5MxG%2FLvIoceZB24LetFY%3D";

        SASToken sasToken = new SASToken(SASToken.SASType.READ, "myorga", "myspace", token);
        assertTrue(sasToken.isValid(cacheBuffer));
    }

    @Test
    void givenTimedoutToken_whenIsValid_thenFalse() {
        // start of token 20 minutes ago
        String start = URLEncoder.encode(ZonedDateTime.now().minusMinutes(20L).truncatedTo(ChronoUnit.SECONDS).toInstant().atZone(ZoneOffset.UTC).toString(),
                Charset.defaultCharset());
        // end of token 5 minutes ago
        String end = URLEncoder.encode(ZonedDateTime.now().minusMinutes(5L).truncatedTo(ChronoUnit.SECONDS).toInstant().atZone(ZoneOffset.UTC).toString(),
                Charset.defaultCharset());

        String token = "sv=2021-04-10&st=" + start + "&se=" + end + "&sr=c&sp=racwl&sig=ebgCqSgWRsNo" + "%2Fjs6MTNIXYQ5MxG%2FLvIoceZB24LetFY%3D";

        SASToken sasToken = new SASToken(SASToken.SASType.READ, "myorga", "myspace", token);
        assertFalse(sasToken.isValid(cacheBuffer));
    }

    @Test
    void givenInvalidToken_whenIsValid_thenError() {
        // start of token 20 minutes ago
        String start = "asdf";
        // end of token 5 minutes ago
        String end = "asdf";

        String token = "sv=2021-04-10&st=" + start + "&se=" + end + "&sr=c&sp=racwl&sig=ebgCqSgWRsNo" + "%2Fjs6MTNIXYQ5MxG%2FLvIoceZB24LetFY%3D";

        SASToken sasToken = new SASToken(SASToken.SASType.READ, "myorga", "myspace", token);
        assertThrows(IllegalArgumentException.class, () -> sasToken.isValid(cacheBuffer));
    }
}
