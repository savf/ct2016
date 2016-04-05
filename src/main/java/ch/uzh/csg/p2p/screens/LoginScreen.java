package main.java.ch.uzh.csg.p2p.screens;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import main.java.ch.uzh.csg.p2p.Node;

public class LoginScreen extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static String FRAMETITLE = "Login Screen";
	private final int DEFAULTPORT = 4000;
	
	private JLabel idLabel;
	private JTextField idTextField;
	private JLabel localPortLabel;
	private JTextField localPortTextField;
	private JLabel ipLabel;
	private JTextField ipTextField;
	private JLabel remotePortLabel;
	private JTextField remotePortTextField;
	private JButton newNetworkButton;
	private JButton enterNetworkButton;
	
	private Node node;

	public LoginScreen(){
		super(FRAMETITLE);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.setSize(400, 400);
		this.setPreferredSize(new Dimension(400, 400));
		this.setLayout(new GridLayout(0, 2));
		
		initComponents();
		pack();

		this.addComponentListener(new ComponentAdapter(){
			@Override
            public void componentHidden(ComponentEvent e) {
				if(node != null) {
					node.shutdown();
				}
                ((JFrame)(e.getComponent())).dispose();
            }
		});
		
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setVisible(true);
	}
	
	private void initComponents(){
		idLabel = new JLabel("ID (integer only):");
		idTextField = new JTextField();
		
		localPortLabel = new JLabel("Port for local peer:");
		localPortTextField = new JTextField();
		
		ipLabel = new JLabel("IP of remote peer (empty to create new network):");
		ipTextField = new JTextField();
		
		remotePortLabel = new JLabel("Port for remote peer:");
		remotePortTextField = new JTextField();
		
		newNetworkButton = new JButton("New network");
		newNetworkButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				try {
					
					int id = !idTextField.getText().equals("") ? Integer.parseInt(idTextField.getText()) : ((Long)System.currentTimeMillis()).intValue();
					if(!localPortTextField.getText().equals("")) {
						int localport = Integer.parseInt(localPortTextField.getText());
						node = new Node(id, localport, null, 0);
					}
					else {
						node = new Node(id, DEFAULTPORT, null, 0);
						//JFrame frame = (JFrame) SwingUtilities.getRoot((Component) e.getSource());
						//JOptionPane.showMessageDialog(frame, "Local port cannot be empty.");
					}
				} catch (Exception e1) {}
			}
		});
		
		enterNetworkButton = new JButton("Join Network");
		enterNetworkButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				try {
					int id = !idTextField.getText().equals("") ? Integer.parseInt(idTextField.getText()) : ((Long)System.currentTimeMillis()).intValue();
					String ip = ipTextField.getText();
					if(!ip.equals("") && !remotePortTextField.getText().equals("") && !localPortTextField.getText().equals("")){
						int localPort = Integer.parseInt(localPortTextField.getText());
						int remotePort = Integer.parseInt(remotePortTextField.getText());
						node = new Node(id, localPort, ip, remotePort);
					}else{
						JFrame frame = (JFrame) SwingUtilities.getRoot((Component) e.getSource());
						JOptionPane.showMessageDialog(frame, "Remote IP, remote port and local port cannot be empty.");
					}
				} catch (Exception e1) {}
			}
		});
		
		this.add(idLabel);
		this.add(idTextField);
		this.add(localPortLabel);
		this.add(localPortTextField);
		this.add(ipLabel);
		this.add(ipTextField);
		this.add(remotePortLabel);
		this.add(remotePortTextField);
		this.add(newNetworkButton);
		this.add(enterNetworkButton);
	}
	
}
