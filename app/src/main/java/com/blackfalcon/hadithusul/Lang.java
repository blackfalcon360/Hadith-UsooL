package com.blackfalcon.hadithusul;

import java.util.HashMap;
import java.util.Map;

/** Interface texts in Urdu (default) and English. The lessons themselves are written in Urdu with Arabic quotations. */
public final class Lang {
    private Lang() { }

    public static final int UR = 0, EN = 1;
    private static final Map<String, String[]> T = new HashMap<>();

    private static void a(String k, String ur, String en) { T.put(k, new String[]{ur, en}); }

    static {
        a("title", "اصولِ حدیث", "Usul al-Hadith");
        a("subtitle", "علمِ مصطلح الحدیث کا باقاعدہ کورس", "A structured course in the terminology and methodology of hadith");
        a("tab_lessons", "دروس", "Lessons");
        a("tab_gloss", "اصطلاحات", "Glossary");
        a("tab_check", "جانچ", "Checker");
        a("tab_quiz", "امتحان", "Quiz");
        a("tab_set", "ترتیبات", "Settings");
        a("progress", "پیش رفت", "Progress");
        a("lessons_read", "سبق پڑھے گئے", "lessons read");
        a("continue", "جہاں چھوڑا تھا", "Continue where you stopped");
        a("search_lessons", "دروس میں تلاش", "Search lessons");
        a("search_terms", "اصطلاح تلاش کریں (عربی، اردو یا English)", "Search terms (Arabic, Urdu or English)");
        a("all", "سب", "All");
        a("bookmarked", "★ نشان زد", "★ Bookmarked");
        a("unit", "باب", "Unit");
        a("back", "واپس", "Back");
        a("prev", "‹ پچھلا سبق", "‹ Previous");
        a("next", "اگلا سبق ›", "Next ›");
        a("copy", "کاپی / شیئر", "Copy / share");
        a("refs", "📚 حوالہ جات", "📚 References");
        a("scholars", "علماء کا موقف", "Scholars' positions");
        a("note", "نوٹ", "Note");
        a("example", "مثال", "Example");
        a("summary_en", "English summary", "Summary");
        a("open_lesson", "سبق دیکھیں", "Open lesson");
        a("terms_n", "اصطلاحات", "terms");
        a("no_match", "کوئی نتیجہ نہیں", "No results");
        a("check_title", "سند کی جانچ کا رہنما", "Chain checker");
        a("check_note", "یہ تعلیمی مشق ہے جو سکھاتی ہے کہ محدثین کن سوالات سے گزر کر حکم لگاتے ہیں۔ کسی حدیث کا حتمی حکم ماہرینِ فن کی تحقیق سے معلوم کریں۔", "A learning exercise showing the questions a muhaddith works through. For the ruling on any real hadith, refer to the verdicts of qualified specialists.");
        a("restart", "دوبارہ شروع کریں", "Restart");
        a("step_back", "ایک قدم پیچھے", "Step back");
        a("result", "نتیجہ", "Result");
        a("explain", "وضاحت", "Explanation");
        a("source", "ماخذ", "Source");
        a("quiz_pick", "امتحان کا دائرہ چنیں", "Choose the quiz scope");
        a("quiz_all", "تمام ابواب", "All units");
        a("quiz_start", "امتحان شروع کریں", "Start quiz");
        a("quiz_q", "سوال", "Question");
        a("quiz_score", "اسکور", "Score");
        a("quiz_next", "اگلا سوال", "Next question");
        a("quiz_finish", "نتیجہ دیکھیں", "See result");
        a("quiz_result", "آپ کا نتیجہ", "Your result");
        a("quiz_best", "بہترین", "Best");
        a("correct", "✔ درست", "✔ Correct");
        a("wrong", "✘ غلط", "✘ Wrong");
        a("try_again", "دوبارہ کوشش کریں", "Try again");
        a("menu", "مینو", "Menu");
        a("great", "ماشاء اللہ! بہت خوب", "Excellent!");
        a("good", "اچھی کوشش، کچھ دروس دوبارہ دیکھ لیں", "Good effort; review a few lessons");
        a("again", "دروس دوبارہ پڑھ کر پھر کوشش کریں", "Re-read the lessons and try again");
        a("set_lang", "زبان", "Language");
        a("set_size", "تحریر کا سائز", "Text size");
        a("size_s", "چھوٹا", "Small");
        a("size_m", "درمیانہ", "Medium");
        a("size_l", "بڑا", "Large");
        a("reset", "پیش رفت صفر کریں", "Reset progress");
        a("reset_q", "کیا تمام پیش رفت، نشانات اور امتحان کے نتائج صاف کریں؟", "Clear all progress, bookmarks and quiz results?");
        a("yes", "ہاں", "Yes");
        a("cancel", "منسوخ", "Cancel");
        a("sources_title", "ماخذ اور مطالعہ کی فہرست", "Sources and reading list");
        a("src_classic", "کلاسیکی کتب", "Classical works");
        a("src_modern", "معاصر کتب", "Contemporary works");
        a("src_classic_list",
                "• ابن الصلاح، معرفة أنواع علوم الحديث (المقدمة)\n• النووي، التقريب والتيسير؛ السيوطي، تدريب الراوي\n• ابن حجر، نزهة النظر في توضيح نخبة الفكر؛ النكت على ابن الصلاح؛ هدي الساري؛ تقريب التهذيب\n• العراقي، ألفية الحديث؛ السخاوي، فتح المغيث\n• الخطيب البغدادي، الكفاية في علم الرواية\n• الذهبي، الموقظة في علم مصطلح الحديث\n• ابن رجب، شرح علل الترمذي\n• الشافعي، الرسالة؛ اختلاف الحديث\n• ابن تيمية، مجموع الفتاوى (علم الحديث)؛ قاعدة جليلة في التوسل والوسيلة\n• ولي الله الدهلوي، حجة الله البالغة",
                "• Ibn al-Salah, Ma'rifat Anwa' 'Ulum al-Hadith (al-Muqaddima)\n• al-Nawawi, al-Taqrib; al-Suyuti, Tadrib al-Rawi\n• Ibn Hajar, Nuzhat al-Nazar; al-Nukat 'ala Ibn al-Salah; Hady al-Sari; Taqrib al-Tahdhib\n• al-'Iraqi, Alfiyyat al-Hadith; al-Sakhawi, Fath al-Mughith\n• al-Khatib al-Baghdadi, al-Kifaya\n• al-Dhahabi, al-Mawqiza\n• Ibn Rajab, Sharh 'Ilal al-Tirmidhi\n• al-Shafi'i, al-Risala; Ikhtilaf al-Hadith\n• Ibn Taymiyyah, Majmu' al-Fatawa ('Ilm al-Hadith); Qa'ida Jalila fi al-Tawassul wa-l-Wasila\n• Shah Waliullah al-Dihlawi, Hujjat Allah al-Balighah");
        a("src_modern_list",
                "• محمود الطحان، تيسير مصطلح الحديث؛ أصول التخريج ودراسة الأسانيد\n• نور الدين عتر، منهج النقد في علوم الحديث\n• أحمد محمد شاكر، الباعث الحثيث شرح اختصار علوم الحديث\n• صبحي الصالح، علوم الحديث ومصطلحه\n• مصطفى السباعي، السنة ومكانتها في التشريع الإسلامي\n• ابن عثيمين، مصطلح الحديث (ابتدائی مطالعے کے لیے)\n• ناصر الدين الألباني، الحديث حجة بنفسه في العقائد والأحكام\n• ظفر أحمد العثماني، قواعد في علوم الحديث (بتحقيق عبد الفتاح أبو غدة)\n• عبد الحي اللكنوي، الرفع والتكميل في الجرح والتعديل (بتحقيق أبو غدة)\n• Muhammad Mustafa Azami, Studies in Hadith Methodology and Literature; Studies in Early Hadith Literature",
                "• Mahmud al-Tahhan, Taysir Mustalah al-Hadith; Usul al-Takhrij\n• Nur al-Din 'Itr, Manhaj al-Naqd fi 'Ulum al-Hadith\n• Ahmad Shakir, al-Ba'ith al-Hathith\n• Subhi al-Salih, 'Ulum al-Hadith wa Mustalahuhu\n• Mustafa al-Siba'i, al-Sunna wa Makanatuha\n• Ibn 'Uthaymin, Mustalah al-Hadith (beginner's text)\n• al-Albani, al-Hadith Hujja bi-Nafsihi fi al-'Aqa'id wa-l-Ahkam\n• Zafar Ahmad al-'Uthmani, Qawa'id fi 'Ulum al-Hadith (ed. Abd al-Fattah Abu Ghudda)\n• Abd al-Hayy al-Laknawi, al-Raf' wa-l-Takmil (ed. Abu Ghudda)\n• M. M. Azami, Studies in Hadith Methodology and Literature; Studies in Early Hadith Literature");
        a("cite_note", "حوالے کتاب اور باب/نوع کے نام کے ساتھ دیے گئے ہیں۔ صفحات اور جلد کے نمبر طباعتوں کے لحاظ سے بدل جاتے ہیں، اس لیے اپنے نسخے سے ملا لیں۔ جہاں علماء کا اختلاف ہے وہاں صاحبِ موقف کا نام دیا گیا ہے۔ شیخ الاسلام ابن تیمیہ رحمہ اللہ کے اقوال ان کی کتب سے مفہوماً دیے گئے ہیں؛ ابن عثیمین رحمہ اللہ کی «مصطلح الحدیث» ابتدائی مطالعے کی فہرست میں ہے۔",
                "References give the book and chapter/type; page and volume numbers vary between editions, so check them against your copy. Where scholars differ, each position is attributed by name. Ibn Taymiyyah's views are given in meaning from his works; Ibn 'Uthaymin's Mustalah al-Hadith is listed for beginners.");
        a("about_title", "اس ایپ کے بارے میں", "About this app");
        a("about", "یہ ایپ علمِ اصولِ حدیث (مصطلح الحدیث) کی تعلیم کے لیے ہے: تعریفات، اقسام، شرائط اور علماء کے مواقف، حوالوں کے ساتھ۔ یہ فتویٰ یا کسی خاص حدیث کی تصحیح و تضعیف کا ذریعہ نہیں۔ دینی معاملات میں مستند اہلِ علم سے رجوع کریں۔ ایپ مکمل طور پر آف لائن چلتی ہے۔",
                "This app teaches the principles of hadith (mustalah al-hadith): definitions, categories, conditions and the positions of scholars, with references. It is not a fatwa and does not authenticate or weaken any particular hadith; consult qualified scholars for religious rulings. It works fully offline.");
        a("en_note", "انگریزی خلاصے ہر سبق کے آخر میں ہیں؛ تفصیلی وضاحت اردو میں ہے۔", "English summaries appear at the end of each lesson (at the top in English mode); the detailed explanation is in Urdu.");
    }

    public static String t(int lang, String key) {
        String[] v = T.get(key);
        return v == null ? key : v[lang];
    }
}
