package net.idonow.common.cache;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public abstract class EntityCacheNames {

    public static final String COUNTRY = "countryByCode";
    public static final String ALL_COUNTRIES = "allCountries";

    public static final String CURRENCY = "currency";
    public static final String ALL_CURRENCIES = "allCurrencies";

    public static final String MEASUREMENT = "measurement";
    public static final String ALL_MEASUREMENTS = "allMeasurements";

    public static final String PROFESSION_CATEGORY = "profCategory";
    public static final String ALL_PROFESSION_CATEGORIES = "allProfCategories";

    public static final String PROFESSION = "profession";
    public static final String ALL_PROFESSIONS = "allProfessions";

    public static final String ROLE = "role";
    public static final String ALL_ROLES = "allRoles";

    public static final String USER = "user";
    public static final String PROFESSIONAL = "professional";
    public static final String SYSTEM_USER = "systemUser";
    public static final String ALL_SYSTEM_USERS = "allSystemUsers";
    public static final String ALL_USERS = "allUsers";
    public static Set<String> getCacheNames() throws IllegalAccessException {

        Set<String> cacheNames = new HashSet<>();

        // Get fields via reflection
        Field[] fields = EntityCacheNames.class.getFields();
        // Get value for each field
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(null);
            cacheNames.add(value.toString());
        }
        return cacheNames;
    }

}
