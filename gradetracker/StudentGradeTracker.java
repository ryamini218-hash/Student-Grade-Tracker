import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.text.DecimalFormat;

// ─── Data Model ──────────────────────────────────────────────────────────────

class Student {
    private String name;
    private String id;
    private ArrayList<Double> grades;
    private String[] subjectNames;

    public Student(String name, String id, double[] scores, String[] subjects) {
        this.name = name;
        this.id = id;
        this.subjectNames = subjects;
        this.grades = new ArrayList<>();
        for (double s : scores) grades.add(s);
    }

    public String getName()   { return name; }
    public String getId()     { return id; }
    public ArrayList<Double> getGrades() { return grades; }
    public String[] getSubjectNames() { return subjectNames; }

    public double getAverage() {
        if (grades.isEmpty()) return 0;
        double sum = 0;
        for (double g : grades) sum += g;
        return sum / grades.size();
    }

    public double getHighest() {
        return grades.stream().mapToDouble(Double::doubleValue).max().orElse(0);
    }

    public double getLowest() {
        return grades.stream().mapToDouble(Double::doubleValue).min().orElse(0);
    }

    public String getLetterGrade() {
        double avg = getAverage();
        if (avg >= 90) return "A";
        if (avg >= 80) return "B";
        if (avg >= 70) return "C";
        if (avg >= 60) return "D";
        return "F";
    }

    public String getStatus() {
        return getAverage() >= 60 ? "PASS" : "FAIL";
    }
}

// ─── Grade Manager ────────────────────────────────────────────────────────────

class GradeManager {
    private ArrayList<Student> students = new ArrayList<>();

    public void addStudent(Student s) { students.add(s); }
    public ArrayList<Student> getStudents() { return students; }

    public void removeStudent(String id) {
        students.removeIf(s -> s.getId().equals(id));
    }

    public Student findById(String id) {
        return students.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    public double getClassAverage() {
        if (students.isEmpty()) return 0;
        return students.stream().mapToDouble(Student::getAverage).average().orElse(0);
    }

    public Student getTopStudent() {
        return students.stream().max(Comparator.comparingDouble(Student::getAverage)).orElse(null);
    }

    public Student getLowestStudent() {
        return students.stream().min(Comparator.comparingDouble(Student::getAverage)).orElse(null);
    }

    public int countPassing() {
        return (int) students.stream().filter(s -> s.getAverage() >= 60).count();
    }
}

// ─── Color Palette ────────────────────────────────────────────────────────────

class Palette {
    static final Color BG         = new Color(0xF7F8FC);
    static final Color CARD       = Color.WHITE;
    static final Color PRIMARY    = new Color(0x4F6EF7);
    static final Color PRIMARY_DK = new Color(0x3A56D4);
    static final Color ACCENT     = new Color(0x22C55E);
    static final Color DANGER     = new Color(0xEF4444);
    static final Color WARNING    = new Color(0xF59E0B);
    static final Color TEXT       = new Color(0x1E293B);
    static final Color MUTED      = new Color(0x64748B);
    static final Color BORDER     = new Color(0xE2E8F0);
    static final Color PASS_BG    = new Color(0xDCFCE7);
    static final Color FAIL_BG    = new Color(0xFEE2E2);
    static final Color A_COLOR    = new Color(0x4F6EF7);
    static final Color B_COLOR    = new Color(0x22C55E);
    static final Color C_COLOR    = new Color(0xF59E0B);
    static final Color D_COLOR    = new Color(0xFB923C);
    static final Color F_COLOR    = new Color(0xEF4444);
}

// ─── Custom UI Components ─────────────────────────────────────────────────────

class RoundedPanel extends JPanel {
    private int radius;
    private Color borderColor;
    public RoundedPanel(int radius) {
        this(radius, null);
    }
    public RoundedPanel(int radius, Color border) {
        this.radius = radius;
        this.borderColor = border;
        setOpaque(false);
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, radius, radius));
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(0.75f, 0.75f, getWidth()-2.5f, getHeight()-2.5f, radius, radius));
        }
        g2.dispose();
        super.paintComponent(g);
    }
}

class PrimaryButton extends JButton {
    private Color base, hover;
    private boolean hovered = false;
    public PrimaryButton(String text, Color base) {
        super(text);
        this.base = base;
        this.hover = base.darker();
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
            public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
        });
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(hovered ? hover : base);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
        g2.dispose();
        super.paintComponent(g);
    }
}

class StatCard extends RoundedPanel {
    public StatCard(String label, String value, Color accent) {
        super(14, Palette.BORDER);
        setBackground(Palette.CARD);
        setLayout(new BorderLayout(0, 6));
        setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(Palette.MUTED);
        lbl.setLetterSpacing(1.5f);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 28));
        val.setForeground(accent);

        add(lbl, BorderLayout.NORTH);
        add(val, BorderLayout.CENTER);
    }
}

// ─── Mini Bar Chart for Report ────────────────────────────────────────────────

class GradeBarChart extends JPanel {
    private double[] scores;
    private String[] labels;
    private static final DecimalFormat DF = new DecimalFormat("0.0");

    public GradeBarChart(double[] scores, String[] labels) {
        this.scores = scores;
        this.labels = labels;
        setOpaque(false);
        setPreferredSize(new Dimension(400, 160));
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (scores == null || scores.length == 0) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int n = scores.length;
        int pad = 12, bottom = 30, top = 10;
        int chartH = getHeight() - bottom - top;
        int totalW = getWidth() - 2 * pad;
        int barW = Math.max(20, (totalW / n) - 8);
        int gap = (totalW - barW * n) / (n + 1);

        Color[] barColors = {Palette.PRIMARY, Palette.ACCENT, Palette.WARNING,
                             new Color(0xA78BFA), new Color(0xFB7185), new Color(0x22D3EE)};

        for (int i = 0; i < n; i++) {
            int x = pad + gap + i * (barW + gap);
            int barH = (int) (scores[i] / 100.0 * chartH);
            int y = top + chartH - barH;

            Color c = barColors[i % barColors.length];
            // Draw bar with rounded top
            g2.setColor(c);
            g2.fill(new RoundRectangle2D.Float(x, y, barW, barH, 6, 6));
            // Score label on top
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(Palette.TEXT);
            String scoreStr = DF.format(scores[i]);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(scoreStr, x + (barW - fm.stringWidth(scoreStr)) / 2, y - 3);
            // Subject label below
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(Palette.MUTED);
            String lbl = labels != null && i < labels.length ? labels[i] : "S" + (i+1);
            if (lbl.length() > 6) lbl = lbl.substring(0, 6);
            g2.drawString(lbl, x + (barW - fm.stringWidth(lbl)) / 2, getHeight() - 8);
        }
        g2.dispose();
    }
}

// ─── Add Student Dialog ───────────────────────────────────────────────────────

class AddStudentDialog extends JDialog {
    private JTextField nameField, idField;
    private JTextField[] gradeFields;
    private JTextField[] subjectFields;
    private JSpinner subjectCountSpinner;
    private JPanel gradePanel;
    private boolean confirmed = false;
    private Student result;

    public AddStudentDialog(Frame parent) {
        super(parent, "Add New Student", true);
        buildUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        setBackground(Palette.BG);
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Palette.BG);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JLabel title = new JLabel("Add New Student");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Palette.TEXT);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        root.add(title, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Palette.BG);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 0, 5, 10);

        nameField = styledField("e.g. Alice Johnson");
        idField   = styledField("e.g. STU001");

        addRow(form, gc, 0, "Full Name", nameField);
        addRow(form, gc, 1, "Student ID", idField);

        // Subject count
        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 1;
        JLabel scLbl = new JLabel("Number of Subjects");
        scLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        scLbl.setForeground(Palette.MUTED);
        form.add(scLbl, gc);

        gc.gridx = 1; gc.gridwidth = 2;
        subjectCountSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 10, 1));
        subjectCountSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(subjectCountSpinner, gc);

        PrimaryButton setBtn = new PrimaryButton("Set Subjects", Palette.MUTED);
        setBtn.setPreferredSize(new Dimension(120, 34));
        gc.gridx = 3; gc.gridwidth = 1;
        form.add(setBtn, gc);

        // Grade/Subject panel
        gradePanel = new JPanel(new GridBagLayout());
        gradePanel.setBackground(Palette.BG);
        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 4;
        gc.insets = new Insets(12, 0, 0, 0);
        form.add(gradePanel, gc);

        // Build initial 4-subject grid
        rebuildGradePanel(4);

        setBtn.addActionListener(e -> rebuildGradePanel((int) subjectCountSpinner.getValue()));

        root.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(Palette.BG);
        btnRow.setBorder(new EmptyBorder(20, 0, 0, 0));

        PrimaryButton cancel = new PrimaryButton("Cancel", Palette.MUTED);
        cancel.setPreferredSize(new Dimension(90, 36));
        cancel.addActionListener(e -> dispose());

        PrimaryButton add = new PrimaryButton("Add Student", Palette.PRIMARY);
        add.setPreferredSize(new Dimension(130, 36));
        add.addActionListener(e -> submit());

        btnRow.add(cancel);
        btnRow.add(add);
        root.add(btnRow, BorderLayout.SOUTH);

        setContentPane(root);
        setMinimumSize(new Dimension(520, 380));
    }

    private void rebuildGradePanel(int count) {
        gradePanel.removeAll();
        gradeFields   = new JTextField[count];
        subjectFields = new JTextField[count];

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 0, 4, 8);

        // Header row
        gc.gridy = 0;
        gc.gridx = 0; gc.weightx = 0.5;
        JLabel sh = new JLabel("Subject Name");
        sh.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sh.setForeground(Palette.MUTED);
        gradePanel.add(sh, gc);
        gc.gridx = 1; gc.weightx = 0.3;
        JLabel gh = new JLabel("Score (0–100)");
        gh.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gh.setForeground(Palette.MUTED);
        gradePanel.add(gh, gc);

        String[] defSubjects = {"Mathematics", "Science", "English", "History",
                                "Geography", "Physics", "Chemistry", "Biology", "CS", "Art"};
        for (int i = 0; i < count; i++) {
            gc.gridy = i + 1;
            gc.gridx = 0; gc.weightx = 0.5;
            subjectFields[i] = styledField(defSubjects[i % defSubjects.length]);
            subjectFields[i].setText(defSubjects[i % defSubjects.length]);
            gradePanel.add(subjectFields[i], gc);
            gc.gridx = 1; gc.weightx = 0.3;
            gradeFields[i] = styledField("e.g. 85");
            gradePanel.add(gradeFields[i], gc);
        }

        gradePanel.revalidate();
        gradePanel.repaint();
        pack();
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row, String label, JTextField field) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0;
        gc.insets = new Insets(5, 0, 5, 10);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(Palette.MUTED);
        p.add(lbl, gc);
        gc.gridx = 1; gc.gridwidth = 3; gc.weightx = 1;
        p.add(field, gc);
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(0xAEC0D0));
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(Palette.TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Palette.BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        f.setBackground(Color.WHITE);
        return f;
    }

    private void submit() {
        String name = nameField.getText().trim();
        String id   = idField.getText().trim();
        if (name.isEmpty() || id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and ID are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double[] scores = new double[gradeFields.length];
        String[] subjects = new String[gradeFields.length];
        for (int i = 0; i < gradeFields.length; i++) {
            subjects[i] = subjectFields[i].getText().trim();
            if (subjects[i].isEmpty()) subjects[i] = "Subject " + (i+1);
            try {
                double v = Double.parseDouble(gradeFields[i].getText().trim());
                if (v < 0 || v > 100) throw new NumberFormatException();
                scores[i] = v;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Score for " + subjects[i] + " must be 0–100.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        result = new Student(name, id, scores, subjects);
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public Student getStudent()  { return result; }
}

// ─── Report Panel ─────────────────────────────────────────────────────────────

class ReportPanel extends JPanel {
    private static final DecimalFormat DF = new DecimalFormat("0.00");

    public ReportPanel(Student s) {
        setLayout(new BorderLayout(0, 16));
        setBackground(Palette.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header card
        RoundedPanel header = new RoundedPanel(14, Palette.BORDER);
        header.setBackground(Palette.PRIMARY);
        header.setLayout(new BorderLayout(0, 4));
        header.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel nameL = new JLabel(s.getName());
        nameL.setFont(new Font("Segoe UI", Font.BOLD, 22));
        nameL.setForeground(Color.WHITE);

        JLabel idL = new JLabel("ID: " + s.getId() + "   ·   Grade: " + s.getLetterGrade() + "   ·   " + s.getStatus());
        idL.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        idL.setForeground(new Color(0xC7D2FE));

        header.add(nameL, BorderLayout.NORTH);
        header.add(idL, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Stats row
        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setOpaque(false);
        stats.add(new StatCard("Average", DF.format(s.getAverage()) + "%", Palette.PRIMARY));
        stats.add(new StatCard("Highest", DF.format(s.getHighest()) + "%", Palette.ACCENT));
        stats.add(new StatCard("Lowest",  DF.format(s.getLowest()) + "%", Palette.DANGER));
        add(stats, BorderLayout.CENTER);

        // Chart + breakdown
        JPanel bottom = new JPanel(new BorderLayout(16, 0));
        bottom.setOpaque(false);

        // Bar chart
        RoundedPanel chartCard = new RoundedPanel(14, Palette.BORDER);
        chartCard.setBackground(Palette.CARD);
        chartCard.setLayout(new BorderLayout(0, 8));
        chartCard.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel chartTitle = new JLabel("Score by Subject");
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chartTitle.setForeground(Palette.TEXT);
        ArrayList<Double> grades = s.getGrades();
        double[] arr = grades.stream().mapToDouble(Double::doubleValue).toArray();
        chartCard.add(chartTitle, BorderLayout.NORTH);
        chartCard.add(new GradeBarChart(arr, s.getSubjectNames()), BorderLayout.CENTER);
        bottom.add(chartCard, BorderLayout.CENTER);

        // Subject breakdown table
        RoundedPanel tableCard = new RoundedPanel(14, Palette.BORDER);
        tableCard.setBackground(Palette.CARD);
        tableCard.setLayout(new BorderLayout(0, 8));
        tableCard.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel tblTitle = new JLabel("Subject Breakdown");
        tblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblTitle.setForeground(Palette.TEXT);
        tableCard.add(tblTitle, BorderLayout.NORTH);

        String[] cols = {"Subject", "Score", "Grade"};
        String[] subjects = s.getSubjectNames();
        Object[][] data = new Object[grades.size()][3];
        for (int i = 0; i < grades.size(); i++) {
            String subj = (subjects != null && i < subjects.length) ? subjects[i] : "S" + (i+1);
            data[i][0] = subj;
            data[i][1] = DF.format(grades.get(i));
            double sc = grades.get(i);
            data[i][2] = sc>=90?"A": sc>=80?"B": sc>=70?"C": sc>=60?"D":"F";
        }
        JTable tbl = new JTable(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.setRowHeight(26);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tbl.getTableHeader().setBackground(Palette.BG);
        tbl.getTableHeader().setForeground(Palette.MUTED);
        tbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                cell.setBorder(new EmptyBorder(0, 8, 0, 8));
                cell.setBackground(r % 2 == 0 ? Color.WHITE : Palette.BG);
                if (c == 2) {
                    String g = val.toString();
                    cell.setForeground(g.equals("A") ? Palette.A_COLOR :
                                       g.equals("B") ? Palette.B_COLOR :
                                       g.equals("C") ? Palette.C_COLOR :
                                       g.equals("D") ? Palette.D_COLOR : Palette.F_COLOR);
                    cell.setFont(cell.getFont().deriveFont(Font.BOLD));
                } else {
                    cell.setForeground(Palette.TEXT);
                }
                return cell;
            }
        });
        tableCard.add(new JScrollPane(tbl), BorderLayout.CENTER);
        bottom.add(tableCard, BorderLayout.EAST);

        Dimension tableCardSize = new Dimension(220, 160);
        tableCard.setPreferredSize(tableCardSize);

        add(bottom, BorderLayout.SOUTH);
    }
}

// ─── Main App Window ──────────────────────────────────────────────────────────

public class StudentGradeTracker extends JFrame {
    private GradeManager manager = new GradeManager();
    private DefaultTableModel tableModel;
    private JTable studentTable;
    private JLabel classAvgLabel, topStudentLabel, passingLabel, totalLabel;
    private static final DecimalFormat DF = new DecimalFormat("0.00");

    public StudentGradeTracker() {
        setTitle("Student Grade Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setSize(1150, 720);
        setLocationRelativeTo(null);
        setBackground(Palette.BG);
        buildUI();
        loadSampleData();
        refreshStats();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Palette.BG);
        setContentPane(root);

        // ── Sidebar ──────────────────────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(0x1E293B));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(28, 20, 28, 20));

        JLabel logoIcon = new JLabel("📊");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        logoIcon.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logoIcon);
        sidebar.add(Box.createVerticalStrut(6));

        JLabel logoText = new JLabel("GradeTracker");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoText.setForeground(Color.WHITE);
        logoText.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logoText);

        JLabel logoSub = new JLabel("Student Analytics");
        logoSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        logoSub.setForeground(new Color(0x64748B));
        logoSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logoSub);
        sidebar.add(Box.createVerticalStrut(36));

        // Stat cards in sidebar
        classAvgLabel   = sidebarStat("Class Average", "—");
        topStudentLabel = sidebarStat("Top Student", "—");
        passingLabel    = sidebarStat("Passing", "—");
        totalLabel      = sidebarStat("Total Students", "0");

        for (JPanel p : new JPanel[]{
            wrapStat("CLASS AVERAGE",   classAvgLabel,   new Color(0x4F6EF7)),
            wrapStat("TOP STUDENT",     topStudentLabel,  Palette.ACCENT),
            wrapStat("PASSING",         passingLabel,     Palette.WARNING),
            wrapStat("TOTAL STUDENTS",  totalLabel,       new Color(0xA78BFA))
        }) {
            p.setAlignmentX(Component.LEFT_ALIGNMENT);
            sidebar.add(p);
            sidebar.add(Box.createVerticalStrut(12));
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel ver = new JLabel("v1.0 · Java Swing");
        ver.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        ver.setForeground(new Color(0x334155));
        ver.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(ver);

        root.add(sidebar, BorderLayout.WEST);

        // ── Main content ─────────────────────────────────────────────────────
        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(Palette.BG);
        main.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Top toolbar
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel pageTitle = new JLabel("Student Roster");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        pageTitle.setForeground(Palette.TEXT);
        toolbar.add(pageTitle, BorderLayout.WEST);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGroup.setOpaque(false);

        PrimaryButton addBtn    = new PrimaryButton("＋  Add Student", Palette.PRIMARY);
        PrimaryButton reportBtn = new PrimaryButton("📄  Report", new Color(0x6366F1));
        PrimaryButton removeBtn = new PrimaryButton("✕  Remove", Palette.DANGER);

        for (JButton b : new JButton[]{addBtn, reportBtn, removeBtn}) {
            b.setPreferredSize(new Dimension(140, 38));
            btnGroup.add(b);
        }
        toolbar.add(btnGroup, BorderLayout.EAST);
        main.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Name", "Subjects", "Average", "Highest", "Lowest", "Grade", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        studentTable = new JTable(tableModel);
        studentTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        studentTable.setRowHeight(40);
        studentTable.setShowGrid(false);
        studentTable.setIntercellSpacing(new Dimension(0, 0));
        studentTable.setSelectionBackground(new Color(0xEEF2FF));
        studentTable.setSelectionForeground(Palette.TEXT);
        studentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        studentTable.getTableHeader().setBackground(Palette.BG);
        studentTable.getTableHeader().setForeground(Palette.MUTED);
        studentTable.getTableHeader().setBorder(new EmptyBorder(0, 0, 6, 0));
        studentTable.getTableHeader().setPreferredSize(new Dimension(0, 36));

        // Column widths
        int[] colW = {75, 180, 70, 90, 90, 90, 70, 80};
        for (int i = 0; i < colW.length; i++)
            studentTable.getColumnModel().getColumn(i).setPreferredWidth(colW[i]);

        // Custom cell renderer
        DefaultTableCellRenderer cr = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                cell.setBorder(new EmptyBorder(0, 12, 0, 12));
                cell.setBackground(sel ? new Color(0xEEF2FF) : (r % 2 == 0 ? Color.WHITE : Palette.BG));
                cell.setForeground(Palette.TEXT);
                cell.setFont(new Font("Segoe UI", Font.PLAIN, 13));

                if (c == 6) { // Grade column
                    String g = val.toString();
                    cell.setForeground(g.equals("A") ? Palette.A_COLOR :
                                       g.equals("B") ? Palette.B_COLOR :
                                       g.equals("C") ? Palette.C_COLOR :
                                       g.equals("D") ? Palette.D_COLOR : Palette.F_COLOR);
                    cell.setFont(cell.getFont().deriveFont(Font.BOLD, 13f));
                }
                if (c == 7) { // Status column
                    boolean pass = val.toString().equals("PASS");
                    cell.setForeground(pass ? Palette.ACCENT : Palette.DANGER);
                    cell.setFont(cell.getFont().deriveFont(Font.BOLD, 11f));
                }
                if (c == 3) { // Average — bold
                    cell.setFont(cell.getFont().deriveFont(Font.BOLD, 13f));
                    cell.setForeground(Palette.PRIMARY);
                }
                return cell;
            }
        };
        for (int i = 0; i < cols.length; i++)
            studentTable.getColumnModel().getColumn(i).setCellRenderer(cr);

        JScrollPane scroll = new JScrollPane(studentTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        RoundedPanel tableCard = new RoundedPanel(14, Palette.BORDER);
        tableCard.setBackground(Palette.CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.add(scroll, BorderLayout.CENTER);
        main.add(tableCard, BorderLayout.CENTER);

        root.add(main, BorderLayout.CENTER);

        // ── Button actions ────────────────────────────────────────────────────
        addBtn.addActionListener(e -> addStudent());
        removeBtn.addActionListener(e -> removeStudent());
        reportBtn.addActionListener(e -> showReport());
    }

    private JLabel sidebarStat(String label, String def) {
        JLabel l = new JLabel(def);
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setForeground(Color.WHITE);
        return l;
    }

    private JPanel wrapStat(String label, JLabel valLabel, Color accent) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setBackground(new Color(0x0F172A));
        p.setBorder(new CompoundBorder(
            new LineBorder(new Color(0x1E293B), 0),
            new EmptyBorder(12, 14, 12, 14)
        ));
        // Left accent bar
        p.setLayout(new BorderLayout());

        JPanel inner = new JPanel(new BorderLayout(0, 2));
        inner.setBackground(new Color(0x0F172A));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(new Color(0x475569));
        inner.add(lbl, BorderLayout.NORTH);
        inner.add(valLabel, BorderLayout.CENTER);

        // accent dot
        JPanel dot = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillOval(0, 4, 8, 8);
                g2.dispose();
            }
        };
        dot.setBackground(new Color(0x0F172A));
        dot.setPreferredSize(new Dimension(18, 20));

        p.add(dot, BorderLayout.WEST);
        p.add(inner, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        p.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(0x1E293B)),
            new EmptyBorder(10, 10, 10, 10)
        ));
        return p;
    }

    private void addStudent() {
        AddStudentDialog dlg = new AddStudentDialog(this);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            Student s = dlg.getStudent();
            if (manager.findById(s.getId()) != null) {
                JOptionPane.showMessageDialog(this, "A student with ID " + s.getId() + " already exists.", "Duplicate", JOptionPane.WARNING_MESSAGE);
                return;
            }
            manager.addStudent(s);
            refreshTable();
            refreshStats();
        }
    }

    private void removeStudent() {
        int row = studentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student to remove.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = tableModel.getValueAt(row, 0).toString();
        String name = tableModel.getValueAt(row, 1).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
            "Remove " + name + " (" + id + ") from the roster?",
            "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            manager.removeStudent(id);
            refreshTable();
            refreshStats();
        }
    }

    private void showReport() {
        int row = studentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student to view their report.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = tableModel.getValueAt(row, 0).toString();
        Student s = manager.findById(id);
        if (s == null) return;

        JDialog reportDlg = new JDialog(this, "Student Report — " + s.getName(), true);
        reportDlg.setBackground(Palette.BG);
        reportDlg.setContentPane(new ReportPanel(s));
        reportDlg.setSize(700, 580);
        reportDlg.setLocationRelativeTo(this);
        reportDlg.setVisible(true);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Student s : manager.getStudents()) {
            tableModel.addRow(new Object[]{
                s.getId(),
                s.getName(),
                s.getGrades().size(),
                DF.format(s.getAverage()) + "%",
                DF.format(s.getHighest()) + "%",
                DF.format(s.getLowest()) + "%",
                s.getLetterGrade(),
                s.getStatus()
            });
        }
    }

    private void refreshStats() {
        List<Student> all = manager.getStudents();
        if (all.isEmpty()) {
            classAvgLabel.setText("—");
            topStudentLabel.setText("—");
            passingLabel.setText("—");
            totalLabel.setText("0");
            return;
        }
        classAvgLabel.setText(DF.format(manager.getClassAverage()) + "%");
        Student top = manager.getTopStudent();
        topStudentLabel.setText(top != null ? top.getName().split(" ")[0] : "—");
        passingLabel.setText(manager.countPassing() + " / " + all.size());
        totalLabel.setText(String.valueOf(all.size()));
    }

    private void loadSampleData() {
        String[] subjects = {"Math", "Science", "English", "History"};
        manager.addStudent(new Student("Alice Johnson",  "STU001", new double[]{92, 88, 95, 91}, subjects));
        manager.addStudent(new Student("Bob Martinez",   "STU002", new double[]{75, 82, 68, 77}, subjects));
        manager.addStudent(new Student("Carol White",    "STU003", new double[]{55, 60, 58, 62}, subjects));
        manager.addStudent(new Student("David Lee",      "STU004", new double[]{98, 95, 97, 99}, subjects));
        manager.addStudent(new Student("Emma Thompson",  "STU005", new double[]{83, 79, 86, 81}, subjects));
        manager.addStudent(new Student("Frank Wilson",   "STU006", new double[]{45, 52, 48, 50}, subjects));
        refreshTable();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new StudentGradeTracker().setVisible(true));
    }
}