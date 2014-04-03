package rita.network;

import java.io.Serializable;

/**
 * Clase Mensaje que contiene la informacion de lo que se envia o recibe.
 * @author P-M
 * 
 */
public class Mensaje implements Serializable {
    
	private static final long serialVersionUID = 1L;
	
	/** Valor de la accion que se realiza */
   	public String accion = "";
   	public String cliente = "";

   	public Mensaje(String unaAccion, String unCliente){
   		accion = unaAccion;
   		cliente = unCliente;
   	}
}
