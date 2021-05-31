package agents.buyer;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;

public class BookBuyerAgent extends GuiAgent {

    protected BookBuyerGui gui;

    @Override
    protected void setup() {
        if (this.getArguments().length == 1) {
            gui = (BookBuyerGui) this.getArguments()[0];
            gui.bookBuyerAgent = this;
        }

        ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
        addBehaviour(parallelBehaviour);

        parallelBehaviour.addSubBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage aclMessage = receive();
                if (aclMessage != null) {
                    System.out.println("Sender : " + aclMessage.getSender().getName());
                    System.out.println("Content : " + aclMessage.getContent());
                    System.out.println("Speech Act : " + ACLMessage.getPerformative(aclMessage.getPerformative()));
                    gui.logMessage(aclMessage);

                    // ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                    // reply.addReceiver(aclMessage.getSender());
                    ACLMessage reply = aclMessage.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Trying to buy => " + aclMessage.getContent());
                    send(reply);
                } else {
                    System.out.println("Bloc ...");
                    block();
                }
            }
        });
    }

    @Override
    protected void beforeMove() {
        try {
            System.out.println("Before Migration From " + this.getContainerController().getContainerName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void afterMove() {
        try {
            System.out.println("After Migration To " + this.getContainerController().getContainerName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("I am going to die ...");
    }

    @Override
    protected void onGuiEvent(GuiEvent evt) {
        if (evt.getType() == 1) {
            String bookName = (String) evt.getParameter(0);
            System.out.println("Agent => " + getAID().getName() + " => " + bookName);
            ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
            aclMessage.setContent(bookName);
            aclMessage.addReceiver(new AID("BookBuyerAgent", AID.ISLOCALNAME));
            send(aclMessage);
        }
    }
}
