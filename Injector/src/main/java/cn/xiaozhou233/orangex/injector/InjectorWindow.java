package cn.xiaozhou233.orangex.injector;

import cn.xiaozhou233.juiceagent.injector.Injector;
import cn.xiaozhou233.juiceagent.injector.WindowInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Injector front-end: lists the running Minecraft windows, optionally accepts a
 * PID typed by hand, and runs {@link Injector#inject(int, String, String)} on a
 * background thread.
 */
public class InjectorWindow extends JFrame {

    private static final String[] WINDOW_TITLES = {
            "Minecraft", "minecraft", "1.8.9", "Badlion", "Lunar"
    };
    private static final String[] IGNORED_TITLES = {
            "Badlion Client", "Badlion Chat", "Home - Lunar Client"
    };

    private static final Color ACCENT = new Color(0xFF8A00);
    private static final Color SUCCESS = new Color(0x66BB6A);
    private static final Color ERROR = new Color(0xEF5350);
    private static final Color MUTED = new Color(0xAAAAAA);

    private final String agentPath;
    private final String configDir;

    private final DefaultComboBoxModel<ProcessEntry> processModel = new DefaultComboBoxModel<>();
    private final JComboBox<ProcessEntry> processBox = new JComboBox<>(processModel);
    private final JButton refreshButton = new JButton("Refresh");
    private final JCheckBox customPidCheck = new JCheckBox("Use custom PID");
    private final JTextField pidField = new JTextField();
    private final JButton injectButton = new JButton("Inject");
    private final JLabel statusLabel = new JLabel("Ready", SwingConstants.CENTER);

    private boolean busy;

    public InjectorWindow(String agentPath, String configDir) {
        super("OrangeX Injector");
        this.agentPath = agentPath;
        this.configDir = configDir;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setContentPane(buildContent());
        setSize(470, 340);
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(injectButton);

        refreshButton.addActionListener(event -> refreshProcesses());
        customPidCheck.addActionListener(event -> {
            boolean custom = customPidCheck.isSelected();
            pidField.setVisible(custom);
            revalidate();
            repaint();
            if (custom) {
                pidField.requestFocusInWindow();
            }
            updateControls();
        });
        injectButton.addActionListener(event -> inject());

        refreshProcesses();
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(new EmptyBorder(20, 26, 16, 26));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        JLabel title = new JLabel(
                "<html><font color='#FF8A00'><b>OrangeX</b></font> Injector</html>",
                SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 20, 0);
        root.add(title, gbc);

        processBox.setPreferredSize(new Dimension(200, 34));
        JPanel processRow = new JPanel(new BorderLayout(8, 0));
        processRow.add(processBox, BorderLayout.CENTER);
        processRow.add(refreshButton, BorderLayout.EAST);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 10, 0);
        root.add(processRow, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 8, 0);
        root.add(customPidCheck, gbc);

        pidField.setPreferredSize(new Dimension(0, 34));
        pidField.putClientProperty("JTextField.placeholderText", "Enter a process ID");
        pidField.setVisible(false);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 10, 0);
        root.add(pidField, gbc);

        injectButton.setPreferredSize(new Dimension(0, 40));
        injectButton.setFont(injectButton.getFont().deriveFont(Font.BOLD, 15f));
        injectButton.setBackground(ACCENT);
        injectButton.setForeground(Color.WHITE);
        injectButton.putClientProperty("JButton.buttonType", "roundRect");
        gbc.gridy = row++;
        gbc.insets = new Insets(10, 0, 12, 0);
        root.add(injectButton, gbc);

        statusLabel.setFont(statusLabel.getFont().deriveFont(12f));
        statusLabel.setForeground(MUTED);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 0, 0);
        root.add(statusLabel, gbc);

        // Keeps the content aligned to the top when the PID field is hidden.
        gbc.gridy = row;
        gbc.weighty = 1.0;
        root.add(Box.createVerticalGlue(), gbc);

        return root;
    }

    /**
     * Rescans the open windows and fills the selection box. Errors are shown
     * in the status label instead of terminating the window.
     */
    public void refreshProcesses() {
        processModel.removeAllElements();

        List<ProcessEntry> processes;
        try {
            processes = scanProcesses();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            setStatus("Failed to scan windows: " + throwable.getClass().getSimpleName(), ERROR);
            updateControls();
            return;
        }

        for (ProcessEntry process : processes) {
            processModel.addElement(process);
        }

        if (processes.isEmpty()) {
            setStatus("No Minecraft process found", ERROR);
        } else {
            setStatus(processes.size() + " Minecraft process(es) found", MUTED);
        }
        updateControls();
    }

    private List<ProcessEntry> scanProcesses() {
        Map<Integer, ProcessEntry> found = new LinkedHashMap<>();
        for (String title : WINDOW_TITLES) {
            WindowInfo[] windows = Injector.findWindowsByTitle(title);
            if (windows == null) {
                continue;
            }
            for (WindowInfo window : windows) {
                if (window == null || window.title == null || found.containsKey(window.pid)) {
                    continue;
                }
                if (isIgnored(window.title)) {
                    continue;
                }
                found.put(window.pid, new ProcessEntry(window.pid, window.title));
            }
        }
        return new ArrayList<>(found.values());
    }

    private static boolean isIgnored(String title) {
        for (String ignored : IGNORED_TITLES) {
            if (title.contains(ignored)) {
                return true;
            }
        }
        return false;
    }

    private void inject() {
        Integer pid = selectedPid();
        if (pid == null) {
            return;
        }

        busy = true;
        updateControls();
        setStatus("Injecting into PID " + pid + "...", MUTED);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return Injector.inject(pid, agentPath, configDir);
            }

            @Override
            protected void done() {
                busy = false;
                updateControls();
                try {
                    if (Boolean.TRUE.equals(get())) {
                        setStatus("Injection succeeded (PID " + pid + ")", SUCCESS);
                    } else {
                        setStatus("Injection failed (PID " + pid + ")", ERROR);
                        JOptionPane.showMessageDialog(
                                InjectorWindow.this,
                                "Injector.inject returned false for PID " + pid + ".",
                                "Injection failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    setStatus("Injection interrupted", ERROR);
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause() == null
                            ? exception : exception.getCause();
                    cause.printStackTrace();
                    setStatus("Injection failed (PID " + pid + ")", ERROR);
                    JOptionPane.showMessageDialog(
                            InjectorWindow.this,
                            cause.toString(),
                            "Injection failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private Integer selectedPid() {
        if (customPidCheck.isSelected()) {
            String text = pidField.getText().trim();
            if (text.isEmpty()) {
                showError("Enter a PID first, or untick \"Use custom PID\".");
                return null;
            }
            try {
                int pid = Integer.parseInt(text);
                if (pid <= 0) {
                    throw new NumberFormatException("PID must be positive");
                }
                return pid;
            } catch (NumberFormatException exception) {
                showError("PID must be a positive integer.");
                return null;
            }
        }

        ProcessEntry selected = (ProcessEntry) processBox.getSelectedItem();
        if (selected == null) {
            showError("No Minecraft process selected. Start the game or use a custom PID.");
            return null;
        }
        return selected.pid;
    }

    private void updateControls() {
        boolean custom = customPidCheck.isSelected();
        boolean hasProcess = processModel.getSize() > 0;

        processBox.setEnabled(!busy && !custom && hasProcess);
        refreshButton.setEnabled(!busy && !custom);
        customPidCheck.setEnabled(!busy);
        pidField.setEnabled(!busy);
        injectButton.setEnabled(!busy && (custom || hasProcess));
    }

    private void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    private void showError(String message) {
        setStatus(message, ERROR);
        JOptionPane.showMessageDialog(
                this, message, "OrangeX Injector", JOptionPane.WARNING_MESSAGE);
    }

    private static final class ProcessEntry {
        private final int pid;
        private final String title;

        private ProcessEntry(int pid, String title) {
            this.pid = pid;
            this.title = title;
        }

        @Override
        public String toString() {
            return title + "  (PID: " + pid + ")";
        }
    }
}
