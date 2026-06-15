package com.proxypulse.app.data.region

/** Logical region used for filtering in the UI. */
enum class Region { ALL, EUROPE, AMERICA }

/**
 * Country-code membership for the two regions the app keeps.
 * Edit these sets to widen / narrow the geography.
 */
object Regions {

    // ISO 3166-1 alpha-2 codes for Europe.
    val EUROPE: Set<String> = setOf(
        "AL", "AD", "AT", "BY", "BE", "BA", "BG", "HR", "CY", "CZ",
        "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IS", "IE", "IT",
        "XK", "LV", "LI", "LT", "LU", "MT", "MD", "MC", "ME", "NL",
        "MK", "NO", "PL", "PT", "RO", "RU", "SM", "RS", "SK", "SI",
        "ES", "SE", "CH", "UA", "GB", "VA"
    )

    // ISO 3166-1 alpha-2 codes for the Americas (North, Central, South, Caribbean).
    val AMERICA: Set<String> = setOf(
        "US", "CA", "MX", "BR", "AR", "CL", "CO", "PE", "VE", "EC",
        "BO", "PY", "UY", "GY", "SR", "CR", "PA", "GT", "HN", "SV",
        "NI", "BZ", "CU", "DO", "HT", "JM", "TT", "BS", "BB", "PR"
    )

    /** All allowed codes — anything outside this set is discarded entirely. */
    val ALLOWED: Set<String> = EUROPE + AMERICA

    fun isAllowed(code: String): Boolean =
        code.uppercase() in ALLOWED

    fun regionOf(code: String): Region = when (code.uppercase()) {
        in EUROPE -> Region.EUROPE
        in AMERICA -> Region.AMERICA
        else -> Region.ALL
    }

    fun matches(region: Region, code: String): Boolean = when (region) {
        Region.ALL -> isAllowed(code)
        Region.EUROPE -> code.uppercase() in EUROPE
        Region.AMERICA -> code.uppercase() in AMERICA
    }

    /** Turn a country code into its flag emoji ("DE" -> 🇩🇪). */
    fun flagEmoji(code: String): String {
        val cc = code.uppercase()
        if (cc.length != 2 || cc.any { it !in 'A'..'Z' }) return "🏳️"
        val base = 0x1F1E6 - 'A'.code
        return buildString {
            appendCodePoint(base + cc[0].code)
            appendCodePoint(base + cc[1].code)
        }
    }
}
