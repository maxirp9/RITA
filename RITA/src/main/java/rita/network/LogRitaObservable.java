package rita.network;

import java.util.Observable;

/**
 * @author pvilaltella
 * 
 * Clase para guardar todo lo relevante que debe mostrar el log del servidor
 *
 */
public class LogRitaObservable extends Observable{

	public LogRitaObservable() {
		super();
	}

	/**
	 * Setea el texto del log y Notifica a los observadores del cambio
	 * @param textoLog
	 */
	public void changeData(String textoLog) {
		
		setChanged();
		notifyObservers(textoLog);
	}

}
