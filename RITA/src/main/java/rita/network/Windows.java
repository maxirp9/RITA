package rita.network;

public class Windows extends SistemaOperativo {

	private String prefijo = "cmd /c ";

	@Override
	public String renombrarArchivo(String archivoOrigen, String archivoDestino) {

		String cmd = "ren " + archivoOrigen + " " + archivoDestino;
		this.ejecutarComando(prefijo + cmd);
		return cmd;
	}

	@Override
	public String copiarArchivo(String archivoOrigen, String archivoDestino) {

		String cmd = "copy " + archivoOrigen + " " + archivoDestino;
		this.ejecutarComando(prefijo + cmd);
		return cmd;
	}
	
	@Override
	public void ejecutarComando(String cmd){
		super.ejecutarComando(prefijo + cmd);
	}

}
