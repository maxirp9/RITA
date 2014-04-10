package rita.ui.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import rita.network.CantidadConexionesObservable;
import rita.network.ConexionServidor;
import rita.network.ServerRita;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JTextField;

public class DialogServerRita extends JDialog implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6132731169207934920L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private CantidadConexionesObservable cantidadConexionesObservable;
	private ServerRita server;

	public DialogServerRita(java.awt.Frame parent, String titulo, boolean modal) {
		super(parent);
		initialize();
	}
	
	private void initialize(){
		try {
			cantidadConexionesObservable = new CantidadConexionesObservable();
			cantidadConexionesObservable.addObserver(this);
			/*DialogServerRita dialog = new DialogServerRita();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);*/
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			this.setVisible(true);
			correr();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Launch the application.
	 */
	/*public static void main(String[] args) {
		try {
			DialogServerRita dialog = new DialogServerRita();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	/**
	 * Create the dialog.
	 * @return 
	 */
	public void correr() {
		
		setBounds(100, 100, 450, 265);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JLabel label = new JLabel("Puerto:");
			label.setBounds(54, 39, 53, 15);
			contentPanel.add(label);
		}
		{
			textField = new JTextField();
			textField.setBounds(112, 39, 114, 19);
			textField.setText("1234");
			textField.setColumns(10);
			contentPanel.add(textField);
		}
		
		JLabel lblIpServidor = new JLabel("IP Servidor:");
		lblIpServidor.setBounds(54, 12, 91, 15);
		contentPanel.add(lblIpServidor);
		
		final JLabel labelIPServidorValor = new JLabel(traerIp());
		labelIPServidorValor.setBounds(156, 12, 125, 15);
		contentPanel.add(labelIPServidorValor);
		{
			JLabel lblClientesConectados = new JLabel("Clientes conectados:");
			lblClientesConectados.setBounds(54, 77, 164, 15);
			contentPanel.add(lblClientesConectados);
		}
		{
			JLabel lblClientesConectadosValor = new JLabel("0");
			lblClientesConectadosValor.setBounds(230, 77, 34, 15);
			contentPanel.add(lblClientesConectadosValor);
		}
		
		JButton btnEjecutarBatalla = new JButton("Ejecutar Batalla");
		btnEjecutarBatalla.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				server.setIniciarBatalla(true);
				try {
					Socket socket = new Socket(traerIp(), server.getPortNumber());
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		btnEjecutarBatalla.setBounds(54, 115, 146, 25);
		contentPanel.add(btnEjecutarBatalla);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Correr servidor");
				server = ServerRita.getInstance(1234,cantidadConexionesObservable);//new ServerRita(1234,cantidadConexionesObservable);
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						server.start();						
					}
				});
				okButton.setActionCommand("Correr servidor");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton btnPararServidor = new JButton("Parar Servidor");
				btnPararServidor.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						server.stopServer();
					}
				});
				buttonPane.add(btnPararServidor);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	@Override
	public void update(Observable obs, Object data) {
		System.out.println(Thread.currentThread().getId());
		JLabel lbl  = (JLabel) contentPanel.getComponent(5);
		lbl.setText(data.toString());
		lbl.validate();
		lbl.repaint();	
		
	}
	
	public String traerIp(){
		Enumeration<NetworkInterface> interfaces = null;
		String ip = "";
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		while (interfaces.hasMoreElements()){
		    NetworkInterface current = interfaces.nextElement();
		    //System.out.println("1er " + current);		    
		    if (current.getName().startsWith("eth") || current.getName().startsWith("wlan") ){
			    try {
					if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
				} catch (SocketException e) {
					e.printStackTrace();
				}
			    Enumeration<InetAddress> addresses = current.getInetAddresses();
			    while (addresses.hasMoreElements()){
			        InetAddress current_addr = addresses.nextElement();
			        if (current_addr instanceof Inet4Address)			        	
			        	ip = current_addr.getHostAddress().toString();
			        if (current_addr.isLoopbackAddress()) continue;
			        
			    }
			}
		}
		return ip;
	}
	
}
