package ar.edu.et36.redes.service;

import ar.edu.et36.redes.model.Dispositivo;
import java.net.InetAddress;

public class ScannerService {
    private final int timeoutMs;

    public ScannerService(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public Dispositivo escanearIP(String ip) {
        long inicio = System.currentTimeMillis();
        try {
            InetAddress address = InetAddress.getByName(ip);
            boolean alcanzable = address.isReachable(timeoutMs);
            long tiempoRespuesta = System.currentTimeMillis() - inicio;

            String hostName = "Desconocido / Sin DNS";
            if (alcanzable) {
                try {
                    hostName = address.getCanonicalHostName();
                } catch (Exception e) {
                    hostName = "Error al resolver DNS";
                }
            }

            return new Dispositivo(ip, hostName, alcanzable, alcanzable ? tiempoRespuesta : 0);
        } catch (Exception e) {
            return new Dispositivo(ip, "N/A", false, 0);
        }
    }
}