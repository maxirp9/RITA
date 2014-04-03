package rita.network;

import java.util.Observable;

public class CantidadConexionesObservable extends Observable {

	public CantidadConexionesObservable() {
		super();
	}
	
	void changeData(Object data) {
        setChanged(); // the two methods of Observable class
        notifyObservers(data);
    }
}
