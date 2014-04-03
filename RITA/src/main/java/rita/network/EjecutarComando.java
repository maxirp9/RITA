package rita.network;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EjecutarComando {

    public EjecutarComando(String cmd) {

        String s = null;

        try {

            // Determinar en qué SO estamos
            String so = System.getProperty("os.name");

            String comando;

            // Comando para Linux
            if (so.equals("Linux")) {
                //comando = "java -Xmx512M -Dsun.io.useCanonCaches=false -cp /home/pvilaltella/workspaceJava/robocode-1.8.3.0-setup/libs/robocode.jar robocode.Robocode -replay /tmp/batalla.copia.bin -tps 25";
            	comando = cmd;
            } else {
                comando = "cmd /c ipconfig";
            }

            // Ejcutamos el comando
            Process p = Runtime.getRuntime().exec(comando);

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