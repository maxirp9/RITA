package rita.network;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import rita.settings.Settings;
import rita.ui.component.DialogClientRita;
import rita.widget.SourceCode;

public class ClienteRita extends Thread {

	private ConexionServidor conexionServidor;
	private Socket socket;
	private String miDireccion;
	private Logger log = Logger.getLogger(DialogClientRita.class);
	private String ip;
	private int port;
	private String robot;
	private boolean ejecutarRobocode = false;
	static String directorioRobocodeLibs = Settings.getInstallPath() + File.separator + "lib";
	private boolean ventantaAbierta = false; // se usa para cuando cierra la ventana, la clase Cliente debe morir
	
	
	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}


	public ConexionServidor getConexionServidor() {
		return conexionServidor;
	}

	public void setConexionServidor(ConexionServidor conexionServidor) {
		this.conexionServidor = conexionServidor;
	}

	public Logger getLog() {
		return log;
	}

	public void setLog(Logger log) {
		this.log = log;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getRobot() {
		return robot;
	}

	public void setRobot(String robot) {
		this.robot = robot;
	}

	public boolean isEjecutarRobocode() {
		return ejecutarRobocode;
	}

	public void setEjecutarRobocode(boolean ejecutarRobocode) {
		this.ejecutarRobocode = ejecutarRobocode;
	}

	public ClienteRita(String ipServer, int portServer, String robotCliente) throws UnknownHostException, IOException {		
		ip = ipServer;
		port = portServer;
		robot = robotCliente;
		socket = new Socket(ip, port);
		conexionServidor = new ConexionServidor(socket, null, robot);
		setMiDireccion(socket.getLocalAddress().getHostAddress());
		ventantaAbierta = true;
	}
	
	
	public void run() {
		log.info("Cliente Dame conexion");	
		System.out.println(socket.getLocalAddress().getHostName());
		
		InetSocketAddress socketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
		String ipRemota = socketAddress.getAddress().getHostAddress().toString();

		if(!socket.getLocalAddress().getHostName().contains("localhost") && !socket.getLocalAddress().getHostName().contains("127.0.0.1")
				&& !ipRemota.equals(traerIp()))
			this.ejecutarRobocode = true;			
		recibirMensajesServidor();
		closeClient();
	}
	
	/**
	 * Recibe los mensajes del chat reenviados por el servidor
	 */
	public void recibirMensajesServidor() {
		// Obtiene el flujo de entrada del socket
		if (this.hayPedidoRobot()){
			conexionServidor.iniciarConexionSalida();
			this.guardarCodigoFuente();
			conexionServidor.enviarArchivo(robot);
		}
		else
			log.error("Falla del pedido de robot del Cliente: "
					+ socket.getLocalAddress().getHostAddress());

		try {
			Mensaje mensajeRecibido;
			mensajeRecibido = conexionServidor.recibirMensaje();
			
			while (!mensajeRecibido.accion.equals("ParoServidor") && !mensajeRecibido.accion.equals("BinGenerado") && ventantaAbierta) {
				log.info("El Cliente "
						+ getMiDireccion()
						+ " recibe mensaje:"
						+ mensajeRecibido.accion);
				mensajeRecibido = conexionServidor.recibirMensaje();
			}
			
			log.info("El Cliente " + getMiDireccion() + " recibe mensaje:" + mensajeRecibido.accion);
			
			if(!mensajeRecibido.accion.equals("ParoServidor")){
				if (ventantaAbierta){
				//if (mensajeRecibido.accion.equals("BinGenerado")) {
					log.info("Ya esta el BinGenerado para el cliente: "
							+ getMiDireccion());
	
					Mensaje mensajeAEnviar = new Mensaje("DameBinFile", getMiDireccion());
					conexionServidor.enviarMensaje(mensajeAEnviar);
					log.info("Pide el binario el cliente: "
							+ getMiDireccion());
					DialogClientRita.startWait();
					conexionServidor.recibirArchivo(Settings.getBinaryPath() + File.separator + "batalla.copia.bin");
					conexionServidor.recibirArchivo(Settings.getInstallPath() + File.separator + "resultado-batalla.txt");
					log.info("Paso el resultado: "
							+ getMiDireccion());
			
				if(this.ejecutarRobocode)
					ejecutarRobocode();
					DialogClientRita.resetearBotones();
					DialogClientRita.stopWait();
				}
			}else
			{
				log.info("PARA EL SERVIDOR");
				DialogClientRita.resetearBotones();
			}
		} catch (NullPointerException ex) {
			log.error("El socket no se creo correctamente. ");
		}
	}

	private void guardarCodigoFuente() {
		
		SourceCode sourceCode = SourceCode.getInstance();
		
		try {
			sourceCode.saveSourceCode();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean hayPedidoRobot() {

		boolean pedidoRobot = true;
		Mensaje mensajeRecibido;
		// Pone el mensaje recibido en mensajes para que se notifique
		// a sus observadores que hay un nuevo mensaje.
		mensajeRecibido = conexionServidor.recibirMensaje();
		if (mensajeRecibido.accion.equals("ListoHilo")) {
			log.info("Cliente: "
					+ getMiDireccion()
					+ " acepta hilo");
		} else {
			log.error("El Cliente "
					+ getMiDireccion()
					+ " espera ListoHilo y recibe mensaje incorrecto:"
					+ mensajeRecibido.accion);
			pedidoRobot = false;
		}
		return pedidoRobot;
	}

	/**
	 * Ejecuta el robocode
	 */
	public void ejecutarRobocode() { 
		String cmd = "java -Xmx512M -Dsun.io.useCanonCaches=false -cp " + directorioRobocodeLibs + File.separator + "robocode.jar robocode.Robocode -replay " + Settings.getBinaryPath() + File.separator + "batalla.copia.bin" + " -tps 25";
		Settings.getSO().ejecutarComando(cmd);
		log.info(cmd);
		log.info("Se ejecuta Robocode en el cliente "
				+ socket.getLocalAddress().getHostAddress());
		
	}

	public void closeClient() {
		try {
			log.info("Cierro el cliente "
					+ socket.getLocalAddress().getHostAddress());
			socket.close();
			DialogClientRita.clienteRita = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public String getMiDireccion() {
		return miDireccion;
	}

	public void setMiDireccion(String miDireccion) {
		this.miDireccion = miDireccion;
	}

	public boolean isVentantaAbierta() {
		return ventantaAbierta;
	}

	public void setVentantaAbierta(boolean ventantaAbierta) {
		this.ventantaAbierta = ventantaAbierta;
	}

	
	public String traerIp() {
		Enumeration<NetworkInterface> interfaces = null;
		String ip = "";
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		while (interfaces.hasMoreElements()) {
			NetworkInterface current = interfaces.nextElement();
			// System.out.println("1er " + current);
			if (current.getName().startsWith("eth")
					|| current.getName().startsWith("wlan")) {
				try {
					if (!current.isUp() || current.isLoopback()
							|| current.isVirtual())
						continue;
				} catch (SocketException e) {
					e.printStackTrace();
				}
				Enumeration<InetAddress> addresses = current.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress current_addr = addresses.nextElement();
					if (current_addr instanceof Inet4Address)
						ip = current_addr.getHostAddress().toString();
					if (current_addr.isLoopbackAddress())
						continue;

				}
			}
		}
		return ip;
	}
}