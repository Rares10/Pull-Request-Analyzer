package org.analyzer.pullrequestanalyzer;

import org.analyzer.pullrequestanalyzer.domain.ProjectData;
import org.analyzer.pullrequestanalyzer.logic.DataParser;
import org.analyzer.pullrequestanalyzer.logic.DataRegister;
import org.analyzer.pullrequestanalyzer.logic.analyzer.DataAnalyzer;
import org.analyzer.pullrequestanalyzer.logic.loader.DataLoader;
import org.analyzer.pullrequestanalyzer.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Enumeration;

@Service
public class Main extends JPanel  implements ActionListener {

    @Qualifier("github")
    private DataLoader gitHubDataLoader;
    @Qualifier("bitbucket")
    private DataLoader bitBucketDataLoader;
    private DataLoader dataLoader;
    private DataRegister dataRegister;
    private DataAnalyzer dataAnalyzer;
    private DataParser dataParser;
    private ProjectRepository projectRepository;

    private JFrame frame;
    private JLabel choice;
    private JRadioButton option1;
    private JRadioButton option2;
    private JRadioButton option3;
    private ButtonGroup options;
    private JLabel gitSolution;
    private JRadioButton gitSolution1;
    private JRadioButton gitSolution2;
    private ButtonGroup gitSolutions;
    private JLabel username;
    private JTextField usernameText;
    private JLabel password;
    private JTextField passwordText;
    private JLabel owner;
    private JTextField ownerText;
    private JLabel repo;
    private JTextField repoText;
    private JLabel repoToAnalyze;
    private JTextField repoToAnalyzeText;
    private JButton start;
    private int optionSelected;

    @Autowired
    public Main(@Qualifier("github") DataLoader gitHubDataLoader,
                @Qualifier("bitbucket") DataLoader bitBucketDataLoader,
                DataRegister dataRegister,
                DataAnalyzer dataAnalyzer,
                ProjectRepository projectRepository,
                DataParser dataParser) {
        this.gitHubDataLoader = gitHubDataLoader;
        this.bitBucketDataLoader = bitBucketDataLoader;
        this.dataRegister = dataRegister;
        this.dataAnalyzer = dataAnalyzer;
        this.projectRepository = projectRepository;
        this.dataParser = dataParser;
        initComponents();
    }

    private void initComponents() {

        frame = new JFrame("Pull Request Analyzer");
        choice = new JLabel("What would you like to do?");
        gitSolution = new JLabel("What type of repository to you want to load?");
        username = new JLabel("Username: ");
        usernameText = new JTextField();
        password = new JLabel("Password: ");
        passwordText = new JPasswordField();
        owner = new JLabel("Repository owner: ");
        ownerText = new JTextField();
        repo = new JLabel("Repository name: ");
        repoText = new JTextField();
        repoToAnalyze = new JLabel("Name of repository to analyze: ");
        repoToAnalyzeText = new JTextField();
        start = new JButton("Start");
        start.addActionListener(this);
        start.setActionCommand("start");

        JPanel optionsQuestionComponent = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
        optionsQuestionComponent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel optionsComponent = new JPanel();
        optionsComponent.setLayout(new BoxLayout(optionsComponent, BoxLayout.Y_AXIS));
        optionsComponent.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        optionsComponent.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JPanel gitQuestionComponent = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gitQuestionComponent.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
        gitQuestionComponent.add(gitSolution);
        JPanel gitSolutionsComponent = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gitSolutionsComponent.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JPanel detailsComponent = new JPanel();
        detailsComponent.setLayout(new BoxLayout(detailsComponent, BoxLayout.Y_AXIS));
        detailsComponent.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        JPanel startComponent = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startComponent.add(start);
        startComponent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        optionsQuestionComponent.add(choice);
        detailsComponent.add(username);
        detailsComponent.add(usernameText);
        detailsComponent.add(password);
        detailsComponent.add(passwordText);
        detailsComponent.add(owner);
        detailsComponent.add(ownerText);
        detailsComponent.add(repo);
        detailsComponent.add(repoText);
        detailsComponent.add(repoToAnalyze);
        detailsComponent.add(repoToAnalyzeText);

        options = new ButtonGroup();
        option1 = new JRadioButton("Load a new project to database" );
        option1.addActionListener(this);
        option1.setActionCommand("option1");
        options.add(option1);
        option2 = new JRadioButton("Load a new project to database and analyze it" );
        option2.addActionListener(this);
        option2.setActionCommand("option2");
        options.add(option2);
        option3 = new JRadioButton("Analyze an existing project" );
        option3.addActionListener(this);
        option3.setActionCommand("option3");
        options.add(option3);

        optionsComponent.add(option1);
        optionsComponent.add(option2);
        optionsComponent.add(option3);

        gitSolutions = new ButtonGroup();
        gitSolution1 = new JRadioButton("GitHub");
        gitSolution1.addActionListener(this);
        gitSolution1.setActionCommand("gitSolution1");
        gitSolution2 = new JRadioButton("Bitbucket");
        gitSolution2.addActionListener(this);
        gitSolution2.setActionCommand("gitSolution2");
        gitSolutions.add(gitSolution1);
        gitSolutions.add(gitSolution2);
        gitSolutionsComponent.add(gitSolution1);
        gitSolutionsComponent.add(gitSolution2);

        setDetailsInvisible();

        Container ui = frame.getContentPane();
        ui.setLayout(new BoxLayout( ui, BoxLayout.Y_AXIS));

        ui.add(optionsQuestionComponent);
        ui.add(optionsComponent);
        ui.add(gitQuestionComponent);
        ui.add(gitSolutionsComponent);
        ui.add(detailsComponent);
        ui.add(startComponent);
        add(ui);
    }

    @PostConstruct
    public void execute() throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                frame.setResizable(true);
                frame.pack();
                showGUI(frame);
            }
        });
    }

    private void showGUI(JFrame frame) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setOpaque(true);
        frame.setContentPane(this);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    public void actionPerformed(ActionEvent e) {

        String cmd = e.getActionCommand();

        if (cmd.equals("option1")) {
            setDetailsVisible();
            repoToAnalyze.setVisible(false);
            repoToAnalyzeText.setVisible(false);
            optionSelected = 1;
        }
        if (cmd.equals("option2")) {
            setDetailsVisible();
            repoToAnalyze.setVisible(false);
            repoToAnalyzeText.setVisible(false);
            optionSelected = 2;
        }
        if (cmd.equals("option3")) {
            setDetailsInvisible();
            repoToAnalyze.setVisible(true);
            repoToAnalyzeText.setVisible(true);
            start.setVisible(true);
            frame.pack();
            optionSelected = 3;
        }
        if (cmd.equals("gitSolution1")) {
            this.dataLoader = gitHubDataLoader;
        }
        if (cmd.equals("gitSolution2")) {
            this.dataLoader = bitBucketDataLoader;
        }
        if (cmd.equals("start")) {
            try {
                startAction(optionSelected);
            } catch (ParseException | InterruptedException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void startAction(int optionSelected) throws ParseException, InterruptedException, IOException {

        switch (optionSelected) {
            case 1 : option1Selected(); break;
            case 2 : option2Selected(); break;
            case 3 : option3Selected(); break;
        }

    }

    private void option1Selected() throws ParseException, InterruptedException, IOException {

        if (gitOptionNotSelected())
            return;

        if (usernameText.getText().equals("") || passwordText.getText().equals("")  || ownerText.getText().equals("") || repoText.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Please fill in all the data!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dataLoader.setUsername(usernameText.getText());
        dataLoader.setPassword(passwordText.getText());
        dataLoader.setOwner(ownerText.getText());
        dataLoader.setRepository(repoText.getText());
        dataLoader.loadData();
    }

    private void option2Selected() throws ParseException, InterruptedException, IOException {

        if (gitOptionNotSelected())
            return;
        if (usernameText.getText().equals("") || passwordText.getText().equals("")  || ownerText.getText().equals("") || repoText.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Please fill in all the data!", "Error", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        dataLoader.setUsername(usernameText.getText());
        dataLoader.setPassword(passwordText.getText());
        dataLoader.setOwner(ownerText.getText());
        dataLoader.setRepository(repoText.getText());
        dataLoader.loadData();
        ProjectData projectToAnalyze = projectRepository.findByRepository(repoText.getText());
        if (projectToAnalyze == null) {
            JOptionPane.showMessageDialog(null, "Repository to analyze does not exist in database!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dataRegister.setProjectToAnalyze(projectRepository.findByRepository(repoText.getText()));
        //dataRegister.registerData();
        dataAnalyzer.setProjectToAnalyze(projectRepository.findByRepository(repoText.getText()));
        //dataAnalyzer.analyzeData();
        //dataParser.parseData();
    }

    private void option3Selected() throws IOException {

        if (repoToAnalyzeText.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Please enter a repository name!", "Error", JOptionPane.PLAIN_MESSAGE);
            return;
        }

        ProjectData projectToAnalyze = projectRepository.findByRepository(repoToAnalyzeText.getText());

        if (projectToAnalyze == null) {
            JOptionPane.showMessageDialog(null, "Project does not exist in database!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dataRegister.setProjectToAnalyze(projectRepository.findByRepository(repoToAnalyzeText.getText()));
        dataRegister.registerData();
        dataAnalyzer.setProjectToAnalyze(projectRepository.findByRepository(repoToAnalyzeText.getText()));
        dataAnalyzer.analyzeData();
        dataParser.parseData();
    }

    private boolean gitOptionNotSelected() {

        boolean notChecked = true;
        for (Enumeration<AbstractButton> buttons = gitSolutions.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if( button.isSelected() )
                notChecked = false;
        }
        if(notChecked) {
            JOptionPane.showMessageDialog(null, "Please choose a git solution!", "Error",JOptionPane.PLAIN_MESSAGE);
            return true;
        }
        return false;
    }

    private void setDetailsVisible() {

        gitSolution.setVisible(true);
        gitSolution1.setVisible(true);
        gitSolution2.setVisible(true);
        username.setVisible(true);
        usernameText.setVisible(true);
        password.setVisible(true);
        passwordText.setVisible(true);
        owner.setVisible(true);
        ownerText.setVisible(true);
        repo.setVisible(true);
        repoText.setVisible(true);
        start.setVisible(true);
        frame.pack();
    }

    private void setDetailsInvisible() {

        gitSolution.setVisible(false);
        gitSolution1.setVisible(false);
        gitSolution2.setVisible(false);
        username.setVisible(false);
        usernameText.setVisible(false);
        password.setVisible(false);
        passwordText.setVisible(false);
        owner.setVisible(false);
        ownerText.setVisible(false);
        repo.setVisible(false);
        repoText.setVisible(false);
        repoToAnalyze.setVisible(false);
        repoToAnalyzeText.setVisible(false);
        start.setVisible(false);
        frame.pack();
    }
}