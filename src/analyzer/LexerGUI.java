package analyzer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class LexerGUI extends JFrame {
    private final JTextArea inputArea;
    private final DefaultTableModel tokenModel;
    private final DefaultTableModel symbolModel;
    private final DefaultTableModel errorModel;
    private final Lexer lexer;

    public LexerGUI() {
        super("Java 词法分析器");
        this.lexer = new Lexer();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        inputArea = new JTextArea();
        inputArea.setText("int main() {\n    float x = 12.5;\n    // sample\n    String s = \"hello\";\n}\n");
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createTitledBorder("源代码输入"));
        inputScroll.setPreferredSize(new Dimension(1000, 220));

        JButton analyzeButton = new JButton("开始词法分析");
        analyzeButton.addActionListener(e -> analyze());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputScroll, BorderLayout.CENTER);
        topPanel.add(analyzeButton, BorderLayout.SOUTH);

        tokenModel = new DefaultTableModel(new Object[]{"序号", "类型", "词素", "行", "列"}, 0);
        symbolModel = new DefaultTableModel(new Object[]{"名称", "类别", "首次出现行"}, 0);
        errorModel = new DefaultTableModel(new Object[]{"错误信息", "行", "列"}, 0);

        JTable tokenTable = new JTable(tokenModel);
        JTable symbolTable = new JTable(symbolModel);
        JTable errorTable = new JTable(errorModel);

        JScrollPane tokenPane = new JScrollPane(tokenTable);
        tokenPane.setBorder(BorderFactory.createTitledBorder("Token 表"));
        JScrollPane symbolPane = new JScrollPane(symbolTable);
        symbolPane.setBorder(BorderFactory.createTitledBorder("符号表"));
        JScrollPane errorPane = new JScrollPane(errorTable);
        errorPane.setBorder(BorderFactory.createTitledBorder("错误表"));

        JSplitPane bottomTop = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tokenPane, symbolPane);
        bottomTop.setResizeWeight(0.65);

        JSplitPane bottomPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, bottomTop, errorPane);
        bottomPanel.setResizeWeight(0.7);

        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.CENTER);
    }

    private void analyze() {
        tokenModel.setRowCount(0);
        symbolModel.setRowCount(0);
        errorModel.setRowCount(0);

        LexerResult result = lexer.analyze(inputArea.getText());

        int i = 1;
        for (Token t : result.getTokens()) {
            tokenModel.addRow(new Object[]{i++, t.getType(), t.getLexeme(), t.getLine(), t.getColumn()});
        }

        for (SymbolEntry s : result.getSymbols()) {
            symbolModel.addRow(new Object[]{s.getName(), s.getKind(), s.getFirstLine()});
        }

        for (LexError err : result.getErrors()) {
            errorModel.addRow(new Object[]{err.getMessage(), err.getLine(), err.getColumn()});
        }
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> new LexerGUI().setVisible(true));
    }
}
