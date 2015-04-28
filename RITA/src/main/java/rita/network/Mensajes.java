
package rita.network;

import java.util.ArrayList;
import java.util.Observable;

/**
 * Objeto observable del patron observer.
 * 
 */

public class Mensajes extends Observable{

    private String mensaje;
    private int cantidadRobots = 0;
    private int cantidadConexiones = 0;
    private int cierreWorkers = 0;
    

	private ArrayList<String> robotsEnBatalla;
    
    public Mensajes(){
    	robotsEnBatalla = new ArrayList<String>();
    }
    
    public String getMensaje(){
        return mensaje;
    }
    
    public void setMensaje(String mensaje){
        this.mensaje = mensaje;
        // Indica que el mensaje ha cambiado
        //this.setChanged();
        // Notifica a los observadores que el mensaje ha cambiado y se lo pasa
        // (Internamente notifyObservers llama al metodo update del observador)
        //this.notifyObservers(this.getMensaje());
    }

	public int getCantidadRobots() {
		return cantidadRobots;
	}

	public void setCantidadRobots(int cantidadRobots) {
		this.cantidadRobots = cantidadRobots;
//		this.setChanged();
        // Notifica a los observadores que el mensaje ha cambiado y se lo pasa
        // (Internamente notifyObservers llama al metodo update del observador)
//        this.notifyObservers(this.getCantidadRobots());
	}
	
	public void incrementarCantidadRobots(){
		cantidadRobots++;
	}

	public int getCierreWorkers() {
		return cierreWorkers;
	}
	
	public void setCierreWorkers(int cierreWorkers) {
		this.cierreWorkers = cierreWorkers;
	}
	
	public void incrementarCierreWorkers(){
		cierreWorkers++;
	}
	
	public int getCantidadConexiones() {
		return cantidadConexiones;
	}

	public void setCantidadConexiones(int cantidadConexiones) {
		this.cantidadConexiones = cantidadConexiones;
	}
	
	public void incrementarCantidadConexiones(){
		cantidadConexiones++;
	}

	public ArrayList<String> getRobotsEnBatalla() {
		return robotsEnBatalla;
	}

	public void setRobotsEnBatalla(ArrayList<String> robotsEnBatalla) {
		this.robotsEnBatalla = robotsEnBatalla;
	}
	
	public void agregarRobot(String robot){
		robotsEnBatalla.add(robot);
	}
	
}