-- Idonow initial static data

-- INSERT CURRENCIES (Java class java.util.Currency has incomplete data for AMD)

INSERT INTO currency
    (iso_code, numeric_code, display_name, symbol, fraction_digits)
VALUES ('AMD', '051', 'Armenian Dram', '֏', 2),
       ('USD', '840', 'US Dollar', '$', 2),
       ('RUB', '643', 'Russian Ruble', '₽', 2),
       ('EUR', '978', 'Euro', '€', 2);


-- INSERT COUNTRIES

INSERT INTO country (country_code, name, currency_id)
VALUES ('AM', 'Republic of Armenia', (SELECT id from currency WHERE iso_code = 'AMD'));


-- INSERT MEASUREMENT UNITS

INSERT INTO measurement_unit
    (common_code, level_cat, name, representation_symbol)
VALUES ('MTR', '1', 'metre', 'm'),
       ('MTK', '1', 'square metre', 'm²'),
       ('MTQ', '1', 'cubic metre', 'm³'),
       ('DMT', '1M', 'decimetre', 'dm'),
       ('DMK', '1S', 'square decimetre', 'dm²'),
       ('DMQ', '1S', 'cubic decimetre', 'dm³'),
       ('LM', '3.1C', 'linear metre', null),
       ('GRM', '1S', 'gram', 'g'),
       ('KGM', '1', 'kilogram', 'kg'),
       ('TNE', '1S', 'tonne', 't'),
       ('H87', '3.8', 'piece', null),
       ('LTR', '1', 'litre', 'l'),
       ('HAR', '1S', 'hectare', 'ha');
