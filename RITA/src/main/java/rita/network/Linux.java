package rita.network;

public class Linux extends SistemaOperativo {

	@Override
	public String renombrarArchivo(String archivoOrigen, String archivoDestino, String path) {

		String cmd = "mv " + path + archivoOrigen + " " + path + archivoDestino;
		this.ejecutarComando(cmd);
		return cmd;
	}

	@Override
	public String copiarArchivo(String archivoOrigen, String archivoDestino) {

		String cmd = "cp " + archivoOrigen + " " + archivoDestino;
		this.ejecutarComando(cmd);
		return cmd;
	}
	
	@Override
	public void ejecutarComando(String cmd){
		super.ejecutarComando(cmd);
	}

}
