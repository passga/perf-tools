package org.bonitasoft.audit.process;

import static org.bonitasoft.audit.process.OrgaGenerator.Role;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import org.bonitasoft.engine.bpm.actor.impl.ActorDefinitionImpl;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.test.BuildTestUtil;

public class ProcessGenerator {


    private  static  ProcessGenerator processGenerator = null;
    private static File folder;
    private  List<DesignProcessDefinition> processDefinitions = new ArrayList<>();


    public static ProcessGenerator generate(File folder) {
        ProcessGenerator.folder = folder;
        if (processGenerator == null) {
            processGenerator = new ProcessGenerator();
        }
        return processGenerator;
    }

    public void buildSimpleProcessDefinition() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("simpleProcessTest", "1.0");
        ActorMapping actorMapping = ActorMappingGenerator.crateInstance().withEmployeeActor().generateActors(2).build();

        String employeeActor = "Employee actor";
        processDefinitionBuilder.addStartEvent("StartProcess").addUserTask("Task1", employeeActor).
                addContract().addInput("input1", Type.TEXT, "").addEndEvent("EndProcess").addTransition("StartProcess", "Task1").addTransition("Task1", "EndProcess");
        processDefinitionBuilder.setActorInitiator(employeeActor);
        DesignProcessDefinition process = processDefinitionBuilder.getProcess();
        for (Actor actor : actorMapping.getActors()) {
            ActorDefinitionImpl actorDefinition = new ActorDefinitionImpl(actor.getName());
            actorDefinition.setDescription(actor.getDescription());
            process.getActorsList().add(actorDefinition);
        }

        toBarFiles(process, actorMapping, folder, false);

    }

    public void buildSimpleProcessDefinitionWithOnlyAutomaticTask() throws Exception {
        ActorMapping actorMapping = ActorMappingGenerator.crateInstance().withEmployeeActor().build();

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("SimpleProcessDefinitionWithOnlyAutomaticTask", "1.0");

        processDefinitionBuilder.addStartEvent("StartProcess").addAutomaticTask("Step1").addEndEvent("endProcess").addTransition("StartProcess", "Step1").addTransition("Step1", "endProcess");

        processDefinitionBuilder.setActorInitiator("Employee actor");
        DesignProcessDefinition process = processDefinitionBuilder.getProcess();
        for (Actor actor : actorMapping.getActors()) {
            ActorDefinitionImpl actorDefinition = new ActorDefinitionImpl(actor.getName());
            actorDefinition.setDescription(actor.getDescription());
            process.getActorsList().add(actorDefinition);
        }
        toBarFiles(process, actorMapping, folder, false);

    }

    public void buildSimpleProcessWithStringIndex() throws Exception {
        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("processWithStringIndex", "2.0");
        ActorMapping actorMapping = ActorMappingGenerator.crateInstance().withEmployeeActor().build();

        processDefinitionBuilder.addStartEvent("StartProcess").addUserTask("Task1", "Employee actor").addContract().addInput("input1", Type.TEXT, "").addUserTask("Task2", "Employee actor").addUserTask("Task3", "Employee actor").addEndEvent("EndProcess").addTransition("StartProcess", "Task1").addTransition("Task1", "Task2").addTransition("Task2", "Task3").addTransition("Task3", "EndProcess");

        processDefinitionBuilder.setStringIndex(1, "name", new ExpressionBuilder().createConstantStringExpression("process With String Index"));
        processDefinitionBuilder.setStringIndex(2, "label", new ExpressionBuilder().createConstantStringExpression("test"));
        processDefinitionBuilder.setStringIndex(3, "composant", new ExpressionBuilder().createConstantStringExpression("audit"));
        processDefinitionBuilder.setStringIndex(4, "version", new ExpressionBuilder().createConstantStringExpression("2"));
        processDefinitionBuilder.setStringIndex(5, "actor", new ExpressionBuilder().createConstantStringExpression("employe actor"));

        processDefinitionBuilder.setActorInitiator("Employee actor");

        DesignProcessDefinition process = processDefinitionBuilder.getProcess();
        for (Actor actor : actorMapping.getActors()) {
            ActorDefinitionImpl actorDefinition = new ActorDefinitionImpl(actor.getName());
            actorDefinition.setDescription(actor.getDescription());
            process.getActorsList().add(actorDefinition);
        }


        toBarFiles(process, actorMapping, folder, false);
    }

    public void buildProcessWithConnector() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithDependencies", "1.0");
        ActorMapping actorMapping = ActorMappingGenerator.crateInstance().withEmployeeActor().build();
        processDefinitionBuilder.setActorInitiator("Employee actor");
        processDefinitionBuilder.addStartEvent("StartProcess").addAutomaticTask("Task1").addTransition("StartProcess", "Task1");
        DesignProcessDefinition process = processDefinitionBuilder.getProcess();
        for (Actor actor : actorMapping.getActors()) {
            ActorDefinitionImpl actorDefinition = new ActorDefinitionImpl(actor.getName());
            actorDefinition.setDescription(actor.getDescription());
            process.getActorsList().add(actorDefinition);
        }
        toBarFiles(process, actorMapping, folder, true);

    }

    public void buildMassProcessDefinition(int nbProcess) throws Exception {
        ActorMapping actorMapping = ActorMappingGenerator.crateInstance().withEmployeeActor().withInitiatorActor().build();


        List<DesignProcessDefinition> designProcessDefinitions = buildNbProcessDefinitionWithHumanAndAutomatic("ProcessWithComplexActorMapping",
                nbProcess, "Initiator", Arrays.asList("Step1", "Step2", "Step3"), Arrays.asList(true, true, false));

        for (DesignProcessDefinition designProcessDefinition :
                designProcessDefinitions) {
            for (Actor actor : actorMapping.getActors()) {
                ActorDefinitionImpl actorDefinition = new ActorDefinitionImpl(actor.getName());
                actorDefinition.setDescription(actor.getDescription());
                designProcessDefinition.getActorsList().add(actorDefinition);
            }

            toBarFiles(designProcessDefinition, actorMapping, folder, false);
        }
    }


    public static List<DesignProcessDefinition> buildNbProcessDefinitionWithHumanAndAutomatic(String processName,
                                                                                              int nbProcess, String actor, List<String> stepNames, List<Boolean> isHuman) throws
            InvalidProcessDefinitionException {
        List<DesignProcessDefinition> processDefinitions = new ArrayList();

        for (int i = 0; i < nbProcess; ++i) {
            if (i >= 0 && i < 10) {
                processName = processName + "0";
            }

            DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(processName + i, "1.0" + i, stepNames, isHuman, actor, true);
            processDefinitions.add(designProcessDefinition);
        }

        return processDefinitions;
    }


    public void toBarFiles(DesignProcessDefinition definition, ActorMapping actorMapping, File folder,
                           boolean withDepencies) throws Exception {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.setActorMapping(actorMapping);
        businessArchiveBuilder.setProcessDefinition(definition);
        String fileName = "process_" + definition.getName() + ".bar";
        if (withDepencies) {
            for (int i = 0; i < 50; i++) {
                byte[] randomBytes = generateRandomFile();
                businessArchiveBuilder.addClasspathResource(new BarResource("jar" + i + ".jar", randomBytes));
            }
        }

        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchiveBuilder.done(), new File(folder, fileName));


    }

    private byte[] generateRandomFile() {
        SecureRandom random = new SecureRandom();
        int byteArraySize = Math.abs(random.nextInt());
        byte[] randomBytes = new byte[1 * 1000 * 1000];
        random.nextBytes(randomBytes);
        return randomBytes;
    }


}


