// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions;

import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for validating parameters.
 */
public final class ValidationUtil {
    private static Pattern tenantIdentifierCharPattern = Pattern.compile("^(?:[A-Z]|[0-9]|[a-z]|-|.)+$");

    public static void validateAllEmpty(String className, Map<String, Object> parameters) {
        ClientLogger logger = new ClientLogger(className);
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (entry.getValue() == null) {
                missing.add(entry.getKey());
            }
        }
        if (missing.size() == parameters.size()) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Must provide non-null values for "
                + String.join(" or ", missing) + " properties in " + className));
        }
    }
}
