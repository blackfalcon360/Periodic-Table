package com.blackfalcon.periodictable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {

    static final int ACCENT = 0xFF1E88E5;

    Store st;
    int cBg, cCard, cText, cSub;
    TextView[] tabViews;
    View[] panes;

    // table tab
    TableView table;
    EditText etSearch;
    LinearLayout legendBox;
    TextView[] modeChips;
    int mode = 0, hlCat = -1;

    // list tab
    ListView lv;
    BaseAdapter adapter;
    EditText etList;
    final ArrayList<Integer> shown = new ArrayList<Integer>();
    boolean sortByName = false;
    TextView[] sortChips;

    // quiz tab
    final Random rnd = new Random();
    final Handler h = new Handler(Looper.getMainLooper());
    TextView qText, qScore;
    Button[] qBtns = new Button[4];
    LinearLayout qBox;
    int qCorrectIdx = -1, qTotal = 0, qRight = 0, qStreak = 0;
    boolean qLocked = false;

    // settings tab
    RadioGroup rgLang, rgTheme, rgUnit;
    int[] langIds = new int[3];
    int[] themeIds = new int[2];
    int[] unitIds = new int[3];

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        st = new Store(this);
        buildUI();
        showTab(0);
    }

    // ---------- helpers ----------
    String tr(String ur, String en, String ar) { return st.tr(ur, en, ar); }

    int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    GradientDrawable round(int color, int r) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(r));
        return g;
    }

    TextView tv(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    LinearLayout cardBox() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(round(cCard, 14));
        c.setPadding(dp(14), dp(10), dp(14), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(10), dp(5), dp(10), dp(5));
        c.setLayoutParams(lp);
        return c;
    }

    EditText searchBox(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(0xFF777777);
        e.setTextColor(Color.BLACK);
        e.setTextSize(15);
        e.setBackground(round(0xFFFFFFFF, 12));
        e.setPadding(dp(14), dp(9), dp(14), dp(9));
        e.setSingleLine(true);
        e.setInputType(InputType.TYPE_CLASS_TEXT);
        return e;
    }

    TextView chip(String s, boolean on, View.OnClickListener l) {
        TextView t = tv(s, 13, on ? Color.WHITE : cText, false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(7), dp(12), dp(7));
        t.setBackground(round(on ? ACCENT : (st.dark() ? 0xFF2A2A2A : 0xFFE0E0E0), 16));
        t.setOnClickListener(l);
        return t;
    }

    HorizontalScrollView hscroll(LinearLayout inner) {
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        hs.addView(inner);
        return hs;
    }

    LinearLayout.LayoutParams chipLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(3), 0, dp(3), 0);
        return lp;
    }

    void openElement(int z) {
        Intent i = new Intent(this, DetailActivity.class);
        i.putExtra("z", z);
        startActivity(i);
    }

    // ---------- main UI ----------
    void buildUI() {
        boolean dark = st.dark();
        cBg = dark ? 0xFF000000 : 0xFFF2F2F2;
        cCard = dark ? 0xFF1A1A1A : 0xFFFFFFFF;
        cText = dark ? 0xFFFFFFFF : 0xFF111111;
        cSub = dark ? 0xFFAAAAAA : 0xFF555555;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(cBg);
        root.setLayoutDirection(st.rtl() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        String[] names = {tr("جدول", "Table", "الجدول"), tr("فہرست", "List", "القائمة"),
            tr("کوئز", "Quiz", "اختبار"), tr("سیٹنگز", "Settings", "الإعدادات")};
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(dp(6), dp(10), dp(6), dp(6));
        tabViews = new TextView[4];
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            TextView t = tv(names[i], 14, cText, false);
            t.setGravity(Gravity.CENTER);
            t.setPadding(dp(4), dp(9), dp(4), dp(9));
            t.setOnClickListener(v -> showTab(idx));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            tabs.addView(t, lp);
            tabViews[i] = t;
        }
        root.addView(tabs);

        FrameLayout content = new FrameLayout(this);
        panes = new View[4];
        panes[0] = buildTablePane();
        panes[1] = buildListPane();
        panes[2] = buildQuizPane();
        panes[3] = buildSettingsPane();
        for (int i = 0; i < 4; i++) {
            content.addView(panes[i], new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        root.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView credit = tv("By: Black Falcon \uD83E\uDD85", 13, cSub, false);
        credit.setGravity(Gravity.CENTER);
        credit.setPadding(0, dp(6), 0, dp(8));
        root.addView(credit);

        setContentView(root);
    }

    void showTab(int i) {
        for (int k = 0; k < 4; k++) {
            boolean sel = k == i;
            tabViews[k].setTextColor(sel ? Color.WHITE : cText);
            tabViews[k].setBackground(round(sel ? ACCENT : cCard, 20));
            panes[k].setVisibility(sel ? View.VISIBLE : View.GONE);
        }
    }

    // ---------- table tab ----------
    View buildTablePane() {
        LinearLayout pane = new LinearLayout(this);
        pane.setOrientation(LinearLayout.VERTICAL);

        etSearch = searchBox(tr("نام، علامت یا نمبر تلاش کریں…", "Search name, symbol or number…",
                "ابحث بالاسم أو الرمز أو الرقم…"));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sp.setMargins(dp(10), dp(2), dp(10), dp(6));
        pane.addView(etSearch, sp);

        LinearLayout modes = new LinearLayout(this);
        modeChips = new TextView[4];
        for (int m = 0; m < 4; m++) {
            final int mm = m;
            modeChips[m] = chip(Labels.mode(st, m), mode == m, v -> setMode(mm));
            modes.addView(modeChips[m], chipLp());
        }
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mp.setMargins(dp(7), 0, dp(7), dp(4));
        pane.addView(hscroll(modes), mp);

        legendBox = new LinearLayout(this);
        LinearLayout.LayoutParams lgp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lgp.setMargins(dp(7), 0, dp(7), dp(4));
        pane.addView(hscroll(legendBox), lgp);

        table = new TableView(this);
        table.setTheme(st.dark());
        table.setMode(mode);
        table.setTexts(tr("متواتر جدول", "Periodic Table", "الجدول الدوري"),
                tr("رنگ: ", "Color: ", "اللون: ") + Labels.mode(st, mode),
                tr("دو انگلیوں سے زوم، ٹیپ کر کے تفصیل", "Pinch to zoom \u2022 tap an element",
                        "قرّب بإصبعين \u2022 اضغط على عنصر"));
        table.setListener(new TableView.Listener() {
            @Override public void onPick(int z) { openElement(z); }
            @Override public void onMarker(int cat) {
                if (mode != 0) setMode(0);
                hlCat = (hlCat == cat) ? -1 : cat;
                applyFilter();
                buildLegend();
            }
        });
        pane.addView(table, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable e) { applyFilter(); }
        });
        buildLegend();
        return pane;
    }

    void applyFilter() {
        table.setFilter(mode == 0 ? hlCat : -1, etSearch.getText().toString());
    }

    void setMode(int m) {
        mode = m;
        if (m != 0) hlCat = -1;
        for (int i = 0; i < modeChips.length; i++) {
            boolean on = i == m;
            modeChips[i].setTextColor(on ? Color.WHITE : cText);
            modeChips[i].setBackground(round(on ? ACCENT : (st.dark() ? 0xFF2A2A2A : 0xFFE0E0E0), 16));
        }
        table.setMode(m);
        table.setTexts(tr("متواتر جدول", "Periodic Table", "الجدول الدوري"),
                tr("رنگ: ", "Color: ", "اللون: ") + Labels.mode(st, m),
                tr("دو انگلیوں سے زوم، ٹیپ کر کے تفصیل", "Pinch to zoom \u2022 tap an element",
                        "قرّب بإصبعين \u2022 اضغط على عنصر"));
        applyFilter();
        buildLegend();
    }

    View legendItem(int color, String label, boolean selected, View.OnClickListener l) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(dp(10), dp(5), dp(12), dp(5));
        GradientDrawable g = new GradientDrawable();
        g.setColor(selected ? (st.dark() ? 0xFF3A3A3A : 0xFFCFD8DC) : (st.dark() ? 0xFF1A1A1A : 0xFFFFFFFF));
        g.setCornerRadius(dp(14));
        if (selected) g.setStroke(dp(2), color);
        r.setBackground(g);
        TextView dot = new TextView(this);
        dot.setBackground(round(color, 6));
        r.addView(dot, new LinearLayout.LayoutParams(dp(12), dp(12)));
        TextView t = tv(label, 12, cText, selected);
        t.setPadding(dp(6), 0, 0, 0);
        r.addView(t);
        if (l != null) r.setOnClickListener(l);
        LinearLayout.LayoutParams lp = chipLp();
        r.setLayoutParams(lp);
        return r;
    }

    void buildLegend() {
        legendBox.removeAllViews();
        if (mode == 0) {
            for (int c = 0; c < 11; c++) {
                final int cc = c;
                legendBox.addView(legendItem(Pal.CAT[c], Labels.cat(st, c), hlCat == c, v -> {
                    hlCat = (hlCat == cc) ? -1 : cc;
                    applyFilter();
                    buildLegend();
                }));
            }
        } else if (mode == 1) {
            char[] ss = {'s', 'l', 'g', '?'};
            for (char s : ss) legendBox.addView(legendItem(Pal.state(s), Labels.state(st, s), false, null));
        } else if (mode == 2) {
            char[] bs = {'s', 'p', 'd', 'f'};
            for (char b : bs) legendBox.addView(legendItem(Pal.block(b), b + "-" + tr("بلاک", "block", "كتلة"),
                    false, null));
        } else {
            legendBox.addView(legendItem(Pal.eneg(0.8), tr("کم (~0.8)", "Low (~0.8)", "منخفضة (~0.8)"), false, null));
            legendBox.addView(legendItem(Pal.eneg(2.2), tr("درمیانی (~2.2)", "Medium (~2.2)", "متوسطة (~2.2)"), false, null));
            legendBox.addView(legendItem(Pal.eneg(3.9), tr("زیادہ (~4.0)", "High (~4.0)", "عالية (~4.0)"), false, null));
            legendBox.addView(legendItem(Pal.UNKNOWN, tr("ڈیٹا نہیں", "No data", "لا بيانات"), false, null));
        }
    }

    // ---------- list tab ----------
    View buildListPane() {
        LinearLayout pane = new LinearLayout(this);
        pane.setOrientation(LinearLayout.VERTICAL);

        etList = searchBox(tr("نام، علامت یا نمبر تلاش کریں…", "Search name, symbol or number…",
                "ابحث بالاسم أو الرمز أو الرقم…"));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sp.setMargins(dp(10), dp(2), dp(10), dp(6));
        pane.addView(etList, sp);

        LinearLayout sortRow = new LinearLayout(this);
        sortChips = new TextView[2];
        sortChips[0] = chip(tr("ایٹمی نمبر کے مطابق", "By atomic number", "حسب العدد الذري"), !sortByName, v -> setSort(false));
        sortChips[1] = chip(tr("نام کے مطابق", "By name", "حسب الاسم"), sortByName, v -> setSort(true));
        sortRow.addView(sortChips[0], chipLp());
        sortRow.addView(sortChips[1], chipLp());
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rp.setMargins(dp(7), 0, dp(7), dp(6));
        pane.addView(sortRow, rp);

        lv = new ListView(this);
        lv.setDividerHeight(0);
        lv.setClipToPadding(false);
        adapter = new BaseAdapter() {
            @Override public int getCount() { return shown.size(); }
            @Override public Object getItem(int p) { return shown.get(p); }
            @Override public long getItemId(int p) { return p; }
            @Override public View getView(int p, View cv, ViewGroup parent) { return listRow(shown.get(p)); }
        };
        lv.setAdapter(adapter);
        lv.setOnItemClickListener((parent, view, pos, id) -> openElement(shown.get(pos)));
        pane.addView(lv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        etList.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable e) { refreshList(); }
        });
        refreshList();
        return pane;
    }

    void setSort(boolean byName) {
        sortByName = byName;
        for (int i = 0; i < 2; i++) {
            boolean on = (i == 1) == byName;
            sortChips[i].setTextColor(on ? Color.WHITE : cText);
            sortChips[i].setBackground(round(on ? ACCENT : (st.dark() ? 0xFF2A2A2A : 0xFFE0E0E0), 16));
        }
        refreshList();
    }

    void refreshList() {
        String q = etList.getText().toString().trim().toLowerCase(Locale.ROOT);
        shown.clear();
        for (int z = 1; z <= Elements.N; z++) {
            if (q.length() == 0 || Elements.SYM[z].toLowerCase(Locale.ROOT).contains(q)
                    || Elements.EN[z].toLowerCase(Locale.ROOT).contains(q)
                    || Elements.UR[z].contains(q) || Elements.AR[z].contains(q)
                    || String.valueOf(z).equals(q)) {
                shown.add(z);
            }
        }
        if (sortByName) {
            final String lang = st.lang();
            Collections.sort(shown, new Comparator<Integer>() {
                @Override public int compare(Integer a, Integer b) {
                    return Elements.name(a, lang).compareToIgnoreCase(Elements.name(b, lang));
                }
            });
        }
        adapter.notifyDataSetChanged();
    }

    View listRow(int z) {
        int cat = Elements.cat(z);
        int col = Pal.CAT[cat];
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setBackground(round(cCard, 14));
        r.setPadding(dp(10), dp(8), dp(12), dp(8));
        TextView tile = tv(Elements.SYM[z], 20, Pal.textOn(col), true);
        tile.setGravity(Gravity.CENTER);
        tile.setBackground(round(col, 10));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(dp(52), dp(52));
        tp.setMargins(0, 0, dp(12), 0);
        r.addView(tile, tp);
        LinearLayout col2 = new LinearLayout(this);
        col2.setOrientation(LinearLayout.VERTICAL);
        col2.addView(tv(Elements.name(z, st.lang()), 16, cText, true));
        col2.addView(tv("#" + z + "  \u2022  " + Elements.mass(z) + "  \u2022  " + Labels.cat(st, cat), 12, cSub, false));
        r.addView(col2, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return wrapRow(r);
    }

    View wrapRow(View r) {
        FrameLayout f = new FrameLayout(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(10), dp(4), dp(10), dp(4));
        f.addView(r, lp);
        return f;
    }

    // ---------- quiz tab ----------
    View buildQuizPane() {
        ScrollView sv = new ScrollView(this);
        qBox = new LinearLayout(this);
        qBox.setOrientation(LinearLayout.VERTICAL);
        qBox.setPadding(0, dp(4), 0, dp(12));
        sv.addView(qBox);

        LinearLayout top = cardBox();
        qScore = tv("", 13, cSub, false);
        top.addView(qScore);
        qText = tv("", 20, cText, true);
        qText.setPadding(0, dp(10), 0, dp(6));
        top.addView(qText);
        qBox.addView(top);

        LinearLayout opts = cardBox();
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            Button b = new Button(this);
            b.setTextColor(Color.WHITE);
            b.setAllCaps(false);
            b.setTextSize(17);
            b.setOnClickListener(v -> answer(idx));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, dp(5), 0, dp(5));
            opts.addView(b, lp);
            qBtns[i] = b;
        }
        qBox.addView(opts);
        newQuestion();
        return sv;
    }

    void newQuestion() {
        qLocked = false;
        int type = rnd.nextInt(3);
        int z = 1 + rnd.nextInt(103);
        String lang = st.lang();
        String[] options = new String[4];
        int[] zs = new int[4];
        qCorrectIdx = rnd.nextInt(4);
        boolean[] used = new boolean[104];
        used[z] = true;
        zs[qCorrectIdx] = z;
        for (int i = 0; i < 4; i++) {
            if (i == qCorrectIdx) continue;
            int w;
            do {
                w = 1 + rnd.nextInt(103);
            } while (used[w]);
            used[w] = true;
            zs[i] = w;
        }
        for (int i = 0; i < 4; i++) {
            options[i] = type == 0 ? Elements.SYM[zs[i]] : (type == 1 ? Elements.name(zs[i], lang)
                    : String.valueOf(zs[i]));
        }
        if (type == 0) {
            qText.setText(tr("\"" + Elements.name(z, lang) + "\" کی علامت کیا ہے؟",
                    "What is the symbol of " + Elements.name(z, lang) + "?",
                    "ما رمز " + Elements.name(z, lang) + "؟"));
        } else if (type == 1) {
            qText.setText(tr("کون سا عنصر علامت \"" + Elements.SYM[z] + "\" رکھتا ہے؟",
                    "Which element has the symbol " + Elements.SYM[z] + "?",
                    "أي عنصر رمزه " + Elements.SYM[z] + "؟"));
        } else {
            qText.setText(tr("\"" + Elements.name(z, lang) + "\" کا ایٹمی نمبر کیا ہے؟",
                    "What is the atomic number of " + Elements.name(z, lang) + "?",
                    "ما العدد الذري لـ " + Elements.name(z, lang) + "؟"));
        }
        for (int i = 0; i < 4; i++) {
            qBtns[i].setText(options[i]);
            qBtns[i].setBackground(round(0xFF455A64, 12));
        }
        updateScore();
    }

    void updateScore() {
        qScore.setText(tr("درست: ", "Correct: ", "الصحيح: ") + qRight + " / " + qTotal
                + tr("   مسلسل: ", "   Streak: ", "   متتالية: ") + qStreak
                + tr("   بہترین: ", "   Best: ", "   الأفضل: ") + st.best());
    }

    void answer(int idx) {
        if (qLocked) return;
        qLocked = true;
        qTotal++;
        if (idx == qCorrectIdx) {
            qRight++;
            qStreak++;
            if (qStreak > st.best()) st.setBest(qStreak);
        } else {
            qStreak = 0;
            qBtns[idx].setBackground(round(0xFFC62828, 12));
        }
        qBtns[qCorrectIdx].setBackground(round(0xFF2E7D32, 12));
        updateScore();
        h.postDelayed(new Runnable() {
            @Override public void run() { newQuestion(); }
        }, 1100);
    }

    // ---------- settings tab ----------
    RadioGroup radios(String[] labels, int checked, int[] ids) {
        RadioGroup g = new RadioGroup(this);
        for (int i = 0; i < labels.length; i++) {
            RadioButton r = new RadioButton(this);
            r.setText(labels[i]);
            r.setTextColor(cText);
            r.setTextSize(15);
            r.setId(View.generateViewId());
            ids[i] = r.getId();
            g.addView(r);
        }
        g.check(ids[checked]);
        return g;
    }

    TextView label(String s) {
        TextView t = tv(s, 16, cText, true);
        t.setPadding(0, dp(10), 0, dp(4));
        return t;
    }

    View buildSettingsPane() {
        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, dp(4), 0, dp(12));
        sv.addView(box);

        LinearLayout c = cardBox();
        c.addView(label(tr("زبان", "Language", "اللغة")));
        String l = st.lang();
        rgLang = radios(new String[] {"English", "اردو", "العربية"},
                l.equals("ur") ? 1 : (l.equals("ar") ? 2 : 0), langIds);
        c.addView(rgLang);
        c.addView(label(tr("تھیم", "Theme", "المظهر")));
        rgTheme = radios(new String[] {tr("ڈارک موڈ", "Dark mode", "الوضع الداكن"),
            tr("لائٹ موڈ", "Light mode", "الوضع الفاتح")}, st.dark() ? 0 : 1, themeIds);
        c.addView(rgTheme);
        c.addView(label(tr("درجہ حرارت کی اکائی", "Temperature unit", "وحدة الحرارة")));
        rgUnit = radios(new String[] {"Kelvin (K)", "Celsius (\u00B0C)", "Fahrenheit (\u00B0F)"}, st.unit(), unitIds);
        c.addView(rgUnit);
        Button save = new Button(this);
        save.setText(tr("محفوظ کریں", "Save", "حفظ"));
        save.setTextColor(Color.WHITE);
        save.setBackground(round(ACCENT, 10));
        save.setOnClickListener(v -> saveSettings());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(12), 0, 0);
        c.addView(save, lp);
        box.addView(c);

        TextView info = tv(tr(
            "ایپ مکمل طور پر آف لائن ہے اور انٹرنیٹ یا کسی پرمیشن کی ضرورت نہیں۔ جدول پر دو انگلیوں سے زوم کریں، ڈبل ٹیپ سے زوم اِن/آؤٹ، اور \"57–71\" یا \"89–103\" کے خانے پر ٹیپ کر کے لینتھینائڈ/ایکٹینائڈ نمایاں کریں۔\n\nاقدار معیاری حوالہ جاتی اعداد ہیں (گول کیے ہوئے)؛ بھاری عناصر کی خصوصیات زیادہ تر متوقع ہیں۔ اہم سائنسی کام کے لیے سرکاری ماخذ (IUPAC) سے تصدیق کریں۔",
            "The app is fully offline and needs no internet or permission. On the table: pinch to zoom, double-tap to zoom in/out, and tap the \"57\u201371\" or \"89\u2013103\" box to highlight the lanthanides/actinides.\n\nValues are standard reference numbers (rounded); properties of the heaviest elements are mostly predicted. For serious scientific work verify with an official source (IUPAC).",
            "التطبيق يعمل دون إنترنت ولا يحتاج إلى أي إذن. في الجدول: قرّب بإصبعين، والنقر المزدوج للتكبير/التصغير، واضغط على المربع \"57\u201371\" أو \"89\u2013103\" لإبراز اللانثانيدات/الأكتينيدات.\n\nالقيم أرقام مرجعية قياسية (مقرّبة)؛ وخصائص العناصر الثقيلة متوقعة في الغالب. للأعمال العلمية الجادة تحقق من مصدر رسمي (IUPAC)."),
            12, cSub, false);
        info.setPadding(dp(16), dp(10), dp(16), dp(8));
        box.addView(info);
        return sv;
    }

    void saveSettings() {
        String[] langs = {"en", "ur", "ar"};
        int sel = rgLang.getCheckedRadioButtonId();
        for (int i = 0; i < 3; i++) if (langIds[i] == sel) st.setLang(langs[i]);
        st.setDark(rgTheme.getCheckedRadioButtonId() == themeIds[0]);
        int us = rgUnit.getCheckedRadioButtonId();
        for (int i = 0; i < 3; i++) if (unitIds[i] == us) st.setUnit(i);
        mode = 0;
        hlCat = -1;
        buildUI();
        showTab(3);
        Toast.makeText(this, tr("محفوظ ہو گیا", "Saved", "تم الحفظ"), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        h.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle(tr("ایپ بند کریں؟", "Exit app?", "إغلاق التطبيق؟"))
            .setMessage(tr("کیا آپ واقعی باہر نکلنا چاہتے ہیں؟", "Do you really want to exit?",
                    "هل تريد الخروج فعلًا؟"))
            .setPositiveButton(tr("ہاں", "Yes", "نعم"), (dlg, w) -> finish())
            .setNegativeButton(tr("نہیں", "No", "لا"), null)
            .show();
    }
}
