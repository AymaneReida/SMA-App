package agents.buyer;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ControllerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BookBuyerAgent extends GuiAgent {

    protected BookBuyerGui gui;
    protected AID[] sellerAgents;

    @Override
    protected void setup() {
        if (this.getArguments().length == 1) {
            gui = (BookBuyerGui) this.getArguments()[0];
            gui.bookBuyerAgent = this;
        }

        ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
        addBehaviour(parallelBehaviour);

        parallelBehaviour.addSubBehaviour(new CyclicBehaviour() {
            private int counter = 0;
            private List<ACLMessage> replies = new ArrayList<ACLMessage>();

            @Override
            public void action() {
                MessageTemplate messageTemplate = MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                        MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
                                MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE),
                                        MessageTemplate.MatchPerformative(ACLMessage.REFUSE))
                        ));
                ACLMessage aclMessage = receive(messageTemplate);
                if (aclMessage != null) {
                    System.out.println("Sender : " + aclMessage.getSender().getName());
                    System.out.println("Content : " + aclMessage.getContent());
                    System.out.println("Speech Act : " + ACLMessage.getPerformative(aclMessage.getPerformative()));

                    // ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                    // reply.addReceiver(aclMessage.getSender());
                    /* ACLMessage reply = aclMessage.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Trying to buy => " + aclMessage.getContent());
                    send(reply); */

                    switch (aclMessage.getPerformative()) {
                        case ACLMessage.REQUEST:
                            gui.logMessage(aclMessage);
                            ACLMessage aclMessage2 = new ACLMessage(ACLMessage.CFP);
                            aclMessage2.setContent(aclMessage.getContent());
                            for (AID aid : sellerAgents) {
                                aclMessage2.addReceiver(aid);
                            }
                            send(aclMessage2);
                            break;
                        case ACLMessage.PROPOSE:
                            gui.logMessage(aclMessage);
                            ++counter;
                            replies.add(aclMessage);
                            if (counter == sellerAgents.length) {
                                ACLMessage bestOffer = replies.get(0);
                                double min = Double.parseDouble(bestOffer.getContent());

                                for (ACLMessage offer : replies) {
                                    double price = Double.parseDouble(offer.getContent());
                                    if (price < min) {
                                        bestOffer = offer;
                                        min = price;
                                    }
                                }

                                ACLMessage aclMessageAccept = bestOffer.createReply();
                                aclMessageAccept.setContent(String.valueOf(min));
                                aclMessageAccept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                send(aclMessageAccept);
                            }
                            break;
                        case ACLMessage.AGREE:
                            ACLMessage aclMessage3 = new ACLMessage(ACLMessage.CONFIRM);
                            aclMessage3.setContent(aclMessage.getContent());
                            aclMessage3.addReceiver(new AID("consumer", AID.ISLOCALNAME));
                            send(aclMessage3);
                            break;
                        case ACLMessage.REFUSE:
                            break;
                        default:
                            break;
                    }
                } else {
                    System.out.println("Bloc ...");
                    block();
                }
            }
        });

        parallelBehaviour.addSubBehaviour(new TickerBehaviour(this, 6000) {
            @Override
            protected void onTick() {
                try {
                    // Update the list of seller agents
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription serviceDescription = new ServiceDescription();
                    serviceDescription.setType("book-selling");
                    template.addServices(serviceDescription);
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    sellerAgents = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        sellerAgents[i] = result[i].getName();
                    }
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
