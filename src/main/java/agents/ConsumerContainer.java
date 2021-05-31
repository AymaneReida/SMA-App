package agents;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
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

public class ConsumerContainer extends Application {

    protected ConsumerAgent consumerAgent;
    ObservableList<String> observableListData;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        startContainer();
        primaryStage.setTitle("Consumer Container");
        BorderPane borderPane = new BorderPane();

        HBox hBox1 = new HBox();
        hBox1.setPadding(new Insets(10));
        hBox1.setSpacing(10);
        Label labelBookName = new Label("Book Name :");
        TextField textFieldBookName = new TextField();
        Button buttonOK = new Button("OK");
        hBox1.getChildren().addAll(labelBookName, textFieldBookName, buttonOK);
        borderPane.setTop(hBox1);
        observableListData = FXCollections.observableArrayList();
        ListView<String> listViewMessages = new ListView<String>(observableListData);
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);
        vBox.getChildren().add(listViewMessages);
        borderPane.setCenter(vBox);

        buttonOK.setOnAction(evt -> {
            String bookName = textFieldBookName.getText();
            GuiEvent guiEvent = new GuiEvent(this, 1);
            guiEvent.addParameter(bookName);
            consumerAgent.onGuiEvent(guiEvent);
        });
        Scene scene = new Scene(borderPane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startContainer() throws Exception {
        Runtime runtime = Runtime.instance();
        ProfileImpl profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        AgentContainer container = runtime.createAgentContainer(profile);

        AgentController consumerController =
                container.createNewAgent(
                        "consumer",
                        "agents.ConsumerAgent",
                        new Object[]{this});
        consumerController.start();
    }

    public void logMessage(ACLMessage aclMessage) {
        Platform.runLater(() -> {
            observableListData.add(aclMessage.getSender().getName() +
                    " => " + aclMessage.getContent());
        });
    }
}
