package rita.network;
/**
 * Javier Abell�n. 18 Mar 2006
 * 
 * Programa de ejemplo de como transmitir un fichero por un socket. Esta es el
 * mensaje que contiene los cachos de fichero que se van enviando
 * 
 */

import java.io.Serializable;

/**
 * Mensaje que contiene parte del fichero que se est� transmitiendo.
 * 
 * @author Javier Abell�n
 * 
 */
public class MensajeTomaFichero implements Serializable {
    /** Nombre del fichero que se transmite. Por defecto "" */
    public String nombreFichero = "";

    /** Si este es el �ltimo mensaje del fichero en cuesti�n o hay m�s despu�s */
    public boolean ultimoMensaje = true;

    /** Cuantos bytes son v�lidos en el array de bytes */
    public int bytesValidos = 0;

    /** Array con bytes leidos del fichero */
    public byte[] contenidoFichero = new byte[MensajeTomaFichero.LONGITUD_MAXIMA];

    /** N�mero m�ximo de bytes que se envia�n en cada mensaje */
    public final static int LONGITUD_MAXIMA = 10;
}
