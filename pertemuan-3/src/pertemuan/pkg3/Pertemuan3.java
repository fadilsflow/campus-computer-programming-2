package pertemuan.pkg3;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

/**
 * @author fadils
 */
public class Pertemuan3 extends JFrame {

    private static final String API_KEY = "1b932005eeba5e1c9bb6560908bdf2c0";
    private static final String API_URL = "https://api.exchangerate.host/live?access_key=" + API_KEY + "&currencies=USD,AUD,CAD,PLN,MXN&format=1";

    private JTextArea resultTextArea;
    private JButton refreshButton;
    private JProgressBar progressBar;
    private JComboBox<String> currencyComboBox;
    private JTextField amountTextField;
    private JButton convertButton;
    private JLabel resultLabel;

    private final OkHttpClient client = new OkHttpClient();
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private JSONObject ratesData;

    public Pertemuan3() {
        setTitle("Currency Exchange App - Multi-threaded");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initComponents();
        initListeners();

        setLocationRelativeTo(null);
        setVisible(true);

        // Load data on startup
        fetchExchangeRates();
    }

    private void initComponents() {
        // North Panel
        JPanel northPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Currency Exchange Rates", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        northPanel.add(titleLabel, BorderLayout.CENTER);

        refreshButton = new JButton("Refresh Data");
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(refreshButton, BorderLayout.WEST);
        buttonPanel.add(progressBar, BorderLayout.CENTER);
        northPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);

        // Center Panel
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        add(scrollPane, BorderLayout.CENTER);

        // South Panel - Converter
        JPanel southPanel = new JPanel(new GridLayout(3, 1));

        JPanel conversionPanel = new JPanel(new FlowLayout());
        conversionPanel.add(new JLabel("Convert: "));

        amountTextField = new JTextField(10);
        conversionPanel.add(amountTextField);

        currencyComboBox = new JComboBox<>(new String[]{"USD", "AUD", "CAD", "PLN", "MXN"});
        conversionPanel.add(currencyComboBox);

        convertButton = new JButton("Convert to EUR");
        conversionPanel.add(convertButton);

        southPanel.add(conversionPanel);

        resultLabel = new JLabel("Result will be shown here", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 14));
        southPanel.add(resultLabel);

        add(southPanel, BorderLayout.SOUTH);
    }

    private void initListeners() {
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchExchangeRates();
            }
        });

        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertCurrency();
            }
        });
    }

    private void fetchExchangeRates() {
        refreshButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setString("Loading data...");
        resultTextArea.setText("Fetching currency exchange rates...");

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Request request = new Request.Builder()
                            .url(API_URL)
                            .build();

                    try (Response response = client.newCall(request).execute()) {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        // Process on EDT to update UI safely
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                displayResults(jsonResponse);
                                progressBar.setIndeterminate(false);
                                progressBar.setValue(100);
                                progressBar.setString("Data loaded successfully");
                                refreshButton.setEnabled(true);
                            }
                        });
                    }
                } catch (IOException e) {
                    // Handle error on EDT
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            resultTextArea.setText("Error fetching data: " + e.getMessage());
                            progressBar.setIndeterminate(false);
                            progressBar.setValue(0);
                            progressBar.setString("Failed to load data");
                            refreshButton.setEnabled(true);
                        }
                    });
                }
            }
        });
    }

    private void displayResults(JSONObject jsonResponse) {
        if (jsonResponse.getBoolean("success")) {
            ratesData = jsonResponse.getJSONObject("quotes");

            StringBuilder sb = new StringBuilder();
            sb.append("Base Currency: ").append(jsonResponse.getString("source")).append("\n");
            sb.append("Timestamp: ").append(jsonResponse.getLong("timestamp")).append("\n\n");
            sb.append(String.format("%-10s %-15s\n", "Currency", "Exchange Rate"));
            sb.append("---------------------------\n");

            // Extract and display each rate
            for (String currency : new String[]{"USD", "AUD", "CAD", "PLN", "MXN"}) {
                String key = "USD" + currency;
                if (ratesData.has(key)) {
                    double rate = ratesData.getDouble(key);
                    sb.append(String.format("%-10s %-15.6f\n", currency, rate));
                }
            }

            resultTextArea.setText(sb.toString());
        } else {
            resultTextArea.setText("API Error: Unable to fetch exchange rates.");
        }
    }

    private void convertCurrency() {
        if (ratesData == null) {
            resultLabel.setText("Please fetch exchange rates first!");
            return;
        }

        try {
            final double amount = Double.parseDouble(amountTextField.getText());
            final String selectedCurrency = (String) currencyComboBox.getSelectedItem();

            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Simulate complex calculation
                        Thread.sleep(500);

                        final String key = "USD" + selectedCurrency;
                        if (ratesData.has(key)) {
                            final double rate = ratesData.getDouble(key);
                            final double result = amount * rate;

                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    DecimalFormat df = new DecimalFormat("#.##");
                                    resultLabel.setText(amount + " USD = " + df.format(result) + " " + selectedCurrency);
                                }
                            });
                        } else {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    resultLabel.setText("Exchange rate not available for " + selectedCurrency);
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        } catch (NumberFormatException e) {
            resultLabel.setText("Please enter a valid number!");
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Start application on EDT
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Pertemuan3();
            }
        });
    }
}
