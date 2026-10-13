import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

// CLASS: Bin
class Bin {
    private String binID;
    private String location;
    private String fillLevel;

    public Bin(String binID, String location, String fillLevel) {
        this.binID = binID;
        this.location = location;
        this.fillLevel = fillLevel; // constructor = initialize the object when they created
    }

    public void updateFillLevel(String level) {
        this.fillLevel = level;
    }

    public boolean isFull() {
        return fillLevel.equalsIgnoreCase("Full");
    }

    public String getInfo() {
        return "BinID: " + binID + " | Location: " + location + " | Fill Level: " + fillLevel;
    }

    public String getBinID() { return binID; }
    public String getLocation() { return location; }
    public String getFillLevel() { return fillLevel; }
}

// CLASS: Truck
class Truck {
    private String truckID;
    private ArrayList<Bin> route = new ArrayList<>(); // collect the number of bins
    private int capacity = 20; // one truck carry 20 Bins

    public Truck(String truckID) {
        this.truckID = truckID;
    }

    public void assignBin(Bin bin) {  // Adding bins into route
        if (!isFull()) {   // add if the truck is not full
            route.add(bin);
        }
    }

    public boolean isFull() { //checks the capacity if it exceeded 20 return true
        return route.size() >= capacity;
    }

    public String getTruckID() { return truckID; } // getters to access the id and bins
    public ArrayList<Bin> getRoute() { return route; }
}

//  CLASS: Scheduler
class Scheduler {
    private ArrayList<Bin> bins;
    private ArrayList<Truck> trucks;

    public Scheduler(ArrayList<Bin> bins, ArrayList<Truck> trucks) {
        this.bins = bins;
        this.trucks = trucks;
    }

    public void generateSchedule() {
        if (trucks.isEmpty()) return; // if there are no trucks stop

        ArrayList<Bin> fullBins = new ArrayList<>();
        for (Bin b : bins) { // checks the bin;if bin is full assigned into fullBins
            if (b.isFull()) fullBins.add(b);
        }

        int truckIndex = 0;
        for (Bin b : fullBins) {
            Truck currentTruck = trucks.get(truckIndex); //get the current truc using
            currentTruck.assignBin(b); // Assigning the bin to the trucks

            if (currentTruck.isFull() && truckIndex < trucks.size() - 1) {
                truckIndex++; // truck is full assigned to the next truck
            }
        }
    }
}

// ABSTRACT CLASS: User
abstract class User {
    protected String username;
    protected String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean login(String u, String p) {
        return username.equals(u) && password.equals(p);
    }

    public abstract void showDashboard();
}

// CLASS: Admin
class Admin extends User {
    private ArrayList<Bin> bins = new ArrayList<>();
    private ArrayList<Truck> trucks = new ArrayList<>();

    public Admin(String username, String password) {
        super(username, password); //super calls the constructor of the parent class(user)
    }

    public void addBin(Bin bin) {
        bins.add(bin);
    }

    public void addTruck(Truck truck) {
        trucks.add(truck);
    }

    public ArrayList<Bin> getBins() { return bins; }
    public ArrayList<Truck> getTrucks() { return trucks; }

    @Override
    public void showDashboard() {
        new AdminDashboard(this);
    }
}

// CLASS: Driver
class Driver extends User {
    private ArrayList<Bin> assignedBins = new ArrayList<>();

    public Driver(String username, String password) {
        super(username, password);
    }

    public ArrayList<Bin> getAssignedBins() { return assignedBins; }

    public void setAssignedBins(ArrayList<Bin> bins) {
        assignedBins = bins;
    }

    @Override
    public void showDashboard() {
        new DriverDashboard(this);
    }
}

//CLASS: LoginPage
class LoginPage extends JFrame implements ActionListener {
    JTextField userField;
    JPasswordField passField;
    JComboBox<String> roleBox;
    JButton loginBtn, exitBtn;

    static Admin admin = new Admin("admin", "1234");
    static Driver driver = new Driver("driver", "1234");

    public LoginPage() {
        setTitle("Smart Waste Scheduler - Login");
        setSize(400, 350);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Smart Waste Scheduler", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));

        formPanel.add(new JLabel("Username:"));
        userField = new JTextField();
        formPanel.add(userField);

        formPanel.add(new JLabel("Password:"));
        passField = new JPasswordField();
        formPanel.add(passField);

        formPanel.add(new JLabel("Role:"));
        roleBox = new JComboBox<>(new String[]{"Select", "Admin", "Driver"});
        formPanel.add(roleBox);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 100, 20, 100));

        loginBtn = new JButton("Login");
        exitBtn = new JButton("Exit");

        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(loginBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(exitBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        loginBtn.addActionListener(this);
        exitBtn.addActionListener(e -> System.exit(0));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public void actionPerformed(ActionEvent e) {
        String user = userField.getText();
        String pass = new String(passField.getPassword());
        String role = (String) roleBox.getSelectedItem();

        if (role.equals("Admin")) {
            if (admin.login(user, pass)) {
                dispose();
                admin.showDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Admin login!");
            }
        } else if (role.equals("Driver")) {
            if (driver.login(user, pass)) {
                dispose();
                driver.showDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Driver login!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a role.");
        }
    }
}

//CLASS: AdminDashboard
class AdminDashboard extends JFrame implements ActionListener {
    Admin admin;
    JButton addBinBtn, addTruckBtn, scheduleBtn, backBtn, exitBtn;

    public AdminDashboard(Admin admin) {
        this.admin = admin;
        setTitle("Admin Dashboard");
        setSize(400, 400);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));

        addBinBtn = new JButton("Add Bin");
        addTruckBtn = new JButton("Add Truck");
        scheduleBtn = new JButton("Generate Schedule");
        backBtn = new JButton("Back to Login");
        exitBtn = new JButton("Exit Application");

        Font btnFont = new Font("Arial", Font.PLAIN, 16);
        JButton[] allBtns = {addBinBtn, addTruckBtn, scheduleBtn, backBtn, exitBtn};
        for (JButton b : allBtns) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setFont(btnFont);
            b.setMaximumSize(new Dimension(250, 40));
            b.setFocusPainted(false);
            buttonPanel.add(b);
            buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        add(buttonPanel, BorderLayout.CENTER);

        addBinBtn.addActionListener(this);
        addTruckBtn.addActionListener(this);
        scheduleBtn.addActionListener(this);
        backBtn.addActionListener(e -> { dispose(); new LoginPage(); });
        exitBtn.addActionListener(e -> System.exit(0));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addBinBtn) {
            if (admin.getBins().size() >= 50) {
                JOptionPane.showMessageDialog(this, "You can only add up to 50 bins!");
                return;
            }

            JTextField idField = new JTextField();
            JTextField locField = new JTextField();
            JComboBox<String> levelBox = new JComboBox<>(new String[]{"Empty", "Half-full", "Full"});

            JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
            panel.add(new JLabel("Bin ID:"));
            panel.add(idField);
            panel.add(new JLabel("Location:"));
            panel.add(locField);
            panel.add(new JLabel("Fill Level:"));
            panel.add(levelBox);

            // This version includes the Question Mark
            int result = JOptionPane.showOptionDialog(
                    this,panel,
                    "Enter Bin Details",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, // ❓ brings back the question mark icon
                    null,
                    null,
                    null
            );

            if (result == JOptionPane.OK_OPTION) {
                String id = idField.getText().trim();
                String loc = locField.getText().trim();
                String level = (String) levelBox.getSelectedItem();

                if (id.isEmpty() || loc.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill all fields!");
                    return;
                }

                admin.addBin(new Bin(id, loc, level));
                JOptionPane.showMessageDialog(this, "Bin Added Successfully! (" + admin.getBins().size() + "/50)");
            }
        } else if (e.getSource() == addTruckBtn) {
            String truckID = "T" + (admin.getTrucks().size() + 1);
            admin.addTruck(new Truck(truckID));
            JOptionPane.showMessageDialog(this, "Truck Added! (ID: " + truckID + ")");
        } else if (e.getSource() == scheduleBtn) {
            Scheduler s = new Scheduler(admin.getBins(), admin.getTrucks());
            s.generateSchedule();

            ArrayList<Bin> fullBins = new ArrayList<>();
            for (Bin b : admin.getBins()) {
                if (b.isFull()) fullBins.add(b);
            }
            LoginPage.driver.setAssignedBins(fullBins);

            JOptionPane.showMessageDialog(this, "Schedule Generated! (Driver updated)");
        }
    }
}

//CLASS: DriverDashboard
class DriverDashboard extends JFrame {
    public DriverDashboard(Driver driver) {
        setTitle("Driver Dashboard");
        setSize(400, 300);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Driver Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JTextArea area = new JTextArea();
        area.setEditable(false);

        StringBuilder sb = new StringBuilder("Assigned Bins:\n\n");
        if (driver.getAssignedBins().isEmpty()) {
            sb.append("No bins assigned yet.\n");
        } else {
            for (Bin b : driver.getAssignedBins()) {
                sb.append(b.getInfo()).append("\n");
            }
        }
        area.setText(sb.toString());

        add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton backBtn = new JButton("Back");
        JButton exitBtn = new JButton("Exit");
        bottomPanel.add(backBtn);
        bottomPanel.add(exitBtn);

        backBtn.addActionListener(e -> { dispose(); new LoginPage(); });
        exitBtn.addActionListener(e -> System.exit(0));

        add(bottomPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setLocationRelativeTo(null);
    }
}

//MAIN FUNCTION
public class SmartWasteScheduler {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage());
    }
}