Modelo
Es la representación de datos. Su función es agarrar información de cada equipo detectado en la red. No tiene codigo.
ip (String): Dirección ip del dispositivo.
nombreHost (String): Nombre de red resuelto por dns (o mensaje de error si es que lo hay).
conectado (boolean): Estado de conectividad (true si respondio al ping o false si no responde).
tiempoRespuestaMs (long): Cuanto ttiemppo de latencia hay entre las redes.
Responsabilidad: Sirve como estructura de paso de datos entre los servicios de escaneo y la tabla de la interfaz grafica.

Vista
Es la interfaz grafica que interactua con el usuario. Se encarga de construir la ventana para hacer el ping y mostrar los resultados tabulados.
Componentes de entrada
JTextField para txtIpInicio, txtIpFin, txtTimeout y txtReintentos.
Componentes de resultado:
JTable comunicada con DefaultTableModel para mostrar las filas. La columna "Activo" esta configurada para renderizarse como un JCheckBox.
JProgressBar que refleja el porcentaje del rango escaneado.
JLabel para mostrar la cantidad de "Equipos activos".
Componentes de accion:
JButton para las acciones: Iniciar escaneo, Detener escaneo, Limpiar, Guardar resultados y Mostrar solo activos.

Controlador
Representa la logica y la comunicacion entre la Vista y el Modelo. Captura las pteiciones del usuario, procesa comandos de red y actualiza la interfaz.

NetworkUtils.java:
validarIP(): Aplica expresiones y conversiones para verificar la sintaxis de las IP.
generarRangoIPs(): Transforma las IP de inicio y fin a valores numéricos (long) para iterar y generar la lista completa de direcciones del rango.

ScannerService.java (Servicio de Red):
escanearIP(): Utiliza InetAddress.isReachable() para hacer el ping y getCanonicalHostName() para el DNS. Retorna una instancia poblada de Dispositivo.
