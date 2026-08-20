package com.ots.wikiscrapper.domain;

import java.util.Map;

/** Polish Wikipedia article titles for world countries (ISO 3166-1 alpha-2). */
public final class PolishCountryWikiTitles {

    private static final Map<String, String> BY_ISO = Map.ofEntries(
            Map.entry("AF", "Afganistan"), Map.entry("AL", "Albania"), Map.entry("DZ", "Algieria"),
            Map.entry("AD", "Andora"), Map.entry("AO", "Angola"), Map.entry("AG", "Antigua i Barbuda"),
            Map.entry("AR", "Argentyna"), Map.entry("AM", "Armenia"), Map.entry("AU", "Australia"),
            Map.entry("AT", "Austria"), Map.entry("AZ", "Azerbejdżan"), Map.entry("BS", "Bahamy"),
            Map.entry("BH", "Bahrajn"), Map.entry("BD", "Bangladesz"), Map.entry("BB", "Barbados"),
            Map.entry("BY", "Białoruś"), Map.entry("BE", "Belgia"), Map.entry("BZ", "Belize"),
            Map.entry("BJ", "Benin"), Map.entry("BT", "Bhutan"), Map.entry("BO", "Boliwia"),
            Map.entry("BA", "Bośnia i Hercegowina"), Map.entry("BW", "Botswana"), Map.entry("BR", "Brazylia"),
            Map.entry("BN", "Brunei"), Map.entry("BG", "Bułgaria"), Map.entry("BF", "Burkina Faso"),
            Map.entry("BI", "Burundi"), Map.entry("CV", "Republika Zielonego Przylądka"),
            Map.entry("KH", "Kambodża"), Map.entry("CM", "Kamerun"), Map.entry("CA", "Kanada"),
            Map.entry("CF", "Republika Środkowoafrykańska"), Map.entry("TD", "Czad"), Map.entry("CL", "Chile"),
            Map.entry("CN", "Chiny"), Map.entry("CO", "Kolumbia"), Map.entry("KM", "Komory"), Map.entry("CG", "Kongo"),
            Map.entry("CD", "Demokratyczna Republika Konga"), Map.entry("CR", "Kostaryka"),
            Map.entry("CI", "Wybrzeże Kości Słoniowej"), Map.entry("HR", "Chorwacja"), Map.entry("CU", "Kuba"),
            Map.entry("CY", "Cypr"), Map.entry("CZ", "Czechy"), Map.entry("DK", "Dania"), Map.entry("DJ", "Dżibuti"),
            Map.entry("DM", "Dominika"), Map.entry("DO", "Dominikana"), Map.entry("EC", "Ekwador"),
            Map.entry("EG", "Egipt"), Map.entry("SV", "Salwador"), Map.entry("GQ", "Gwinea Równikowa"),
            Map.entry("ER", "Erytrea"), Map.entry("EE", "Estonia"), Map.entry("SZ", "Eswatini"),
            Map.entry("ET", "Etiopia"), Map.entry("FJ", "Fidżi"), Map.entry("FI", "Finlandia"),
            Map.entry("FR", "Francja"), Map.entry("GA", "Gabon"), Map.entry("GM", "Gambia"),
            Map.entry("GE", "Gruzja"), Map.entry("DE", "Niemcy"), Map.entry("GH", "Ghana"), Map.entry("GR", "Grecja"),
            Map.entry("GD", "Grenada"), Map.entry("GT", "Gwatemala"), Map.entry("GN", "Gwinea"),
            Map.entry("GW", "Gwinea-Bissau"), Map.entry("GY", "Gujana"), Map.entry("HT", "Haiti"),
            Map.entry("HN", "Honduras"), Map.entry("HU", "Węgry"), Map.entry("IS", "Islandia"),
            Map.entry("IN", "Indie"), Map.entry("ID", "Indonezja"), Map.entry("IR", "Iran"), Map.entry("IQ", "Irak"),
            Map.entry("IE", "Irlandia"), Map.entry("IL", "Izrael"), Map.entry("IT", "Włochy"),
            Map.entry("JM", "Jamajka"), Map.entry("JP", "Japonia"), Map.entry("JO", "Jordania"),
            Map.entry("KZ", "Kazachstan"), Map.entry("KE", "Kenia"), Map.entry("KI", "Kiribati"),
            Map.entry("KP", "Korea Północna"), Map.entry("KR", "Korea Południowa"), Map.entry("KW", "Kuwejt"),
            Map.entry("KG", "Kirgistan"), Map.entry("LA", "Laos"), Map.entry("LV", "Łotwa"), Map.entry("LB", "Liban"),
            Map.entry("LS", "Lesotho"), Map.entry("LR", "Liberia"), Map.entry("LY", "Libia"),
            Map.entry("LI", "Liechtenstein"), Map.entry("LT", "Litwa"), Map.entry("LU", "Luksemburg"),
            Map.entry("MG", "Madagaskar"), Map.entry("MW", "Malawi"), Map.entry("MY", "Malezja"),
            Map.entry("MV", "Malediwy"), Map.entry("ML", "Mali"), Map.entry("MT", "Malta"),
            Map.entry("MH", "Wyspy Marshalla"), Map.entry("MR", "Mauretania"), Map.entry("MU", "Mauritius"),
            Map.entry("MX", "Meksyk"), Map.entry("FM", "Mikronezja"), Map.entry("MD", "Mołdawia"),
            Map.entry("MC", "Monako"), Map.entry("MN", "Mongolia"), Map.entry("ME", "Czarnogóra"),
            Map.entry("MA", "Maroko"), Map.entry("MZ", "Mozambik"), Map.entry("MM", "Mjanma"),
            Map.entry("NA", "Namibia"), Map.entry("NR", "Nauru"), Map.entry("NP", "Nepal"),
            Map.entry("NL", "Holandia"), Map.entry("NZ", "Nowa Zelandia"), Map.entry("NI", "Nikaragua"),
            Map.entry("NE", "Niger"), Map.entry("NG", "Nigeria"), Map.entry("MK", "Macedonia Północna"),
            Map.entry("NO", "Norwegia"), Map.entry("OM", "Oman"), Map.entry("PK", "Pakistan"),
            Map.entry("PW", "Palau"), Map.entry("PS", "Palestyna"), Map.entry("PA", "Panama"),
            Map.entry("PG", "Papua-Nowa Gwinea"), Map.entry("PY", "Paragwaj"), Map.entry("PE", "Peru"),
            Map.entry("PH", "Filipiny"), Map.entry("PL", "Polska"), Map.entry("PT", "Portugalia"),
            Map.entry("QA", "Katar"), Map.entry("RO", "Rumunia"), Map.entry("RU", "Rosja"), Map.entry("RW", "Rwanda"),
            Map.entry("KN", "Saint Kitts i Nevis"), Map.entry("LC", "Saint Lucia"),
            Map.entry("VC", "Saint Vincent i Grenadyny"), Map.entry("WS", "Samoa"), Map.entry("SM", "San Marino"),
            Map.entry("ST", "Wyspy Świętego Tomasza i Książęca"), Map.entry("SA", "Arabia Saudyjska"),
            Map.entry("SN", "Senegal"), Map.entry("RS", "Serbia"), Map.entry("SC", "Seszele"),
            Map.entry("SL", "Sierra Leone"), Map.entry("SG", "Singapur"), Map.entry("SK", "Słowacja"),
            Map.entry("SI", "Słowenia"), Map.entry("SB", "Wyspy Salomona"), Map.entry("SO", "Somalia"),
            Map.entry("ZA", "Republika Południowej Afryki"), Map.entry("SS", "Sudan Południowy"),
            Map.entry("ES", "Hiszpania"), Map.entry("LK", "Sri Lanka"), Map.entry("SD", "Sudan"),
            Map.entry("SR", "Surinam"), Map.entry("SE", "Szwecja"), Map.entry("CH", "Szwajcaria"),
            Map.entry("SY", "Syria"), Map.entry("TJ", "Tadżykistan"), Map.entry("TZ", "Tanzania"),
            Map.entry("TH", "Tajlandia"), Map.entry("TL", "Timor Wschodni"), Map.entry("TG", "Togo"),
            Map.entry("TO", "Tonga"), Map.entry("TT", "Trynidad i Tobago"), Map.entry("TN", "Tunezja"),
            Map.entry("TR", "Turcja"), Map.entry("TM", "Turkmenistan"), Map.entry("TV", "Tuvalu"),
            Map.entry("UG", "Uganda"), Map.entry("UA", "Ukraina"), Map.entry("AE", "Zjednoczone Emiraty Arabskie"),
            Map.entry("GB", "Wielka Brytania"), Map.entry("US", "Stany Zjednoczone"), Map.entry("UY", "Urugwaj"),
            Map.entry("UZ", "Uzbekistan"), Map.entry("VU", "Vanuatu"), Map.entry("VA", "Watykan"),
            Map.entry("VE", "Wenezuela"), Map.entry("VN", "Wietnam"), Map.entry("YE", "Jemen"),
            Map.entry("ZM", "Zambia"), Map.entry("ZW", "Zimbabwe")
    );

    private PolishCountryWikiTitles() {
    }

    public static String get(String isoCode) {
        return BY_ISO.getOrDefault(isoCode, isoCode);
    }
}
