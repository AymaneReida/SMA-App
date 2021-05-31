package agents.buyer;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BookBuyerGui extends Application {

    protected BookBuyerAgent bookBuyerAgent;
    ListView<String> listViewMessages;
    ObservableList<String> observableListData;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        startContainer();
        primaryStage.setTitle("Book Buyer Gui");
        BorderPane borderPane = new BorderPane();
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10));
        observableListData = FXCollections.observableArrayList();
        listViewMessages = new ListView<String>(observableListData);
        vBox.getChildren().add(listViewMessages);
        borderPane.setCenter(vBox);
        Scene scene = new Scene(borderPane, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startContainer() throws Exception {
        Runtime runtime = Runtime.instance();
        ProfileImpl profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        AgentContainer agentContainer = runtime.createAgentContainer(profile);

        AgentController consumerController =
                agentContainer.createNewAgent(
                        "BookBuyerAgent",
                        BookBuyerAgent.class.getName(),
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
