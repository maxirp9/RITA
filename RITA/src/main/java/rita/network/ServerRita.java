package rita.network;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import rita.battle.BatallaBin;
import rita.settings.Settings;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;

/**
 * @author pvilaltella
 * 
 */
public class ServerRita extends Thread {

	// Variable para el singleton
	private static ServerRita instance = null;
	//private ServerRita(){}
	
	// Puerto por defecto para escuchar las peticiones
	private final static int DEFAULT_PORT_NUMBER = 1234;
	// Cantidad maxima de conexiones que acepta el servidor
	private final static int MAX_CONNECTIONS = 10;
	// Maximo tiempo de espera de la conexion
	private final static int TIMEOUT = 300000;
	// Puerto donde se escuchan los pedido de conexiones
	private int portNumber;
	// Se pone en verdadero cuando el servidor se debe apagar
	private boolean shutDownFlag = false;
	// Cantidad de conexiones activas
	private int activeConnectionCount = 0;
	// Flag de inicio de batalla
	private boolean iniciarBatalla = false;
	// Valor de la ip donde corre el servidor
	private String ip;

	private Mensajes mensajes;
	private ClientesConectadosObservable clientesConectadosObservable;

	private Logger log = Logger.getLogger(ServerRita.class);
	private String textoLog;

	private ServerSocket serverSocket;
	private boolean start = false;
	
	private ArrayList<String> robotsEnBatalla;
	
	static String directorioRobocodeLibs = Settings.getInstallPath() + "lib";
	private LogRitaObservable logRitaObservable;

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	public static ServerRita getInstance(int port, ClientesConectadosObservable clientes) {
		if (instance == null) {
			instance = new ServerRita(port, clientes);
		}
		return instance;
	}

	/**
	 * Constructor Servidor RITA
	 */
	private ServerRita() {
		// this(DEFAULT_PORT_NUMBER, cantidad);
		portNumber = DEFAULT_PORT_NUMBER;
		mensajes = new Mensajes();
		setLogRitaObsevable(new LogRitaObservable());
	}

	/**
	 * Constructor Servidor RITA definiendo el puerto
	 * 
	 * @param defaultPortNumber
	 */
	private ServerRita(int defaultPortNumber, ClientesConectadosObservable clientes) {
		initialize(defaultPortNumber,clientes);
	}

	private void initialize(int defaultPortNumber, ClientesConectadosObservable clientes) {
		portNumber = defaultPortNumber;
		clientesConectadosObservable = clientes;
		mensajes = new Mensajes();
		robotsEnBatalla = new ArrayList<String>();
		setLogRitaObsevable(new LogRitaObservable());

	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public boolean isShutDownFlag() {
		return shutDownFlag;
	}

	public void setShutDownFlag(boolean shutDownFlag) {
		this.shutDownFlag = shutDownFlag;
	}

	public int getActiveConnectionCount() {
		return activeConnectionCount;
	}

	public void setActiveConnectionCount(int activeConnectionCount) {
		this.activeConnectionCount = activeConnectionCount;
	}

	public void run() {

		// Carga el archivo de configuracion de log4J
		PropertyConfigurator.configure("log4j.properties");
		// Mensajes mensajes = new Mensajes();
		Socket socket = null;
		createServerSocket();
		setIp(serverSocket.getInetAddress().toString());

		// While para mantener el servidor activo hasta el envio del apagado
		while (!shutDownFlag) {
			// Esperando las conexiones nuevas
			try {

				while (true && (activeConnectionCount < MAX_CONNECTIONS)
						&& !iniciarBatalla) {
					try {
						String texto = "Servidor a la espera de conexiones.";
						log.info(texto);
						setTextoLog(texto);
						socket = serverSocket.accept(); // Aceptando las
														// conexiones
					} catch (InterruptedIOException e) {
						log.error("Error: " + e.getMessage());
					} // try

					if (!iniciarBatalla) {
						// SUMO SOLO LOS CONECTADOS
						//setActiveConnectionCount(activeConnectionCount + 1);
						String texto = "Cliente con la IP "
								+ socket.getInetAddress().getHostAddress()
								+ " conectado.";
						log.info(texto);
						setTextoLog(texto);
						// Crea el objeto worker para procesar las conexiones
						ServerWorkerRita serverWorkerRita = new ServerWorkerRita(
								socket, mensajes, clientesConectadosObservable, this);
						serverWorkerRita.start();
					}

				} // end del While
				while (mensajes.getCantidadRobots() < activeConnectionCount) {
					// A la espera de todos los robots
					try {
						Thread.currentThread();
						log.info("El Server esperando a los ROBOTS..."
								+ mensajes.getCantidadRobots() + " de "
								+ activeConnectionCount);
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				executeBattle();
				// Espero que los workers envien los archivos para reiniciar el
				// servidor
				while (mensajes.getCantidadConexiones() < activeConnectionCount) {
					try {
						log.info("El Server esperando a los hilos..."
								+ mensajes.getCantidadConexiones() + " de "
								+ activeConnectionCount);
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// Ejecuto Robocode en el Servidor
				this.ejecutarRobocode();
				// Pongo en false para queden esperando nuevamente los hilos
				closeServerSocket();
				createServerSocket();
				reiniciarVariables();
				// NO debería cerrar las conexiones hasta que no se envien todos
				// los mensajes
			} catch (IOException e) {
				log.error("Error de input");
			}
		}// Cierre while del cierre del servidor

	}
	
	public void ejecutarRobocode() {		
		
		String cmd = "java -Xmx512M -Dsun.io.useCanonCaches=false -cp " + directorioRobocodeLibs + File.separator + "robocode.jar robocode.Robocode -replay " + Settings.getBinaryPath() + File.separator + "batalla.copia.bin" + " -tps 25";
		log.error(cmd);
		log.info("Se ejecuta Robocode en el Servidor");
		EjecutarComando comando = new EjecutarComando(cmd);		
		
	}

	private void reiniciarVariables() {
		// TODO Auto-generated method stub

		mensajes.setGeneroBin(false);
		setActiveConnectionCount(0);
		mensajes.setCantidadRobots(0);
		mensajes.setCantidadConexiones(0);
		setIniciarBatalla(false);
		// BatallaBin.borrarArchivoBatalla();
		mensajes.getRobotsEnBatalla().clear();
		robotsEnBatalla.clear();
		//AVISO QUE CAMBIO TERMINO LA EJECUCION PARA LIMPIAR LOS ROBOTS EN PANTALLA
		clientesConectadosObservable.changeData(robotsEnBatalla);

	}

	private void createServerSocket() {
		try {
			// Creo el ServerSocket
			serverSocket = new ServerSocket(portNumber, MAX_CONNECTIONS);
			serverSocket.setSoTimeout(TIMEOUT);
			String texto = "Servidor iniciando...";
			log.info(texto);
			setTextoLog(texto);
			
		} catch (IOException e) {
			log.error("No se puede crear el socket");
			e.printStackTrace();
			return;
		} // Try
	}

	private void closeServerSocket() {
		// Apago el servidor
		try {
			serverSocket.close();
		} catch (IOException ex) {
			log.error("Error al cerrar el servidor: " + ex.getMessage());
		}
	}

	private void executeBattle() {
		BatallaBin.compilarRobots(mensajes);
		log.info("Compila los archivo .java");
		BatallaBin.crearArchivoBatalla(mensajes);
		log.info("Se creo el archivo .battle de configuracion");
		BatallaBin.generarArchivoBinario();
		log.info("Ejecuta la batalla y crea el bin");
		mensajes.setGeneroBin(true);
	}

	/**
	 * Parar el servidor
	 */
	public void stopServer() {
		setShutDownFlag(true);
	}

	public boolean isIniciarBatalla() {
		return iniciarBatalla;
	}

	public void setIniciarBatalla(boolean iniciarBatalla) {
		this.iniciarBatalla = iniciarBatalla;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public LogRitaObservable getLogRita() {
		return logRitaObservable;
	}

	public void setLogRitaObsevable(LogRitaObservable logRita) {
		this.logRitaObservable = logRita;
	}

	public String getTextoLog() {
		return textoLog;
	}

	public void setTextoLog(String textoLog) {
		//cantidadConexionesObservable.changeData(activeConnectionCount);
		this.textoLog = textoLog;
		logRitaObservable.changeData(textoLog);
	public void addRobotNames(String nombreArchivo) {
		// TODO Auto-generated method stub
		this.robotsEnBatalla.add(nombreArchivo);
		clientesConectadosObservable.changeData(robotsEnBatalla);
	}

}
