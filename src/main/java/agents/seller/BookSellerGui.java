package agents.seller;

import agents.ConsumerAgent;
import agents.buyer.BookBuyerAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BookSellerGui extends Application {

    protected BookSellerAgent bookSellerAgent;
    protected ObservableList<String> observableListData;
    protected AgentContainer container;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        startContainer();
        primaryStage.setTitle("Book Seller Gui");
        BorderPane borderPane = new BorderPane();

        HBox hBox1 = new HBox();
        hBox1.setPadding(new Insets(10));
        hBox1.setSpacing(10);
        Label labelSellerName = new Label("Seller Name :");
        TextField textFieldSellerName = new TextField();
        Button buttonSeller = new Button("Deploy Seller Agent");
        hBox1.getChildren().addAll(labelSellerName, textFieldSellerName, buttonSeller);
        borderPane.setTop(hBox1);
        observableListData = FXCollections.observableArrayList();
        ListView<String> listViewMessages = new ListView<String>(observableListData);
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);
        vBox.getChildren().add(listViewMessages);
        borderPane.setCenter(vBox);

        buttonSeller.setOnAction(evt -> {
            try {
                String SellerName = textFieldSellerName.getText();
                AgentController consumerController = container.createNewAgent(
                        SellerName,
                        BookSellerAgent.class.getName(),
                        new Object[]{this});
                consumerController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        });
        Scene scene = new Scene(borderPane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startContainer() throws Exception {
        Runtime runtime = Runtime.instance();
        ProfileImpl profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        container = runtime.createAgentContainer(profile);
    }

    public void logMessage(ACLMessage aclMessage) {
        Platform.runLater(() -> {
            observableListData.add(aclMessage.getSender().getName() +
                    " => " + aclMessage.getContent());
        });
    }
}
