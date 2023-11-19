package memory;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MemoryApp extends Application {
	
	private static Stage pmStage;
	public static String currentLang = "fr";
	public static ResourceBundle locales;

	@Override
	public void start(Stage primaryStage) throws Exception {
		pmStage = primaryStage;
		
		startApp();
	}
	
	public static void startApp() {
		try {
			ResourceBundle bundle = ResourceBundle.getBundle("locales.Memory", new Locale(currentLang));
			FXMLLoader loader = new FXMLLoader(MemoryApp.class.getResource("view/MemoryMain.fxml"), bundle);
			BorderPane borderPane = loader.load();  // peut lever IOEXception
			MemoryController ctrl  = loader.getController();
			locales=bundle;
			
			ctrl.setFenetrePrincipale(pmStage);
			ctrl.setScenePaneObj(borderPane);
	
			Scene scene = new Scene(borderPane);
			pmStage.setScene(scene);
			pmStage.setTitle("Memory Game - Aidan Balasch");
			pmStage.show();
			
		} catch (IOException e) {
			System.exit(1);
		}
	}
	
	public static void main2(String[] args) {
		Application.launch(args);
	}

}
