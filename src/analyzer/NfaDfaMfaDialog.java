package analyzer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;

/**
 * NFA-DFA-MFA 转换窗口。
 */
public class NfaDfaMfaDialog extends JDialog {
    private final JTextField regexField;

    private final DefaultTableModel nfaModel;
    private final DefaultTableModel dfaModel;
    private final DefaultTableModel mfaModel;

    private final JLabel nfaStartLabel;
    private final JLabel nfaEndLabel;
    private final JLabel dfaStartLabel;
    private final JLabel dfaEndLabel;
    private final JLabel mfaStartLabel;
    private final JLabel mfaEndLabel;

    private final RegexToNfaConverter regexConverter = new RegexToNfaConverter();
    private final AutomatonConverter automatonConverter = new AutomatonConverter();

    private Automaton currentNfa;
    private Automaton currentDfa;
    private Automaton currentMfa;

    public NfaDfaMfaDialog(JFrame owner) {
        super(owner, "NFA_DFA_MFA", true);
        setSize(1200, 760);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        JPanel expressionPanel = new JPanel(new BorderLayout(8, 8));
        expressionPanel.setBorder(BorderFactory.createTitledBorder("表达式"));

        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
        inputPanel.add(new JLabel("请输入一个正则式："), BorderLayout.WEST);
        regexField = new JTextField("(a*|b)*");
        inputPanel.add(regexField, BorderLayout.CENTER);
        inputPanel.add(new JLabel("例如：(a*|b)*"), BorderLayout.EAST);

        JPanel actionPanel = new JPanel();
        JButton validateBtn = new JButton("验证正则式");
        JButton closeBtn = new JButton("退出");
        validateBtn.addActionListener(e -> validateRegex());
        closeBtn.addActionListener(e -> dispose());
        actionPanel.add(validateBtn);
        actionPanel.add(closeBtn);

        expressionPanel.add(inputPanel, BorderLayout.CENTER);
        expressionPanel.add(actionPanel, BorderLayout.EAST);

        nfaModel = new DefaultTableModel(new Object[]{"起始状态", "接受符号", "到达状态"}, 0);
        dfaModel = new DefaultTableModel(new Object[]{"起始状态", "接受符号", "到达状态"}, 0);
        mfaModel = new DefaultTableModel(new Object[]{"起始状态", "接受符号", "到达状态"}, 0);

        nfaStartLabel = new JLabel("开始状态集：");
        nfaEndLabel = new JLabel("终结状态集：");
        dfaStartLabel = new JLabel("开始状态集：");
        dfaEndLabel = new JLabel("终结状态集：");
        mfaStartLabel = new JLabel("开始状态集：");
        mfaEndLabel = new JLabel("终结状态集：");

        JPanel centerPanel = new JPanel(new GridLayout(1, 3, 8, 8));
        centerPanel.add(buildSection("正则式->NFA", nfaModel, nfaStartLabel, nfaEndLabel, true));
        centerPanel.add(buildSection("NFA->DFA", dfaModel, dfaStartLabel, dfaEndLabel, false));
        centerPanel.add(buildSection("DFA->MFA", mfaModel, mfaStartLabel, mfaEndLabel, false));

        add(expressionPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel buildSection(String title,
                                DefaultTableModel model,
                                JLabel startLabel,
                                JLabel endLabel,
                                boolean firstSection) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createTitledBorder(title));

        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.add(startLabel);
        infoPanel.add(endLabel);

        JPanel buttonPanel = new JPanel();

        if (firstSection) {
            JButton readNfaBtn = new JButton("读取NFA文件");
            JButton genNfaBtn = new JButton("生成NFA");
            JButton saveNfaBtn = new JButton("保存");
            readNfaBtn.addActionListener(e -> readNfa());
            genNfaBtn.addActionListener(e -> generateNfa());
            saveNfaBtn.addActionListener(e -> saveAutomaton(currentNfa, "保存NFA"));
            buttonPanel.add(readNfaBtn);
            buttonPanel.add(genNfaBtn);
            buttonPanel.add(saveNfaBtn);
        } else if (title.equals("NFA->DFA")) {
            JButton readDfaBtn = new JButton("读取DFA文件");
            JButton genDfaBtn = new JButton("生成DFA");
            JButton saveDfaBtn = new JButton("保存");
            readDfaBtn.addActionListener(e -> readDfa());
            genDfaBtn.addActionListener(e -> generateDfa());
            saveDfaBtn.addActionListener(e -> saveAutomaton(currentDfa, "保存DFA"));
            buttonPanel.add(readDfaBtn);
            buttonPanel.add(genDfaBtn);
            buttonPanel.add(saveDfaBtn);
        } else {
            JButton genMfaBtn = new JButton("生成MFA");
            JButton saveMfaBtn = new JButton("保存");
            genMfaBtn.addActionListener(e -> generateMfa());
            saveMfaBtn.addActionListener(e -> saveAutomaton(currentMfa, "保存MFA"));
            buttonPanel.add(genMfaBtn);
            buttonPanel.add(saveMfaBtn);
        }

        JPanel south = new JPanel(new BorderLayout(4, 4));
        south.add(infoPanel, BorderLayout.CENTER);
        south.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void validateRegex() {
        String regex = regexField.getText().trim();
        boolean valid = regexConverter.isValidRegex(regex);
        JOptionPane.showMessageDialog(
                this,
                valid ? "正则式格式合法。" : "正则式格式不合法，请检查括号与字符。",
                "验证结果",
                valid ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
        );
    }

    private void generateNfa() {
        try {
            currentNfa = regexConverter.convert(regexField.getText().trim());
            fillTable(nfaModel, currentNfa);
            fillMeta(currentNfa, nfaStartLabel, nfaEndLabel);
            clearDfaMfaView();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "生成NFA失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateDfa() {
        if (currentNfa == null) {
            JOptionPane.showMessageDialog(this, "请先生成或读取 NFA。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        currentDfa = automatonConverter.nfaToDfa(currentNfa);
        fillTable(dfaModel, currentDfa);
        fillMeta(currentDfa, dfaStartLabel, dfaEndLabel);
        mfaModel.setRowCount(0);
        mfaStartLabel.setText("开始状态集：");
        mfaEndLabel.setText("终结状态集：");
    }

    private void generateMfa() {
        if (currentDfa == null) {
            JOptionPane.showMessageDialog(this, "请先生成或读取 DFA。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        currentMfa = automatonConverter.dfaToMfa(currentDfa);
        fillTable(mfaModel, currentMfa);
        fillMeta(currentMfa, mfaStartLabel, mfaEndLabel);
    }

    private void readNfa() {
        Automaton loaded = chooseAndLoad("读取NFA文件");
        if (loaded != null) {
            currentNfa = loaded;
            fillTable(nfaModel, currentNfa);
            fillMeta(currentNfa, nfaStartLabel, nfaEndLabel);
            clearDfaMfaView();
        }
    }

    private void readDfa() {
        Automaton loaded = chooseAndLoad("读取DFA文件");
        if (loaded != null) {
            currentDfa = loaded;
            fillTable(dfaModel, currentDfa);
            fillMeta(currentDfa, dfaStartLabel, dfaEndLabel);
            mfaModel.setRowCount(0);
            mfaStartLabel.setText("开始状态集：");
            mfaEndLabel.setText("终结状态集：");
        }
    }

    private Automaton chooseAndLoad(String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = chooser.getSelectedFile();
        try {
            return AutomatonFileUtil.load(file);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "读取失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void saveAutomaton(Automaton automaton, String title) {
        if (automaton == null) {
            JOptionPane.showMessageDialog(this, "当前没有可保存的数据。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setSelectedFile(new File("automaton.txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            AutomatonFileUtil.save(automaton, chooser.getSelectedFile());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "保存失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearDfaMfaView() {
        dfaModel.setRowCount(0);
        mfaModel.setRowCount(0);
        dfaStartLabel.setText("开始状态集：");
        dfaEndLabel.setText("终结状态集：");
        mfaStartLabel.setText("开始状态集：");
        mfaEndLabel.setText("终结状态集：");
        currentDfa = null;
        currentMfa = null;
    }

    private void fillTable(DefaultTableModel model, Automaton automaton) {
        model.setRowCount(0);
        for (String[] row : automaton.toTransitionRows()) {
            model.addRow(row);
        }
    }

    private void fillMeta(Automaton automaton, JLabel startLabel, JLabel endLabel) {
        startLabel.setText("开始状态集：{" + automaton.getStartState() + "}");
        startLabel.setToolTipText(startLabel.getText());
        endLabel.setText("终结状态集：" + automaton.getAcceptStates());
        endLabel.setToolTipText(endLabel.getText());
    }
}
