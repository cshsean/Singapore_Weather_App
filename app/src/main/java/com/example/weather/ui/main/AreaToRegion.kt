package com.example.weather.ui.main

fun getRegionFromArea(areaName: String): String? {
    val areaToRegion = mapOf(
        // North Region (includes former North-East)
        "Sembawang" to "north", "Woodlands" to "north", "Yishun" to "north",
        "Mandai" to "north", "Simpang" to "north", "Sungei Kadut" to "north",
        "Lim Chu Kang" to "north", "Ang Mo Kio" to "north", "Hougang" to "north",
        "Punggol" to "north", "Sengkang" to "north", "Serangoon" to "north",
        "Seletar" to "north", "North-Eastern Islands" to "north",

        // East Region
        "Bedok" to "east", "Changi" to "east", "Changi Bay" to "east",
        "Pasir Ris" to "east", "Paya Lebar" to "east", "Tampines" to "east",

        // Central Region
        "Bishan" to "central", "Bukit Merah" to "central", "Bukit Timah" to "central",
        "Downtown Core" to "central", "Geylang" to "central", "Kallang" to "central",
        "Marine Parade" to "central", "Museum" to "central", "Newton" to "central",
        "Novena" to "central", "Orchard" to "central", "Outram" to "central",
        "Queenstown" to "central", "River Valley" to "central", "Rochor" to "central",
        "Singapore River" to "central", "Southern Islands" to "central",
        "Straits View" to "central", "Tanglin" to "central", "Toa Payoh" to "central",
        "Marina East" to "central", "Marina South" to "central",

        // West Region
        "Boon Lay" to "west", "Bukit Batok" to "west", "Bukit Panjang" to "west",
        "Clementi" to "west", "Jurong East" to "west", "Jurong West" to "west",
        "Pioneer" to "west", "Tengah" to "west", "Tuas" to "west",
        "Western Islands" to "west", "Western Water Catchment" to "west"
    )

    return areaToRegion[areaName]
}

