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

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public record SASToken(SASToken.SASType type, String organization, String space, String token) {
    private static final String PROP_DURATION = "duration";
    /**
     * Regex for duration within the SAS-Token ('se' within the token - see
     * <a href="https://www.c-sharpcorner.com/article/demystifying-sas-token-basics/">Demystifying SAS Tokens - Basics</a>)
     */
    private static final String DURATION_REGEX = format("^.+se=(?<%s>(?:[1-9]\\d{3}-(?:(?:0[1-9]|1[0-2])-" + "(?:0[1-9]|1\\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(?:Z|[+-][01]\\d:[0-5]\\d)).+$", PROP_DURATION);
    private static final Pattern DURATION_PATTERN = Pattern.compile(DURATION_REGEX);

    public boolean isValid(int cacheBuffer) {
        String encoded = URLDecoder.decode(token, Charset.defaultCharset());
        Matcher matcher = DURATION_PATTERN.matcher(encoded);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid sas-token!");
        }
        String duration = matcher.group(PROP_DURATION);
        ZonedDateTime timeout = ZonedDateTime.parse(duration);

        return ZonedDateTime.now().isBefore(timeout.minusMinutes(cacheBuffer));
    }

    public enum SASType {
        READ, WRITE, DELETE
    }

}
