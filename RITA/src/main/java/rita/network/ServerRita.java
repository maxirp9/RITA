package rita.network;

import net.sf.robocode.cachecleaner.CacheCleaner;
import net.sf.robocode.io.FileUtil;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import rita.battle.BatallaBin;
import rita.settings.Settings;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
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
	private CantidadConexionesObservable cantidadConexionesObservable;

	private Logger log = Logger.getLogger(ServerRita.class);

	private ServerSocket serverSocket;
	private boolean start = false;
	
	static String directorioRobocodeLibs = Settings.getInstallPath() + "lib";

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	public static ServerRita getInstance(int port, CantidadConexionesObservable cantidad) {
		if (instance == null) {
			instance = new ServerRita(port, cantidad);
		}
		return instance;
	}

	/**
	 * Constructor Servidor RITA
	 */
	private ServerRita() {
		// this(DEFAULT_PORT_NUMBER, cantidad);
		portNumber = DEFAULT_PORT_NUMBER;
		cantidadConexionesObservable = new CantidadConexionesObservable();
		mensajes = new Mensajes();
	}

	/**
	 * Constructor Servidor RITA definiendo el puerto
	 * 
	 * @param defaultPortNumber
	 */
	private ServerRita(int defaultPortNumber, CantidadConexionesObservable cantidad) {
		
		initialize(defaultPortNumber,cantidad);
	}

	private void initialize(int defaultPortNumber, CantidadConexionesObservable cantidad) {
		portNumber = defaultPortNumber;
		cantidadConexionesObservable = cantidad;
		mensajes = new Mensajes();

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
		cantidadConexionesObservable.changeData(activeConnectionCount);
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
						log.info("Servidor a la espera de conexiones.");
						socket = serverSocket.accept(); // Aceptando las
														// conexiones
					} catch (InterruptedIOException e) {
						log.error("Error: " + e.getMessage());
					} // try

					if (!iniciarBatalla) {
						// SUMO SOLO LOS CONECTADOS
						setActiveConnectionCount(activeConnectionCount + 1);
						log.info("Cliente con la IP "
								+ socket.getInetAddress().getHostAddress()
								+ " conectado.");
						// Crea el objeto worker para procesar las conexiones
						ServerWorkerRita serverWorkerRita = new ServerWorkerRita(
								socket, mensajes);
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

	}

	private void createServerSocket() {
		try {
			// Creo el ServerSocket
			serverSocket = new ServerSocket(portNumber, MAX_CONNECTIONS);
			serverSocket.setSoTimeout(TIMEOUT);
			log.info("Servidor iniciando...");
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

}
