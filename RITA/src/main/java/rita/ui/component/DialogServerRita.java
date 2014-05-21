package rita.ui.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;

import rita.network.CantidadConexionesObservable;
import rita.network.ClientesConectadosObservable;
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.border.BevelBorder;

import org.eclipse.wb.swing.FocusTraversalOnArray;

import java.awt.Component;

import javax.swing.SwingConstants;

public class DialogServerRita extends JDialog implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6132731169207934920L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textPort;
	private CantidadConexionesObservable cantidadConexionesObservable;
	private ClientesConectadosObservable clientesConectadosObservable;
	private ServerRita server;
	private JButton okButton;
	private JButton btnPararServidor;
	private JLabel lblServerOn;
	private JLabel lblServerOff;

	public DialogServerRita(java.awt.Frame parent, String titulo, boolean modal) {
		super(parent);
		initialize();
	}

	private void initialize() {
		try {
			clientesConectadosObservable = new ClientesConectadosObservable();
			clientesConectadosObservable.addObserver(this);
			/*
			 * DialogServerRita dialog = new DialogServerRita();
			 * dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			 * dialog.setVisible(true);
			 */
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
	/*
	 * public static void main(String[] args) { try { DialogServerRita dialog =
	 * new DialogServerRita();
	 * dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	 * dialog.setVisible(true); } catch (Exception e) { e.printStackTrace(); } }
	 */

	/**
	 * Create the dialog.
	 * 
	 * @return
	 */
	public void correr() {

		setBounds(100, 100, 285, 413);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JLabel lblPort = new JLabel("Puerto:");
			lblPort.setBounds(54, 39, 53, 15);
			contentPanel.add(lblPort);
		}
		{
			textPort = new JTextField();
			textPort.setBounds(112, 39, 114, 19);
			textPort.setText("1234");
			textPort.setColumns(10);
			contentPanel.add(textPort);
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
					Socket socket = new Socket(traerIp(), server
							.getPortNumber());
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		btnEjecutarBatalla.setBounds(71, 315, 146, 25);
		contentPanel.add(btnEjecutarBatalla);
		{
			JList listCientesConectados = new JList();
			listCientesConectados.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			listCientesConectados.setModel(new AbstractListModel() {
				String[] values = new String[] {"No hay conectados..."};
				public int getSize() {
					return values.length;
				}
				public Object getElementAt(int index) {
					return values[index];
				}
			});
			listCientesConectados.setToolTipText("Clientes conectados");
			listCientesConectados.setBounds(54, 134, 164, 169);
			contentPanel.add(listCientesConectados);
		}
		
		JLabel lblUsuariosConectados = new JLabel("Listado de conectados:");
		lblUsuariosConectados.setBounds(54, 104, 173, 18);
		contentPanel.add(lblUsuariosConectados);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				{
					
					lblServerOn = new JLabel(new ImageIcon(DialogServerRita.class.getResource("/images/icons/serverOn.png")));
					lblServerOn.setVisible(false);
					buttonPane.add(lblServerOn);
					
					lblServerOff = new JLabel(new ImageIcon(DialogServerRita.class.getResource("images/icons/serverOff.png")));
					lblServerOff.setVisible(true);
					buttonPane.add(lblServerOff);
					
					btnPararServidor = new JButton("Parar");
					btnPararServidor.setVerticalAlignment(SwingConstants.TOP);
					btnPararServidor.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							//SETEO LOS BOTONES E IMAGEN DE ESTADO DEL SERVIDOR
							lblServerOn.setVisible(false);
							lblServerOff.setVisible(true);
							okButton.setEnabled(true);
							btnPararServidor.setEnabled(false);
							server.stopServer();
							try {
								Socket socket = new Socket(traerIp(), server
										.getPortNumber());
							} catch (UnknownHostException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
							System.out.println("STOP Servidor");
						}
					});
					okButton = new JButton("Iniciar");
					okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							server = ServerRita.getInstance(Integer.valueOf(textPort.getText()),						
									clientesConectadosObservable);
							server.start();
							//SETEO LOS BOTONES E IMAGEN DE ESTADO DEL SERVIDOR
							lblServerOn.setVisible(true);
							lblServerOff.setVisible(false);
							okButton.setEnabled(false);
							btnPararServidor.setEnabled(true);
						}
					});
					buttonPane.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{okButton, btnPararServidor}));
					okButton.setActionCommand("Correr servidor");
					buttonPane.add(okButton);
					getRootPane().setDefaultButton(okButton);
					buttonPane.add(btnPararServidor);
					
					
				}
			}
		}
	}

	@Override
	public void update(Observable obs, Object data) {
		System.out.println(Thread.currentThread().getId());

		if (obs instanceof CantidadConexionesObservable) {
			JLabel lbl = (JLabel) contentPanel.getComponent(5);
			lbl.setText(data.toString());
			lbl.validate();
			lbl.repaint();
		}
		if (obs instanceof ClientesConectadosObservable) {
			JList lst = (JList) contentPanel.getComponent(7);
			ArrayList<String> arrayList = (ArrayList<String>) data;
			//Crear un objeto DefaultListModel
			DefaultListModel listModel = new DefaultListModel();
			//Recorrer el contenido del ArrayList
			for(int i=0; i<arrayList.size(); i++) {
			    //Añadir cada elemento del ArrayList en el modelo de la lista
			    listModel.add(i, arrayList.get(i));
			}
			//Asociar el modelo de lista al JList
			lst.setModel(listModel);

			lst.validate();
			lst.repaint();
			
			JLabel lbl = (JLabel) contentPanel.getComponent(5);
			lbl.setText(String.valueOf(arrayList.size()));
			lbl.validate();
			lbl.repaint();
		}

	}

	public String traerIp() {
		Enumeration<NetworkInterface> interfaces = null;
		String ip = "";
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		while (interfaces.hasMoreElements()) {
			NetworkInterface current = interfaces.nextElement();
			// System.out.println("1er " + current);
			if (current.getName().startsWith("eth")
					|| current.getName().startsWith("wlan")) {
				try {
					if (!current.isUp() || current.isLoopback()
							|| current.isVirtual())
						continue;
				} catch (SocketException e) {
					e.printStackTrace();
				}
				Enumeration<InetAddress> addresses = current.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress current_addr = addresses.nextElement();
					if (current_addr instanceof Inet4Address)
						ip = current_addr.getHostAddress().toString();
					if (current_addr.isLoopbackAddress())
						continue;

				}
			}
		}
		return ip;
	}
}
