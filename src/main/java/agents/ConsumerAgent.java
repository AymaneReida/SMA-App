package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ControllerException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsumerAgent extends GuiAgent {

    protected ConsumerContainer consumerContainer;

    @Override
    protected void setup() {
        String bookName = null;
        if (this.getArguments().length == 1) {
            consumerContainer = (ConsumerContainer) this.getArguments()[0];
            consumerContainer.consumerAgent = this;
        }
        System.out.println("Initialisation de l'agent " + this.getAID().getName());
        System.out.println("I am trying to buy the book " + bookName);

        ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
        addBehaviour(parallelBehaviour);

        /* addBehaviour(new Behaviour() {
            private int counter = 0;

            @Override
            public void action() {
                System.out.println("-------------------------------------");
                System.out.println("Step " + counter);
                System.out.println("-------------------------------------");
                ++counter;
            }

            @Override
            public boolean done() {
                return (counter == 10);
            }
        }); */

        /* addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("One Shot Behaviour");
            }
        }); */

        parallelBehaviour.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("One Shot Behaviour");
            }
        });

        /* addBehaviour(new CyclicBehaviour() {
            private int counter = 0;

            @Override
            public void action() {
                System.out.println("Counter => " + counter);
                ++counter;
            }
        }); */

        /* addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                System.out.println("Tick ");
                System.out.println(myAgent.getAID().getLocalName());
            }
        }); */
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm");
        Date date = null;
        try {
            date = dateFormat.parse("25/05/2021:19:58");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        /* addBehaviour(new WakerBehaviour(this, date) {
            @Override
            protected void onWake() {
                System.out.println("Waker Behaviour ...");
            }
        }); */

        parallelBehaviour.addSubBehaviour(new WakerBehaviour(this, date) {
            @Override
            protected void onWake() {
                System.out.println("Waker Behaviour ...");
            }
        });

        parallelBehaviour.addSubBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate messageTemplate =
                        MessageTemplate.and(
                                MessageTemplate.MatchPerformative(ACLMessage.CFP),
                                MessageTemplate.MatchLanguage("FR")
                        );
                // ACLMessage aclMessage = receive(messageTemplate);
                ACLMessage aclMessage = receive();
                if (aclMessage != null) {
                    System.out.println("Sender : " + aclMessage.getSender().getName());
                    System.out.println("Content : " + aclMessage.getContent());
                    System.out.println("Speech Act : " + ACLMessage.getPerformative(aclMessage.getPerformative()));

                    /* ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
                    reply.addReceiver(aclMessage.getSender());
                    reply.setContent("Price = 900");
                    send(reply); */

                    consumerContainer.logMessage(aclMessage);
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
