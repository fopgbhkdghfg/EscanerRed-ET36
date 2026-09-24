package ar.edu.et36.redes.view;

import ar.edu.et36.redes.model.Dispositivo;
import ar.edu.et36.redes.service.NetworkUtils;
import ar.edu.et36.redes.service.ScannerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.PrintWriter;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private JTextField txtIpInicio, txtIpFin, txtTimeout, txtReintentos;
    private JButton btnIniciar, btnDetener, btnLimpiar, btnGuardar, btnMostrarActivos;
    private JProgressBar progressBar;
    private JTable tablaResultados;
    private DefaultTableModel modelTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblEquiposActivos;

    private SwingWorker<Void, Dispositivo> workerActual;
    private int equiposActivosCount = 0;
    private boolean filtradoActivos = false;

    public VentanaPrincipal() {
        setTitle("Escáner de Red");
        setSize(750, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        // --- PANEL SUPERIOR: FORMULARIO ---
        JPanel panelCampos = new JPanel(new GridLayout(4, 2, 5, 5));
        panelCampos.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        panelCampos.add(new JLabel("IP de inicio:"));
        txtIpInicio = new JTextField("10.160.7.223");
        panelCampos.add(txtIpInicio);

        panelCampos.add(new JLabel("IP de fin:"));
        txtIpFin = new JTextField("10.160.7.233");
        panelCampos.add(txtIpFin);

        panelCampos.add(new JLabel("Tiempo de espera (ms):"));
        txtTimeout = new JTextField("1000");
        panelCampos.add(txtTimeout);

        panelCampos.add(new JLabel("Número de reintentos:"));
        txtReintentos = new JTextField("1");
        panelCampos.add(txtReintentos);

        add(panelCampos, BorderLayout.NORTH);

        String[] columnas = {"IP", "Nombre equipo", "Activo", "Tiempo (ms)"};
        
        modelTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Boolean.class; 
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaResultados = new JTable(modelTabla);
        sorter = new TableRowSorter<>(modelTabla);
        tablaResultados.setRowSorter(sorter);

        add(new JScrollPane(tablaResultados), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Listo");

        lblEquiposActivos = new JLabel("Equipos activos: 0");
        lblEquiposActivos.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblEquiposActivos.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        btnIniciar = new JButton("Iniciar escaneo");
        btnDetener = new JButton("Detener escaneo");
        btnLimpiar = new JButton("Limpiar");
        btnGuardar = new JButton("Guardar resultados");
        btnMostrarActivos = new JButton("Mostrar solo activos");

        btnDetener.setEnabled(false);

        panelBotones.add(btnIniciar);
        panelBotones.add(btnDetener);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnMostrarActivos);

        panelInferior.add(progressBar);
        panelInferior.add(lblEquiposActivos);
        panelInferior.add(panelBotones);

        add(panelInferior, BorderLayout.SOUTH);

        btnIniciar.addActionListener(e -> ejecutarEscaneo());
        btnDetener.addActionListener(e -> detenerEscaneo());
        btnLimpiar.addActionListener(e -> limpiarTodo());
        btnGuardar.addActionListener(e -> guardarResultados());
        btnMostrarActivos.addActionListener(e -> alternarFiltroActivos());
    }

    private void ejecutarEscaneo() {
        String ipIni = txtIpInicio.getText().trim();
        String ipFin = txtIpFin.getText().trim();

        if (!NetworkUtils.validarIP(ipIni) || !NetworkUtils.validarIP(ipFin)) {
            JOptionPane.showMessageDialog(this, "Las direcciones IP ingresadas no son válidas.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int timeout = Integer.parseInt(txtTimeout.getText().trim());
            int reintentos = Integer.parseInt(txtReintentos.getText().trim());
            List<String> listaIPs = NetworkUtils.generarRangoIPs(ipIni, ipFin);

            limpiarTabla();
            btnIniciar.setEnabled(false);
            btnDetener.setEnabled(true);
            equiposActivosCount = 0;
            lblEquiposActivos.setText("Equipos activos: 0");
            progressBar.setString("Escaneando...");

            workerActual = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    ScannerService service = new ScannerService(timeout);
                    int total = listaIPs.size();

                    for (int i = 0; i < total; i++) {
                        if (isCancelled()) break;

                        String ip = listaIPs.get(i);
                        Dispositivo dev = null;

                        for (int r = 0; r < reintentos; r++) {
                            dev = service.escanearIP(ip);
                            if (dev.isConectado()) break;
                        }

                        publish(dev);
                        setProgress((int) (((i + 1) / (float) total) * 100));
                    }
                    return null;
                }

                @Override
                protected void process(List<Dispositivo> chunks) {
                    for (Dispositivo dev : chunks) {
                        if (dev.isConectado()) equiposActivosCount++;

                        modelTabla.addRow(new Object[]{
                                dev.getIp(),
                                dev.getNombreHost(),
                                dev.isConectado(), 
                                dev.isConectado() ? dev.getTiempoRespuestaMs() : ""
                        });
                    }
                    lblEquiposActivos.setText("Equipos activos: " + equiposActivosCount);
                    progressBar.setValue(getProgress());
                }

                @Override
                protected void done() {
                    btnIniciar.setEnabled(true);
                    btnDetener.setEnabled(false);
                    if (isCancelled()) {
                        progressBar.setString("Escaneo detenido");
                    } else {
                        progressBar.setString("Escaneo finalizado");
                    }
                }
            };

            workerActual.execute();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en los parametros ingresados.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void detenerEscaneo() {
        if (workerActual != null && !workerActual.isDone()) {
            workerActual.cancel(true);
        }
    }

    private void limpiarTodo() {
        limpiarTabla();
        progressBar.setValue(0);
        progressBar.setString("Listo");
        lblEquiposActivos.setText("Equipos activos: 0");
        if (filtradoActivos) alternarFiltroActivos();
    }

    private void limpiarTabla() {
        modelTabla.setRowCount(0);
    }

    private void alternarFiltroActivos() {
        if (!filtradoActivos) {
            sorter.setRowFilter(RowFilter.regexFilter("true", 2));
            btnMostrarActivos.setText("Mostrar todos");
            filtradoActivos = true;
        } else {
            sorter.setRowFilter(null);
            btnMostrarActivos.setText("Mostrar solo activos");
            filtradoActivos = false;
        }
    }

    private void guardarResultados() {
        if (modelTabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos para guardar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile() + ".txt")) {
                for (int i = 0; i < modelTabla.getRowCount(); i++) {
                    writer.printf("%s | %s | %s | %s%n",
                            modelTabla.getValueAt(i, 0),
                            modelTabla.getValueAt(i, 1),
                            (boolean) modelTabla.getValueAt(i, 2) ? "Activo" : "Inactivo",
                            modelTabla.getValueAt(i, 3));
                }
                JOptionPane.showMessageDialog(this, "Resultados guardados con éxito.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
