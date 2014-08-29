package rita.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import rita.settings.Settings;

public class ServerWorkerRita extends Thread implements Observer {

	private Logger log = Logger.getLogger(ServerWorkerRita.class);
	private Socket socket;
	private ObjectInputStream entradaDatos;
	private ObjectOutputStream salidaDatos;
	private Mensajes mensajes;
	private ServerRita server;
	private String nombreRobot;

	static String directorioTempRobots = Settings.getRobotsnetPath();

	public ServerWorkerRita(Socket socketEntrada, Mensajes mensajeEntrada, ClientesConectadosObservable clientesEntrada, ServerRita serverInstance) {
		socket = socketEntrada;
		mensajes = mensajeEntrada;
		server = serverInstance;

		try {
			salidaDatos = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException ex) {
			log.error("Error al crear los stream de entrada y salida : "
					+ ex.getMessage());
		}
	}

	@Override
	public void run() {
		String textoLog;

		boolean errorConexion = false;
		// Se apunta a la lista de observadores de mensajes
		mensajes.addObserver(this);

		this.aceptarConexion();
		this.iniciarConexionEntrada();
		this.pedirRobot();

		while (!server.isShutDownFlag() && !mensajes.getGeneroBin() && !errorConexion) {
			// Esperando que se genere el bin
			try {
				log.info("El Cliente "
						+ socket.getInetAddress().getHostAddress()
						+ " esperando el bin ...");
				Thread.sleep(1000);
				
				Mensaje mensajeAEnviar = new Mensaje("VerificoConexion", socket.getInetAddress().getHostAddress());
				try {
					salidaDatos.writeObject(mensajeAEnviar);
				} catch (IOException e) {
					e.printStackTrace();
					errorConexion = true;					
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} // WHILE
		
		if(!server.isShutDownFlag()){
			if (!errorConexion){
	
				// Podria ponerse en el update
				this.binGenerado();
				if (this.hayPedidoBinario()){
					this.enviarArchivoBinario();
					textoLog = "Envio del archivo Binario al cliente: "
							+ socket.getInetAddress().getHostAddress();
					this.server.guardarLog(textoLog);
				}
				else
					log.error("Falla del pedido binario del Cliente: "
							+ socket.getInetAddress().getHostAddress());
				mensajes.incrementarCantidadConexiones();
				try {
					entradaDatos.close();
					salidaDatos.close();
				} catch (IOException ex2) {
					log.error("Error al cerrar los stream de entrada y salida :"
							+ ex2.getMessage());
				}
			}else{
				this.server.deleteRobotName(this.nombreRobot);
			}
		}else{
			Mensaje mensajeAEnviar = new Mensaje("ParoServidor", socket.getInetAddress().getHostAddress());
			try {
				salidaDatos.writeObject(mensajeAEnviar);
			} catch (IOException e) {
				e.printStackTrace();
				errorConexion = true;					
			}
		}
	}

	private void iniciarConexionEntrada() {

		try {
			entradaDatos = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			log.error("Error al crear el stream de entrada:" + e.getMessage());
			e.printStackTrace();
		}
		
	}

	private void pedirRobot() {

		try {

			// Se abre un fichero para empezar a copiar lo que se reciba.
			// FileOutputStream fos = new FileOutputStream(mensaje.nombreFichero
			// + "_copia");
			String path = directorioTempRobots + File.separator;
			String nombreArch = "copia-" + this.getId() + ".java";
			String archivo = path + nombreArch;
			FileOutputStream fos = new FileOutputStream(archivo);
			String nombreArchivo = "";

			// Se crea un ObjectInputStream del socket para leer los mensajes
			// que contienen el fichero.
			MensajeTomaFichero mensajeRecibido;
			Object mensajeAux;
			do {
				// Se lee el mensaje en una variabla auxiliar
				mensajeAux = this.entradaDatos.readObject();

				// Si es del tipo esperado, se trata
				if (mensajeAux instanceof MensajeTomaFichero) {
					mensajeRecibido = (MensajeTomaFichero) mensajeAux;
					// Se escribe en pantalla y en el fichero
					// System.out.print(new
					// String(mensajeRecibido.contenidoFichero, 0,
					// mensajeRecibido.bytesValidos));
					fos.write(mensajeRecibido.contenidoFichero, 0,
							mensajeRecibido.bytesValidos);
					nombreArchivo = mensajeRecibido.nombreFichero;
				} else {
					// Si no es del tipo esperado, se marca error y se termina
					// el bucle
					log.error("Mensaje no esperado "
							+ mensajeAux.getClass().getName());
					break;
				}
			} while (!mensajeRecibido.ultimoMensaje);
			// Sumo la cantidad de robots
			mensajes.incrementarCantidadRobots();
			// Agrego el robot al listado para la batalla
			mensajes.agregarRobot(nombreArchivo);
			
			server.addRobotNames(nombreArchivo);
			nombreRobot = nombreArchivo;
			log.info("Ya termino la trasferencia del Robot del cliente: "
					+ socket.getInetAddress().getHostAddress());
			// Se cierra fichero
			fos.close();
			String archivoDestino = nombreArchivo + ".java"; 
			String cmd = Settings.getSO().renombrarArchivo(nombreArch, archivoDestino, path);
			log.info("Comando: " + cmd);
			log.info("Comando ejecutado ...");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private boolean hayPedidoBinario() {
		boolean pedidoBin = true;
		
		// Espera el mensaje enviado por el cliente de DameBinFile
		try {
			Object datoLeido = null;
			Mensaje mensajeRecibido = null;
			datoLeido = entradaDatos.readObject();
			if (datoLeido instanceof Mensaje)
				mensajeRecibido = (Mensaje) datoLeido;

			// Pone el mensaje recibido en mensajes para que se notifique
			// a sus observadores que hay un nuevo mensaje.

			if (mensajeRecibido.accion.equals("DameBinFile")) {
				log.info("Me preparo para enviar BINARIO al cliente: "
						+ socket.getInetAddress().getHostAddress());
			} else {
				log.error("El Cliente "
						+ socket.getInetAddress().getHostAddress()
						+ " espera DameBinFile y recibe mensaje incorrecto:"
						+ mensajeRecibido.accion);
				pedidoBin = false;
			}
		} catch (IOException ex) {
			log.error("Error al intentar leer un mensaje: " + ex.getMessage());
		} catch (ClassNotFoundException e) {
			log.error("Error no existe la clase recibida: " + e.getMessage());
		}

		return pedidoBin;
	}

	private void enviarArchivoBinario() {

		try {
			String fichero = Settings.getBinaryPath() + File.separator + "batalla.bin";
			boolean enviadoUltimo = false;
			// Se abre el fichero.
			FileInputStream fis = new FileInputStream(fichero);

			// Se instancia y rellena un mensaje de envio de fichero
			MensajeTomaFichero mensaje = new MensajeTomaFichero();
			mensaje.nombreFichero = fichero;

			// Se leen los primeros bytes del fichero en un campo del mensaje
			int leidos = fis.read(mensaje.contenidoFichero);

			// Bucle mientras se vayan leyendo datos del fichero
			while (leidos > -1) {

				// Se rellena el n�mero de bytes leidos
				mensaje.bytesValidos = leidos;

				// Si no se han leido el m�ximo de bytes, es porque el fichero
				// se ha acabado y este es el �ltimo mensaje
				if (leidos < MensajeTomaFichero.LONGITUD_MAXIMA) {
					mensaje.ultimoMensaje = true;
					enviadoUltimo = true;
				} else {
					mensaje.ultimoMensaje = false;
				}

				// Se env�a por el socket
				this.salidaDatos.writeObject(mensaje);

				// Si es el �ltimo mensaje, salimos del bucle.
				if (mensaje.ultimoMensaje) {
					break;
				}

				// Se crea un nuevo mensaje
				mensaje = new MensajeTomaFichero();
				mensaje.nombreFichero = fichero;

				// y se leen sus bytes.
				leidos = fis.read(mensaje.contenidoFichero);
			}

			if (enviadoUltimo == false) {
				mensaje.ultimoMensaje = true;
				mensaje.bytesValidos = 0;
				this.salidaDatos.writeObject(mensaje);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.info("Termine de enviar el archivo Binario al cliente: "
				+ socket.getInetAddress().getHostAddress());

	}

	@Override
	public void update(Observable o, Object arg) {

		/*
		 * try { // Envia el mensaje al cliente
		 * salidaDatos.writeUTF(arg.toString()); } catch (IOException ex) {
		 * log.error("Error al enviar mensaje al cliente (" + ex.getMessage() +
		 * ")."); }
		 */
	}

	private void aceptarConexion() {
		
		log.info("Soy hilo de " + socket.getInetAddress().getHostAddress());
		Mensaje mensajeAEnviar = new Mensaje("ListoHilo", socket.getInetAddress().getHostAddress());
		try {
			salidaDatos.writeObject(mensajeAEnviar);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mensajes.setMensaje(mensajeAEnviar.accion);
	}

	private void binGenerado() {
		log.info("Bin Generado para el cliente:"
				+ socket.getInetAddress().getHostAddress());

		Mensaje mensajeAEnviar = new Mensaje("BinGenerado", socket.getInetAddress().getHostAddress());
		mensajes.setMensaje(mensajeAEnviar.accion);
		try {
			salidaDatos.writeObject(mensajeAEnviar);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mensajes.setMensaje(mensajeAEnviar.accion);
	}
}