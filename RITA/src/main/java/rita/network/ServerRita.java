package rita.network;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import rita.battle.BatallaBin;
import rita.settings.Settings;
import rita.widget.DialogLogRita;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

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
	private LogServer logServer;

	private ServerSocket serverSocket;
	private boolean start = false;
	
	private ArrayList<String> robotsEnBatalla;
	
	static String directorioRobocodeLibs = Settings.getInstallPath() + File.separator + "lib";

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
		setLogServer(DialogLogRita.getInstance().getLogServer());
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
		setLogServer(DialogLogRita.getInstance().getLogServer());
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
		createFolders();

		// While para mantener el servidor activo hasta el envio del apagado
		while (!shutDownFlag) {
			// Esperando las conexiones nuevas
			try {

				while (!shutDownFlag && true && (activeConnectionCount < MAX_CONNECTIONS)
						&& !iniciarBatalla) {
					try {
						String texto = "Servidor a la espera de conexiones.";
						log.info(texto);						
						getLogServer().setTexto(texto);
						socket = serverSocket.accept(); // Aceptando las
														// conexiones
					} catch (InterruptedIOException e) {
						log.error("Error: " + e.getMessage());
					} // try

					if (!shutDownFlag && !iniciarBatalla) {
						// SUMO SOLO LOS CONECTADOS
						//setActiveConnectionCount(activeConnectionCount + 1);
						String texto = "Cliente con la IP "
								+ socket.getInetAddress().getHostAddress()
								+ " conectado.";
						log.info(texto);
						getLogServer().setTexto(texto);
						// Crea el objeto worker para procesar las conexiones
						ServerWorkerRita serverWorkerRita = new ServerWorkerRita(
								socket, mensajes, clientesConectadosObservable, this);
						serverWorkerRita.start();
					}

				} // end del While
				while (!shutDownFlag && mensajes.getCantidadRobots() < activeConnectionCount) {
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
				if(!shutDownFlag)
					executeBattle();
				// Espero que los workers envien los archivos para reiniciar el
				// servidor
				while (!shutDownFlag && mensajes.getCantidadConexiones() < activeConnectionCount) {
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
				if(!shutDownFlag)
					this.ejecutarRobocode();
				// Pongo en false para queden esperando nuevamente los hilos
				closeServerSocket();
				if(!shutDownFlag)
					createServerSocket();
				reiniciarVariables();
				// NO debería cerrar las conexiones hasta que no se envien todos
				// los mensajes
			} catch (IOException e) {
				log.error("Error de input");
			}
		}// Cierre while del cierre del servidor
		
		if(shutDownFlag)
			log.info("Stop Servidor");
	}
	
	private void createFolders() {

		ArrayList<String> arrayDir = new ArrayList<String>();
		arrayDir.add("battles");
		arrayDir.add("binary");
		arrayDir.add("robotsnet");
		
		// si el directorio de configuracion del usuario no existe => crearlo
		for (String dir : arrayDir) {
			
			File ritaDir = new File(Settings.getInstallPath(),dir);
			
			if(!ritaDir.exists()) {
				try {
					FileUtils.forceMkdir(ritaDir);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Ejecuta robocode
	 */
	public void ejecutarRobocode() { 
		
		String cmd = "java -Xmx512M -Dsun.io.useCanonCaches=false -cp " + directorioRobocodeLibs + File.separator + "robocode.jar robocode.Robocode -replay " + Settings.getBinaryPath() + File.separator + "batalla.copia.bin" + " -tps 25";
		Settings.getSO().ejecutarComando(cmd);
		log.info(cmd);
		String texto = "Se ejecuta Robocode en el Servidor";
		log.info(texto);
		getLogServer().setTexto(texto);
		
	}

	private void reiniciarVariables() {

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
		String texto;
		try {
			// Creo el ServerSocket
			serverSocket = new ServerSocket(portNumber, MAX_CONNECTIONS);
			serverSocket.setSoTimeout(TIMEOUT);
			texto = "Servidor iniciando...";
			log.info(texto);
			getLogServer().setTexto(texto);
			
		} catch (IOException e) {
			texto = "No se puede crear el socket";
			log.error(texto);
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
		String texto = "En proceso de ejecución de la batalla";
		getLogServer().setTexto(texto);
		BatallaBin.compilarRobots(mensajes);
		log.info("Compila los archivo .java");
		BatallaBin.crearArchivoBatalla(mensajes);
		log.info("Se creo el archivo .battle de configuracion");
		BatallaBin.generarArchivoBinario();
		texto = "Ejecuta la batalla y crea el bin";
		log.info(texto);
		mensajes.setGeneroBin(true);
		BatallaBin.borrarArchivosRobots(mensajes);
	}

	/**
	 * Parar el servidor
	 */
	public void stopServer() {
		String texto = "Servidor detenido.";
		log.info(texto);		
		getLogServer().setTexto(texto);
		setShutDownFlag(true);
		instance = null;
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
	
	public void addRobotNames(String nombreArchivo) {

		this.robotsEnBatalla.add(nombreArchivo);
		clientesConectadosObservable.changeData(robotsEnBatalla);
	}

	public LogServer getLogServer() {
		return logServer;
	}

	public void setLogServer(LogServer logServer) {
		this.logServer = logServer;
	}

}
