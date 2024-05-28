import org.opencv.core.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private CameraPanel cameraPanel;
    private final RaceLogic raceLogic;
    private JLabel timerLabel;
    private DefaultTableModel tableModelYellow;
    private DefaultTableModel tableModelGreen;
    private JTable tableYellow;
    private JTable tableGreen;
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/aps1?zeroDateTimeBehavior=CONVERT_TO_NULL";
    private static final String DB_USER = "aps1";
    private static final String DB_PASSWORD = "1234";

    public MainFrame() {
        initComponents();
        raceLogic = new RaceLogic(this);
    }

    private void initComponents() {
        GroupLayout layout = new GroupLayout(getContentPane());
        setLayout(layout);

        cameraPanel = new CameraPanel();
        JScrollPane cameraScrollPane = new JScrollPane(cameraPanel);

        JPanel controlPanel = createControlPanel();

        tableModelYellow = new DefaultTableModel(new String[]{"Volta", "Data e Hora", "Tempo da Corrida", "Distância Percorrida (cm)"}, 0);
        tableYellow = new JTable(tableModelYellow);
        JScrollPane scrollPaneYellow = new JScrollPane(tableYellow);

        tableModelGreen = new DefaultTableModel(new String[]{"Volta", "Data e Hora", "Tempo da Corrida", "Distância Percorrida (cm)"}, 0);
        tableGreen = new JTable(tableModelGreen);
        JScrollPane scrollPaneGreen = new JScrollPane(tableGreen);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addComponent(cameraScrollPane)
                        .addComponent(controlPanel)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(scrollPaneYellow)
                                .addComponent(scrollPaneGreen)
                        )
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(cameraScrollPane)
                        .addComponent(controlPanel)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(scrollPaneYellow)
                                .addComponent(scrollPaneGreen)
                        )
        );

        setSize(new Dimension(800, 965));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        GroupLayout controlLayout = new GroupLayout(controlPanel);
        controlPanel.setLayout(controlLayout);

        timerLabel = new JLabel("00:00:000");
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JButton startButton = new JButton("Iniciar");
        startButton.addActionListener((ActionEvent e) -> raceLogic.resetRace());

        JButton pauseButton = new JButton("Pausar / Retomar");
        pauseButton.addActionListener((ActionEvent e) -> raceLogic.pauseOrContinueTimer());

        JButton raceInfoButton = new JButton("Informações da Corrida");
        raceInfoButton.addActionListener((ActionEvent e) -> raceLogic.updateRaceInfoLabel());

        JButton moreInfoButton = new JButton("Mostrar Tabelas");
        moreInfoButton.addActionListener((ActionEvent e) -> showTablesInfo());

        JButton clearTablesButton = new JButton("Limpar Tabelas");
        clearTablesButton.addActionListener((ActionEvent e) -> clearTables());

        controlLayout.setAutoCreateGaps(true);
        controlLayout.setAutoCreateContainerGaps(true);

        controlLayout.setHorizontalGroup(
                controlLayout.createSequentialGroup()
                        .addComponent(timerLabel)
                        .addComponent(startButton)
                        .addComponent(pauseButton)
                        .addComponent(raceInfoButton)
                        .addComponent(moreInfoButton)
                        .addComponent(clearTablesButton)
        );

        controlLayout.setVerticalGroup(
                controlLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(timerLabel)
                        .addComponent(startButton)
                        .addComponent(pauseButton)
                        .addComponent(raceInfoButton)
                        .addComponent(moreInfoButton)
                        .addComponent(clearTablesButton)
        );

        return controlPanel;
    }

    private void showTablesInfo() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Não foi possível carregar o driver JDBC");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet tables = metaData.getTables(null, null, "%", null)) {
                List<String> tableNames = new ArrayList<>();
                while (tables.next()) {
                    String tableName = tables.getString(3);
                    if (tableName.toLowerCase().startsWith("tabela") || tableName.toLowerCase().startsWith("informacoes")) {
                        tableNames.add(tableName);
                    }
                }

                JFrame tableListFrame = new JFrame("Lista de Tabelas");
                tableListFrame.setSize(300, 200);
                tableListFrame.setLocationRelativeTo(null);
                tableListFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                JPanel panel = new JPanel();
                panel.setLayout(new GridLayout(tableNames.size(), 1));

                for (String tableName : tableNames) {
                    JButton button = new JButton(tableName);
                    button.addActionListener((ActionEvent e) -> showTableContent(tableName));
                    panel.add(button);
                }

                JScrollPane scrollPane = new JScrollPane(panel);
                tableListFrame.add(scrollPane);

                tableListFrame.setVisible(true);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao acessar o banco de dados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTableContent(String tableName) {
        JFrame frame = new JFrame("Conteúdo da Tabela: " + tableName);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        DefaultTableModel tableModel = new DefaultTableModel();
        JTable table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM " + tableName;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                ResultSet resultSet = pstmt.executeQuery();
                ResultSetMetaData metaData = resultSet.getMetaData();

                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    tableModel.addColumn(metaData.getColumnName(i));
                }

                while (resultSet.next()) {
                    Object[] rowData = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        rowData[i - 1] = resultSet.getObject(i);
                    }
                    tableModel.addRow(rowData);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao acessar o banco de dados: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }

        frame.add(panel);
        frame.setVisible(true);
    }

    private void clearTables() {
        tableModelYellow.setRowCount(0);
        tableModelGreen.setRowCount(0);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Não foi possível carregar o driver JDBC");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet tables = metaData.getTables(null, null, "%", null)) {
                List<String> tableNames = new ArrayList<>();
                while (tables.next()) {
                    String tableName = tables.getString(3);
                    if (tableName.toLowerCase().startsWith("tabela") || tableName.toLowerCase().startsWith("informacoes")) {
                        tableNames.add(tableName);
                    }
                }

                for (String tableName : tableNames) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DROP TABLE IF EXISTS " + tableName);
                    }
                }

                JOptionPane.showMessageDialog(this, "Tabelas excluídas com sucesso.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir tabelas: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public JLabel getTimerLabel() {
        return timerLabel;
    }

    public DefaultTableModel getTableModelYellow() {
        return tableModelYellow;
    }

    public DefaultTableModel getTableModelGreen() {
        return tableModelGreen;
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
