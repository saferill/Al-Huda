package com.alhuda.app.main.quran.data

import com.alhuda.app.main.quran.model.JuzInfo
import com.alhuda.app.main.quran.model.Surah

object QuranDataSource {

    val surahs: List<Surah> = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "The Opening", "Pembukaan", 7, true, 1),
        Surah(2, "البقرة", "Al-Baqarah", "The Cow", "Sapi Betina", 286, false, 1),
        Surah(3, "آل عمران", "Ali 'Imran", "Family of Imran", "Keluarga Imran", 200, false, 3),
        Surah(4, "النساء", "An-Nisa'", "The Women", "Wanita", 176, false, 4),
        Surah(5, "المائدة", "Al-Ma'idah", "The Table Spread", "Hidangan", 120, false, 6),
        Surah(6, "الأنعام", "Al-An'am", "The Cattle", "Binatang Ternak", 165, true, 7),
        Surah(7, "الأعراف", "Al-A'raf", "The Heights", "Tempat Tertinggi", 206, true, 8),
        Surah(8, "الأنفال", "Al-Anfal", "The Spoils of War", "Rampasan Perang", 75, false, 9),
        Surah(9, "التوبة", "At-Tawbah", "The Repentance", "Pengampunan", 129, false, 10),
        Surah(10, "يونس", "Yunus", "Jonah", "Nabi Yunus", 109, true, 11),
        Surah(11, "هود", "Hud", "Hud", "Nabi Hud", 123, true, 11),
        Surah(12, "يوسف", "Yusuf", "Joseph", "Nabi Yusuf", 111, true, 12),
        Surah(13, "الرعد", "Ar-Ra'd", "The Thunder", "Guruh", 43, false, 13),
        Surah(14, "إبراهيم", "Ibrahim", "Abraham", "Nabi Ibrahim", 52, true, 13),
        Surah(15, "الحجر", "Al-Hijr", "The Rocky Tract", "Negeri Kaum Tsamud", 99, true, 14),
        Surah(16, "النحل", "An-Nahl", "The Bee", "Lebah", 128, true, 14),
        Surah(17, "الإسراء", "Al-Isra'", "The Night Journey", "Perjalanan Malam", 111, true, 15),
        Surah(18, "الكهف", "Al-Kahf", "The Cave", "Gua", 110, true, 15),
        Surah(19, "مريم", "Maryam", "Mary", "Maryam", 98, true, 16),
        Surah(20, "طه", "Taha", "Ta-Ha", "Thaha", 135, true, 16),
        Surah(21, "الأنبياء", "Al-Anbiya'", "The Prophets", "Para Nabi", 112, true, 17),
        Surah(22, "الحج", "Al-Hajj", "The Pilgrimage", "Haji", 78, false, 17),
        Surah(23, "المؤمنون", "Al-Mu'minun", "The Believers", "Orang-Orang Mukmin", 118, true, 18),
        Surah(24, "النور", "An-Nur", "The Light", "Cahaya", 64, false, 18),
        Surah(25, "الفرقان", "Al-Furqan", "The Criterion", "Pembeda", 77, true, 18),
        Surah(26, "الشعراء", "Ash-Shu'ara'", "The Poets", "Para Penyair", 227, true, 19),
        Surah(27, "النمل", "An-Naml", "The Ant", "Semut", 93, true, 19),
        Surah(28, "القصص", "Al-Qasas", "The Stories", "Kisah-Kisah", 88, true, 20),
        Surah(29, "العنكبوت", "Al-'Ankabut", "The Spider", "Laba-Laba", 69, true, 20),
        Surah(30, "الروم", "Ar-Rum", "The Romans", "Bangsa Romawi", 60, true, 21),
        Surah(31, "لقمان", "Luqman", "Luqman", "Luqman", 34, true, 21),
        Surah(32, "السجدة", "As-Sajdah", "The Prostration", "Sujud", 30, true, 21),
        Surah(33, "الأحزاب", "Al-Ahzab", "The Combined Forces", "Golongan yang Bersekutu", 73, false, 21),
        Surah(34, "سبأ", "Saba'", "Sheba", "Kaum Saba'", 54, true, 22),
        Surah(35, "فاطر", "Fatir", "Originator", "Pencipta", 45, true, 22),
        Surah(36, "يس", "Ya-Sin", "Ya Sin", "Yasin", 83, true, 22),
        Surah(37, "الصافات", "As-Saffat", "Those who set the Ranks", "Barisan-Barisan", 182, true, 23),
        Surah(38, "ص", "Sad", "The Letter Sad", "Shad", 88, true, 23),
        Surah(39, "الزمر", "Az-Zumar", "The Troops", "Rombongan", 75, true, 23),
        Surah(40, "غافر", "Ghafir", "The Forgiver", "Maha Pengampun", 85, true, 24),
        Surah(41, "فصلت", "Fussilat", "Explained in Detail", "Yang Dijelaskan", 54, true, 24),
        Surah(42, "الشورى", "Ash-Shura", "The Consultation", "Musyawarah", 53, true, 25),
        Surah(43, "الزخرف", "Az-Zukhruf", "The Ornaments of Gold", "Perhiasan", 89, true, 25),
        Surah(44, "الدخان", "Ad-Dukhan", "The Smoke", "Kabut", 59, true, 25),
        Surah(45, "الجاثية", "Al-Jathiyah", "The Crouching", "Yang Berlutut", 37, true, 25),
        Surah(46, "الأحقاف", "Al-Ahqaf", "The Wind-Curved Sandhills", "Bukit-Bukit Pasir", 35, true, 26),
        Surah(47, "محمد", "Muhammad", "Muhammad", "Nabi Muhammad", 38, false, 26),
        Surah(48, "الفتح", "Al-Fath", "The Victory", "Kemenangan", 29, false, 26),
        Surah(49, "الحجرات", "Al-Hujurat", "The Rooms", "Kamar-Kamar", 18, false, 26),
        Surah(50, "ق", "Qaf", "The Letter Qaf", "Qaf", 45, true, 26),
        Surah(51, "الذاريات", "Adh-Dhariyat", "The Winnowing Winds", "Angin yang Menerbangkan", 60, true, 26),
        Surah(52, "الطور", "At-Tur", "The Mount", "Bukit Tursina", 49, true, 27),
        Surah(53, "النجم", "An-Najm", "The Star", "Bintang", 62, true, 27),
        Surah(54, "القمر", "Al-Qamar", "The Moon", "Bulan", 55, true, 27),
        Surah(55, "الرحمن", "Ar-Rahman", "The Beneficent", "Maha Pengasih", 78, false, 27),
        Surah(56, "الواقعة", "Al-Waqi'ah", "The Inevitable", "Hari Kiamat", 96, true, 27),
        Surah(57, "الحديد", "Al-Hadid", "The Iron", "Besi", 29, false, 27),
        Surah(58, "المجادلة", "Al-Mujadila", "The Pleading Woman", "Gugatan", 22, false, 28),
        Surah(59, "الحشر", "Al-Hashr", "The Exile", "Pengusiran", 24, false, 28),
        Surah(60, "الممتحنة", "Al-Mumtahanah", "She that is to be examined", "Wanita yang Diuji", 13, false, 28),
        Surah(61, "الصف", "As-Saff", "The Ranks", "Barisan", 14, false, 28),
        Surah(62, "الجمعة", "Al-Jumu'ah", "The Congregation", "Hari Jum'at", 11, false, 28),
        Surah(63, "المنافقون", "Al-Munafiqun", "The Hypocrites", "Orang-Orang Munafik", 11, false, 28),
        Surah(64, "التغابن", "At-Taghabun", "The Mutual Disillusion", "Hari Ditampakkan Kesalahan", 18, false, 28),
        Surah(65, "الطلاق", "At-Talaq", "The Divorce", "Talak", 12, false, 28),
        Surah(66, "التحريم", "At-Tahrim", "The Prohibition", "Mengharamkan", 12, false, 28),
        Surah(67, "الملك", "Al-Mulk", "The Sovereignty", "Kerajaan", 30, true, 29),
        Surah(68, "القلم", "Al-Qalam", "The Pen", "Pena", 52, true, 29),
        Surah(69, "الحاقة", "Al-Haqqah", "The Reality", "Hari Kiamat", 52, true, 29),
        Surah(70, "المعارج", "Al-Ma'arij", "The Ascending Stairways", "Tempat Naik", 44, true, 29),
        Surah(71, "نوح", "Nuh", "Noah", "Nabi Nuh", 28, true, 29),
        Surah(72, "الجن", "Al-Jinn", "The Jinn", "Jin", 28, true, 29),
        Surah(73, "المزمل", "Al-Muzzammil", "The Enshrouded One", "Orang yang Berselimut", 20, true, 29),
        Surah(74, "المدثر", "Al-Muddaththir", "The Cloaked One", "Orang yang Berkemul", 56, true, 29),
        Surah(75, "القيامة", "Al-Qiyamah", "The Resurrection", "Hari Kiamat", 40, true, 29),
        Surah(76, "الإنسان", "Al-Insan", "Man", "Manusia", 31, false, 29),
        Surah(77, "المرسلات", "Al-Mursalat", "The Emissaries", "Malaikat yang Diutus", 50, true, 29),
        Surah(78, "النبأ", "An-Naba'", "The Tidings", "Berita Besar", 40, true, 30),
        Surah(79, "النازعات", "An-Nazi'at", "Those who drag forth", "Malaikat yang Mencabut", 46, true, 30),
        Surah(80, "عبس", "'Abasa", "He Frowned", "Bermuka Masam", 42, true, 30),
        Surah(81, "التكوير", "At-Takwir", "The Overthrowing", "Menggulung", 29, true, 30),
        Surah(82, "الانفطار", "Al-Infitar", "The Cleaving", "Terbelah", 19, true, 30),
        Surah(83, "المطففين", "Al-Mutaffifin", "The Defrauding", "Orang-Orang Curang", 36, true, 30),
        Surah(84, "الانشقاق", "Al-Inshiqaq", "The Splitting Open", "Terbelah", 25, true, 30),
        Surah(85, "البروج", "Al-Buruj", "The Mansions of the Stars", "Gugusan Bintang", 22, true, 30),
        Surah(86, "الطارق", "At-Tariq", "The Morning Star", "Yang Datang di Malam Hari", 17, true, 30),
        Surah(87, "الأعلى", "Al-A'la", "The Most High", "Maha Tinggi", 19, true, 30),
        Surah(88, "الغاشية", "Al-Ghashiyah", "The Overwhelming", "Hari Pembalasan", 26, true, 30),
        Surah(89, "الفجر", "Al-Fajr", "The Dawn", "Fajar", 30, true, 30),
        Surah(90, "البلد", "Al-Balad", "The City", "Negeri", 20, true, 30),
        Surah(91, "الشمس", "Ash-Shams", "The Sun", "Matahari", 15, true, 30),
        Surah(92, "الليل", "Al-Layl", "The Night", "Malam", 21, true, 30),
        Surah(93, "الضحى", "Ad-Duha", "The Morning Hours", "Waktu Dhuha", 11, true, 30),
        Surah(94, "الشرح", "Ash-Sharh", "The Relief", "Kelapangan", 8, true, 30),
        Surah(95, "التين", "At-Tin", "The Fig", "Buah Tin", 8, true, 30),
        Surah(96, "العلق", "Al-'Alaq", "The Clot", "Segumpal Darah", 19, true, 30),
        Surah(97, "القدر", "Al-Qadr", "The Power", "Kemuliaan", 5, true, 30),
        Surah(98, "البينة", "Al-Bayyinah", "The Clear Proof", "Bukti Nyata", 8, false, 30),
        Surah(99, "الزلزلة", "Az-Zalzalah", "The Earthquake", "Keguncangan", 8, false, 30),
        Surah(100, "العاديات", "Al-'Adiyat", "The Courser", "Kuda Perang yang Berlari Kencang", 11, true, 30),
        Surah(101, "القارعة", "Al-Qari'ah", "The Calamity", "Hari Kiamat", 11, true, 30),
        Surah(102, "التكاثر", "At-Takathur", "The Rivalry in world increase", "Bermegah-Megahan", 8, true, 30),
        Surah(103, "العصر", "Al-'Asr", "The Declining Day", "Masa / Waktu", 3, true, 30),
        Surah(104, "الهمزة", "Al-Humazah", "The Traducer", "Pengumpat", 9, true, 30),
        Surah(105, "الفيل", "Al-Fil", "The Elephant", "Gajah", 5, true, 30),
        Surah(106, "قريش", "Quraysh", "Quraysh", "Suku Quraisy", 4, true, 30),
        Surah(107, "الماعون", "Al-Ma'un", "The Small Kindnesses", "Barang yang Berguna", 7, true, 30),
        Surah(108, "الكوثر", "Al-Kawthar", "The Abundance", "Nikmat yang Berlimpah", 3, true, 30),
        Surah(109, "الكافرون", "Al-Kafirun", "The Disbelievers", "Orang-Orang Kafir", 6, true, 30),
        Surah(110, "النصر", "An-Nasr", "The Divine Support", "Pertolongan", 3, false, 30),
        Surah(111, "المسد", "Al-Masad", "The Palm Fiber", "Gejolak Api / Sabut", 5, true, 30),
        Surah(112, "الإخلاص", "Al-Ikhlas", "The Sincerity", "Kemurnian Keesaan Allah", 4, true, 30),
        Surah(113, "الفلق", "Al-Falaq", "The Daybreak", "Waktu Subuh", 5, true, 30),
        Surah(114, "الناس", "An-Nas", "Mankind", "Manusia", 6, true, 30),
    )

    val juzList: List<JuzInfo> = listOf(
        JuzInfo(1, "الجزء الأول", 1, "Al-Fatihah", 1),
        JuzInfo(2, "الجزء الثاني", 2, "Al-Baqarah", 142),
        JuzInfo(3, "الجزء الثالث", 2, "Al-Baqarah", 253),
        JuzInfo(4, "الجزء الرابع", 3, "Ali 'Imran", 93),
        JuzInfo(5, "الجزء الخامس", 4, "An-Nisa'", 24),
        JuzInfo(6, "الجزء السادس", 4, "An-Nisa'", 148),
        JuzInfo(7, "الجزء السابع", 5, "Al-Ma'idah", 82),
        JuzInfo(8, "الجزء الثامن", 6, "Al-An'am", 111),
        JuzInfo(9, "الجزء التاسع", 7, "Al-A'raf", 88),
        JuzInfo(10, "الجزء العاشر", 8, "Al-Anfal", 41),
        JuzInfo(11, "الجزء الحادي عشر", 9, "At-Tawbah", 93),
        JuzInfo(12, "الجزء الثاني عشر", 11, "Hud", 6),
        JuzInfo(13, "الجزء الثالث عشر", 12, "Yusuf", 53),
        JuzInfo(14, "الجزء الرابع عشر", 15, "Al-Hijr", 1),
        JuzInfo(15, "الجزء الخامس عشر", 17, "Al-Isra'", 1),
        JuzInfo(16, "الجزء السادس عشر", 18, "Al-Kahf", 75),
        JuzInfo(17, "الجزء السابع عشر", 21, "Al-Anbiya'", 1),
        JuzInfo(18, "الجزء الثامن عشر", 23, "Al-Mu'minun", 1),
        JuzInfo(19, "الجزء التاسع عشر", 25, "Al-Furqan", 21),
        JuzInfo(20, "الجزء العشرون", 27, "An-Naml", 56),
        JuzInfo(21, "الجزء الحادي والعشرون", 29, "Al-'Ankabut", 46),
        JuzInfo(22, "الجزء الثاني والعشرون", 33, "Al-Ahzab", 31),
        JuzInfo(23, "الجزء الثالث والعشرون", 36, "Ya-Sin", 28),
        JuzInfo(24, "الجزء الرابع والعشرون", 39, "Az-Zumar", 32),
        JuzInfo(25, "الجزء الخامس والعشرون", 41, "Fussilat", 47),
        JuzInfo(26, "الجزء السادس والعشرون", 46, "Al-Ahqaf", 1),
        JuzInfo(27, "الجزء السابع والعشرون", 51, "Adh-Dhariyat", 31),
        JuzInfo(28, "الجزء الثامن والعشرون", 58, "Al-Mujadila", 1),
        JuzInfo(29, "الجزء التاسع والعشرون", 67, "Al-Mulk", 1),
        JuzInfo(30, "الجزء الثلاثون", 78, "An-Naba'", 1),
    )

    val surahStartPages: IntArray = intArrayOf(
        1, 2, 50, 77, 106, 128, 151, 177, 187, 208, 221, 235, 249, 255, 262, 267, 282, 293, 305, 312,
        322, 332, 342, 350, 359, 367, 377, 385, 396, 404, 411, 415, 418, 428, 434, 440, 446, 453, 458, 467,
        477, 483, 489, 496, 499, 502, 507, 511, 515, 518, 520, 523, 526, 528, 531, 534, 537, 542, 545, 549,
        551, 553, 554, 556, 558, 560, 562, 564, 566, 568, 570, 572, 574, 575, 577, 578, 580, 582, 583, 585,
        586, 587, 587, 589, 590, 591, 591, 592, 593, 594, 595, 595, 596, 596, 597, 597, 598, 598, 599, 599,
        600, 600, 601, 601, 601, 602, 602, 602, 603, 603, 603, 604, 604, 604
    )

    val juzStartPages: IntArray = intArrayOf(
        1, 22, 42, 62, 82, 102, 121, 142, 162, 182,
        201, 222, 242, 262, 282, 302, 322, 342, 362, 382,
        402, 422, 442, 462, 482, 502, 522, 542, 562, 582
    )

    val juzEndPages: IntArray = intArrayOf(
        21, 41, 61, 81, 101, 120, 141, 161, 181, 200,
        221, 241, 261, 281, 301, 321, 341, 361, 381, 401,
        421, 441, 461, 481, 501, 521, 541, 561, 581, 604
    )

    val juzDescriptions: Array<String> = arrayOf(
        "Al-Fatihah 1 - Al-Baqarah 141",
        "Al-Baqarah 142 - Al-Baqarah 252",
        "Al-Baqarah 253 - Ali 'Imran 92",
        "Ali 'Imran 93 - An-Nisa' 23",
        "An-Nisa' 24 - An-Nisa' 147",
        "An-Nisa' 148 - Al-Ma'idah 81",
        "Al-Ma'idah 82 - Al-An'am 110",
        "Al-An'am 111 - Al-A'raf 87",
        "Al-A'raf 88 - Al-Anfal 40",
        "Al-Anfal 41 - At-Tawbah 92",
        "At-Tawbah 93 - Hud 5",
        "Hud 6 - Yusuf 52",
        "Yusuf 53 - Ibrahim 52",
        "Al-Hijr 1 - An-Nahl 128",
        "Al-Isra' 1 - Al-Kahf 74",
        "Al-Kahf 75 - Ta-Ha 135",
        "Al-Anbiya' 1 - Al-Hajj 78",
        "Al-Mu'minun 1 - Al-Furqan 20",
        "Al-Furqan 21 - An-Naml 55",
        "An-Naml 56 - Al-'Ankabut 45",
        "Al-'Ankabut 46 - Al-Ahzab 30",
        "Al-Ahzab 31 - Ya-Sin 27",
        "Ya-Sin 28 - Az-Zumar 31",
        "Az-Zumar 32 - Fussilat 46",
        "Fussilat 47 - Al-Jathiyah 37",
        "Al-Ahqaf 1 - Adh-Dhariyat 30",
        "Adh-Dhariyat 31 - Al-Hadid 29",
        "Al-Mujadila 1 - At-Tahrim 12",
        "Al-Mulk 1 - Al-Mursalat 50",
        "An-Naba' 1 - An-Nas 6"
    )

    fun getJuzDescription(juzNumber: Int): String {
        if (juzNumber in 1..30) {
            val start = juzStartPages[juzNumber - 1]
            val end = juzEndPages[juzNumber - 1]
            return "${juzDescriptions[juzNumber - 1]} • Hal. $start–$end"
        }
        return ""
    }

    fun getStartPageForSurah(surahNumber: Int): Int {
        if (surahNumber in 1..114) {
            return surahStartPages[surahNumber - 1]
        }
        return 1
    }

    fun getStartPageForJuz(juzNumber: Int): Int {
        if (juzNumber in 1..30) {
            return juzStartPages[juzNumber - 1]
        }
        return 1
    }

    fun getSurahForPage(page: Int): Surah {
        val clampedPage = page.coerceIn(1, 604)
        for (i in surahStartPages.indices.reversed()) {
            if (clampedPage >= surahStartPages[i]) {
                return surahs[i]
            }
        }
        return surahs[0]
    }

    fun getJuzForPage(page: Int): Int {
        val clampedPage = page.coerceIn(1, 604)
        for (i in juzStartPages.indices.reversed()) {
            if (clampedPage >= juzStartPages[i]) {
                return i + 1
            }
        }
        return 1
    }
}
