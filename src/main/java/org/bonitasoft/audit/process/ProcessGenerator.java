package org.bonitasoft.audit.process;

import static org.bonitasoft.audit.process.OrgaGenerator.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.test.BuildTestUtil;

public class ProcessGenerator {


    private  static  ProcessGenerator processGenerator = null;
    private  List<DesignProcessDefinition> processDefinitions = new ArrayList<>();


    public  ProcessGenerator generate() throws InvalidProcessDefinitionException {
        processDefinitions.add(buildSimpleProcessDefinition(Role.EMPLOYEE.name() + " Actor"));
        processDefinitions.add(buildProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition(Role.MEMBER + " Actor"));
     /*   processDefinitions.addAll(
                BuildTestUtil.buildNbProcessDefinitionWithHumanAndAutomatic(50, Arrays.asList("step1", "step2", "Step3"), Arrays.asList(true, true, false)));*/
        return processGenerator;
    }

    public static ProcessGenerator createInstance() {
        if (processGenerator == null) {
            processGenerator = new ProcessGenerator();
        }
        return processGenerator;
    }

    private  DesignProcessDefinition buildSimpleProcessDefinition(String actorName) throws InvalidProcessDefinitionException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("simpleProcess", "1.0");
        processBuilder.addActor(actorName);
        processBuilder.addStartEvent("StartProcess").addUserTask("Task1", "Employee actor").
                addContract().addInput("input1", Type.TEXT, "").addEndEvent("EndProcess").addTransition("StartProcess", "Task1");
        return processBuilder.getProcess();
    }

    private  DesignProcessDefinition buildProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition(String actorName) throws InvalidProcessDefinitionException {
        ProcessDefinitionBuilder processBuilder = (new ProcessDefinitionBuilder()).createNewInstance("ProcessName", "1.0");
        processBuilder.addActor(actorName).addDescription("Coding all-night-long");
        return processBuilder.addAutomaticTask("step1").addUserTask("step2", "Employee actor").addUserTask("step3", "Employee actor").addUserTask("step4", "Employee actor").addTransition("step1", "step2").addTransition("step1", "step3").addTransition("step1", "step4").getProcess();
    }

    public  void toBarFiles(File folder) throws Exception {
        int i = 0;
        for (DesignProcessDefinition definition : processDefinitions) {
            final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
            businessArchiveBuilder.setActorMapping(ActorMappingGenerator.buildActorMapping());
            businessArchiveBuilder.setProcessDefinition(definition);
            String fileName = "process_" + i++ + ".bar";
            final BusinessArchive businessArchive = businessArchiveBuilder.done();
            BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, new File(folder, fileName));
        }

    }


}
