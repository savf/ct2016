package ch.uzh.csg.p2p.screens;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import ch.uzh.csg.p2p.Node;

public class LoginScreen extends JFrame{
	
	private final static String FRAMETITLE = "Login Screen";
	
	private JLabel idLabel;
	private JTextField idTextField;
	private JLabel ipLabel;
	private JTextField ipTextField;
	private JButton newNetworkButton;
	private JButton enterNetworkButton;
	
	private Node node;

	public LoginScreen(){
		super(FRAMETITLE);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(400, 400);
		this.setPreferredSize(new Dimension(400, 400));
		this.setLayout(new GridLayout(0, 2));
		
		initComponents();
		pack();

		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setVisible(true);
	}
	
	private void initComponents(){
		idLabel = new JLabel("ID (Muss ein Integer sein):");
		idTextField = new JTextField();
		
		ipLabel = new JLabel("IP von einem bekannten Node (Leer für neues Netzwerk erstellen):");
		ipTextField = new JTextField();
		
		newNetworkButton = new JButton("Neues Netzwerk");
		newNetworkButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				try {
					int id = !idTextField.getText().equals("") ? Integer.parseInt(idTextField.getText()) : ((Long)System.currentTimeMillis()).intValue();
					node = new Node(id, null);
				} catch (IOException e1) {}
			}
		});
		
		enterNetworkButton = new JButton("Netzwerk beitretten");
		enterNetworkButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				try {
					int id = !idTextField.getText().equals("") ? Integer.parseInt(idTextField.getText()) : ((Long)System.currentTimeMillis()).intValue();
					String ip = ipTextField.getText();
					if(!ip.equals("")){
						node = new Node(id, ip);
					}else{
						//TODO warning ip not set
					}
				} catch (IOException e1) {}
			}
		});
		
		this.add(idLabel);
		this.add(idTextField);
		this.add(ipLabel);
		this.add(ipTextField);
		this.add(newNetworkButton);
		this.add(enterNetworkButton);
	}
	
}
