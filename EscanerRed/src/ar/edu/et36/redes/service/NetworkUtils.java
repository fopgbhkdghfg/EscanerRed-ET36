package ar.edu.et36.redes.service;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class NetworkUtils {

    public static boolean validarIP(String ip) {
        try {
            String[] partes = ip.split("\\.");
            if (partes.length != 4) return false;
            for (String parte : partes) {
                int valor = Integer.parseInt(parte);
                if (valor < 0 || valor > 255) return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static List<String> generarRangoIPs(String ipInicio, String ipFin) throws Exception {
        List<String> listaIPs = new ArrayList<>();
        long inicio = ipToLong(InetAddress.getByName(ipInicio));
        long fin = ipToLong(InetAddress.getByName(ipFin));

        if (inicio > fin) {
            throw new IllegalArgumentException("La IP de inicio debe ser menor o igual a la IP de fin.");
        }

        for (long i = inicio; i <= fin; i++) {
            listaIPs.add(longToIp(i));
        }
        return listaIPs;
    }

    private static long ipToLong(InetAddress ip) {
        byte[] octetos = ip.getAddress();
        long resultado = 0;
        for (byte octeto : octetos) {
            resultado = (resultado << 8) | (octeto & 0xFF);
        }
        return resultado;
    }

    private static String longToIp(long ip) {
        return String.format("%d.%d.%d.%d",
                (ip >> 24) & 0xFF,
                (ip >> 16) & 0xFF,
                (ip >> 8) & 0xFF,
                ip & 0xFF);
    }
}
