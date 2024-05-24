import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import static org.opencv.core.Core.*;
import static org.opencv.imgproc.Imgproc.*;

public class main extends JFrame {
    private CameraPanel cameraPanel;
    private VideoCapture capture;
    private boolean yellowCrossed = false;
    private boolean greenCrossed = false;
    private Timer yellowCrossingDelayTimer = null;
    private Timer greenCrossingDelayTimer = null;
    private DefaultTableModel tableModelYellow;
    private DefaultTableModel tableModelGreen;
    private JTable tableYellow;
    private JTable tableGreen;
    private int yellowLaps = 1;
    private int greenLaps = 1;
    private int totalYellowLaps = 0;
    private int totalGreenLaps = 0;
    private long totalRaceTime = 0;
    private long startedTime = 0;
    private long pausedTime = 0;
    private JLabel timerLabel;
    private JButton startButton;
    private JButton pauseButton;
    private static final boolean isRunning = true;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/aps1?zeroDateTimeBehavior=CONVERT_TO_NULL";
    private static final String DB_USER = "aps1";
    private static final String DB_PASSWORD = "1234";
    
    public main() {
        initComponents();
        yellowCrossingDelayTimer = new Timer(500, (ActionEvent e) -> {
            yellowCrossed = false;
            yellowCrossingDelayTimer.stop();
        });
        greenCrossingDelayTimer = new Timer(500, (ActionEvent e) -> {
            greenCrossed = false;
            greenCrossingDelayTimer.stop();
        });
        setupTimerLabel();
    }

    private void initComponents() {
        GroupLayout layout = new GroupLayout(getContentPane());
        setLayout(layout);

        cameraPanel = new CameraPanel();
        JScrollPane cameraScrollPane = new JScrollPane(cameraPanel);

        JPanel controlPanel = new JPanel();
        GroupLayout controlLayout = new GroupLayout(controlPanel);
        controlPanel.setLayout(controlLayout);

        timerLabel = new JLabel("00:00:000");

        startButton = new JButton("Iniciar");
        startButton.addActionListener((ActionEvent e) -> {
            resetRace();
            startTimer();
        });

        pauseButton = new JButton("Pausar / Retomar");
        pauseButton.addActionListener((ActionEvent e) -> pauseOrContinueTimer());

        JButton raceInfoButton = new JButton("Informações da Corrida");
        raceInfoButton.addActionListener((ActionEvent e) -> updateRaceInfoLabel());
        
        JButton moreInfoButton = new JButton("Mostrar Tabelas");
        moreInfoButton.addActionListener((ActionEvent e) -> showTablesInfo());
        
        JButton clearTablesButton = new JButton("limpar tabela");
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

        tableModelYellow = new DefaultTableModel();
        tableModelYellow.addColumn("Volta");
        tableModelYellow.addColumn("Data e Hora");
        tableModelYellow.addColumn("Tempo da Corrida");
        tableModelYellow.addColumn("Distância Percorrida (cm)"); // Adicionando a coluna de distância percorrida
        tableYellow = new JTable(tableModelYellow);
        JScrollPane scrollPaneYellow = new JScrollPane(tableYellow);

        tableModelGreen = new DefaultTableModel();
        tableModelGreen.addColumn("Volta");
        tableModelGreen.addColumn("Data e Hora");
        tableModelGreen.addColumn("Tempo da Corrida");
        tableModelGreen.addColumn("Distância Percorrida (cm)"); // Adicionando a coluna de distância percorrida
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
    }
}
    private void clearTables() {
    // Limpa a tabela presente no aplicativo
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
                String query = "DROP TABLE IF EXISTS " + tableName;
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(query);
                }
            }

            JOptionPane.showMessageDialog(this, "Tabelas excluídas com sucesso.");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Erro ao excluir tabelas: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
}


    private void dropTable(Connection conn, String tableName) throws SQLException {
        String query = "DROP TABLE " + tableName;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
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

                // Adiciona as colunas à tabela
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    tableModel.addColumn(metaData.getColumnName(i));
                }

                // Adiciona as linhas à tabela
                while (resultSet.next()) {
                    Object[] rowData = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        rowData[i - 1] = resultSet.getObject(i);
                    }
                    tableModel.addRow(rowData);
                }
            }
        } catch (SQLException ex) {
        }

        frame.add(panel);
        frame.setVisible(true);
    }

public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
    ResultSetMetaData metaData = rs.getMetaData();

    // Names of columns
    Vector<String> columnNames = new Vector<>();
    int columnCount = metaData.getColumnCount();
    for (int column = 1; column <= columnCount; column++) {
        columnNames.add(metaData.getColumnName(column));
    }

    // Data of the table
    Vector<Vector<Object>> data = new Vector<>();
    while (rs.next()) {
        Vector<Object> vector = new Vector<>();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            vector.add(rs.getObject(columnIndex));
        }
        data.add(vector);
    }

    return new DefaultTableModel(data, columnNames);
}


    private void setupTimerLabel() {
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
    }

    private void startTimer() {
        if (startedTime == 0) {
            startedTime = System.currentTimeMillis();
            Timer timer = new Timer(1, (ActionEvent e) -> updateTimerLabel());
            timer.start();
        }
    }

    private void updateTimerLabel() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startedTime;
        if (pausedTime != 0) {
            elapsedTime -= (currentTime - pausedTime);
        }
        timerLabel.setText(formatTime(elapsedTime));
    }

    private void resetTimer() {
    startedTime = 0;
    pausedTime = 0;
    totalRaceTime = 0;
    startButton.setText("Iniciar Nova Corrida");
}

    private void resetRace() {
        resetTimer();
        yellowLaps = 1;
        greenLaps = 1;
        tableModelYellow.setRowCount(0);
        tableModelGreen.setRowCount(0);
    }

private void updateRaceInfoLabel() {
    long minutes = totalRaceTime / 1000 / 60;
    long seconds = (totalRaceTime / 1000) % 60;
    double yellowAverageSpeed = calculateAverageSpeed(totalYellowLaps, totalRaceTime);
    double greenAverageSpeed = calculateAverageSpeed(totalGreenLaps, totalRaceTime);

    // Arredondando para 2 casas decimais
    yellowAverageSpeed = Math.round(yellowAverageSpeed * 100.0) / 100.0;
    greenAverageSpeed = Math.round(greenAverageSpeed * 100.0) / 100.0;

    String raceInfo = String.format("<html><body style='width: 300px; padding: 10px;'>" +
            "<h2 style='text-align: center;'>Relatório da Corrida</h2>" +
            "<p><b>Tempo Total:</b> %d:%02d</p>" +
            "<p><b>Voltas Amarelo:</b> %d (%.2f cm)</p>" +
            "<p><b>Voltas Verde:</b> %d (%.2f cm)</p>" +
            "<p><b>Velocidade Média Amarelo:</b> %.2f km/h</p>" +
            "<p><b>Velocidade Média Verde:</b> %.2f km/h</p>",
            minutes, seconds, totalYellowLaps, totalYellowLaps * 146.0,
            totalGreenLaps, totalGreenLaps * 146.0,
            yellowAverageSpeed, greenAverageSpeed);


    JFrame raceInfoFrame = new JFrame("Relatório da Corrida");
    raceInfoFrame.setSize(400, 300);
    raceInfoFrame.setLocationRelativeTo(null);
    raceInfoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());

    JLabel label = new JLabel(raceInfo);
    label.setFont(new Font("Arial", Font.PLAIN, 16));
    panel.add(label, BorderLayout.CENTER);

    JButton backButton = new JButton("Voltar à Página Anterior");
    backButton.addActionListener((ActionEvent e) -> raceInfoFrame.dispose());
    panel.add(backButton, BorderLayout.SOUTH);

    JButton moreInfoButton = new JButton("Mais Informações");
    moreInfoButton.addActionListener((ActionEvent e) -> showMoreInfoDialog());
    panel.add(moreInfoButton, BorderLayout.NORTH);

    JButton uploadButton = new JButton("Enviar para Banco de Dados");
    uploadButton.addActionListener((ActionEvent e) -> uploadRaceInfoToDatabase());
    panel.add(uploadButton, BorderLayout.SOUTH);

    raceInfoFrame.add(panel);

    raceInfoFrame.setVisible(true);
}
private void uploadRaceInfoToDatabase() {
    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
        System.out.println("Não foi possível carregar o driver JDBC");
        return;
    }

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
        String tableName = "informacoes_corrida";
        if (!tableExists(conn, tableName)) {
            createTable(conn, tableName);
        }

        String query = "INSERT INTO " + tableName + " (tempo_total, voltas_amarelo, voltas_verde, velocidade_media_amarelo, velocidade_media_verde) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            long totalTimeSeconds = totalRaceTime / 1000;
            double yellowAverageSpeed = calculateAverageSpeed(totalYellowLaps, totalRaceTime);
            double greenAverageSpeed = calculateAverageSpeed(totalGreenLaps, totalRaceTime);

            pstmt.setLong(1, totalTimeSeconds);
            pstmt.setInt(2, totalYellowLaps);
            pstmt.setInt(3, totalGreenLaps);
            pstmt.setDouble(4, yellowAverageSpeed);
            pstmt.setDouble(5, greenAverageSpeed);

            pstmt.executeUpdate();
        }
    } catch (SQLException e) {
    }
}


private double calculateAverageSpeed(int laps, long totalTime) {
    if (laps == 0 || totalTime == 0) {
        return 0;
    }
    double totalDistance = laps * 146.0; // Distância total em cm
    double totalDistanceKm = totalDistance / 100000.0; // Convertendo para quilômetros
    double totalTimeHours = totalTime / 3600000.0; // Convertendo para horas
    return totalDistanceKm / totalTimeHours; // Velocidade média em km/h
}



private String getBestLapTime(Color color) {
    DefaultTableModel tableModel = color.equals(Color.YELLOW) ? tableModelYellow : tableModelGreen;
    double bestTime = Double.MAX_VALUE;
    for (int i = 0; i < tableModel.getRowCount(); i++) {
        double time = parseTime((String) tableModel.getValueAt(i, 2));
        if (time < bestTime) {
            bestTime = time;
        }
    }
    return formatTime((long) bestTime);
}


private void showMoreInfoDialog() {
    JFrame moreInfoFrame = new JFrame("Mais Informações da Corrida");
    moreInfoFrame.setSize(800, 600);
    moreInfoFrame.setLocationRelativeTo(null);
    moreInfoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(1, 2, 10, 10));

    JPanel yellowPanel = new JPanel(new BorderLayout());
    JLabel labelYellow = new JLabel("Informações do Carrinho Amarelo");
    labelYellow.setHorizontalAlignment(SwingConstants.CENTER);
    yellowPanel.add(labelYellow, BorderLayout.NORTH);
    yellowPanel.add(new JScrollPane(tableYellow), BorderLayout.CENTER);

    JPanel greenPanel = new JPanel(new BorderLayout());
    JLabel labelGreen = new JLabel("Informações do Carrinho Verde");
    labelGreen.setHorizontalAlignment(SwingConstants.CENTER);
    greenPanel.add(labelGreen, BorderLayout.NORTH);
    greenPanel.add(new JScrollPane(tableGreen), BorderLayout.CENTER);

    panel.add(yellowPanel);
    panel.add(greenPanel);

    moreInfoFrame.add(panel);
    moreInfoFrame.setVisible(true);
}




    private void saveToDatabase(int lap, String timestamp, String formattedTime, java.awt.Color color, double distance) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Não foi possível carregar o driver JDBC");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String tableName = color == java.awt.Color.YELLOW ? "tabela_amarela" : "tabela_verde";
            if (!tableExists(conn, tableName)) {
                createTable(conn, tableName);
            }

            String query = "INSERT INTO " + tableName + " (volta, data_hora, tempo_corrida, distancia) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, lap);
                pstmt.setString(2, timestamp);
                pstmt.setString(3, formattedTime);
                pstmt.setDouble(4, distance);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
        }
    }


    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet resultSet = meta.getTables(null, null, tableName, null)) {
            return resultSet.next();
        }
    }

    private void createTable(Connection conn, String tableName) throws SQLException {
    String query = "";
        switch (tableName) {
            case "tabela_amarela" -> query = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY,"
                        + "volta INT NOT NULL,"
                        + "data_hora DATETIME NOT NULL,"
                        + "tempo_corrida TIME NOT NULL,"
                        + "distancia DOUBLE NOT NULL"
                        + ")";
            case "tabela_verde" -> query = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY,"
                        + "volta INT NOT NULL,"
                        + "data_hora DATETIME NOT NULL,"
                        + "tempo_corrida TIME NOT NULL,"
                        + "distancia DOUBLE NOT NULL"
                        + ")";
            case "informacoes_corrida" -> query = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                        + "tempo_total BIGINT,"
                        + "voltas_amarelo INT,"
                        + "voltas_verde INT,"
                        + "velocidade_media_amarelo DOUBLE,"
                        + "velocidade_media_verde DOUBLE"
                        + ")";
            default -> {
            }
        }

    try (Statement stmt = conn.createStatement()) {
        stmt.executeUpdate(query);
    }
}


   private void saveScreenshotAndUpdateLaps(java.awt.Color color) {
    long currentTime = System.currentTimeMillis();

    if (startedTime != 0) {
        long elapsedTime = currentTime - startedTime;
        if (pausedTime != 0) {
            elapsedTime -= (currentTime - pausedTime);
        }
        totalRaceTime = elapsedTime;

        String formattedTime = formatTime(elapsedTime);

        String timestamp = getCurrentTimestamp();

        DefaultTableModel tableModel = color.equals(java.awt.Color.YELLOW) ? tableModelYellow : tableModelGreen;
        JTable table = color.equals(java.awt.Color.YELLOW) ? tableYellow : tableGreen;
        int lap = color.equals(java.awt.Color.YELLOW) ? yellowLaps++ : greenLaps++;

        double distance = lap * 146.0; // Calculando a distância percorrida

        tableModel.addRow(new Object[]{lap, timestamp, formattedTime, distance});
        saveToDatabase(lap, timestamp, formattedTime, color, distance);

        if (color.equals(java.awt.Color.YELLOW)) {
            totalYellowLaps++;
        } else {
            totalGreenLaps++;
        }
    }
}

private String formatTime(long milliseconds) {
    long seconds = milliseconds / 1000;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    long remainingSeconds = seconds % 60;
    long remainingMilliseconds = milliseconds % 1000;

    // Formata o tempo com zeros à esquerda, se necessário
    return String.format("%02d:%02d:%02d.%03d", hours, minutes, remainingSeconds, remainingMilliseconds);
}




    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private void startCamera() {
        new Thread(() -> {
            capture = new VideoCapture();
            capture.open(1);
            if (!capture.isOpened()) {
                System.out.println("Error: Unable to open camera.");
                return;
            }

            capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 1280);
            capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 720);
            capture.set(Videoio.CAP_PROP_FPS, 30);

            Mat frame = new Mat();
            while (true) {
                capture.read(frame);

                if (!frame.empty()){
                    detectObjects(frame, cameraPanel.getFinishLine());

                    SwingUtilities.invokeLater(() -> {
                        cameraPanel.setFrame(frame);
                        cameraPanel.repaint();
                    });
                }

                if (!isVisible()) {
                    break;
                }
            }
        }).start();
    }

    private void detectObjects(Mat frame, Line2D finishLine) {
        Mat hsvFrame = new Mat();
        cvtColor(frame, hsvFrame, COLOR_BGR2HSV);

        Mat mask1 = new Mat();
        inRange(hsvFrame, new Scalar(20, 100, 100), new Scalar(30, 255, 255), mask1);

        Mat mask2 = new Mat();
        inRange(hsvFrame, new Scalar(40, 100, 100), new Scalar(80, 255, 255), mask2);

        List<MatOfPoint> contours1 = new ArrayList<>();
        List<MatOfPoint> contours2 = new ArrayList<>();
        Mat hierarchy1 = new Mat();
        Mat hierarchy2 = new Mat();
        findContours(mask1, contours1, hierarchy1, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        findContours(mask2, contours2, hierarchy2, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        Rect[] boundingRects1 = new Rect[contours1.size()];
        for (int i = 0; i < contours1.size(); i++) {
            boundingRects1[i] = boundingRect(contours1.get(i));
        }

        Rect[] boundingRects2 = new Rect[contours2.size()];
        for (int i = 0; i < contours2.size(); i++) {
            boundingRects2[i] = boundingRect(contours2.get(i));
        }

        cameraPanel.setDetectedObjects(boundingRects1, boundingRects2);

        checkObjectCrossingLine(boundingRects1, Color.YELLOW, finishLine);
        checkObjectCrossingLine(boundingRects2, Color.GREEN, finishLine);
    }

    private void checkObjectCrossingLine(Rect[] boundingRects, java.awt.Color color, Line2D finishLine) {
    for (Rect rect : boundingRects) {
        if (finishLine != null && finishLine.intersects(rect.x, rect.y, rect.width, rect.height)) {
            if (color.equals(java.awt.Color.YELLOW) && !yellowCrossed) {
                saveScreenshotAndUpdateLaps(java.awt.Color.YELLOW);
                yellowCrossed = true;
                yellowCrossingDelayTimer.restart();
            } else if (color.equals(java.awt.Color.GREEN) && !greenCrossed) {
                saveScreenshotAndUpdateLaps(java.awt.Color.GREEN);
                greenCrossed = true;
                greenCrossingDelayTimer.restart();
            }
            return;
        }
    }
}


    private void pauseOrContinueTimer() {
        if (startedTime != 0) {
            if (pausedTime == 0) {
                pausedTime = System.currentTimeMillis();
                startButton.setText("Continuar");
            } else {
                long pauseDuration = System.currentTimeMillis() - pausedTime;
                startedTime += pauseDuration;
                pausedTime = 0;
                startButton.setText("Reiniciar");
            }
        }
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        SwingUtilities.invokeLater(() -> {
            main camera = new main();
            camera.startCamera();
            camera.setVisible(true);
        });
    }

    private double parseTime(String string) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}

class CameraPanel extends JPanel {

    private Mat frame;
    private Rect[] detectedObjects1;
    private Rect[] detectedObjects2;
    private Point lineStart;
    private Point lineEnd;

    public CameraPanel() {
        setPreferredSize(new Dimension(640, 420));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lineStart = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lineEnd = e.getPoint();
                repaint();
            }
        });
    }

    public void setFrame(Mat frame) {
        this.frame = frame;
    }

    public Scalar getHSVColor(int x, int y) {
        if (frame != null && x >= 0 && x < frame.width() && y >= 0 && y < frame.height()) {
            double[] hsv = frame.get(y, x);
            return new Scalar(hsv[0], hsv[1], hsv[2]);
        }
        return null;
    }

    public void setDetectedObjects(Rect[] detectedObjects1, Rect[] detectedObjects2) {
        this.detectedObjects1 = detectedObjects1;
        this.detectedObjects2 = detectedObjects2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (frame != null) {
            ImageIcon image = new ImageIcon(Utils.matToBufferedImage(frame));
            g.drawImage(image.getImage(), 0, 0, getWidth(), getHeight(), null);

            Graphics2D g2d = (Graphics2D) g;

            int frameWidth = frame.width();
            int frameHeight = frame.height();

            if (lineStart != null && lineEnd != null) {
                g2d.setColor(Color.RED);
                g2d.drawLine(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y);
            }

            if (detectedObjects1 != null) {
                g2d.setColor(Color.YELLOW);
                for (Rect rect : detectedObjects1) {
                    int x = rect.x * getWidth() / frameWidth;
                    int y = rect.y * getHeight() / frameHeight;
                    int width = rect.width * getWidth() / frameWidth;
                    int height = rect.height * getHeight() / frameHeight;
                    g2d.drawRect(x, y, width, height);
                }
            }

            if (detectedObjects2 != null) {
                g2d.setColor(Color.GREEN);
                for (Rect rect : detectedObjects2) {
                    int x = rect.x * getWidth() / frameWidth;
                    int y = rect.y * getHeight() / frameHeight;
                    int width = rect.width * getWidth() / frameWidth;
                    int height = rect.height * getHeight() / frameHeight;
                    g2d.drawRect(x, y, width, height);
                }
            }
        }
    }

    public Line2D getFinishLine() {
        if (lineStart != null && lineEnd != null) {
            return new Line2D.Double(lineStart, lineEnd);
        }
        return null;
    }
}

class Utils {
    public static BufferedImage matToBufferedImage(Mat matrix) {
        int cols = matrix.cols();
        int rows = matrix.rows();
        int elemSize = (int) matrix.elemSize();
        byte[] data = new byte[cols * rows * elemSize];
        int type;
        matrix.get(0, 0, data);

        switch (matrix.channels()) {
            case 1 -> type = BufferedImage.TYPE_BYTE_GRAY;
            case 3 -> {
                type = BufferedImage.TYPE_3BYTE_BGR;
                byte b;
                for (int i = 0; i < data.length; i = i + 3) {
                    b = data[i];
                    data[i] = data[i + 2];
                    data[i + 2] = b;
                }
            }
            default -> {
                return null;
            }
        }

        BufferedImage image2 = new BufferedImage(cols, rows, type);
        image2.getRaster().setDataElements(0, 0, cols, rows, data);
        return image2;
    }

    public static BufferedImage takeScreenshot(Component component) {
        BufferedImage screenshot = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
        component.paint(screenshot.getGraphics());
        return screenshot;
    }
}