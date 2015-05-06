package rita.battle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.String;
import java.util.ArrayList;

import rita.compiler.CompileString;
import rita.network.Mensajes;
import rita.settings.Settings;

public class BatallaBin {
	
	static String directorioRobocodeLibs = Settings.getInstallPath()  + File.separator  + "lib";
	static String directorioRobocodeBatallas = Settings.getInstallPath() + File.separator + "battles";
	static String directorioRobocodeBinario = Settings.getInstallPath() + File.separator + "binary";
	
	/**
	 * Creacion del archivo de configuracion de la batalla con los robots
	 * participantes
	 */
	public static void crearArchivoBatalla(Mensajes mensajes, String rondas) {
		File f;
		f = new File(directorioRobocodeBatallas + File.separator + "batalla.battle");
		
		System.out.println("RONDAS: " + String.valueOf(rondas));

		// Escritura
		try {
			String listado = join(", ", mensajes.getRobotsEnBatalla());
			//listado = listado.replaceAll(".class", "");
			FileWriter w = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(w);
			PrintWriter wr = new PrintWriter(bw);
			wr.write("#Battle Properties \n");
			wr.append("robocode.battleField.width=800\n"
					+ "robocode.battleField.height=600\n"
					+ "robocode.battle.numRounds="+ rondas +"\n"
					+ "robocode.battle.gunCoolingRate=0.1\n"
					+ "robocode.battle.rules.inactivityTime=450\n"
					+ "robocode.battle.hideEnemyNames=true\n"
					+ "robocode.battle.selectedRobots=" + listado + "\n");// escribimos
																			// en
																			// el
																			// archivo
			// wr.append(" - y aqui continua"); //concatenamos en el archivo sin
			// borrar lo existente
			// ahora cerramos los flujos de canales de datos, al cerrarlos el
			// archivo quedará guardado con información escrita
			// de no hacerlo no se escribirá nada en el archivo
			wr.close();
			bw.close();
		} catch (IOException e) {
		};

	}

	/**
	 * Devuleve un arreglo de Strings con los nombres de los archivos del
	 * directorio directorioTemp
	 * 
	 * @param extension
	 *            String
	 * @return ArrayList<String>
	 */
	public static ArrayList<String> listadoRobots(String extension, String dir) {
		File directorio = new File(dir);
		String[] robots = directorio.list();
		ArrayList<String> listaRobots = new ArrayList<String>();

		for (int i = 0; i < robots.length; i++) {
			// Saco la extension pasada por parametro del robot
			// Solo utilizo los archivos compilados
			String[] robot = robots[i].split("\\.");
			try {
				if (robot[1].equals(extension)) {
					listaRobots.add(robots[i]);
				}
				
			} catch (ArrayIndexOutOfBoundsException e) {
				// No proceso los que no tienen extension
				System.out.println("------------------------- \n" +
						"Error de los limites del arreglo: " + robots[i]);
			}

		}
		return listaRobots;
	}

	/**
	 * Devuelve un String uniendo el arreglo array pasado por parametro con el
	 * string union pasado por parametro
	 * 
	 * @param union
	 *            String
	 * @param array
	 *            ArrayList<Sting>
	 * @return String
	 */
	public static String join(String union, ArrayList<String> array) {
		String ret = "";
//		for (String string : array) {
//			ret += "sample." + string + union;
//		}		
		
		for (int i = 0; i < array.size(); i++) {
			if (i == array.size()-1)
				ret += Settings.getProperty("defaultpackage") + "." + array.get(i) + "*";
			else
				ret += Settings.getProperty("defaultpackage") + "." + array.get(i) + "*" + union;
		}
		return ret;

	}

	/**
	 * Compila los robots que estan en el directorio directorioTempRobots
	 */
	public static void compilarRobots( Mensajes mensajes) {
		ArrayList<String> robots = mensajes.getRobotsEnBatalla(); // BatallaBin.listadoRobots("java",directorioTempRobots);
		for (String nombre : robots) {

			File javaSourceFile = new File(Settings.getRobotsnetPath(), nombre + ".java");
			File javaOutputDirectory = new File(Settings.getRobotsPath());
			try {
				CompileString.compile(javaSourceFile, javaOutputDirectory);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String archivoOrigen = Settings.getRobotsnetPath() + File.separator + nombre + ".java"; 
			String archivoDestino = Settings.getRobotsPath() + File.separator + Settings.getProperty("defaultpackage");
			String cmd = Settings.getSO().copiarArchivo(archivoOrigen, archivoDestino);
			System.out.println("Comando: " + cmd);

		}
	}

	public static void generarArchivoBinario() {
		String cmd = "java -Xmx512M -Dsun.io.useCanonCaches=false -cp " + directorioRobocodeLibs + File.separator + "robocode.jar robocode.Robocode -battle " + directorioRobocodeBatallas + File.separator + "batalla.battle -nodisplay -record " +  Settings.getBinaryPath() + File.separator + "batalla.bin -results " + Settings.getInstallPath() + File.separator + "resultado-batalla.txt";
		Settings.getSO().ejecutarComando(cmd);
		System.out.println("Comando: " + cmd);		
						
	}
	
	public static void borrarArchivoBatalla(){
		
		File f;
		f = new File(directorioRobocodeBatallas + File.separator + "batalla.battle");

		// Limpio el archivo battalla.battle
		try {
			
			FileWriter w = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(w);
			PrintWriter wr = new PrintWriter(bw);
			wr.write(" ");
			wr.close();
			bw.close();
		} catch (IOException e) {
		};
	}
	
	/**
	 * Borrar los archivos temporales de los robots
	 * @param mensajes
	 */
	public static void borrarArchivosRobots(Mensajes mensajes){
		borrarArchivosRobotsJava();
		borrarArchivosRobotsDeClase(mensajes);
	}
	
	/**
	 * Borrar los archivos java temporales
	 */
	private static void borrarArchivosRobotsJava(){
		
		String dir = Settings.getRobotsnetPath();
		File f;

		File directorio = new File(dir);
		String[] robots = directorio.list();
		
		for (int i = 0; i < robots.length; i++) {
			f = new File(dir + File.separator + robots[i]);
			f.delete();

		}
		
	}
	
	/**
	 * Borrar los archivos de clase de los robots
	 * @param mensajes
	 */
	private static void borrarArchivosRobotsDeClase(Mensajes mensajes){
		
		String dir = Settings.getRobotsPath();
		
		File archivoClass;
		ArrayList<String> robots = mensajes.getRobotsEnBatalla();
		
		for (String robot : robots) {
			
			String archivo = dir + File.separator + Settings.getProperty("defaultpackage") + File.separator + robot + ".";
			archivoClass = new File(archivo + "class");
			archivoClass.delete();
			archivoClass = new File(archivo + "java");
			archivoClass.delete();
		}
		
	}
	
	/**
	 *  Borra los archivos binarios
	 */
	public static void borrarArchivosBinarios(){
		
		File archivoClass;
		String dir = Settings.getBinaryPath();
			
		String archivo = dir + File.separator;
		archivoClass = new File(archivo+ "batalla.bin");
		archivoClass.delete();
		archivoClass = new File(archivo + "batalla.copia.bin");
		archivoClass.delete();
		
	}

}
