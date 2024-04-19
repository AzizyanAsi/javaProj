package net.idonow.common.util;

import net.idonow.common.data.CountryCode;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberUtils {

    public final static Map<CountryCode, PhoneNumberData> phonePatternMap = new HashMap<>();

    private final static String placeholder = "###";

    static {
        phonePatternMap.put(CountryCode.AM, new PhoneNumberData("+374",
                "^(" + placeholder + "|0)?[\\s]?(\\d{2}|\\(\\d{2}\\))[-\\s]?(\\d{2})[-\\s]?(\\d{2})[-\\s]?(\\d{2})$"));
    }

    // PhoneNumberData: +<prefix>(<local>)<number>
    public static boolean validate(String rawNumber, CountryCode countryCode) {
        PhoneNumberData numberPattern = phonePatternMap.get(countryCode);
        // Return true for null values
        if (rawNumber == null) {
            return true;
        }
        if (numberPattern != null) {
            Matcher matcher = numberPattern.getRegexpPattern().matcher(rawNumber);
            return matcher.matches();
        }
        return false;
    }

    // MUST be called ONLY AFTER validate for the same CountryCode
    // Normalizing by pattern to split into groups by parentheses on demand
    public static String normalize(String rawNumber, CountryCode countryCode) {
        PhoneNumberData numberPattern = phonePatternMap.get(countryCode);
        Matcher matcher = numberPattern.getRegexpPattern().matcher(rawNumber);

        if (matcher.matches()) {
            StringBuilder sb = new StringBuilder(numberPattern.getCountryCode());
            for (short i = 2; i <= matcher.groupCount(); i++) {
                String group = matcher.group(i);
                if (i == 2 && group.startsWith("(") && group.endsWith(")")) {
                    group = group.substring(1, group.length() - 1);
                }
                sb.append(group);
            }
            return sb.toString();
        }
        return rawNumber;
    }

    public static PhoneNumberData getPhoneNumberData(CountryCode countryCode) {
        return countryCode == null ? null : phonePatternMap.get(countryCode);
    }

    public static class PhoneNumberData {

        private final String countryCode;
        private final Pattern regexpPattern;

        PhoneNumberData(String countryCode, String patternProxy) {
            // Replace placeholder of patternProxy with countryCode ('+' sign needs escaping)
            this.regexpPattern = Pattern.compile(patternProxy.replaceFirst(placeholder, "\\\\" + countryCode));
            this.countryCode = countryCode;
        }

        public Pattern getRegexpPattern() {
            return regexpPattern;
        }

        public String getCountryCode() {
            return countryCode;
        }
    }
}
