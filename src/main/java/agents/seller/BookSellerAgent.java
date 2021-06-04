package agents.seller;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ControllerException;

import java.util.Random;

public class BookSellerAgent extends GuiAgent {

    protected BookSellerGui gui;

    @Override
    protected void setup() {
        if (this.getArguments().length == 1) {
            gui = (BookSellerGui) this.getArguments()[0];
            gui.bookSellerAgent = this;
        }
        ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
        addBehaviour(parallelBehaviour);

        parallelBehaviour.addSubBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate messageTemplate = MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.CFP),
                        MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));
                ACLMessage aclMessage = receive(messageTemplate);
                if (aclMessage != null) {
                    System.out.println("Sender : " + aclMessage.getSender().getName());
                    System.out.println("Content : " + aclMessage.getContent());
                    System.out.println("Speech Act : " + ACLMessage.getPerformative(aclMessage.getPerformative()));

                    switch (aclMessage.getPerformative()) {
                        case ACLMessage.CFP:
                            gui.logMessage(aclMessage);
                            ACLMessage reply = aclMessage.createReply();
                            reply.setPerformative(ACLMessage.PROPOSE);
                            reply.setContent(String.valueOf(500 + new Random().nextInt(1000)));
                            send(reply);
                            break;
                        case ACLMessage.ACCEPT_PROPOSAL:
                            ACLMessage aclMessage2 = aclMessage.createReply();
                            aclMessage2.setContent(aclMessage.getContent());
                            aclMessage2.setPerformative(ACLMessage.AGREE);
                            send(aclMessage2);
                            break;
                        default:
                            break;
                    }

                    /* ACLMessage reply = aclMessage.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Trying to buy => " + aclMessage.getContent());
                    send(reply); */
                } else {
                    System.out.println("Bloc ...");
                    block();
                }
            }
        });

        parallelBehaviour.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("Publication du service dans Directory Facilitator ...");
                DFAgentDescription agentDescription = new DFAgentDescription();
                agentDescription.setName(getAID());
                ServiceDescription serviceDescription = new ServiceDescription();
                serviceDescription.setType("book-selling");
                serviceDescription.setName("book-trading");
                agentDescription.addServices(serviceDescription);
                try {
                    DFService.register(myAgent, agentDescription);
                } catch (FIPAException e) {
                    e.printStackTrace();
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
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onGuiEvent(GuiEvent evt) {
        if (evt.getType() == 1) {
            String bookName = (String) evt.getParameter(0);
            System.out.println("Agent => " + getAID().getName() + " => " + bookName);
            ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
            aclMessage.setContent(bookName);
            aclMessage.addReceiver(new AID("BookSellerAgent", AID.ISLOCALNAME));
            send(aclMessage);
        }
    }
}
