package rita.network;

public class LogServer  {

	private String texto;
	private LogRitaObservable logRitaObservable;
	
	/**
	 *  Clase para guardar el log del Server
	 */
	public LogServer() {
				
		setLogRitaObservable(new LogRitaObservable());
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String textoLog) {
		this.texto = textoLog;
		logRitaObservable.changeData(textoLog);
	}

	public LogRitaObservable getLogRitaObservable() {
		return logRitaObservable;
	}

	public void setLogRitaObservable(LogRitaObservable logRitaObservable) {
		this.logRitaObservable = logRitaObservable;
	}

}
