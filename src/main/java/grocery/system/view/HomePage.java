package grocery.system.view;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class HomePage {

    public HomePage(){




        //above here you implement the buttons and whatever designs you want

        VBox pageLayout = new VBox(); // this will order the page layout
        pageLayout.setSpacing(30);
        BorderPane root = new BorderPane(pageLayout); //the box will be put into the scene
        Scene pageScene = new Scene(root, 1280,960); //which is initialized here

    }

}
