package com.blackfalcon.hadithusul;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The course material, read from plain text files in the app's assets (see content_*.txt). */
public final class Content {

    public static final class Unit {
        public String id, ur, en;
    }

    public static final class Block {
        public final String type, text;
        Block(String t, String x) { type = t; text = x; }
    }

    public static final class Lesson {
        public String id, unit, ur, en, ar;
        public final List<Block> blocks = new ArrayList<>();

        public List<String> of(String type) {
            List<String> out = new ArrayList<>();
            for (Block b : blocks) if (b.type.equals(type)) out.add(b.text);
            return out;
        }

        /** plain text of the whole lesson (for copy / share) */
        public String plain() {
            StringBuilder sb = new StringBuilder();
            sb.append(ur).append("  |  ").append(ar).append("\n\n");
            for (Block b : blocks) {
                if (b.type.equals("REF") || b.type.equals("EN")) continue;
                sb.append(b.text).append("\n\n");
            }
            sb.append("حوالہ جات:\n");
            for (String r : of("REF")) sb.append("• ").append(r).append("\n");
            for (String e : of("EN")) sb.append("\n").append(e).append("\n");
            return sb.toString();
        }
    }

    public static final class Term {
        public String ar, ur, en, lesson;
    }

    public static final class Question {
        public String unit, q;
        public final String[] opt = new String[4];
        public int correct;
        public String expl;
    }

    public final Map<String, Unit> units = new LinkedHashMap<>();
    public final List<Lesson> lessons = new ArrayList<>();
    public final List<Term> terms = new ArrayList<>();
    public final List<Question> questions = new ArrayList<>();

    private static List<String> lines(InputStream in) throws IOException {
        List<String> out = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String l;
        boolean first = true;
        while ((l = br.readLine()) != null) {
            if (first && l.length() > 0 && l.charAt(0) == '\uFEFF') l = l.substring(1);
            first = false;
            l = l.trim();
            if (l.length() == 0 || l.charAt(0) == '#') continue;
            out.add(l);
        }
        br.close();
        return out;
    }

    public void parseLessons(InputStream in) throws IOException {
        Lesson cur = null;
        for (String l : lines(in)) {
            String[] f = l.split("\\|", -1);
            String k = f[0];
            if (k.equals("UNIT") && f.length >= 4) {
                Unit u = new Unit();
                u.id = f[1]; u.ur = f[2]; u.en = f[3];
                units.put(u.id, u);
            } else if (k.equals("LESSON") && f.length >= 6) {
                cur = new Lesson();
                cur.id = f[1]; cur.unit = f[2]; cur.ur = f[3]; cur.en = f[4]; cur.ar = f[5];
                lessons.add(cur);
            } else if (k.equals("END")) {
                cur = null;
            } else if (cur != null && f.length >= 2 && (k.equals("UR") || k.equals("AR") || k.equals("EX") || k.equals("SCH") || k.equals("NOTE") || k.equals("REF") || k.equals("EN"))) {
                cur.blocks.add(new Block(k, l.substring(k.length() + 1)));
            }
        }
    }

    public void parseGlossary(InputStream in) throws IOException {
        for (String l : lines(in)) {
            String[] f = l.split("\\|", -1);
            if (f[0].equals("TERM") && f.length >= 4) {
                Term t = new Term();
                t.ar = f[1]; t.ur = f[2]; t.en = f[3]; t.lesson = f.length > 4 ? f[4] : "";
                terms.add(t);
            }
        }
    }

    public void parseQuiz(InputStream in) throws IOException {
        for (String l : lines(in)) {
            String[] f = l.split("\\|", -1);
            if (f[0].equals("Q") && f.length >= 9) {
                Question q = new Question();
                q.unit = f[1]; q.q = f[2];
                for (int i = 0; i < 4; i++) q.opt[i] = f[3 + i];
                try { q.correct = Integer.parseInt(f[7].trim()); } catch (NumberFormatException e) { continue; }
                q.expl = f[8];
                questions.add(q);
            }
        }
    }

    public Lesson lesson(String id) {
        for (Lesson l : lessons) if (l.id.equals(id)) return l;
        return null;
    }

    public int indexOf(String id) {
        for (int i = 0; i < lessons.size(); i++) if (lessons.get(i).id.equals(id)) return i;
        return -1;
    }
}
