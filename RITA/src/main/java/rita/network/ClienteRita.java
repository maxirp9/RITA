package rita.network;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import rita.settings.Settings;
import rita.ui.component.DialogClientRita;

public class ClienteRita extends Thread {

	private ConexionServidor conexionServidor;
	private Socket socket;
	private String miDireccion;
	private Logger log = Logger.getLogger(DialogClientRita.class);
	private String ip;
	private int port;
	private String robot;
	
	static String directorioRobocodeLibs = Settings.getInstallPath() + "lib";

	public ClienteRita(String ipServer, int portServer, String robotCliente) {		
		ip = ipServer;
		port = portServer;
		robot = robotCliente;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public void run(){
		log.info("Cliente Dame conexion");							
		try {
			socket = new Socket(ip, port);
			conexionServidor = new ConexionServidor(socket, null, robot);
			setMiDireccion(socket.getLocalAddress().getHostAddress());
		} catch (UnknownHostException ex) {
			log.error("No se ha podido conectar con el servidor ("
					+ ex.getMessage() + ").");							
		} catch (IOException ex) {
			log.error("No se ha podido conectar con el servidor ("
					+ ex.getMessage() + ").");
		}
		
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
			conexionServidor.enviarArchivo(robot); // Usamos el nombre del usuario PROVISORIAMENTE para la prueba
		}
		else
			log.error("Falla del pedido de robot del Cliente: "
					+ socket.getLocalAddress().getHostAddress());

		try {
			Mensaje mensajeRecibido;
			mensajeRecibido = conexionServidor.recibirMensaje();
			
			if (mensajeRecibido.accion.equals("BinGenerado")) {
				log.info("Ya esta el BinGenerado para el cliente: "
						+ getMiDireccion());

				Mensaje mensajeAEnviar = new Mensaje("DameBinFile", getMiDireccion());
				conexionServidor.enviarMensaje(mensajeAEnviar);
				log.info("Pide el binario el cliente: "
						+ getMiDireccion());
				conexionServidor.recibirArchivo("batalla.copia.bin");
			} else
				log.error("El Cliente "
						+ getMiDireccion()
						+ " espera BinGenerado y recibe mensaje incorrecto:"
						+ mensajeRecibido.accion);

			ejecutarRobocode();
		} catch (NullPointerException ex) {
			log.error("El socket no se creo correctamente. ");
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

	public void ejecutarRobocode() {		
		
		String cmd = "java -Xmx512M -Dsun.io.useCanonCaches=false -cp " + directorioRobocodeLibs + File.separator + "robocode.jar robocode.Robocode -replay " + Settings.getBinaryPath() + File.separator + "batalla.copia.bin" + " -tps 25";
		
		log.error(cmd);
		log.info("Se ejecuta Robocode en el cliente "
				+ socket.getLocalAddress().getHostAddress());
		EjecutarComando comando = new EjecutarComando(cmd);		
		
	}

	private void closeClient() {
		try {
			log.info("Cierro el cliente "
					+ socket.getLocalAddress().getHostAddress());
			socket.close();			
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

}
