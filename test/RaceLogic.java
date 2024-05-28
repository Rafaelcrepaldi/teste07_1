import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

public class RaceLogic {
    private final MainFrame mainFrame;
    private Timer raceTimer;
    private long startTime;
    private boolean isTimerRunning;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/aps1?zeroDateTimeBehavior=CONVERT_TO_NULL";
    private static final String DB_USER = "aps1";
    private static final String DB_PASSWORD = "1234";

    public RaceLogic(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public void resetRace() {
        if (raceTimer != null) {
            raceTimer.cancel();
        }
        startTime = System.currentTimeMillis();
        isTimerRunning = true;

        raceTimer = new Timer();
        raceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTimerLabel();
            }
        }, 0, 10);

        mainFrame.getTableModelYellow().setRowCount(0);
        mainFrame.getTableModelGreen().setRowCount(0);
    }

    public void pauseOrContinueTimer() {
        if (isTimerRunning) {
            raceTimer.cancel();
            isTimerRunning = false;
        } else {
            isTimerRunning = true;
            long pausedTime = System.currentTimeMillis() - startTime;

            raceTimer = new Timer();
            raceTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateTimerLabel();
                }
            }, 0, 10);
        }
    }

    public void updateRaceInfoLabel() {
        StringBuilder raceInfo = new StringBuilder();
        raceInfo.append("Voltas Carro Amarelo:\n");
        for (int i = 0; i < mainFrame.getTableModelYellow().getRowCount(); i++) {
            raceInfo.append(mainFrame.getTableModelYellow().getValueAt(i, 0)).append(", ");
        }
        raceInfo.append("\nVoltas Carro Verde:\n");
        for (int i = 0; i < mainFrame.getTableModelGreen().getRowCount(); i++) {
            raceInfo.append(mainFrame.getTableModelGreen().getValueAt(i, 0)).append(", ");
        }
        JOptionPane.showMessageDialog(mainFrame, raceInfo.toString(), "Informações da Corrida", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateTimerLabel() {
        long elapsed = System.currentTimeMillis() - startTime;
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss:SSS");
        mainFrame.getTimerLabel().setText(sdf.format(elapsed));
    }

    private void logRaceData(String car, int lap, long elapsed) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new java.util.Date());

        DefaultTableModel tableModel = car.equals("Yellow") ? mainFrame.getTableModelYellow() : mainFrame.getTableModelGreen();
        tableModel.addRow(new Object[]{lap, timestamp, elapsed, "Distância Percorrida"});

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO informacoes_" + car + " (volta, data_hora, tempo_corrida, distancia_percorrida) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, lap);
                pstmt.setString(2, timestamp);
                pstmt.setLong(3, elapsed);
                pstmt.setString(4, "Distância Percorrida");
                pstmt.executeUpdate();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(mainFrame, "Erro ao salvar os dados da corrida: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
