
package rita.network;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JTextField;

import org.apache.log4j.Logger;

import rita.settings.Settings;

/**
 * Esta clase gestiona el envio de datos entre el cliente y el servidor.
 * 
 * @author 
 */
public class ConexionServidor implements ActionListener {
    
    private Logger log = Logger.getLogger(ConexionServidor.class);
    private Socket socket; 
    private ObjectOutputStream salidaDatos;
    private ObjectInputStream entradaDatos;
	private FileInputStream fis;
    
    public ConexionServidor(Socket socket, JTextField tfMensaje, String usuario) {
        this.socket = socket;
        
        try {
        	entradaDatos = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			log.error("Error al crear el stream de salida : " + e.getMessage());
			e.printStackTrace();
		}
    }

    public void enviarMensaje(Mensaje mensaje) {
        try {
        	salidaDatos.writeObject(mensaje); 
        } catch (IOException ex) {
            log.error("Error al intentar enviar un mensaje: " + ex.getMessage());
        }
    }   
    
    public Mensaje recibirMensaje() {
    	Object datoLeido = null;
    	Mensaje mensajeRecibido = null;
        try {
        	datoLeido = entradaDatos.readObject();
        	if (datoLeido instanceof Mensaje) 
				mensajeRecibido = (Mensaje) datoLeido;
			
        } catch (IOException ex) {
            log.error("Error al intentar leer un mensaje: " + ex.getMessage());
        } catch (ClassNotFoundException e) {
        	log.error("Error no existe la clase recibida: " + e.getMessage());
		}
        return mensajeRecibido;
    }
    
    public void enviarArchivo(String nombre){

		try {
	   			
			String fichero = Settings.getRobotsPath() + File.separator + Settings.getProperty("defaultpackage") + File.separator + nombre + ".java";

			boolean enviadoUltimo = false;
			fis = new FileInputStream(fichero);

			// Se instancia y rellena un mensaje de envio de fichero
			MensajeTomaFichero mensaje = new MensajeTomaFichero();
			mensaje.nombreFichero = nombre;

			// Se leen los primeros bytes del fichero en un campo del mensaje
			int leidos = fis.read(mensaje.contenidoFichero);

			// Bucle mientras se vayan leyendo datos del fichero
			while (leidos > -1) {

				// Se rellena el numero de bytes leidos
				mensaje.bytesValidos = leidos;

				// Si no se han leido el maximo de bytes, es porque el fichero
				// se ha acabado y este es el ultimo mensaje
				if (leidos < MensajeTomaFichero.LONGITUD_MAXIMA) {
					mensaje.ultimoMensaje = true;
					enviadoUltimo = true;
				} else {
					mensaje.ultimoMensaje = false;
				}

				// Se envia por el socket
				salidaDatos.writeObject(mensaje);

				// Si es el ultimo mensaje, salimos del bucle.
				if (mensaje.ultimoMensaje) {
					break;
				}

				// Se crea un nuevo mensaje
				mensaje = new MensajeTomaFichero();
				mensaje.nombreFichero = nombre;

				// y se leen sus bytes.
				leidos = fis.read(mensaje.contenidoFichero);
			}

			if (enviadoUltimo == false) {
				mensaje.ultimoMensaje = true;
				mensaje.bytesValidos = 0;
				salidaDatos.writeObject(mensaje);
			}
			// Se cierra el ObjectOutputStream
			log.info("Termine de enviar el archivo del Robot al Servidor, soy Cliente: "
					+ socket.getLocalAddress().getHostAddress());
		} catch (Exception e) {
			log.error("Error al crear el stream de salida : " + e.getMessage());
			e.printStackTrace();
		}

    }
    
    public void recibirArchivo(String nombre){
		try {

			// Se abre un fichero para empezar a copiar lo que se reciba.
			// FileOutputStream fos = new FileOutputStream(mensaje.nombreFichero
			// + "_copia");
			FileOutputStream fos = new FileOutputStream(nombre);

			MensajeTomaFichero mensajeRecibido;
			Object mensajeAux;
			do {
				//log.info("Transfiriendo archivo binario: "
				//		+ socket.getLocalAddress().getHostAddress());
				
				// Se lee el mensaje en una variabla auxiliar
				mensajeAux = entradaDatos.readObject();

				// Si es del tipo esperado, se trata
				if (mensajeAux instanceof MensajeTomaFichero) {
					mensajeRecibido = (MensajeTomaFichero) mensajeAux;
					// Se escribe en pantalla y en el fichero
					// System.out.print(new
					// String(mensajeRecibido.contenidoFichero, 0,
					// mensajeRecibido.bytesValidos));
					fos.write(mensajeRecibido.contenidoFichero, 0,
							mensajeRecibido.bytesValidos);
				} else {
					// Si no es del tipo esperado, se marca error y se termina
					// el bucle
					log.error("Mensaje no esperado "
							+ mensajeAux.getClass().getName());
					break;
				}
			} while (!mensajeRecibido.ultimoMensaje);

			log.info("Ya termino la trasferencia del BIN el cliente: "
					+ socket.getLocalAddress().getHostAddress());
			// Se cierra socket y fichero
			fos.close();

		} catch (Exception e) {
			log.error("Error al crear el stream de entrada : " + e.getMessage());
			e.printStackTrace();
		}

    }
    
    public void cerrarConexion(){
    	try {
			entradaDatos.close();
			salidaDatos.close();
		} catch (IOException e) {
			log.error("Error al cerrar el stream de entrada o salida : " + e.getMessage());
		}
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void iniciarConexionSalida() {
		try {
        	salidaDatos = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			log.error("Error al crear el stream de salida : " + e.getMessage());
			e.printStackTrace();
		}
	}
}