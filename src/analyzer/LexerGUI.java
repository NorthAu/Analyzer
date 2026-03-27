package analyzer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;

/**
 * Swing 主界面：仿“编译辅助系统”布局（菜单栏 + 工具栏 + 多分栏面板）。
 */
public class LexerGUI extends JFrame {
    private final JTextArea inputArea;
    private final JTextArea tokenTextArea;
    private final DefaultTableModel symbolModel;
    private final DefaultTableModel errorModel;
    private final JLabel statusLabel;
    private final Lexer lexer;

    public LexerGUI() {
        super("编译原理教学辅助系统");
        this.lexer = new Lexer();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 820);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(4, 4));

        setJMenuBar(buildMenuBar());
        add(buildToolBar(), BorderLayout.NORTH);

        inputArea = new JTextArea();
        inputArea.setText(defaultExampleCode());
        JScrollPane inputPane = new JScrollPane(inputArea);
        inputPane.setBorder(BorderFactory.createTitledBorder("源程序输入区"));

        tokenTextArea = new JTextArea();
        tokenTextArea.setEditable(false);
        JScrollPane tokenPane = new JScrollPane(tokenTextArea);
        tokenPane.setBorder(BorderFactory.createTitledBorder("Token 输出区"));

        symbolModel = new DefaultTableModel(new Object[]{"标识符", "类别", "首次出现行"}, 0);
        JTable symbolTable = new JTable(symbolModel);
        JScrollPane symbolPane = new JScrollPane(symbolTable);
        symbolPane.setBorder(BorderFactory.createTitledBorder("符号表"));

        errorModel = new DefaultTableModel(new Object[]{"错误信息", "行号", "列号"}, 0);
        JTable errorTable = new JTable(errorModel);
        JScrollPane errorPane = new JScrollPane(errorTable);
        errorPane.setBorder(BorderFactory.createTitledBorder("错误信息"));

        JSplitPane rightBottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, symbolPane, errorPane);
        rightBottom.setResizeWeight(0.5);

        JSplitPane rightVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tokenPane, rightBottom);
        rightVertical.setResizeWeight(0.58);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPane, rightVertical);
        mainSplit.setResizeWeight(0.38);

        add(mainSplit, BorderLayout.CENTER);

        statusLabel = new JLabel("状态：就绪");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("文件(F)");
        JMenuItem newItem = new JMenuItem("新建");
        JMenuItem openItem = new JMenuItem("打开");
        JMenuItem saveItem = new JMenuItem("保存输入");
        JMenuItem exportItem = new JMenuItem("导出Token");
        JMenuItem exitItem = new JMenuItem("退出");

        newItem.addActionListener(e -> newFile());
        openItem.addActionListener(e -> openFile());
        saveItem.addActionListener(e -> saveInputFile());
        exportItem.addActionListener(e -> exportTokenOutput());
        exitItem.addActionListener(e -> dispose());

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu editMenu = new JMenu("编辑(E)");
        JMenuItem cutItem = new JMenuItem("剪切");
        JMenuItem copyItem = new JMenuItem("复制");
        JMenuItem pasteItem = new JMenuItem("粘贴");
        JMenuItem clearItem = new JMenuItem("清空全部");

        cutItem.addActionListener(e -> inputArea.cut());
        copyItem.addActionListener(e -> inputArea.copy());
        pasteItem.addActionListener(e -> inputArea.paste());
        clearItem.addActionListener(e -> clearAll());

        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(clearItem);

        JMenu analysisMenu = new JMenu("词法分析(W)");
        JMenuItem runItem = new JMenuItem("开始分析");
        JMenuItem clearResultItem = new JMenuItem("清空结果");
        runItem.addActionListener(e -> analyze());
        clearResultItem.addActionListener(e -> clearResultOnly());
        analysisMenu.add(runItem);
        analysisMenu.add(clearResultItem);

        JMenu helpMenu = new JMenu("帮助(H)");
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(analysisMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JPanel buildToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        toolBar.add(createToolButton("新建", this::newFile));
        toolBar.add(createToolButton("打开", this::openFile));
        toolBar.add(createToolButton("保存输入", this::saveInputFile));
        toolBar.addSeparator();
        toolBar.add(createToolButton("剪切", () -> inputArea.cut()));
        toolBar.add(createToolButton("复制", () -> inputArea.copy()));
        toolBar.add(createToolButton("粘贴", () -> inputArea.paste()));
        toolBar.addSeparator();
        toolBar.add(createToolButton("分析", this::analyze));
        toolBar.add(createToolButton("清空结果", this::clearResultOnly));
        toolBar.add(createToolButton("清空全部", this::clearAll));
        toolBar.add(createToolButton("导出Token", this::exportTokenOutput));
        toolBar.addSeparator();
        toolBar.add(createToolButton("帮助", this::showAbout));

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrap.add(toolBar);
        wrap.setPreferredSize(new Dimension(1250, 44));
        return wrap;
    }

    private JButton createToolButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
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
        clearResultOnly();

        LexerResult result = lexer.analyze(inputArea.getText());
        StringBuilder tokenText = new StringBuilder();
        tokenText.append(String.format("%-6s %-24s %-14s %-6s %-6s%n", "序号", "词素", "类型", "行", "列"));
        tokenText.append("----------------------------------------------------------------\n");

        int index = 1;
        for (Token token : result.getTokens()) {
            tokenText.append(String.format(
                    "%-6d %-24s %-14s %-6d %-6d%n",
                    index++, trimForColumn(token.getLexeme()), token.getType(), token.getLine(), token.getColumn()
            ));
        }
        tokenTextArea.setText(tokenText.toString());

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

    private String trimForColumn(String lexeme) {
        String normalized = lexeme.replace("\n", "\\n");
        return normalized.length() <= 22 ? normalized : normalized.substring(0, 19) + "...";
    }

    private void newFile() {
        inputArea.setText("");
        clearResultOnly();
        statusLabel.setText("状态：新建完成");
    }

    private void clearAll() {
        inputArea.setText("");
        clearResultOnly();
        statusLabel.setText("状态：已清空输入与输出");
    }

    private void clearResultOnly() {
        tokenTextArea.setText("");
        symbolModel.setRowCount(0);
        errorModel.setRowCount(0);
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择源代码文件");
        chooser.setFileFilter(new FileNameExtensionFilter("Text / Java Files", "txt", "java"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            statusLabel.setText("状态：已取消打开文件");
            return;
        }

        File selected = chooser.getSelectedFile();
        try {
            inputArea.setText(FileUtil.readText(selected));
            statusLabel.setText("状态：已打开文件 - " + selected.getName());
        } catch (IOException e) {
            statusLabel.setText("状态：文件读取失败 - " + e.getMessage());
        }
    }

    private void saveInputFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("保存当前输入");
        chooser.setSelectedFile(new File("source.txt"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            statusLabel.setText("状态：已取消保存输入");
            return;
        }

        File target = chooser.getSelectedFile();
        try {
            FileUtil.writeText(target, inputArea.getText());
            statusLabel.setText("状态：输入已保存 - " + target.getName());
        } catch (IOException e) {
            statusLabel.setText("状态：保存失败 - " + e.getMessage());
        }
    }

    private void exportTokenOutput() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("导出 Token 输出");
        chooser.setSelectedFile(new File("tokens.txt"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            statusLabel.setText("状态：已取消导出 Token");
            return;
        }

        File target = chooser.getSelectedFile();
        try {
            FileUtil.writeText(target, tokenTextArea.getText());
            statusLabel.setText("状态：Token 已导出 - " + target.getName());
        } catch (IOException e) {
            statusLabel.setText("状态：导出失败 - " + e.getMessage());
        }
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(
                this,
                "编译原理实践：词法分析器设计\n"
                        + "功能：输入、分析、Token/符号表/错误输出、文件导入导出。",
                "关于",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> new LexerGUI().setVisible(true));
    }
}
