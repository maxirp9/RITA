package rita.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class SistemaOperativo {
	
	public abstract String renombrarArchivo(String archivoOrigen, String archivoDestino);
	public abstract String copiarArchivo(String archivoOrigen, String archivoDestino);
	
	public void ejecutarComando(String cmd){
        String s = null;

        try {

/*
            ProcessBuilder pb=new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process=pb.start();
            BufferedReader inStreamReader = new BufferedReader(
                new InputStreamReader(process.getInputStream())); 

            while(inStreamReader.readLine() != null){
                //do something with commandline output.
            	System.out.println(inStreamReader.readLine());
            }*/
            
            // Ejcutamos el comando
            Process p = Runtime.getRuntime().exec(cmd);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // Leemos la salida del comando
            System.out.println("Ésta es la salida standard del comando:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // Leemos los errores si los hubiera
            System.out.println("Ésta es la salida standard de error del comando (si la hay):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

            //System.exit(0);
        } catch (IOException e) {
            System.out.println("Excepción: ");
            e.printStackTrace();
            System.exit(-1);
        }
	}
	
}
