package analyzer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

/**
 * Swing 主界面：输入源码 -> 执行分析 -> 查看 Token/符号/错误结果。
 */
public class LexerGUI extends JFrame {
    private final JTextArea inputArea;
    private final JLabel statusLabel;
    private final DefaultTableModel tokenModel;
    private final DefaultTableModel symbolModel;
    private final DefaultTableModel errorModel;
    private final Lexer lexer;

    public LexerGUI() {
        super("编译原理实践 - 词法分析器设计");
        this.lexer = new Lexer();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        inputArea = new JTextArea();
        inputArea.setText(defaultExampleCode());
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createTitledBorder("源程序输入区"));
        inputScroll.setPreferredSize(new Dimension(1100, 260));

        JPanel buttonPanel = new JPanel();
        JButton analyzeButton = new JButton("开始分析");
        JButton clearButton = new JButton("清空");
        JButton loadButton = new JButton("导入文件");
        buttonPanel.add(analyzeButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(loadButton);

        analyzeButton.addActionListener(e -> analyze());
        clearButton.addActionListener(e -> clearAll());
        loadButton.addActionListener(e -> loadFile());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputScroll, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        tokenModel = new DefaultTableModel(new Object[]{"序号", "词素", "Token 类型", "行号", "列号"}, 0);
        symbolModel = new DefaultTableModel(new Object[]{"标识符", "类别", "首次出现行"}, 0);
        errorModel = new DefaultTableModel(new Object[]{"错误信息", "行号", "列号"}, 0);

        JTable tokenTable = new JTable(tokenModel);
        JTable symbolTable = new JTable(symbolModel);
        JTable errorTable = new JTable(errorModel);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Token 列表", new JScrollPane(tokenTable));
        tabbedPane.addTab("符号表", new JScrollPane(symbolTable));
        tabbedPane.addTab("错误信息", new JScrollPane(errorTable));

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("分析结果输出区"));
        resultPanel.add(tabbedPane, BorderLayout.CENTER);

        statusLabel = new JLabel("状态：等待分析");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, resultPanel);
        splitPane.setResizeWeight(0.42);

        add(splitPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private String defaultExampleCode() {
        return "int main() {\n"
                + "    int n = 10;\n"
                + "    float x = 12.5;\n"
                + "    char c = 'a';\n"
                + "    String s = \"hello\";\n"
                + "    if (n >= 10 && x != 0.0) {\n"
                + "        // sample comment\n"
                + "        n += 1;\n"
                + "    }\n"
                + "}\n";
    }

    private void analyze() {
        tokenModel.setRowCount(0);
        symbolModel.setRowCount(0);
        errorModel.setRowCount(0);

        LexerResult result = lexer.analyze(inputArea.getText());

        int index = 1;
        for (Token token : result.getTokens()) {
            tokenModel.addRow(new Object[]{
                    index++, token.getLexeme(), token.getType(), token.getLine(), token.getColumn()
            });
        }

        for (SymbolEntry entry : result.getSymbols()) {
            symbolModel.addRow(new Object[]{entry.getName(), entry.getKind(), entry.getFirstLine()});
        }

        for (LexError error : result.getErrors()) {
            errorModel.addRow(new Object[]{error.getMessage(), error.getLine(), error.getColumn()});
        }

        statusLabel.setText(String.format(
                "状态：分析完成。Token=%d，符号=%d，错误=%d",
                result.getTokens().size(), result.getSymbols().size(), result.getErrors().size()
        ));
    }

    private void clearAll() {
        inputArea.setText("");
        tokenModel.setRowCount(0);
        symbolModel.setRowCount(0);
        errorModel.setRowCount(0);
        statusLabel.setText("状态：内容已清空");
    }

    private void loadFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择源代码文件");
        chooser.setFileFilter(new FileNameExtensionFilter("Text / Java Files", "txt", "java"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            statusLabel.setText("状态：已取消导入文件");
            return;
        }

        File selected = chooser.getSelectedFile();
        try {
            inputArea.setText(FileUtil.readText(selected));
            statusLabel.setText("状态：已导入文件 - " + selected.getName());
        } catch (IOException e) {
            statusLabel.setText("状态：文件读取失败 - " + e.getMessage());
        }
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> new LexerGUI().setVisible(true));
    }
}
