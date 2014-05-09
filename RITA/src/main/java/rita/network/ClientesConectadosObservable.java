package rita.network;

import java.util.Observable;

public class ClientesConectadosObservable extends Observable {

	public ClientesConectadosObservable() {
		super();
	}
	
	void changeData(Object data) {
        setChanged(); // the two methods of Observable class
        notifyObservers(data);
    }
}
