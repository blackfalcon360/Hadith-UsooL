package com.blackfalcon.hadithusul;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class MainActivity extends Activity {

    private static final int BG = Color.BLACK, CARD = 0xFF12171E, EDGE = 0xFF2B3440, GOLD = 0xFFE6BE6A, GREEN = 0xFF2ECC71,
            BLUE = 0xFF4FC3F7, AMBER = 0xFFFFB300, RED = 0xFFE57373, GRAY = 0xFF9AA4AF, TEXT = 0xFFECECEC;

    private SharedPreferences prefs;
    private Content content = new Content();
    private int lang, sizeIdx;
    private float scale = 1f;
    private int tab = 0, overlay = 0;

    private final Set<String> read = new HashSet<>(), marks = new HashSet<>();
    private String lastLesson = "";
    private boolean onlyMarks = false;
    private String lessonQuery = "", termQuery = "";
    private final Set<String> openTerms = new HashSet<>();

    // views
    private final TextView[] tabs = new TextView[5];
    private final View[] pages = new View[6];
    private LinearLayout lessonsBox, glossBox, checkBox, quizBox, readerBox;

    // reader
    private String curLesson = "";

    // checker
    private final List<String> wzPath = new ArrayList<>();

    // quiz
    private int quizStage = 0, quizIdx = 0, quizScore = 0;
    private String quizUnit = "all";
    private List<Content.Question> quizList = new ArrayList<>();
    private int[] perm = {0, 1, 2, 3};
    private boolean answered = false;
    private int picked = -1;

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
    private String t(String k) { return Lang.t(lang, k); }

    // ------------------------------------------------------------- lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("hadithusul", MODE_PRIVATE);
        lang = prefs.getInt("lang", Lang.UR);
        sizeIdx = prefs.getInt("size", 1);
        scale = sizeIdx == 0 ? 0.9f : (sizeIdx == 2 ? 1.2f : 1.0f);
        read.addAll(prefs.getStringSet("read", new HashSet<String>()));
        marks.addAll(prefs.getStringSet("marks", new HashSet<String>()));
        lastLesson = prefs.getString("last", "");
        tab = prefs.getInt("tab", 0);
        try {
            content.parseLessons(getAssets().open("content_lessons.txt"));
            content.parseGlossary(getAssets().open("content_glossary.txt"));
            content.parseQuiz(getAssets().open("content_quiz.txt"));
        } catch (Exception e) {
            Toast.makeText(this, "Could not load the course content", Toast.LENGTH_LONG).show();
        }
        wzPath.add(Wizard.START);
        buildUi();
    }

    @Override
    public void onBackPressed() {
        if (overlay != 0) { overlay = 0; showTab(); return; }
        if (quizStage != 0 && tab == 3) { quizStage = 0; renderQuiz(); return; }
        super.onBackPressed();
    }

    // ------------------------------------------------------------- helpers
    private GradientDrawable box(int fill, int edge, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setStroke(dp(1), edge);
        g.setCornerRadius(dp(radius));
        return g;
    }

    private TextView tv(String text, float size, int color) {
        TextView x = new TextView(this);
        x.setText(text);
        x.setTextSize(size);
        x.setTextColor(color);
        return x;
    }

    /** Urdu / Arabic paragraph: always right-to-left and right-aligned */
    private TextView rtl(String text, float size, int color) {
        TextView x = tv(text, size, color);
        x.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        x.setTextDirection(View.TEXT_DIRECTION_RTL);
        x.setGravity(Gravity.START);
        x.setLineSpacing(0, 1.4f);
        return x;
    }

    private LinearLayout.LayoutParams lp(int w, int h, float weight) { return new LinearLayout.LayoutParams(w, h, weight); }

    private LinearLayout.LayoutParams weight(int l, int r) {
        LinearLayout.LayoutParams p = lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        p.setMargins(l, 0, r, 0);
        return p;
    }

    private LinearLayout card(int edge) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(box(CARD, edge, 14));
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams l = lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        l.bottomMargin = dp(10);
        c.setLayoutParams(l);
        return c;
    }

    private TextView button(String text, int edge) {
        TextView b = tv(text, 15, Color.WHITE);
        b.setGravity(Gravity.CENTER);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setBackground(box(0xFF1B2430, edge, 12));
        b.setPadding(dp(10), dp(12), dp(10), dp(12));
        return b;
    }

    private TextView chip(String text, boolean on, View.OnClickListener l) {
        TextView c = tv(text, 14, on ? Color.WHITE : GRAY);
        c.setGravity(Gravity.CENTER);
        c.setTypeface(on ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        c.setBackground(box(on ? 0xFF1B2430 : CARD, on ? GOLD : EDGE, 10));
        c.setPadding(dp(12), dp(9), dp(12), dp(9));
        c.setOnClickListener(l);
        return c;
    }

    /** A text box whose typed text is BLACK (on a white box). */
    private EditText input(String hint, String text) {
        EditText e = new EditText(new ContextThemeWrapper(this, android.R.style.Theme_Material_Light));
        e.setSingleLine(true);
        e.setHint(hint);
        e.setText(text);
        e.setTextColor(Color.BLACK);
        e.setHintTextColor(0xFF777777);
        e.setTextSize(16);
        e.setBackground(box(Color.WHITE, 0xFFBBBBBB, 10));
        e.setPadding(dp(12), dp(10), dp(12), dp(10));
        return e;
    }

    private void save(String key, Set<String> s) { prefs.edit().putStringSet(key, new HashSet<String>(s)).apply(); }

    private String unitTitle(String id) {
        Content.Unit u = content.units.get(id);
        return u == null ? id : (lang == Lang.UR ? u.ur : u.en);
    }

    private String lessonTitle(Content.Lesson l) { return lang == Lang.UR ? l.ur : l.en; }

    // ------------------------------------------------------------- shell
    private void buildUi() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);
        root.setLayoutDirection(lang == Lang.UR ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(8), dp(6), dp(8), dp(30));

        LinearLayout tabRow = new LinearLayout(this);
        tabRow.setPadding(0, 0, 0, dp(8));
        String[] keys = {"tab_lessons", "tab_gloss", "tab_check", "tab_quiz", "tab_set"};
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            TextView x = tv(t(keys[i]), 13, Color.WHITE);
            x.setGravity(Gravity.CENTER);
            x.setPadding(0, dp(9), 0, dp(9));
            x.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    overlay = 0;
                    tab = idx;
                    prefs.edit().putInt("tab", tab).apply();
                    showTab();
                }
            });
            tabRow.addView(x, weight(dp(2), dp(2)));
            tabs[i] = x;
        }
        col.addView(tabRow);

        FrameLayout frame = new FrameLayout(this);
        pages[0] = scroll(lessonsBox = vbox());
        pages[1] = scroll(glossBox = vbox());
        pages[2] = scroll(checkBox = vbox());
        pages[3] = scroll(quizBox = vbox());
        pages[4] = buildSettings();
        pages[5] = scroll(readerBox = vbox());
        for (View v : pages) frame.addView(v);
        col.addView(frame, lp(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(col, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView credit = tv("By: Black Falcon \uD83E\uDD85", 12, GRAY);
        credit.setPadding(dp(8), dp(3), dp(8), dp(3));
        FrameLayout.LayoutParams cl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.RIGHT);
        cl.setMargins(0, 0, dp(8), dp(4));
        root.addView(credit, cl);

        setContentView(root);
        renderLessons();
        renderGlossary();
        renderChecker();
        renderQuiz();
        showTab();
    }

    private LinearLayout vbox() {
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setPadding(dp(2), dp(2), dp(2), dp(16));
        return b;
    }

    private ScrollView scroll(LinearLayout box) {
        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        sv.addView(box);
        return sv;
    }

    private void showTab() {
        for (int i = 0; i < 5; i++) {
            pages[i].setVisibility(overlay == 0 && i == tab ? View.VISIBLE : View.GONE);
            tabs[i].setBackground(box(i == tab ? 0xFF1B2430 : CARD, i == tab ? GOLD : EDGE, 10));
            tabs[i].setTypeface(i == tab ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        }
        pages[5].setVisibility(overlay == 5 ? View.VISIBLE : View.GONE);
        if (tab == 0 && overlay == 0) renderLessons();
    }

    // ------------------------------------------------------------- lessons list
    private void renderLessons() {
        if (lessonsBox == null) return;
        lessonsBox.removeAllViews();
        TextView title = tv(t("title"), 26, GOLD);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        lessonsBox.addView(title);
        TextView sub = tv(t("subtitle"), 13, GRAY);
        sub.setPadding(0, 0, 0, dp(10));
        lessonsBox.addView(sub);

        int total = content.lessons.size();
        LinearLayout pc = card(EDGE);
        pc.addView(tv(t("progress") + ":  " + read.size() + " / " + total + "  " + t("lessons_read"), 14, TEXT));
        LinearLayout bar = new LinearLayout(this);
        bar.setBackground(box(0xFF1B2430, EDGE, 6));
        View fill = new View(this);
        fill.setBackground(box(GREEN, GREEN, 6));
        float frac = total == 0 ? 0f : Math.min(1f, read.size() / (float) total);
        bar.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        bar.addView(fill, lp(0, dp(10), Math.max(0.001f, frac)));
        bar.addView(new View(this), lp(0, dp(10), Math.max(0.001f, 1f - frac)));
        LinearLayout.LayoutParams bp = lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(10), 0);
        bp.topMargin = dp(8);
        pc.addView(bar, bp);
        lessonsBox.addView(pc);

        final Content.Lesson last = content.lesson(lastLesson);
        if (last != null) {
            LinearLayout cc = card(GOLD);
            cc.addView(tv(t("continue"), 12, GRAY));
            TextView lt = tv(last.id + "  " + lessonTitle(last), 17, TEXT);
            lt.setTypeface(Typeface.DEFAULT_BOLD);
            cc.addView(lt);
            cc.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { openLesson(last.id); } });
            lessonsBox.addView(cc);
        }

        EditText q = input(t("search_lessons"), lessonQuery);
        q.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                String n = s.toString().trim();
                if (!n.equals(lessonQuery)) { lessonQuery = n; renderLessonList(); }
            }
        });
        LinearLayout.LayoutParams ql = lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        ql.bottomMargin = dp(8);
        lessonsBox.addView(q, ql);

        LinearLayout chips = new LinearLayout(this);
        chips.addView(chip(t("all"), !onlyMarks, new View.OnClickListener() { @Override public void onClick(View v) { onlyMarks = false; renderLessons(); } }), weight(0, dp(4)));
        chips.addView(chip(t("bookmarked"), onlyMarks, new View.OnClickListener() { @Override public void onClick(View v) { onlyMarks = true; renderLessons(); } }), weight(dp(4), 0));
        LinearLayout.LayoutParams cp = lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        cp.bottomMargin = dp(10);
        lessonsBox.addView(chips, cp);

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setTag("list");
        lessonsBox.addView(list);
        renderLessonList();
    }

    private void renderLessonList() {
        LinearLayout list = (LinearLayout) lessonsBox.findViewWithTag("list");
        if (list == null) return;
        list.removeAllViews();
        String q = lessonQuery.toLowerCase(Locale.getDefault());
        String lastUnit = "";
        int shown = 0;
        for (final Content.Lesson l : content.lessons) {
            if (onlyMarks && !marks.contains(l.id)) continue;
            if (q.length() > 0) {
                String hay = (l.ur + " " + l.en + " " + l.ar + " " + l.id).toLowerCase(Locale.getDefault());
                if (!hay.contains(q)) continue;
            }
            if (!l.unit.equals(lastUnit)) {
                lastUnit = l.unit;
                TextView uh = tv(t("unit") + " " + l.unit + ":  " + unitTitle(l.unit), 14, AMBER);
                uh.setTypeface(Typeface.DEFAULT_BOLD);
                uh.setPadding(dp(2), dp(10), 0, dp(6));
                list.addView(uh);
            }
            LinearLayout row = card(EDGE);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            TextView num = tv(l.id, 15, GOLD);
            num.setTypeface(Typeface.DEFAULT_BOLD);
            num.setPadding(0, 0, dp(10), 0);
            row.addView(num);
            LinearLayout mid = new LinearLayout(this);
            mid.setOrientation(LinearLayout.VERTICAL);
            TextView nm = tv(lessonTitle(l), 16, TEXT);
            nm.setTypeface(Typeface.DEFAULT_BOLD);
            mid.addView(nm);
            TextView ar = rtl(l.ar, 13, GRAY);
            mid.addView(ar);
            row.addView(mid, lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            String mark = (marks.contains(l.id) ? "★ " : "") + (read.contains(l.id) ? "✔" : "");
            TextView st = tv(mark, 16, read.contains(l.id) ? GREEN : AMBER);
            row.addView(st);
            row.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { openLesson(l.id); } });
            list.addView(row);
            shown++;
        }
        if (shown == 0) {
            TextView none = tv(t("no_match"), 14, GRAY);
            none.setPadding(dp(4), dp(10), 0, 0);
            list.addView(none);
        }
    }

    // ------------------------------------------------------------- lesson reader
    private void openLesson(String id) {
        final Content.Lesson l = content.lesson(id);
        if (l == null) return;
        curLesson = id;
        lastLesson = id;
        read.add(id);
        save("read", read);
        prefs.edit().putString("last", id).apply();
        overlay = 5;
        showTab();
        readerBox.removeAllViews();

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = tv("‹  " + t("back"), 16, BLUE);
        back.setPadding(dp(4), dp(8), dp(14), dp(8));
        back.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { overlay = 0; showTab(); } });
        top.addView(back, lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        final TextView star = tv(marks.contains(id) ? "★" : "☆", 26, AMBER);
        star.setPadding(dp(10), 0, dp(10), 0);
        star.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!marks.remove(curLesson)) marks.add(curLesson);
                save("marks", marks);
                star.setText(marks.contains(curLesson) ? "★" : "☆");
            }
        });
        top.addView(star);
        readerBox.addView(top);

        TextView num = tv(t("unit") + " " + l.unit + "  ·  " + unitTitle(l.unit), 12, GRAY);
        readerBox.addView(num);
        TextView title = rtl(l.ur, 24 * scale, GOLD);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        readerBox.addView(title);
        TextView en = tv(l.en, 14, GRAY);
        en.setPadding(0, 0, 0, dp(4));
        readerBox.addView(en);
        TextView ar = rtl(l.ar, 18 * scale, TEXT);
        ar.setPadding(0, 0, 0, dp(10));
        readerBox.addView(ar);

        if (lang == Lang.EN) addSummary(l);
        String prevType = "";
        for (Content.Block b : l.blocks) {
            String thisType = b.type;
            switch (b.type) {
                case "UR": {
                    TextView p = rtl(b.text, 17 * scale, TEXT);
                    p.setPadding(dp(2), dp(4), dp(2), dp(8));
                    readerBox.addView(p);
                    break;
                }
                case "AR": {
                    LinearLayout c = card(GOLD);
                    c.addView(rtl(b.text, 20 * scale, GOLD));
                    readerBox.addView(c);
                    break;
                }
                case "EX": {
                    LinearLayout c = card(GREEN);
                    TextView h = tv(t("example"), 12, GREEN);
                    h.setTypeface(Typeface.DEFAULT_BOLD);
                    c.addView(h);
                    c.addView(rtl(b.text, 16 * scale, TEXT));
                    readerBox.addView(c);
                    break;
                }
                case "SCH": {
                    LinearLayout c = card(BLUE);
                    if (!prevType.equals("SCH")) {
                        TextView h = tv(t("scholars"), 12, BLUE);
                        h.setTypeface(Typeface.DEFAULT_BOLD);
                        c.addView(h);
                    }
                    c.addView(rtl(b.text, 16 * scale, TEXT));
                    readerBox.addView(c);
                    break;
                }
                case "NOTE": {
                    LinearLayout c = card(AMBER);
                    TextView h = tv(t("note"), 12, AMBER);
                    h.setTypeface(Typeface.DEFAULT_BOLD);
                    c.addView(h);
                    c.addView(rtl(b.text, 15 * scale, TEXT));
                    readerBox.addView(c);
                    break;
                }
                default:
                    break;
            }
            prevType = thisType;
        }

        List<String> refs = l.of("REF");
        if (!refs.isEmpty()) {
            LinearLayout c = card(EDGE);
            TextView h = tv(t("refs"), 15, GOLD);
            h.setTypeface(Typeface.DEFAULT_BOLD);
            c.addView(h);
            for (String r : refs) {
                TextView rr = rtl("• " + r, 14 * scale, TEXT);
                rr.setPadding(0, dp(5), 0, 0);
                c.addView(rr);
            }
            TextView cn = rtl(t("cite_note"), 11, GRAY);
            cn.setPadding(0, dp(8), 0, 0);
            c.addView(cn);
            readerBox.addView(c);
        }
        if (lang == Lang.UR) addSummary(l);

        LinearLayout nav = new LinearLayout(this);
        int i = content.indexOf(id);
        final Content.Lesson prev = i > 0 ? content.lessons.get(i - 1) : null;
        final Content.Lesson next = i >= 0 && i < content.lessons.size() - 1 ? content.lessons.get(i + 1) : null;
        TextView pb = button(t("prev"), prev != null ? GOLD : EDGE), nb = button(t("next"), next != null ? GOLD : EDGE);
        if (prev != null) pb.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { openLesson(prev.id); } });
        if (next != null) nb.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { openLesson(next.id); } });
        nav.addView(pb, weight(0, dp(4)));
        nav.addView(nb, weight(dp(4), 0));
        LinearLayout.LayoutParams np = lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        np.bottomMargin = dp(8);
        readerBox.addView(nav, np);
        TextView cp = button(t("copy"), BLUE);
        cp.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { shareLesson(l); } });
        readerBox.addView(cp);
        ((ScrollView) pages[5]).scrollTo(0, 0);
    }

    private void addSummary(Content.Lesson l) {
        List<String> en = l.of("EN");
        if (en.isEmpty()) return;
        LinearLayout c = card(EDGE);
        TextView h = tv(t("summary_en"), 12, GRAY);
        h.setTypeface(Typeface.DEFAULT_BOLD);
        c.addView(h);
        TextView x = tv(en.get(0), 14 * scale, TEXT);
        x.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        x.setTextDirection(View.TEXT_DIRECTION_LTR);
        x.setGravity(Gravity.START);
        x.setLineSpacing(0, 1.25f);
        c.addView(x);
        readerBox.addView(c);
    }

    private void shareLesson(Content.Lesson l) {
        String text = l.plain();
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("lesson", text));
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(i, t("copy")));
    }

    // ------------------------------------------------------------- glossary
    private void renderGlossary() {
        glossBox.removeAllViews();
        TextView title = tv(t("tab_gloss"), 22, GOLD);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        glossBox.addView(title);
        TextView cnt = tv(content.terms.size() + " " + t("terms_n"), 12, GRAY);
        cnt.setPadding(0, 0, 0, dp(8));
        glossBox.addView(cnt);
        EditText q = input(t("search_terms"), termQuery);
        q.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                String n = s.toString().trim();
                if (!n.equals(termQuery)) { termQuery = n; renderTermList(); }
            }
        });
        LinearLayout.LayoutParams ql = lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        ql.bottomMargin = dp(8);
        glossBox.addView(q, ql);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setTag("terms");
        glossBox.addView(list);
        renderTermList();
    }

    private static String strip(String s) { return s.replaceAll("[\\u064B-\\u0652\\u0640]", ""); }

    private void renderTermList() {
        LinearLayout list = (LinearLayout) glossBox.findViewWithTag("terms");
        if (list == null) return;
        list.removeAllViews();
        String q = strip(termQuery).toLowerCase(Locale.getDefault());
        int shown = 0;
        for (final Content.Term tm : content.terms) {
            if (q.length() > 0) {
                String hay = (strip(tm.ar) + " " + tm.ur + " " + tm.en).toLowerCase(Locale.getDefault());
                if (!hay.contains(q)) continue;
            }
            shown++;
            final boolean open = openTerms.contains(tm.ar);
            final LinearLayout c = card(open ? GOLD : EDGE);
            TextView ar = rtl(tm.ar, 20, GOLD);
            ar.setTypeface(Typeface.DEFAULT_BOLD);
            c.addView(ar);
            if (open) {
                TextView ur = rtl(tm.ur, 16 * scale, TEXT);
                ur.setPadding(0, dp(4), 0, dp(4));
                c.addView(ur);
                TextView en = tv(tm.en, 13 * scale, GRAY);
                en.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                en.setTextDirection(View.TEXT_DIRECTION_LTR);
                en.setGravity(Gravity.START);
                c.addView(en);
                if (tm.lesson.length() > 0 && content.lesson(tm.lesson) != null) {
                    final String lid = tm.lesson;
                    TextView go = tv("› " + t("open_lesson") + " " + lid, 14, BLUE);
                    go.setPadding(0, dp(8), 0, 0);
                    go.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { openLesson(lid); } });
                    c.addView(go);
                }
            }
            c.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (!openTerms.remove(tm.ar)) openTerms.add(tm.ar);
                    renderTermList();
                }
            });
            list.addView(c);
        }
        if (shown == 0) {
            TextView none = tv(t("no_match"), 14, GRAY);
            none.setPadding(dp(4), dp(10), 0, 0);
            list.addView(none);
        }
    }

    // ------------------------------------------------------------- chain checker
    private void renderChecker() {
        checkBox.removeAllViews();
        TextView title = tv(t("check_title"), 22, GOLD);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        checkBox.addView(title);
        TextView note = rtl(t("check_note"), 12, GRAY);
        note.setPadding(0, dp(2), 0, dp(10));
        checkBox.addView(note);

        final Wizard.Node n = Wizard.get(wzPath.get(wzPath.size() - 1));
        if (n == null) return;
        if (!n.result) {
            LinearLayout c = card(GOLD);
            c.addView(rtl(n.ur, 18 * scale, TEXT));
            checkBox.addView(c);
            for (final Wizard.Opt o : n.opts) {
                TextView b = button(o.ur, EDGE);
                b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                b.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                b.setTextDirection(View.TEXT_DIRECTION_RTL);
                b.setTypeface(Typeface.DEFAULT);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { wzPath.add(o.next); renderChecker(); }
                });
                LinearLayout.LayoutParams bl = lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
                bl.bottomMargin = dp(8);
                checkBox.addView(b, bl);
            }
        } else {
            LinearLayout c = card(GREEN);
            TextView h = tv(t("result"), 12, GREEN);
            h.setTypeface(Typeface.DEFAULT_BOLD);
            c.addView(h);
            TextView ar = rtl(n.ar, 24 * scale, GOLD);
            ar.setTypeface(Typeface.DEFAULT_BOLD);
            c.addView(ar);
            c.addView(rtl(n.ur, 17 * scale, TEXT));
            TextView en = tv(n.en, 13, GRAY);
            en.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            en.setGravity(Gravity.START);
            en.setPadding(0, dp(2), 0, 0);
            c.addView(en);
            checkBox.addView(c);
            LinearLayout e = card(EDGE);
            TextView eh = tv(t("explain"), 12, AMBER);
            eh.setTypeface(Typeface.DEFAULT_BOLD);
            e.addView(eh);
            e.addView(rtl(n.expl, 15 * scale, TEXT));
            TextView sh = tv(t("source"), 12, GRAY);
            sh.setPadding(0, dp(8), 0, 0);
            e.addView(sh);
            e.addView(rtl(n.ref, 13 * scale, GRAY));
            checkBox.addView(e);
        }
        LinearLayout row = new LinearLayout(this);
        TextView back = button(t("step_back"), EDGE), restart = button(t("restart"), BLUE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { if (wzPath.size() > 1) { wzPath.remove(wzPath.size() - 1); renderChecker(); } }
        });
        restart.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { wzPath.clear(); wzPath.add(Wizard.START); renderChecker(); }
        });
        row.addView(back, weight(0, dp(4)));
        row.addView(restart, weight(dp(4), 0));
        checkBox.addView(row);
    }

    // ------------------------------------------------------------- quiz
    private AlertDialog.Builder dialog() {
        return new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Material_Light_Dialog_Alert));
    }

    private void startQuiz() {
        List<Content.Question> all = new ArrayList<>();
        for (Content.Question q : content.questions) if (quizUnit.equals("all") || q.unit.equals(quizUnit)) all.add(q);
        if (all.isEmpty()) return;
        Collections.shuffle(all, new Random());
        quizList = all;
        quizIdx = 0;
        quizScore = 0;
        quizStage = 1;
        newPerm();
        renderQuiz();
    }

    private void newPerm() {
        List<Integer> p = new ArrayList<>();
        for (int i = 0; i < 4; i++) p.add(i);
        Collections.shuffle(p, new Random());
        for (int i = 0; i < 4; i++) perm[i] = p.get(i);
        answered = false;
        picked = -1;
    }

    private void renderQuiz() {
        quizBox.removeAllViews();
        TextView title = tv(t("tab_quiz"), 22, GOLD);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        quizBox.addView(title);
        if (quizStage == 0) {
            TextView h = tv(t("quiz_pick"), 13, GRAY);
            h.setPadding(0, 0, 0, dp(8));
            quizBox.addView(h);
            addQuizChoice("all", t("quiz_all") + "  (" + content.questions.size() + ")");
            for (String uid : content.units.keySet()) {
                int n = 0;
                for (Content.Question q : content.questions) if (q.unit.equals(uid)) n++;
                if (n > 0) addQuizChoice(uid, uid + ".  " + unitTitle(uid) + "  (" + n + ")");
            }
            TextView start = button(t("quiz_start"), GREEN);
            start.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { startQuiz(); } });
            LinearLayout.LayoutParams sl = lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
            sl.topMargin = dp(6);
            quizBox.addView(start, sl);
            return;
        }
        if (quizStage == 2) {
            int n = quizList.size();
            int pct = Math.round(100f * quizScore / n);
            String key = "best_" + quizUnit;
            int best = prefs.getInt(key, -1);
            if (pct > best) { prefs.edit().putInt(key, pct).apply(); best = pct; }
            LinearLayout c = card(GOLD);
            c.addView(tv(t("quiz_result"), 13, GRAY));
            TextView sc = tv(quizScore + " / " + n + "   (" + pct + "%)", 34, GOLD);
            sc.setTypeface(Typeface.DEFAULT_BOLD);
            c.addView(sc);
            c.addView(rtl(t(pct >= 80 ? "great" : (pct >= 50 ? "good" : "again")), 16, TEXT));
            TextView bs = tv(t("quiz_best") + ": " + best + "%", 13, GRAY);
            bs.setPadding(0, dp(6), 0, 0);
            c.addView(bs);
            quizBox.addView(c);
            TextView again = button(t("try_again"), GREEN), menu = button(t("menu"), BLUE);
            again.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { startQuiz(); } });
            menu.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { quizStage = 0; renderQuiz(); } });
            LinearLayout row = new LinearLayout(this);
            row.addView(again, weight(0, dp(4)));
            row.addView(menu, weight(dp(4), 0));
            quizBox.addView(row);
            return;
        }
        final Content.Question q = quizList.get(quizIdx);
        TextView head = tv(t("quiz_q") + " " + (quizIdx + 1) + " / " + quizList.size() + "     " + t("quiz_score") + ": " + quizScore, 13, GRAY);
        head.setPadding(0, 0, 0, dp(8));
        quizBox.addView(head);
        LinearLayout qc = card(GOLD);
        qc.addView(rtl(q.q, 18 * scale, TEXT));
        quizBox.addView(qc);
        for (int pos = 0; pos < 4; pos++) {
            final int orig = perm[pos];
            TextView b = button(q.opt[orig], EDGE);
            b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            b.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            b.setTextDirection(View.TEXT_DIRECTION_RTL);
            b.setTypeface(Typeface.DEFAULT);
            if (answered) {
                if (orig == q.correct) b.setBackground(box(0xFF10281C, GREEN, 12));
                else if (orig == picked) b.setBackground(box(0xFF2A1416, RED, 12));
            } else {
                b.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        answered = true;
                        picked = orig;
                        if (orig == q.correct) quizScore++;
                        renderQuiz();
                    }
                });
            }
            LinearLayout.LayoutParams bl = lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
            bl.bottomMargin = dp(8);
            quizBox.addView(b, bl);
        }
        if (answered) {
            LinearLayout e = card(picked == q.correct ? GREEN : RED);
            TextView v = tv(t(picked == q.correct ? "correct" : "wrong"), 15, picked == q.correct ? GREEN : RED);
            v.setTypeface(Typeface.DEFAULT_BOLD);
            e.addView(v);
            e.addView(rtl(q.expl, 15 * scale, TEXT));
            quizBox.addView(e);
            boolean lastQ = quizIdx >= quizList.size() - 1;
            TextView next = button(t(lastQ ? "quiz_finish" : "quiz_next"), GOLD);
            next.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (quizIdx >= quizList.size() - 1) quizStage = 2;
                    else { quizIdx++; newPerm(); }
                    renderQuiz();
                }
            });
            quizBox.addView(next);
        }
    }

    private void addQuizChoice(final String uid, String label) {
        boolean on = quizUnit.equals(uid);
        int best = prefs.getInt("best_" + uid, -1);
        TextView c = chip(label + (best >= 0 ? "   ·   " + t("quiz_best") + " " + best + "%" : ""), on, new View.OnClickListener() {
            @Override public void onClick(View v) { quizUnit = uid; renderQuiz(); }
        });
        c.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams cl = lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        cl.bottomMargin = dp(6);
        quizBox.addView(c, cl);
    }

    // ------------------------------------------------------------- settings
    private View buildSettings() {
        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        LinearLayout box = vbox();
        sv.addView(box);

        TextView title = tv(t("tab_set"), 22, GOLD);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        box.addView(title);

        box.addView(label(t("set_lang")));
        LinearLayout lr = new LinearLayout(this);
        lr.addView(chip("اردو", lang == Lang.UR, new View.OnClickListener() { @Override public void onClick(View v) { prefs.edit().putInt("lang", Lang.UR).apply(); recreate(); } }), weight(0, dp(4)));
        lr.addView(chip("English", lang == Lang.EN, new View.OnClickListener() { @Override public void onClick(View v) { prefs.edit().putInt("lang", Lang.EN).apply(); recreate(); } }), weight(dp(4), 0));
        box.addView(lr);

        box.addView(label(t("set_size")));
        LinearLayout sr = new LinearLayout(this);
        String[] sk = {"size_s", "size_m", "size_l"};
        for (int i = 0; i < 3; i++) {
            final int si = i;
            sr.addView(chip(t(sk[i]), sizeIdx == i, new View.OnClickListener() { @Override public void onClick(View v) { prefs.edit().putInt("size", si).apply(); recreate(); } }), weight(i == 0 ? 0 : dp(3), i == 2 ? 0 : dp(3)));
        }
        box.addView(sr);

        TextView reset = button(t("reset"), RED);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialog().setMessage(t("reset_q"))
                        .setPositiveButton(t("yes"), new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface d, int w) {
                                prefs.edit().clear().apply();
                                recreate();
                            }
                        }).setNegativeButton(t("cancel"), null).show();
            }
        });
        LinearLayout.LayoutParams rp = lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        rp.topMargin = dp(14);
        box.addView(reset, rp);

        LinearLayout src = card(GOLD);
        src.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        TextView st = tv(t("sources_title"), 16, GOLD);
        st.setTypeface(Typeface.DEFAULT_BOLD);
        st.setPadding(0, 0, 0, dp(6));
        src.addView(st);
        TextView c1 = tv(t("src_classic"), 13, AMBER);
        c1.setTypeface(Typeface.DEFAULT_BOLD);
        src.addView(c1);
        src.addView(tv(t("src_classic_list"), 13, TEXT));
        TextView c2 = tv(t("src_modern"), 13, AMBER);
        c2.setTypeface(Typeface.DEFAULT_BOLD);
        c2.setPadding(0, dp(8), 0, 0);
        src.addView(c2);
        src.addView(tv(t("src_modern_list"), 13, TEXT));
        TextView cn = tv(t("cite_note"), 11, GRAY);
        cn.setPadding(0, dp(8), 0, 0);
        src.addView(cn);
        LinearLayout.LayoutParams sp = (LinearLayout.LayoutParams) src.getLayoutParams();
        sp.topMargin = dp(14);
        box.addView(src);

        LinearLayout ab = card(EDGE);
        TextView ah = tv(t("about_title"), 14, AMBER);
        ah.setTypeface(Typeface.DEFAULT_BOLD);
        ab.addView(ah);
        ab.addView(rtl(t("about"), 13, TEXT));
        TextView en = tv(t("en_note"), 12, GRAY);
        en.setPadding(0, dp(6), 0, 0);
        ab.addView(en);
        box.addView(ab);
        TextView credit = tv("Brought to you By: Black Falcon \uD83E\uDD85", 13, GRAY);
        credit.setPadding(dp(4), dp(4), 0, 0);
        box.addView(credit);
        return sv;
    }

    private TextView label(String s) {
        TextView x = tv(s, 14, AMBER);
        x.setTypeface(Typeface.DEFAULT_BOLD);
        x.setPadding(dp(2), dp(14), 0, dp(6));
        return x;
    }
}
