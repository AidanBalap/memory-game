package memory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.util.Duration;
import memory.om.Jeu;
import memory.om.Reponse;

public class MemoryController implements Initializable {
	
	private Stage fenetrePrincipale;
	private Parent parentScene;
	private Jeu currentGame;
	private String queuedCardId;
	private int gameScore;
	private char cardType = 't';
	
	private String[] valList;

	@FXML private GridPane gameGrid;
	@FXML private Label scoreCounter;
	@FXML private Label playsCounter;
	@FXML private Label pairsFoundCounter;
	@FXML private CheckMenuItem cheatMode;

	public void setFenetrePrincipale(Stage fenetrePrincipale) {
		this.fenetrePrincipale = fenetrePrincipale;
		this.fenetrePrincipale.setOnCloseRequest(event -> {
			event.consume();
			actQuitterApp();
		});
		
		this.fillValuesLists();
	}
	
	public void setScenePaneObj(BorderPane borderPane) {
		this.parentScene = borderPane;
	}
	
	private int askGameOptions() {
		TextInputDialog dialog = new TextInputDialog();

		dialog.setTitle(MemoryApp.locales.getString("dialog_newgame_title"));
		dialog.setHeaderText(MemoryApp.locales.getString("dialog_newgame_header"));
		dialog.setContentText(MemoryApp.locales.getString("dialog_newgame_contenttext"));

		Optional<String> result = dialog.showAndWait();
		
		while (true) {
			if (!result.isPresent()) {
			    return 0;
			}
			
			if (!result.isEmpty()) {
				try {
			        int nbPairs = Integer.parseInt( result.get() );
			        
			        if (2 <= nbPairs && nbPairs <= 12) {
			        	return nbPairs*2;
			        }
			    } catch( Exception e ) {
			    	result = dialog.showAndWait();
			    }
			}
			
			result = dialog.showAndWait();
		}
	}
	
	private void generateGameButtons(GridPane pfGameGrid, int pfNbColumns, int pfNbLines) {
		double btnHeight = ((pfGameGrid.getHeight()-30)/pfNbLines);
		double btnWidth = ((pfGameGrid.getWidth()-30)/pfNbColumns);
	
		for (int c=0; c<pfNbColumns; c++) {
			for (int l=0; l<pfNbLines; l++) {
				Button btn = new Button();
				String btnId = Integer.toString(c+(l*pfNbColumns));
				
				btn.setId("gameGridBtn"+ btnId);
				btn.setAlignment(Pos.CENTER);
				btn.setMinSize(btnWidth, btnHeight);
				this.setButtonState(btn, 'd');
				
				btn.setOnAction(event -> {
					actClickCart(event);
				});

				pfGameGrid.add(btn, c, l);
			}
		}
	}
	

	@FXML
	private void actClickCart(ActionEvent btnEvent) {
		if (this.currentGame.isPartieTerminee()) {
			this.finishGame();
			return;
		}
		
		Button btnClicked = (Button) btnEvent.getSource();
		int shortId = Integer.parseInt(btnClicked.getId().replace("gameGridBtn", ""));
		
		Reponse play = this.currentGame.jouer(shortId);
		
		if (play == Reponse.PREMIERE) {
			this.setButtonState(btnClicked, 'a');
			this.queuedCardId = btnClicked.getId();
			
		} else if (play == Reponse.GAGNE) {
			Button otherButton = (Button) parentScene.lookup("#"+ this.queuedCardId);
			
			this.setButtonState(otherButton, 'f');
			this.setButtonState(btnClicked, 'f');
			this.gameScore += 10;
			this.queuedCardId = null;
			
		} else if (play == Reponse.PERDU) {
			Button otherButton = (Button) parentScene.lookup("#"+ this.queuedCardId);
			this.gameScore -= 10;
			this.queuedCardId = null;
			
			PauseTransition pause = new PauseTransition(Duration.seconds(1));
		    pause.setOnFinished(event ->{
		    	this.setButtonState(btnClicked, 'd');
				this.setButtonState(otherButton, 'd');
		    });
		    
		    this.setButtonState(btnClicked, 'e');
		    this.setButtonState(otherButton, 'e');
		    pause.play();
			
		} else if (play == Reponse.ERREUR) {
			
		}
		
		this.updateCounters();
		
		if (this.currentGame.isPartieTerminee()) {
			this.finishGame();
		}
	}
	
	private void setButtonState(Button btn, char state) {
		String textVal = this.getValueId(btn.getId());
		String colorVal;
		
		btn.setDisable(true);
		
		if (state == 'd') { // default
			btn.setDisable(false);
			textVal = "O";
			colorVal = "(238,232,170)";
			
		} else if (state == 'a') { // active
			colorVal = "(238,232,170)";
			
		} else if (state == 'f') { // found
			colorVal = "(173,255,47)";
			
		} else { // error
			colorVal = "(255,69,0)";
		}
		
		if (this.cardType == 'c') {
			if (state != 'd') {
				colorVal = this.getValueId(btn.getId());
				
				if (state == 'f') {
					textVal = "✓";
				} else {
					textVal = "X";
				}
			}
		}
		
		btn.setText(textVal);
		btn.setStyle("-fx-background-color: rgb"+ colorVal +";");
	}
	
	private void finishGame() {
		Alert info = new Alert(AlertType.CONFIRMATION);
		info.setTitle(MemoryApp.locales.getString("dialog_gamefinish_title"));
		info.setHeaderText(MemoryApp.locales.getString("dialog_gamefinish_header"));
		
		Optional<ButtonType> response = info.showAndWait();

		if (response.orElse(null) == ButtonType.OK) {
			this.actNewGame();
		}
	}
	
	private void updateCounters() {
		this.pairsFoundCounter.setText(Integer.toString(this.currentGame.getNbCartesTrouvees()/2));
		this.playsCounter.setText(Integer.toString(this.currentGame.getNbCoupsJoues()/2));
		this.scoreCounter.setText(Integer.toString(this.gameScore));
	}
	
	/**
	 * 
	 * 
	 * MENU
	 * 
	 * 
	 */
	
	// GAME
	@FXML
	private void actNewGame() {
		int nbColumns;
		int nbLines;
		
		int nbCartes = this.askGameOptions();
		
		if (nbCartes == 0) {
			return;
		}
		
		if (nbCartes % 4 == 0) {
			nbColumns = 4;
		} else {
			nbColumns = 2;
		}
		
		nbLines = nbCartes / nbColumns;
		
		gameGrid.getChildren().clear();
		
		this.gameScore = 0;
		this.currentGame = new Jeu(nbCartes/2, cheatMode.isSelected());
		
		this.generateGameButtons(gameGrid, nbColumns, nbLines);
		this.updateCounters();
		
		this.fenetrePrincipale.show();
	}
	
	@FXML
	private void actClear() {
		gameGrid.getChildren().clear();
	}
	
	@FXML
	private void actQuitterApp() {
		Alert confirm = new Alert(AlertType.CONFIRMATION);
		confirm.setTitle(MemoryApp.locales.getString("dialog_quitapp_title"));
		confirm.setHeaderText(MemoryApp.locales.getString("dialog_quitapp_header"));
		Optional<ButtonType> response = confirm.showAndWait();

		if (response.orElse(null) == ButtonType.OK) {
			this.fenetrePrincipale.close();
		}
	}
	
	// VIEW
	@FXML
	private void actChangeCardType(ActionEvent btnEvent) {
		MenuItem btnClicked = (MenuItem) btnEvent.getSource();
		char cardType = btnClicked.getId().charAt(0);
		this.cardType = cardType;
		this.fillValuesLists();
	}
	
	// LANG
	@FXML
	private void actChangeLang(ActionEvent btnEvent) {
		MenuItem btnClicked = (MenuItem) btnEvent.getSource();
		String langId = btnClicked.getId().replace("languageBtn_", "");
		
		Alert confirm = new Alert(AlertType.CONFIRMATION);
		confirm.setTitle(MemoryApp.locales.getString("dialog_changelang_title"));
		confirm.setHeaderText(MemoryApp.locales.getString("dialog_changelang_header"));
		Optional<ButtonType> response = confirm.showAndWait();

		if (response.orElse(null) == ButtonType.OK) {
			MemoryApp.currentLang = langId;
			MemoryApp.startApp();
		}
	}
	
	// SETTINGS
	@FXML
	private void actHelp() {
		Alert info = new Alert(AlertType.INFORMATION);
		info.setTitle(MemoryApp.locales.getString("dialog_help_title"));
		info.setHeaderText(MemoryApp.locales.getString("dialog_help_header"));
		info.showAndWait();
	}
	
	@FXML
	private void actAbout() {
		Alert info = new Alert(AlertType.INFORMATION);
		info.setTitle(MemoryApp.locales.getString("dialog_about_title"));
		info.setHeaderText(MemoryApp.locales.getString("dialog_about_header"));
		info.showAndWait();
	}
	// END MENU
	

	// VIEW VALUES
	public String getValueId(String btnId) {
		int shortId = Integer.parseInt(btnId.replace("gameGridBtn", ""));
		return this.valList[this.currentGame.getCarteValeur(shortId)];
	}
	
	public void fillValuesLists() {
		String[] valL = new String[12];
		
		if (this.cardType == 't') {
			for (int i=0; i<valL.length; i++) {
				valL[i] = Integer.toString(i);
			}
			
		} else if (this.cardType == 'e') {
			valL[0] = "ʘ‿ʘ";
	        valL[1] = "ʕᵔᴥᵔʔ";
	        valL[2] = "┬─┬⃰͡ (ᵔᵕᵔ͜ )";
	        valL[3] = "(ツ)";
	        valL[4] = "ʕ •`ᴥ•´ʔ";
	        valL[5] = "☜(⌒▽⌒)☞";
	        valL[6] = "ᵒᴥᵒ#";
	        valL[7] = "(╯°□°）╯";
	        valL[8] = "( ͡° ͜ʖ ͡°)";
	        valL[9] = "ಠ‿ಠ";
	        valL[10] = "ಥ﹏ಥ";
	        valL[11] = "°‿‿°";
			
		} else if (this.cardType == 'c') {
			valL[0] = "(0,0,210)";
	        valL[1] = "(210,210,0)";
	        valL[2] = "(0,210,210)";
	        valL[3] = "(210,0,210)";
	        valL[4] = "(192,192,192)";
	        valL[5] = "(128,128,128)";
	        valL[6] = "(128,0,0)";
	        valL[7] = "(128,128,0)";
	        valL[8] = "(0,128,0)";
	        valL[9] = "(128,0,128)";
	        valL[10] = "(0,128,128)";
	        valL[11] = "(0,0,128)";
	        
		}
		
		this.valList = valL;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {}
}
