import java.util.Random;
import java.awt.event.*;
import javax.swing.*;

/**
 * Work in progress
 *
 * Eigenes JFrame anstatt Zeichenfensterklasse testen:
 *
 * public class MainFrame extends javax.swing.JFrame {
 *
 *     public static void main(String[] args) { methode();}
 *
 *     public static void methode() {
 *             MainFrame frame = new MainFrame();
 *             frame.setTitle("Word Cloud");
 *             frame.setSize(1000, 620);
 *             frame.setResizable(true);
 *             frame.setLocation(50, 50);
 *             frame.setVisible(true);
 *     }
 *
 * }
 * (wichtig: frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE); -> programm beenden wenn fenster geschlossen)
 *
 * alternative: fxml
 */
public class Field {
    private final Outwall oTop;
    private final Outwall oRight;
    private final Outwall oBottom;
    private final Outwall oLeft;

    private Space[] spaces;

    private final FishingRod rod;
    private int rodColor;
    private boolean fishOnRod;
    private int caughtFishCount;
    private int caughtTrashCount;
    private int currentFishAmount;
    private int soldFishCount;
    private int balance;
    private int baitAmount;
    private int decorLevel;

    private int autoFishingState;
    private boolean isAutoFishing;
    private boolean unlockedAutoCheat;

    private final Random rdm = new Random();
    private JButton moveButton; //Steuerung mit Tastatur recherchiern -> weniger Buttons!
    private JButton autoButton;
    private JButton sellButton;
    private JButton sellAllButton;
    private JButton upgradeButton;
    private JButton colorButton;
    private JButton decorButton;
    private JButton methodtest;

    private Thread fishing = new Thread() {
        public void run() {
            while (true) {
                Zeichenfenster.warte(100);
                if (rod.isInWater()) {
                    int x = rdm.nextInt(20 - baitAmount);
                    if (x == 0) {
                        //int y = rdm.nextInt(20);
                        //if(y != 0) {
                            fishOnRod = true;
                            setSpaceColor(372, 3);
                            spaces[372].fill();
                            Zeichenfenster.warte(1000);
                            fishOnRod = false;
                            setSpaceColor(372, 1);
                            spaces[372].fill();
                        //}
                    }
                    //System.out.println("Warte auf Fisch...");
                }
            }
        }
    };

    private Thread autoFishing = new Thread() {
        public void run() {
            while (true) {
                Zeichenfenster.warte(100);
                while (isAutoFishing) {
                    Zeichenfenster.warte(100);
                    if (fishOnRod) {
                        moveFishingRod();
                        Zeichenfenster.warte(500);
                        if(isAutoFishing) {
                            moveFishingRod();
                        }
                    }
                }
            }
        }
    };

    public static void main(String[] args) {
        new Field();
    }

    public Field() {
        oTop = new Outwall(40, 0, 40, 840, 0);
        oRight = new Outwall(840, 40, 840, 40, 0);
        oBottom = new Outwall(0, 840, 40, 840, 0);
        oLeft = new Outwall(0, 0, 840, 40, 0);

        rod = new FishingRod();
        rodColor = 4;
        fishOnRod = false;
        caughtFishCount = 0;
        currentFishAmount = 0;
        soldFishCount = 0;
        balance = 0;
        baitAmount = 0;
        decorLevel = 0;

        autoFishingState = 0;
        isAutoFishing = false;
        unlockedAutoCheat = false;

        spaces = new Space[400];
        createSpaces();
        createImageDefault();
        createRodDefault();

        drawField();
        drawStats();

        fishing.start();
        autoFishing.start();

        moveButton = new JButton(" [Move the Rod] ");
        Zeichenfenster.gibFenster().komponenteHinzufuegen(moveButton, "unten");
        moveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(isAutoFishing) {
                    isAutoFishing = false;
                }
                moveFishingRod();
            }
        });
        sellButton = new JButton(" [Sell one fish] ");
        Zeichenfenster.gibFenster().komponenteHinzufuegen(sellButton, "unten");
        sellButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sellFish();
            }
        });
        sellAllButton = new JButton(" [Sell  all fish] ");
        Zeichenfenster.gibFenster().komponenteHinzufuegen(sellAllButton, "unten");
        sellAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sellAllFish();
            }
        });
        upgradeButton = new JButton(" [Get an upgrade] ");
        Zeichenfenster.gibFenster().komponenteHinzufuegen(upgradeButton, "unten");
        upgradeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buyUpgrade();
            }
        });
        colorButton = new JButton(" [Change color] ");
        Zeichenfenster.gibFenster().komponenteHinzufuegen(colorButton, "unten");
        colorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buyColor();
            }
        });
        decorButton = new JButton(" [Get decor] ");
        Zeichenfenster.gibFenster().komponenteHinzufuegen(decorButton, "unten");
        decorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buyDecor();
            }
        });
        autoButton = new JButton(" [AutoFishing] ");
        Zeichenfenster.gibFenster().komponenteHinzufuegen(autoButton, "unten");
        autoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(unlockedAutoCheat) {
                    performCheatSwitch();
                }
                else {
                    System.out.println("AutoFishing wurde noch nicht freigeschlalten");
                }
            }
        });

//        methodtest = new JButton(" [methodtest] ");
//        Zeichenfenster.gibFenster().komponenteHinzufuegen(methodtest, "unten");
//        methodtest.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//
//            }
//        });
    }

    public void startCheat() {
        if (!rod.isInWater()) {
            moveFishingRod();
        }
        isAutoFishing = true;
        autoFishingState = 1; //1 heisst der cheat laeuft
        System.out.println("Cheat wurde gestartet!");
    }

    public void stopCheat() {
        if (rod.isInWater()) {
            moveFishingRod();
        }
        isAutoFishing = false;
        autoFishingState = 0; //0 heisst der cheat lauft nicht
        System.out.println("Cheat wurde beendet!");
    }

    public void performCheatSwitch() {
        switch (autoFishingState) {
            case 0 -> startCheat();
            case 1 -> stopCheat();
        }
        //drawField();
        updateStats();
    }

    public void drawField() {
        oTop.fill();
        oRight.fill();
        oBottom.fill();
        oLeft.fill();
        fillSpaces();
    }

    public void drawLowerField() {
        fillLowerSpaces();
    }

    /**
     * Erstellt die Felder in weiss.
     */
    public void createSpaces() {
        int left = 40;
        int top = 40;
        int hight = 40;
        int broad = 40;
        int color = 8;
        int x = 0;
        for (int u = 0; u < 20; u++) {
            for (int i = 0; i < 20; i++) {
                spaces[x] = new Space(left, top, hight, broad, color);

                left = left + broad;
                x++;
            }
            left = 40;
            top = top + hight;
        }
    }

    public void fillSpaces() {
        for (int i = 0; i < spaces.length; i++) {
            spaces[i].fill();
        }
    }

    public void fillLowerSpaces() {
        for (int i = 140; i < spaces.length; i++) {
            spaces[i].fill();
        }
    }

    public void setSpaceColor(int numberOfSpace, int color) {
        spaces[numberOfSpace].setColor(color);
    }

    public void createImageDefault() {
        int color = 3;
        for (int i = 0; i < 125; i++) {
            setSpaceColor(i, color);
        }
        color = 2;
        for (int i = 125; i < 166; i++) {
            setSpaceColor(i, color);
        }
        color = 1;
        for (int i = 166; i < 400; i++) {
            setSpaceColor(i, color);
        }

        color = 2;
        int[] x = new int[]{170, 171, 172, 177, 178, 179, 180, 181, 182, 200, 320, 340, 341, 342, 360, 361, 362, 363, 380, 381, 382, 383, 384, 385, 386, 387, 388, 395, 396, 397, 398, 399, 378, 379};
        for (int i = 0; i < x.length; i++) {
            setSpaceColor(x[i], 2);
        }
    }

    public void createRodDefault() { //mit einem Image in FishingRod austauschen  -> Bild auf Hintergrund
        int color = rodColor;
        setSpaceColor(386, color);
        setSpaceColor(366, color);
        setSpaceColor(346, color);
        setSpaceColor(326, color);
        setSpaceColor(307, color);
        setSpaceColor(287, color);
        setSpaceColor(267, color);
        setSpaceColor(247, color);
        setSpaceColor(227, color);
        setSpaceColor(207, color);
        color = 7;
        setSpaceColor(228, color);
        setSpaceColor(248, color);
        setSpaceColor(269, color);
        setSpaceColor(289, color);
        setSpaceColor(309, color);
        setSpaceColor(329, color);
        color = 0;
        setSpaceColor(350, color);
    }

    public void deleteRodImage() { //mit einem Image in FishingRod austauschen  -> Bild auf Hintergrund
        if(!rod.isInWater()) {
            int color = 2; //gruen
            setSpaceColor(386, color);
            color = 1; //blau
            setSpaceColor(366, color);
            setSpaceColor(346, color);
            setSpaceColor(326, color);
            setSpaceColor(307, color);
            setSpaceColor(287, color);
            setSpaceColor(267, color);
            setSpaceColor(247, color);
            setSpaceColor(227, color);
            setSpaceColor(207, color);
            setSpaceColor(228, color);
            setSpaceColor(248, color);
            setSpaceColor(269, color);
            setSpaceColor(289, color);
            setSpaceColor(309, color);
            setSpaceColor(329, color);
            setSpaceColor(350, color);

            //fish etc
            int[] x = new int[] {234, 235, 236, 252, 253, 254, 255, 256, 257, 273, 274, 275, 276, 277, 292, 293, 294, 295, 296, 297, 314, 315, 316};
            for (int i = 0; i < x.length; i++) {
                setSpaceColor(x[i], 1);
            }
        }
        else {
            int color = 2;
            setSpaceColor(386, color);
            color = 1;
            setSpaceColor(366, color);
            setSpaceColor(346, color);
            setSpaceColor(327, color);
            setSpaceColor(307, color);
            setSpaceColor(288, color);
            setSpaceColor(268, color);
            setSpaceColor(249, color);
            setSpaceColor(270, color);
            setSpaceColor(290, color);
            setSpaceColor(311, color);
            setSpaceColor(331, color);
            setSpaceColor(351, color);

            setSpaceColor(372, 1); //Fish Preview
        }
    }

    public void moveFishingRod() {
        deleteRodImage();
        //createImageDefault();
        if (!rod.isInWater()) { //mit einem Image in FishingRod austauschen -> Bild auf Hintergrund
            int color = rodColor;
            setSpaceColor(386, color);
            setSpaceColor(366, color);
            setSpaceColor(346, color);
            setSpaceColor(327, color);
            setSpaceColor(307, color);
            setSpaceColor(288, color);
            setSpaceColor(268, color);
            setSpaceColor(249, color);
            color = 7;
            setSpaceColor(270, color);
            setSpaceColor(290, color);
            setSpaceColor(311, color);
            setSpaceColor(331, color);
            color = 0;
            setSpaceColor(351, color);
            rod.setState(true);
            drawLowerField();
            updateStats();
        }
        else {
            createRodDefault();
            rod.setState(false);
            if (!fishOnRod) {
                drawLowerField();
                updateStats();
            }
            else {
                int y = rdm.nextInt(10);
                if (y == 9) { //not a fish
                    caughtTrashCount++;
                    int[] x = new int[]{234, 235, 236, 253, 254, 256, 257, 273, 277, 293, 294, 296, 297, 314, 315, 316}; //-> change in deleteRodImage()
                    for (int j : x) {
                        setSpaceColor(j, 0);
                    }
                }
                else {//System.out.println("Du hast einen Fisch gefangen!");
                    caughtFishCount++;
                    currentFishAmount++;
                    if (!unlockedAutoCheat && caughtFishCount >= 3) {
                        unlockedAutoCheat = true;
                    }
                    switch (y) { //determine color of fish
                        case 0, 1, 2, 3, 4, 5, 6 -> drawAFish(3);
                        case 7, 8 -> drawAFish(4);
                        case 9 -> drawAFish(10);
                    }
                }
            }
        }
        drawLowerField();
        updateStats();
    }

    public void drawAFish(int color) {
        int[] x = new int[]{252, 254, 255, 256, 273, 274, 275, 277, 292, 294, 295, 296};
        for (int j : x) {
            setSpaceColor(j, color);
        }
        setSpaceColor(276, 0); //Fisheye
    }

    public void drawStats() {
        Zeichenfenster.gibFenster().zeichneText("Fische gefangen: " + caughtFishCount, 100, 100);
        Zeichenfenster.gibFenster().zeichneText("Aktuelle Anzahl an Fischen: " + currentFishAmount, 100, 130);
        Zeichenfenster.gibFenster().zeichneText("Anzahl an Münzen: " + balance, 100, 160);
        Zeichenfenster.gibFenster().zeichneText("Verkaufte Fische: " + soldFishCount, 100, 190);
        Zeichenfenster.gibFenster().zeichneText("Köderstärke: " + baitAmount, 500, 100);
        if(!unlockedAutoCheat) {
            Zeichenfenster.gibFenster().zeichneText("Autofishing: locked", 500, 130);
            Zeichenfenster.gibFenster().zeichneText("Autofishing: disabled", 500, 160);
        }
        else {
            Zeichenfenster.gibFenster().zeichneText("Autofishing: unlocked", 500, 130);
            if(isAutoFishing) {
                Zeichenfenster.gibFenster().zeichneText("Autofishing: enabled", 500, 160);
            }
            else {
                Zeichenfenster.gibFenster().zeichneText("Autofishing: disabled", 500, 160);
            }
        }
    }

    public void updateStats() { //causes lags (-> Zeichenfenster.gibFenster.loescheRechteck?)
        if(decorLevel == 0) {
            Zeichenfenster.gibFenster().fuelleRechteck(199, 90, 150, 13, 3); //Fische gefangen
            Zeichenfenster.gibFenster().fuelleRechteck(249, 120, 150, 13, 3); // Aktuelle Anzahl an Fischen
            Zeichenfenster.gibFenster().fuelleRechteck(201, 150, 150, 13, 3); // Anzahl an Münzen
            Zeichenfenster.gibFenster().fuelleRechteck(198, 180, 150, 13, 3); // Verkaufete Fische
            Zeichenfenster.gibFenster().fuelleRechteck(570, 90, 150, 13, 3); // Köderstärke
            Zeichenfenster.gibFenster().fuelleRechteck(565, 120, 150, 13, 3); // Autofishing locked
            Zeichenfenster.gibFenster().fuelleRechteck(565, 150, 150, 13, 3); // Autofishing disabled
            drawStats();
        }
        else {
            Zeichenfenster.gibFenster().fuelleRechteck(199, 90, 150, 13, 8); //Fische gefangen
            Zeichenfenster.gibFenster().fuelleRechteck(249, 120, 70, 13, 8); // Aktuelle Anzahl an Fischen
            Zeichenfenster.gibFenster().fuelleRechteck(201, 150, 119, 13, 8); // Anzahl an Münzen
            Zeichenfenster.gibFenster().fuelleRechteck(198, 180, 150, 13, 3); // Verkaufete Fische
            Zeichenfenster.gibFenster().fuelleRechteck(570, 90, 80, 13, 8); // Köderstärke
            Zeichenfenster.gibFenster().fuelleRechteck(565, 120, 80, 13, 8); // Autofishing locked
            Zeichenfenster.gibFenster().fuelleRechteck(565, 150, 80, 10, 8); // Autofishing disabled
            drawStats();
        }
    }

    public void sellFish() {
        if (currentFishAmount < 1) {
            System.out.println("Du hast keine Fische die du verkaufen kannst!");
        } else {
            currentFishAmount--;
            soldFishCount++;
            balance = balance + 5;
            System.out.println("Einen Fisch verkauft!");
            updateStats();
        }
    }
    public void sellAllFish() {
        if (currentFishAmount < 1) {
            System.out.println("Du hast keine Fische die du verkaufen kannst!");
        } else {
            soldFishCount = soldFishCount + currentFishAmount;
            balance = balance + currentFishAmount * 5;
            currentFishAmount = 0;
            System.out.println("Alle Fische verkauft!");
            updateStats();
        }
    }

    public void buyUpgrade() {
        if (balance >= 10) {
            if (baitAmount < 10) {
                balance = balance - 10;
                baitAmount++;
                updateStats();
                System.out.println("Neues Upgrade gekauft!");
            } else {
                System.out.println("Du hast schon alle Upgrades!");
            }
        } else {
            System.out.println("Du hast nicht genug Geld!");
        }
    }

    public void buyColor() {
        if (balance >= 5) {
            balance = balance - 5;
            switch (rodColor) {
                case 4 -> rodColor = 5;
                case 5 -> rodColor = 6;
                case 6 -> rodColor = 10;
                case 10 -> rodColor = 4;
            }
            if(!rod.isInWater()) {
                createRodDefault();
            }
            else {
                int color = rodColor;
                setSpaceColor(386, color);
                setSpaceColor(366, color);
                setSpaceColor(346, color);
                setSpaceColor(327, color);
                setSpaceColor(307, color);
                setSpaceColor(288, color);
                setSpaceColor(268, color);
                setSpaceColor(249, color);
                color = 7;
                setSpaceColor(270, color);
                setSpaceColor(290, color);
                setSpaceColor(311, color);
                setSpaceColor(331, color);
                color = 0;
                setSpaceColor(351, color);
            }
            drawLowerField();
            updateStats();
            System.out.println("Farbe gewechselt!");
        }
        else {
            System.out.println("Du hast nicht genug Geld!");
        }
    }

    public void buyDecor() {
        switch (decorLevel) {
            case 0: buyClouds(); break;
            case 1: buyDuck(); break;
        }
    }

    public void buyClouds() { //nr 8
        if(balance >= 10) {
            balance = balance - 10;

            setSpaceColor(4, 8); //cloud 1 //in den Hintergrund mit einfuegen
            setSpaceColor(5, 8);
            setSpaceColor(23, 8);
            setSpaceColor(24, 8);
            setSpaceColor(25, 8);
            setSpaceColor(26, 8);
            setSpaceColor(27, 8);
            setSpaceColor(44, 8);
            setSpaceColor(45, 8);
            setSpaceColor(46, 8);

            for(int i = 31; i < 36; i++) { //cloud 2
                setSpaceColor(i, 8);
                setSpaceColor(i+20, 8);
            }
            setSpaceColor(50, 8);
            setSpaceColor(72, 8);
            setSpaceColor(73, 8);

            decorLevel++;

            drawField();
            updateStats();
        }
        else {
            System.out.println("Du hast nicht genug Geld!");
        }
    }

    public void buyDuck() { //gelb 6, rot 4
        if(balance >= 15) {
            balance = balance - 15;

            for(int i = 222; i < 224; i++) {
                setSpaceColor(i, 6);
                setSpaceColor(i+20, 6);
            }
            for(int i = 262; i < 266; i++) {
                setSpaceColor(i, 6);
                setSpaceColor(i+20, 6);
            }
            setSpaceColor(241, 4);

            decorLevel++;

            drawLowerField();
            updateStats();
        }
        else {
            System.out.println("Du hast nicht genug Geld!");
        }
    }
}
